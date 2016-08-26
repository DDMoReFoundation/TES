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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.TestsHelper;


/**
 * 
 * FileUtils based resource retriever unit test
 *
 */
public class FileUtilsResourceRetrieverTest {
	/**
	 * Temporary shared location directory
	 */
	File tmpFileshareDirectory;
	/**
	 * Temporary destination directory
	 */
	File tmpDestinationLocationDirectory;
	
	String requestID;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tmpFileshareDirectory = TestsHelper.createTmpDirectory();
		tmpDestinationLocationDirectory = TestsHelper.createTmpDirectory();
		requestID = UUID.randomUUID().toString();
		new File(tmpFileshareDirectory,requestID).mkdirs();
	}
	@After
	public void tearDown() {
    	FileUtils.deleteQuietly(tmpFileshareDirectory);
    	FileUtils.deleteQuietly(tmpDestinationLocationDirectory);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfDestinationDirIsNotSet() {
		FileUtilsResourceRetriever retriever = new FileUtilsResourceRetriever();
		retriever.setRequestAttributes(createRequestAttributesWithFileShares());
		retriever.setRequestID(requestID);
		retriever.retrieve();
	}
    protected Map<String, String> createRequestAttributesWithFileShares() {
        Map<String,String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), tmpFileshareDirectory.getAbsolutePath());
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), tmpFileshareDirectory.getAbsolutePath());
        return requestAttributes;
    }

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfRequestAttributesAreNotSet() {
		FileUtilsResourceRetriever retriever = new FileUtilsResourceRetriever();
		retriever.setDestinationDir(tmpDestinationLocationDirectory);
		retriever.setRequestID(requestID);
		retriever.retrieve();
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfRequestIDNotSet() {
		FileUtilsResourceRetriever retriever = new FileUtilsResourceRetriever();
        retriever.setRequestAttributes(createRequestAttributesWithFileShares());
		retriever.setDestinationDir(tmpDestinationLocationDirectory);
		retriever.retrieve();
	}

	@Test
	public void shouldConvertASharedLocationPathToDestinationPath() {
		FileUtilsResourceRetriever retriever = new FileUtilsResourceRetriever();
		retriever.setDestinationDir(tmpDestinationLocationDirectory);
        retriever.setRequestAttributes(createRequestAttributesWithFileShares());
		
		File outputDir = new File(tmpFileshareDirectory,requestID);
		
		File aFile = new File(outputDir,"/a/b/c");
		File expected = new File(tmpDestinationLocationDirectory,"a/b/c");
		File result = retriever.getOutputDestinationFile(aFile, requestID);
		
		assertEquals(expected.getAbsolutePath(), result.getAbsolutePath());
	}

	@Test
	public void shouldRetrieveFilesFromSharedLocation() throws IOException {
		FileUtilsResourceRetriever retriever = new FileUtilsResourceRetriever();
		retriever.setDestinationDir(tmpDestinationLocationDirectory);
        retriever.setRequestAttributes(createRequestAttributesWithFileShares());
		retriever.setRequestID(requestID);
		File outputDir = new File(tmpFileshareDirectory,requestID);

		File dirA = new File(outputDir,"A");
		dirA.mkdir();
		File dirB = new File(outputDir,"B");
		dirB.mkdir();
		File dirC = new File(outputDir,"C");
		dirC.mkdir();
		
		File aFile = new File(dirA,"a.txt");
		FileUtils.writeStringToFile(aFile, "This is test content of a.txt file.");

		File bFile = new File(dirB,"b.txt");
		FileUtils.writeStringToFile(bFile, "This is test content of b.txt file.");

		retriever.retrieve();
		
		assertTrue(new File(tmpDestinationLocationDirectory,dirA.getName()).exists());
		assertTrue(new File(tmpDestinationLocationDirectory,dirB.getName()).exists());
		assertTrue(new File(tmpDestinationLocationDirectory,dirC.getName()).exists());
		assertTrue(new File(tmpDestinationLocationDirectory,dirA.getName()+"/" + aFile.getName()).exists());
		assertTrue(new File(tmpDestinationLocationDirectory,dirB.getName()+"/" + bFile.getName()).exists());

	}
	
}
