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
package com.mango.mif.core.messaging;

import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.services.JobManagementService;

/**
 * 
 * Resolves a cancellation queue based on the job id and connectors registry
 */
public class CancellationQueueResolver extends BaseQueueResolver {
	
	private JobManagementService jobManagementService;
	
	@Override
	public String resolve(String message) {
		
		Job job = jobManagementService.getJob(message);
		if(job == null) {
			LOG.warn("Received cancellation request for a job that has not yet been scheduled.");
			return "";
		}
		Connector connector = connectorsRegistry.getConnectorByExecutionType(job.getExecutionRequest().getType());
		return connector.getCancellationQueue();
	}

	public JobManagementService getJobManagementService() {
		return jobManagementService;
	}

	public void setJobManagementService(JobManagementService jobManagementService) {
		this.jobManagementService = jobManagementService;
	}	
}
