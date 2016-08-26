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
package com.mango.mif.connector.runner.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.mango.mif.connector.runner.impl.statehandler.JobProcessingAwareInvokerResultHandler;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.template.JobAwareFreemarkerTemplateCommandBuilder;
import com.mango.mif.core.services.JobManagementService;

/**
 * A default implementation of SCXML processing cancellation handler. It builds a command using command builder, executes it and then it passes the result to the result handler.
 * It does not update Job status and job status should not be updated in the cancellation handler.
 */
public class SCXMLProcessingCancellationHandlerImpl implements SCXMLProcessingCancellationHandler {

    private final static Logger LOG = Logger.getLogger(SCXMLProcessingCancellationHandlerImpl.class);
    private JobManagementService jobManagementService;
    private Job job;
    private DefaultJobRunner jobRunner;
    private JobAwareFreemarkerTemplateCommandBuilder commandBuilder;
    private JobProcessingAwareInvokerResultHandler invokerResultHandler;

    @Override
    public void execute() throws ExecutionException {
        setJob(getJobManagementService().getJob(getJobRunner().getJobId()));
        String command = null;
        try {
            commandBuilder.setJob(getJob());
            command = commandBuilder.getCommand();
        } catch (ExecutionException e) {
            throw new ExecutionException("Could not generate command.", e);
        }

        if (StringUtils.isEmpty(command )) {
            throw new ExecutionException("Empty command scheduled for execution.");
        }

        LOG.debug("Command that will be executed:" + command);
        InvokerResult result = null;

        try {
            result = getInvoker().execute(command);
        } catch (ExecutionException e) {
            throw new ExecutionException("Could not execute command.", e);
        }

        invokerResultHandler.setJob(getJob());

        invokerResultHandler.handle(result);
    }

    protected Invoker getInvoker() throws ExecutionException {
        return getJob().getInvoker();
    }

    @Required
    public void setCommandBuilder(JobAwareFreemarkerTemplateCommandBuilder commandBuilder) {
        this.commandBuilder = commandBuilder;
    }

    @Required
    public JobAwareFreemarkerTemplateCommandBuilder getCommandBuilder() {
        return commandBuilder;
    }

    public void setInvokerResultHandler(JobProcessingAwareInvokerResultHandler invokerResultHandler) {
        this.invokerResultHandler = invokerResultHandler;
    }

    @Required
    public JobProcessingAwareInvokerResultHandler getInvokerResultHandler() {
        return invokerResultHandler;
    }

    protected Job getJob() {
        return job;
    }

    protected void setJob(Job job) {
        this.job = job;
    }

    public void setJobRunner(DefaultJobRunner jobRunner) {
        this.jobRunner = jobRunner;
    }


    public DefaultJobRunner getJobRunner() {
        return jobRunner;
    }

    public JobManagementService getJobManagementService() {
        return jobManagementService;
    }

    @Required
    public void setJobManagementService(JobManagementService jobManagementService) {
        this.jobManagementService = jobManagementService;
    }

}
