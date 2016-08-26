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
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.ExecutionRequestBuilder;


/**
 * Tests Execution Request rejection handling
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations = {
        "classpath:/com/mango/mif/core/messaging/RejectedExecutionRequestIT-context.xml",
        "classpath:/spring/JmsTest-Config.xml"})
public class RejectedExecutionRequestIT implements MessageListener {
    private static final Logger LOG = Logger.getLogger(RejectedExecutionRequestIT.class);

    private static final String REQUEST_ID = "REQUEST_ID";
    
    @Autowired
    private JmsTemplate jmsTemplate;

    protected static CountDownLatch processingSignal = new CountDownLatch(1);
    
    protected static AtomicBoolean successFlag = new AtomicBoolean(false);
    
    private static final String CONNECTOR_ID = "CONNECTOR_ID";

    private static final String OUTPUT_QUEUE = "OUTPUT.QUEUE";
    private static final String INPUT_QUEUE = "INPUT.QUEUE";
    
    
    @Test
    public void shouldReceiveMessageAfterRejection() throws InterruptedException, JAXBException {
        final String executionRequestMessage = new ExecutionRequestBuilder().setRequestId(REQUEST_ID).setExecutionType(CONNECTOR_ID)
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_FILESHARE")
                .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_FILESHARE").getExecutionRequestMsg();
        
        
        jmsTemplate.send(INPUT_QUEUE, new MessageCreator() {
            
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage();
                
                message.setText(executionRequestMessage);
                LOG.debug(String.format("Sending %s ", executionRequestMessage));
                return message;
            }
        });
        

        jmsTemplate.send(INPUT_QUEUE, new MessageCreator() {
            
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage();
                
                message.setText("<root><element>asdf</element></root>");
                LOG.debug(String.format("Sending %s ", executionRequestMessage));
                return message;
            }
        });
        
        processingSignal.await();
        
        if(!successFlag.get()) {
            fail("Execution Request rejection message flow failed. See log messages for details.");
        }
    }

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage)message;
        String text = "";
        
        try {
            text = textMessage.getText();
            LOG.info(String.format("Received message %s",text ));
            if(!(text.contains("REQUEST REJECTED") && text.contains("FAILED"))) {
                successFlag.set(false);
            } else {
                successFlag.set(true);
            }
        } catch (JMSException e) {
            LOG.error(e);
            successFlag.set(false);
        } finally {
            processingSignal.countDown();
        }
    }
    
    
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
    
    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }
    
    public static JobManagementService mockJobManagementService() {
        JobManagementService jobManagementService = mock(JobManagementService.class);
        Job job = mock(Job.class);
        when(job.getConnectorId()).thenReturn(CONNECTOR_ID);
        when(jobManagementService.getJob(REQUEST_ID)).thenReturn(job);
        return jobManagementService;
    }

    public static ConnectorsRegistry mockConnectorsRegistry() {
        ConnectorsRegistry connectorsRegistry = mock(ConnectorsRegistry.class);
        Connector connector = mock(Connector.class);
        when(connector.getInternalQueue()).thenReturn(OUTPUT_QUEUE);
        when(connectorsRegistry.getConnectorById(CONNECTOR_ID)).thenReturn(connector);
        return connectorsRegistry;
    }
}
