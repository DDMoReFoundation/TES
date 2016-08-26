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

package com.mango.mif.connector.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.ConnectorExceptions;
import com.mango.mif.connector.JobRuntimeMetricsProvider;
import com.mango.mif.connector.runner.JobRunner;
import com.mango.mif.connector.runner.JobRunnerFactory;
import com.mango.mif.connector.runner.status.JobDetailedStatusBuilder;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.domain.Precondition;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.messaging.MessagingHelper;
import com.mango.mif.core.messaging.RejectedExecutionRequestConsumer;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.ExecutionResponseBuilder;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.TextMessageCreator;

/**
 * An implementation of a Connector that for each valid consumed request creates a job object and delegates its execution to a {@link JobRunner}.
 * 
 * New instances of JobRunner are created using a JobRunnerFactory.
 */
public class ConnectorWithJobRunners extends CommandExecutionConnector {

    private static final Logger LOG = Logger.getLogger(ConnectorWithJobRunners.class);
    
    private String connectorId;
    
    private String responseQueue;
    private String requestQueue;
    private String cancellationQueue;
    private String internalQueue;

    private JobRunnerFactory jobRunnerFactory;
    private ConcurrentMap<String, JobRunner> jobRunnerRegistry = new ConcurrentHashMap<String, JobRunner>();
    private AsyncTaskExecutor taskExecutor;

    private JmsTemplate jmsTemplate;
    private JobManagementService jobManagementService;

    private JobDetailedStatusBuilder jobCancelledDetailedStatusBuilder;

    private JobDetailedStatusBuilder jobRejectedDetailedStatusBuilder;

    private JobRuntimeMetricsProvider jobRuntimeMetricsProvider;

    /**
     * @return the unique ID string of this Connector
     */
	public String getConnectorId() {
	    return connectorId;
    }

	/**
	 * @param connectorId - the unique ID string of this Connector
	 */
	@Required
	public void setConnectorId(final String connectorId) {
	    this.connectorId = connectorId;
    }

    @Override
    public void doFailureRecovery() {

        Stopwatch watch = new Stopwatch();
        watch.start();

        LOG.info(String.format("Job Recovery ( %s ) : Starting check for unfinished Connector jobs.  Querying database...", getConnectorId()));
        List<Job> uncompletedJobs = jobManagementService.getUncompletedConnectorJobs(getConnectorId());
        Iterable<Job> runningJobs = Iterables.filter(uncompletedJobs, new Predicate<Job>() {

            @Override
            public boolean apply(Job job) {
                return job.getJobStatus().equals(JobStatus.PROCESSING);
            }
        });
        Iterable<Job> scheduledJobs = Iterables.filter(uncompletedJobs, new Predicate<Job>() {

            @Override
            public boolean apply(Job job) {
                return job.getJobStatus().equals(JobStatus.SCHEDULED);
            }
        });

        LOG.info(String.format("Job Recovery ( %s ) : Found %d jobs", getConnectorId(), uncompletedJobs.size()));
        int noOfRescheduledJobs = submitJobRunnersForJobs(runningJobs);
        noOfRescheduledJobs += submitJobRunnersForJobs(scheduledJobs);

        // rest of the uncompleted jobs is redelivered by JMS

        LOG.info(String.format("Job Recovery ( %s ) : Rescheduled %d jobs of total %d ", getConnectorId(), noOfRescheduledJobs,
            uncompletedJobs.size()));

        long timeTaken = watch.elapsedMillis();
        LOG.info(String.format("Job Recovery ( %s ) took %d (ms) [ %d (secs) ]", getConnectorId(), timeTaken,
            TimeUnit.MILLISECONDS.toSeconds(timeTaken)));
        watch.stop();
    }

    /**
     * Schedules bulk of jobs
     * @param jobs
     */
    private int submitJobRunnersForJobs(Iterable<Job> jobs) {
        JobRunner runner = null;
        int counter = 0;
        for (Job job : jobs) {
            runner = jobRunnerFactory.createJobRunner(job);
            taskExecutor.submit(runner);
            jobRunnerRegistry.put(job.getJobId(), runner);
            counter++;
            LOG.info(String.format("Job Recovery ( %s ) :  Re-submitted persisted job with id %s, state %s.", getConnectorId(), job.getJobId(),
                job.getJobStatus()));
        }
        return counter;
    }

