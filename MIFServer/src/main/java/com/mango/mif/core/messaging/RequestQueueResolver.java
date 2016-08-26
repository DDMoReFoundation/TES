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

import com.mango.mif.connector.Connector;
import com.mango.mif.domain.ExecutionRequest;

/**
 * 
 * A queue resolver that parses the message as an execution request and based in it's properties
 * resolves destination queue
 * 
 */
public class RequestQueueResolver extends BaseQueueResolver {

    @Override
    public String resolve(String message) {
        ExecutionRequest executionRequest = null;
        executionRequest = MessagingHelper.parseExecutionRequest(message);
        Connector connector = connectorsRegistry.getConnectorByExecutionType(executionRequest.getType());
        if (connector == null) {
            return "";
        }
        return connector.getRequestQueue();
    }
}
