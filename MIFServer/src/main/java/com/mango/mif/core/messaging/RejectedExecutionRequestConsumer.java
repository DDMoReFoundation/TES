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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;

import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.ExecutionResponseBuilder;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.TextMessageCreator;

/**
 * 
 * Consumer reading messages from MIF's Dead Letter Queue and ensuring that the jobs have been failed with an appropriate error message.
 */
public class RejectedExecutionRequestConsumer implements MessageListener {

    private static final Logger LOG = Logger.getLogger(RejectedExecutionRequestConsumer.class);
    public static final String EXECUTION_REQUEST_REJECTED = "REQUEST REJECTED";
    private JobManagementService jobManagementService;

    private JmsTemplate jmsTemplate;

    private ConnectorsRegistry connectorsRegistry;

    /**
     * consumes all messages except the ones for which JMSException was thrown. If the message contains ExecutionRequest
     * It creates an ExecutionResponse with status FAILED and response attribute set to REQUEST_REJECTED and submits
     * the response to connector's internal queue.
     */
    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String messageContent = textMessage.getText();
            ExecutionRequest executionRequest = MessagingHelper.parseExecutionRequest(messageContent);

            handleExecutionRequest(executionRequest);
        } catch (JMSException jmsException) {
            // if there was JMS exception, do not consume the message.
            throw new IllegalArgumentException(jmsException);
        } catch (Exception e) {
            //any other exception just log
            LOG.error(e);
        }
        LOG.error(String.format("The following message has been rejected by the system:\n %s", message));
    }

    private void handleExecutionRequest(ExecutionRequest executionRequest) throws JAXBException {
        final Job job = jobManagementService.getJob(executionRequest.getRequestId());
        if (job == null) {
            LOG.error(String.format("Job with ID %s does not exist", executionRequest.getRequestId()));
        } else {
            String executionResponseMsg = MessagingHelper
                    .marshallExecutionResponse(createExecutionResponse(executionRequest.getRequestId()));
            Connector connector = connectorsRegistry.getConnectorByExecutionType(executionRequest.getType());
            String connectorInternalQueue = connector.getInternalQueue();
            jmsTemplate.send(connectorInternalQueue, new TextMessageCreator(executionResponseMsg));
            LOG.info(String.format("Message was sent to %s connector for completion: %s", job.getConnectorId(), executionResponseMsg));
        }
    }

    @Required
    public void setJobManagementService(JobManagementService jobManagementService) {
        this.jobManagementService = jobManagementService;
    }

    public JobManagementService getJobManagementService() {
        return jobManagementService;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    ExecutionResponse createExecutionResponse(String requestId) throws JAXBException {
        return new ExecutionResponseBuilder().setRequestId(requestId).setStatus(JobStatus.FAILED.name())
                .setResult(EXECUTION_REQUEST_REJECTED).getExecutionResponse();
    }

    @Required
    public void setConnectorsRegistry(ConnectorsRegistry connectorsRegistry) {
        this.connectorsRegistry = connectorsRegistry;
    }

    public ConnectorsRegistry getConnectorsRegistry() {
        return connectorsRegistry;
    }
}
