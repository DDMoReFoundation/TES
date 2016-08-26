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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.JobRuntimeMetricsProvider;
import com.mango.mif.connector.runner.JobRunner;
import com.mango.mif.connector.runner.JobRunnerFactory;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.status.JobDetailedStatusBuilder;
import com.mango.mif.core.dao.impl.JobRepository;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.JobInvokerProvider;
import com.mango.mif.core.messaging.RejectedExecutionRequestConsumer;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.core.services.JobManagementServiceImpl;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.ExecutionResponseBuilder;
import com.mango.mif.domain.ExecutionType;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.TestsHelper;

/**
 * Tests the Connector class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectorWithJobRunnersTest {

    private final static Logger LOG = Logger.getLogger(ConnectorWithJobRunnersTest.class);

    @Mock
    JobManagementService jobManagementService;
    @Mock
    JobRunnerFactory jobRunnerFactory;
    @Mock
    ThreadPoolTaskExecutor taskExecutor;
    @Mock
    JmsTemplate jmsTemplate;
    @Mock
    ConcurrentMap<String, JobRunner> jobRunnerRegistry;
    @Mock
    JobDetailedStatusBuilder jobCancelledDetailedStatusBuilder;
    @Mock
    JobDetailedStatusBuilder jobRejectedDetailedStatusBuilder;
    @Mock
    JobRuntimeMetricsProvider jobRuntimeMetricsProvider;

    private static final String REQUEST_ID = "REQUEST_ID";
    /**
     * instance under test
     */
    private ConnectorWithJobRunners connector;

    private File tmpDir;

    private Job job;

    private final static String CONNECTOR_ID = "MOCK_CONNECTOR_ID";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        connector = new ConnectorWithJobRunners();
        CommandExecutionTarget commandExecutionTarget = new CommandExecutionTarget();
        connector.setConnectorId(CONNECTOR_ID);
        connector.setJobManagementService(jobManagementService);
        connector.setJobRunnerFactory(jobRunnerFactory);
        connector.setTaskExecutor(taskExecutor);
        connector.setJmsTemplate(jmsTemplate);
        connector.setJobRejectedDetailedStatusBuilder(jobRejectedDetailedStatusBuilder);
        connector.setJobCancelledDetailedStatusBuilder(jobCancelledDetailedStatusBuilder);
        connector.setJobRuntimeMetricsProvider(jobRuntimeMetricsProvider);
        tmpDir = TestsHelper.createTmpDirectory();
        connector.setCancellationQueue("CANCELLATION-QUEUE");
        connector.setRequestQueue("REQUEST-QUEUE");
        connector.setCommandExecutionTarget(commandExecutionTarget);
        job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.NEW);
        when(jobManagementService.createNewJob()).thenReturn(job);
        when(jobManagementService.saveJob(job)).thenReturn(job);
        when(jobManagementService.saveJobRunnerState((String) any(), (JobRunnerState) any())).thenReturn(job);
        when(jobManagementService.saveJobStatus((String) any(), (String) any())).thenReturn(job);
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tmpDir);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Test(expected = NullPointerException.class)
    public void onMessage_shouldThrowExceptionIfMessageIsNull() {
        connector.onMessage(null);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void onMessage_shouldThrowExceptionIfMessageHasInvalidContent() throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenThrow(JMSException.class);
        connector.onMessage(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_shouldThrowExceptionIfMessageHasNoContent() throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn("");
        connector.onMessage(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_shouldThrowExceptionIfMessageHasNullContent() throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(null);
        connector.onMessage(message);
    }

    @Test(expected = RuntimeException.class)
    public void onMessage_shouldThrowExceptionIfExecutionRequestMessageIsInvalid() throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn("<invalid content>");

        LOG.debug("The  org.xml.sax.SAXParseException is expected to be printed out in the log");
        connector.onMessage(message);

    }

    @Test
    public void createJobForRequest_shouldCreateAJobForValidExecutionRequest() throws JAXBException, MIFException {
    	final CommandExecutionTarget commandExecutionTarget = new CommandExecutionTarget();
    	this.connector.setCommandExecutionTarget(commandExecutionTarget);
        final ExecutionRequest executionRequest = createRequestBuilder().getExecutionRequest();
        
        final Job mockJob = connector.createJobForRequest(executionRequest);
        assertNotNull("Job should have been created", mockJob);
        
        verify(mockJob).setConnectorId(connector.getConnectorId());
        verify(mockJob).setExecutionRequest(executionRequest);
        verify(mockJob).setCommandExecutionTarget(commandExecutionTarget);
        
        verify(jobManagementService).createNewJob();
        verify(jobManagementService).saveJob(job);
        verifyNoMoreInteractions(job);
    }

    @Test
    public void onMessage_shouldNotCreateAJobIfJobForGivenRequestAlreadyExists() throws JAXBException, MIFException, JMSException {
        ConnectorWithJobRunners spiedConnector = spy(connector);
        TextMessage message = mock(TextMessage.class);
        ExecutionRequestBuilder executionRequestBuiler = createRequestBuilder();
        when(jobRunnerFactory.createJobRunner((Job) any())).thenReturn(mock(JobRunner.class));
        when(message.getText()).thenReturn(executionRequestBuiler.getExecutionRequestMsg());
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        spiedConnector.onMessage(message);
        verify(spiedConnector, times(0)).createJobForRequest((ExecutionRequest) any());
    }

    @Test
    public void onMessage_shouldCreateAJobForGivenRequest() throws JAXBException, MIFException, JMSException {
        ConnectorWithJobRunners spiedConnector = spy(connector);
        TextMessage message = mock(TextMessage.class);
        ExecutionRequestBuilder executionRequestBuiler = createRequestBuilder();
        when(message.getText()).thenReturn(executionRequestBuiler.getExecutionRequestMsg());
        when(jobRunnerFactory.createJobRunner((Job) any())).thenReturn(mock(JobRunner.class));

        spiedConnector.onMessage(message);
        verify(spiedConnector, times(1)).createJobForRequest((ExecutionRequest) any());
    }

    @Test
    public void onMessage_shouldScheduleAJobRunnerOnlyIfOneWasNotYetScheduled() throws JAXBException, MIFException, JMSException {
        ConnectorWithJobRunners spiedConnector = spy(connector);
        TextMessage message = mock(TextMessage.class);
        ExecutionRequestBuilder executionRequestBuiler = createRequestBuilder();
        when(jobRunnerFactory.createJobRunner((Job) any())).thenReturn(mock(JobRunner.class));
        when(message.getText()).thenReturn(executionRequestBuiler.getExecutionRequestMsg());
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.SCHEDULED);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        when(jobManagementService.saveJob((Job) any())).thenReturn(job);
        spiedConnector.onMessage(message);
        verify(spiedConnector, never()).scheduleJob((Job) any());
    }

    @Test(expected = NullPointerException.class)
    public void handleResultMessage_shoudThrowExceptionIfResultMessageIsNull() {
        connector.handleResultMessage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleResultMessage_shoudThrowExceptionIfResultMessageDoesNotContainRequestID() throws JAXBException {
        String responseMessage = createResponseBuilder().getExecutionResponseMsg();
        responseMessage = responseMessage.replaceAll("<requestId>[\\w-]+</requestId>", "<requestId></requestId>");
        connector.handleResultMessage(responseMessage);
    }

    @Test(expected = IllegalStateException.class)
    public void handleResultMessage_shoudThrowExceptionIfJobForGivenRequestIDDoesNotExist() throws JAXBException {
        String responseMessage = createResponseBuilder().getExecutionResponseMsg();
        connector.handleResultMessage(responseMessage);
    }

    @Test
    public void handleResultMessage_shoudRemoveJobRunnerFromTheRegistry() throws JAXBException {
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.COMPLETED);
        String responseMessage = createResponseBuilder().getExecutionResponseMsg();
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        connector.getJobRunnerRegistry().put(REQUEST_ID, mock(JobRunner.class));
        connector.handleResultMessage(responseMessage);
        assertFalse("Job should be removed from the job runner registry", connector.getJobRunnerRegistry().containsKey(REQUEST_ID));
    }

    @Test
    public void scheduleJob_shouldScheduleJobRunnerForGivenJob() throws JMSException, MIFException, JAXBException {
        ExecutionRequestBuilder executionRequestBuiler = createRequestBuilder();
        Job job = connector.createJobForRequest(executionRequestBuiler.getExecutionRequest());
        JobRunner jobRunner = mock(JobRunner.class);
        when(jobRunnerFactory.createJobRunner(job)).thenReturn(jobRunner);
        connector.scheduleJob(job);
        verify(jobManagementService).update(eq(job.getJobId()), (ObjectStateUpdater<Job>) any());
    }

    //FIXME:  ID 10461
    @Ignore("ID 10461: Temporarily commented this out as workaround for service accessing filesystem after job completion - see ticket.")
    @Test
    public void completeJob_shouldCompleteAJobAndCleanUserPassword() throws JAXBException {
        //Given
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.COMPLETED);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);

        //when
        connector.completeJob(createResponseBuilder().setStatus(JobStatus.COMPLETED.name()).getExecutionResponse());

        //then
        assertEquals("User password was not cleaned up from job", "", job.getPassword());
        assertEquals("User password was not cleaned up from execution request", "", job.getExecutionRequest().getUserPassword());
        assertEquals("Job status not set to completed", JobStatus.COMPLETED, JobStatus.valueOf(job.getClientRequestStatus()));
        assertNotNull("Reponse message is not set.", job.getExecutionResponse());
        verify(jobManagementService).saveJob(job);
    }

    @Test
    public void completeJob_shouldCompleteAJob() throws JAXBException, MIFException {
        //Given
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.PROCESSING);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        ExecutionResponse executionResponse = createResponseBuilder().setStatus(JobStatus.COMPLETED.name()).getExecutionResponse();
        //when
        connector.completeJob(executionResponse);

        //then
        assertEquals(JobStatus.COMPLETED.name(), executionResponse.getStatus());
        verify(jobManagementService).update(eq(REQUEST_ID), (ObjectStateUpdater<Job>) any());
    }

    @Test
    public void createFinalizingJobStatusUpdater_shouldCreateUpdaterRewrtingFinalStateFromExecutionResponse() throws JAXBException,
            MIFException {
        //Given
        String finalResult = "FINAL RESULT";
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.PROCESSING);
        ExecutionResponse executionResponse = createResponseBuilder().setStatus(JobStatus.COMPLETED.name()).setResult(finalResult)
                .getExecutionResponse();
        //when
        ObjectStateUpdater<Job> jobStatusUpdater = connector.createFinalizingJobStatusUpdater(executionResponse);
        jobStatusUpdater.update(job);
        //then
        verify(job).setJobStatus(JobStatus.COMPLETED);
        verify(job).setExecutionResponse(executionResponse);
        verify(jmsTemplate).send(any(String.class), any(MessageCreator.class));
    }

    @Test
    public void createFinalizingJobStatusUpdater_shouldCreateUpdaterRewrtingFinalStateFromExecutionResponseAndSettingRejectedDetailedStatus()
            throws JAXBException, ExecutionException {
        //Given
        String finalResult = RejectedExecutionRequestConsumer.EXECUTION_REQUEST_REJECTED;

        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.PROCESSING);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        DetailedStatus detailedStatus = mock(DetailedStatus.class);
        when(detailedStatus.getSummary()).thenReturn("Rejected");
        when(jobRejectedDetailedStatusBuilder.getDetailedStatus()).thenReturn(detailedStatus);

        ExecutionResponse executionResponse = createResponseBuilder().setStatus(JobStatus.FAILED.name()).setResult(finalResult)
                .getExecutionResponse();
        //when
        ObjectStateUpdater<Job> jobStatusUpdater = connector.createFinalizingJobStatusUpdater(executionResponse);
        jobStatusUpdater.update(job);
        //then
        assertEquals("Rejected",executionResponse.getSummaryMessage());
        verify(job).setDetailedStatusMsg((String) any());
        verify(job).setJobStatus(JobStatus.FAILED);
        verify(job).setExecutionResponse(executionResponse);
        verify(jmsTemplate).send(any(String.class), any(MessageCreator.class));
    }

    @Test(expected = IllegalStateException.class)
    public void createFinalizingJobStatusUpdater_shouldThrowExceptionIfMarshallingOfDetailedStatusFails() throws JAXBException,
            ExecutionException {
        //Given
        String finalResult = RejectedExecutionRequestConsumer.EXECUTION_REQUEST_REJECTED;
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.PROCESSING);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        DetailedStatus detailedStatus = mock(DetailedStatus.class);
        when(detailedStatus.getRequestId()).thenReturn(REQUEST_ID);
        when(jobRejectedDetailedStatusBuilder.getDetailedStatus()).thenReturn(detailedStatus);
        doThrow(JAXBException.class).when(detailedStatus).asString();
        ExecutionResponse executionResponse = createResponseBuilder().setStatus(JobStatus.FAILED.name()).setResult(finalResult)
                .getExecutionResponse();
        //when
        ObjectStateUpdater<Job> jobStatusUpdater = connector.createFinalizingJobStatusUpdater(executionResponse);
        jobStatusUpdater.update(job);
    }

    @Test
    public void completeJob_shouldNotCompleteAJobIfItIsInFinalStateAlready() throws JAXBException, MIFException {
        //Given
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.CANCELLED);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        ExecutionResponse executionResponse = createResponseBuilder().setStatus(JobStatus.COMPLETED.name()).getExecutionResponse();
        //when
        connector.completeJob(executionResponse);

        //then
        assertEquals(JobStatus.COMPLETED.name(), executionResponse.getStatus());
        verify(jobManagementService, never()).update(eq(REQUEST_ID), (ObjectStateUpdater<Job>) any());
    }

    @Test
    public void completeJob_shouldSetToFailedStateIfCompletedInNonFinalState() throws JAXBException, MIFException {
        //Given
        Job job = createJob(REQUEST_ID, CONNECTOR_ID, JobStatus.PROCESSING);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        ExecutionResponse executionResponse = createResponseBuilder().setStatus(JobStatus.PROCESSING.name()).getExecutionResponse();

        //when
        connector.completeJob(executionResponse);

        //then
        assertEquals(JobStatus.FAILED.name(), executionResponse.getStatus());
        verify(jobManagementService).update(eq(REQUEST_ID), (ObjectStateUpdater<Job>) any());
    }

    @Test
    public void doFailureRecovery_shouldRescheduleJobRunners() {

        List<Job> jobs = Lists.newArrayList(createJob(UUID.randomUUID().toString(), CONNECTOR_ID, JobStatus.PROCESSING),
            createJob(UUID.randomUUID().toString(), CONNECTOR_ID, JobStatus.PROCESSING),
            createJob(UUID.randomUUID().toString(), CONNECTOR_ID, JobStatus.PROCESSING_FINISHED),
            createJob(UUID.randomUUID().toString(), CONNECTOR_ID, JobStatus.SCHEDULED),
            createJob(UUID.randomUUID().toString(), CONNECTOR_ID, JobStatus.NEW));

        List<Job> jobs2 = Lists.newArrayList(createJob(UUID.randomUUID().toString(), "SOME_OTHER_CONNECTOR_ID", JobStatus.PROCESSING));

        for (Job job : jobs) {
            when(jobRunnerFactory.createJobRunner(job)).thenReturn(mock(JobRunner.class));
        }

        for (Job job : jobs2) {
            when(jobRunnerFactory.createJobRunner(job)).thenReturn(mock(JobRunner.class));
        }

        when(jobManagementService.getUncompletedConnectorJobs(CONNECTOR_ID)).thenReturn(jobs);
        when(jobManagementService.getUncompletedConnectorJobs("SOME_OTHER_CONNECTOR_ID")).thenReturn(jobs2);

        connector.doFailureRecovery();

        //Should not get "SOME_OTHER_CONNECTOR_ID" job
        assertEquals(3, connector.getJobRunnerRegistry().size());
    }

    @Test
    public void getRuntimeMetrics_shouldGenerateRuntimeMetrics() {

        Job jobA = mock(Job.class);
        when(jobA.getJobId()).thenReturn("JobA");
        Job jobB = mock(Job.class);
        when(jobB.getJobId()).thenReturn("JobB");

        JobRunner jobRunnerA = mock(JobRunner.class), jobRunnerB = mock(JobRunner.class);

        ConcurrentMap<String, JobRunner> registry = Maps.newConcurrentMap();
        registry.put(jobA.getJobId(), jobRunnerA);
        registry.put(jobB.getJobId(), jobRunnerB);

        connector.setJobRunnerRegistry(registry);

        when(jobManagementService.getJob(jobA.getJobId())).thenReturn(jobA);
        when(jobManagementService.getJob(jobB.getJobId())).thenReturn(jobB);
        when(jobRuntimeMetricsProvider.getRuntimeMetrics(jobA)).thenReturn("JOB A");
        when(jobRuntimeMetricsProvider.getRuntimeMetrics(jobB)).thenReturn("JOB B");

        String metrics = connector.getRuntimeMetrics();
        LOG.debug(metrics);
        assertEquals(
            "<b>Connector Name:</b> MOCK_CONNECTOR_ID -  NumJobs [2] - Active: 0 used of 0. Curr: 0 Core Num Threads: 0 KeepAliveSecs: 0<br><br>JOB B<br>JOB A<br><br>",
            metrics);
    }

    @Test(expected = RuntimeException.class)
    public void createJobRunnerSchedulingUpdater_shouldThrowExceptionIfJobRunnerIsRejectedAndNotUpdateJobStatus() {
        JobRunner jobRunner = mock(JobRunner.class);
        ObjectStateUpdater<Job> updater = connector.createJobRunnerSchedulingUpdater();

        doThrow(RuntimeException.class).when(taskExecutor).submit(jobRunner);
        updater.update(job);

        verify(connector.getJobRunnerFactory()).createJobRunner(job);
        verify(job, never()).setJobStatus((JobStatus) any());
        assertNull(connector.getJobRunnerRegistry().get(job.getJobId()));
    }

    @Test
    public void createJobRunnerSchedulingUpdater_shouldPersistJobWith_SHEDULED_StatusIfSubmissionSuccessful() {
        JobRunner jobRunner = mock(JobRunner.class);
        when(connector.getJobRunnerFactory().createJobRunner(job)).thenReturn(jobRunner);
        ObjectStateUpdater<Job> updater = connector.createJobRunnerSchedulingUpdater();
        updater.update(job);
        verify(connector.getJobRunnerFactory()).createJobRunner(job);
        assertNotNull(connector.getJobRunnerRegistry().get(job.getJobId()));
        verify(job).setJobStatus(JobStatus.SCHEDULED);
        verify(taskExecutor).submit(jobRunner);

    }

    @Test
    public void createCancelledJobDetailedStatus_shouldCreateJobCancelledDetailedStatus() throws ExecutionException {
        DetailedStatus detailedStatus = mock(DetailedStatus.class);
        when(detailedStatus.getRequestId()).thenReturn(REQUEST_ID);
        when(jobCancelledDetailedStatusBuilder.getDetailedStatus()).thenReturn(detailedStatus);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(mock(Job.class));
        DetailedStatus result = connector.createCancelledJobDetailedStatus(REQUEST_ID);
        assertNotNull(result);
    }

    @Test(expected = RuntimeException.class)
    public void createCancelledJobDetailedStatus_shouldThrowExceptionIfCreationOfDetailedStatusFails() throws ExecutionException {
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(mock(Job.class));
        doThrow(ExecutionException.class).when(jobCancelledDetailedStatusBuilder).getDetailedStatus();

        connector.createCancelledJobDetailedStatus(REQUEST_ID);
    }

    @Test
    public void createRejectedJobDetailedStatus_shouldCreateJobRejectedDetailedStatus() throws ExecutionException {
        DetailedStatus detailedStatus = mock(DetailedStatus.class);
        when(detailedStatus.getRequestId()).thenReturn(REQUEST_ID);
        when(jobRejectedDetailedStatusBuilder.getDetailedStatus()).thenReturn(detailedStatus);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(mock(Job.class));
        DetailedStatus result = connector.createRejectedJobDetailedStatus(REQUEST_ID);
        assertNotNull(result);
    }

    @Test(expected = RuntimeException.class)
    public void createRejectedJobDetailedStatus_shouldThrowExceptionIfCreationOfDetailedStatusFails() throws ExecutionException {
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(mock(Job.class));
        doThrow(ExecutionException.class).when(jobRejectedDetailedStatusBuilder).getDetailedStatus();

        connector.createRejectedJobDetailedStatus(REQUEST_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void cancelJob_shouldThrowExceptionIfMarshalizationOfDetailedStatusFails() throws ExecutionException, JAXBException {
        connector.setJobManagementService(createJobManagementService());
        when(job.getJobStatus()).thenReturn(JobStatus.PROCESSING);

        DetailedStatus detailedStatus = mock(DetailedStatus.class);
        when(detailedStatus.getRequestId()).thenReturn(REQUEST_ID);
        when(jobCancelledDetailedStatusBuilder.getDetailedStatus()).thenReturn(detailedStatus);
        doThrow(JAXBException.class).when(detailedStatus).asString();
        connector.cancelUnscheduledJob(job);
    }

    @Test
    public void handleCancelMessage_shouldCancelJobRunnerForGivenJobIfOneAlreadyExists() {
        JobRunner jobRunner = mock(JobRunner.class);
        connector.getJobRunnerRegistry().put(REQUEST_ID, jobRunner);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        when(job.getJobStatus()).thenReturn(JobStatus.SCHEDULED);

        connector.handleCancelMessage(REQUEST_ID);

        verify(jobRunner).cancelProcessing();
    }

    @Test
    public void handleCancelMessage_shouldCancelJobIfJobRunnerDoesNotExist() throws ExecutionException {
        connector.setJobManagementService(createJobManagementService());
        DetailedStatus detailedStatus = mock(DetailedStatus.class);
        when(detailedStatus.getRequestId()).thenReturn(REQUEST_ID);
        when(jobCancelledDetailedStatusBuilder.getDetailedStatus()).thenReturn(detailedStatus);

        connector.handleCancelMessage(REQUEST_ID);

        verify(job).setJobStatus(JobStatus.CANCELLED);
        verify(job).setDetailedStatusMsg((String) any());
        verify(job).setExecutionResponseMsg((String) any());
    }

    @Test(expected = IllegalStateException.class)
    public void handleCancelMessage_shouldThrowExceptionIfJobWasScheduledButJobRunnerDoesNotExist() {
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        when(job.getJobStatus()).thenReturn(JobStatus.SCHEDULED);
        connector.handleCancelMessage(REQUEST_ID);
    }

    @Test(expected = NullPointerException.class)
    public void handleCancelMessage_shoudThrowExceptionWhenHandlingACancelMessageWithNullRequestID() {
        connector.handleCancelMessage(null);
    }

    @Test(expected = IllegalStateException.class)
    public void handleCancelMessage_shouldThrowExceptionIfJobWithGivenIdDoesNotExist() {
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(null);
        connector.handleCancelMessage(REQUEST_ID);
    }

    @Test
    public void handleCancelMessage_shouldSkipCancellationIfJobAlreadyInFinalState() {
        connector.setJobManagementService(createJobManagementService());
        when(job.getJobStatus()).thenReturn(JobStatus.FAILED);
        connector.handleCancelMessage(REQUEST_ID);
        verify(job, never()).setJobStatus(JobStatus.CANCELLED);
        verify(job, never()).setDetailedStatusMsg((String) any());
    }

    private JobManagementService createJobManagementService() {
        JobManagementServiceImpl jobManagementService = new JobManagementServiceImpl();
        jobManagementService.setJobInvokerProvider(mock(JobInvokerProvider.class));
        JobRepository jobRepository = mock(JobRepository.class);
        when(jobRepository.getJob(REQUEST_ID)).thenReturn(job);
        when(jobRepository.getJobForUpdate(REQUEST_ID)).thenReturn(job);
        jobManagementService.setJobRepository(jobRepository);
        jobManagementService.setMifServiceAcccountPassword("");
        jobManagementService.setMifServiceAccountUserName("");
        return jobManagementService;
    }

    /**
     * Creates mock job instance
     */
    private Job createJob(String jobId, String connectorId, JobStatus jobStatus) {
        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn(jobId);
        ExecutionRequest exectuionRequest = mock(ExecutionRequest.class);
        when(job.getExecutionRequest()).thenReturn(exectuionRequest);
        when(exectuionRequest.getUserPassword()).thenReturn("mock-password");
        when(job.getUserName()).thenReturn("mock-user");
        when(job.getConnectorId()).thenReturn(connectorId);
        when(job.getClientRequestStatus()).thenReturn(jobStatus.name());
        when(job.getJobStatus()).thenReturn(jobStatus);
        return job;
    }

    /**
     * Creates response builder creating mock response message
     * @return
     */
    private ExecutionResponseBuilder createResponseBuilder() {
        ExecutionResponseBuilder responseBuilder = new ExecutionResponseBuilder();
        responseBuilder.setRequestId(REQUEST_ID).setStatus(JobStatus.COMPLETED.name());
        return responseBuilder;
    }

    private ExecutionRequestBuilder createRequestBuilder() {
        return new ExecutionRequestBuilder().setExecutionType(ExecutionType.Unknown.name())
        .setRequestId(REQUEST_ID)
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE");
    }
}
