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

import java.util.Date;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.annotation.Scheduled;

public class DataPurgeService {

	public static final String BEAN_ID 			= "DataPurgeService";
	public static final String DAILY_OFF_PEAK 	= "0 23 * * *";
	
	private static final String MARKER = "####################";
	private static final Logger LOG = Logger.getLogger(DataPurgeService.class);
	
	/**
	 * The maximum number of days after which the data for finished job will be purged 
	 */
	private Integer maxRetainDays;
	
	private JobManagementService jobManagementService;
	
	@Scheduled(cron = DAILY_OFF_PEAK)
	public void purgeData() {
		logInfo("Starting Job Data Purge");
		
		Date date = new Date();
		//List<Job> jobsToPurge =  jobManagementService.getJobsFinishedBefore(date);
	}

	private void logInfo(String msg) {
		LOG.info(String.format("%s %s %s", MARKER, BEAN_ID, MARKER));
		LOG.info("		" + msg);
	}

	@Required
	public void setMaxRetainDays(Integer maxRetainDays) {
		logInfo(String.format("Keeping job data for a maximum of %s days", maxRetainDays));
		this.maxRetainDays = maxRetainDays;
	}

	public Integer getMaxRetainDays() {
		return maxRetainDays;
	}

	@Required
	public void setJobManagementService(JobManagementService jobManagementService) {
		this.jobManagementService = jobManagementService;
	}

	public JobManagementService getJobManagementService() {
		return jobManagementService;
	}
}
