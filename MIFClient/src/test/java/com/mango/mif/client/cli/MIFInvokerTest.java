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
package com.mango.mif.client.cli;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class MIFInvokerTest {

    /**
     * Logger
     */
    public static final Logger LOG = Logger.getLogger(MIFInvokerTest.class);
	/**
	 * Example input files directory
	 */
	private File exampleInputFilesDir;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		exampleInputFilesDir = FileUtils.toFile(MIFInvokerTest.class.getResource("example"));
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfBrokerUrlPropertyNotSet() {
		String[] args = new String[] {exampleInputFilesDir.getAbsolutePath(),"connector-id"};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.MIF_REST_SERVICE_URL, "empty");
		properties.setProperty(MIFInvoker.SHARED_FILES, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_NAME, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_PASSWORD, "empty");
		MIFInvoker.checkArguments(args,properties);
	
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfRESTServiceUrlPropertyNotSet() {
		String[] args = new String[] {exampleInputFilesDir.getAbsolutePath(),"connector-id"};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.MIF_BROKER_URL, "empty");
		properties.setProperty(MIFInvoker.SHARED_FILES, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_NAME, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_PASSWORD, "empty");
		MIFInvoker.checkArguments(args,properties);
	
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfSharedLocationPropertyNotSet() {
		String[] args = new String[] {exampleInputFilesDir.getAbsolutePath(),"connector-id"};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.MIF_BROKER_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_REST_SERVICE_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_NAME, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_PASSWORD, "empty");
		MIFInvoker.checkArguments(args,properties);
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfUserNamePropertyNotSet() {
		String[] args = new String[] {exampleInputFilesDir.getAbsolutePath(),"connector-id"};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.SHARED_FILES, "empty");
		properties.setProperty(MIFInvoker.MIF_BROKER_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_REST_SERVICE_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_PASSWORD, "empty");
		MIFInvoker.checkArguments(args,properties);
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfUserPasswordPropertyNotSet() {
		String[] args = new String[] {exampleInputFilesDir.getAbsolutePath(),"connector-id"};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.SHARED_FILES, "empty");
		properties.setProperty(MIFInvoker.MIF_BROKER_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_REST_SERVICE_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_NAME, "empty");
		MIFInvoker.checkArguments(args,properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionIfNotDirectoryPassedNotSet() {
		String[] args = new String[] {new File(exampleInputFilesDir,"example.R").getAbsolutePath(),"connector-id"};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.MIF_BROKER_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_REST_SERVICE_URL, "empty");
		properties.setProperty(MIFInvoker.SHARED_FILES, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_NAME, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_PASSWORD, "empty");
        properties.setProperty(MIFInvoker.MIF_EXECUTION_HOST_FILESHARE_REMOTE, "empty");
        properties.setProperty(MIFInvoker.MIF_EXECUTION_HOST_FILESHARE, "empty");
		MIFInvoker.checkArguments(args,properties);
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionIfNotEnoughArguments() {
		String[] args = new String[] {new File(exampleInputFilesDir,"example.R").getAbsolutePath()};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.MIF_BROKER_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_REST_SERVICE_URL, "empty");
		properties.setProperty(MIFInvoker.SHARED_FILES, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_NAME, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_PASSWORD, "empty");
		MIFInvoker.checkArguments(args,properties);
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionIfDirectoryDoesNotExist() {
		String[] args = new String[] {new File(exampleInputFilesDir,"doesNotExist").getAbsolutePath()};
		Properties properties = new Properties();
		properties.setProperty(MIFInvoker.MIF_BROKER_URL, "empty");
		properties.setProperty(MIFInvoker.MIF_REST_SERVICE_URL, "empty");
		properties.setProperty(MIFInvoker.SHARED_FILES, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_NAME, "empty");
		properties.setProperty(MIFInvoker.MIF_CLIENT_USER_PASSWORD, "empty");
		MIFInvoker.checkArguments(args,properties);
	}

    @Test
    public void shouldLoadPropertiesFromTheSpecifiedPath() throws IOException {
        File file = File.createTempFile("MIFInvokerTest","");
        try {
            FileUtils.writeStringToFile(file, "a=valuea\nb=valueb\nc=valuec", "ISO8859_1");
            Properties properties = new Properties();
            MIFInvoker.loadProperties(file.getAbsolutePath(),properties);
            assertEquals("valuea", properties.get("a"));
            assertEquals("valueb", properties.get("b"));
            assertEquals("valuec", properties.get("c"));
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }
}
