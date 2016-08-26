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


/**
 * Default implementation of {@link CommandBuilderContext}
 */
public class CommandBuilderContextImpl implements CommandBuilderContext {
    
    private Map<String,Object> context = Maps.newHashMap();
    
    @Override
    public void setVariables(Map<String,?> variables) {
        Preconditions.checkNotNull(variables, "Null variables map was passed");
        for (Map.Entry<String, ?> entry : variables.entrySet()) {
            setVariable(entry.getKey(), entry.getValue());
        }
    }
    @Override
    public void setVariable(String name, Object value) {
        Preconditions.checkNotNull(name,"Variable name was null");
        Preconditions.checkArgument(StringUtils.isNotBlank(name),"Variable name was blank");
        Preconditions.checkNotNull(value,String.format("Value of the variable %s was null",name));
        context.put(name, value);
    }
    
    @Override
    public Object getVariable(String name) {
        Preconditions.checkArgument(containsVariable(name),String.format("Variable %s does not exist",name));
        return context.get(name);
    }
    
    @Override
    public boolean containsVariable(String name) {
        Preconditions.checkNotNull(name,"Variable name was null");
        Preconditions.checkArgument(StringUtils.isNotBlank(name),"Variable name was blank");
        return context.containsKey(name);
    }
    
    @Override
    public Map<String, Object> getVariables() {
        return Maps.newHashMap(context);
    }
}
