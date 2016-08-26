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
package com.mango.mif.connector.runner.impl;

import java.net.URL;

import javax.jms.IllegalStateException;

import com.mango.mif.connector.runner.JobRunnerDriver;
import com.mango.mif.connector.runner.impl.statehandler.StateHandler;


/**
 * Extends a generic JobRunnerDriver with methods specific to SCXML implementation.
 * 
 */
public interface SCXMLDriver extends JobRunnerDriver {
	/**
	 * A constant used for a null event
	 */
	public final static String NULL_EVENT = "NONE";
	/**
	 * A constant used for a null state
	 */
	public final static String NULL_STATE = "NONE";
    /**
     * fires an event that will result in transition of the state machine from one state to another
     * @param event
     */
    void fireEvent(String event);
    
    /**
     * Registers a state handler
     * @param stateHandler
     */
    void registerHandler(StateHandler stateHandler);
    
    /**
     * Unregisters a state handler
     * @param stateHandler
     * @return state handler if it has been removed, null otherwise
     */
    StateHandler unregisterHandler(StateHandler stateHandler);
    /**
     * Unregisters a state handler associated with the given state
     * @param state
     * @return unregistered handler if it has been removed, null otherwise
     */
    StateHandler unregisterHandler(String state);
    /**
     * Sets the SCXML document which defines state machine
     * @param scxmlDocument
     * @throws IllegalStateException 
     */
    void setSCXMLDocument(URL scxmlDocument) throws IllegalStateException;
    /**
     * Handles completion of the state handler, ensures that handling the completion of the handler is atomic
     * @param sHandler
     * @throws IllegalStateException 
     */
    void stateHandlerProcessingCompleted(StateHandler sHandler) throws IllegalStateException;
    /**
     * Handles failure of the given state handler
     * @param abstractStateHandler
     */
    void stateHandlerProcessingFailed(StateHandler stateHandler);
    /**
     * Invoked by state handler before any processing starts.
     * @param abstractStateHandler
     */
    void stateHandlerProcessingStarted(StateHandler abstractStateHandler);
    /**
     * Registers the handler that should be invoked by 'cancel' method
     * @param stateHandler
     */
    void setCancellationHandler(SCXMLProcessingCancellationHandler stateHandler);
    /**
     * 
     * @return state handler
     */
    SCXMLProcessingCancellationHandler getCancellationHandler();
}