    @Override
    public synchronized void onMessage(Message message) {
        Preconditions.checkNotNull(message, "Received null message");
        TextMessage msg = (TextMessage) message;
        String receivedText = null;
        try {
            receivedText = msg.getText();
        } catch (JMSException e) {
            throw new RuntimeException(ConnectorExceptions.INVALID_REQUEST_MESSAGE.getMessage());
        }
        Preconditions.checkArgument(StringUtils.isNotEmpty(receivedText), "Received empty request message");
        LOG.debug(String.format("Received the message from client %s", receivedText));

        ExecutionRequest executionRequest = MessagingHelper.parseExecutionRequest(receivedText);

        LOG.info(String.format("*** Execution Metrics : {Scheduling and Preparation.1} Received the "
            + "execution request message with request id [%s]", executionRequest.getRequestId()));

        executeJobRequest(executionRequest);
    }

    /**
     * Creates a job and schedules a job runner for the given execution request
     * 
     * @param executionRequest
     */
    void executeJobRequest(ExecutionRequest executionRequest) {

        Job job = null;
        if (!jobExists(executionRequest.getRequestId())) {
            job = createJobForRequest(executionRequest);
        } else {
            LOG.warn(String.format("Job with ID %s already exists.", executionRequest.getRequestId()));
            job = jobManagementService.getJob(executionRequest.getRequestId());
        }

        if (!jobScheduled(job)) {
            scheduleJob(job);
        } else {
            LOG.warn(String.format("A job runner with for job %s ALREADY has been scheduled for execution.",
                executionRequest.getRequestId()));
        }
        LOG.info(String.format("Execution Request %s has been consumed.", executionRequest.getRequestId()));
    }

    /**
     * Creates a {@link Job} for request. After this method has been completed the job for the given request exists in the DB.
     * <p>
     * Of particular note of the sub-objects of the Job, that are populated in this method, are the following:
     * <ul>
     * <li>executionRequest - {@link ExecutionRequest} that has been submitted by MIF Client and deserialised by MIF Server
     * <li>commandExecutionTarget - {@link CommandExecutionTarget} as configured in the Connector config
     * </ul>
     *
     * @param executionRequest
     * @return the job
     */
    protected Job createJobForRequest(ExecutionRequest executionRequest) {
        Job job = jobManagementService.createNewJob();

        job.setConnectorId(getConnectorId()); // Duplicates the executionType from the CommandExecutionTarget
        
        job.setExecutionRequest(executionRequest);
        
        job.setCommandExecutionTarget(getCommandExecutionTarget());
        
        jobManagementService.saveJob(job);
        return job;
    }

    /**
     * Schedules a job for execution. After the method has been completed the job has been successfully scheduled for execution.
     * 
     * @param executionRequest
     */
    void scheduleJob(final Job job) {

        LOG.info(String.format("*** Execution Metrics : {Scheduling and Preparation.2} " + "Created JobRunner and Job with jobId [%s]",
            job.getJobId()));

        jobManagementService.update(job.getJobId(), createJobRunnerSchedulingUpdater());
        LOG.info(String.format("Submitted the job with id %s to the execution service", job.getJobId()));

        LOG.info(String.format("*** Execution Metrics : {Scheduling and Preparation.3} "
            + "Submitted JobRunner with jobId [%s] to task executor", job.getJobId()));
    }

    /**
     * Creates an updater that sets job status to scheduled only if jobRunner has been successfully submitted
     * @param taskExecutor
     * @param jobRunner
     * @return updater
     */
    ObjectStateUpdater<Job> createJobRunnerSchedulingUpdater() {
        return new ObjectStateUpdater<Job>().addCondition(new Precondition<Job>() {

            @Override
            public void check(Job job) {
                JobRunner jobRunner = jobRunnerFactory.createJobRunner(job);
                LOG.debug(String.format("Created runner for: %s", job.getJobId()));
                taskExecutor.submit(jobRunner);
                jobRunnerRegistry.put(job.getJobId(), jobRunner);
            }

        }).addCommand(new Function<Job, Job>() {

            @Override
            public Job apply(Job job) {
                job.setJobStatus(JobStatus.SCHEDULED);
                return job;
            }
        });
    }

