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

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test stub that ensures that the test does not modify System properties and impacts other tests
 */
public class SystemPropertiesPreservingTestStub {

    private static final Properties initialProperties = new Properties();

    @BeforeClass
    public static void backupSystemProperties() {
        initialProperties.putAll(System.getProperties());
    }

    @AfterClass
    public static void revertSystemProperties() throws IOException {
        Properties p = new Properties();
        p.putAll(initialProperties);
        System.setProperties(p);
    }

}