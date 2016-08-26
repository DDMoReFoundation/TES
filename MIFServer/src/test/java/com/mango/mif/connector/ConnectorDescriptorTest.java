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
package com.mango.mif.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.mango.mif.connector.impl.CommandExecutionConnector;


public class ConnectorDescriptorTest {

	private CommandExecutionConnector connector;

	/**
	 * Set-up tasks prior to each test being run.
	 */
	@Before
	public void setUp() throws Exception {
		this.connector = mock(CommandExecutionConnector.class);
		stub(this.connector.getConnectorId()).toReturn("CONN_ID");
		final CommandExecutionTarget commandExecutionTarget = new CommandExecutionTarget();
		commandExecutionTarget.setCustomScriptsDirectory("classpath:/com/mango/mif/helloworld-connector/scripts");
		// Connector is a mock object but since its commandExecutionTarget accessor methods are final it is actually setting this property
		this.connector.setCommandExecutionTarget(commandExecutionTarget);
	}
	
	@Test
	public void testConstructorSingleExecutionType() {
		final ConnectorDescriptor descr = new ConnectorDescriptor(this.connector, "EXECTYPE1");
		assertSame("Checking the connector", this.connector, descr.getConnector());
		assertEquals("Checking the executionTypes", Sets.newHashSet("EXECTYPE1"), descr.getExecutionTypes());
	}
	
	@Test
	public void testConstructorMultipleExecutionType() {
		final ConnectorDescriptor descr = new ConnectorDescriptor(this.connector, Sets.newHashSet("EXECTYPE1", "EXECTYPE2"));
		assertSame("Checking the connector", this.connector, descr.getConnector());
		assertEquals("Checking the executionTypes", Sets.newHashSet("EXECTYPE1", "EXECTYPE2"), descr.getExecutionTypes());
	}
	
	@Test
	public void testInitialisationOfCustomScriptsDirectory() throws IOException, URISyntaxException {
		final ConnectorDescriptor descr = new ConnectorDescriptor(this.connector, "EXECTYPE1");
		descr.extractCustomScripts();
		final String updatedCustomScriptsDirectory = this.connector.getCommandExecutionTarget().getCustomScriptsDirectory();
		assertTrue("Custom Scripts Directory should now be an absolute path", new File(updatedCustomScriptsDirectory).isAbsolute());
		assertEquals("Checking the contents of the custom script file read from its absolute-path location",
			"Hello world!\r\n", FileUtils.readFileToString(new File(updatedCustomScriptsDirectory, "blah.script")));
	}

}
