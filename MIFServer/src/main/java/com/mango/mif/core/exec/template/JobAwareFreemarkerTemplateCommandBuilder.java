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
package com.mango.mif.core.exec.template;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.domain.ExecutionRequest;

/**
 * As the name implies, this freemarker template command builder is aware of jobs.
 * The (only) reason this class exists (thus far) is to provide one central point at which
 * information from the execution request attributes can be transferred to the command
 * builder - i.e at the point at which the job is set (the job having access to the
 * aforementioned execution request attributes).
 */
public class JobAwareFreemarkerTemplateCommandBuilder extends FreemarkerTemplateCommandBuilder {
    /**
     * The expected name for the job instance when placed in the context.
     */
    public static final String CONTEXT_NAME_JOB = "job";
    
    public JobAwareFreemarkerTemplateCommandBuilder() throws ExecutionException {
        super();
    }

    public JobAwareFreemarkerTemplateCommandBuilder(Map<String, Object> map) throws ExecutionException {
        super(map);
    }

    /**
     * This sets job into the freemarker context, but we also further initialise the context with values from the request attributes,
     * together with the submit and grid host preamble.
     * 
     * @param job The job to set within the freemarker context (and a lot else besides)
     * @throws ExecutionException If we can't set stuff.
     */
    public void setJob(Job job) {
        Preconditions.checkNotNull(job, "The job cannot be null");
        ExecutionRequest executionRequest = job.getExecutionRequest();
        CommandBuilderContextBuilder contextBuilder = new CommandBuilderContextBuilder(getContext())
                    .populateCommandBuilderContext(executionRequest)
                    .setVariable(CONTEXT_NAME_JOB, job);
        setContext(contextBuilder.buildCommandBuilderContext());
    }
}
