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
package com.mango.mif.core.resource;

import java.io.File;
import java.util.List;

import com.mango.mif.exception.MIFException;

/**
 * 
 * Resource Publisher is responsible for a process of publishing files to the shared location
 * 
 */
public interface ResourcePublisher {

    /**
     * Publishes files to the shared location and returns a unique ID of the request directory
     * @return
     */
    String publish() throws MIFException;

    /**
     * Adds the file.
     *
     * @param file the file
     * @param root the root
     */
    void addFile(File file, File root);

    /**
     * Adds the file.
     *
     * @param file the file
     */
    void addFile(File file);

    /**
     * Adds the files.
     *
     * @param files the files
     */
    void addFiles(List<File> files);

    /**
     * Gets the request id.
     *
     * @return the request id
     */
    String getRequestID();

    /**
     * Sets the script preamble.
     *
     * @param submitHostPreamble the new script preamble
     */
    void setSubmitHostPreamble(String submitHostPreamble);

    /**
     * Sets the resource copier.
     *
     * @param resourceCopier the new resource copier
     */
    void setResourceCopier(ResourceCopier resourceCopier);

}
