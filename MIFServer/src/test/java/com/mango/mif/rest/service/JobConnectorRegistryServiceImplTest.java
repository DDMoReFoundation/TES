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
package com.mango.mif.rest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.impl.CommandExecutionConnector;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.domain.ClientAvailableConnectorDetails;


@RunWith(PowerMockRunner.class)
@PrepareForTest(CommandExecutionConnector.class)
public class JobConnectorRegistryServiceImplTest {

	private JobConnectorRegistryServiceImpl registryService;
	private ConnectorsRegistry connectorsRegistry;

	/**
	 * Set-up tasks prior to each test being run.
	 */
	@Before
	public void setUp() {
		this.registryService = new JobConnectorRegistryServiceImpl();
		this.connectorsRegistry = mock(ConnectorsRegistry.class);
		this.registryService.setConnectorsRegistry(this.connectorsRegistry);
	}

	/**
	 * Test method for
	 * {@link com.mango.mif.rest.service.JobConnectorRegistryServiceImpl#setConnectorsRegistry(com.mango.mif.core.services.ConnectorsRegistry)}
	 * and {@link com.mango.mif.rest.service.JobConnectorRegistryServiceImpl#getConnectorsRegistry()}.
	 */
	@Test
	public void testSetGetConnectorsRegistry() {
		final ConnectorsRegistry r = mock(ConnectorsRegistry.class);
		this.registryService.setConnectorsRegistry(r);
		assertSame(r, this.registryService.getConnectorsRegistry());
	}

	/**
	 * Test method for {@link com.mango.mif.rest.service.JobConnectorRegistryServiceImpl#getSupportedExecutionTypes()}.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@Test
	public void testGetConnectorNames() throws JsonParseException, JsonMappingException, IOException {
		when(this.connectorsRegistry.getExecutionTypes()).thenReturn(Arrays.asList("CONN1", "CONN2", "CONN3"));
		List<String> foundExecutionTypes = new ObjectMapper().readValue(registryService.getSupportedExecutionTypes(), List.class);
		assertEquals(Arrays.asList("CONN1","CONN2","CONN3"), foundExecutionTypes);
	}

	/**
	 * Test method for {@link com.mango.mif.rest.service.JobConnectorRegistryServiceImpl#getCommandExecutionTargetDetails(java.lang.String)}.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@Test
	public void testGetCommandExecutionTargetDetails() throws JsonParseException, JsonMappingException, IOException {
		// Set up a fully populated CommandExecutionTarget
		final CommandExecutionTarget commandExecTarget = new CommandExecutionTarget();
		commandExecTarget.setToolExecutablePath("/path/to/my_command");
		commandExecTarget.setResultsIncludeRegex(".*\\.out");
        commandExecTarget.setResultsExcludeRegex(".*\\.exe");
		commandExecTarget.setEnvironmentSetupScript("setup.bat");
		commandExecTarget.setConverterToolboxPath("/path/to/convert.bat");
		
		final CommandExecutionConnector connector = PowerMockito.mock(CommandExecutionConnector.class);
		stub(connector.getCommandExecutionTarget()).toReturn(commandExecTarget);
		
		when(this.connectorsRegistry.getConnectorByExecutionType("CONN2")).thenReturn(connector);

		// Call the method under test
		final String json = this.registryService.getCommandExecutionTargetDetails("CONN2");

		// Convert back into a ClientAvailableConnectorDetails instance; in the real world this is done in CommandRegistryImpl in FIS		
		final ClientAvailableConnectorDetails deserialisedClientAvailableConnectorDetails = new ObjectMapper().readValue(json, ClientAvailableConnectorDetails.class);
		assertEquals("Checking that the Included Output Filenames Regular Expression was retrieved correctly",
			commandExecTarget.getResultsIncludeRegex(), deserialisedClientAvailableConnectorDetails.getResultsIncludeRegex());
        assertEquals("Checking that the Excluded Output Filenames Regular Expression was retrieved correctly",
            commandExecTarget.getResultsExcludeRegex(), deserialisedClientAvailableConnectorDetails.getResultsExcludeRegex());
        
		// Convert back into a CommandExecutionTarget instance; this is checking that the MIFServer-internal
		// CommandExecutionTarget-specific properties are not accidentally exposed to clients
		final CommandExecutionTarget deserialisedCommandExecTarget = new ObjectMapper().readValue(json, CommandExecutionTarget.class);
		assertNull("Checking that the Tool Executable Path was NOT exposed to clients",
			deserialisedCommandExecTarget.getToolExecutablePath());
		assertNull("Checking that the Environment Setup Script was NOT exposed to clients",
			deserialisedCommandExecTarget.getEnvironmentSetupScript());
		assertNull("Checking that the Converter Toolbox Path was NOT exposed to clients",
			deserialisedCommandExecTarget.getConverterToolboxPath());
		assertEquals("Checking that the Excluded Output Filenames Regular Expression was retrieved correctly",
			commandExecTarget.getResultsExcludeRegex(), deserialisedCommandExecTarget.getResultsExcludeRegex());
        assertEquals("Checking that the Included Output Filenames Regular Expression was retrieved correctly",
            commandExecTarget.getResultsIncludeRegex(), deserialisedCommandExecTarget.getResultsIncludeRegex());
			
	}
	
	/**
	 * Test method for {@link com.mango.mif.rest.service.JobConnectorRegistryServiceImpl#getCommandExecutionTargetDetails(java.lang.String)}.
	 */
	@Test(expected=IllegalStateException.class)
	public void testGetCommandExecutionTargetDetailsForNonExistentConnector() {
		this.registryService.getCommandExecutionTargetDetails("CONN1");
	}

}
