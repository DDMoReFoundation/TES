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
package com.mango.mif.core.resource.shell;

import java.util.ArrayList;
import java.util.List;

import com.mango.mif.core.exec.CommandBuilder;

/**
 * The Class ResourceRetrieverSetter.
 * Responsible for setting arguments to set up a resource retriever. 
 */
public class ResourceRetrieverSetter {
	/**
	 * Request ID
	 */
	protected String	requestID;
	
	/** The builder. */
	CommandBuilder builder;
	
	/**
	 * User name
	 */
	private String	userName;
	/**
	 * User encrypted password
	 */
	private String	password;
	
	/** The files list which are to be ignored during copy. */
	private List<String> ignoreFilesList = new ArrayList<String>();
	
	/**
     * The port for JSCH to use
     */
    private int port = 22;
	
    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getIgnoreFilesList() {
		return ignoreFilesList;
	}

	public void setIgnoreFilesList(List<String> ignoreFilesList) {
		this.ignoreFilesList = ignoreFilesList;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public CommandBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(CommandBuilder builder) {
		this.builder = builder;
	}


}
