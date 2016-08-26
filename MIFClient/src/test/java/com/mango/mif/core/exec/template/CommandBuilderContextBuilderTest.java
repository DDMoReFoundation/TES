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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.domain.ExecutionRequest;


/**
 * Tests {@link CommandBuilderContextBuilder}
 */
public class CommandBuilderContextBuilderTest {
    private static final Logger LOG = Logger.getLogger(CommandBuilderContextBuilderTest.class);
    private CommandBuilderContextBuilder commandBuilderContextBuilder;

    
    private static final String REQUEST_ATTRIBUTE_BLOCK = "REQUEST_ATTRIBUTE_BLOCK";
    
    @Before
    public void setUp() {
        this.commandBuilderContextBuilder = new CommandBuilderContextBuilder();
    }

    @Test
    public void shouldPopulateContextWithDefaultRequestAttributes() {
        CommandBuilderContextBuilder contextBuilder = new CommandBuilderContextBuilder();
        CommandBuilderContext context = contextBuilder.buildCommandBuilderContext();
        assertEquals(context.getVariable(ExecutionRequestAttributeName.MANGO_SGE_QSTAT.getName()),"qstat");
    }
    

    @Test
    public void shouldPopulateContextWithDefaultRequestAttributesAndAddValuesFromTheMap() {
        Map<String, Object> initialContext = Maps.newHashMap();
        initialContext.put("CUSTOM_ATTR", "CUSTOM_VALUE");
        CommandBuilderContextBuilder contextBuilder = new CommandBuilderContextBuilder(initialContext);
        CommandBuilderContext context = contextBuilder.buildCommandBuilderContext();
        assertEquals(context.getVariable(ExecutionRequestAttributeName.MANGO_SGE_QSTAT.getName()),"qstat");
        assertEquals(context.getVariable("CUSTOM_ATTR"),"CUSTOM_VALUE");
    }
    
    @Test
    public void shouldPopulateContextWithDefaultRequestAttributesAndAddValuesFromTheContext() {
        Map<String, Object> initialContext = Maps.newHashMap();
        initialContext.put("CUSTOM_ATTR", "CUSTOM_VALUE");
        CommandBuilderContext mockContext = mock(CommandBuilderContext.class);
        when(mockContext.getVariables()).thenReturn(initialContext);
        CommandBuilderContextBuilder contextBuilder = new CommandBuilderContextBuilder(mockContext);
        CommandBuilderContext context = contextBuilder.buildCommandBuilderContext();
        assertEquals(context.getVariable(ExecutionRequestAttributeName.MANGO_SGE_QSTAT.getName()),"qstat");
        assertEquals(context.getVariable("CUSTOM_ATTR"),"CUSTOM_VALUE");
    }
    
    @Test
    public void populateCommandBuilderContext_shouldPopulateContextWithPredefinedAndCustomRequestAttributes() {
        Map<String, String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "fileshare");
        requestAttributes.put("CUSTOM_ATTR", "CUSTOM_VALUE");
        commandBuilderContextBuilder.populateCommandBuilderContext(requestAttributes);
        
        CommandBuilderContext context = commandBuilderContextBuilder.buildCommandBuilderContext();
        
