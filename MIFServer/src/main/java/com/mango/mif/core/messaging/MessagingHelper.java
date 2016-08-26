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

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;

import com.mango.mif.connector.ConnectorExceptions;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * Provides helper methods used to marshall/unmarshall messages
 */
public final class MessagingHelper {
	
	private MessagingHelper() {
		
	}
    /**
     * Parses execution request.
     *
     * @param requestMsg
     * @return the request
     * @throws IllegalArgumentException in case of any error with handling request message string
     */
    public static ExecutionRequest parseExecutionRequest(String requestMsg) {
        ExecutionRequest request = null;
        try {
            request = JAXBUtils.unmarshall(requestMsg, ExecutionJaxbUtils.CONTEXT_CLASSES);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(ConnectorExceptions.INVALID_REQUEST_MESSAGE_FORMAT.getMessage());
        }
        if(StringUtils.isEmpty(request.getRequestId())) {
            throw new IllegalArgumentException(ConnectorExceptions.REQUEST_ID_NOT_PRESENT.getMessage());
        }
        return request;
    }
    
    /**
     * Parses execution response.
     *
     * @param responseMsg the response message
     * @return the response
     * @throws IllegalArgumentException in case of any error with handling response message string
     */
    public static ExecutionResponse parseExecutionResponse(String responseMsg) {
        ExecutionResponse response = null;
        try {
            response = JAXBUtils.unmarshall(responseMsg, ExecutionJaxbUtils.CONTEXT_CLASSES);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(ConnectorExceptions.INVALID_RESPONSE_MESSAGE_FORMAT.getMessage());
        }
        if(StringUtils.isEmpty(response.getRequestId())) {
            throw new IllegalArgumentException(ConnectorExceptions.REQUEST_ID_NOT_PRESENT.getMessage());
        }
        return response;
    }
    
    /**
     * Marshalls execution response
     * @param executionResponse
     * @return string representation of the execution response
     * @throws IllegalArgumentException in case of any error with handling response 
     */
    public static String marshallExecutionResponse(ExecutionResponse executionResponse) {
        String executionResponseMsg = null;
        try {
            executionResponseMsg = JAXBUtils.marshall(executionResponse, ExecutionJaxbUtils.CONTEXT_CLASSES);
        } catch (JAXBException e) {           
            throw new IllegalArgumentException("Could not marshall execution response.", e);
        }
        return executionResponseMsg;
    }
    
    /**
     * Marshalls execution request
     * @param executionRequest
     * @return string representation of the execution request
     * @throws IllegalArgumentException in case of any error with handling execution request 
     */
    public static String marshallExecutionRequest(ExecutionRequest executionRequest) {
        String executionRequestMsg = null;
        try {
            executionRequestMsg = JAXBUtils.marshall(executionRequest, ExecutionJaxbUtils.CONTEXT_CLASSES);
        } catch (JAXBException e) {           
            throw new IllegalArgumentException("Could not marshall execution request.", e);
        }
        return executionRequestMsg;
    }
}
