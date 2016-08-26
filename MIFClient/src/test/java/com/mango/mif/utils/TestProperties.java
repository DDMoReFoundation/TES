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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A class holding common properties used in tests
 */
public class TestProperties {
    private final static Logger LOG = Logger.getLogger(TestProperties.class);
    /**
     * Other user name
     */
    public final static String MIF_OTHER_CLIENT_USER_NAME = "mif.other.client.userName";
    /**
     * Other user password
     */
    public final static String MIF_OTHER_CLIENT_USER_PASSWORD = "mif.other.client.password";
    /**
     * Port
     */
    public final static String MIF_JSCH_PORT = "mif.jsch.port";
    /**
     * Jsch host name
     */
    public final static String MIF_JSCH_HOST = "mif.jsch.host";
    /**
     * Common scripts location (relative to SHARED_RESOURCES_DIRECTORY)
     */
    public static final String MIF_COMMON_SCRIPTS_LOCATION = "mif.commonScriptsDirectory";
    /**
     * Generic scripts location (relative to SHARED_RESOURCES_DIRECTORY)
     */
    public static final String MIF_GENERIC_SCRIPTS_LOCATION = "mif.genericScriptsDirectory";
    /**
     * A root directory of common resource files for MIFServer and MIFClient
     */
    public static final String MIF_RUNTIME_DIRECTORY = "mif.runtime.dir";
    /**
     * templates directory location (relative to SHARED_RESOURCES_DIRECTORY)
     */
    public static final String TEMPLATES_DIRECTORY_LOCATION = "mif.templatesDirectory";
    /**
     * A path of the 'scripts/common' scripts directory relative to MIF_RUNTIME_DIRECTORY
     */
    public static final String MIF_COMMON_SCRIPTS_DIRECTORY_PATH = "mif.test.commonScriptsDirectoryPath";
    /**
     * A path of the 'scripts/generic' scripts directory relative to MIF_RUNTIME_DIRECTORY
     */
    public static final String MIF_GENERIC_SCRIPTS_DIRECTORY_PATH = "mif.test.genericScriptsDirectoryPath";
    /**
     * A path of the 'templates' directory relative to MIF_RUNTIME_DIRECTORY
     */
    public static final String MIF_TEMPLATES_DIRECTORY_PATH = "mif.test.templatesDirectoryPath";
    /**
     * 
     * @return names of the properties specified as static members of this class
     */
	public final static List<String> getPropertyNames() {
		final Field[] declaredFields = TestProperties.class.getDeclaredFields();
		final List<String> result = new ArrayList<String>();
		for (Field field : declaredFields) {
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)) {
				try {
                    result.add((String)field.get(null));
                } catch (IllegalArgumentException e) {
                    LOG.error(e);
                } catch (IllegalAccessException e) {
                    LOG.error(e);
                }
			}
		}
		return result;
	}
}
