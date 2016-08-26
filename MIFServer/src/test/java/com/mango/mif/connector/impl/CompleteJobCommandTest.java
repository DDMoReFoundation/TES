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
package com.mango.mif.connector.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.mango.mif.core.domain.Job;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.JobStatus;


/**
 * Tests CompleteJobCommand
 */
@RunWith(MockitoJUnitRunner.class)
public class CompleteJobCommandTest {
    
    @Mock ExecutionResponse executionResponse;
    
    @Mock Job job;
    @Test
    public void should_perform_finalizing_updates_on_job_object() {
        when(executionResponse.getStatus()).thenReturn(JobStatus.COMPLETED.name());
        CompleteJobCommand command = new CompleteJobCommand(executionResponse);
        
        command.apply(job);
        
        verify(job).setJobStatus(JobStatus.COMPLETED);
        verify(job).setExecutionResponse(executionResponse);
        
    }

}
