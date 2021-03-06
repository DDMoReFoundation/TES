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
package com.mango.mif.nonmem.gridengine.domain;

import javax.xml.bind.annotation.XmlRootElement;

import com.mango.mif.domain.DetailedStatus;

/**
 * Objects of this class hold detailed status information of NONMEM run processing
 */
@XmlRootElement(name = "NONMEMProcessingDetailedStatus")
public class NONMEMProcessingDetailedStatus extends DetailedStatus {
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
     * Working directory
     */
    private String workingDirectory;
    /**
     * Taken from  the $PROB statement of the control file
     */
    private String problem;
    /**
     * Taken from the $DATA statement of the control file
     */
    private String dataFileName;
    /**
     * How long NONMEM has been running (in milliseconds)
     */
    private String duration;
    /**
     * Status
     */
    private String status;
    /**
     * When NONMEM started to run (in milliseconds)
     */
    private String startTime;
    /**
     * When NONMEM last updated its files (in milliseconds)
     */
    private String lastUpdated;
    /**
     * Real Run name, in case of PsN bootstrap child nonmem run has two control file names associated (or more)
     * psn*.mod ones and the 'real' name. thie is the real one, in case of NMFE execution that field should be the same as controlFileName
     */
    private String runName;

    private String description;

    private String estimationMethod;

    private String objectiveFunctionValue;

    private String outputFile;

    private String requestDirectory;

    private String gridJobDirectory;

    public void setControlFileName(String controlFileName) {
        this.controlFileName = controlFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setProblem(String problem) {
        this.problem = problem;
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

    public String getProblem() {
        return problem;
    }

    public String getDataFileName() {
        return dataFileName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setEstimationMethod(String estimationMethod) {
        this.estimationMethod = estimationMethod;
    }

    public String getEstimationMethod() {
        return estimationMethod;
    }

    public void setObjectiveFunctionValue(String objectiveFunctionValue) {
        this.objectiveFunctionValue = objectiveFunctionValue;
    }

    public String getObjectiveFunctionValue() {
        return objectiveFunctionValue;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getRunName() {
        return runName;
    }

    public String getRequestDirectory() {
        return requestDirectory;
    }

    public void setRequestDirectory(String requestDirectory) {
        this.requestDirectory = requestDirectory;
    }

    public String getGridJobDirectory() {
        return gridJobDirectory;
    }

    public void setGridJobDirectory(String gridJobDirectory) {
        this.gridJobDirectory = gridJobDirectory;
    }
}
