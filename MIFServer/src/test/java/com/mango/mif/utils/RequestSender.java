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
package com.mango.mif.utils;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Replicating navigator component Request. 
 */
public class RequestSender implements Runnable {
	final static Logger LOG = Logger.getLogger(RequestSender.class);
	
	JmsTemplate jmsTemplate;
	
	/**JMS Destination for the Execution Request Queue	 */
	private final String destination;

	private final String buildMessage;
	

	public RequestSender(String destination, String buildMessage, JmsTemplate jmsTemplate2) {
		this.jmsTemplate = jmsTemplate2;
		this.buildMessage = buildMessage;
		this.destination = destination;
	}

	/* Submits the request to MIF execution environment.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		LOG.info("Navigator MOCK Client : Sending messages ");
		jmsTemplate.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				message.setText(buildMessage);
				return message;
			}
		});
	}

}
