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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.client.api.TaskExecutionManager;
import com.mango.mif.exception.MIFException;

/**
 * Test for {@link TaskExecutionResultsListener}.
 */
public class TaskExecutionResultsListenerTest {
	static final Logger LOG = Logger.getLogger(TaskExecutionResultsListenerTest.class);
	
	TaskExecutionResultsListener  taskExecutionResultsListener = null;
	TaskExecutionManager taskExecutionManager = null;
	TextMessage message = null;
	String responseReceived =  "Some sample text";
	 
	@Before
	public void before() throws JMSException {
		taskExecutionManager = mock(TaskExecutionManager.class);
		message = mock(TextMessage.class);
		when(message.getText()).thenReturn(responseReceived);
		
		taskExecutionResultsListener = new TaskExecutionResultsListener();
		taskExecutionResultsListener.setTaskExecutionManager(taskExecutionManager);
		
	}
	
	@Test
	public void shouldDelegateTheRequestToTheTaskExecutionManger () throws MIFException {
		taskExecutionResultsListener.onMessage(message);
		verify(taskExecutionManager).handleResultMessage(responseReceived);
		
	}
	
}
