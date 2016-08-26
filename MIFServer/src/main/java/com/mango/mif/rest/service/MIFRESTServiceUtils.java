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
package com.mango.mif.rest.service;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;

import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.exception.MIFException;


/**
 * This class contains utility functions used across MIF rest services
 * it ensures that the same logic is applied 
 */
public class MIFRESTServiceUtils {

    public static final String MSG_ERR_NO_JOB = "Unable to find job details for %s";

    public static final byte[] EMPTY_FILE_CONTENT = new byte[0];
    /**
     * Copies details of the 'from' detailed status to 'to' detailed status
     * @param from
     * @param to
     */
    public static void copyDetails(DetailedStatus from, DetailedStatus to) {
        to.setRequestId(from.getRequestId());
        to.setSummary(from.getSummary());
    }

    /**
     * Retrieves job with the given jobID from the job management service
     * @param jobManagementService - a job management service instance
     * @param jobId - job that should be retrieved
     * @return the job with the given job id
     * @throws MIFException if job does not exist
     */
    public static Job getJob(JobManagementService jobManagementService, String jobId) throws MIFException {
        Job job = jobManagementService.getJob(jobId);
        if (job == null) {
            throw new MIFException(String.format("Job %s does not exist. ",jobId));
        }
        return job;
    }

    /**
     * Retrieves file contents
     * @param invokerHelper - invoker helper that should be used to retrieve the contents of the file
     * @param file which contents should be retrieved
     * @return a byte array with content of the file encoded in UTF-8
     * @throws MIFException if content of the file could not be retrieved
     */
    public static byte[] getFileContentsForJob(InvokerHelper invokerHelper, File file) throws MIFException {
        byte[] fileContents = EMPTY_FILE_CONTENT;

        try {
            String contents = invokerHelper.getFileContents(file);
            if (!StringUtils.isBlank(contents)) {
                try {
                    return contents.getBytes(Charsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    throw new MIFException(String.format("Problem retrieving file %s contents.", file), e);
                }
            }
        } catch (ExecutionException e) {
            throw new MIFException(String.format("Problem retrieving file %s contents.", file), e);
        }
        return fileContents;
    }

    /**
     * Retrieves detailed status object for a given job
     * @param job from which detailed status should be retrieved
     * @return detailed status for a given job
     * @throws MIFException if detailed status is null or there is an error
     */
    public static DetailedStatus retrieveDetailedStatus(Job job) throws MIFException {
        DetailedStatus detailedStatus = null;
        try {
            detailedStatus = job.getDetailedStatus();
        } catch (MIFException e) {
            throw new MIFException(String.format("Could not retrieve detailed status for the request %s.",job.getJobId()), e);
        }
        if (detailedStatus == null) {
            throw new MIFException(String.format("Detailed status for jobId %s does not exist. ",job.getJobId()));
        }
        return detailedStatus;
    }
}
