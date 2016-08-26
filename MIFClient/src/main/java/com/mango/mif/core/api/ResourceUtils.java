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
package com.mango.mif.core.api;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mango.mif.core.resource.fileutils.FileUtilsResourceRetriever;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * 
 * Helper class for accessing and operating on resources
 * 
 */
public class ResourceUtils {
	/**
	 * Logger
	 */
	public final static Logger LOG = Logger.getLogger(ResourceUtils.class);
	/**
	 * Creates empty folder
	 * @param fileSrc
	 * @param fileDest
	 * @throws IOException
	 */
	public static void createEmptyFolder(File fileSrc, File fileDest) throws IOException {
		
		Preconditions.checkNotNull(fileSrc, "From resource is null.");
		Preconditions.checkNotNull(fileDest, "To file is null");
		LOG.debug("copyEmptyFolder " + fileSrc.getAbsolutePath() + " to " + fileDest.getAbsolutePath());
		if (!fileSrc.exists()) {
			return;
		}

		if (fileSrc.isDirectory()) {
			// if directory not exists, create it
			if (!fileDest.exists()) {
			    FileUtils.forceMkdir(fileDest);
			}
		}
		
	}
	
	
    /**
     * recursively trespasses through the directory structure and returns sorted list of all the files not matching the filename filter
     * 
     * The method ensures that directories are returned only if they are empty.
     * 
     * @param directory
     * @return
     */
    public static List<File> listFiles(File directory, FilenameFilter fileNameFilter) {
    	List<File> result = Lists.newArrayList();
    	for(File file : directory.listFiles(fileNameFilter)) {
    		if(file.isDirectory()) {
    			result.addAll(listFiles(file,fileNameFilter));
    		} else {
    			result.add(file.getAbsoluteFile());
    			LOG.info("File: " + file);
    		}
        }
    	if(result.isEmpty()) {
    		result.add(directory.getAbsoluteFile());
			LOG.info("File: " + directory);
    	}
    	Collections.sort(result, new Comparator<File>(){
			@Override
			public int compare(File fileA, File fileB) {
				return fileA.getAbsolutePath().compareTo(fileB.getAbsolutePath());
			}
    		
    	});
    	return result;
    }
    /**
     * recursively trespasses through the directory structure and returns sorted list of all the files 
     * 
     * @param directory
     * @return
     */
    public static List<File> listFiles(File directory) {
    	return listFiles(directory, new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		});
    }
	/**
	 * Copies file
	 * @param file
	 * @throws IOException 
	 */
	public static void copyFile(File file, File destFile) throws IOException {
		if (file.isDirectory()) {
			FileUtils.copyDirectory(file, destFile);
		} else {
			FileUtils.copyFile(file, destFile);
		}
	}
	

	/**
	 * 
	 * @param file
	 * @param requestID
	 * @return a relative file path
	 */
	public final static File convertToRelativePath(File rootDirectory, File file) {
		String path = file.getAbsolutePath().replace(rootDirectory.getAbsolutePath()+File.separator, "");
		return new File(path);
	}
	
	/**
	 * Copies results to a output directory
	 * 
	 * TODO it does not work in AsUser environment
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void dumpExecutionFiles(File outputDirectory,
			ExecutionRequest executionRequest,
			ExecutionResponse executionResponse)
			throws IOException, JAXBException {
		File requestResultsDir = new File(outputDirectory, executionResponse
				.getRequestId().toString());
		FileUtils.writeStringToFile(new File(requestResultsDir,
				"executionResponse.xml"), JAXBUtils.marshall(executionResponse,
				ExecutionJaxbUtils.CONTEXT_CLASSES), "UTF-8");
		FileUtils.writeStringToFile(new File(requestResultsDir,
				"executionRequest.xml"), JAXBUtils.marshall(executionRequest,
				ExecutionJaxbUtils.CONTEXT_CLASSES), "UTF-8");
		requestResultsDir.mkdir();
		
		FileUtilsResourceRetriever retriever = new FileUtilsResourceRetriever();
		retriever.setDestinationDir(requestResultsDir);
		retriever.setRequestID(executionRequest.getRequestId().toString());
		retriever.setRequestAttributes(executionRequest.getRequestAttributes());
		
		retriever.retrieve();
	}
}
