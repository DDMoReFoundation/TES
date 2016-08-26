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
package com.mango.mif.core.dao.impl;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.impl.CommandExecutionConnector;
import com.mango.mif.connector.impl.ConnectorWithJobRunners;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionType;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.exception.MIFException;


/**
 * Tests job repository
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"/spring/test-hibernate-context.xml",
		"/com/mango/mif/core/dao/impl/JobRepositoryTest-context.xml"
	 })
public class JobRepositoryTest {
	
	@Resource(name = "jobRepository")
	private JobRepository jobRepository;
	
	@Before
	public void setUp() {
		final ConnectorsRegistry mockConnectorsRegistry = mock(ConnectorsRegistry.class);
		this.jobRepository.setConnectorsRegistry(mockConnectorsRegistry);
		final CommandExecutionConnector mockConnector = PowerMockito.mock(CommandExecutionConnector.class);
		stub(mockConnectorsRegistry.getConnectorById(isA(String.class))).toReturn(mockConnector);
	}

    @Test
    @Transactional
    public void save_should_save_and_retrieve_job() throws MIFException, JAXBException {
        Job job = createJob();
        job.setProperty("MOCK-KEY", "MOCK-VALUE");
        Job savedJob = jobRepository.save(job);
        assertThat("Job ID", savedJob.getJobId(), is(equalTo(job.getJobId())));
        assertNotNull(savedJob.getExecutionRequestMsg());
        assertNotNull(savedJob.getExecutionRequest());
        assertThat("Message Must be equal ", savedJob.getExecutionRequestMsg(), is(equalTo(job.getExecutionRequestMsg())));
        assertNotNull(savedJob.getJobId());
        assertEquals("TEST_SUMMARY_MESSAGE_OF_JOB_"+savedJob.getJobId(),job.getDetailedStatus().getSummary());
        assertNotNull(savedJob.getData().get("MOCK-KEY"));
    }
    
    
    @Test
    @Transactional
    public void getUncompletedJobs_should_return_only_uncompleted_jobs() throws MIFException, JAXBException {
        Job job = null;
        for(JobStatus s : JobStatus.values()) {
            if(JobStatus.NOT_AVAILABLE.equals(s)) continue;
            for(int i=0; i<3;i++) {
                job = createJob();
                job.setConnectorId("blah");
                job.setJobStatus(s);
                jobRepository.save(job);
            }
        }
        List<Job> jobs = jobRepository.getUncompletedJobs();
        assertEquals(12,jobs.size());
    }
    
    @Test
    @Transactional
    public void getUncompletedConnectorJobs_should_return_only_uncompleted_jobs() throws MIFException, JAXBException {
        Job job = null;
        
        String connectorId = "MOCK-CONNECTOR-ID";
        
        String otherConnectorId = "OTHER-CONNECTOR-ID";

        for(JobStatus s : JobStatus.values()) {
            if(JobStatus.NOT_AVAILABLE.equals(s)) continue;
            for(int i=0; i<3;i++) {
                job = createJob();
                job.setJobStatus(s);
                job.setConnectorId(connectorId);
                jobRepository.save(job);
            }
        }

        for(JobStatus s : JobStatus.values()) {
            if(JobStatus.NOT_AVAILABLE.equals(s)) continue;
            for(int i=0; i<3;i++) {
                job = createJob();
                job.setJobStatus(s);
                job.setConnectorId(otherConnectorId);
                jobRepository.save(job);
            }
        }
        List<Job> jobs = jobRepository.getUncompletedConnectorJobs(connectorId);
        assertEquals(12,jobs.size());
    }
    
    @Test
    @Transactional
    public void testRetrievalOfCommandExecutionTarget() throws MIFException, JAXBException {
    
    	final String CONNECTOR_ID = "CONNECTOR-ID";
    	
    	final CommandExecutionTarget commandExecutionTargetFromConnectorsRegistry = new CommandExecutionTarget();
    	final ConnectorWithJobRunners connectorFromConnectorsRegistry = new ConnectorWithJobRunners();
		connectorFromConnectorsRegistry.setConnectorId(CONNECTOR_ID);
    	connectorFromConnectorsRegistry.setCommandExecutionTarget(commandExecutionTargetFromConnectorsRegistry);
    
    	final ConnectorsRegistry mockConnectorsRegistry = mock(ConnectorsRegistry.class);
    	when(mockConnectorsRegistry.getConnectorById(CONNECTOR_ID)).thenReturn(connectorFromConnectorsRegistry);
    	this.jobRepository.setConnectorsRegistry(mockConnectorsRegistry);
    
    	final Job originalJob = createJob();
    	originalJob.setConnectorId(CONNECTOR_ID);
    	final String jobId = originalJob.getJobId();
    	
    	originalJob.setCommandExecutionTarget(new CommandExecutionTarget());
    	
    	this.jobRepository.save(originalJob);
    	
    	Job retrievedJob = this.jobRepository.get(jobId);
    	
    	assertNotSame("CommandExecutionTarget fetched and set as part of Jobs retrieval should be different to the CommandExecutionTarget that was set on the Job initially",
    		originalJob.getCommandExecutionTarget(), retrievedJob.getCommandExecutionTarget());
    	assertSame("CommandExecutionTarget fetched and set as part of Jobs retrieval should be the one from the ConnectorsRegistry",
    		commandExecutionTargetFromConnectorsRegistry, retrievedJob.getCommandExecutionTarget());
    	
    	verify(mockConnectorsRegistry).getConnectorById(CONNECTOR_ID);
    	verifyNoMoreInteractions(mockConnectorsRegistry);
    }
     
    
    /**
     * Creates a test job
     * @return
     * @throws MIFException
     * @throws JAXBException
     */
    private Job createJob() throws MIFException, JAXBException {
        String randomUUID = UUID.randomUUID().toString();
        Job job = new Job();
        String clientReqMessage = new ExecutionRequestBuilder().setRequestId(randomUUID).setExecutionType(ExecutionType.Unknown.name())
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE").getExecutionRequestMsg();
        job.setExecutionRequestMsg(clientReqMessage);
        DetailedStatus detailedStatus = new DetailedStatus();
        detailedStatus.setRequestId(job.getJobId());
        detailedStatus.setSummary("TEST_SUMMARY_MESSAGE_OF_JOB_"+randomUUID);
        job.setDetailedStatusMsg(detailedStatus.asString());
        job.setUserName("MOCK-USER");
        return job;
    }
    
}
