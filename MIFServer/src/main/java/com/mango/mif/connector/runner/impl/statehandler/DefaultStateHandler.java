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

import org.springframework.beans.factory.annotation.Required;

import com.mango.mif.connector.runner.impl.AbstractStateHandler;
import com.mango.mif.connector.runner.impl.DefaultJobRunner;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.services.JobManagementService;

/**
 * A default state handler that does not do any processing
 */
public class DefaultStateHandler extends AbstractStateHandler {
    
    private JobManagementService jobManagementService;
    private Job job;
    
    private DefaultJobRunner jobRunner;
    
	private ObjectStateUpdater<Job>    jobUpdater = new ObjectStateUpdater<Job>();
    
    /**
     * Constructor
     * @param state
     * @param exitEvent
     */
    public DefaultStateHandler(String state, String exitEvent) {
        super(state, exitEvent);
    }

    /**
     * Constructor
	 * @param state
	 */
	public DefaultStateHandler(String state) {
		super(state);
	}

	@Override
    protected void doProcessing() throws StateHandlerException {
        LOG.debug("Processing " + getState());
    }	
	
    @Override
    public String failed() {
        return JobRunnerState.FAILED.getTriggeringEvent();
    }

    @Override
    protected void doPreprocessing() throws StateHandlerException {
        LOG.debug("Preprocessing " + getState());
        job = jobManagementService.getJob(jobRunner.getJobId());
    }

    @Override
    protected void doPostProcessing() throws StateHandlerException {
        LOG.debug("PostProcessing " + getState());
    }

    @Override
    public void cancelProcessing() {
        LOG.debug("cancelProcessing " + getState());
    }
    
    public JobManagementService getJobManagementService() {
		return jobManagementService;
	}

    @Required
	public void setJobManagementService(JobManagementService jobManagementService) {
		this.jobManagementService = jobManagementService;
	}
    
    public void setJobRunner(DefaultJobRunner jobRunner) {
        this.jobRunner = jobRunner;
    }
    
    
    public DefaultJobRunner getJobRunner() {
        return jobRunner;
    }

    /**
     * Runtime job instance - it is not intended to be persisted. For use by state handler to handle interim changes to job state.
     * Any updates that should be performed on the persisted object should be recorded using the jobUpdater which then are flushed
     * on safe point.
     */
    protected Job getJob() {
        return job;
    }
    /**
     * Sets the runtime job instance
     * @see DefaultStateHandler#getJob()
     * @param job
     */
    protected void setJob(Job job) {
        this.job = job;
    }
    
    @Override
    public String complete() {
        job = getJobManagementService().update(getJob().getJobId(),jobUpdater);
        return super.complete();
    }
    /**
     * 
     * @return updater that should be used to record changes that should be then made on Job instance at safe-point
     */
	public ObjectStateUpdater<Job> getJobUpdater() {
		return jobUpdater;
	}
	
	public void setJobUpdater(ObjectStateUpdater<Job> jobUpdater) {
		this.jobUpdater = jobUpdater;
	}
}
