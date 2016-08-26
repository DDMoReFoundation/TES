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
package com.mango.mif.psnbootstrap.gridengine.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;

import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.psnbootstrap.gridengine.domain.PsNBootstrapProcessingDetailedStatus;


/**
 * REST service exposed by PsN Bootstrap connector
 */
@Path("/gepbc")
public interface ConnectorService extends com.mango.mif.client.api.rest.ConnectorService {
    /**
     * Retrieves detailed status of PsN Bootstrap job 
     * @param jobId
     * @return
     */
    @GET
    @Path("/jobs/info/detailed-status/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the bootstrap detailed status for a job given the job id") })
    PsNBootstrapProcessingDetailedStatus getPsNBootstrapDetailedStatus(@PathParam("jobId") String jobId);

    
    /**
     * Kills a given bootstrap job's child jobs 
     * @param jobId
     * @param sgeJobIds list of bootstrap child jobs
     * @return
     */
    @POST
    @Path("/jobs/command/delete-child-jobs/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Kill a bootstrap child given the childs job id and the Sun Grid Engine job ids") })
    MIFResponse killPsNBootstrapChildSGEJobs(@PathParam("jobId") String jobId, @MatrixParam("sgeJobId") List<String> sgeJobIds);
    
}
