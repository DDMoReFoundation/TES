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
package com.mango.mif.nonmem.gridengine.rest;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;

import com.mango.mif.client.api.rest.IndividualProgessResponse;
import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.exception.MIFException;
import com.mango.mif.nonmem.gridengine.domain.NONMEMProcessingDetailedStatus;
import com.mango.mif.nonmem.gridengine.domain.NmMetrics;

/**
 * REST Service interface exposing NONMEM specific actions in the MIFServer web service. CXF
 * configuration in Spring allows us to use this interface to create a proxy
 * bean to the RESTful web service implementation.
 * 
 */
@Path("/genc")
public interface ConnectorService extends com.mango.mif.client.api.rest.ConnectorService {
    /**
     * A file created in NONMEM working directory to stop the processing
     */
    String STOP_FILE_NAME = "stop.sig";
    /**
     * A file created in NONMEM working directory to skip the current estimation
     */
    String SKIP_FILE_NAME = "next.sig";
    
    /**
     * a file created in NONMEM working directory to trigger individual estimation progress.
     */
    String ESTIMATION_PROGRESS_FILE_NAME = "subject.sig";
	/**
	 * Will attempt to gracefully halt execution of the job referenced by Id 
	 * by creating an appropriate signal file in the jobs current working directory
	 * 
	 * NOTE: This is a NONMEM 7.2 feature and will not work in versions below
	 * 
	 * Will not check whether the job is still running
	 * Will return error responses if
	 * 
	 * <ul>
	 * 		<li>the job does not exist</li>
	 * 		<li>the job has no working directory</li>
	 * 		<li>no id is provided</li>
	 * 		<li>the signal file cannot be created</li>
	 * </ul>
	 * 
	 * @param jobId the Id of the job in question
	 * @return
	 * @throws MIFException
	 */
	@GET
    @Path("/jobs/command/stop/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Gracefully stop the job referenced by id using a signal file") })
	MIFResponse stop(@PathParam("jobId") String jobId) throws MIFException;
	

    /**
     * Will attempt to skip the current estimation step 
     * by creating an appropriate signal file in the jobs current working directory
     * 
     * NOTE: This is a NONMEM 7.2 feature and will not work in versions below
     * 
     * Will not check whether the job is still running
     * Will return error responses if
     * 
     * <ul>
     *      <li>the job does not exist</li>
     *      <li>the job has no working directory</li>
     *      <li>no id is provided</li>
     *      <li>the signal file cannot be created</li>
     * </ul>
     * 
     * @param jobId the Id of the job in question
     * @return
     * @throws MIFException
     */
    @POST
    @Path("/jobs/command/skip-estimation/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Skip the current estimation by using a signal file") })
    MIFResponse skipEstimation(@PathParam("jobId") String jobId) throws MIFException;
    
    /**
     * Individual estimation progress is monitored once signal file, subject.sig is put in place.
     * This method will return some part of result back to caller and caller can poll to this method to get more results.
     * NOTE: This is a NONMEM 7.2 feature and will not work in versions below
     * 
     * @param jobId the Id of the job in question
     * @return
     * @throws MIFException
     */
    @POST
    @Path("/jobs/command/individual-progress/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("return individual estimation progress by using a signal file") })
    IndividualProgessResponse getIndividualEstimationProgress(@PathParam("jobId") String jobId) throws MIFException;
    
    /**
     * The individual estimation is triggered by signal file placed in appropriate place and stopped by removing the same signal file.
     * the boolean flag passed to this method determines whether to enable or disable individual estimation for the given job id.
     * NOTE: This is a NONMEM 7.2 feature and will not work in versions below
     * 
     * @param jobId the Id of the job in question
     * @param enableIndividualProgress
     * @return
     * @throws MIFException
     */
    @POST
    @Path("/jobs/command/monitor-individual/{jobId}/{enableIndividualProgress}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("output individual estimation progress by using a signal file") })
    MIFResponse toggleIndividualProgressMonitor(@PathParam("jobId") String jobId, @PathParam("enableIndividualProgress")boolean enableIndividualProgress) throws MIFException;    
    

    /**
     * Collects nonmem job metrics. 
     * @param jobId
     * @param selectedGraphs
     * @param metricsValueCount 0 indicates no limitation
     * @return nonmem metrics
     */
    @GET
    @Path("/jobs/info/metrics/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Retrieves NONMEM metrics given a job id") })
    NmMetrics getNONMEMMetrics( @PathParam("jobId") String jobId, @QueryParam("selectedGraphs") List<String> selectedGraphs, @DefaultValue("0") @QueryParam("metricsValueCount") int metricsValueCount);
    

    /**
     * Retrieves a detailed status specific to NONMEM job.
     * @param jobId
     * @return nonmem detailed status
     * @throws MIFException
     */
    @GET
    @Path("/jobs/info/detailed-status/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the NONMEM detailed status for a job given the job id") })
    NONMEMProcessingDetailedStatus getNONMEMDetailedStatus(@PathParam("jobId") String jobId) throws MIFException;
}
