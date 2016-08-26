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
package com.mango.mif.client.api;

import java.util.UUID;

import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.exception.MIFException;

/**
 * This interface would be responsible for defining the components managing the requests and responses that are submitted/received to the MIF. 
 */
public interface TaskExecutionManager {
	
	/**
	 * Responsible for submitting the Request to the the execution Environment. 
	 * @param string : Execution request in String.
	 * 
	 */
	void submit(String string) throws MIFException;
	
	/**
	 * Responsible for submitting the Request to the the execution Environment.
	 *
	 * @param request the request
	 */
	void submit(ExecutionRequest request) throws MIFException;

	/**
	 * Responsible for handling cancellation of the Request to the the execution Environment.
	 *
	 * @deprecated use cancelRequest(String requestId) instead
	 *
	 * @param softwareName the software name
	 * @param requestUid the request uid.
	 * @return the string
	 * 
	 */
	@Deprecated
	void cancelRequest(UUID requestUid) throws MIFException;

    /**
     * Responsible for handling cancellation of the Request to the the execution Environment.
     *
     * @param softwareName the software name
     * @param requestUid the request uid.
     * @return the string
     * 
     */
    void cancelRequest(String requestUid) throws MIFException;
    
	/**
	 * Handles result message from the Result Listener.
	 *
	 * @param responseReceived the response received
	 * @return the string
	 */
	String handleResultMessage(String responseReceived) throws MIFException;
	
	/**
	 * Sets Execution message handler.
	 *
	 * @param executionMessageHandler the new execution message handler
	 */
	void setExecutionMessageHandler(TaskExecutionMessageHandler executionMessageHandler);

	void acknowledgeResponse(String requestUid) throws MIFException;

}