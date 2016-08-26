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

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.domain.ExecutionRequest;


/**
 * A Builder that contains shorthand methods for populating command builder context.
 * This class (and extending classes) is preferable way for updating command builder contexts.
 * 
 * This class ensures that the context is correctly initialized.
 * 
 * Any further refactoring can be performed when:
 * * submitHostPreamble and gridHostPreamble attributes are moved to Attributes Map of the ExecutionRequest
 * * when EXECUTION_HOST* variables are used instead of GRID_HOST* variables in templates
 */
public class CommandBuilderContextBuilder {
    
    private CommandBuilderContext context;
    
    /**
     * Creates a builder with a default initial context
     */
    public CommandBuilderContextBuilder() {
        context = new CommandBuilderContextImpl();
        init();
    }
    
    /**
     * Creates a builder with a given initial context
     */
    public CommandBuilderContextBuilder(Map<String,Object> initialContext) {
        this();
        setVariables(initialContext);
    }

    /**
     * Creates a builder with a given initial context
     */
    public CommandBuilderContextBuilder(CommandBuilderContext initialContext) {
        this();
        setVariables(initialContext.getVariables());
    }
    
    private void init() {

        for (ExecutionRequestAttributeName attr : ExecutionRequestAttributeName.values()) {
            String name = attr.getName();
            String value = attr.getDefaultValue();

            context.setVariable(name, value);
        }
        
        // This is just for the test harnesses really...
        // FIXME this can be removed once gridHostPreamble and submitHostPreamble are moved to request attributes
        setGridHostPreamble("");
        setSubmitHostPreamble("");
    }
    
    public static CommandBuilderContext createInitContext() {
        return new CommandBuilderContextBuilder().buildCommandBuilderContext();
    }
    
    /**
     * Add the submit host preamble as a variable to the context.  This encourages all to use the suggested context name.
     * @param preamble The preamble.
     * @return this builder
     */
    public CommandBuilderContextBuilder setSubmitHostPreamble(String preamble) {
        context.setVariable(CommandBuilderContext.CONTEXT_NAME_SUBMIT_HOST_PREAMBLE, preamble);
        return this;
    }

    /**
     * Add the grid host preamble as a variable to the context.  This encourages all to use the suggested context name.
     * Note that because I'm trying to move away from "grid host" preamble (for environments which don't have grids, but
     * still need preambles) this function will also set the execution host preamble.
     *
     * @param preamble The preamble.
     * @return this builder
     */
    public CommandBuilderContextBuilder setGridHostPreamble(String preamble) {
        context.setVariable(CommandBuilderContext.CONTEXT_NAME_GRID_HOST_PREAMBLE, preamble);
        context.setVariable(ExecutionRequestAttributeName.EXECUTION_HOST_PREAMBLE.getName(), preamble);
        return this;
    }

    /**
     * Merges the passed attributes with the predefined request attributes in ExecutionRequestAttributeName.
     * Sets the variables from the merged map on this object.
     * 
     * @param requestAttributes The request attributes
     * @return this builder
     */
    public CommandBuilderContextBuilder populateCommandBuilderContext(Map<String, String> requestAttributes) {
        Map<String, String> mergedVariables = mergeAttributes(requestAttributes);
        context.setVariables(mergedVariables);
        context.setVariable(CommandBuilderContext.CONTEXT_NAME_REQUEST_ATTRIBUTE_BLOCK, buildRequestAttributesBlock(mergedVariables));
        return this;
    }
    /**
     * Uses the execution request to populate the context with request attributes, submitHostPreamble (if not null) and gridHostPreamble (if not null)
     * 
     * @param executionRequest the execution request
     * @return this builder
     */
    public CommandBuilderContextBuilder populateCommandBuilderContext(ExecutionRequest executionRequest) {
        populateCommandBuilderContext(executionRequest.getRequestAttributes());
        if(executionRequest.getSubmitHostPreamble()!=null) {
            this.setSubmitHostPreamble(executionRequest.getSubmitHostPreamble());
        }
        if(executionRequest.getGridHostPreamble()!=null) {
            this.setGridHostPreamble(executionRequest.getGridHostPreamble());
        }
        return this;
    }
    /**
     * Merges the predefined attributes with the attributes passed in the map
     * 
     * @param requestAttributes
     * @return a map holding all predefined attributes overriden by values specified in the requestAttributes map and extended with the custom attributes from requestAttributes 
     */
    private static Map<String, String> mergeAttributes(Map<String, String> requestAttributes) {
        Map<String,String> mergedVariables = Maps.newHashMap();
        for (ExecutionRequestAttributeName attr : ExecutionRequestAttributeName.values()) {
            String name = attr.getName();
            if (!requestAttributes.containsKey(name)) {
                if(attr.hasDefaultValue()) {
                    mergedVariables.put(name, attr.getDefaultValue());
                }
            } else {
                String value = requestAttributes.get(name);
                if (StringUtils.isBlank(value)) {
                    if(attr.hasDefaultValue()) {
                        value = attr.getDefaultValue();
                    }
                }
                mergedVariables.put(name, value);
            }
        }
        //process not predefined attributes
        for (Map.Entry<String, String> entry : requestAttributes.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if(!mergedVariables.containsKey(name)) {
                mergedVariables.put(name, value);
            }
        }
        return mergedVariables;
    }

    /**
     * Builds bash variables declaration block.
     * WARNING - elements in the requestAttributes that are members of this enum for which 'wantedInBlock' is false will NOT be included in the result
     * 
     * @param requestAttributes variables that should be considered
     * @return a contents of a variables block
     */
    private static String buildRequestAttributesBlock(Map<String,String> requestAttributes) {
        StringBuilder block = new StringBuilder();
        for (Map.Entry<String, String> entry : requestAttributes.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            ExecutionRequestAttributeName attr = ExecutionRequestAttributeName.find(name);

            if (attr != null) {
                if(attr.isWantedInBlock()) {
                    block.append(createVariableExport(name,value));
                }
            } else {
                block.append(createVariableExport(name,value));
            }
        }
        return block.toString();
    }
    /**
     * Generate Bourne Shell syntax for exporting variables
     *       VAR="value"; export VAR
     *
     * @param name
     * @param value
     * @return formatted block
     */
    private static String createVariableExport(String name, String value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Name of the script variable cant be blank");
        Preconditions.checkNotNull(value, "Value of the script variable cant be null");
        StringBuilder block = new StringBuilder();
        block.append(name);
        block.append("=\"");
        block.append(value);
        block.append("\"; export ");
        block.append(name);
        block.append("\n");
        return block.toString();
    }
    
    /**
     * Builds command builder context.
     * 
     * @return context instance
     */
    public CommandBuilderContext buildCommandBuilderContext() {
        Map<String,Object> variables = context.getVariables();
        CommandBuilderContext result = new CommandBuilderContextImpl();
        result.setVariables(variables);
        return result;
    }

    /**
     * Registers a variable with a given name and value
     * @param name of the variable
     * @param value of the vaiable
     * @return this builder
     */
    public CommandBuilderContextBuilder setVariable(String name, Object value) {
        this.context.setVariable(name, value);
        return this;
    }
    

    /**
     * Sets a bulk of variables on the context.
     * @param variables that should be set
     * @return this builder
     */
    public CommandBuilderContextBuilder setVariables(Map<String,Object> variables) {
        Preconditions.checkNotNull(variables,"A map with variables was null");
        for(Map.Entry<String, Object> en : variables.entrySet()) {
            this.context.setVariable(en.getKey(), en.getValue());
        }
        return this;
    }
}
