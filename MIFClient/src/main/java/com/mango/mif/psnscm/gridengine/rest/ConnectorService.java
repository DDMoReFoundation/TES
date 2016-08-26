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
package com.mango.mif.psnscm.gridengine.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;

import com.mango.mif.psnscm.gridengine.domain.PsNSCMProcessingDetailedStatus;


/**
 * REST services exposed by PsN SCM Connector
 */
@Path("/gepsc")
public interface ConnectorService extends com.mango.mif.client.api.rest.ConnectorService {

    /**
     * Retrieves PsN SCM job detailed status
     * @param jobId
     * @return
     */
    @GET
    @Path("/jobs/info/detailed-status/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the SCM detailed status for a job given the job id") })
    PsNSCMProcessingDetailedStatus getPsNSCMDetailedStatus(@PathParam("jobId") String jobId);

    /**
     * Retrieves PsN SCM job log file contents.
     * @param jobId
     * @return
     */
    @GET
    @Path("/jobs/info/scmlog-contents/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, "application/text", MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the log file content for an SCM job given the job id") })
    byte[] getSCMLogFileContent(@PathParam("jobId") String jobId);
    
    /**
     * Retrieves PsN SCM job log file path
     * @param jobId
     * @return
     */
    @GET
    @Path("/jobs/info/scmlog-path/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the log file path for an SCM job given the job id") })
    String getSCMLogFilePath(@PathParam("jobId") String jobId);
}
