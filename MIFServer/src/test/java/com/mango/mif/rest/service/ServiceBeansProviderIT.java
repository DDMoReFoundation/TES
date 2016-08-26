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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.apache.cxf.endpoint.Server;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/com/mango/mif/rest/service/ServiceBeansProviderIT-context.xml"
} )
public class ServiceBeansProviderIT {

    @Autowired(required=false)
    ServiceBeansProvider serviceBeansProvider;
    
    @Resource(name="serviceBeans")
    List<Object> serviceBeans;
    
    @Resource(name="restServices")
    Server restServices;
    
    @Test
    public void shouldReturnAvailableServiceBeans() {
        assertNotNull(serviceBeansProvider.createServiceBeans());
        assertTrue("There should be two service beans discovered", serviceBeansProvider.createServiceBeans().size()==2);
    }

    @Test
    public void shouldBeAlistWithTwoServiceBeans() {
        assertNotNull(serviceBeans);
        assertTrue("There should be two service beans discovered", serviceBeans.size()==2);
    }

    @Test
    public void shouldSuccessfulyInstantiateServerBean() {
        assertNotNull(restServices);
    }
}
