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

import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.exception.MIFException;

/**
 * Interface Responsible for copying Resources from Source Directory to the Destination Directory.
 * This should also support list of files and the folder to be ignored during copy.
 *  
 */
public interface ResourceCopier {

    /**
     * Set the job id for logging purposes.
     * @param id The job id.
     */
    void setJobId(String id) throws ExecutionException;

    /**
     * Copy the files from source directory to Destination.
     * If the List of files is provided then the copying process would 
     * ONLY copy the files that are been provided , from the source to the destination Directory.
     *
     * @param filesList the files list :
     * @throws MIFException the mIF exception
     */
    void copy(List<File> filesList) throws ExecutionException;
}
