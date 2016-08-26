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

import java.net.URI;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  JAXB wrapper for the datastream we get back from the REST Service method.
 */
@XmlRootElement
//@XmlAccessorType(XmlAccessType.PROPERTY)
public class FileSystemItem {
	
		public static final String FOLDER_ITEM = "directory";
		public static final String FILE_ITEM = "file";
		
		private String name;
		
		private String endPath;
		
	    private URI uri;

	    private String type;

	    /**
	     * 
	     * @return
	     */
	    @XmlElement
		public URI getUri() {
			return uri;
		}

		public void setUri(URI uri) {
			this.uri = uri;
		}

		/**
		 * 
		 * @return
		 */
	    @XmlElement
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	    @XmlElement
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlElement
		public String getEndPath() {
			return endPath;
		}

		public void setEndPath(String endPath) {
			this.endPath = endPath;
		}
		
}
