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

package com.mango.mif.connector.runner.impl.statehandler;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.template.JobAwareFreemarkerTemplateCommandBuilder;

/**
 * The Generic Shell Based Prepare (State) Handler has the following responsibilities:
 * <br>- Use a template to create the script that will submit our stuff to the grid
 * <br>- Expand the prepareHandler.ftl template and run the prepareHandler.sh script
 * files need to be copied back to the request folder.
 */
public class ShellBasedPrepareHandler extends BaseGenericShellBasedStateHandler {

    private String submitScriptName = "MIF-submitter.sh";

    // As we are effectively expanding two templates - one for the "grid submit host"
    // and one for prepareHandler.ftl, we need two command builders (the other is called
    // "commandBuilder" and is in the parent class.  Both are job aware.
    //
    private JobAwareFreemarkerTemplateCommandBuilder submitScriptBuilder;

    /**
     * @param state
     */
    public ShellBasedPrepareHandler(String state) {
        super(state);
    }

    /**
     * @param state
     * @param exitEvent
     */
    public ShellBasedPrepareHandler(String state, String exitEvent) {
        super(state, exitEvent);
    }

    /**
     * Generate the script, run prepareHandler.
     */
    @Override
    protected void doProcessing() throws StateHandlerException {

        try {
            Preconditions.checkNotNull(commandBuilder, "Command builder not set");
            Preconditions.checkNotNull(submitScriptBuilder, "The grid submit script builder cannot be null at this point");
            Preconditions.checkNotNull(getJobRunner(), "The job runner cannot be null");
            Preconditions.checkNotNull(getInvoker(), "The job runner's invoker is not set");
            Preconditions.checkNotNull(invokerResultHandler, "Invoker result handler not set");

            // STEP 1.
            // Hopefully the grid submit script builder can get everything it needs from the job,
            // as everything else (paths to shell scripts) can be injected via spring.
            Invoker invoker = getInvoker();
            Job job = getJob();
            commandBuilder.setJob(job);
            String command = commandBuilder.getCommand();

            Stopwatch watch = new Stopwatch().start();
            InvokerResult results = invoker.execute(command);
            LOG.info("PrepareHandler (COPY TO GRIDSHARE) took: " + watch.elapsedTime(TimeUnit.SECONDS));

            if (InvokerHelper.failed(results)) {
                LOG.error("Failed command: " + command);
                LOG.error("exit status: " + InvokerHelper.exitStatus(results));
                LOG.error("stdout: " + InvokerHelper.output(results));
                LOG.error("stderr: " + InvokerHelper.errors(results));
                throw new StateHandlerException("ShellBasedPrepareHandler bad exit status "
                              + InvokerHelper.exitStatus(results) 
                              + " from handling script ("
                              + InvokerHelper.errors(results)
                              + "). "
                              + results.getOutputStream());
            }

            // STEP 2.
            submitScriptBuilder.setJob(getJob());

            String contents = submitScriptBuilder.getCommand();
            InvokerHelper invokerHelper = new InvokerHelper(invoker);
            File submitScript = new File(getJob().getJobMifHiddenDir(), submitScriptName);
            if (!invokerHelper.createFileFromContents(contents, submitScript.getAbsolutePath(), 0755)) {
                throw new StateHandlerException("Failed to create submit file \""
                        + submitScript.getAbsolutePath()
                        + "\". See logs for details");
            }

        } catch (StateHandlerException she) {
            throw she;
        } catch (Exception e) {
            LOG.error(e);
            throw new StateHandlerException("GenericShellBasedPrepareHandler processing failed: " + e.getMessage(), e);
        }
    }
    
    public JobAwareFreemarkerTemplateCommandBuilder getGridSubmitScriptBuilder() {
        return submitScriptBuilder;
    }

    public void setGridSubmitScriptBuilder(JobAwareFreemarkerTemplateCommandBuilder gridScriptBuilder) {
        this.submitScriptBuilder = gridScriptBuilder;
    }

    public String getSubmitScriptName() {
        return submitScriptName;
    }

    public void setSubmitScriptName(String submitScriptName) {
        this.submitScriptName = submitScriptName;
    }
}
