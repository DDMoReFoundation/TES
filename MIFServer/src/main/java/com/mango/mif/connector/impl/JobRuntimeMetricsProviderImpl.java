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

import com.google.common.base.Preconditions;
import com.mango.mif.connector.JobRuntimeMetricsProvider;
import com.mango.mif.core.domain.Job;


/**
 * Basic implementation of the metrics provider, it is execution-type independent
 */
public class JobRuntimeMetricsProviderImpl implements JobRuntimeMetricsProvider {

    @Override
    public String getRuntimeMetrics(Job job) {
        Preconditions.checkNotNull(job,"Job can't be null");
        return buildJobRuntimeMetrics(job);
    }

    private String buildJobRuntimeMetrics(Job job) {
        StringBuilder info = new StringBuilder();
        info.append("Job ID: ");
        info.append(job.getJobId());
        info.append("(secs) Execution File Name: ");
        info.append(job.getExecutionFile());
        info.append(" Username: ");
        info.append(job.getUserName());
        return info.toString();
    }
}
