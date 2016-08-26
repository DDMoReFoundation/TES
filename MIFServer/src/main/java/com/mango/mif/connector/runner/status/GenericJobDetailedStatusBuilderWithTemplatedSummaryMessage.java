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
package com.mango.mif.connector.runner.status;

import org.apache.commons.lang.StringUtils;

import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.template.JobAwareFreemarkerTemplateCommandBuilder;
import com.mango.mif.domain.DetailedStatus;

/**
 * 
 * Freemarker template based summary message builder
 */
public class GenericJobDetailedStatusBuilderWithTemplatedSummaryMessage extends GenericJobDetailedStatusBuilder {

    /**
     * Text builder, which gathers the information we want into a pretty string
     */
    protected JobAwareFreemarkerTemplateCommandBuilder summaryMessageBuilder;

    /**
     * A handy invoker for invoking command execution;
     */
    private Invoker invoker;

    @Override
    public void setJob(Job job) throws ExecutionException {
        super.setJob(job);
        invoker = job.getInvoker();
        summaryMessageBuilder.setJob(job);
    }

    @Override
    public DetailedStatus getDetailedStatus() throws ExecutionException {
        DetailedStatus status = super.getDetailedStatus();
        status.setSummary(StringUtils.trimToEmpty(summaryMessageBuilder.getCommand()));
        return status;
    }

    public void setSummaryMessageBuilder(JobAwareFreemarkerTemplateCommandBuilder summaryMessageBuilder) {
        this.summaryMessageBuilder = summaryMessageBuilder;
    }

    public Invoker getInvoker() {
        return invoker;
    }
}
