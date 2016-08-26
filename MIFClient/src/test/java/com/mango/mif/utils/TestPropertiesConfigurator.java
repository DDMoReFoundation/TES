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

import static com.mango.mif.utils.TestProperties.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mango.mif.connector.TestsHelper;

/**
 * Component responsible for loading and preparing properties used by tests
 * 
 */
public class TestPropertiesConfigurator {
    /**
     * System property specifying a path to a file holding test properties
     */
    public final static String TEST_PROPERTIES_FILE_PATH_PROPERTY = "mif.build.properties.file.location";
    
    static String TEST_PROPERTIES_RESOURCE = "/tests.properties";
    
    private final static Logger LOG = Logger.getLogger(TestPropertiesConfigurator.class);
    
    /**
     * Initiates System properties based on the system property TEST_PROPERTIES_FILE_PATH_PROPERTY and TEST_PROPERTIES_RESOURCE.
     * 
     * The processing is as follows:
     * 1. If TEST_PROPERTIES_FILE_PATH_PROPERTY has been specified, these are loaded and merged with System properties (Existing System properties are not overwritten)
     * 1.a If TEST_PROPERTIES_FILE_PATH_PROPERTY points to an invalid resource IllegalArgumentException is thrown
     * 2. If TEST_PROPERTIES_FILE_PATH_PROPERTY has been specified in TEST_PROPERTIES_RESOURCE these are loaded
     * 2.a If TEST_PROPERTIES_FILE_PATH_PROPERTY points to an invalid resource IllegalArgumentException is thrown
     * 3. Properties from TEST_PROPERTIES_RESOURCE are merged with System properties (Existing System properties are not overwritten)
     * 4. Scripts and templates directories locations are resolved if they have not been set explicitly 
     * 
     * @param defaultProperties - default properties they will be overridden by properties from external sources
     * 
     * @throws IOException - if there was a problem reading in properties from TEST_PROPERTIES_FILE_PATH_PROPERTY or TEST_PROPERTIES_RESOURCE
     * @throws IllegalArgumentException - If location specified by TEST_PROPERTIES_FILE_PATH_PROPERTY can't be read
     */
    public final static void initProperties(Properties defaultProperties) throws IOException {
        String propertiesFilePath = System.getProperty(TEST_PROPERTIES_FILE_PATH_PROPERTY);

        final Properties defaultTestProperties = TestsHelper.createPropertiesFromStream(TestPropertiesConfigurator.class.getResourceAsStream(TEST_PROPERTIES_RESOURCE));
        
        
        if(StringUtils.isBlank(propertiesFilePath)) {
            LOG.warn(String.format("System Property %s not set", TEST_PROPERTIES_FILE_PATH_PROPERTY));
            propertiesFilePath = defaultTestProperties.getProperty(TEST_PROPERTIES_FILE_PATH_PROPERTY);
            if(StringUtils.isBlank(propertiesFilePath)) {
                LOG.warn(String.format("Property %s not set, just default properties from %s will be used.", TEST_PROPERTIES_FILE_PATH_PROPERTY,TEST_PROPERTIES_RESOURCE));
            } else {
                LOG.warn(String.format("System Property %s was set in %s. That is development environment.", TEST_PROPERTIES_FILE_PATH_PROPERTY,TEST_PROPERTIES_RESOURCE));
                loadPropertiesFromExternalFile(propertiesFilePath);
            }
        } else {
            loadPropertiesFromExternalFile(propertiesFilePath);
        }
        LOG.debug(String.format("Loading properties from: %s.", TEST_PROPERTIES_RESOURCE));
        
        loadProperties(defaultTestProperties);

        loadProperties(defaultProperties);
        
        resolveProperties();
        
        printProperties();
    }
    
    /**
     * See initProperties
     */
    public final static void initProperties() throws IOException {
        initProperties(new Properties());
    }
    /**
     * Loads properties from a given path
     * @param propertiesFilePath
     * @throws MalformedURLException
     * @throws IOException
     */
    private final static void loadPropertiesFromExternalFile(String propertiesFilePath) throws MalformedURLException, IOException {
        LOG.debug(String.format("Loading properties from: %s.", propertiesFilePath));
        final File propertiesFile = new File(propertiesFilePath);
        final boolean exists = propertiesFile.exists();
        final boolean canRead = propertiesFile.canRead();
        if(!exists|| !canRead) {
            // that is a fatal error, someone specified invalid file
            throw new IllegalArgumentException(String.format("File %s is invalid: exists [%b], can read [%b].",propertiesFilePath,exists, canRead));
        }
        
        loadProperties(TestsHelper.createPropertiesFromStream(new FileInputStream(propertiesFile)));
    }
    /**
     * Loads properties from the given input stream and merges them with the system properties
     * @param inStream
     */
    private final static void loadProperties(Properties properties ) {
        for(Entry<Object,Object> en: properties.entrySet()) {
            String key = (String)en.getKey();
            String value = (String)en.getValue();
            if(StringUtils.isBlank(System.getProperty(key))) {
                System.setProperty(key,value);
            }
        }
    }

    private final static void printProperties() {
        LOG.debug(StringUtils.repeat("#", 80));
        LOG.debug("System Properties");
        for(String property : TestProperties.getPropertyNames()){
            LOG.debug(String.format("Property: %s %s",  property, System.getProperty(property)));
        }
        LOG.debug(StringUtils.repeat("#", 80));
    }

    private final static void resolveProperties() {
        final String runtimeDirectory = System.getProperty(MIF_RUNTIME_DIRECTORY);
        
        
        if(StringUtils.isBlank(System.getProperty(MIF_COMMON_SCRIPTS_LOCATION))) {
            System.setProperty(MIF_COMMON_SCRIPTS_LOCATION,new File(runtimeDirectory,System.getProperty(MIF_COMMON_SCRIPTS_DIRECTORY_PATH)).getAbsolutePath());
        }

        if(StringUtils.isBlank(System.getProperty(MIF_GENERIC_SCRIPTS_LOCATION))) {
            System.setProperty(MIF_GENERIC_SCRIPTS_LOCATION,new File(runtimeDirectory,System.getProperty(MIF_GENERIC_SCRIPTS_DIRECTORY_PATH)).getAbsolutePath());
        }

        if(StringUtils.isBlank(System.getProperty(TEMPLATES_DIRECTORY_LOCATION))) {
            System.setProperty(TEMPLATES_DIRECTORY_LOCATION,new File(runtimeDirectory,System.getProperty(MIF_TEMPLATES_DIRECTORY_PATH)).getAbsolutePath());
        }
    }
}
