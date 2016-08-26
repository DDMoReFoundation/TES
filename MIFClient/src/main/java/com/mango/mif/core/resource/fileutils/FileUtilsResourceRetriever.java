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
package com.mango.mif.core.resource.fileutils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.mango.mif.core.api.ResourceUtils;
import com.mango.mif.core.resource.BaseResourceRetriever;

/**
 * 
 * Basic FileUtils resource retriever that copies files from the output directory of a request 
 * to a specified destination directory
 * 
 */
public class FileUtilsResourceRetriever extends BaseResourceRetriever {
	/**
	 * Logger
	 */
	public final static Logger LOG = Logger.getLogger(FileUtilsResourceRetriever.class);
	/**
	 * Destination directory
	 */
	private File destinationDir;
	
	@Override
	public void retrieve() {
		Preconditions.checkNotNull(destinationDir);
		Preconditions.checkNotNull(requestID);
        Preconditions.checkNotNull(requestAttributes);
		
		File requestDir = getRequestDirectory(requestID);
		
		List<File> files = ResourceUtils.listFiles(requestDir, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		});
		for(File file : files) {
			try {
				ResourceUtils.copyFile(file, getOutputDestinationFile(file,requestID));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * @param destinationDir the destinationDir to set
	 */
	public void setDestinationDir(File destinationDir) {
		this.destinationDir = destinationDir;
	}
	/**
	 * 
	 * @param file
	 * @param requestID
	 * @return a file path of the file on the shared location
	 */
	File getOutputDestinationFile(File file, String requestID) {
		File requestDir = getRequestDirectory(requestID);
		String path = file.getAbsolutePath().replace(requestDir.getAbsolutePath()+File.separator, "");
		return new File(destinationDir,path);
	}
}
