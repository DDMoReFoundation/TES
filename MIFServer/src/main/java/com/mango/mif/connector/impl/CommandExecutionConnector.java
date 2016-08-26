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

package com.mango.mif.connector.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.Connector;

/**
 * An implementation of a Connector that provides the capability to execute a command.
 */
public abstract class CommandExecutionConnector implements Connector {

    private static final Logger LOGGER = Logger.getLogger(CommandExecutionConnector.class);

    private CommandExecutionTarget commandExecutionTarget;
    
    /**
     * @param target the {@link CommandExecutionTarget} for this Connector
     */
    @Required
    public final void setCommandExecutionTarget(final CommandExecutionTarget target) {
    	this.commandExecutionTarget = target;
    }
    
    /**
     * @return the {@link CommandExecutionTarget} of this Connector
     */
    public final CommandExecutionTarget getCommandExecutionTarget() {
    	return this.commandExecutionTarget;
    }
    
}
