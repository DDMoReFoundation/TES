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
package com.mango.mif.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;
import com.mango.mif.connector.impl.CommandExecutionConnector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.JobInvokerProvider;
import com.mango.mif.domain.JobStatus;


/**
 * 
 * Integration test testing transactional-aspects of the JobManagementService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/spring/test-hibernate-context.xml",
        "/com/mango/mif/core/services/JobManagementServiceIntegrationTest-context.xml"
     })
public class JobManagementServiceIntegrationTest {
    
    private final static Logger LOG = Logger.getLogger(JobManagementServiceIntegrationTest.class);

    protected static final String TEST_METADATA_KEY = "TEST-METADATA-KEY";
    
    @Resource(name = "jobManagementService")
    private JobManagementServiceImpl jobManagementService;

    private Invoker invoker;

    private JobInvokerProvider jobInvokerProvider;
    
    @Autowired
    private ConnectorsRegistry mockConnectorsRegistry;
    
    @Before
    public void setUp() {
        
        invoker = mock(Invoker.class);
        jobInvokerProvider = mock(JobInvokerProvider.class);
        
        jobManagementService.setJobInvokerProvider(jobInvokerProvider);
        jobManagementService.setMifServiceAcccountPassword("MOCK-PASSWORD");
        jobManagementService.setMifServiceAccountUserName("MOCK-USER");
        
        assertNotNull("A (mock) ConnectorsRegistry should have been created by Spring",
        	this.jobManagementService.getJobRepository().getConnectorsRegistry());
        assertSame("The singleton (Mock) ConnectorsRegistry should have also been injected into this test class for ease of use",
        	this.jobManagementService.getJobRepository().getConnectorsRegistry(), mockConnectorsRegistry);
    }
    
    @Test
    public void should_run_long_and_short_transactions_on_the_same_job_in_parallel() throws InterruptedException {

    	final String CONNECTOR_ID = "MYCONNECTORID1";
    	
        Job job = jobManagementService.createNewJob();
        String jobId = "1234";
        job.setJobId(jobId);
		job.setConnectorId(CONNECTOR_ID);
        
        jobManagementService.saveJob(job);
    
        int commandCounter = 1;
        ObjectStateUpdater<Job> longStateUpdater = new ObjectStateUpdater<Job>();
        long longCommandSleep = 1000;
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));
    
    
        UpdatePerformer longTransactionPerformer = new UpdatePerformer(jobManagementService,jobId, longStateUpdater);
        
        
        ObjectStateUpdater<Job> shortStateUpdater = new ObjectStateUpdater<Job>();
        long shortCommandSleep = 200;
        shortStateUpdater.addCommand(createUpdateCommand("SHORT-COMMAND-" + commandCounter++, shortCommandSleep));
        shortStateUpdater.addCommand(createUpdateCommand("SHORT-COMMAND-" + commandCounter++, shortCommandSleep));
        shortStateUpdater.addCommand(createUpdateCommand("SHORT-COMMAND-" + commandCounter++, shortCommandSleep));
        shortStateUpdater.addCommand(createUpdateCommand("SHORT-COMMAND-" + commandCounter++, shortCommandSleep));
        shortStateUpdater.addCommand(createUpdateCommand("SHORT-COMMAND-" + commandCounter++, shortCommandSleep));


        final CommandExecutionConnector mockConnector = mock(CommandExecutionConnector.class);
        stub(this.mockConnectorsRegistry.getConnectorById(CONNECTOR_ID)).toReturn(mockConnector);
        
        
        UpdatePerformer shortTransactionPerformer = new UpdatePerformer(jobManagementService,jobId, shortStateUpdater);
    
    
        longTransactionPerformer.start();
        
        Thread.sleep(1000);
        shortTransactionPerformer.start();
        
        
        longTransactionPerformer.join();
        shortTransactionPerformer.join();
        
        assertNull(longTransactionPerformer.getError());
        assertNull(shortTransactionPerformer.getError());
        
        verify(this.mockConnectorsRegistry, atLeastOnce()).getConnectorById(CONNECTOR_ID);
        verifyNoMoreInteractions(this.mockConnectorsRegistry);
    }

    @Test
    public void should_run_long_transactions_and_perform_status_update_on_the_same_job_in_parallel() throws InterruptedException {

    	final String CONNECTOR_ID = "MYCONNECTORID2";
    	
        Job job = jobManagementService.createNewJob();
        String jobId = "12345";
        job.setJobId(jobId);
		job.setConnectorId(CONNECTOR_ID);
        
        jobManagementService.saveJob(job);
    
        int commandCounter = 1;
        ObjectStateUpdater<Job> longStateUpdater = new ObjectStateUpdater<Job>();
        long longCommandSleep = 1000;
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));
        longStateUpdater.addCommand(createUpdateCommand("LONG-COMMAND-" + commandCounter++, longCommandSleep));


        final CommandExecutionConnector mockConnector = mock(CommandExecutionConnector.class);
        stub(this.mockConnectorsRegistry.getConnectorById(CONNECTOR_ID)).toReturn(mockConnector);
        
    
        UpdatePerformer longTransactionPerformer = new UpdatePerformer(jobManagementService,jobId, longStateUpdater);
        
        longTransactionPerformer.start();
        
        
        Thread.sleep(1000);
        
        jobManagementService.saveJobStatus(jobId,JobStatus.CANCELLED.name());
        
        longTransactionPerformer.join();
        
        
        assertEquals(JobStatus.CANCELLED.name(), jobManagementService.getJob(jobId).getClientRequestStatus());
        
        verify(this.mockConnectorsRegistry, atLeastOnce()).getConnectorById(CONNECTOR_ID);
        verifyNoMoreInteractions(this.mockConnectorsRegistry);
    }
    

    @Test
    public void should_handle_race_condition() throws InterruptedException {
    
    	final String CONNECTOR_ID = "MYCONNECTORID3";

        Job job = jobManagementService.createNewJob();
        final String jobId = "123456";
        job.setJobId(jobId);
        job.setConnectorId(CONNECTOR_ID);
        
        jobManagementService.saveJob(job);
        
        
        final CommandExecutionConnector mockConnector = mock(CommandExecutionConnector.class);
        stub(this.mockConnectorsRegistry.getConnectorById(CONNECTOR_ID)).toReturn(mockConnector);
        
    
        final int UPDATERS_NO = 50;
        JobStatusUpdater[] updaters = new JobStatusUpdater[UPDATERS_NO];
        for(int i=0;i<UPDATERS_NO;i++) {
            updaters[i] = new JobStatusUpdater(jobManagementService,jobId);
        }
        
        
        Thread.sleep(1000);
        
        for(Thread t : updaters) {
            t.start();
            
        }

        for(Thread t : updaters) {
            t.join();
            
        }


        for(JobStatusUpdater t : updaters) {
            assertNull(t.getError());
        }
        
        
        verify(this.mockConnectorsRegistry, atLeastOnce()).getConnectorById(CONNECTOR_ID);
        verifyNoMoreInteractions(this.mockConnectorsRegistry);
    }
    
    private Function<Job,Job> createUpdateCommand(final String commandId, final long sleep) {
        return new Function<Job,Job>() {
            @Override
            public Job apply(Job input) {
                
                LOG.debug(String.format("Executing command %s taking %s msec on job %s",commandId,sleep, input.getJobId()));
                
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
                
                input.setProperty(TEST_METADATA_KEY, commandId);
                
                return input;
            }
        };
    }
    
    
    private class UpdatePerformer extends Thread {
        private final JobManagementService jobManagementService;
        private final String jobId;
        private final ObjectStateUpdater<Job> jobUpdater;
        private Throwable error;
        
        public UpdatePerformer(JobManagementService jobManagementService, String jobId, ObjectStateUpdater<Job> jobUpdater) {
            super();
            this.jobManagementService = jobManagementService;
            this.jobId = jobId;
            this.jobUpdater = jobUpdater;
        }

        public void run() {
            try {
            	jobManagementService.update(jobId, jobUpdater);
            } catch(Throwable th) {
                error = th;
                LOG.error(th, th);
            }
        }
        
        
        public Throwable getError() {
            return error;
        }
    }
    
    private class JobStatusUpdater extends Thread {
        private final JobManagementService jobManagementService;
        private final String jobId;
        private Throwable error;
        
        public JobStatusUpdater(JobManagementService jobManagementService, String jobId) {
            super();
            this.jobManagementService = jobManagementService;
            this.jobId = jobId;
        }

        public void run() {
            try {
                jobManagementService.saveJobStatus(jobId,JobStatus.CANCELLED.name());
            } catch(Throwable th) {
                error = th;
                LOG.error(th, th);
            }
        }
        
        
        public Throwable getError() {
            return error;
        }
    }
}
