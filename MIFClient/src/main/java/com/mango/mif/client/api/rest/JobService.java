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
package com.mango.mif.client.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;

import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.exception.MIFException;

/**
 * REST Service interface for making calls to MIFServer web service. CXF
 * configuration in Spring allows us to use this interface to create a proxy
 * bean to the RESTful webservice implementation.
 * 
 * Convention:
 *      url segments lower cased and spaces represented with '-'
 * 
 * Design:
 * 
 * Main JobService
 *  jobservice - root url
 *  jobservice/jobs/{url/part/describing/action}/{jobId} - Identifies the action, 
 *  jobservice/jobs/info/{property}/{jobId} - returns a given property
 *      e.g. jobservice/jobs/info/detailed-status/{jobId} - returns a detailed status of a given job
 *  jobservice/jobs/command/{command}/{jobId}?{extra-parameters} - performs a given operation on a specified job
 *      e.g. jobservice/jobs/command/cancel
 *  jobservice/jobs/info/{named-query}?{parameters} - a named query and its parameters
 *      e.g. jobservice/jobs/info/summary-messages?status=running
 *  jobservice/jobs/acknowlaged?date={date}
 * 
 */
@Path("/jobservice")
public interface JobService {
    
    @GET        
    @Produces({ MediaType.TEXT_XML})
    @Descriptions({ @Description("Provide an index in XML format for help documentation on XML schema for job service") })
    String indextext();
    
    @GET        
    @Produces({ MediaType.TEXT_HTML})
    @Descriptions({ @Description("Provide an index in HTML format for help documentation on XML schema for job service") })
    String indexweb();
    
    @POST
    @Path("/execute")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    @Descriptions({ @Description("Submit an execution request for a job") })
    String execute(@FormParam("executionRequest") String executionRequest) throws MIFException;
    /**
     * Retrieves job status
     * @param jobId
     * @return one of {@link com.mango.mif.domain.JobStatus}
     */
    @GET
    @Path("/jobs/info/status/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the status for a job given the job id") })
    String getJobStatus(@PathParam("jobId") String jobId);

    /**
     * Retrieves a list of running jobs (i.e. not COMPLETED, CANCELLED, FAILED)
     * @return
     */
    @GET
    @Path("/jobs/info/summary-messages/running")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the summary messages for ALL running jobs") })
    JobSummaryMessages getRunningJobsSummaryMessages();

    /**
     * Retrieves a detailed status of a given job
     * @param jobId
     * @return
     * @throws MIFException if job does not exist, there was a problem with detailed status format or detailed status does not exist
     */
    @GET
    @Path("/jobs/info/detailed-status/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the detailed status for a job given the job id") })
    DetailedStatus getDetailedStatus(@PathParam("jobId") String jobId) throws MIFException;
    

    /**
     * Responsible for retrieving execution response for the given job.
     * @param jobId
     * @return execution response of a job
     * @throws MIFException if execution response does not exist or job for the given id does not exist
     */
    @GET
    @Path("/jobs/info/execution-response/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the execution response for a job given the job id") })
    ExecutionResponse getExecutionResponse(@PathParam("jobId") String jobId) throws MIFException;


    /**
    * Retrieve all jobs marked as acknowledged upto the date specified but after specified 
    * date minus a configurable number of days.
    * @return String[] the job ids
    * @param dateTo long value representing the linux date upto which we look for
    * acknowledged jobs
    */
    @GET
    @Path("/jobs/info/acknowledged/{dateTo}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Retrieves acknowledged jobs from dateTo -n days up to the dateTo") })
    String[] getAcknowledgedJobsByDate( @PathParam("dateTo") Long dateTo);  
    
    /**
     * Retrieves a list of job's files under given location
     * @param jobId
     * @param endPath
     * @return
     * @throws MIFException if a job does not exist or there is a problem with retrieving files
     */
	@GET
	@Path("/jobs/info/dir-listing/{jobId}/{endPath:.*}")
	@Produces({ MediaType.APPLICATION_JSON,  MediaType.TEXT_XML })
	@Descriptions({ @Description("List a directory given a job id and the path") })
	FileList listDir(@PathParam("jobId") String jobId, @PathParam("endPath") String endPath) throws MIFException;
	
	/**
     * Retrieves the grid job directory
     * @param jobId
     * @return
     */
    @GET
    @Path("/jobs/info/grid-job-dir/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the directory for a job on the grid share.") })
    String getJobDirectory(@PathParam("jobId") String jobId);

	/**
     * Retrieves tail of a given job's file
	 * @param jobId
	 * @param endPath
     *            the rest of the path to the file to be returned.
	 * @return
	 * @throws MIFException if a job does not exist or there is a problem with retrieving file content
	 */
	@GET
	@Path("/jobs/info/file-tail/{jobId}/{endPath:.*}")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_XML })
	@Descriptions({ @Description("Tail a file contents given the job id and the path to the file") })
	byte[] getTailedFileContents(@PathParam("jobId") String jobId,
			@PathParam("endPath") String endPath) throws MIFException;

    /**
     * Retrieves full file contents of a given job's file
     * @param jobId
     * @param endPath
     *            full path to the file to be returned.
     * @return
     * @throws MIFException if a job does not exist or there is a problem with retrieving file content
     */
	@GET
	@Path("/jobs/info/file-content/{jobId}/{endPath:.*}")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_XML })
	@Descriptions({ @Description("Retrieve a file contents given the job id and the path to the file") })
	byte[] getFullFileContents(@PathParam("jobId") String jobId, @PathParam("endPath") String endPath)
			throws MIFException;
    

    /**
     * Cancels a given job
     * @param jobId
     * @return mif response object with SUCCESS status if request was successful, with FAILED status otherwise.
     */
    @POST
    @Path("/jobs/command/cancel/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Cancels a given job") })
    MIFResponse cancel(@PathParam("jobId") String jobId);
    
    
}
