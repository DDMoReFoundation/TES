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
package com.mango.mif.core.services;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mango.mif.connector.Connector;
import com.mango.mif.connector.ConnectorDescriptor;
import com.mango.mif.core.services.DiscoveringConnectorsRegistry;

/**
 * 
 * Tests DiscoveringConnectorsRegistry
 * 
 */
public class DiscoveringConnectorsRegistryTest {
    private DiscoveringConnectorsRegistry connectorsRegistry;
    private final static String MOCK_EXECUTION_TYPE = "executionType";
    private final static String CONNECTOR_ID = "CONNECTOR-ID";
    
    @Before
    public void setUp() throws Exception {
        Collection<ConnectorDescriptor> connectorDescriptors = Lists.newArrayList();
        Connector connector = mock(Connector.class);
        when(connector.getConnectorId()).thenReturn(CONNECTOR_ID);
        ConnectorDescriptor descriptor = new ConnectorDescriptor(connector, Sets.newHashSet(MOCK_EXECUTION_TYPE));
        connectorDescriptors.add(descriptor);
        connectorsRegistry = new DiscoveringConnectorsRegistry();
        connectorsRegistry.setConnectorDescriptors(connectorDescriptors);
    }

    @Test
    public void shouldReturnConnectorForExecutionType() {
        assertNotNull(connectorsRegistry.getConnectorByExecutionType(MOCK_EXECUTION_TYPE));
        assertEquals(CONNECTOR_ID, connectorsRegistry.getConnectorByExecutionType(MOCK_EXECUTION_TYPE).getConnectorId());
    }

    @Test
    public void shouldReturnNullIfConnectorForGivenExecutionTypeDoesNotExist() {
        assertNull(connectorsRegistry.getConnectorByExecutionType("NOT-BOUND-EXECUTION-TYPE"));
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfMoreThanOneConnectorExistsWithGivenId() {

        Connector connector = mock(Connector.class);
        when(connector.getConnectorId()).thenReturn(CONNECTOR_ID);
        
        Collection<ConnectorDescriptor> connectorDescriptors = Lists.newArrayList();
        ConnectorDescriptor descriptor = new ConnectorDescriptor(connector, Sets.newHashSet(MOCK_EXECUTION_TYPE));
        connectorDescriptors.add(descriptor);
        descriptor = new ConnectorDescriptor(connector, Sets.newHashSet(MOCK_EXECUTION_TYPE));
        connectorDescriptors.add(descriptor);
        connectorsRegistry.setConnectorDescriptors(connectorDescriptors);
        
        connectorsRegistry.getConnectorById(CONNECTOR_ID);
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfMoreThanOneConnectorExistsForExecutionType() {

        Connector connector = mock(Connector.class);
        when(connector.getConnectorId()).thenReturn(CONNECTOR_ID);
        
        Collection<ConnectorDescriptor> connectorDescriptors = Lists.newArrayList();
        ConnectorDescriptor descriptor = new ConnectorDescriptor(connector, Sets.newHashSet(MOCK_EXECUTION_TYPE));
        connectorDescriptors.add(descriptor);
        descriptor = new ConnectorDescriptor(connector, Sets.newHashSet(MOCK_EXECUTION_TYPE));
        connectorDescriptors.add(descriptor);
        connectorsRegistry.setConnectorDescriptors(connectorDescriptors);
        
        connectorsRegistry.getConnectorByExecutionType(MOCK_EXECUTION_TYPE);
    }

    @Test
    public void shouldReturnConnectorWithGivenId() {
        assertNotNull(connectorsRegistry.getConnectorById(CONNECTOR_ID));
        assertEquals(CONNECTOR_ID, connectorsRegistry.getConnectorById(CONNECTOR_ID).getConnectorId());
    }

    @Test
    public void shouldReturnNullIfConnectorWithGivenConnectorIdDoesNotExist() {
        assertNull(connectorsRegistry.getConnectorById("NOT-BOUND-CONNECTOR-ID"));
    }

    @Test
    public void shouldReturnAllRegisteredConnectors() {
        assertTrue("Connector Registry returned empty list of connectors", connectorsRegistry.getConnectors().size()>0);
    }
    
    @Test
    public void shouldReturnAllSupportedExecutionTypes() {
        assertTrue("Connector Registry returned empty list of execution types", connectorsRegistry.getExecutionTypes().size()>0);
    }
}
