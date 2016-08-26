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
package com.mango.mif.client.domain;

import org.apache.commons.lang.StringUtils;

/**
 * These are names of variables placed in the Execution Request which are fixed between Navigator and MIF.
 */
public enum ExecutionRequestAttributeName {

    SHELL("SHELL", "/bin/bash", ExecutionRequestAttributeName.DO_NOT_INCLUDE_IN_REQUEST_ATTRIBUTE_BLOCK),

    MANGO_DEBUG("MANGO_LOGGING", ""),
    UMASK("UMASK", "007"),
    FILEACCEPTPATTERN("FILE_ACCEPT_PATTERN", ""),
    DIRACCEPTPATTERN("DIR_ACCEPT_PATTERN", ""),
    FILEIGNOREPATTERN("FILE_IGNORE_PATTERN", ""),
    DIRIGNOREPATTERN("DIR_IGNORE_PATTERN", ""),
    MANGO_SGE_QSTAT("MANGO_SGE_QSTAT", "qstat"),
    MANGO_SGE_QACCT("MANGO_SGE_QACCT", "qacct"),
    MANGO_SGE_QDEL("MANGO_SGE_QDEL", "qdel"),
    MANGO_SGE_QSUB("MANGO_SGE_QSUB", "qsub"),
    QSUB_PARAMETERS("QSUB_PARAMETERS", ""),
    USER_DIR_ADDITIONAL_MODES("USER_DIR_ADDITIONAL_MODES", ""),
    USER_SHELL_SCRIPT_EXTENSIONS("USER_SHELL_SCRIPT_EXTENSIONS", ".sh"),
    COPY_MODE("COPY_MODE", "all"),

    // The intention is that one day, we get rid of "GRID_SHELL" and replace it with "EXECUTION_HOST_SHELL" because it is
    // less SGE specific.
    //
    EXECUTION_HOST_SHELL("EXECUTION_HOST_SHELL", "/bin/bash", ExecutionRequestAttributeName.DO_NOT_INCLUDE_IN_REQUEST_ATTRIBUTE_BLOCK),
    EXECUTION_HOST_PREAMBLE("EXECUTION_HOST_PREAMBLE", "", ExecutionRequestAttributeName.DO_NOT_INCLUDE_IN_REQUEST_ATTRIBUTE_BLOCK),
    
    GRID_SHELL("GRID_SHELL", "/bin/bash", ExecutionRequestAttributeName.DO_NOT_INCLUDE_IN_REQUEST_ATTRIBUTE_BLOCK),
    
    EXECUTION_HOST_FILESHARE("EXECUTION_HOST_FILESHARE", ""),
    EXECUTION_HOST_FILESHARE_REMOTE("EXECUTION_HOST_FILESHARE_REMOTE", "");

    private static final boolean DO_NOT_INCLUDE_IN_REQUEST_ATTRIBUTE_BLOCK = false;
    private final String name;
    private final String defaultValue;

    private final boolean wantedInBlock;

    ExecutionRequestAttributeName(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.wantedInBlock = true;
    }

    ExecutionRequestAttributeName(String name, String defaultValue, boolean includeInBlock) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.wantedInBlock = includeInBlock;
    }

    public String getName() {
        return this.name;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public boolean hasDefaultValue() {
        return StringUtils.isNotBlank(this.defaultValue);
    }

    /**
     * Return whether the variable should be expanded into the request attribute block at the top of the script.
     * @return whether this is wanted in the script block or not
     */
    public boolean isWantedInBlock() {
        return this.wantedInBlock;
    }

    /**
     * Find an ExecutionRequestAttributeName corresponding to the specified name.
     * @param s the specified name
     * @return The corresponding ExecutionRequestAttributeName, or null if name is not found.
     */
    public static ExecutionRequestAttributeName find(String s) {
        for (ExecutionRequestAttributeName attr : ExecutionRequestAttributeName.values()) {
            if (attr.getName().equals(s)) {
                return attr;
            }
        }
        return null;
    }
   
    
}
