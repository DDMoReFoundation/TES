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
package com.mango.mif.domain;

import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * Builds Execution request.
 *
 */
public class ExecutionRequestBuilder {

    /** Execution request being built up. */
    private ExecutionRequest executionRequest;

    public ExecutionRequestBuilder() {
        this.executionRequest = new ExecutionRequest();
    }

    public ExecutionRequestBuilder setName(String name) {
        executionRequest.setName(name);
        return this;
    }

    /**
     * Sets the user name.
     *
     * @param userName the user name
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setUserName(String userName) {
        this.executionRequest.setUserName(userName);
        return this;
    }

    /**
     * Sets the user password.
     *
     * @param userPassword the user password
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setUserPassword(String userPassword) {
        this.executionRequest.setUserPassword(userPassword);
        return this;
    }

    /**
     * Sets the execution parameters.
     *
     * @param executionParameters the execution parameters
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setExecutionParameters(String executionParameters) {
        this.executionRequest.setExecutionParameters(executionParameters);
        return this;
    }

    /**
     * Previously known as setRunAsUserMode...
     * @param submitAsUserMode flag indicating whether we want run as user or run as navplus
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setSubmitAsUserMode(boolean submitAsUserMode) {
        this.executionRequest.setSubmitAsUserMode(submitAsUserMode);
        return this;
    }

    /**
     * Sets the execution file.
     *
     * @param executionFile the execution file
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setExecutionFile(String executionFile) {
        this.executionRequest.setExecutionFile(executionFile);
        return this;
    }

    /**
     * Sets execution type.
     *
     * @param executionType the execution type
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setExecutionType(String executionType) {
        this.executionRequest.setType(executionType);
        return this;
    }

    /**
     * Sets request id.
     *
     * @param requestId the request id
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setRequestId(String requestId) {
        this.executionRequest.setRequestId(requestId);
        return this;
    }

    /**
     * Sets the script preamble.
     * 
     * @param gridHostPreamble the grid preamble as a string.
     * @return the execution request builder
     * 
     */
    public ExecutionRequestBuilder setSubmitHostPreamble(String submitHostPreamble) {
        // FIXME should use preconditions, instead of letting set null value 
        if (submitHostPreamble == null) {
            submitHostPreamble = "";
        }
        this.executionRequest.setSubmitHostPreamble(submitHostPreamble);
        return this;
    }

    /**
     * Sets the grid preamble.
     * 
     * @param gridHostPreamble the grid preamble as a string.
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setGridHostPreamble(String gridHostPreamble) {
        // FIXME should use preconditions, instead of letting set null value
        if (gridHostPreamble == null) {
            gridHostPreamble = "";
        }
        this.executionRequest.setGridHostPreamble(gridHostPreamble);
        return this;
    }

    /**
     * Add a name/value pair to the request attributes.
     * @param name The (shell) variable name
     * @param value The value
     * @return the execution request builder
     */
    public ExecutionRequestBuilder addAttribute(String name, String value) {
        this.executionRequest.addRequestAttribute(name, value);
        return this;
    }

    /**
     * Sets the request attributes. It cleans up any existing request attributes.
     * 
     * @param requestAttributes the map of Name/Value pairs that are the request attributes.
     * @return the execution request builder
     */
    public ExecutionRequestBuilder setRequestAttributes(Map<String, String> requestAttributes) {
        this.executionRequest.setRequestAttributes(requestAttributes);
        return this;
    }

    /**
     * Builds execution request message.
     *
     * @return the execution request msg
     * @throws JAXBException the jAXB exception
     * @throws IllegalStateException if type, request id, ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE or ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE request attributes are not set
     * 
     * FIXME refactor so it throws {@link IllegalStateException} instead of JAXBException - this exception is implementation-specific
     */
    public String getExecutionRequestMsg() throws JAXBException {
        Preconditions.checkState(StringUtils.isNotEmpty(executionRequest.getType()), "Execution Request type not set or is empty.");
        Preconditions.checkState(StringUtils.isNotEmpty(executionRequest.getRequestId()), "Execution Request ID not set or is empty.");
        Preconditions.checkState(StringUtils.isNotEmpty(executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName())), "Execution Host Fileshare path not set");
        Preconditions.checkState(StringUtils.isNotEmpty(executionRequest.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName())), "Execution Host Remote Fileshare path not set");
        return JAXBUtils.marshall(executionRequest, ExecutionJaxbUtils.CONTEXT_CLASSES);
    }

    /**
     * Builds execution request message. The returned Execution Request instance is different than the one internally used.
     *
     * @return the execution request
     * @throws JAXBException the jAXB exception
     * @throws IllegalStateException if type, request id, ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE or ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE request attributes are not set
     */
    public ExecutionRequest getExecutionRequest() {
        try {
            return JAXBUtils.unmarshall(getExecutionRequestMsg(), ExecutionJaxbUtils.CONTEXT_CLASSES);
        } catch (JAXBException ex) {
            throw new IllegalStateException("Could not build an execution request", ex);
        }
    }
}
