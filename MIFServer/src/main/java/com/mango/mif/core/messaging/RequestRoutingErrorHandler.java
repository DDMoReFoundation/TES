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

import org.springframework.jms.core.JmsTemplate;

import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponseBuilder;

/**
 * Implements error handling for Execution requests router.
 * 
 */
public class RequestRoutingErrorHandler extends BaseRoutingErrorHandler {
    /**
     * @param responseQueue
     * @param jmsTemplate
     */
    public RequestRoutingErrorHandler(String responseQueue, JmsTemplate jmsTemplate) {
        super(responseQueue, jmsTemplate);
    }

    @Override
    public void destinationNotResolved(String messageText) {
        super.destinationNotResolved(messageText);
        ExecutionRequest executionRequest;
        executionRequest = MessagingHelper.parseExecutionRequest(messageText);
        ExecutionResponseBuilder executionResponseBuilder = new ExecutionResponseBuilder()
        .setStatus(DriverProcessingStatus.FAILED.getMessage()).setRequestId(executionRequest.getRequestId()).setResult("Execution type " + executionRequest.getType() + " not found");
        
        try {
            String executionResponse = executionResponseBuilder.getExecutionResponseMsg();
            sendMessage(executionResponse);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create a failure response message for request " + messageText,e);
        }
        
    }
}
