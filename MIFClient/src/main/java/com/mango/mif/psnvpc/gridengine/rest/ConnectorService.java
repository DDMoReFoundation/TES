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
package com.mango.mif.psnvpc.gridengine.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;

import com.mango.mif.exception.MIFException;
import com.mango.mif.psnvpc.gridengine.domain.PsNVPCProcessingDetailedStatus;


/**
 * REST service exposed by PsN VPC connector
 */
@Path("/gepvc")
public interface ConnectorService extends com.mango.mif.client.api.rest.ConnectorService {
    /**
     * Retrieves PsN VPC job detailed status
     * @param jobId
     * @return
     * @throws MIFException if job does not exist or there is an error when retrieving detailed status
     */
    @GET
    @Path("/jobs/info/detailed-status/{jobId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Descriptions({ @Description("Get the VPC detailed status for a job given the job id") })
    PsNVPCProcessingDetailedStatus getPsNVPCDetailedStatus(@PathParam("jobId") String jobId) throws MIFException;
}
