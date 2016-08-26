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
package com.mango.mif.helloworldremote.rest;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mango.mif.client.api.rest.ConnectorService;
import com.mango.mif.connector.impl.CommandExecutionConnector;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.exception.MIFException;


/**
 * Implementation of REST services specific to Hello World Connector.
 */
@Service
@Path("/helloworld-services")
public class ConnectorServiceImpl implements ConnectorService {

	private static final Logger LOGGER = Logger.getLogger(ConnectorServiceImpl.class);
	
	@Autowired
	private ConnectorsRegistry connectorsRegistry;	

	@GET
    @Path("/getSomeInfo/{myParam}")
    @Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description("Performs a dummy action") })
	public String doSomething(@PathParam("myParam") String myParam) throws MIFException {
	
		final StringBuffer strBuf = new StringBuffer();
		final CommandExecutionConnector connector = (CommandExecutionConnector) this.connectorsRegistry.getConnectorById("HELLOWORLD_LINUX_REMOTE");
		strBuf.append("The time is now: " + DateFormat.getDateTimeInstance().format(new GregorianCalendar().getTime()) + "\n");
		strBuf.append("Your parameter was: " + myParam + "\n");
		strBuf.append("Hello-World execution target details are:\n"
				+ "  toolExecutablePath=" + connector.getCommandExecutionTarget().getToolExecutablePath() + "\n"
				+ "  customScriptsDirectory=" + connector.getCommandExecutionTarget().getCustomScriptsDirectory() + "\n"
				+ "  environmentSetupScript=" + connector.getCommandExecutionTarget().getEnvironmentSetupScript() + "\n"
		);
		
		return strBuf.toString();
	}
    
}
