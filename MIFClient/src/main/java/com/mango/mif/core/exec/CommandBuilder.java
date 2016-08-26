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

package com.mango.mif.core.exec;

import com.google.common.base.Preconditions;
import com.mango.mif.core.exec.template.CommandBuilderContext;
/**
 *
 * Abstract command builder
 * 
 * FIXME should be an interface not abstract class - must be refactored together with the rest of Command Builder hierarchy.
 * 
 */
public abstract class CommandBuilder {

    private CommandBuilderContext context;
    
    /**
     * Builds a command
     * @return the built command
     * @throws ExecutionException if couldn't generate a command
     */
    public abstract String getCommand() throws ExecutionException;
    
    public CommandBuilderContext getContext() {
        return context;
    }
    
    /**
     * Command builder context
     * @param context
     */
    public void setContext(CommandBuilderContext context) {
        this.context = context;
    }

    /**
     * Add an object into the context.
     * 
     * {@link CommandBuilderContext}
     * 
     * @param name The name by which the object will be known
     * @param value The object itself
     */
    public void setVariable(String name, Object value) throws ExecutionException {
        Preconditions.checkNotNull(getContext(),"Context was not set on the command builder");
        Preconditions.checkNotNull(name,"Variable name was null");
        Preconditions.checkNotNull(value,"Variable value was null");
        getContext().setVariable(name, value);
    }

    /**
     * Retrieve a variable from a context
     * 
     * {@link CommandBuilderContext}
     * 
     * @param variableName The name by which the object may be known
     * @return The object itself
     */
    public Object getVariable(String key) {
        Preconditions.checkNotNull(getContext(),"Context was not set on the command builder");
        return getContext().getVariable(key);
    }

}
