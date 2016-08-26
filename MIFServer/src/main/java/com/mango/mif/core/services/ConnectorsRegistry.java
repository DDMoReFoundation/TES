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
package com.mango.mif.core.services;

import java.util.Collection;

import com.mango.mif.connector.Connector;

/**
 * 
 * A registry holding connectors instances
 */
public interface ConnectorsRegistry {
    
	/**
     * @return a connector by execution type. 
	 */
	Connector getConnectorByExecutionType(String executionType);

    /**
     * @return a connector by connectorId. 
     */
    Connector getConnectorById(String connectorId);
	
	/**
     * @return a list of all connectors. 
	 */
	Collection<Connector> getConnectors();
	
	/**
	 * @return supported execution types by all connectors
	 */
	Collection<String> getExecutionTypes();
}
