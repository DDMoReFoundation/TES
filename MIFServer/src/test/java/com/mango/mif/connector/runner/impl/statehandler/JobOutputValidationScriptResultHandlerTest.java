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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.connector.runner.impl.statehandler.JobOutputValidationScriptResultHandler;
import com.mango.mif.connector.runner.impl.statehandler.ValidationFailedException;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;


/**
 * Tests JobOutputValidationScriptResultHandlerTest
 */
@RunWith(MockitoJUnitRunner.class)
public class JobOutputValidationScriptResultHandlerTest {
    JobOutputValidationScriptResultHandler invokerResultHandler;

    private final static String DEFAULT_EXIT_EVENT = "default";

    @Mock 
    Job job;

    @Test(expected=ValidationFailedException.class)
    public void handle_should_throw_exception_if_invoker_result_represents_failure() throws ExecutionException {
        invokerResultHandler = new JobOutputValidationScriptResultHandler(DEFAULT_EXIT_EVENT);
        invokerResultHandler.setJob(job);
        InvokerResult invokerResult = mock(InvokerResult.class);
        when(invokerResult.getExitStatus()).thenReturn(2);
        when(invokerResult.getErrorStream()).thenReturn("STD_ERR");
        when(invokerResult.getOutputStream()).thenReturn("STD_OUT");

        invokerResultHandler.handle(invokerResult);
    }


    @Test
    public void handle_should_result_in_default_event_for_successful() throws ExecutionException {
        invokerResultHandler = new JobOutputValidationScriptResultHandler(DEFAULT_EXIT_EVENT);
        invokerResultHandler.setJob(job);
        InvokerResult invokerResult = mock(InvokerResult.class);
        when(invokerResult.getExitStatus()).thenReturn(0);
        when(invokerResult.getErrorStream()).thenReturn("STD_ERR");
        when(invokerResult.getOutputStream()).thenReturn("STD_OUT");

        invokerResultHandler.handle(invokerResult);
        
        assertEquals(DEFAULT_EXIT_EVENT, invokerResultHandler.getStateExitEvent());
    }

}