    /**
     * This message is invoked by the JobRunnerResultListener which consumes the
     * result message from the Job Runner and delegates the message to the Dispatcher.
     * 
     * @param jobRunnerResponse the job runner response
     */
    public synchronized void handleResultMessage(String jobRunnerResponse) {
        Preconditions.checkNotNull(jobRunnerResponse, "Response message is null");
        ExecutionResponse response = MessagingHelper.parseExecutionResponse(jobRunnerResponse);
        String uuid = response.getRequestId();

        if (jobExists(uuid)) {
            LOG.info("Removing Runner handling the request " + uuid + " from Registry");
            jobRunnerRegistry.remove(uuid);
            completeJob(response);
        } else {
            throw new IllegalStateException("Received a result message for a request that does not exist. Request ID:" + uuid);
        }

        LOG.info(String.format("*** Execution Metrics : {ResultHandling.2} Received the message [%s] and "
            + "sending the result [%s] to TaskExecutionResultListener", jobRunnerResponse, uuid));

        LOG.debug(String.format("Response Message %s handling completed: ", jobRunnerResponse));
    }

    /**
     * Completes the job
     * @param executionResponse
     */
    void completeJob(ExecutionResponse executionResponse) {

        String requestId = executionResponse.getRequestId();

        JobStatus jobStatus = JobStatus.valueOf(executionResponse.getStatus());
        LOG.debug("Finalizing the " + requestId + " with status " + jobStatus);
        if (!jobStatus.isFinal()) {
            executionResponse.setStatus(JobStatus.FAILED.name());
            executionResponse.setResult("Job has been completed not in final state. [ " + jobStatus + " ].");
        }

        if (!jobManagementService.getJob(requestId).getJobStatus().isFinal()) {
            //only update if job is not in final state, if it is then that means that response hasn't been yet sent to the client.
            ObjectStateUpdater<Job> jobStateUpdater = createFinalizingJobStatusUpdater(executionResponse);
            jobManagementService.update(requestId, jobStateUpdater);
        }

        LOG.debug("The job " + requestId + " processing has been finished with status " + jobStatus);
    }

    ObjectStateUpdater<Job> createFinalizingJobStatusUpdater(ExecutionResponse executionResponse) {
        ObjectStateUpdater<Job> jobStateUpdater = new ObjectStateUpdater<Job>();

        if (RejectedExecutionRequestConsumer.EXECUTION_REQUEST_REJECTED.equals(executionResponse.getResult())) {

            final DetailedStatus detailedStatus = createRejectedJobDetailedStatus(executionResponse.getRequestId());
            executionResponse.setSummaryMessage(detailedStatus.getSummary());
            jobStateUpdater.addCommand(new Function<Job, Job>() {

                @Override
                public Job apply(Job job) {
                    try {
                        job.setDetailedStatusMsg(detailedStatus.asString());
                    } catch (JAXBException e) {
                        throw new IllegalStateException(String.format("Could not marshal detailed status for cancelled state for job %s.",
                            job.getJobId()), e);
                    }
                    return job;
                }
            });
        }

        jobStateUpdater.addCommand(new CompleteJobCommand(executionResponse));
        jobStateUpdater.addCommand(new Function<Job, Job>() {

            @Override
            public Job apply(Job job) {
                jmsTemplate.send(getResponseQueue(), new TextMessageCreator(job.getExecutionResponseMsg()));
                return job;
            }
        });
        return jobStateUpdater;
    }

    /**
     * Checks the database for a job, given the uuid.
     * @param uuid the uuid of the job passed to the connector in the request message.
     * @return boolean to indicate if the job is in the database.
     */
    private boolean jobExists(String uuid) {
        return null != jobManagementService.getJob(uuid);
    }

