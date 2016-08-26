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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.messaging.RequestRoutingErrorHandler;
import com.mango.mif.domain.ExecutionRequestBuilder;


public class RequestRoutingErrorHandlerTest {
    /**
     * jms template
     */
    @Mock JmsTemplate jmsTemplate;
    /**
     * mock execution request
     */
    String executionRequest;
    /**
     * Routing error handler
     */
    RequestRoutingErrorHandler errorHandler;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder()
            .setRequestId(UUID.randomUUID().toString()).setExecutionType("MOCK-CONNECTOR")
            .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
            .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE");
        executionRequest = requestBuilder.getExecutionRequestMsg();
        
        errorHandler = new RequestRoutingErrorHandler("MOCK-RESPONSE-QUEUE",jmsTemplate);
        
    }

    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfMessageTextIsNull() {
        errorHandler.destinationNotResolved(null);
    }

    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfResponseQueueNotSet() {
        errorHandler.setResponseQueue(null);
        errorHandler.destinationNotResolved(executionRequest);
    }


    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfJMSTemplateNotSet() {
        errorHandler.setJmsTemplate(null);
        errorHandler.destinationNotResolved(executionRequest);
    }

    @Test
    public void shouldSendAResponseMessage() {
        errorHandler.destinationNotResolved(executionRequest);
        verify(jmsTemplate).send(eq("MOCK-RESPONSE-QUEUE"), (MessageCreator) any());
    }
}
