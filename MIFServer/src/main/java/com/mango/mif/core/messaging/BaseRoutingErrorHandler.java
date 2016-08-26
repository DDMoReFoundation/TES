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
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.google.common.base.Preconditions;

/**
 * 
 * Basic implementation of routing error handler
 */
public class BaseRoutingErrorHandler implements RoutingErrorHandler {

    /**
     * Logger 
     */
    protected static final Logger LOG = Logger.getLogger(CancellationRequestRoutingErrorHandler.class);
    /** 
     * The jms template. 
     */
    protected JmsTemplate jmsTemplate;
    /**
     * Response queue
     */
    protected String responseQueue;

    public BaseRoutingErrorHandler(String responseQueue, JmsTemplate jmsTemplate) {
        Preconditions.checkNotNull(jmsTemplate, "JMS template not set.");
        Preconditions.checkNotNull(responseQueue, "Response queue not set.");
        this.jmsTemplate = jmsTemplate;
        this.responseQueue = responseQueue;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setResponseQueue(String responseQueue) {
        this.responseQueue = responseQueue;
    }
    @Override
    public void destinationNotResolved(String messageText) {
        Preconditions.checkNotNull(jmsTemplate, "JMS template not set.");
        Preconditions.checkNotNull(responseQueue, "Response queue not set.");
        Preconditions.checkNotNull(messageText, "Message is null set.");
    }
    /**
     * sends a message to the given destination queue
     */
    protected void sendMessage(final String messageText) {
        jmsTemplate.send(responseQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage();
                message.setText(messageText);
                return message;
            }
        });
    }
}