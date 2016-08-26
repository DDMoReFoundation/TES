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
package com.mango.mif;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.domain.ClientAvailableConnectorDetails;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * MIFHttpRestClient for interacting with MIFServer REST services.
 */
public class MIFHttpRestClient {
    private static final Logger log = Logger.getLogger(MIFHttpRestClient.class);
    private String endPoint;
    private HttpClient client;

    /**
     * Constructor.
     * @param endPoint - MIF endpoint
     */
    public MIFHttpRestClient(final String endPoint) {
        this.endPoint = endPoint;
        client = new HttpClient();
    }
    
    /**
     * Discover the Client-Available Connector Details for all Connectors exposed by MIF.
     * <p>e
     * @return Map of executionType to {@link ClientAvailableConnectorDetails}
     */
    public Map<String, ClientAvailableConnectorDetails> getClientAvailableConnectorDetails() {

    	// Build the end-point and execute the request to obtain all supported execution types
    	final String endpoint = buildEndpoint("jobconnectorregistry");
    	final GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.APPLICATION_JSON);
        final String returnedJSON = executeMethod(endpoint, get);
        
        // Deserialise the returned JSON
        List<String> executionTypes;
        try {
        	executionTypes = new ObjectMapper().readValue(returnedJSON, List.class);
        } catch (final Exception e) {
        	throw new RuntimeException("Unable to deserialise JSON representation of executionTypes list", e);
        }
        
        // Process the executionTypes, firing off separate requests to fetch the details for each Connector
        final Map<String, ClientAvailableConnectorDetails> clientAvailableConnectorDetails = new HashMap<String, ClientAvailableConnectorDetails>();
        for (final String executionType : executionTypes) {
    		clientAvailableConnectorDetails.put(executionType, getClientAvailableConnectorDetails(executionType));
        }
        return clientAvailableConnectorDetails;
    }
    
    /**
     * Discover the Client-Available Connector Details for a particular Connector exposed by MIF.
     * <p>
     * @param executionType - associated with a Connector
     * @return a {@link ClientAvailableConnectorDetails}
     * @throws IllegalStateException if no Connector providing the specified executionType exists
     */
    public ClientAvailableConnectorDetails getClientAvailableConnectorDetails(final String executionType) {
    
    	// Build the end-point and execute the request to obtain the Client-Available Connector Details
        final String endpoint = buildEndpoint("jobconnectorregistry", executionType);
        final GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.APPLICATION_JSON);                      
        final String returnedJSON = executeMethod(endpoint, get);
        
        // Deserialise the returned JSON
        try {
        	return new ObjectMapper().readValue(returnedJSON, ClientAvailableConnectorDetails.class);
        } catch (final Exception e) {
        	throw new RuntimeException("Unable to deserialise JSON representation of ClientAvailableConnectorDetails superset of CommandExecutionTarget", e);
        }
    }

    /**
     * Submit an execution request to MIF.
     * <p>
     * @param {@link {@link ExecutionRequest} containing all the parameters that determine a job for MIF to execute
     * @return job id 
     */
    public String executeJob(ExecutionRequest jobRequest) {
        
        String endpoint = buildEndpoint("jobservice", "execute");
        PostMethod post = new PostMethod(endpoint);
        
        String xml;
        try {
            xml = JAXBUtils.marshall(jobRequest, ExecutionJaxbUtils.CONTEXT_CLASSES);
        } catch (JAXBException e1) {
            throw new RuntimeException("Could not marshall execution request",e1);
        }
        post.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);        
        post.setParameter("executionRequest", xml);
        
        log.info("Sending execution request: " + jobRequest.getRequestId() 
            + " type " + jobRequest.getType() 
            + " for " + jobRequest.getUserName());
        
        return executeMethod(endpoint, post);
    }

    private String executeMethod(String endpoint, HttpMethod method) {
        int status = 0;
        String result = null;
        try {
            status = client.executeMethod(method);
            result = method.getResponseBodyAsString();
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Could not connect to server %s.",endpoint),e);
        }
        
        if(status!=200||result==null) {
            throw new IllegalStateException(String.format("Unexpected response from MIF: %s", result));
        }
        return result;
    }

    /**
     * Check status of a job.
     * Retrieve current job status from MIF
     * @param job id
     */
    public String checkStatus(String jobId) {
        String endpoint = buildEndpoint("jobservice", "jobs", "info", "status", jobId);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);                      
        return executeMethod(endpoint,get);
    }

    /**
     * Retrieves execution response from MIF
     * @param jobId
     */
    public String getExecutionResponse(String jobId) {
        String endpoint = buildEndpoint("jobservice", "jobs", "info", "execution-response", jobId);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);                      
        return executeMethod(endpoint,get);
    }
    
    /**
     * Cancel a job.
     * @param jobId - job id
     */
    public MIFResponse cancel(String jobId) {
        Preconditions.checkNotNull(jobId, "Job ID can't be null");
        String endpoint = buildEndpoint("jobservice", "jobs", "command", "cancel", jobId);
        PostMethod post = new PostMethod(endpoint);
        post.addRequestHeader("accept", MediaType.APPLICATION_JSON);
        String response = executeMethod(endpoint,post);
        try {
            return new ObjectMapper().readValue(response, MIFResponse.class);
        } catch (final Exception e) {
            throw new RuntimeException("Unable to deserialise JSON representation of MIFResponse.", e);
        }
    }

    /**
     * kills MIF
     */
    public String killMif() {
        String endpoint = buildEndpoint("cs", "stop");
        PostMethod post = new PostMethod(endpoint);
        post.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        return executeMethod(endpoint,post);
    }

    /**
     * Checks the health of MIF instance
     * @return true if healthy
     */
    public boolean healthcheck() {
        String endpoint = buildEndpoint("cs", "healthcheck");
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        try {
            return "OK".equalsIgnoreCase(executeMethod(endpoint,get));
        } catch(Exception e) {
            log.error("Error when trying to check the health of the service",e);
            return false;
        }
    }
    
    private String buildEndpoint(String...parts) {
        StringBuilder builder = new StringBuilder();
        builder.append(endPoint);
        builder.append(Joiner.on("/").join(parts));
        return builder.toString();
    }
}
