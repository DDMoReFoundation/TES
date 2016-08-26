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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.dao.impl.JobRepository;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.JobInvokerProvider;
import com.mango.mif.core.services.JobManagementServiceImpl;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionType;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.exception.MIFException;

/**
 * Tests JobManagementServiceImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class JobManagementServiceImplTest {
    @InjectMocks
    JobManagementServiceImpl jobManagementService;
    @Mock Invoker invoker;

    @Mock JobInvokerProvider jobInvokerProvider;

    @Mock JobRepository jobRepository;

    private final static String MOCK_PASSWORD = "MOCK_PASSWORD";
    private final static String MOCK_USER = "MOCK_USER";
    @Before
    public void setUp() {
        jobManagementService.setMifServiceAcccountPassword(MOCK_PASSWORD);
        jobManagementService.setMifServiceAccountUserName(MOCK_USER);
        
    }
    
    @Test
    public void createNewJob_should_create_new_job() throws ExecutionException {
        Job job = jobManagementService.createNewJob();
        
        assertNotNull(job);
        assertEquals(MOCK_PASSWORD, job.getPassword());
        assertEquals(MOCK_USER, job.getUserName());
        assertEquals(JobStatus.NEW, job.getJobStatus());
        verify(jobInvokerProvider).createInvoker(job);
    }
    
    @Test
    public void saveJob_should_save_job() throws ExecutionException, MIFException, JAXBException {
        Job job = createJob();
        jobManagementService.saveJob(job);
        verify(jobRepository).save(job);
    }


    @Test(expected=NullPointerException.class)
    public void createNewJob_should_throw_exception_if_service_account_not_set() {
        jobManagementService.setMifServiceAccountUserName(null);
        jobManagementService.createNewJob();
    }

    @Test
    public void addMetadataToJob_should_update_metadata_on_a_job() {
        String jobId = "1";
        String propertyName = "MOCK-PROPERTY";
        String propertyValue = "MOCK-VALUE";
        Job job = mock(Job.class);
        when(jobRepository.getJobForUpdate("1")).thenReturn(job);
        
        Job saved = mock(Job.class);
        when(jobRepository.save(job)).thenReturn(saved);
        
        jobManagementService.addMetadataToJob(jobId, propertyName, propertyValue);
        
        
        verify(job).setProperty(propertyName, propertyValue);
        verify(jobRepository).save(job);
    }

    @Test
    public void initJob_should_initialize_job() throws ExecutionException {
        Job job = mock(Job.class);
        when(jobInvokerProvider.createInvoker(job)).thenReturn(invoker);
        jobManagementService.initJob(job);
        
        verify(job).setInvoker(invoker);
    }
    
    @Test
    public void update_should_apply_changes_specified_by_jobUpdater() {
        Job job = mock(Job.class);
        
        when(jobRepository.getJobForUpdate((String)any())).thenReturn(job);
        
        @SuppressWarnings("unchecked")
        ObjectStateUpdater<Job> jobUpdater = mock(ObjectStateUpdater.class);
        
        jobManagementService.update(job.getJobId(), jobUpdater);
        
        verify(jobUpdater).update(job);
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
        String clientReqMessage = new ExecutionRequestBuilder().setRequestId(randomUUID).setExecutionType(ExecutionType.R_Script.name())
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE").getExecutionRequestMsg();
        job.setExecutionRequestMsg(clientReqMessage);
        job.setJobId(randomUUID.toString());
        DetailedStatus detailedStatus = new DetailedStatus();
        detailedStatus.setRequestId(job.getJobId());
        detailedStatus.setSummary("TEST_SUMMARY_MESSAGE_OF_JOB_"+randomUUID);
        job.setDetailedStatusMsg(detailedStatus.asString());
        return job;
    }
    
}