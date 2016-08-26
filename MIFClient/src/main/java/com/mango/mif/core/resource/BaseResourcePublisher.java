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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.exec.CommandBuilder;
import com.mango.mif.utils.Pair;

/**
 * Basic implementation of the resource publisher.
 * 
 * TODO add the DESDecryptor and use it to decrypt the password
 * 
 */
public abstract class BaseResourcePublisher implements ResourcePublisher {
    /**
     * Files list
     */
    protected List<File> filesToPublish = new ArrayList<File>();
    /**
     * Files with their own root list.
     */
    protected List<Pair<File, File>> rootedFiles = new ArrayList<Pair<File, File>>();
    /**
     * Root directory relative to which files should be copied
     */
    protected File rootDirectory;
    /**
     * The command builder
     */
    protected CommandBuilder commandBuilder;
    /**
     * The request attributes
     */
    protected Map<String, String> requestAttributes;
    /**
     * The preamble for the grid host.
     */
    protected String gridHostPreamble;
    /**
     * The preamble for the script host.
     */
    protected String submitHostPreamble;
    
    private String requestID;

    @Override
    public String getRequestID() {
        Preconditions.checkNotNull(requestID, "Request ID is null, files have not yet been published");
        return requestID;
    }

    
    protected void setRequestID(String requestID) {
        this.requestID = requestID;
    }
    
    /**
     * @param files
     *            the files to set
     */
    @Override
    public void addFiles(List<File> files) {
        this.filesToPublish.addAll(files);
    }

    /**
     * @param filesToPublish
     *            the files to set
     */
    @Override
    public void addFile(File file) {
        this.filesToPublish.add(file);
    }

    /**
     * Add a file with its own root. So, you want to add the file
     * /usr/share/foo/bar/budgie/myfile1.txt but you want to remove the
     * /usr/share/foo/bar part and just get the file copied across to the target
     * directory as /target/budgie/myfile1.txt
     * 
     * @param file
     *            The file you want to add, say
     *            /usr/share/foo/bar/budgie/myfile1.txt
     * @param root
     *            the root you want to subtract when copying, say
     *            /usr/share/foo/bar
     */
    @Override
    public void addFile(File file, File root) {
        Pair<File, File> pair = Pair.of(file, root);
        rootedFiles.add(pair);
    }

    public CommandBuilder getCommandBuilder() {
        return commandBuilder;
    }

    public void setSubmitHostPreamble(String submitHostPreamble) {
        this.submitHostPreamble = submitHostPreamble;
    }

    public void setGridHostPreamble(String gridHostPreamble) {
        this.gridHostPreamble = gridHostPreamble;
    }

    @Override
    public void setResourceCopier(ResourceCopier resourceCopier) {
        throw new RuntimeException("Method Not Supported Exeception");
    }
    /**
     * Gets the request directory.
     *
     * @param uuid the uuid
     * @return the request directory
     */
    public File getRequestDirectory(String uuid) {
        return new File(requestAttributes.get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()), uuid);
    }
}
