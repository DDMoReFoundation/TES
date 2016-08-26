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
package com.mango.mif.client.api.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
 * REST Service interface for determining available job connectors on the server.
 */
@Path("/jobconnectorregistry")
public interface JobConnectorRegistryService {

    /**
     * Get a list of the execution types indicating the connectors that the server supports.
     * One connector may supports multiple execution types, which are in essence all synonyms
     * for that same connector.
     * <p>
     * @return a JSON list of the executionTypes supported by the installed connectors
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String getSupportedExecutionTypes();
    
    /**
     * Get JSON representation of a CommandExecutionTarget in MIFServer; this is "discovered" by clients of MIFServer.
     * <p>
     * @param executionType - the executionType identifying a ConnectorDescriptor in MIFServer for which
     * 						  to obtain the Connector itself and thereby the CommandExecutionTarget
     * @return JSON representation of the CommandExecutionTarget
     */
    @GET
    @Path("{executionType}")
    @Produces(MediaType.APPLICATION_JSON)
    String getCommandExecutionTargetDetails(@PathParam("executionType") String executionType);
    
}
