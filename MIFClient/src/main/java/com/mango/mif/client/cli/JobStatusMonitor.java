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
package com.mango.mif.client.cli;


import org.apache.log4j.Logger;

import com.mango.mif.client.api.rest.JobService;

/**
 * Responsible for polling REST service for job update
 */
public class JobStatusMonitor implements Runnable {

	/** The Constant LOG. */
	final static Logger LOG = Logger.getLogger(JobStatusMonitor.class);
	/** The job id. */
	protected final String jobId;

	/** The job service to handle the job requests. */
	protected final JobService jobService;

	/**
	 * Instantiates a new status helper.
	 *
	 * @param jobId the job id
	 * @param latch the latch
	 * @param jobService job service
	 */
	public JobStatusMonitor(String jobId, JobService jobService) {
		this.jobId = jobId;
		this.jobService = jobService;
	}


	/* 
	 * Queries the Rest API of MIF to determine the status of the job submitted
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run(){
		String result = null;
		do{
				try {
					result = jobService.getJobStatus(this.jobId);
					LOG.info(" CURRENT STATUS of the Job with ID " + this.jobId  + "   " + (result));
					Thread.sleep(3000);
					
				} catch (InterruptedException e) {
					LOG.error("Thread Interupted Error ", e);
				} 
		} while(!isDone(result)); 			
		
	}

	/**
	 * 
	 * @param response
	 * @return true if polling should conitnue
	 */
	protected boolean isDone(String response) {
        return response==null;
    }
}