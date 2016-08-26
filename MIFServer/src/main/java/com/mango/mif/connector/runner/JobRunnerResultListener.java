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
package com.mango.mif.connector.runner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.mango.mif.connector.ConnectorExceptions;
import com.mango.mif.connector.impl.ConnectorWithJobRunners;

/**
 * This class is responsible for delegating handling of the result messages from Job Runner to connector 
 */
public class JobRunnerResultListener implements MessageListener {
	/**
	 * Logger
	 */
	final static Logger LOG = Logger.getLogger(JobRunnerResultListener.class);
	/**
	 * Connector to which handling of the result will be delegated
	 */
	private ConnectorWithJobRunners connector;
	
	@Override
	public void onMessage(Message message) {
		TextMessage msg = (TextMessage) message;
		String receivedText = null;
		try {
			receivedText = msg.getText();
		} catch (JMSException e) {
			throw new RuntimeException(ConnectorExceptions.INVALID_RESPONSE_MESSAGE.getMessage());
		}
		LOG.debug("Received the message" + receivedText);
		LOG.info(String.format("*** Execution Metrics : {ResultHandling.1} Received and delegating the message [%s] to Connector", 
        		receivedText));
		connector.handleResultMessage(receivedText);
	}

	public ConnectorWithJobRunners getConnector() {
		return connector;
	}

	public void setConnector(ConnectorWithJobRunners connector) {
		this.connector = connector;
	}

}

