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

import javax.xml.bind.JAXBException;

import com.google.common.base.Function;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.status.JobDetailedStatusBuilder;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.template.JobAwareFreemarkerTemplateCommandBuilder;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.MIFProperties;

/**
 * Generic shell based command builder that uses replaceable invoker to execute commands built with the command builder.
 * It also uses a replaceable results handler, to parse the outputs and reason on the next steps of the execution
 * 
 */
public class BaseGenericShellBasedStateHandler extends DefaultStateHandler {
    /**
     * Command builder
     */
    protected JobAwareFreemarkerTemplateCommandBuilder commandBuilder;
    /**
     * Result handler
     */
    protected JobProcessingAwareInvokerResultHandler invokerResultHandler;
    /**
     * Summary message provider
     */
    protected JobDetailedStatusBuilder detailedStatusBuilder;

    
    /**
     * @param state
     * @param exitEvent
     */
    public BaseGenericShellBasedStateHandler(String state) {
        super(state);
    }

    /**
     * @param state
     * @param exitEvent
     */
    public BaseGenericShellBasedStateHandler(String state, String exitEvent) {
        super(state, exitEvent);
    }

    /**
     * @param commandBuiler the commandBuiler to set
     */
    public void setCommandBuilder(JobAwareFreemarkerTemplateCommandBuilder commandBuilder) {
        this.commandBuilder = commandBuilder;
    }

    /**
     * @param invokerResultHandler the invokerResultHandler to set
     */
    public void setInvokerResultHandler(JobProcessingAwareInvokerResultHandler invokerResultHandler) {
        this.invokerResultHandler = invokerResultHandler;
    }

    public JobAwareFreemarkerTemplateCommandBuilder getCommandBuilder() {
        return commandBuilder;
    }

    public JobProcessingAwareInvokerResultHandler getInvokerResultHandler() {
        return invokerResultHandler;
    }

    protected Invoker getInvoker() throws ExecutionException {
        return getJob().getInvoker();
    }


    public void setDetailedStatusBuilder(JobDetailedStatusBuilder detailedStatusBuilder) {
        this.detailedStatusBuilder = detailedStatusBuilder;
    }

    public JobDetailedStatusBuilder getDetailedStatusBuilder() {
        return detailedStatusBuilder;
    }

    /**
     * A method that is responsible for handling creation of the state summary message
     * @throws ExecutionException 
     * @throws JAXBException 
     * @throws MIFException 
     */
    protected void summarizeState() throws JAXBException, ExecutionException, MIFException {
        if (detailedStatusBuilder != null) {
            detailedStatusBuilder.setJob(getJob());
            DetailedStatus detailedStatus = detailedStatusBuilder.getDetailedStatus();
            getJobRunner().setSummaryMessage(detailedStatus.getSummary());
            final String detailedStatusMarshalled = detailedStatus.asString();
            LOG.debug("Detailed status generated for job " + getJob().getJobId() + ".\n" + detailedStatusMarshalled);
            getJobUpdater().addCommand(new Function<Job,Job>() {

                @Override
                public Job apply(Job job) {
                    job.setDetailedStatusMsg(detailedStatusMarshalled);
                    return job;
                }
            });
        } else {
            try {
                if (getJob().getDetailedStatus() == null) {
                    setDefaultSummaryOnTheJob(MIFProperties.getInstance().getProperty("connector.startupSummaryMessage"));
                }
            } catch (Exception e) {
                LOG.error("summarizeState caught exception while generating the default summary: ", e);
                setDefaultSummaryOnTheJob(MIFProperties.getInstance().getProperty("connector.unavailableSummaryMessage"));
            }
        }
    }
    
    @Override
    protected void doPreprocessing() throws StateHandlerException {
        super.doPreprocessing();
        setJob(getJobManagementService().saveJobRunnerState(getJobRunner().getJobId(), JobRunnerState.byStateName(handledState)));
    }
    
    private void setDefaultSummaryOnTheJob(String summaryMessage) {
        DetailedStatus detailedStatus = new DetailedStatus();
        detailedStatus.setRequestId(getJob().getJobId());
        detailedStatus.setSummary(summaryMessage);
        try {
            final String detailedStatusMsg = detailedStatus.asString();
            LOG.debug("Detailed status generated for job " + getJob().getJobId() + ".\n" + detailedStatusMsg);
            getJobUpdater().addCommand(new Function<Job,Job>() {

                @Override
                public Job apply(Job job) {
                    job.setDetailedStatusMsg(detailedStatusMsg);
                    return job;
				}
			});
        } catch (JAXBException e) {
            LOG.error("Error when generating default summary", e);
        }
    }

    @Override
    protected void doPostProcessing() throws StateHandlerException {
        super.doPostProcessing();
        try {
            summarizeState();
        } catch (Exception e) {
            LOG.error("Could not generate detailed status for job " + getJob().getJobId() + ".", e);
            try {
                if(getJob().getDetailedStatus()==null) {
                    setDefaultSummaryOnTheJob(MIFProperties.getInstance().getProperty("connector.unavailableSummaryMessage"));
                }
            } catch (MIFException e1) {
                LOG.error("Error retrieving the current detailed status", e1);
                setDefaultSummaryOnTheJob(MIFProperties.getInstance().getProperty("connector.unavailableSummaryMessage"));
            }
        }
    }
    
}
