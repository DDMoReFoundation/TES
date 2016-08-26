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
package com.mango.mif.core.domain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.messaging.MessagingHelper;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * The job that holds runtime data related to handling of an Execution Request.
 */
/*
 * 
 * Important!!!
 * There should be NO methods specific to execution type or third party tool added to this class. Such functionality should be provided by a separate class.
 */
@Entity
@Table(name = "JOBS")
public class Job {

    private static final Logger LOG = Logger.getLogger(Job.class);

    private static final int EXECUTION_MSG_MAX_LEN = 10000;

    @XmlTransient
    private String jobId;

    @XmlTransient
    private Invoker invoker;

    private String connectorId;

    private Map<String, String> data = new HashMap<String, String>();

    protected String executionRequestMsg;

    private String clientRequestStatus;

    private String detailedStatusMsg;

    private String executionResponseMsg;

    private DetailedStatus detailedStatus;

    private ExecutionRequest executionRequest;

    private ExecutionResponse executionResponse;
    
    private CommandExecutionTarget commandExecutionTarget;
    
    private String userName;

    private String password;

    private long version;
    
    /**
     * The time in millis that this job was handled and acknowledged.
     */
    private long acknowledged;


    @Id
    @Column(name = "JOB_ID")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Transient
    public JobRunnerState getJobRunnerState() {
        String status = getData().get(JobRunnerState.STATUS);
        if (status == null) {
            return JobRunnerState.UNDEFINED;
        }
        return JobRunnerState.valueOf(status);
    }

    public void setJobRunnerState(JobRunnerState status) {
        setProperty(JobRunnerState.STATUS, status.name());
    }
    
    @Column(name = "CONNECTOR_ID")
    public String getConnectorId() {
        return this.connectorId;
    }
    
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    @Lob
    @Column(name = "EXECUTION_REQUEST_MESSAGE", length = EXECUTION_MSG_MAX_LEN)
    public String getExecutionRequestMsg() {
        return executionRequestMsg;
    }
    
    @Lob
    @Column(name = "EXECUTION_RESPONSE", length = EXECUTION_MSG_MAX_LEN)
    public String getExecutionResponseMsg() {
        return executionResponseMsg;
    }

    /**
     * Will deserialise the provided message to set the execution request property on this job
     * NOTE: If request provided submitAsUser=true it will also populate the job's user credentials
     * with the details contained within the request
     * @param executionRequestMsg
     */
    public void setExecutionRequestMsg(String executionRequestMsg) {
        this.executionRequestMsg = executionRequestMsg;
        if(executionRequestMsg==null) {
            executionRequest = null;
            return;
        }
        executionRequest = MessagingHelper.parseExecutionRequest(executionRequestMsg);
        populateJobWithExecutionMessageData(executionRequest);
    }
    
    public void setExecutionResponseMsg(String executionResponseMsg) {
        this.executionResponseMsg = executionResponseMsg;
        if(executionResponseMsg==null) {
            executionResponse = null;
            return;
        }
        this.executionResponse = MessagingHelper.parseExecutionResponse(executionResponseMsg);;
    }
    
    @Column(name = "CLIENT_REQUEST_STATUS")
    public String getClientRequestStatus() {
        return clientRequestStatus;
    }

    public void setClientRequestStatus(String clientRequestStatus) {
        this.clientRequestStatus = clientRequestStatus;
    }
    
    @Transient
    public ExecutionResponse getExecutionResponse() {
        return executionResponse;
    }
    
