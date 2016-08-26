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
package com.mango.mif.connector.runner.impl.statehandler;

import java.util.concurrent.Callable;

import com.mango.mif.connector.runner.impl.SCXMLDriver;


/**
 * 
 * A state handler that handles execution of business logic that should be invoked on the given state
 * 
 */
public interface StateHandler extends Callable<String> {
    /**
     * State that the handler 
     * @return
     */
    String getState();
    /**
     * Sets the driver that marshals the processing
     * @param driver
     */
    void setSCXMLDriver(SCXMLDriver driver);
    /**
     * Commits any persisted data.
     * @return
     */
    String complete();
    
    /**
     * Processes failure
     * @return event that should be issued on failure
     */
    String failed();
    /**
     * 
     * @return exception being a cause of failure
     */
    Exception getStateHandlerException();
    
    /**
     * cancels the state handler processing that method should ensure that any processing
     * being performed by that state handler is stopped. i.e. any external process started, should be killed, and polling should be stopped etc.
     */
    void cancelProcessing();
}