    private boolean jobScheduled(Job job) {
        return JobStatus.SCHEDULED.compareTo(job.getJobStatus()) <= 0;
    }

    /**
     * Handles cancellation request.
     *
     * @param requestID the request uid.
     */
    public synchronized void handleCancelMessage(String requestID) {
        Preconditions.checkNotNull(requestID, "Requested cancellation of a request with null id");

        if (!jobExists(requestID)) {
            throw new IllegalStateException(String.format("Job for the request %s does not exist.", requestID));
        }
        Job job = jobManagementService.getJob(requestID);

        LOG.info(String.format("Performing cancellation of the job %s.", job.getJobId()));

        if (job.getJobStatus().isFinal()) {
            LOG.info(String.format("Job %s already in final state skipping cancellation.", requestID));
            return;
        }

        if (jobScheduled(job)) {
            cancelScheduledJob(job);
        } else {
            cancelUnscheduledJob(job);
        }
        LOG.info(String.format("Cancellation Request %s has been processed.", requestID));
    }

    /**
     * Performs cancellation of a job for which a job runner has been scheduled
     * @param job
     */
    private void cancelScheduledJob(Job job) {
        LOG.info(String.format("Cancelling Job Runner for job %s.", job.getJobId()));
        if (!jobRunnerRegistry.containsKey(job.getJobId())) {
            throw new IllegalStateException(String.format("Job %s is scheduled but job runner for the job does not exist.", job.getJobId()));
        }
        jobRunnerRegistry.get(job.getJobId()).cancelProcessing();
    }

    /**
     * Performs required actions on a job that is being cancelled but JobRunner hasn't been scheduled
     */
    void cancelUnscheduledJob(Job job) {
        LOG.info(String.format("Job Runner for %s has not been scheduled.", job.getJobId()));
        final DetailedStatus detailedStatus = createCancelledJobDetailedStatus(job.getJobId());
        final String responseMsg = createCancellationResponseMsg(detailedStatus);
        ObjectStateUpdater<Job> updater = new ObjectStateUpdater<Job>().addCommand(new Function<Job, Job>() {

            @Override
            public Job apply(Job job) {
                job.setJobStatus(JobStatus.CANCELLED);
                try {
                    job.setDetailedStatusMsg(detailedStatus.asString());
                } catch (JAXBException e) {
                    throw new IllegalStateException(String.format("Could not marshal detailed status for cancelled state for job %s.",
                        job.getJobId()), e);
                }
                job.setExecutionResponseMsg(responseMsg);
                return job;
            }
        }).addCommand(new Function<Job, Job>() {

            @Override
            public Job apply(Job job) {
                jmsTemplate.send(getResponseQueue(), new TextMessageCreator(job.getExecutionResponseMsg()));
                return job;
            }
        });
        ;
        jobManagementService.update(job.getJobId(), updater);
    }

    private String createCancellationResponseMsg(DetailedStatus detailedStatus) {
        Preconditions.checkNotNull(detailedStatus, "Datiled status is null");
        return new ExecutionResponseBuilder().setRequestId(detailedStatus.getRequestId()).setSummaryMessage(detailedStatus.getSummary())
                .setStatus(JobStatus.CANCELLED.name()).toString();
    }

