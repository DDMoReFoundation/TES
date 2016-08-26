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

import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import static org.mockito.Mockito.*;

import com.mango.mif.core.messaging.MessageRouter;
import com.mango.mif.core.messaging.QueueResolver;
import com.mango.mif.core.messaging.RoutingErrorHandler;

/**
 * Tests the message router
 */
public class MessageRouterTest {
	/**
	 * request router
	 */
	private MessageRouter messageRouter;
	/**
	 * jms template
	 */
	@Mock JmsTemplate jmsTemplate;
	/**
	 * queue resolver
	 */
	@Mock QueueResolver queueResolver;
	/**
	 * JMS text message
	 */
	@Mock TextMessage textMessage;
    /**
     * Routing error handler
     */
    @Mock RoutingErrorHandler errorHandler;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		messageRouter = new MessageRouter();
		messageRouter.setJmsTemplate(jmsTemplate);
		messageRouter.setQueueResolver(queueResolver);
        messageRouter.setRoutingErrorHandler(errorHandler);
		when(textMessage.getText()).thenReturn("MOCK-MESSAGE");
	}

	@Test
	public void shouldSendAMessageToADestinationQueueResolvedByQueueResolver() {
		when(queueResolver.resolve("MOCK-MESSAGE")).thenReturn("MOCK-QUEUE");
		messageRouter.onMessage(textMessage);
		verify(jmsTemplate).send(eq("MOCK-QUEUE"), (MessageCreator) any());
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfDestinationQueueIsNotResolvedToNull() {
        messageRouter.publishToConnectorQueue(null,"MOCK-MESSAGE");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionIfDestinationQueueIsResolvedToEmptyQueueName() {
		messageRouter.publishToConnectorQueue("","MOCK-MESSAGE");
	}

    @Test
    public void shouldInvokeErrorHandlerIfDestinationEmptyOrNull() {
        when(queueResolver.resolve("MOCK-MESSAGE")).thenReturn("");
        messageRouter.onMessage(textMessage);
        verify(errorHandler).destinationNotResolved("MOCK-MESSAGE");
    }
	
	
    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfRoutingErrorHandlerIsNotSet() {
        messageRouter.setRoutingErrorHandler(null);
        when(queueResolver.resolve("MOCK-MESSAGE")).thenReturn("");
        messageRouter.onMessage(textMessage);
        verify(jmsTemplate).send(eq("MOCK-QUEUE"), (MessageCreator) any());
    }
}
