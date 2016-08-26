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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB wrapper for the List of file links we get back from the REST Service method.
 */
@XmlRootElement(name = "fileList")
//@XmlAccessorType(XmlAccessType.PROPERTY)
public class FileList {
	
	
	/**
	 * List of wrapper objects that model a file/folder item.
	 */
	private List<FileSystemItem> fileSystemItems = new ArrayList<FileSystemItem>();


	/**
	 * @return the files
	 */
	@XmlElementWrapper(name="fileSystemItems")
	@XmlElement(name="fileSystemItem")
	public List<FileSystemItem> getFileSystemItems() {
		return fileSystemItems;
	}

	
	/**
	 * @param files the files to set
	 */
	public void setFileSystemItems(List<FileSystemItem> fileSystemItems) {
		this.fileSystemItems = fileSystemItems;
	}

}
