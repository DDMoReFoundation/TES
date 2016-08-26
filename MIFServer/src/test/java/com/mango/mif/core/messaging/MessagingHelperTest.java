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

import static org.junit.Assert.*;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.powermock.api.mockito.PowerMockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.messaging.MessagingHelper;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.ExecutionResponseBuilder;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;


/**
 * Tests messaging helper.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JAXBUtils.class)
public class MessagingHelperTest {

    private ExecutionRequestBuilder createExecutionRequestBuilder() {
        return new ExecutionRequestBuilder().setRequestId("REQUEST_ID").setExecutionType("EXECUTION_TYPE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE");
    }
    
    @Test
    public void parseExecutionRequest_should_parse_execution_request() throws JAXBException {
        String message = createExecutionRequestBuilder().getExecutionRequestMsg();
        assertNotNull(MessagingHelper.parseExecutionRequest(message));
    }

    @Test(expected=IllegalArgumentException.class)
    public void parseExecutionRequest_should_throw_exception_if_request_id_not_set_on_request() throws JAXBException {
        String message = createExecutionRequestBuilder().getExecutionRequestMsg();
        message = message.replace("REQUEST_ID", "");
        MessagingHelper.parseExecutionRequest(message);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void parseExecutionRequest_should_throw_exception_if_fails_to_parse_request() throws JAXBException {
        String message = createExecutionRequestBuilder().getExecutionRequestMsg();
        mockStatic(JAXBUtils.class);
        when(JAXBUtils.unmarshall(message,ExecutionJaxbUtils.CONTEXT_CLASSES)).thenThrow(mock(JAXBException.class));
        MessagingHelper.parseExecutionRequest(message);
    }
    

    @Test
    public void parseExecutionResponse_should_parse_execution_response() throws JAXBException {
        String message = new ExecutionResponseBuilder().setRequestId("REQUEST_ID").setStatus("STATUS").getExecutionResponseMsg();
        assertNotNull(MessagingHelper.parseExecutionResponse(message));
    }

    @Test(expected=IllegalArgumentException.class)
    public void parseExecutionResponse_should_throw_exception_if_request_id_not_set_on_response() throws JAXBException {
        String message = new ExecutionResponseBuilder().setRequestId("REQUEST_ID").setStatus("STATUS").getExecutionResponseMsg();
        message = message.replace("REQUEST_ID", "");
        MessagingHelper.parseExecutionResponse(message);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void parseExecutionResponse_should_throw_exception_if_fails_to_parse_response() throws JAXBException {
        String message = new ExecutionResponseBuilder().setRequestId("REQUEST_ID").setStatus("STATUS").getExecutionResponseMsg();
        mockStatic(JAXBUtils.class);
        when(JAXBUtils.unmarshall(message,ExecutionJaxbUtils.CONTEXT_CLASSES)).thenThrow(mock(JAXBException.class));
        MessagingHelper.parseExecutionResponse(message);
    }
    
    
    @Test
    public void marshallExecutionResponse_should_marshal_execution_response() throws JAXBException {
        ExecutionResponse executionResponse = new ExecutionResponseBuilder().setRequestId("REQUEST_ID").setStatus("STATUS").getExecutionResponse();
        assertNotNull(MessagingHelper.marshallExecutionResponse(executionResponse));
    }

    @Test(expected=IllegalArgumentException.class)
    public void marshallExecutionResponse_should_throw_exception_if_fails_to_marshal() throws JAXBException {
        ExecutionResponse executionResponse = new ExecutionResponseBuilder().setRequestId("REQUEST_ID").setStatus("STATUS").getExecutionResponse();
        mockStatic(JAXBUtils.class);
        when(JAXBUtils.marshall(executionResponse,ExecutionJaxbUtils.CONTEXT_CLASSES)).thenThrow(mock(JAXBException.class));
        MessagingHelper.marshallExecutionResponse(executionResponse);
    }
    

    @Test
    public void marshallExecutionRequest_should_marshal_execution_request() {
        ExecutionRequest executionRequest = createExecutionRequestBuilder().getExecutionRequest();
        assertNotNull(MessagingHelper.marshallExecutionRequest(executionRequest));
    }
    

    @Test(expected=IllegalArgumentException.class)
    public void marshallExecutionRequest_should_throw_exception_if_fails_to_marshal_request() throws JAXBException {
        ExecutionRequest executionRequest = createExecutionRequestBuilder().getExecutionRequest();
        mockStatic(JAXBUtils.class);
        when(JAXBUtils.marshall(executionRequest,ExecutionJaxbUtils.CONTEXT_CLASSES)).thenThrow(mock(JAXBException.class));
        MessagingHelper.marshallExecutionRequest(executionRequest);
    }
}
