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

import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.domain.DetailedStatus;


/**
 * 
 * A generic detailed status builder, returning empty string by default
 * 
 */
public class GenericJobDetailedStatusBuilder implements JobDetailedStatusBuilder {
    /**
     * Job
     */
    protected Job job;
    
    @Override
    public void setJob(Job job) throws ExecutionException {
        this.job = job;
    }

    @Override
    public DetailedStatus getDetailedStatus() throws ExecutionException {
        DetailedStatus detailedStatus = new DetailedStatus();
        detailedStatus.setRequestId(job.getJobId());
        return detailedStatus;
    }


}
