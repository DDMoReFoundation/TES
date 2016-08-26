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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javax.jms.IllegalStateException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerException;


/**
 * Tests abstract state handler
 */
public class AbstractStateHandlerTest {

    /**
     * Instance under test
     */
    private AbstractStateHandler stateHandler;
    /**
     * Driver
     */
    @Mock
    SCXMLDriver scxmlDriver;
    /**
     * Mock state
     */
    private String mockState = "state";
    /**
     * mock event
     */
    private String mockEvent = "event";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldInvokeFailIfDoProcessingThrowsException() {

        stateHandler = new AbstractStateHandler(mockState, mockEvent) {

            @Override
            protected void doProcessing() throws StateHandlerException {
                throw new StateHandlerException("State handler exception");
            }

            @Override
            protected void doPreprocessing() throws StateHandlerException {
            }

            @Override
            protected void doPostProcessing() throws StateHandlerException {
            }
        };
        stateHandler.setSCXMLDriver(scxmlDriver);

        stateHandler = spy(stateHandler);
        stateHandler.call();

        verify(stateHandler).fail();
    }

    @Test
    public void shouldInvokeFailIfDriverCompleteMethodThrowsException() throws IllegalStateException {
        stateHandler = spy(new AbstractStateHandler(mockState, mockEvent) {

            @Override
            protected void doProcessing() throws StateHandlerException {
                //do nothing
            }

            @Override
            protected void doPreprocessing() throws StateHandlerException {
            }

            @Override
            protected void doPostProcessing() throws StateHandlerException {
            }
        });

        doThrow(new IllegalStateException("message")).when(scxmlDriver).stateHandlerProcessingCompleted(stateHandler);
        stateHandler.setSCXMLDriver(scxmlDriver);
        stateHandler.call();

        verify(stateHandler).fail();
    }

    @Test
    public void shouldInvokeDriverFailedMethodWhenNotInFailedNorCancelledState() {
        stateHandler = spy(new AbstractStateHandler(mockState, mockEvent) {

            @Override
            protected void doProcessing() throws StateHandlerException {
                //do nothing
            }

            @Override
            protected void doPreprocessing() throws StateHandlerException {
            }

            @Override
            protected void doPostProcessing() throws StateHandlerException {
            }
        });
        when(scxmlDriver.getProcessingStatus()).thenReturn(DriverProcessingStatus.RUNNING);

        stateHandler.setSCXMLDriver(scxmlDriver);
        stateHandler.fail();

        verify(scxmlDriver).stateHandlerProcessingFailed(stateHandler);
    }

    @Test
    public void shouldNotInvokeDriverFailedMethodWhenCancelledState() {
        stateHandler = spy(new AbstractStateHandler(mockState, mockEvent) {

            @Override
            protected void doProcessing() throws StateHandlerException {
                //do nothing
            }

            @Override
            protected void doPreprocessing() throws StateHandlerException {
            }

            @Override
            protected void doPostProcessing() throws StateHandlerException {
            }
        });
        when(scxmlDriver.getProcessingStatus()).thenReturn(DriverProcessingStatus.CANCELLED);
        stateHandler.setSCXMLDriver(scxmlDriver);
        stateHandler.fail();

        verify(scxmlDriver, times(0)).stateHandlerProcessingFailed(stateHandler);
    }

    @Test
    public void shouldNotInvokeDriverFailedMethodWhenInFailedState() {
        stateHandler = spy(new AbstractStateHandler(mockState, mockEvent) {

            @Override
            protected void doProcessing() throws StateHandlerException {
                //do nothing
            }

            @Override
            protected void doPreprocessing() throws StateHandlerException {
            }

            @Override
            protected void doPostProcessing() throws StateHandlerException {
            }
        });
        when(scxmlDriver.getProcessingStatus()).thenReturn(DriverProcessingStatus.FAILED);
        stateHandler.setSCXMLDriver(scxmlDriver);
        stateHandler.fail();

        verify(scxmlDriver, times(0)).stateHandlerProcessingFailed(stateHandler);
    }
}
