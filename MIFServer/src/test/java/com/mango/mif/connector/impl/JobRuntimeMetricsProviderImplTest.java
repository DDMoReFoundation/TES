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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.mango.mif.core.domain.Job;


/**
 * Tests {@link JobRuntimeMetricsProviderImpl}
 */
public class JobRuntimeMetricsProviderImplTest {
    private static final Logger LOG = Logger.getLogger(JobRuntimeMetricsProviderImplTest.class);

    @Test
    public void getRuntimeMetrics_shouldGenerateRuntimeMetrics() {
        JobRuntimeMetricsProviderImpl metricsProvider = new JobRuntimeMetricsProviderImpl();
        

        Job job = mock(Job.class);
        when(job.getJobId()).thenReturn("JOB_A");
        when(job.getUserName()).thenReturn("mock-user");
        when(job.getExecutionFile()).thenReturn("EXECUTION_FILE");
        
        String metrics = metricsProvider.getRuntimeMetrics(job);
        LOG.debug(metrics);
        assertEquals(
            "Job ID: JOB_A(secs) Execution File Name: EXECUTION_FILE Username: mock-user",
            metrics);
    }
}
