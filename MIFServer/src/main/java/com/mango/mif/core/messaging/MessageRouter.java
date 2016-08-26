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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;

import com.google.common.base.Preconditions;
import com.mango.mif.connector.ConnectorExceptions;
import com.mango.mif.utils.TextMessageCreator;

/**
 *
 * Component responsible for routing requests from a client to an appropriate queue
 *
 */
public class MessageRouter implements MessageListener {
    /**
     * Logger
     */
    private final static Logger LOG = Logger.getLogger(MessageRouter.class);
    /**
     * Resolver
     */
    private QueueResolver queueResolver;
    /**
     * The jms template.
     */
    private JmsTemplate jmsTemplate;
    /**
     * Handles routing errors
     */
    private RoutingErrorHandler routingErrorHandler;

    @Override
    public void onMessage(Message message) {
        Preconditions.checkNotNull(message, "Received null message");
        Preconditions.checkNotNull(queueResolver,"No routing resolver set");
        Preconditions.checkNotNull(jmsTemplate,"Template is not set");
        Preconditions.checkNotNull(routingErrorHandler,"Routing error handler is not set");
        TextMessage msg = (TextMessage) message;
        String receivedText = null;
        try {
            receivedText = msg.getText();
        } catch (JMSException e) {
            LOG.error(e);
            throw new RuntimeException(ConnectorExceptions.INVALID_REQUEST_MESSAGE.getMessage());
        }
        Preconditions.checkArgument(StringUtils.isNotEmpty(receivedText), "Received empty request message");
        LOG.debug("Received the message" + receivedText);
        String destination = queueResolver.resolve(receivedText);

        if(StringUtils.isEmpty(destination)) {
            LOG.error("Destination queue could not be resolved for message " + receivedText);
            routingErrorHandler.destinationNotResolved(receivedText);
            return; // consume the message
        }
        publishToConnectorQueue(destination,receivedText);
    }
    /**
     * Publishes a message to the appropriate connector cancellation queue
     * @param message
     */
    void publishToConnectorQueue(String destination, String message) {
        Preconditions.checkNotNull(destination,"Destination queue can't be null");
        Preconditions.checkArgument(!StringUtils.isEmpty(destination), "Destination queue could not be resolved");
        Preconditions.checkNotNull(message,"Message can't be null");
        Preconditions.checkNotNull(jmsTemplate,"Template is null");
        jmsTemplate.send(destination, new TextMessageCreator(message));
    }


    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setQueueResolver(QueueResolver queueResolver) {
        this.queueResolver = queueResolver;
    }

    public void setRoutingErrorHandler(RoutingErrorHandler routingErrorHandler) {
        this.routingErrorHandler = routingErrorHandler;
    }
}
