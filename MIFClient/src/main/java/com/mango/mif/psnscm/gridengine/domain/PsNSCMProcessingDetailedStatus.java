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
package com.mango.mif.psnscm.gridengine.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.mango.mif.domain.DetailedStatus;


/**
 * 
 * Objects of this class hold detailed status information of PsN SCM run processing
 */
@XmlRootElement(name = "PsNSCMProcessingDetailedStatus")
public class PsNSCMProcessingDetailedStatus extends DetailedStatus {
    /**
     * username who started the job
     */
    private String userName;
    /**
     * grid job id
     */
    private String gridJobId;
    /**
     * Control file name
     */
    private String controlFileName;
    /**
     * How long NONMEM has been running
     */
    private String duration;
    
    /**
	 * Error details if any raised during call to job service.
     * FIXME Check if used within navigator and remove
	 */
	private String errors;
	
    /**
     * Summary message
     * FIXME Remove
     */
    private String parameterCovariateRelation;
    /**
     * Child processes
     * FIXME remove
     */
    private List<PsNSCMModelProcessingStatus> models = Lists.newArrayList();
    
    public void setControlFileName(String controlFileName) {
        this.controlFileName = controlFileName;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getControlFileName() {
        return controlFileName;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public String getGridJobId() {
        return gridJobId;
    }
    
    public void setGridJobId(String gridJobId) {
        this.gridJobId = gridJobId;
    }
    
    public void setModels(List<PsNSCMModelProcessingStatus> models) {
        this.models = models;
    }
    
    public List<PsNSCMModelProcessingStatus> getModels() {
        return models;
    }
    
    public void addModel(PsNSCMModelProcessingStatus model) {
        models.add(model);
    }
    
    public void setParameterCovariateRelation(String parameterCovariateRelation) {
        this.parameterCovariateRelation = parameterCovariateRelation;
    }
    
    public String getParameterCovariateRelation() {
        return parameterCovariateRelation;
    }

	public String getErrors() {
		return errors;
	}

	public void setErrors(String errors) {
		this.errors = errors;
	}
    
    
}
