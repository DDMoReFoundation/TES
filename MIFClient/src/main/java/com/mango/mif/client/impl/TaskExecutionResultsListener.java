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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.core.task.TaskExecutor;

import com.mango.mif.client.api.TaskExecutionManager;
import com.mango.mif.client.err.TaskExecutorExceptions;
import com.mango.mif.exception.MIFException;

/**
 * Class registered as JMS Listener to handle the response from MIF. 
 */
public class TaskExecutionResultsListener implements MessageListener{
	private static final Logger logger = Logger.getLogger(TaskExecutionResultsListener.class);
	
	/** Task Executor instance that is wired through Spring*/
	private TaskExecutor executor;
	
	/** Task Manager Note this is the same instance that is wired through Spring*/
	private TaskExecutionManager taskExecutionManager;
	
	/** To enable concurrency should be set as true from spring*/
	private boolean concurrent;

	/**
	 * Consumes the message of the queue and delegates the message to TaskExecutionManager.
	 *  
	 * @param message the message
	 */
	@Override
	public void onMessage(Message message) {
		TextMessage msg = (TextMessage) message;
		String receivedText = null;
		try {
			receivedText = msg.getText();
			logger.info(String.format("*** Execution Metrics : {ResponseHandling.1} Received and delegating the message [%s] to TaskExecution Manager", 
	        		receivedText));
			handleMessage(receivedText);
		} catch (MIFException e) {
			throw new RuntimeException(TaskExecutorExceptions.INVALID_RESPONSE_MESSAGE.getMessage(),e);
		} catch (JMSException e) {
			logger.error(e);
			throw new RuntimeException(TaskExecutorExceptions.INVALID_RESPONSE_MESSAGE.getMessage(),e);
		}
	}
	
	/**
	 * Takes a message and if executor is defined it handles the message
	 * concurrently otherwise uses the default handling
	 */
	private void handleMessage(final String message) throws MIFException{
		if(concurrent && executor != null) {
			handleMessageConcurrently(message);
		}else {
			taskExecutionManager.handleResultMessage(message);
		}
	}
	
	/**
	 * Takes a message and creates a Runnable that delegates the message
	 * to a taskExecutionManager
	 * Effectively handles the messages concurrently
	 */
	private void handleMessageConcurrently(final String message) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					taskExecutionManager.handleResultMessage(message);
				} catch (Throwable t) {
					logger.error("Unable to handle message : \n"+message, t);
				}
			}
		});
	}
	
	public boolean isConcurrent() {
		return concurrent;
	}

	public void setConcurrent(boolean concurrent) {
		this.concurrent = concurrent;
	}
	
	public TaskExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(TaskExecutor executor) {
		this.executor = executor;
	}

	/**
	 * Gets the task execution manager.
	 *
	 * @return the task execution manager
	 */
	public TaskExecutionManager getTaskExecutionManager() {
		return taskExecutionManager;
	}

	/**
	 * Sets the task execution manager.
	 *
	 * @param taskExecutionManager the new task execution manager
	 */
	public void setTaskExecutionManager(TaskExecutionManager taskExecutionManager) {
		this.taskExecutionManager = taskExecutionManager;
	}
}
