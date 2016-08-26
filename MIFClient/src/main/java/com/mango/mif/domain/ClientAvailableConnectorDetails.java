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
package com.mango.mif.domain;


/**
 * Certain configured properties of a Connector need to be made available to clients of MIF.
 * <p>
 * This class contains these.
 * <p>
 * Within MIFServer, {@link com.mango.mif.connector.CommandExecutionTarget CommandExecutionTarget}
 * subclasses this class to add the properties required for command execution (primarily, the path to
 * the tool to run). It is only clients of MIF that would actually use this
 * ClientAvailableConnectorDetails class directly.
 */
public class ClientAvailableConnectorDetails {

    private String resultsIncludeRegex;
    private String resultsExcludeRegex;
	
	/**
	 * @param outputFilenamesRegex - the regular expression that matches the names of those files generated
	 * 								 (or modified) as part of execution of the tool and that are to be
	 * 								 copied back to the client
	 * @deprecated use {@link ClientAvailableConnectorDetails#setResultsIncludeRegex(String)}
	 */
	@Deprecated
	public void setOutputFilenamesRegex(final String outputFilenamesRegex) {
		this.resultsIncludeRegex = outputFilenamesRegex;
	}
	/**
	 * @return the regular expression that matches the names of those files generated (or modified) as part
	 * 		   of execution of the tool and that are to be copied back to the client.
	 * @deprecated use {@link ClientAvailableConnectorDetails#getResultsIncludeRegex()}
	 */
	@Deprecated
	public String getOutputFilenamesRegex() {
		return this.resultsIncludeRegex;
	}

    public void setResultsExcludeRegex(String resultsExcludeRegex) {
        this.resultsExcludeRegex = resultsExcludeRegex;
    }
    
    public void setResultsIncludeRegex(String resultsIncludeRegex) {
        this.resultsIncludeRegex = resultsIncludeRegex;
    }
    
    /**
     * @return the regular expression that matches the names of those files generated (or modified) as part
     *         of execution of the tool and that are NOT to be copied back to the client.
     */
    public String getResultsExcludeRegex() {
        return resultsExcludeRegex;
    }
    
    /**
     * @return the regular expression that matches the names of those files generated (or modified) as part
     *         of execution of the tool and that are to be copied back to the client.
     */
    public String getResultsIncludeRegex() {
        return resultsIncludeRegex;
    }
}
