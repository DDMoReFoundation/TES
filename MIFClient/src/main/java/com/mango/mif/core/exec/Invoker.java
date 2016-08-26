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


/**
 * Invoker used to execute commands
 */
public interface Invoker {
    
    /**
     * Executes given command.
     * 
     * @param command the command, plus any arguments (may be multi-line)
     * @return the results of the command execution (stdout, stderr, exit status)
     */
    InvokerResult execute(String command) throws ExecutionException;

    /**
     * Executes given command with the standard input specified
     * 
     * @param command the command, plus any arguments (may be multi-line)
     * @param input text to shove up the command's standard input
     * @return the results of the command execution (stdout, stderr, exit status)
     */
    InvokerResult execute(String command, String input) throws ExecutionException;
}
