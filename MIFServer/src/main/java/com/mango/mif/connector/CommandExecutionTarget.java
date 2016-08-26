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

import com.mango.mif.domain.ClientAvailableConnectorDetails;


/**
 * Contains execution target (Third-Party Tool and Environment) runtime parameters
 * associated with a given command. The subset of these parameters that are to be
 * exposed to clients are held in the {@link ClientAvailableConnectorDetails}
 * superclass within MIFClient; this CommandExecutionTarget class is internal to
 * MIFServer.
 */
public class CommandExecutionTarget extends ClientAvailableConnectorDetails {

	private String toolExecutablePath;
    private String customScriptsDirectory;
	private String environmentSetupScript;
	private String converterToolboxPath;
	
	/**
	 * @return the executable to run
	 */
	public String getToolExecutablePath() {
		return toolExecutablePath;
	}
	
	/**
	 * @param toolExecutablePath - the executable to run
	 */
	public void setToolExecutablePath(String toolExecutablePath) {
		this.toolExecutablePath = toolExecutablePath;
	}
	
	/**
	 * Return the Connector-specific scripts directory. Depending on whether this
	 * is called pre- or post- {@link com.mango.mif.connector.ConnectorDescriptor ConnectorDescriptor}
	 * initialisation, this will either return a classpath-based location or a filesystem
	 * absolute path - see the explanation on {@link #setCustomScriptsDirectory(String)}.
	 * @see #setCustomScriptsDirectory(String)
	 * @see {@link com.mango.mif.connector.ConnectorDescriptor#extractCustomScripts()}
	 * <p>
	 * @return the directory containing scripts specific to this Connector
	 */
	public String getCustomScriptsDirectory() {
	    return this.customScriptsDirectory;
    }

	/**
	 * This Connector-specific scripts directory property should be Spring-configured
	 * to be classpath-based, since the Connector and all its resources should be
	 * self-contained within its JAR. However as part of initialisation, these scripts
	 * are extracted from the JAR file, and this customScriptsDirectory property is
	 * updated to be the absolute path to the directory in which the scripts were
	 * extracted, to enable the FreeMarker-generated shell scripts to execute these
	 * custom scripts via e.g.
	 * <code>${job.commandExecutionTarget.customScriptsDirectory}/myscript.sh</code>.
	 * @see {@link com.mango.mif.connector.ConnectorDescriptor#extractCustomScripts()}
	 * <p>
	 * @param customScriptsDirectory - the directory containing scripts specific to this Connector
	 */
	public void setCustomScriptsDirectory(final String customScriptsDirectory) {
		this.customScriptsDirectory = customScriptsDirectory;
    }
	
	/**
	 * @return a setup script that is intended to be run prior to each execution of a command,
	 * 		   if referenced as such from within the appropriate FreeMarker template
	 */
	public String getEnvironmentSetupScript() {
		return environmentSetupScript;
	}

	/**
	 * @param environmentSetupScript - a setup script that is intended to be run prior to each
	 * 								   execution of a command, if referenced as such from within
	 * 								   the appropriate FreeMarker template
	 */
	public void setEnvironmentSetupScript(String environmentSetupScript) {
		this.environmentSetupScript = environmentSetupScript;
	}
	
	/**
	 * @return absolute path to the converter toolbox executable
	 */
	public String getConverterToolboxPath() {
		return converterToolboxPath;
	}

	/**
	 * @param converterToolboxPath - absolute path to the converter toolbox executable
	 */
	public void setConverterToolboxPath(String converterToolboxPath) {
		this.converterToolboxPath = converterToolboxPath;
	}

}
