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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

/**
 * Consumer that always rejects messages
 */
public class RejectingMessageConsumer implements MessageListener {

    private final Map<String, AtomicInteger> rejectedMessagesCounter = Maps.newHashMap();

    private final static Logger LOG = Logger.getLogger(RejectingMessageConsumer.class);

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        String messageID = "";
        int counter = 0;
        try {
            messageID = textMessage.getText();
        } catch (JMSException e) {
            LOG.error(e);
        }
        if (rejectedMessagesCounter.containsKey(messageID)) {
            counter = rejectedMessagesCounter.get(messageID).incrementAndGet();
        } else {
            rejectedMessagesCounter.put(messageID, new AtomicInteger());
        }

        LOG.info(String.format("Received %s message %d times", messageID, counter));

        throw new RuntimeException("Message is being rejected");
    }

    /**
     * 
     * @param messageID
     * @return number of times that the message was redelivered
     */
    public int getCounter(String messageID) {
        if (rejectedMessagesCounter.containsKey(messageID)) {
            return rejectedMessagesCounter.get(messageID).get();
        } else {
            return 0;
        }
    }
}
