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

import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.exception.MIFException;

/**
 * JobManagementService provides management services over the persistent job entity. 
 */
public interface JobManagementService {

    /**
     * Creates a job object.
     * @param job is the job to create
     * @return the newly created job
     */
    Job saveJob(Job job);

    /**
     * Save the state of the job runner, see {@link JobRunnerState}.
     * @param jobId is the id of the job
     * @param internalJobState is the internal SCXML job state to save
     */
    Job saveJobRunnerState(String jobId, JobRunnerState internalJobState);
    
    /**
     * Save the status of the job
     * @param jobId is the id of the job
     * @param status is the status of the job
     */
    Job saveJobStatus(String jobId, String status);
    
    /**
     * Get the job given the job id
     * @param jobId is the id of the job
     * @return the job
     */
    Job getJob(String jobId);
    
    /**
     * Get the uncompleted dispatcher jobs for a given connector
     * @param connectorId is the id of the connector
     * @return the list of jobs
     */
    List<Job> getUncompletedConnectorJobs(String connectorId);
    
    /**
     * Get the uncompleted jobs for all dispatchers.
     * @return the list of jobs
     */
    List<Job> getUncompletedJobs();
    
    /**
     * Get the summary messages for the running jobs
     * @return a map of jobid:summary messages
     */
    Map<String,String> getSummaryMessagesForRunningJobs();
    
    /**
     * Get the detailed status from a persistent job object
     * @param jobId is the job id
     * @return the XML detailed status
     * @throws MIFException
     */
    DetailedStatus getDetailedStatus(String jobId) throws MIFException;
    
    /**
     * Create a new non-persistent job.
     * @return a job
     */
    Job createNewJob();

    /**
     * Get all the jobs that have been marked as acknowledged after specified date
     * 
     * @param date
     * @return list of job ids
     */
    String[] getAcknowledgedJobsToDate(Long uptoTime);
    
    /**
     * Adds a given metadata to a job.
     * @param jobId
     * @param propertyName
     * @param propertyValue
     * @return new state of a job
     */
    Job addMetadataToJob(String jobId, String propertyName, String propertyValue);
    
    /**
     * Applies changes specified by JobUpdater on a job with the given id and persists the new state
     * 
     * @param jobId
     * @param jobUpdater
     * @return new job state
     */
    Job update(String jobId, ObjectStateUpdater<Job> jobUpdater);
    
}