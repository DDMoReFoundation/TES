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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.TestsHelper;
import com.mango.mif.core.resource.shell.PublisherParameters;


/**
 * FileUtils based resource publisher unit test
 */
public class FileUtilsResourcePublisherTest {
	File tmpFileshareDirectory;
	File tmpRootLocationDirectory;

	@Before
	public void setUp() {
		tmpFileshareDirectory = TestsHelper.createTmpDirectory();
		tmpRootLocationDirectory = TestsHelper.createTmpDirectory();
	}

	@After
	public void tearDown() {
    	FileUtils.deleteQuietly(tmpFileshareDirectory);
    	FileUtils.deleteQuietly(tmpRootLocationDirectory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfRootDirectoryIsNotSet() {
        Map<String,String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), tmpFileshareDirectory.getAbsolutePath());
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), tmpFileshareDirectory.getAbsolutePath());
        
        PublisherParameters publisherParameters = new PublisherParameters();
        publisherParameters.setRequestAttributes(requestAttributes);
		FileUtilsResourcePublisher publisher = new FileUtilsResourcePublisher(publisherParameters);
		publisher.publish();
		
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfRequestAttributesAreNotSet() {
        PublisherParameters publisherParameters = new PublisherParameters();
        publisherParameters.setRootDirectory(tmpRootLocationDirectory);
        FileUtilsResourcePublisher publisher = new FileUtilsResourcePublisher(publisherParameters);
		publisher.publish();
	}


    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfRequestAttributeEXECUTION_HOST_FILESHAREIsNotSet() {
        Map<String,String> requestAttributes = Maps.newHashMap();
        PublisherParameters publisherParameters = new PublisherParameters();
        publisherParameters.setRequestAttributes(requestAttributes);
        publisherParameters.setRootDirectory(tmpRootLocationDirectory);
        new FileUtilsResourcePublisher(publisherParameters);
    }
    
	@Test
	public void shouldConvertTheExternalFilePathToSharedLocationPath() {
        Map<String,String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), tmpFileshareDirectory.getAbsolutePath());
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), tmpFileshareDirectory.getAbsolutePath());
        
        PublisherParameters publisherParameters = new PublisherParameters();
        publisherParameters.setRequestAttributes(requestAttributes);
        publisherParameters.setRootDirectory(tmpRootLocationDirectory);
        FileUtilsResourcePublisher publisher = new FileUtilsResourcePublisher(publisherParameters);
		String requestID = UUID.randomUUID().toString();
		File aFile = new File(tmpRootLocationDirectory,"a/b/c");
		File expected = new File(tmpFileshareDirectory,requestID+"/a/b/c");
		File result = publisher.getRequestDestinationFile(aFile, requestID);
		assertEquals(expected.getAbsolutePath(), result.getAbsolutePath());
	}

	
	@Test
	public void shouldPublishListOfInputFiles() throws IOException {
        Map<String,String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), tmpFileshareDirectory.getAbsolutePath());
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), tmpFileshareDirectory.getAbsolutePath());
        
        PublisherParameters publisherParameters = new PublisherParameters();
        publisherParameters.setRequestAttributes(requestAttributes);
        publisherParameters.setRootDirectory(tmpRootLocationDirectory);
        FileUtilsResourcePublisher publisher = new FileUtilsResourcePublisher(publisherParameters);
		
		File dirA = new File(tmpRootLocationDirectory,"A");
		dirA.mkdir();
		File dirB = new File(tmpRootLocationDirectory,"B");
		dirB.mkdir();
		File dirC = new File(tmpRootLocationDirectory,"C");
		dirC.mkdir();
		
		File aFile = new File(dirA,"a.txt");
		FileUtils.writeStringToFile(aFile, "This is test content of a.txt file.");

		File bFile = new File(dirB,"b.txt");
		FileUtils.writeStringToFile(bFile, "This is test content of b.txt file.");
		
		List<File> files = Lists.newArrayList(dirA, dirB, dirC, aFile, bFile);
		
		publisher.addFiles(files);
		
		String requestID = publisher.publish();
		
		File requestInputDirectory = new File(tmpFileshareDirectory,requestID);
		assertTrue(new File(requestInputDirectory,dirA.getName()).exists());
		assertTrue(new File(requestInputDirectory,dirB.getName()).exists());
		assertTrue(new File(requestInputDirectory,dirC.getName()).exists());
		assertTrue(new File(requestInputDirectory,dirA.getName()+"/" + aFile.getName()).exists());
		assertTrue(new File(requestInputDirectory,dirB.getName()+"/" + bFile.getName()).exists());
	}
}
