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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.task.AsyncTaskExecutor;

import com.mango.mif.connector.runner.impl.DefaultJobRunner;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.impl.SCXMLDriver;
import com.mango.mif.connector.runner.impl.SCXMLDriverImpl;
import com.mango.mif.connector.runner.impl.statehandler.DefaultStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.PendingStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerException;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.services.JobManagementService;


/**
 * 
 * Tests Pending state handler
 */
public class PendingStateHandlerTest {
	
	private PendingStateHandler stateHandler;
	@Mock private JobManagementService jobManagementService;
	@Mock private DefaultJobRunner jobRunner;
    @Mock private AsyncTaskExecutor stateHandlersExecutor;
	 private SCXMLDriverImpl sCXMLDriver;
    private Job job;
	
	@SuppressWarnings("unchecked")
    @Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		sCXMLDriver = new SCXMLDriverImpl();
		sCXMLDriver.setStateHandlersExecutor(stateHandlersExecutor);
		sCXMLDriver.setSCXMLDocument(PendingStateHandlerTest.class.getResource("/META-INF/JobRunnerWithFailover.scxml"));
		sCXMLDriver.registerHandler(new DefaultStateHandler(JobRunnerState.PENDING.getStateName(), SCXMLDriver.NULL_EVENT));
        sCXMLDriver.registerHandler(new DefaultStateHandler(JobRunnerState.RUNNING.getStateName(), SCXMLDriver.NULL_EVENT));
		stateHandler = new PendingStateHandler(JobRunnerState.TASK_PENDING.getStateName(), JobRunnerState.TASK_PREPARING.getTriggeringEvent());
		stateHandler.setJobManagementService(jobManagementService);
		stateHandler.setJobRunner(jobRunner);
		stateHandler.setSCXMLDriver(sCXMLDriver);
		sCXMLDriver.registerHandler(stateHandler);
		sCXMLDriver.fireEvent(JobRunnerState.RUNNING.getTriggeringEvent());
		

        job = new Job();
        job.setJobId("1000");
        when(jobRunner.getJobId()).thenReturn(job.getJobId());
        when(jobManagementService.getJob((String)any())).thenReturn(job);
        when(jobManagementService.saveJob(job)).thenReturn(job);
        when(jobManagementService.saveJobRunnerState((String)any(), (JobRunnerState)any())).thenReturn(job);
        when(jobManagementService.update((String)any(),(ObjectStateUpdater<Job>) any())).thenReturn(job);
	}
	
	

	@Test
	public void doPreprocessing_should_result_in_pending_state_if_job_in_new_state() throws StateHandlerException {
        
        job.setJobRunnerState(JobRunnerState.UNDEFINED);
        
		stateHandler.doPreprocessing();
        verify(jobManagementService).saveJobRunnerState(job.getJobId(), JobRunnerState.TASK_PENDING);
        job.setJobRunnerState(JobRunnerState.TASK_PENDING);
		
        stateHandler.doProcessing();
        
		assertEquals(JobRunnerState.TASK_PREPARING.getTriggeringEvent(),stateHandler.complete());
		
	}
	
	@Test
	public void shouldResultInTaskMonitoringEvent() {
		
		job.setJobRunnerState(JobRunnerState.TASK_MONITORING);
        sCXMLDriver.registerHandler(new DefaultStateHandler(JobRunnerState.TASK_MONITORING.getStateName(), SCXMLDriver.NULL_EVENT));
		
		stateHandler.call();

        assertEquals(JobRunnerState.TASK_MONITORING.getTriggeringEvent(),stateHandler.complete());
        assertEquals(JobRunnerState.TASK_MONITORING,job.getJobRunnerState());
        sCXMLDriver.inState(JobRunnerState.TASK_MONITORING.getStateName());
	}
	
	   @Test
	    public void  shouldResultInTaskPostProcessingEvent() {
	        
	        job.setJobRunnerState(JobRunnerState.TASK_POSTPROCESSING);
	        sCXMLDriver.registerHandler(new DefaultStateHandler(JobRunnerState.TASK_POSTPROCESSING.getStateName(), SCXMLDriver.NULL_EVENT));
	        
	        stateHandler.call();

	        assertEquals(JobRunnerState.TASK_POSTPROCESSING.getTriggeringEvent(),stateHandler.complete());
	        assertEquals(JobRunnerState.TASK_POSTPROCESSING,job.getJobRunnerState());
	        sCXMLDriver.inState(JobRunnerState.TASK_POSTPROCESSING.getStateName());
	    }
	
	@Test
	public void shouldResultInFinalFinishedEvent() {

        Job job = new Job();
        job.setJobId("1000");
        job.setJobRunnerState(JobRunnerState.FINISHED);
        sCXMLDriver.registerHandler(new DefaultStateHandler(JobRunnerState.FINISHED.getStateName(), SCXMLDriver.NULL_EVENT));
        when(jobRunner.getJobId()).thenReturn(job.getJobId());
        when(jobManagementService.getJob((String)any())).thenReturn(job);
        when(jobManagementService.saveJob(job)).thenReturn(job);
        
        stateHandler.call();

        assertEquals(JobRunnerState.FINISHED.getTriggeringEvent(),stateHandler.complete());
        assertEquals(JobRunnerState.FINISHED,job.getJobRunnerState());
        sCXMLDriver.inState(JobRunnerState.FINISHED.getStateName());
	}
	
	@Test
	public void shouldResultInFailedEvent() {

        job.setJobRunnerState(JobRunnerState.FAILED);
        sCXMLDriver.registerHandler(new DefaultStateHandler(JobRunnerState.FAILED.getStateName(), SCXMLDriver.NULL_EVENT));
        
        stateHandler.call();

        assertEquals(JobRunnerState.FAILED.getTriggeringEvent(),stateHandler.complete());
        assertEquals(JobRunnerState.FAILED,job.getJobRunnerState());
        sCXMLDriver.inState(JobRunnerState.FAILED.getStateName());
	}

}
