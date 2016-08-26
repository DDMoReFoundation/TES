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
package com.mango.mif.client.domain;

/**
 * The Enum QueueNameLocators.
 * This holds the key that we use to get the actual value of the name of the QUEUE 
 * configured in the property file.
 *
 */
public enum QueueNameLocators {
	
	CANCEL_QUEUE("JOB.CANCEL"),
	REQUEST_QUEUE("JOB.REQUEST"),
	RESPONSE_QUEUE("JOB.RESPONSE"), 
	ACK_QUEUE("JOB.RESPONSE.ACK");
	
	private String queueName;
	
	private QueueNameLocators(String queueName) {
		this.queueName = queueName;
	}
	public String getQueueName() {
		return queueName;
	}

}
