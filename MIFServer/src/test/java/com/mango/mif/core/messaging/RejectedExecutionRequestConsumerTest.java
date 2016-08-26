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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.messaging.RejectedExecutionRequestConsumer;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.TextMessageCreator;

/**
 * Tests the RejectedExectuionRequestConsumer
 */
@RunWith(MockitoJUnitRunner.class)
public class RejectedExecutionRequestConsumerTest {

    private RejectedExecutionRequestConsumer requestConsumer;
    
    @Mock JobManagementService jobManagementService;

    @Mock ConnectorsRegistry connectorsRegistry;

    @Mock Connector connector;
    
    @Mock JmsTemplate jmsTemplate;
    
    private final static String RESULT_QUEUE = "RESULT_QUEUE";
    private final static String REQUEST_ID = "REQUEST_ID";
    private final static String CONNECTOR_ID = "CONNECTOR_ID";
    
    
    @Before
    public void setUp() throws Exception {
        requestConsumer = new RejectedExecutionRequestConsumer();
        requestConsumer.setJmsTemplate(jmsTemplate);
        requestConsumer.setJobManagementService(jobManagementService);
        requestConsumer.setConnectorsRegistry(connectorsRegistry);
        
    }

    @Test
    public void onMessage_shouldSendExecutionResponseToConnectorInternalQueue() throws JAXBException, JMSException {
    	final String EXECUTION_TYPE = "MOCK_EXECUTION_TYPE";
        String executionRequestMessage = new ExecutionRequestBuilder().setRequestId(REQUEST_ID).setExecutionType(EXECUTION_TYPE)
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE").getExecutionRequestMsg();
        TextMessage message = mock(TextMessage.class);
        Job job = mock(Job.class);
        when(connector.getInternalQueue()).thenReturn(RESULT_QUEUE);
        when(connectorsRegistry.getConnectorByExecutionType(EXECUTION_TYPE)).thenReturn(connector);
        when(message.getText()).thenReturn(executionRequestMessage);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        
        // Call the method under test
        requestConsumer.onMessage(message);
        
        verify(job).getConnectorId();
        verify(connector).getInternalQueue();
        verify(connectorsRegistry).getConnectorByExecutionType("MOCK_EXECUTION_TYPE");
        verify(message).getText();
        verify(jobManagementService).getJob(REQUEST_ID);

        verify(jmsTemplate).send(eq(RESULT_QUEUE),(TextMessageCreator)any());
    }

    @Test
    public void createExecutionResponse_shouldCreateFailedExecutionResponse() throws JAXBException {
        ExecutionResponse response = requestConsumer.createExecutionResponse(REQUEST_ID);
        
        assertEquals("FAILED", response.getStatus());
        assertEquals(REQUEST_ID, response.getRequestId());
        assertEquals("REQUEST REJECTED", response.getResult());
    }

    @Test
    public void onMessage_shouldConsumeAnExecutionRequestForNotExistingJobAndNotSubmitResponse() throws JMSException, JAXBException {
        String executionRequestMessage = new ExecutionRequestBuilder().setRequestId(REQUEST_ID).setExecutionType("MOCK_EXECUTION_TYPE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE").getExecutionRequestMsg();
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(executionRequestMessage);
        requestConsumer.onMessage(message);

        verify(jmsTemplate,never()).send(eq(RESULT_QUEUE),(TextMessageCreator)any());
    }
    
    @Test
    public void onMessage_shouldConsumeAnyReceivedMessageNotBeingExecutionRequest() throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn("<custom-xml></custom-xml>");
        
        requestConsumer.onMessage(message);
        
        //if error is not thrown that means that message was consumed
    }

    @Test(expected=IllegalArgumentException.class)
    public void onMessage_shouldRejectMesssageIfJMSExceptionIsThrown() throws JMSException {
        TextMessage message = mock(TextMessage.class);
        doThrow(JMSException.class).when(message).getText();
        
        requestConsumer.onMessage(message);
        
    }
    
    @Test
    public void onMessage_shouldConsumeMessageDespiteAnyErrors() throws JMSException, JAXBException {
        TextMessage message = mock(TextMessage.class);
        String executionRequestMessage = new ExecutionRequestBuilder().setRequestId(REQUEST_ID).setExecutionType("MOCK_EXECUTION_TYPE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE").getExecutionRequestMsg();
        when(message.getText()).thenReturn(executionRequestMessage);
        doThrow(new RuntimeException()).when(jobManagementService).saveJobStatus(REQUEST_ID, JobStatus.FAILED.name());
        requestConsumer.onMessage(message);
        
        //if error is not thrown that means that message was consumed
    }
    
}
