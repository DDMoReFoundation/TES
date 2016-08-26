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

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;


/**
 * Tests {@link CommandBuilderContextImpl}
 */
public class CommandBuilderContextImplTest {

    @Test(expected=NullPointerException.class)
    public void setVariable_shouldThrowExceptionIfNameIsNull() {
        new CommandBuilderContextImpl().setVariable(null, "value");
    }

    @Test(expected=NullPointerException.class)
    public void setVariable_shouldThrowExceptionIfValueIsNull() {
        new CommandBuilderContextImpl().setVariable("name", null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setVariable_shouldThrowExceptionIfNameIsBlank() {
        new CommandBuilderContextImpl().setVariable(" ", "value");
    }
    
    @Test
    public void setVariable_shouldSetVariable() {
        new CommandBuilderContextImpl().setVariable("name", "value");
    }
    
    @Test
    public void setVariables_shouldSetAllVariables() {
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("name1","value1");
        variables.put("name2", "value2");
        CommandBuilderContextImpl context = new CommandBuilderContextImpl();
        context.setVariables(variables);
        assertEquals("value1",context.getVariable("name1"));
        assertEquals("value2",context.getVariable("name2"));
    }

    @Test
    public void getVariable2_shouldGetAllVariables() {
        CommandBuilderContextImpl context = new CommandBuilderContextImpl();
        context.setVariable("name1", "value1");
        context.setVariable("name2", "value2");
        Map<String,Object> variables = context.getVariables();
        assertEquals("value1",variables.get("name1"));
        assertEquals("value2",variables.get("name2"));
    }

    @Test
    public void getVariable_shouldGetVariable() {
        CommandBuilderContextImpl context = new CommandBuilderContextImpl();
        context.setVariable("name", "value");
        assertEquals("value",context.getVariable("name"));
    }
    
    @Test(expected=NullPointerException.class)
    public void getVariable_shouldThrowExceptionIfNameIsNull() {
        new CommandBuilderContextImpl().getVariable(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void getVariable_shouldThrowExceptionIfNameIsBlank() {
        new CommandBuilderContextImpl().getVariable(" ");
    }
    

    @Test(expected=IllegalArgumentException.class)
    public void getVariable_shouldThrowExceptionIfVariableWithAGivenNameDoesNotExist() {
        new CommandBuilderContextImpl().getVariable("non-existing-variable");
    }
    

    @Test(expected=NullPointerException.class)
    public void containsVariable_shouldThrowExceptionIfNameIsNull() {
        new CommandBuilderContextImpl().getVariable(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void containsVariable_shouldThrowExceptionIfNameIsBlank() {
        new CommandBuilderContextImpl().getVariable(" ");
    }
    

    @Test
    public void containsVariable_shouldReturnFalseIfVariableWithAGivenNameDoesNotExist() {
        assertFalse(new CommandBuilderContextImpl().containsVariable("non-existing-variable"));
    }
}
