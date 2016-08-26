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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;

/**
 * Provides simple stats on the running of the MIF services. 
 */
@Path("/statistics")
public interface StatisticsService {

    @GET        
    @Path("/connectors")
    @Produces({ MediaType.TEXT_HTML })
    @Descriptions({ @Description("Provide a list of statistics for clients") })
    String getStatistics();
    
    @GET        
    @Path("/heapdump")
    @Produces({ MediaType.TEXT_HTML })
    @Descriptions({ @Description("Perform a heap dump") })
    String getHeapDump();
}
