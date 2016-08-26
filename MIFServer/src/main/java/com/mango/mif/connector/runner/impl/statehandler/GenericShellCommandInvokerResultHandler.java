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
package com.mango.mif.connector.runner.impl.statehandler;

import com.google.common.base.Preconditions;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;


/**
 * Base implementation of invoker result parser common methods
 *
 */
public class GenericShellCommandInvokerResultHandler extends ShellCommandInvokerResultHandler implements JobProcessingAwareInvokerResultHandler {
	/**
	 * A job
	 */
	private Job job;
	/**
	 * state exit event
	 */
	private String stateExitEvent;
	private ObjectStateUpdater<Job> jobUpdater;

	/**
	 * Constructor
	 * @param exitEvent
	 */
	public GenericShellCommandInvokerResultHandler(String stateExitEvent) {
		this.stateExitEvent = stateExitEvent;
	}
	
	@Override
	public void setJob(Job job) {
		this.job = job;
	}

	@Override
	public void handle(InvokerResult invokerResult) throws ExecutionException {
        Preconditions.checkNotNull(job);
		super.handle(invokerResult);
	}
	
	@Override
	public String getStateExitEvent() {
		return stateExitEvent;
	}
	
	
    public void setStateExitEvent(String stateExitEvent) {
        this.stateExitEvent = stateExitEvent;
    }
    /**
     * That object is not persisted. To update job's persisted state use {@link #getJobUpdater()} instead.
     * @return runtime job state
     */
    protected Job getJob() {
        return job;
    }

    /**
     * Updater that should be used to schedule job state update commands.
     * @return
     */
    protected ObjectStateUpdater<Job> getJobUpdater() {
        return jobUpdater;
    }

	@Override
	public void setJobUpdater(ObjectStateUpdater<Job> jobUpdater) {
		this.jobUpdater = jobUpdater;
	}

}
