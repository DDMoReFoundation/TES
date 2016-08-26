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
package com.mango.mif.core.resource;

import java.io.File;
import java.util.Map;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;


/**
 * 
 * A basic implementation of the resource retriever
 *
 */
public abstract class BaseResourceRetriever implements ResourceRetriever {

    protected String requestID;
	/**
	 * Destination directory where the result files should be copied into
	 */
	protected File destinationDir;
    /**
     * The request attributes
     */
    protected Map<String, String> requestAttributes;
	
	@Override
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	
	public String getRequestID() {
		return requestID;
	}

	/**
	 * @param destinationDir the destinationDir to set
	 */
	public void setDestinationDir(File destinationDir) {
		this.destinationDir = destinationDir;
	}

	
    /**
     * @param requestAttributes the requestAttributes to set
     */
    public void setRequestAttributes(Map<String, String> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }
    /**
     * Gets the request directory.
     *
     * @param uuid the uuid
     * @return the request directory
     */
    public File getRequestDirectory(String uuid) {
        return new File(requestAttributes.get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()), uuid);
    }
}