    @Transient
    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    /**
     * A user name that executes the job, 
     * If submit as user flag of the execution request is false, it returns the service account user name
     */
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "PASSWORD")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * To set and get properties use setProperty and getProperty methods.
     * 
     * TODO this should return a copy. Can't refactor until all code using this to set job metadata is refactored
     * TODO try reducing the visibility of the method 
     * 
     * @return job metadata 
     */
    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @CollectionTable(name = "JOBS_DATA", joinColumns = @JoinColumn(name = "JOB_ID"))
    @MapKeyClass(String.class)
    @MapKeyColumn(name = "JOB_DATA_KEY")
    @Column(name = "JOB_DATA_VALUE", nullable = true)
    public Map<String, String> getData() {
        return data;
    }

    /**
     * To set and get properties use setProperty and getProperty methods.
     * 
     * TODO this should create a copy. Can't refactor until all code using this to set job metadata is refactored
     * TODO try reducing the visibility of the method 
     * 
     * @param job metadata 
     */
    public void setData(Map<String, String> data) {
        this.data = data;
    }
    
    /**
     * @param target - the {@link CommandExecutionTarget} as configured in the Connector config
     */
    public void setCommandExecutionTarget(final CommandExecutionTarget target) {
    	this.commandExecutionTarget = target;
    }
    
    /**
     * The associated {@link CommandExecutionTarget} is a Hibernate-transient property; this object
     * doesn't need to be persisted. Instead, {@link com.mango.mif.core.dao.impl.JobRepository} is
     * responsible for re-populating this when a {@link Job} is retrieved from the database.
     * <p>
     * @return the {@link CommandExecutionTarget} as configured in the Connector config
     */
    @Transient
    public CommandExecutionTarget getCommandExecutionTarget() {
    	return this.commandExecutionTarget;
    }

    /**
     * The script file (.ctl file in case of NONMEM) that should be executed
     * the path is relative to the request root directory
     * @return the execution file path
     * @throws IllegalStateException if execution file is not set on the request, use 'hasExecutionFile' to check if an execution file is set
     */
    @Transient
    public String getExecutionFile() {
        Preconditions.checkState(hasExecutionFile(),"There is no execution file specified in the Execution Request.");
        return getExecutionRequest().getExecutionFile();
    }

    /**
     * 
     * @return true if an execution request specifies an execution file
     */
    @Transient
    public boolean hasExecutionFile() {
        final ExecutionRequest executionRequest = getExecutionRequest();
        Preconditions.checkState(executionRequest!=null, "Execution Request was not set on the job");
        return !StringUtils.isBlank(executionRequest.getExecutionFile());
    }

    /**
     * @return The "basename" of the execution file, i.e. the file name stripped of all directories.
     * @throws IllegalStateException if there is no execution file, use 'hasExecutionFile' to check if it is available
     */
    @Transient
    public String getExecutionFileName() {
        Preconditions.checkState(hasExecutionFile(), "This job does not have execution file");
        return new File(getExecutionFile()).getName();
    }
    
    /**
     * This path is assumed to only be used on template-generated scripts executing on the host running MIF,
     * since it uses <code>new File()</code> to create the path.
     * <p>
     * @return a job working directory - i.e. a directory that contains the execution file (if it is set)
     */
    @Transient
    public File getJobWorkingDirectory() {
        if(!hasExecutionFile()) {
            return getJobDirectory();
        }
        File gridJobExecutionFileHandle = getJobExecutionFilePath();
        if (gridJobExecutionFileHandle.isDirectory()) {
            return gridJobExecutionFileHandle;
        } else {
            return gridJobExecutionFileHandle.getParentFile();
        }
    }
    /**
     * This path must only be used by template-generated scripts executing on the host running MIF,
     * since it returns an absolute path.
     * <p>
     * @return the absolute path to the execution file, on the MIF host
     * @throws IllegalStateException if execution file is not set on the execution request, use 'hasExecutionFile' to check if one was set 
     */
    @Transient
    public File getJobExecutionFilePath() {
        Preconditions.checkState(hasExecutionFile(), "This job does not have execution file");
        File jobExecutionFile = new File(getJobDirectory(), getExecutionFile());
        LOG.debug(String.format("returning job execution file path: %s",jobExecutionFile.getAbsolutePath()));
        return jobExecutionFile;
    }

    @Transient
    public String getRequestUserName() {
        return getExecutionRequest().getUserName();
    }

    public void setExecutionRequest(ExecutionRequest executionRequest) {
        this.executionRequestMsg = MessagingHelper.marshallExecutionRequest(executionRequest);
        this.executionRequest = executionRequest;
        populateJobWithExecutionMessageData(executionRequest);
    }
    
    public void setExecutionResponse(ExecutionResponse executionResponse) {
        this.executionResponseMsg = MessagingHelper.marshallExecutionResponse(executionResponse);
        this.executionResponse = executionResponse;
    }
    
    void populateJobWithExecutionMessageData(ExecutionRequest executionRequest) {
        setJobId(executionRequest.getRequestId());
        if (executionRequest.getSubmitAsUserMode()) {
            setUserName(executionRequest.getUserName());
            setPassword(executionRequest.getUserPassword());
        }
    }
    
    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    @Transient
    public Invoker getInvoker() {
        return invoker;
    }

    public void setDetailedStatusMsg(String detailedStatusMsg) {
        this.detailedStatusMsg = detailedStatusMsg;
        if(detailedStatusMsg==null) {
            detailedStatus = null;
            return;
        }
        try {
            this.detailedStatus = JAXBUtils.unmarshall(detailedStatusMsg,
                    ExecutionJaxbUtils.CONTEXT_CLASSES);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not unmarshall detailed status.", e);
        }
    }

    @Lob
    @Column(name = "DETAILED_STATUS", length = EXECUTION_MSG_MAX_LEN)
    public String getDetailedStatusMsg() {
        return detailedStatusMsg;
    }

    @Transient
    public DetailedStatus getDetailedStatus() throws MIFException {
        return detailedStatus;
    }
    
    /**
     * 
     * @param property for which value should be retrieved
     * @return a value of the property
     * @throws NullPointerException if property is null
     * @throws IllegalArgumentException if property is blank string
     */
    @Transient
    public String getProperty(String property) {
        Preconditions.checkNotNull(property, "Property name was null");
        Preconditions.checkArgument(!StringUtils.isBlank(property), String.format("A property was blank",property));
        return data.get(property);
    }
    /**
     * sets property on a job
     * @param property which should be set on the job
     * @param value that should be associated with the property
     * @throws NullPointerException if property or value is null
     * @throws IllegalArgumentException if property is blank string
     */
    public void setProperty(String property, String value) {
        Preconditions.checkNotNull(property, "Property name was null");
        Preconditions.checkArgument(!StringUtils.isBlank(property), String.format("A property was blank",property));
        Preconditions.checkNotNull(value, String.format("A value for property %s was null",property));
        data.put(property,value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(jobId, executionRequestMsg, clientRequestStatus, userName);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Job) {
            final Job other = (Job) obj;
            return Objects.equal(jobId, other.jobId)
                    && Objects.equal(executionRequestMsg, other.executionRequestMsg)
                    && Objects.equal(clientRequestStatus, other.clientRequestStatus)
                    && Objects.equal(userName, other.userName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        ToStringBuilder.setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
        return ToStringBuilder.reflectionToString(this).toString();
    }

    @Version
    @Column(name = "VERSION")
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Column(name = "TIME_ACKNOWLEDGED", nullable=true)
	public long getAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(long acknowledged) {
		this.acknowledged = acknowledged;
	}
    
    @Transient
    public JobStatus getJobStatus() {
        return JobStatus.valueOf(getClientRequestStatus());
    }
    
    public void setJobStatus(JobStatus jobStatus) {
        this.setClientRequestStatus(jobStatus.name());
    }
    
    /**
     * This must return a UNIX-style path, since the remote host is assumed to be a Linux/UNIX host;
     * but MIF can either be run on Windows or Linux/UNIX.
     * <p>
     * @return a path where the job files are accessible by the Remote Execution Host
     */
    @Transient
    public String getRemoteJobDirectory() {
        ExecutionRequest executionRequest = getExecutionRequest();
        Preconditions.checkNotNull(executionRequest, "Execution Request is not set");
        Preconditions.checkNotNull(executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName()), String.format("Execution Request Attribute %s is not set",ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName()));
        return createPlatformConsistentFilePath(executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName()),jobId);
    }
    
    /**
     * This must return a UNIX-style path, since the remote host is assumed to be a Linux/UNIX host;
     * but MIF can either be run on Windows or Linux/UNIX.
     * <p>
     * @return the path on a remote host to the MIF hidden directory
     */
    @Transient
    public String getRemoteJobMifHiddenDir() {
        return createPlatformConsistentFilePath(getRemoteJobDirectory(), MIFProperties.MIF_HIDDEN_DIRECTORY_NAME);
    }
    
    /**
     * This must return a UNIX-style path, since the remote host is assumed to be a Linux/UNIX host;
     * but MIF can either be run on Windows or Linux/UNIX.
     * <p> 
	 * @return the path to the file where the standard error has been written out by MIF
     */
    @Transient
    public String getRemoteJobErrorStreamFile() {
        return createPlatformConsistentFilePath(getRemoteJobMifHiddenDir(), MIFProperties.MIF_TPT_STD_ERR_FILE_NAME);
    }

    /**
     * This must return a UNIX-style path, since the remote host is assumed to be a Linux/UNIX host;
     * but MIF can either be run on Windows or Linux/UNIX.
     * <p>
	 * @return the path to the file where the standard output has been written out by MIF
     */
    @Transient
    public String getRemoteJobOutputStreamFile() {
        return createPlatformConsistentFilePath(getRemoteJobMifHiddenDir(), MIFProperties.MIF_TPT_STD_OUT_FILE_NAME);
    }

    /**
     * This path is assumed to only be used on template-generated scripts executing on the host running MIF,
     * since it uses <code>new File()</code> to create the path.
     * <p>
     * @return the path to where the job files are accessible by the MIF Host
     */
    @Transient
    public File getJobDirectory() {
        ExecutionRequest executionRequest = getExecutionRequest();
        Preconditions.checkNotNull(executionRequest, "Execution Request is not set");
        Preconditions.checkNotNull(executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()), String.format("Execution Request Attribute %s is not set",ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()));
        return new File(executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()),jobId);
    }

    /**
	 * This path is assumed to only be used on template-generated scripts executing on the host running MIF,
	 * since it uses <code>new File()</code> to create the path.
	 * <p>
	 * @return the path to the file where the standard error has been written out by MIF
	 */
    @Transient
    public File getJobErrorStreamFile() {
        return new File(getJobMifHiddenDir(), MIFProperties.MIF_TPT_STD_ERR_FILE_NAME); 
    }

    /**
	 * This path is assumed to only be used on template-generated scripts executing on the host running MIF,
	 * since it uses <code>new File()</code> to create the path.
	 * <p>
	 * @return the path to the file where the standard output has been written out by MIF
	 */
    @Transient
    public File getJobOutputStreamFile() {
        return new File(getJobMifHiddenDir(), MIFProperties.MIF_TPT_STD_OUT_FILE_NAME);
    }

    /**
     * This path is assumed to only be used on template-generated scripts executing on the host running MIF,
     * since it uses <code>new File()</code> to create the path.
     * <p>
     * @return the path to the MIF hidden directory, accessible by the MIF Host
     */
    @Transient
    public File getJobMifHiddenDir() {
        return new File(getJobDirectory(), MIFProperties.MIF_HIDDEN_DIRECTORY_NAME);
    }
    
    
    /**
     * When MIF is running on Windows but is invoking a Connector that runs expanded templates
     * on a (Linux/UNIX) remote host, where the templates reference properties e.g.
     * remoteJobDirectory on this Job object, then paths cannot be created via
     * <code>new File(...)</code> since they will have backslashes in the path string which
     * is invalid on *NIX.
     * <p>
     * Replacing back-slashes with forward-slashes ensures that paths are valid on both Windows
     * and Linux/UNIX platforms.
     * <p>
     * TODO: Need a better, less hacky and error-prone, way of performing such path translation.
     * <p>
     * @param parentPath
     * @param childPath
     * @return path string with forward slash as the path separator
     */
    private static String createPlatformConsistentFilePath(final String parentPath, final String childPath) {
    	return new File(parentPath, childPath).getPath().replace("\\", "/");
    }
    
}
