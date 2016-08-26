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
package com.mango.mif.core.exec.template;

import java.util.Map;


/**
 * classes implementing this interface hold execution context of CommandBuilder
 */
public interface CommandBuilderContext {
    /**
     * The expected name for the script preamble instance when placed in the context.
     */
    static final String CONTEXT_NAME_SUBMIT_HOST_PREAMBLE = "SUBMIT_HOST_PREAMBLE";

    /**
     * FIXME all execution types should use ExecutionRequestAttributeName.EXECUTION_HOST_PREAMBLE.getName()
     * Removing this requires updates to ExecutionRequest entity
     * 
     * The expected name for the script preamble instance when placed in the context.
     */
    static final String CONTEXT_NAME_GRID_HOST_PREAMBLE = "GRID_HOST_PREAMBLE";

    /**
     * The expected name for all the variables defined in the execution request attributes
     * that haven't been used elsewhere (SHELL, GRID_SHELL, etc. etc.).
     */
    static final String CONTEXT_NAME_REQUEST_ATTRIBUTE_BLOCK = "REQUEST_ATTRIBUTE_BLOCK";
    /**
     * Registers a variable against a given name
     * @param name
     * @param value
     * @throws NullPointerException if name or value are null
     * @throws IllegalArgumentException if name is blank
     */
    void setVariable(String name, Object value);

    /**
     * @param name The name by which the object may be known
     * @return the object registered in the context
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if variable for given name does not exist
     */
    Object getVariable(String name);
    
    /**
     * Sets a bulk of variables
     * @param variables
     * @throws NullPointerException if variables is null
     */
    void setVariables(Map<String,?> variables);
    
    /**
     * @return variables map
     */
    Map<String, Object> getVariables();
    
    /**
     * checks if the variable is registered in the context under given name
     * @param name
     * @return true if variable exists in the context
     */
    boolean containsVariable(String name);
}
