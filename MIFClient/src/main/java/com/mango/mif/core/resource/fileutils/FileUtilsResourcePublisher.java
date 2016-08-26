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
package com.mango.mif.core.resource.fileutils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.api.ResourceUtils;
import com.mango.mif.core.resource.BaseResourcePublisher;
import com.mango.mif.core.resource.shell.PublisherParameters;

/**
 * Resource publisher implementation that uses FileUtils for copying files
 */
public class FileUtilsResourcePublisher extends BaseResourcePublisher {
    /**
     * Logger
     */
    public final static Logger LOG = Logger.getLogger(FileUtilsResourcePublisher.class);
    /**
     * Number of times a resource component will re-try to generate request directory GUID
     */
    private final static int	TRESHOLD	= 10;

    
    /**
     * Creates an instance using the publisher parameters
     */
    public FileUtilsResourcePublisher(PublisherParameters publisherParameters) {
        Preconditions.checkNotNull(publisherParameters, "Parameters must not be null");
        Preconditions.checkArgument(publisherParameters.getRootDirectory()!=null, "Root Directory must be set");
        Preconditions.checkArgument(publisherParameters.getRequestAttributes()!=null, "Request attributes must be set");
        Preconditions.checkArgument(publisherParameters.getRequestAttributes().containsKey(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()), String.format("%s request attribute is empty",ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()));

        this.rootDirectory = publisherParameters.getRootDirectory();
        this.requestAttributes = publisherParameters.getRequestAttributes();
    }
    
    
    @Override
    public String publish() {
        Preconditions.checkNotNull(rootDirectory,"Root directory can't be null");
        String requestID = createRequestDirectory();
        for(File file : filesToPublish) {
            File destFile = getRequestDestinationFile(file,requestID);
            try {
                ResourceUtils.copyFile(file,destFile);
                destFile.setExecutable(file.canExecute());
            } catch (IOException e) {
                LOG.error(e);
                throw new RuntimeException("Could not copy file " + file + " to " + destFile,e);
            }
        }
        return requestID;
    }

    /**
     * 
     * @param file
     * @param requestID
     * @return a file path of the file on the shared location
     */
    protected File getRequestDestinationFile(File file, String requestID) {
        String path = file.getAbsolutePath().replace(rootDirectory.getAbsolutePath()+File.separator, "");
        return new File(getRequestDirectory(requestID),path);
    }

    /**
     * Generates a unique identifier and creates a directory named with that unique ID
     * @return a request directory
     */
    public String createRequestDirectory() {
        UUID uuid = null;
        boolean done = false;
        int i = 0;
        while (!done) {
            uuid = UUID.randomUUID();
            File dir = getRequestDirectory(uuid.toString());

            LOG.debug(String.format("Request directory %s",dir));
            if (dir.exists()) {
                LOG.error(String.format("Request directory %s already exists",dir));
            } else if (createRequestDirectory(dir)) {
                done = true;
                break;
            }
            if (i >= TRESHOLD) {
                // should never happen
                throw new RuntimeException("Could not generate unique ID for request directory.");
            }
            i++;
        }
        return uuid.toString();
    }

    /**
     * Creates request directories accordingly to the convention
     * @param dir
     */
    boolean createRequestDirectory(File dir) {
        Preconditions.checkNotNull(dir);
        if (!dir.mkdir()) {
            LOG.error(String.format("Could not create %s directory",dir));
            return false;
        }
        LOG.info("Created request directory: " + dir);
        return true;
    }

    
}