    DetailedStatus createRejectedJobDetailedStatus(String jobId) {
        Job job = jobManagementService.getJob(jobId);
        Preconditions.checkNotNull(job, "Job %s does not exist");
        try {
            jobRejectedDetailedStatusBuilder.setJob(job);
            return jobRejectedDetailedStatusBuilder.getDetailedStatus();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    DetailedStatus createCancelledJobDetailedStatus(String jobId) {
        Job job = jobManagementService.getJob(jobId);
        Preconditions.checkNotNull(job, "Job %s does not exist");
        try {
            jobCancelledDetailedStatusBuilder.setJob(job);
            return jobCancelledDetailedStatusBuilder.getDetailedStatus();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public ConcurrentMap<String, JobRunner> getJobRunnerRegistry() {
        return jobRunnerRegistry;
    }

    public void setJobRunnerRegistry(ConcurrentMap<String, JobRunner> jobRunnerRegistry) {
        this.jobRunnerRegistry = jobRunnerRegistry;
    }

    public AsyncTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    @Required
    public void setTaskExecutor(AsyncTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public String getResponseQueue() {
        return responseQueue;
    }

    @Override
    @Required
    public void setResponseQueue(String destination) {
        this.responseQueue = destination;
    }

    public JobManagementService getJobManagementService() {
        return jobManagementService;
    }

    @Required
    public void setJobManagementService(JobManagementService jobManagementService) {
        this.jobManagementService = jobManagementService;
    }

    public JobRunnerFactory getJobRunnerFactory() {
        return jobRunnerFactory;
    }

    @Required
    public void setJobRunnerFactory(JobRunnerFactory jobRunnerFactory) {
        this.jobRunnerFactory = jobRunnerFactory;
    }

    @Override
    @Required
    public void setCancellationQueue(String cancellationQueue) {
        this.cancellationQueue = cancellationQueue;
    }

    @Override
    @Required
    public void setRequestQueue(String requestQueue) {
        this.requestQueue = requestQueue;
    }

    @Override
    public String getCancellationQueue() {
        return cancellationQueue;
    }

    @Override
    public String getRequestQueue() {
        return requestQueue;
    }

    @Override
    public String getRuntimeMetrics() {
        StringBuilder info = new StringBuilder();
        ConcurrentMap<String, JobRunner> jubRunnersRegistry = getJobRunnerRegistry();
        info.append("<b>Connector Name:</b> ");
        info.append(getConnectorId());
        info.append(" - ");
        info.append(String.format(" NumJobs [%s]", jubRunnersRegistry.keySet().size()));
        info.append(" -");
        AsyncTaskExecutor executor = getTaskExecutor();
        if (executor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor pExec = (ThreadPoolTaskExecutor) executor;
            info.append(" Active: ");
            info.append(pExec.getActiveCount());
            info.append(" used of ");
            info.append(pExec.getMaxPoolSize());
            info.append(". Curr: ");
            info.append(pExec.getPoolSize());
            info.append(" Core Num Threads: ");
            info.append(pExec.getCorePoolSize());
            info.append(" KeepAliveSecs: ");
            info.append(pExec.getKeepAliveSeconds());
        }
        info.append("<br><br>");

        for (String jobId : jubRunnersRegistry.keySet()) {
            Job job = jobManagementService.getJob(jobId);
            info.append(jobRuntimeMetricsProvider.getRuntimeMetrics(job));
            info.append("<br>");
        }
        info.append("<br>");
        return info.toString();
    }

    @Override
    public String getInternalQueue() {
        return internalQueue;
    }

    @Required
    @Override
    public void setInternalQueue(String internalQueue) {
        this.internalQueue = internalQueue;
    }

    @Required
    public void setJobRejectedDetailedStatusBuilder(JobDetailedStatusBuilder jobRejectedDetailedStatusBuilder) {
        this.jobRejectedDetailedStatusBuilder = jobRejectedDetailedStatusBuilder;
    }

    public JobDetailedStatusBuilder getJobRejectedDetailedStatusBuilder() {
        return jobRejectedDetailedStatusBuilder;
    }

    @Required
    public void setJobCancelledDetailedStatusBuilder(JobDetailedStatusBuilder jobCancelledDetailedStatusBuilder) {
        this.jobCancelledDetailedStatusBuilder = jobCancelledDetailedStatusBuilder;
    }

    public JobDetailedStatusBuilder getJobCancelledDetailedStatusBuilder() {
        return jobCancelledDetailedStatusBuilder;
    }
    
    public JobRuntimeMetricsProvider getJobRuntimeMetricsProvider() {
        return jobRuntimeMetricsProvider;
    }

    @Required
    public void setJobRuntimeMetricsProvider(JobRuntimeMetricsProvider jobRuntimeMetricsProvider) {
        this.jobRuntimeMetricsProvider = jobRuntimeMetricsProvider;
    }

}
