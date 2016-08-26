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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.core.dao.impl.JobRepository;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.JobInvokerProvider;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.exception.MIFException;

/**
 * JobManagementService provides management services over the persistent job entity.
 */
public class JobManagementServiceImpl implements JobManagementService {
    
    private static final Logger LOG = Logger.getLogger(JobManagementServiceImpl.class);
    
    private JobRepository jobRepository;
   
    private String mifServiceAccountUserName;
    private String mifServiceAcccountPassword;
    private JobInvokerProvider jobInvokerProvider;
    
    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Job saveJob(Job job) {
        
        LOG.debug("Save job: " + job.toString());
        
        logTxStatus();
        
        Job saved = jobRepository.save(job);
        initJob(saved);
        
        return saved;
    }
    
    @Override
    public Job createNewJob() {
        Preconditions.checkNotNull(mifServiceAccountUserName,"MIF service account user name not set");
        Preconditions.checkNotNull(mifServiceAcccountPassword,"MIF service account user password not set");
        
        Job job = new Job();
        job.setUserName(getMifServiceAccountUserName());
        job.setPassword(getMifServiceAcccountPassword());
        job.setJobStatus(JobStatus.NEW);
        setJobInvoker(job);
        return job;
    }
    

    @Override
    @Transactional(readOnly=true)
    public List<Job> getUncompletedJobs() {
        
        List<Job> jobs = jobRepository.getUncompletedJobs();        
        for(Job job: jobs) {
            initJob(job);
        }
        return jobs;
    }
    

    @Override
    @Transactional(readOnly=true)
    public List<Job> getUncompletedConnectorJobs(String connectorId) {
        
        List<Job> jobs = jobRepository.getUncompletedConnectorJobs(connectorId);
        for(Job job: jobs) {
            initJob(job);
        }
        return jobs;
    }
    

    @Override
    @Transactional(readOnly=true)    
    public Job getJob(String jobId) {
        
        Job job = jobRepository.getJob(jobId);
        if (job != null) {
            initJob(job);
        }
        return job;
    }       
    

    @Override
    @Transactional(readOnly=true)
    public DetailedStatus getDetailedStatus(String jobId) throws MIFException {
        
        Job job = jobRepository.getJob(jobId);
        if(job==null) {
            return null;
        }
        return job.getDetailedStatus();
    }
    

    @Override
    @Transactional(readOnly=true)
    public Map<String,String> getSummaryMessagesForRunningJobs() {
        
        List<Job> jobs = jobRepository.getUncompletedJobs();
        
        Map<String,String> result = Maps.newHashMap();
        int failureCount = 0;
        for (Job job: jobs) {
            try {
                DetailedStatus detailedStatus = job.getDetailedStatus();
                if(hasSummaryMessage(detailedStatus)) {
                	result.put(job.getJobId(), detailedStatus.getSummary());
                }
            } catch (MIFException mife) {
                // Each job might have some failure  which should not affect
                // retrieval of summary messages for other jobs.
                LOG.warn("Failed in getting detailed status for the Job with jobId - " + job.getJobId(), mife);
                failureCount++;
            }
        }
        if(failureCount > 0)
            LOG.warn("Total no. of failed summary message retrievals = " + failureCount);
        return result;
    }
    
    /**
     * Check if the detailed status has a non empty summary in it
     */
    private boolean hasSummaryMessage(DetailedStatus detailedStatus) {
    	return (detailedStatus != null && StringUtils.isNotEmpty(detailedStatus.getSummary()));
    }
    

    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Job saveJobRunnerState(String jobId, JobRunnerState internalJobState) {
                
        logTxStatus();
        
        Job job = jobRepository.getJobForUpdate(jobId);
        
        job.setProperty(JobRunnerState.STATUS, internalJobState.name()); 
        
        Job saved = jobRepository.save(job);
        initJob(saved);      
         
        LOG.info("Persisted internal JOB RUNNER STATE as " + internalJobState.name() 
            + " on job : " + saved.toString() 
            + " with version " + saved.getVersion());
        
        return saved;
    }
    

    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Job saveJobStatus(String jobId, String status) {
        
        logTxStatus();
        
        Job job = jobRepository.getJobForUpdate(jobId);
        job.setClientRequestStatus(status);
        Job saved = jobRepository.save(job);
        initJob(saved);
        
        LOG.info("Persisted job status as " + status 
            + " on job : " + saved.toString() 
            + " with version " + saved.getVersion());
        
        return saved;
    }

    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Job addMetadataToJob(String jobId, String propertyName, String propertyValue) {
        logTxStatus();
        
        Job job = jobRepository.getJobForUpdate(jobId);
        
        job.setProperty(propertyName, propertyValue);
        
        Job saved = jobRepository.save(job);
        initJob(saved);
        
        LOG.info(String.format("Persisted metadata %s on a job %s with version %s",propertyName, jobId, saved.getVersion()));
        return saved;
    }

    @Override
    @Transactional(readOnly=true)
    public String[] getAcknowledgedJobsToDate(Long uptoTime) {
        
        return jobRepository.getAcknowledgedJobsToDate(uptoTime);     
    }
    
	@Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
	public Job update(String jobId, ObjectStateUpdater<Job> jobUpdater) {

        logTxStatus();
        
        Job job = jobRepository.getJobForUpdate(jobId);
        initJob(job);
        
        jobUpdater.update(job);
        
        
        Job saved = jobRepository.save(job);
        initJob(saved);
        
        LOG.info(String.format("Updated job's state."));
        return saved;
	}
    
    public JobRepository getJobRepository() {
        return jobRepository;
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }
    
    public String getMifServiceAccountUserName() {
        return mifServiceAccountUserName;
    }
    
    public void setMifServiceAccountUserName(String mifServiceAccountUserName) {
        this.mifServiceAccountUserName = mifServiceAccountUserName;
    }
    
    public String getMifServiceAcccountPassword() {
        return mifServiceAcccountPassword;
    }
    
    public void setMifServiceAcccountPassword(String mifServiceAcccountPassword) {
        this.mifServiceAcccountPassword = mifServiceAcccountPassword;
    }
    
    public JobInvokerProvider getJobInvokerProvider() {
        return jobInvokerProvider;
    }
    
    public void setJobInvokerProvider(JobInvokerProvider jobInvokerProvider) {
        this.jobInvokerProvider = jobInvokerProvider;
    }
    
    void initJob(Job job) {
        if(job!=null) {
            setJobInvoker(job);
        }
    }
    
    private void setJobInvoker(Job job) {
        try {
            job.setInvoker(getJobInvokerProvider().createInvoker(job));
        } catch (ExecutionException e) {
            LOG.error(String.format("Could not create invoker for job(%s)", job.getJobId()), e);
        }
    }
        
    private void logTxStatus() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            LOG.debug(String.format("Tx active [ JobManagementService = %s ] [ Thread = %s ]", this.hashCode(), Thread.currentThread().getId()) );
        } else {
            LOG.warn(String.format("No tx Active [ JobManagementService = %s ] [ Thread = %s ]", this.hashCode(), Thread.currentThread().getId()) );
        }
    }
    
}
