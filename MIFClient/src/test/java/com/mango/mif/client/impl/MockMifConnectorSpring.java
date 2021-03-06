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
package com.mango.mif.client.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.client.err.TaskExecutorExceptions;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * Responsible for handling the spring 
 */
public class MockMifConnectorSpring implements MessageListener{

	static final Logger LOG = Logger.getLogger(MockMifConnectorSpring.class);
	
	/**Default Destination for the response. Each sub class overrides it for its specific destination */
	protected String destination = "JOB.RESPONSE";
	
    /**
     * Test resource that is uploaded to resource component
     */
    private static String TEST_RESOURCE = "test.R";
	
	/** The jms template used for sending messages. */
	JmsTemplate jmsTemplate;
	
	/**
	 * Consumes the request posted by Navigator , Creates a Job request, 
	 * it then creates a Job Runner to handle the request, It also adds the Job Runner 
	 * to the  Registry  and submits the Job to the Task Executor for execution.
	 * 
	 */
	@Override
	public synchronized void onMessage(Message message) {
		if (message!= null) {
			TextMessage msg = (TextMessage) message;
			String receivedText = null;
			try {
				receivedText = msg.getText();
			} catch (JMSException e) {
				LOG.error(e);
				throw new RuntimeException(TaskExecutorExceptions.INVALID_REQUEST_MESSAGE.getMessage());
			}
			LOG.info("Received the message from navigator" + receivedText);
			
			String uuid = retriveExternalJobIdFromRequest(receivedText);
			ExecutionRequest reqUnmarshalled;
			try {
				reqUnmarshalled = (ExecutionRequest)JAXBUtils.unmarshall(receivedText, ExecutionJaxbUtils.CONTEXT_CLASSES);
			} catch (JAXBException e1) {
				LOG.error("JAXB Exception for the string " , e1);
				throw new RuntimeException(TaskExecutorExceptions.INVALID_REQUEST_MESSAGE_FORMAT.getMessage());
			}
			final String requestId = reqUnmarshalled.getRequestId();
            final Map<String, String> requestAttributes = reqUnmarshalled.getRequestAttributes();

			new Thread( new Runnable() {

				@Override
				public void run() {
					
					LOG.info("Processing request for " + requestId);

					File output = getRequestDirectory(requestAttributes,requestId.toString());
					try {
						FileUtils.copyFile(FileUtils.toFile(this.getClass().getResource(TEST_RESOURCE)), new File(output,TEST_RESOURCE));
					} catch (IOException e1) {
						LOG.error(e1);
						throw new RuntimeException("Could not copy test result file to shared location", e1);
					}
					
					
					ExecutionResponse executionResponse = new ExecutionResponse();
				    executionResponse.setRequestId(requestId);
				    String marshallRequest = null;    
				    try {
						marshallRequest = JAXBUtils.marshall(executionResponse, ExecutionJaxbUtils.CONTEXT_CLASSES);
					} catch (JAXBException e) {
						LOG.error("JAXBException", e);
					}
					//invoke the handle Result Message
					handleResultMessage(marshallRequest);
				}

                private File getRequestDirectory(Map<String, String> requestAttributes, String requestId) {
                    return new File(requestAttributes.get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()), requestId);
                }
			}).start();
			
			LOG.info("Submitted the job with id " + uuid +" to the execution service");
		} else {
			throw new RuntimeException(TaskExecutorExceptions.INVALID_REQUEST_MESSAGE.getMessage());
		}
	}

	/**
	 * This message is invoked by the JobRunnerResultListener which consumes the
	 * result message from the Job Runner and delegates the message to the Dispatcher.  
	 * @param jobRunnerResponse
	 * @return
	 */
	public synchronized String handleResultMessage(final String jobRunnerResponse) {
		String uuid = null;
		if (jobRunnerResponse == null) {
			throw new RuntimeException(TaskExecutorExceptions.INVALID_RESPONSE_MESSAGE.getMessage());
		}
		uuid = getJobIdFromResponse(jobRunnerResponse);
		if (uuid == null) {
			throw new RuntimeException(TaskExecutorExceptions.REQUEST_ID_NOT_PRESENT.getMessage());
		}
		LOG.info("Sending the following response to clients " + jobRunnerResponse);
		
		jmsTemplate.send(getDestination(), new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					TextMessage message = session.createTextMessage();
					message.setText(jobRunnerResponse);
					return message;
				}
		});
		return uuid;
	}

	/**
	 * Retrieves the Job ID from the Response of Job Runner.
	 * @param jobRunnerResponse
	 * @return
	 */
	protected String getJobIdFromResponse(final String jobRunnerResponse) {
		ExecutionResponse request = null;
		try {
			request = JAXBUtils.unmarshall(ExecutionResponse.class , jobRunnerResponse);
		} catch (JAXBException e) {
			LOG.error("JAXB Exception for the string " + jobRunnerResponse);
			throw new RuntimeException(TaskExecutorExceptions.INVALID_RESPONSE_MESSAGE_FORMAT.getMessage());
		}
		return (request !=null) ? request.getRequestId().toString(): null;
	}


	/**
	 * Retrieves the Job ID from the Request.
	 * @param receivedText
	 * @return
	 */
	protected String retriveExternalJobIdFromRequest(String receivedText) {
		ExecutionRequest request = null;
		try {
			request = JAXBUtils.unmarshall(receivedText, ExecutionJaxbUtils.CONTEXT_CLASSES);
		} catch (JAXBException e) {
			LOG.error("JAXB Exception for the string " , e);
			throw new RuntimeException(TaskExecutorExceptions.INVALID_REQUEST_MESSAGE_FORMAT.getMessage());
		}
		if(request.getRequestId() == null) {
			LOG.error("request id is not present in the request" + receivedText);
			throw new RuntimeException(TaskExecutorExceptions.REQUEST_ID_NOT_PRESENT.getMessage());
		}
		return request.getRequestId().toString();
	}


    public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

}
