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
package com.mango.mif.connector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.mango.mif.connector.impl.ConnectorWithJobRunners;


/**
 * This component is responsible for delegating handling of a cancellation request to the connector
 */
public class CancellationRequestListener implements MessageListener {
	/**
	 * Logger
	 */
	final static Logger LOG = Logger.getLogger(CancellationRequestListener.class);
	/**
	 * Connector to which the handling of cancellatin request will be delegated
	 */
	private ConnectorWithJobRunners connector;
	
	@Override
	public void onMessage(Message message) {
		LOG.info(message);
		TextMessage msg = (TextMessage) message;
		String receivedText = null;
		try {
			receivedText = msg.getText();
		} catch (JMSException e) {
			LOG.error(e);
			throw new RuntimeException(ConnectorExceptions.INVALID_REQUEST_MESSAGE.getMessage());
		}
		LOG.debug("Cancellation for " + connector.getConnectorId() + " received the message " + receivedText );
		connector.handleCancelMessage(receivedText);
	}

	public ConnectorWithJobRunners getConnector() {
		return connector;
	}

	public void setConnector(ConnectorWithJobRunners connector) {
		this.connector = connector;
	}

}

