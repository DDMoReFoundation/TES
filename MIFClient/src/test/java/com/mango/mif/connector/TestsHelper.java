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
package com.mango.mif.connector;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * 
 * Methods used in tests
 */
public class TestsHelper {
    /**
     * Logger
     */
    public final static Logger LOG = Logger.getLogger(TestsHelper.class);

    public static final String MINIMUM_EXECUTION_REQUEST_MSG = "";

    /**
     * Creates working directory
     * @return
     */
    public static File createTmpDirectory() {
        File dir = Files.createTempDir();
        dir.deleteOnExit();
        return dir;
    }

    /**
     * 
     * @return
     */
    public static File createTmpDirectory(String name) {
        Preconditions.checkNotNull(name);
        String tmpDirName = System.getProperty("java.io.tmpdir");
        Preconditions.checkNotNull(tmpDirName);
        File tmpDir = new File(tmpDirName);
        File result = new File(tmpDir,name);
        LOG.info("Created temporary directory:" + result);
        result.mkdirs();
        result.deleteOnExit();
        return result;
    }

    /**
     * @return the name of a temporary directory which is not yet created
     */
    public static File tmpDirectoryPath(String name) {
        Preconditions.checkNotNull(name);
        String tmpDirName = System.getProperty("java.io.tmpdir");
        Preconditions.checkNotNull(tmpDirName);
        File tmpDir = new File(tmpDirName);
        File result = new File(tmpDir,name);
        LOG.info("Temporary directory path:" + result);
        return result;
    }

    /**
     * @param append Any suitable string which will be appended onto the directory name
     * @return A suitable name for a temporary directory, which is unlikely to (but nevertheless may) exist.
     */
    public static String generateTempDirectoryPath(String append) {
        String tmpDirName = System.getProperty("java.io.tmpdir");
        Preconditions.checkNotNull(tmpDirName);
        File temp = new File(tmpDirName, "" + System.currentTimeMillis() + (append == null ? "" : append));
        return temp.getAbsolutePath();
    }

    /**
     * Creates a Properties object from input stream. Ensures that the input stream is closed.
     * @param inStream
     * @return properties
     * @throws IOException
     */
    public final static Properties createPropertiesFromStream(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(inStream);
        } finally {
            IOUtils.closeQuietly(inStream);
        }
        return properties;
    }
}
