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

import com.mango.mif.connector.runner.impl.AbstractStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerException;


/**
 * 
 * Mock state handler, mocks processing for the given number of seconds and then returns.
 * 
 */
public class MockStateHandler extends AbstractStateHandler {
    /**
     * flag indicating that a state handler finished processing
     */
    private volatile boolean done = false;
    /**
     * number of seconds that mock processing should take
     */
    private int time = 1000;
    
    /**
     * Default constructor
     * @param state
     */
    public MockStateHandler(String state, String exitEvent) {
        super(state,exitEvent);
    }
    
    @Override
    protected void doProcessing() {
        String msg = "State " + handledState + " processing";
        LOG.info(msg + " - START");
        try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			LOG.error(e);
		}
        LOG.info(msg + " - END");
    }
    /**
     * 
     * @return true if done
     */
    public synchronized boolean isDone() {
        return done;
    }
    
    /**
     * Sets the number of milliseconds the mock processing should take
     * @param time
     */
    public void setTime(int time) {
        this.time = time;
    }
    
    @Override
    public synchronized String complete() {
        done = true;
        return super.complete();
    }
    
    public void setExitEvent(String exitEvent) {
    	this.exitEvent = exitEvent;
    }

    @Override
    protected void doPreprocessing() throws StateHandlerException {
    }

    @Override
    protected void doPostProcessing() throws StateHandlerException {
    }
}
