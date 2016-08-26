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

import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.InvokerResultHandler;

/**
 * Invoker result handler used by the state handlers
 */
public interface JobProcessingAwareInvokerResultHandler extends InvokerResultHandler {
	/**
	 * Job
	 * @param job
	 */
	void setJob(Job job);
	/**
	 * Job updater used to record commands that should be applied to a job object
	 * @param job
	 */
	void setJobUpdater(ObjectStateUpdater<Job> jobUpdater);
	
	/**
	 * 
	 * @return an event that will be passed to SCXMLDriver to trigger a transition to appropriate state
	 */
	String getStateExitEvent();

}