        assertEquals(context.getVariable("CUSTOM_ATTR"),"CUSTOM_VALUE");
        assertEquals(context.getVariable(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()),"fileshare");
        LOG.debug(String.format("%s being produced %s",REQUEST_ATTRIBUTE_BLOCK,context.getVariable(REQUEST_ATTRIBUTE_BLOCK)));
        assertTrue(String.format("Expected variable %s not found in the Request Attributes block",ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()),((String)context.getVariable(REQUEST_ATTRIBUTE_BLOCK)).contains(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()));
        assertTrue(String.format("Expected variable %s not found in the Request Attributes block","CUSTOM_ATTR"),((String)context.getVariable(REQUEST_ATTRIBUTE_BLOCK)).contains("CUSTOM_ATTR"));
    }
    
    @Test
    public void populateCommandBuilderContext_shouldIncludePredefinedRequestAttributesEvenIfTheyDoNOTAppearOnTheRequestAttributesMap() {
        Map<String, String> requestAttributes = Maps.newHashMap();
        requestAttributes.put("CUSTOM_ATTR", "CUSTOM_VALUE");
        commandBuilderContextBuilder.populateCommandBuilderContext(requestAttributes);
        CommandBuilderContext context = commandBuilderContextBuilder.buildCommandBuilderContext();
        
        assertEquals(context.getVariable("CUSTOM_ATTR"),"CUSTOM_VALUE");
        assertEquals(context.getVariable(ExecutionRequestAttributeName.MANGO_SGE_QSTAT.getName()),"qstat");
        assertEquals(context.getVariable(ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName()),ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getDefaultValue());
        LOG.debug(String.format("%s being produced %s",REQUEST_ATTRIBUTE_BLOCK,context.getVariable(REQUEST_ATTRIBUTE_BLOCK)));
        assertTrue(String.format("Expected variable %s not found in the Request Attributes block",ExecutionRequestAttributeName.MANGO_SGE_QSTAT.getName()),((String)context.getVariable(REQUEST_ATTRIBUTE_BLOCK)).contains(ExecutionRequestAttributeName.MANGO_SGE_QSTAT.getName()));
    }

    @Test
    public void populateCommandBuilderContext_shouldNotIncludePredefinedAttributeInRequestBlockIfItShouldnt() {
        Map<String, String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName(), "shell-path");
        commandBuilderContextBuilder.populateCommandBuilderContext(requestAttributes);
        CommandBuilderContext context = commandBuilderContextBuilder.buildCommandBuilderContext();
        
        assertEquals(context.getVariable(ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName()),"shell-path");
        LOG.debug(String.format("%s being produced %s",REQUEST_ATTRIBUTE_BLOCK,((String)context.getVariable(REQUEST_ATTRIBUTE_BLOCK))));
        assertFalse(String.format("Unexpected variable %s found in the Request Attributes block",ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName()),((String)context.getVariable(REQUEST_ATTRIBUTE_BLOCK)).contains(ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName()));
    }

    @Test
    public void populateCommandBuilderContext_shouldUseDefaultValueIfBlankValueForPredefinedAttributeWasProvided() {
        Map<String, String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName(), "");
        commandBuilderContextBuilder.populateCommandBuilderContext(requestAttributes);
        CommandBuilderContext context = commandBuilderContextBuilder.buildCommandBuilderContext();
        assertEquals(context.getVariable(ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName()),ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getDefaultValue());
    }

    @Test
    public void populateCommandBuilderContext_executionRequest_shouldPopulateContextWithRequestAttributes() {
        Map<String, String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "fileshare");
        requestAttributes.put("CUSTOM_ATTR", "CUSTOM_VALUE");
        ExecutionRequest executionRequest = mock(ExecutionRequest.class);
        when(executionRequest.getRequestAttributes()).thenReturn(requestAttributes);
        commandBuilderContextBuilder.populateCommandBuilderContext(executionRequest);
        
        CommandBuilderContext context = commandBuilderContextBuilder.buildCommandBuilderContext();
        
        assertEquals(context.getVariable("CUSTOM_ATTR"),"CUSTOM_VALUE");
        assertEquals(context.getVariable(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()),"fileshare");
        assertEquals(context.getVariable(ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getName()),ExecutionRequestAttributeName.EXECUTION_HOST_SHELL.getDefaultValue());
        LOG.debug(String.format("%s being produced %s",REQUEST_ATTRIBUTE_BLOCK,context.getVariable(REQUEST_ATTRIBUTE_BLOCK)));
    }
    
    @Test
    public void populateCommandBuilderContext_executionRequest_shouldPopulateContextWithGridHostPreambleFromExecutionRequest() {
        Map<String, String> requestAttributes = Maps.newHashMap();
        ExecutionRequest executionRequest = mock(ExecutionRequest.class);
        when(executionRequest.getRequestAttributes()).thenReturn(requestAttributes);
        when(executionRequest.getGridHostPreamble()).thenReturn("mock-preamble");
        commandBuilderContextBuilder.populateCommandBuilderContext(executionRequest);
        
        CommandBuilderContext context = commandBuilderContextBuilder.buildCommandBuilderContext();
        assertEquals(context.getVariable(CommandBuilderContext.CONTEXT_NAME_GRID_HOST_PREAMBLE),"mock-preamble");
        assertEquals(context.getVariable(ExecutionRequestAttributeName.EXECUTION_HOST_PREAMBLE.getName()),"mock-preamble");
    }

    @Test
    public void populateCommandBuilderContext_executionRequest_shouldPopulateContextWithSubmitHostPreambleFromExecutionRequest() {
        Map<String, String> requestAttributes = Maps.newHashMap();
        ExecutionRequest executionRequest = mock(ExecutionRequest.class);
        when(executionRequest.getRequestAttributes()).thenReturn(requestAttributes);
        when(executionRequest.getSubmitHostPreamble()).thenReturn("mock-preamble");
        commandBuilderContextBuilder.populateCommandBuilderContext(executionRequest);
        
        CommandBuilderContext context = commandBuilderContextBuilder.buildCommandBuilderContext();
        assertEquals(context.getVariable(CommandBuilderContext.CONTEXT_NAME_SUBMIT_HOST_PREAMBLE),"mock-preamble");
    }
}
