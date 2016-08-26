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

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mango.mif.connector.Connector;
import com.mango.mif.core.services.ConnectorsRegistry;


/**
 * 
 * An integration test verifying that connectors are correctly injected into registry
 *
 */
@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration
public class DiscoveringConnectorsRegistryIT {
    
    @Resource(name="connectorsRegistry")
    ConnectorsRegistry connectorsRegistry;
    
    @Before
    public void setUp() {
        int i=0;
        for(Connector connector : connectorsRegistry.getConnectors()) {
            when(connector.getConnectorId()).thenReturn("connector"+i++);
        }
    }
    
    @Test
    public void shouldRetrieveConnectors() {
        assertEquals("Connector Registry is missing connectors", connectorsRegistry.getConnectors().size(),2);
    }

    public static Connector createMockConnector(String connectorId) {
        Connector mock = mock(Connector.class);
        when(mock.getConnectorId()).thenReturn(connectorId);
        
        return mock;
    }
}
