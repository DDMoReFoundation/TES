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
package com.mango.mif.connector.runner;

import java.util.concurrent.Callable;

import com.mango.mif.connector.runner.impl.JobMessage;

/**
 * Classes implementing the interface are responsible for execution of a job
 * 
 */
public interface JobRunner extends Callable<JobMessage> {
    /**
     * Cancels processing
     */
    void cancelProcessing();
    /**
     * Starts processing.
     */
    void startProcessing();
    /**
     * Sets the job id that the runner should process
     * @param job
     */
    void setJobId(String jobId);
    /**
     * 
     * @return jobId
     */
    String getJobId();
}
