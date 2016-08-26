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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jms.IllegalStateException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import com.mango.mif.connector.runner.impl.statehandler.DefaultStateHandler;
import com.mango.mif.core.domain.Job;
import com.mango.mif.domain.ExecutionRequest;

/**
 * Tests JobRunnerWithSCXMLDriverFactoryTest
 */
@RunWith(MockitoJUnitRunner.class)
public class JobRunnerWithSCXMLDriverFactoryTest {
    /**
     * tested instance
     */
    private JobRunnerWithSCXMLDriverFactory jobRunnerFactory;
    /**
     * scxml definition
     */
    @Mock Resource scxmlDefinition;
    /**
     * job runner
     */
    @Mock DefaultJobRunner jobRunner;
    /**
     * job runner
     */
    @Mock SCXMLDriver scxmlDriver;
    
    @Mock Job job;
    /**
     * state handlers
     */
    List<DefaultStateHandler> stateHandlers = new ArrayList<DefaultStateHandler>();

    @Before
    public void setUp() throws IOException {
        stateHandlers.add(mock(DefaultStateHandler.class));
        stateHandlers.add(mock(DefaultStateHandler.class));
        jobRunnerFactory = new JobRunnerWithSCXMLDriverFactory() {

            @Override
            public List<DefaultStateHandler> getStateHandlers() {
                return stateHandlers;
            }

            @Override
            protected DefaultJobRunner createJobRunner() {
                return jobRunner;
            }

            @Override
            public SCXMLDriver createDriver() {
                return scxmlDriver;
            }

            @Override
            public SCXMLProcessingCancellationHandlerImpl createCancellationHandler() {
                return mock(SCXMLProcessingCancellationHandlerImpl.class);
            }
        };
        jobRunnerFactory.setScxmlDefinition(scxmlDefinition);
        when(job.getExecutionRequest()).thenReturn(mock(ExecutionRequest.class));
        when(jobRunner.getJobId()).thenReturn("JOB_ID");
    }

    @Test(expected = NullPointerException.class)
    public void buildDriver_should_throw_exception_if_SCXML_definition_not_set() {
        jobRunnerFactory.setScxmlDefinition(null);
        jobRunnerFactory.buildDriver(jobRunner,job);
    }

    @Test(expected = NullPointerException.class)
    public void buildDriver_should_throw_exception_if_state_handlers_are_not_available() {
        this.stateHandlers = null;
        jobRunnerFactory.buildDriver(jobRunner,job);
    }
    
    @Test
    public void buildDriver_should_set_driver_on_job_runner() {
        jobRunnerFactory.buildDriver(jobRunner,job);
        verify(jobRunner).setDriver(scxmlDriver);
    }

    @Test
    public void createJobRunner_should_create_job_runner() {
        assertNotNull(jobRunnerFactory.createJobRunner(job));
    }

    @Test(expected = RuntimeException.class)
    public void buildDriver_should_throw_exception_if_SCXML_definition_is_invalid() throws IllegalStateException, IOException {
        when(scxmlDefinition.getURL()).thenThrow(mock(IOException.class));
        jobRunnerFactory.buildDriver(jobRunner,job);
    }
    

    @Test(expected = RuntimeException.class)
    public void buildDriver_should_throw_exception_if_SCXML_initialilzation_fails() throws IllegalStateException {
        doThrow(mock(IllegalStateException.class)).when(scxmlDriver).setSCXMLDocument((URL)any());
        jobRunnerFactory.buildDriver(jobRunner,job);
    }
}
