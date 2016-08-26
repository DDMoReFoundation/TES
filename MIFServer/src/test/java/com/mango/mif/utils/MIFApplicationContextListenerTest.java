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
package com.mango.mif.utils;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.collect.Lists;
import com.mango.mif.connector.Connector;
import com.mango.mif.core.services.ConnectorsRegistry;


/**
 * Tests {@link MIFApplicationContextListener}
 */
@RunWith(MockitoJUnitRunner.class)
public class MIFApplicationContextListenerTest {
    @Mock
    private ApplicationContext applicationContext;
    
    @Mock
    private ContextRefreshedEvent contextEvent;
    
    @Before
    public void setUp() {
        when(contextEvent.getApplicationContext()).thenReturn(applicationContext);
    }
    
    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfConnectorsRegistryIsMissing() {
        MIFApplicationContextListener instance = new MIFApplicationContextListener();
        instance.onApplicationEvent(contextEvent);
    }

    @Test
    public void shouldExecuteFailureRecoveryOnConnectors() {
        ConnectorsRegistry connectorRegistry = mock(ConnectorsRegistry.class);
        Connector connectorA = mock(Connector.class);
        Connector connectorB = mock(Connector.class);
        when(connectorRegistry.getConnectors()).thenReturn(Lists.newArrayList(connectorA, connectorB));
        
        when(applicationContext.getBean(eq("connectorsRegistry"))).thenReturn(connectorRegistry);
        
        MIFApplicationContextListener instance = new MIFApplicationContextListener();
        instance.onApplicationEvent(contextEvent);

        
        verify(connectorA,times(1)).doFailureRecovery();
        verify(connectorB,times(1)).doFailureRecovery();
    }
}
