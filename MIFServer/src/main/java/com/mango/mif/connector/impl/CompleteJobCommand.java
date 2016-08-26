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
package com.mango.mif.connector.impl;

import com.google.common.base.Function;
import com.mango.mif.core.domain.Job;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.JobStatus;


/**
 * 
 * A command responsible for performing update on completed job.
 */
class CompleteJobCommand implements Function<Job, Job> {
    private ExecutionResponse executionResponse;
    /**
     * 
     * @param executionResponse execution response
     */
    public CompleteJobCommand(ExecutionResponse executionResponse) {
        super();
        this.executionResponse = executionResponse;
    }


    @Override
    public Job apply(Job job) {
        job.setExecutionResponse(executionResponse);
        job.setJobStatus(JobStatus.valueOf(executionResponse.getStatus()));
        return job;
    }
}
