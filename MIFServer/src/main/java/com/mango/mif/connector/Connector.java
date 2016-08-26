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
package com.mango.mif.connector;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.springframework.beans.factory.annotation.Required;

public interface Connector extends MessageListener {

	/**
	 * @return the unique identifier of this Connector.
	 */
	String getConnectorId();

	/**
	 * Identifies unfinished jobs each time the connector is started and re-runs them.
	 */
	void doFailureRecovery();

	/**
	 * Consumes the request posted by Navigator, Creates a Job request, 
	 * it then creates a Job Runner to handle the request, It also adds the Job Runner 
	 * to the  Registry  and submits the Job to the Task Executor for execution.
	 * 
	 * {@inheritDoc}
	 * 
	 */
	@Override
	void onMessage(Message message);

	String getResponseQueue();

	@Required
	void setResponseQueue(String destination);

    String getInternalQueue();

    @Required
    void setInternalQueue(String internalQueue);
    
	@Required
	void setCancellationQueue(String cancellationQueue);

	@Required
	void setRequestQueue(String requestQueue);

	String getCancellationQueue();

	String getRequestQueue();

	/**
	 * @return a string with metrics information specific to the connector implementation. 
	 */
	String getRuntimeMetrics();
	
}
