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
package com.mango.mif.core.exec.ce;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.kahadb.util.ByteArrayInputStream;
import org.apache.log4j.Logger;

import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;

/**
 * Invoke a command in a Commons Exec sort of way.
 */
public class CeInvoker implements Invoker {

    private static final Logger LOG = Logger.getLogger(CeInvoker.class);

    @Override
    public InvokerResult execute(String command) throws ExecutionException {
        return execute(command, null);
    }

    @Override
    public InvokerResult execute(String command, String input) throws ExecutionException {
        InvokerResult results = null;
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            PumpStreamHandler psh;

            if (input != null) {
                ByteArrayInputStream stdin = new ByteArrayInputStream(input.getBytes("UTF8"));
                psh = new PumpStreamHandler(stdout, stderr, stdin);
            } else {
                psh = new PumpStreamHandler(stdout, stderr);
            }

            File tempScriptFile = null;
            try {
                tempScriptFile = File.createTempFile(Long.toString(System.currentTimeMillis()), determineShellScriptFileExtension());
                tempScriptFile.setExecutable(true);
                FileUtils.writeStringToFile(tempScriptFile, command);

                final CommandLine commandLine = new CommandLine(tempScriptFile);
                final DefaultExecutor exec = new DefaultExecutor();

                // We need this step, otherwise CE throws an exception when a command exits with any non zero value.
                exec.setExitValues(null);
                exec.setStreamHandler(psh);

                int exitCode = 0;
                try {
                    LOG.trace("Commons Exec command: " + commandLine);
                    exitCode = exec.execute(commandLine);
                } catch (Exception e) {
                    // Jonathan says "don't log and throw", but I've seen too many exceptions not logged to chance it.
                    String errorMessage = String.format("Exception using Commons Exec to execute: '%s'", command);
                    LOG.error(errorMessage, e);
                    throw new ExecutionException(errorMessage, e);
                }

                results = new CeInvokerResult(command, stdout.toString(), stderr.toString(), exitCode);

                LOG.trace("Commons Exec stdout: " + stdout);
                LOG.trace("Commons Exec stderr: " + stderr);

            } finally {
                if (tempScriptFile != null) {
                    FileUtils.deleteQuietly(tempScriptFile);
                }
            }

        } catch (Exception e) {
            throw new ExecutionException("Caught exception using commons exec to execute \"" + command + "\"", e);
        }

        return results;
    }
    
    /**
     * On Windows, the temporary batch file has to have the extension ".bat" in order for Windows to
     * recognise it as a batch file and thus be able to execute it.
     * <p>
     * On Linux/Unix the corresponding temporary script file can have any extension but ".sh" is the convention.
     * <p>
     * @return the OS-appropriate file extension as described above
     */
    private static String determineShellScriptFileExtension() {
    	if (SystemUtils.IS_OS_WINDOWS) {
    		return ".bat";
    	}
    	return ".sh";
    }

}
