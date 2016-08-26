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

import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.connector.runner.JobRunner;
import com.mango.mif.connector.runner.JobRunnerDriver;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerWithOutputException;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.domain.Precondition;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.ExecutionResponseBuilder;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.MIFProperties;

/**
 * A default implementation of a Job Runner that uses a JobRunnerDriver to control the execution
 */
public class DefaultJobRunner implements JobRunner {
    /**
     * Logger
     */
    private final static Logger LOG = Logger.getLogger(DefaultJobRunner.class);
    /**
     * job id
     */
    private String jobId;
    /**
     * JMS template used to publish result messages to the dispatcher
     */
    private JmsTemplate jmsTemplate;

    /**
     * Job Result queue
     */
    private String resultDestination;
    /**
     * Result message
     */
    private String resultMessage;
    /**
     * Exception that have been thrown in call method
     */
    private Exception jobRunnerProcessingException;
    /**
     * Summary message
     */
    private String summaryMessage;

    private JobRunnerDriver driver;

    private JobManagementService jobManagementService;
    
    private String unavailableSummaryMessage;

    @Override
    public void startProcessing() {

        ObjectStateUpdater<Job> updater = new ObjectStateUpdater<Job>();
        updater.addCommand(new Function<Job,Job>() {
            @Override
            public Job apply(Job job) {
                if(JobStatus.SCHEDULED.equals(job.getJobStatus())) {
                    job.setJobStatus(JobStatus.PROCESSING);
                }
                return job;
            }
        });
        jobManagementService.update(jobId, updater);

        driver.start();
        LOG.debug("Job Runner for request " + jobId + " has been started.");
    }

    /**
     * Requests a cancellation from the driver, if there are any exceptions thrown during cancellation, the exception is available from
     * the driver. It is expected that if cancellation fails the job is then moved to FAILED state and failed state handler is executed.
     */
    @Override
    public synchronized void cancelProcessing() {
        LOG.debug("Triggering cancellation event for job " + jobId + ".");
        driver.cancel();
    }

    public void setSummaryMessage(String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }

    @Override
    public JobMessage call() {
        JobMessage jobMessage = new JobMessage();
        try {
            main();
        } catch(Exception e) {
            LOG.error("Error executing Job Runner", e);
            jobRunnerProcessingException = e;
        } finally {
            resultMessage = prepareResponse();
            publishResultsMessage();
            LOG.trace(String.format("*** Execution Metrics : {Processing.3} " +
                    "Publishing results for jobId [%s]", jobId));
        }
        return jobMessage;
    }

    /**
     * Encapsulates processing from call method
     * @throws InterruptedException
     */
    void main() throws InterruptedException {
        Preconditions.checkNotNull(jobId, "Job not set.");
        Preconditions.checkNotNull(driver, "Driver not set.");
        Preconditions.checkNotNull(resultDestination, "Result Destination Queue not set.");

        LOG.trace(String.format("*** Execution Metrics : {Processing.1} " +
                "Starting process for jobId [%s]", jobId));

        startProcessing();

        // TODO currently JobRunner actively waits for state machine to reach final state
        // SCXMLDriver in complete method should check if the current status is final and notify job runner
        // to do this make the job runner an observer of the driver or use guava's EventBus

        int secondsCount = 0;
        while(isJobRunning()) {
            LOG.trace(String.format("*** Execution Metrics : {Process.Running.%d} " +
                    "Process running for jobId [%s]", secondsCount, jobId));
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            secondsCount++;
        }
        LOG.trace(String.format("*** Execution Metrics : {Processing.2} " +
                "Process finished running for jobId [%s]", jobId));
    }

    /**
     *
     * @return true if job is being processed and is not cancelled
     */
    private synchronized boolean isJobRunning() {
        return !driver.isInFinalState();
    }

