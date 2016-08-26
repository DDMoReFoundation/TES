/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
package com.mango.mif.core.exec.jsch;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.invoker.InvokerParameters;

/**
 * The JSCH Invoker class lets us execute things in a JSCH way.
 * We need only two things, username and password, plus a whole bunch of optional stuff which is all hidden in JschParameters.
 */
public class JschInvoker implements Invoker {

    private static final Logger logger = Logger.getLogger(JschInvoker.class);

    private final JschParameters parameters;

    public JschInvoker(JschParameters parameters) {
        this.parameters = (JschParameters) parameters.cloneParameters();
    }

    @Override
    public InvokerResult execute(String commandLine) throws ExecutionException {
        return execute(commandLine, null);
    }

    @Override
    public InvokerResult execute(String commandLine, String stdin) throws ExecutionException {
        Preconditions.checkNotNull(parameters, "the parameters cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(parameters.getUserName()), "The username must have been set by this point");
        Preconditions.checkArgument(StringUtils.isNotBlank(parameters.getClearTextPassword()), "The clear text password must have been set by this point");

        JschSessionPool pool = new JschSessionPool();
        JschSession session = pool.get(parameters);
        Preconditions.checkNotNull(session, "The session pool returned a null session!");

        try {
            return execute(session, commandLine, stdin);
        } catch (Exception e) {
            String text = String.format("Exception %s using JSCH to execute \"%s\" with parameters \"%s\"", e.getClass().getName(), commandLine, parameters);
            if (stdin != null) {
                text = String.format("%s and input %s", text, stdin);
            }
            logger.error(text, e);
            throw new ExecutionException(text, e);
        } finally {
            if (session != null) {
                logger.debug("JschInvoker.execute returning session " + session);
                pool.returnObject(parameters, session);
            }
        }
    }

    /**
     * Execute a command.  We need to save the text somewhere, this is because if we don't we hit
     * "Arg list too long" errors (see Mantis ticket 10227).  So, we basically save the command
     * into a hidden file somewhere, then execute that.
     *
     * "Somewhere" is a bit of a problem too.  Initially this was into the user's home directory,
     * but unfortunately at Merck, there isn't enough space assigned to the device on which the
     * home directories live.  As a quick fix, this was moved to /tmp.  This is a bad idea, because
     * /tmp might get cleaned out while the command is executing.
     *
     * Determining which shell is to be used to execute the command is also a bit of a laugh as only
     * the incoming text knows this (via the "hash bang" line at the top).  If this is absent, then
     * the Bourne Shell will be used.
     *
     * Once execution is complete, we delete the file.
     *
     * @param commandLine The command to execute.
     * @param stdin The standard input for that command, which may be null.
     * @return The result of the command execution
     * @throws ExecutionException If the command fails really badly, or if the universe explodes etc.
     */
    private InvokerResult execute(JschSession session, String commandLine, String stdin) throws Exception {

        // Break the incoming command into "lumps" of this many characters.
        final int MAX_LENGTH = 5000;

        InvokerResult results = null;

        // newline terminate commandLine if not already
        //
        if (!commandLine.endsWith("\n")) {
            commandLine = commandLine + "\n";
        }

        logger.debug("JschInvoker.execute (session: " + session + " ) command: " + commandLine);
        if (stdin != null) {
            logger.debug("input: " + stdin);
        }

        // Step 1 is to save the script to a temporary file.  See comments above.  Remember that if we
        // use a relative path, we will automatically be in the user's home directory.  That's just the
        // way JSCH works.
        //
        // The next problem (see MIFDEV-533) is that there is an upper limit upon how much input we can
        // shove down the pipe at JSCH in one go.  The limit is probably around 32768 bytes, but you have
        // to remember we're dealing with UTF-8 here which can take up to 6 bytes per character.  Thus
        // the actual limit is around 5500 characters give or take (take, actually).  So to be absolutely
        // safe, we break the string into chunks of 5000 characters and assemble those remotely using
        // "cat".
        //
        final String scriptPath = generateScriptPath();
        try {
            // As discussed above, loop here breaking the input into lumps of 5000 characters at a time.
            //
            
            check(session, "touch " + scriptPath, StringUtils.EMPTY);

            for (String part : Splitter.fixedLength(MAX_LENGTH).split(commandLine)) {
                check(session, "cat >> " + scriptPath, part);
            }
            
            check(session, "chmod +x " + scriptPath, StringUtils.EMPTY);

            session.setLastCommand(commandLine);

            logger.debug("About to execute command:\n" + commandLine);

            results = session.run(scriptPath, stdin);

        } finally {
            // Remove the script file.
            try {
                session.run("rm -f " + scriptPath);
            } catch (Exception e) {
                logger.error("Caught exception while removing script file \"" + scriptPath + "\"", e);
            }
        }
        return results;
    }

    /**
     * What we do here is figure where the script is going to be written to in order to be
     * executed.  This is different based on the settings of the JschParameters which can
     * specify the script directory.  If this is blank, it is created in the user's current
     * directory, which because of the way JSCH works, will be their home directory.
     * <p>
     * Explicitly don't use {@link File} here since obtaining an absolute path from this would
     * give you a path relevant to the local machine rather than the remote machine.
     * <p>
     * @return The full path of the script file if the script home was specified; or just the
     * filename, prefixed with "./" to ensure that the script file can be executed if the
     * current directory is not on the Unix path, if no script home was specified.
     */
    private String generateScriptPath() {

        // The leading dot business on the filename makes the file hidden in Unix
        final String name = ".Jsch-" + Long.toString(System.currentTimeMillis()) + ".sh";

        if (StringUtils.isBlank(parameters.getScriptDirectory())) {
            return "./" + name;
        }

        return parameters.getScriptDirectory() + "/" + name;
    }

    /**
     * Check if the execution of the command succeeds and if it does not, complain by throwing an
     * execution exception.
     *
     * @param command The command to execute
     * @param input Any input to the command to execute (it may be null).
     */
    private void check(JschSession session, String command, String input) throws ExecutionException {
        InvokerResult invokerResult = session.run(command, input);

        if (InvokerHelper.failed(invokerResult)) {
            String text = String.format("Failure during creation of script file. Command was \"%s\", error text is: \"%s\"",
                    command,
                    InvokerHelper.errors(invokerResult));

            // Log and throw.  Damn.  Sorry.
            logger.error(text);
            throw new ExecutionException(text);
        }
    }

    public InvokerParameters getParameters() {
        return parameters;
    }

}
