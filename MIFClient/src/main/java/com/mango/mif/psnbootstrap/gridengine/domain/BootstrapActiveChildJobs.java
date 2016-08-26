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
package com.mango.mif.psnbootstrap.gridengine.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.nonmem.gridengine.domain.NONMEMProcessingDetailedStatus;



/**
 * A REST response containing a list of summary messages of running jobs
 */
@XmlRootElement(name = "bootstrapActiveChildJobsResponse")
public class BootstrapActiveChildJobs extends MIFResponse {
    /**
     * Active child jobs
     */
    private List<NONMEMProcessingDetailedStatus> activeChildJobs;

    @XmlElementWrapper(name="activeChildJobs")
    @XmlElement(name="activeChildJob")
    public List<NONMEMProcessingDetailedStatus> getActiveChildJobs() {
        return activeChildJobs;
    }
    
    public void setActiveChildJobs(List<NONMEMProcessingDetailedStatus> activeChildJobs) {
        this.activeChildJobs = activeChildJobs;
    }
}
