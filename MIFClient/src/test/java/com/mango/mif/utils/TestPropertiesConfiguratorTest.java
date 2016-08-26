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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Tests TestPropertiesConfigurator
 */
public class TestPropertiesConfiguratorTest {
    private final static String OVERWRITTEN_PROPERTY = "overwritten";
    
    private static final Properties initialProperties = new Properties();
    
    @BeforeClass
    public static void setUp() {
        initialProperties.putAll(System.getProperties());
    }
    
    @After
    public void tearDown() {
        Properties p = new Properties();
        p.putAll(initialProperties);
        System.setProperties(p);
    }

    @Test
    public void shouldLoadDefaultProperties() throws IOException {
        TestPropertiesConfigurator.TEST_PROPERTIES_RESOURCE="/com/mango/mif/utils/first.properties";
        TestPropertiesConfigurator.initProperties();
        
        assertNotNull("Property from the default property file was not loaded.",System.getProperty("notOverwritten"));
    }
    
    @Test
    public void shouldLoadPropertiesFromSpecifiedLocation() throws IOException {
        TestPropertiesConfigurator.TEST_PROPERTIES_RESOURCE="/com/mango/mif/utils/first.properties";
        System.setProperty(TestPropertiesConfigurator.TEST_PROPERTIES_FILE_PATH_PROPERTY, FileUtils.toFile(TestPropertiesConfiguratorTest.class.getResource("/com/mango/mif/utils/second.properties")).getAbsolutePath());
        TestPropertiesConfigurator.initProperties();
        
        assertEquals("value-from-second-file",System.getProperty(OVERWRITTEN_PROPERTY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfSpecifiedPropertiesFileDoesNotExist() throws IOException {
        TestPropertiesConfigurator.TEST_PROPERTIES_RESOURCE="/com/mango/mif/utils/first.properties";
        System.setProperty(TestPropertiesConfigurator.TEST_PROPERTIES_FILE_PATH_PROPERTY, "/tmp/mock/location");
        TestPropertiesConfigurator.initProperties();
        
        assertEquals("value-from-second-file",System.getProperty(OVERWRITTEN_PROPERTY));
    }
}