    /**
     * Publishes result message to the job result queue
     *
     * @param resultMessage
     */
    protected void publishResultsMessage() {
        jobManagementService.update(jobId, createUpdater(jmsTemplate, resultDestination, resultMessage));
    }
    /**
     * Creates updater that ensures that the job is in correct state after submitting an execution response
     * @param jmsTemplate
     * @param resultDestination
     * @param resultMessage
     * @return
     */
    ObjectStateUpdater<Job> createUpdater(final JmsTemplate jmsTemplate, final String resultDestination, final String resultMessage) {
        ObjectStateUpdater<Job> updater = new ObjectStateUpdater<Job>();
        updater.addCondition(new Precondition<Job>() {
            @Override
            public void check(Job object) {
                jmsTemplate.send(resultDestination, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage message = session.createTextMessage();
                        message.setText(resultMessage);
                        return message;
                    }
                });
            }

        }).addCommand(new Function<Job,Job>() {
            @Override
            public Job apply(Job job) {
                job.setJobStatus(JobStatus.PROCESSING_FINISHED);
                return job;
            }

        });

        return updater;
    }

    /**
     * Prepare the Response from Runner.
     * @return
     */
    protected String prepareResponse() {
        JobStatus status = getJobStatus();

        ExecutionResponseBuilder builder = new ExecutionResponseBuilder();
        builder.setRequestId(jobId);
        builder.setStatus(status.name());

        populateSummaryMessage(builder);

        populateResult(builder);
        populateResultDetails(builder);

        String responseString = "";

        try {
            responseString = builder.getExecutionResponseMsg();
        } catch (JAXBException e) {
            LOG.error("Error when marshalling response message", e);
            responseString = prepareMarshallableMessage(builder);
        }

        return responseString;
    }

    /**
     * Creates a message and sets result and detailed result to marshallable values
     * @param builder
     * @return
     */
    private String prepareMarshallableMessage(ExecutionResponseBuilder builder) {
        builder.setResult("ERROR when creating response message");
        builder.setResultDetails("could not marshall response message");
        try {
            return builder.getExecutionResponseMsg();
        } catch (JAXBException e) {
            throw new RuntimeException("Could not marshall a 'safe' response message", e);
        }
    }

    private void populateSummaryMessage(ExecutionResponseBuilder builder) {
        if (summaryMessage != null) {
            builder.setSummaryMessage(summaryMessage);
        } else {
            builder.setSummaryMessage(getUnavailableSummaryMessage());
        }
    }

    private void populateResultDetails(ExecutionResponseBuilder responseBuilder) {
        StringBuilder builder = new StringBuilder();
        if (this.getDriver().getException() != null) {
            builder.append(prepareDetailedResultMessage(getDriver().getException()));
        }
        if (this.jobRunnerProcessingException != null) {
            builder.append("Job Runner exception\n");
            builder.append(prepareDetailedResultMessage(jobRunnerProcessingException));
        }
        responseBuilder.setResult(builder.toString());
    }

    private void populateResult(ExecutionResponseBuilder responseBuilder) {
        StringBuilder builder = new StringBuilder();
        if (this.getDriver().getException() != null) {
            builder.append(prepareResultMessage(getDriver().getException()));
        }
        if (this.jobRunnerProcessingException != null) {
            builder.append("Job Runner exception\n");
            builder.append(prepareResultMessage(jobRunnerProcessingException));
        }
        responseBuilder.setResult(builder.toString());
    }

    /**
     * maps the driver status to JobStatus
     * @return
     */
    private JobStatus getJobStatus() {
        DriverProcessingStatus processingStatus = DriverProcessingStatus.valueOf(driver.getProcessingStatus().getMessage());
        if (jobRunnerProcessingException != null || this.getDriver().getException()!=null) {
            return JobStatus.FAILED;
        }
        switch (processingStatus) {
        case CANCELLED:
            return JobStatus.CANCELLED;
        case FAILED:
            return JobStatus.FAILED;
        case FINISHED:
            return JobStatus.COMPLETED;
        default:
            return JobStatus.PROCESSING;
        }
    }

    /**
     * Builds an error message that will be passed to the client
     *
     * @return  error message that will replace the standard error
     */
    String prepareResultMessage(Exception exception) {
        return prepareMessage(exception, true);
    }

    /**
     * Builds an error message that will be passed to the client.
     *
     * @return error message that will replace the standard error
     */
    String prepareDetailedResultMessage(Exception exception) {
        return prepareMessage(exception, false);
    }

    /**
     * Prepare an error message from the specified exception (and all its causes)
     * that will be passed back to the client and, to all intents and purposes,
     * be used in place of the standard error of whatever command it is we're
     * reporting on.  See comments regarding NAVDEV-6268.
     *
     * @param exception The exception we're dissecting.
     * @param breakOnExecutionException Give up if one of the causes is an execution
     * exception
     * @return A pretty string which will replace the standard error
     */
    String prepareMessage(Exception exception, boolean breakOnExecutionException) {
        StringBuilder errorMsgBuilder = new StringBuilder();
        Throwable tmp = exception;

        // NAVDEV-6268: Many complaints have been made about how MIF is changing the
        // output and error from the command (NONMEM, bootstrap, etc.) and the changed
        // output therefore ends up differing from the contents of MIF.stdout and
        // MIF.stderr.  Since the output of this command, will eventually end up
        // overwriting the standard error, we try to preserve, as best we can, the
        // standard error without changing it.
        //
        StateHandlerWithOutputException sve = extractWithOutputException(exception);
        if (sve != null) {
            // For a validation exception, life is easy since the original error
            // stream is bundled up inside it.
            errorMsgBuilder.append(sve.getMessage() + "\n");
            if (sve.hasErrors()) {
                errorMsgBuilder.append(sve.getStderr());
            }
        } else {
            // I think that what NAVDEV-6268 is saying is that seeing our stack trace
            // in the standard error is unacceptable.  Thus we throw away the stack trace
            // information.  I sure hope we've logged it somewhere.  I obviously don't
            // want to log it here.
            //
            while (tmp != null) {
                errorMsgBuilder.append(tmp.getMessage());
                if (breakOnExecutionException && tmp instanceof ExecutionException) {
                    break;
                }
                errorMsgBuilder.append("\n");
                tmp = tmp.getCause();
            }
        }
        return errorMsgBuilder.toString();
    }

    /**
     * @param exception The exception we're currently dealing with.
     * @return the exception, or the exception's cause, which is a StateHandlerValidationException, or null if there is none
     */
    private StateHandlerWithOutputException extractWithOutputException(Exception exception) {
        Throwable tmp = exception;
        while (tmp != null) {
            if (tmp instanceof StateHandlerWithOutputException) {
                return (StateHandlerWithOutputException) tmp;
            }
            tmp = tmp.getCause();
        }
        return null;
    }

    /**
     * @return the resultMsg
     */
    public String getResultMessage() {
        return resultMessage;
    }

    @Required
    public void setResultDestination(String resultDestination) {
        this.resultDestination = resultDestination;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setDriver(JobRunnerDriver driver) {
        this.driver = driver;
    }

    public JobRunnerDriver getDriver() {
        return driver;
    }

    @Override
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public String toString() {
        String str = jobId;
        return str;
    }


    @Required
    public void setJobManagementService(JobManagementService jobManagementService) {
        this.jobManagementService = jobManagementService;
    }


    public JobManagementService getJobManagementService() {
        return jobManagementService;
    }

    @Required
    public void setUnavailableSummaryMessage(String unavailableSummaryMessage) {
        this.unavailableSummaryMessage = unavailableSummaryMessage;
    }
    
    
    public String getUnavailableSummaryMessage() {
        return unavailableSummaryMessage;
    }
}
