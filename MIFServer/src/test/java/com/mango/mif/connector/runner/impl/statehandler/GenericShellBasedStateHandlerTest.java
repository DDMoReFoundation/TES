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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.connector.runner.impl.DefaultJobRunner;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.impl.SCXMLDriver;
import com.mango.mif.connector.runner.impl.statehandler.GenericShellBasedStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.JobProcessingAwareInvokerResultHandler;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerException;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.template.JobAwareFreemarkerTemplateCommandBuilder;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.encrypt.Encrypter;


/**
 * tests generic shell based state handler
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericShellBasedStateHandlerTest {
    /**
     * Tested instance
     */
    private GenericShellBasedStateHandler stateHandler;
    /**
     * invoker
     */
    @Mock Invoker invoker;
    /**
     * result handler
     */
    @Mock JobProcessingAwareInvokerResultHandler invokerResultHandler;
    /**
     * Command builder
     */
    @Mock JobAwareFreemarkerTemplateCommandBuilder commandBuilder;
    /**
     * Job runner
     */
    @Mock DefaultJobRunner jobRunner;
    /**
     * Encrypter
     */
    @Mock Encrypter encrypter;
    /**
     * Driver
     */
    @Mock SCXMLDriver driver;
    /**
     * job repository
     */
    @Mock JobManagementService jobManagementService;

    @Mock Job job;
    private final static String MOCK_JOB_ID = "JOB_ID";

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        stateHandler = new GenericShellBasedStateHandler(JobRunnerState.UNDEFINED.name(), SCXMLDriver.NULL_EVENT);
        stateHandler.setCommandBuilder(commandBuilder);
        stateHandler.setJobRunner(jobRunner);
        stateHandler.setInvokerResultHandler(invokerResultHandler);
        stateHandler.setSCXMLDriver(driver);
        stateHandler.setJobManagementService(jobManagementService);
        when(jobRunner.getJobId()).thenReturn(MOCK_JOB_ID);
        when(job.getJobId()).thenReturn(MOCK_JOB_ID);
        when(jobManagementService.getJob(MOCK_JOB_ID)).thenReturn(job);
        when(jobManagementService.saveJob(job)).thenReturn(job);
        when(jobManagementService.saveJobRunnerState(eq(MOCK_JOB_ID), (JobRunnerState)any())).thenReturn(job);
        when(jobManagementService.update(eq(MOCK_JOB_ID),(ObjectStateUpdater<Job>) any())).thenReturn(job);
        when(job.getInvoker()).thenReturn(invoker);
    }

    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfCommandBuilderNotSet() throws StateHandlerException {
        stateHandler.setCommandBuilder(null);
        stateHandler.doProcessing();
    }

    @Test(expected=NullPointerException.class)
    public void shouldThrowExceptionIfInvokerResultHanlderIsNotSet() throws StateHandlerException {
        stateHandler.setInvokerResultHandler(null);
        stateHandler.doProcessing();
    }

    @Test(expected=StateHandlerException.class)
    public void shouldThrowExceptionIfInvokerFailsToExecuteACommand() throws ExecutionException, StateHandlerException {
        when(commandBuilder.getCommand()).thenReturn("MOCK_COMMAND");
        when(invoker.execute((String) any())).thenThrow(new ExecutionException("Error message"));
        stateHandler.doPreprocessing();
        stateHandler.doProcessing();
    }

    @Test(expected=StateHandlerException.class)
    public void shouldThrowExceptionIfCommandBuilderFailsToBuildUpACommmand() throws ExecutionException, StateHandlerException, MIFException {
        when(commandBuilder.getCommand()).thenThrow(new ExecutionException("Error message"));
        stateHandler.doPreprocessing();
        stateHandler.doProcessing();
    }

    @Test(expected=StateHandlerException.class)
    public void shouldThrowExceptionIfInvokerResultHandlerFailsToHandleResults() throws StateHandlerException, ExecutionException {
        when(commandBuilder.getCommand()).thenReturn("MOCK_COMMAND");
        doThrow(new ExecutionException("Error message")).when(invokerResultHandler).handle((InvokerResult) any());
        stateHandler.doPreprocessing();
        stateHandler.doProcessing();
    }

    @Test(expected=StateHandlerException.class)
    public void shouldThrowExceptionIfCommandBuilderReturnsEmptyShellCommand() throws StateHandlerException, ExecutionException {
        when(commandBuilder.getCommand()).thenReturn("");
        stateHandler.doPreprocessing();
        stateHandler.doProcessing();
    }


    @Test(expected=StateHandlerException.class)
    public void shouldThrowExceptionIfCommandBuilderReturnsNullShellCommand() throws StateHandlerException, ExecutionException {
        when(commandBuilder.getCommand()).thenReturn(null);
        stateHandler.doPreprocessing();
        stateHandler.doProcessing();
    }

    @Test
    public void shouldPersistJobJustOnPreprocessingAndComplete() throws StateHandlerException, ExecutionException {
        when(commandBuilder.getCommand()).thenReturn("MOCK_COMMAND");
        stateHandler.doPreprocessing();
        verify(jobManagementService,times(1)).saveJobRunnerState((String)any(), (JobRunnerState) any());
        stateHandler.doProcessing();
        stateHandler.complete();
        verify(jobManagementService,times(1)).update(eq(job.getJobId()), (ObjectStateUpdater<Job>) any());
    }
}
