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

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

/**
 * Commonly used properties
 * 
 * TODO this class existence should be exercised - it has been introduced to address technical debt around 
 * too broad class responsibilities of Job class. it is being called in static way but it must be initialised what impacts application lifecycle!
 * 
 * The properties are only used in 'main' source just by Job class. The major usage is in Tests to initialize it.
 * 
 * Refactor the class to 
 */
public class MIFProperties {
	/**
     * MIF hidden directory in the request directory
     */
    public static String MIF_HIDDEN_DIRECTORY_NAME = ".MIF";
    /**
     * name of a file holding the STD error stream
     */
    public static String MIF_TPT_STD_ERR_FILE_NAME = "MIF.stderr";
    /**
     * name of a file holding the STD out file name
     */
    public static String MIF_TPT_STD_OUT_FILE_NAME = "MIF.stdout";
    /**
     * File holding a list of modified files by a Third Party Tool
     */
    public static String MIF_TPT_MODIFIED_LIST_FILE_NAME = "MIF.list";
    /**
     * File holding a list of modified files by during a job execution
     */
    public static String MIF_MODIFIED_LIST_FILE_NAME = "MIF.modified.file.list";
    /**
     * Singleton
     */
    private static MIFProperties instance;
    /**
     * Properties
     */
    private Properties properties;

    /**
     * Constructor
     * @param properties
     */
    private MIFProperties(Properties properties) {
        this.properties = properties;
        load();
    }

    /**
     * loads properties
     * @param properties
     * @return
     */
    public static synchronized MIFProperties loadProperties(Properties properties) {
        if (instance == null) {
            instance = new MIFProperties(properties);
        }
        return instance;
    }

    /**
     * singleton instance
     */
    public static synchronized MIFProperties getInstance() {
        return loadProperties(null);
    }

    void load() {
        Preconditions.checkNotNull(properties, "Properties source not set");

        String prop = getProperty("mif.hiddenDirectoryName");
        if (StringUtils.isNotBlank(prop)) {
            MIF_HIDDEN_DIRECTORY_NAME = prop;
        }

        prop = getProperty("connector.stdErrFileName");
        if (StringUtils.isNotBlank(prop)) {
            MIF_TPT_STD_ERR_FILE_NAME = prop;
        }

        prop = getProperty("connector.stdOutFileName");
        if (StringUtils.isNotBlank(prop)) {
            MIF_TPT_STD_OUT_FILE_NAME = prop;
        }
        
        prop = getProperty("connector.tptModifiedListFileName");
        if (StringUtils.isNotBlank(prop)) {
            MIF_TPT_MODIFIED_LIST_FILE_NAME = prop;
        }
        
        prop = getProperty("mif.modifiedListFileName");
        if (StringUtils.isNotBlank(prop)) {
            MIF_MODIFIED_LIST_FILE_NAME = prop;
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Properties getProperties() {
        return properties;
    }
}
