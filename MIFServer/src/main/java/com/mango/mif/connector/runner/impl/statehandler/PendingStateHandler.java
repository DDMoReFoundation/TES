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

import org.apache.log4j.Logger;
import com.google.common.base.Preconditions;
import com.mango.mif.connector.runner.impl.JobRunnerState;

/**
 * 
 * Pending state handler - Pending is the initial state of the state machine.
 * Responsible for Failure recovery - checks if job received was previously being
 * processed during a system failure.
 * 
 */
public class PendingStateHandler extends DefaultStateHandler {

    public final static Logger LOG = Logger.getLogger(PendingStateHandler.class);

    /**
     * Default Constructor
     * 
     * @param state
     * @param exitEvent
     */
    public PendingStateHandler(String state, String exitEvent) {
        super(state, exitEvent);
    }

    /**
     * Checks if job is in final state and if not it proceeds with execution, if not it does nothing. JobRunner
     */
    @Override
    protected void doProcessing() {
        Preconditions.checkNotNull(getJob(), "Job not set");
        JobRunnerState jobRunnerState = getJob().getJobRunnerState();
        if (!JobRunnerState.TASK_PENDING.equals(jobRunnerState)) {
            exitEvent = jobRunnerState.getTriggeringEvent();
        }
    }

    @Override
    protected void doPreprocessing() throws StateHandlerException {
        setJob(getJobManagementService().getJob(getJobRunner().getJobId()));
    	JobRunnerState runnerState = JobRunnerState.byStateName(handledState);
        if (runnerState.compareTo(getJob().getJobRunnerState()) > 0) {
            //save only if job has been saved last saved at earlier state
            setJob(getJobManagementService().saveJobRunnerState(getJob().getJobId(),runnerState));
        } else {
            LOG.debug("Not updating STATE of the job " + getJob().getJobId() + ", failure recovery detected");
        }
    }

}
