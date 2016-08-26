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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import javax.jms.IllegalStateException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.common.collect.Lists;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.connector.runner.JobRunner;
import com.mango.mif.connector.runner.JobRunnerFactory;
import com.mango.mif.connector.runner.impl.AbstractStateHandler;
import com.mango.mif.connector.runner.impl.DefaultJobRunner;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.impl.SCXMLDriver;
import com.mango.mif.connector.runner.impl.SCXMLDriverImpl;
import com.mango.mif.connector.runner.impl.SCXMLProcessingCancellationHandler;
import com.mango.mif.connector.runner.impl.statehandler.AwaitMockStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.DefaultStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.StateHandler;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionType;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.MIFTestProperties;
import com.mango.mif.utils.TestsHelper;


/**
 * A unit test, checking if cancellation request is handled properly by dispatcher, driver and job runner
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectorWithJobRunnersCancellationWorkflowTest {
	/**
	 * Mock job repository
	 */
	@Mock JobManagementService jobManagementService;
	/**
	 * Mock job repository
	 */
	@Mock JobRunnerFactory jobRunnerFactory;
	/**
	 * Mock job runners task executor
	 */
	@Mock AsyncTaskExecutor taskExecutor;
	/**
	 * jms template
	 */
	@Mock JmsTemplate jobRunnerJmsTemplate;
	/**
	 * jms template
	 */
	@Mock JmsTemplate connectorJmsTemplate;
	/**
	 * mock job runner registry
	 */
	@Mock ConcurrentMap<String, JobRunner> jobRunnerRegistry;
	/**
	 * Request ID
	 */
	private String requestUUID;
	/**
	 * dispatcher instance under test
	 */
	private ConnectorWithJobRunners connector;
	/**
	 * Job
	 */
	private Job job;
	/**
	 * job runner
	 */
	private DefaultJobRunner	jobRunner;
	/**
	 * a test job runner result queue
	 */
	private final static String MOCK_JOB_RUNNER_RESULT_QUEUE = "MOCK_JOB_RUNNER_RESULT_QUEUE";
	/**
	 * a test job runner result queue
	 */
	private final static String MOCK_DISPATCHER_RESULT_QUEUE = "MOCK_DISPATCHER_RESULT_QUEUE";
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		requestUUID = UUID.randomUUID().toString();
		
		connector = new ConnectorWithJobRunners();
		
		connector.setJobManagementService(jobManagementService);
		connector.setJobRunnerFactory(jobRunnerFactory);
		connector.setTaskExecutor(taskExecutor);
		connector.setJmsTemplate(connectorJmsTemplate);
		connector.setJobRunnerRegistry(jobRunnerRegistry);
		connector.setResponseQueue(MOCK_DISPATCHER_RESULT_QUEUE);
		
		final CommandExecutionTarget commandExecutionTarget = new CommandExecutionTarget();
		connector.setCommandExecutionTarget(commandExecutionTarget);
		
		job = new Job();
		job.setJobId(requestUUID);
		job.setJobStatus(JobStatus.PROCESSING);
        when(jobManagementService.createNewJob()).thenReturn(job);
        when(jobManagementService.saveJob(job)).thenReturn(job);
        when(jobManagementService.getJob(requestUUID)).thenReturn(job);

        when(jobManagementService.saveJobRunnerState((String)any(), (JobRunnerState)any())).thenReturn(job);
        when(jobManagementService.saveJobStatus((String)any(), (String)any())).thenReturn(job);

        job = connector.createJobForRequest(new ExecutionRequestBuilder().setExecutionType(ExecutionType.Unknown.name()).setRequestId(requestUUID)
            .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
            .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE").getExecutionRequest());

        jobRunner = new DefaultJobRunner();
        jobRunner.setDriver(configureSCXMLDriver());
        jobRunner.setJobManagementService(jobManagementService);
		jobRunner.setJobId(job.getJobId());
		jobRunner.setJmsTemplate(jobRunnerJmsTemplate);
		jobRunner.setResultDestination(MOCK_JOB_RUNNER_RESULT_QUEUE);
        Properties properties = TestsHelper.createPropertiesFromStream(ConnectorWithJobRunnersCancellationWorkflowTest.class.getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));

		MIFProperties.loadProperties(properties);
		
	}
	/**
	 * Creates an SCXML driver with handlers that will be used in tests
	 * @return
	 * @throws IllegalStateException
	 */
	private SCXMLDriver configureSCXMLDriver() throws IllegalStateException {
		
	    List<AbstractStateHandler> stateHandlers = Lists.newArrayList();
	    
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.PENDING.getStateName(), JobRunnerState.RUNNING.getTriggeringEvent()));
	    stateHandlers.add(new DefaultStateHandler(JobRunnerState.RUNNING.getStateName(), SCXMLDriver.NULL_EVENT));
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.TASK_PENDING.getStateName(), JobRunnerState.TASK_PREPARING.getTriggeringEvent()));
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.TASK_PREPARING.getStateName(), JobRunnerState.TASK_SUBMITTING.getTriggeringEvent()));
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.TASK_SUBMITTING.getStateName(), JobRunnerState.TASK_PROCESSING.getTriggeringEvent()));
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.TASK_PROCESSING.getStateName(), JobRunnerState.TASK_RETRIEVING.getTriggeringEvent()));
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.TASK_RETRIEVING.getStateName(), JobRunnerState.TASK_POSTPROCESSING.getTriggeringEvent()));
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.TASK_POSTPROCESSING.getStateName(), JobRunnerState.TASK_FINISHED.getTriggeringEvent()));
	    stateHandlers.add(new AwaitMockStateHandler(JobRunnerState.TASK_FINISHED.getStateName(), JobRunnerState.FINISHED.getTriggeringEvent()));
	    stateHandlers.add(new DefaultStateHandler(JobRunnerState.FINISHED.getStateName(), SCXMLDriver.NULL_EVENT));
	    stateHandlers.add(new DefaultStateHandler(JobRunnerState.CANCELLED.getStateName(), SCXMLDriver.NULL_EVENT));
        stateHandlers.add(new DefaultStateHandler(JobRunnerState.FAILED.getStateName(), SCXMLDriver.NULL_EVENT));
	    
	    
		ThreadPoolTaskExecutor stateHandlersExecutor = new ThreadPoolTaskExecutor();
		stateHandlersExecutor.initialize();
		
		SCXMLDriverImpl scxmlDriver = new SCXMLDriverImpl();
		scxmlDriver.setStateHandlersExecutor(stateHandlersExecutor);
        scxmlDriver.setSCXMLDocument(ConnectorWithJobRunnersCancellationWorkflowTest.class.getResource("/META-INF/JobRunnerWithFailover.scxml"));
        for(AbstractStateHandler stateHandler : stateHandlers) {
            if(stateHandler instanceof DefaultStateHandler) {
                ((DefaultStateHandler)stateHandler).setJobManagementService(jobManagementService);
                ((DefaultStateHandler)stateHandler).setJobRunner(jobRunner);
            }
            stateHandler.setSCXMLDriver(scxmlDriver);
            scxmlDriver.registerHandler(spy(stateHandler));
        }
        scxmlDriver.setCancellationHandler(mock(SCXMLProcessingCancellationHandler.class));
        return scxmlDriver;
	}
	
	@Test
	public void shouldAcknowledgeCancellationIfCancelledBeforeStarting() throws ExecutionException {
        when(connector.getJobRunnerRegistry().get(job.getJobId())).thenReturn(jobRunner);
        when(connector.getJobRunnerRegistry().containsKey(job.getJobId())).thenReturn(true);
        connector.handleCancelMessage(job.getExecutionRequest().getRequestId().toString());
		
		SCXMLProcessingCancellationHandler cancellationHandler = (((SCXMLDriverImpl)jobRunner.getDriver()).getCancellationHandler());
		DefaultStateHandler cancelledStateHandler = ((DefaultStateHandler)((SCXMLDriverImpl)jobRunner.getDriver()).getStateHandlers().get(JobRunnerState.CANCELLED.getStateName()));
		
		jobRunner.call();

		assertEquals(DriverProcessingStatus.CANCELLED,jobRunner.getDriver().getProcessingStatus());
		
		verify(cancellationHandler).execute();
		verify(cancelledStateHandler).call();
	}

	@Test
	public void shouldCancelExecutionAndReceiveResultMessageOnlyOnce() throws InterruptedException {
		final DefaultJobRunner spiedInstance = spy(jobRunner);
        when(connector.getJobRunnerRegistry().get(job.getJobId())).thenReturn(spiedInstance);
        when(connector.getJobRunnerRegistry().containsKey(job.getJobId())).thenReturn(true);
        Map<String,StateHandler> stateHandlers = ((SCXMLDriverImpl)jobRunner.getDriver()).getStateHandlers();
		
		final CountDownLatch signal = new CountDownLatch(1);
		
		Thread thread = new Thread("Thread assigned to Job runner") {
			public void run() {

				spiedInstance.call();
				signal.countDown();
			};
		};
		thread.start();

		((AwaitMockStateHandler)stateHandlers.get(JobRunnerState.PENDING.getStateName())).getAwaitSignal().countDown();
		((AwaitMockStateHandler)stateHandlers.get("task-pending")).getAwaitSignal().countDown();
		
		AwaitMockStateHandler handler = ((AwaitMockStateHandler)stateHandlers.get("task-pending"));
		
		while(!handler.isDone()) 
		{
			Thread.sleep(1000);
		}
		
		connector.handleCancelMessage(job.getExecutionRequest().getRequestId().toString());
		
		signal.await();
		connector.handleResultMessage(spiedInstance.getResultMessage());
		
		verify(spiedInstance).cancelProcessing();
		assertEquals(DriverProcessingStatus.CANCELLED,spiedInstance.getDriver().getProcessingStatus());
	}
	
	@Test
	public void shouldInvokeACancelledStateHandlerIfStateHandlerOnWhichTheCancellationHasBeenReceivedIsNotifiedInternallyOnCancellation() throws InterruptedException, ExecutionException {
		final DefaultJobRunner spiedInstance = spy(jobRunner);
        when(connector.getJobRunnerRegistry().get(job.getJobId())).thenReturn(spiedInstance);
        when(connector.getJobRunnerRegistry().containsKey(job.getJobId())).thenReturn(true);
        Map<String,StateHandler> stateHandlers = ((SCXMLDriverImpl)jobRunner.getDriver()).getStateHandlers();
		
		final CountDownLatch signal = new CountDownLatch(1);
		
		Thread thread = new Thread("Thread assigned to Job runner") {
			public void run() {
				spiedInstance.call();
				signal.countDown();
			};
		};
		thread.start();

		((AwaitMockStateHandler)stateHandlers.get(JobRunnerState.PENDING.getStateName())).getAwaitSignal().countDown();
		((AwaitMockStateHandler)stateHandlers.get("task-pending")).getAwaitSignal().countDown();
		
		AwaitMockStateHandler handler = ((AwaitMockStateHandler)stateHandlers.get("task-pending"));
		
		while(!handler.isDone()) 
		{
			Thread.sleep(1000);
		}
		
		connector.handleCancelMessage(job.getExecutionRequest().getRequestId().toString());
		
		((AwaitMockStateHandler)stateHandlers.get("task-preparing")).getAwaitSignal().countDown();
		
		signal.await();
		connector.handleResultMessage(spiedInstance.getResultMessage());
		
		SCXMLProcessingCancellationHandler cancellationHandler = (((SCXMLDriverImpl)jobRunner.getDriver()).getCancellationHandler());
		DefaultStateHandler cancelledStateHandler = ((DefaultStateHandler)((SCXMLDriverImpl)jobRunner.getDriver()).getStateHandlers().get(JobRunnerState.CANCELLED.getStateName()));
		
		verify(spiedInstance).cancelProcessing();
		assertEquals(DriverProcessingStatus.CANCELLED,spiedInstance.getDriver().getProcessingStatus());
		verify(cancelledStateHandler).call();
		verify(cancellationHandler).execute();
	}
	
	
}
