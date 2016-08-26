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
package com.mango.mif.core.resource.shell;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.mango.mif.core.exec.CommandBuilder;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.resource.ResourceCopier;

/**
 * The Class ShellBasedResourcePublisherParameters.
 * his acts as a Object that is passed for the creation of the ShellBasedResourcePublisher.
 */
public class PublisherParameters {

    /** The invoker. */
    Invoker invoker;

    /** The resource copier. */
    ResourceCopier resourceCopier;

    /**
     * The script preamble.
     */
    private String submitHostPreamble;

    /**
     * The grid preamble.
     */
    private String gridHostPreamble;

    /**
     * Root directory relative to which files should be copied
     */
    protected File rootDirectory;

    /**
     * The command builder
     */
    protected CommandBuilder commandBuilder;

    /**
     * The request attributes which basically come across in the ExecutionProfile as a list of ExecutionRequestAttribute
     * objects (basically name/value pairs).
     */
    private Map<String, String> requestAttributes;

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public CommandBuilder getCommandBuilder() {
        return commandBuilder;
    }

    public void setCommandBuilder(CommandBuilder commandBuilder) {
        this.commandBuilder = commandBuilder;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public ResourceCopier getResourceCopier() {
        return resourceCopier;
    }

    public void setResourceCopier(ResourceCopier resourceCopier) {
        this.resourceCopier = resourceCopier;
    }

    public String getSubmitHostPreamble() {
        return submitHostPreamble;
    }

    public void setSubmitHostPreamble(String submitHostPreamble) {
        this.submitHostPreamble = submitHostPreamble;
    }

    public String getGridHostPreamble() {
        return gridHostPreamble;
    }

    public void setGridHostPreamble(String gridHostPreamble) {
        this.gridHostPreamble = gridHostPreamble;
    }

    public Map<String, String> getRequestAttributes() {
        return requestAttributes;
    }

    public void setRequestAttributes(Map<String, String> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    public void addRequestAttribute(String name, String value) {
        if (requestAttributes == null) {
            requestAttributes = new HashMap<String, String>();
        }
        requestAttributes.put(name, value);
    }
}
