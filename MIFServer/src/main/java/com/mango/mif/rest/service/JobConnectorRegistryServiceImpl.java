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
package com.mango.mif.rest.service;

import java.util.Collection;

import org.codehaus.jackson.map.ObjectMapper;

import com.mango.mif.client.api.rest.JobConnectorRegistryService;
import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.Connector;
import com.mango.mif.connector.impl.CommandExecutionConnector;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.domain.ClientAvailableConnectorDetails;


public class JobConnectorRegistryServiceImpl implements JobConnectorRegistryService {

    private ConnectorsRegistry connectorsRegistry;

    @Override
    public String getSupportedExecutionTypes() {
        final Collection<String> executionTypes = this.connectorsRegistry.getExecutionTypes();
        try {
        	return new ObjectMapper().writeValueAsString(executionTypes);
        } catch (Exception e) {
        	throw new RuntimeException("Unable to generate JSON representation of executionTypes list", e);
        }
    }
    
	@Override
    public String getCommandExecutionTargetDetails(String executionType) {
		final Connector connector = this.connectorsRegistry.getConnectorByExecutionType(executionType);
		if (!(connector instanceof CommandExecutionConnector)) {
			throw new IllegalStateException("Connector providing execution type of \"" + executionType+ "\" does not provide a command execution target, or no Connector providing this execution type exists.");
		}
		final CommandExecutionTarget commandExecutionTarget = ((CommandExecutionConnector) connector).getCommandExecutionTarget();
		
		try {
			// Only send across the Client-Available Connector Details as defined in the superclass of that name,
			// rather than the CommandExecutionTarget subclass itself which is internal to MIF Server
			return new ObjectMapper().writerWithType(ClientAvailableConnectorDetails.class).writeValueAsString(commandExecutionTarget);
		} catch (Exception e) {
			throw new RuntimeException("Unable to generate JSON representation of ClientAvailableConnectorDetails superset of CommandExecutionTarget", e);
		}
    }

    public ConnectorsRegistry getConnectorsRegistry() {
        return this.connectorsRegistry;
    }

    public void setConnectorsRegistry(ConnectorsRegistry connectorsRegistry) {
        this.connectorsRegistry = connectorsRegistry;
    }

}
