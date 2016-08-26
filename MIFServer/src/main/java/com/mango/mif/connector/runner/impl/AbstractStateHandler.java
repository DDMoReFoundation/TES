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

import org.apache.log4j.Logger;

import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.connector.runner.impl.statehandler.StateHandler;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerException;

/**
 * Abstract State Handler providing an implementation common to all state handlers
 */
public abstract class AbstractStateHandler implements StateHandler {
    /**
     * Logger
     */
    protected static final Logger LOG = Logger.getLogger(AbstractStateHandler.class);
    /**
     * State that the handler is associated with
     */
    final protected String handledState;
    /**
     * Event that will be sent to driver when the handler finishes its job
     */
    protected String exitEvent;
    /**
     * Driver that the handler is registered to
     */
    protected SCXMLDriver driver;
    /**
     * Failure cause
     */
    private Exception stateHandlerException;

    /**
     * Constructs a new state handler for given state and given exit event
     * @param state
     * @param exitEvent - exit event name or <code>NONE</code> if there is no exit event expected
     */
    public AbstractStateHandler(String state, String exitEvent) {
        this.handledState = state;
        this.exitEvent = exitEvent;
    }

    /**
     * Constructs a new state handler for given state 
     * @param state
     */
    public AbstractStateHandler(String state) {
        this.handledState = state;
        this.exitEvent = SCXMLDriver.NULL_EVENT;
    }

    @Override
    public String call() {
        try {
            driver.stateHandlerProcessingStarted(this);
            doPreprocessing();
            doProcessing();
            doPostProcessing();
            driver.stateHandlerProcessingCompleted(this);
        } catch (Exception e) {
            LOG.error("Error processing " + handledState + " state handler.", e);
            stateHandlerException = e;
            fail();
        }
        return "DONE-" + handledState;
    }

    /**
     * A method called before the doProcessing, the handlers should implement the method 
     * with any preprocessing business logic
     */
    abstract protected void doPreprocessing() throws StateHandlerException;

    /**
     * A method responsible for performing processing that should happen when the state is triggered
     */
    abstract protected void doProcessing() throws StateHandlerException;

    /**
     * A method responsible for performing processing that should happen after the main processing but before 
     * persisting job 
     */
    abstract protected void doPostProcessing() throws StateHandlerException;

    @Override
    public String getState() {
        return handledState;
    }

    @Override
    public void setSCXMLDriver(SCXMLDriver driver) {
        this.driver = driver;
    }

    /**
     * {@inheritDoc}
     * 
     * This method should be invoked last if overridden.
     * @throws StateHandlerException 
     */
    @Override
    public String complete() {
        return (SCXMLDriver.NULL_EVENT.equals(exitEvent) ? null : exitEvent);
    }

    /**
     * initiates state processing failure handling procedure
     */
    protected void fail() {
        if (DriverProcessingStatus.CANCELLED.equals(driver.getProcessingStatus())
                || DriverProcessingStatus.FAILED.equals(driver.getProcessingStatus())) {
            /* if the job has been cancelled or has failed, don't handle failure, 
             * because cancellation/failure might result in sate handlers processing failures
             */
            return;
        }
        LOG.error("Processing of a handler for state " + handledState + " failed. ");

        driver.stateHandlerProcessingFailed(this);
    }

    @Override
    public String failed() {
        return SCXMLDriver.NULL_EVENT;
    }

    @Override
    public Exception getStateHandlerException() {
        return stateHandlerException;
    }

    // TODO: Can this be removed now?
    @Override
    public void cancelProcessing() {
    }
}