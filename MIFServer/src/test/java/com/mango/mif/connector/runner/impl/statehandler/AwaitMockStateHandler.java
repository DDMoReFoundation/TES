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

import java.util.concurrent.CountDownLatch;

import com.mango.mif.connector.runner.impl.AbstractStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerException;

/**
 * 
 * A state handler that expects to be notified by an external thread when in doProcessing method.
 * 
 */
public class AwaitMockStateHandler extends AbstractStateHandler {
    /**
     * lock
     */
    private CountDownLatch awaitSignal = new CountDownLatch(1);
    /**
     * processing done
     */
    private volatile boolean done = false;
	/**
	 * @param state
	 * @param exitEvent
	 */
	public AwaitMockStateHandler(String state, String exitEvent) {
		super(state, exitEvent);
	}

	/* (non-Javadoc)
	 * @see com.mango.mif.connector.runner.impl.AbstractStateHandler#doProcessing()
	 */
	@Override
	protected void doProcessing() throws StateHandlerException {
    	try {
			awaitSignal.await();
		} catch (InterruptedException e) {
			LOG.error(e);
			throw new StateHandlerException("Error when waiting for signal from external process.",e);
		} finally {
			done = true;
		}
	}
	
	/**
	 * @return the awaitSignal
	 */
	public CountDownLatch getAwaitSignal() {
		return awaitSignal;
	}
	
	/**
	 * @return the done
	 */
	public boolean isDone() {
		return done;
	}

    @Override
    protected void doPreprocessing() throws StateHandlerException {
    }

    @Override
    protected void doPostProcessing() throws StateHandlerException {
    }
    
    @Override
    public void cancelProcessing() {
        super.cancelProcessing();
        awaitSignal.countDown();
    }
	
}
