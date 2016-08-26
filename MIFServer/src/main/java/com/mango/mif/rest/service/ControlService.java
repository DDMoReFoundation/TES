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
package com.mango.mif.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Kills process running MIF. Not for production, never commit this!
 */
@Service
@Path("/cs")
public class ControlService {
    
    private static final Logger LOG = Logger.getLogger(ControlService.class);
    
    private static final long SHUTDOWN_DELAY = 5000;
    
    @Autowired
    private ShutdownTask shutdownTask;
    
    @POST
    @Path("/stop")
    @Descriptions({ @Description("Kills process running MIF") })
    public String stop() {
        shutdownTask.run(SHUTDOWN_DELAY);
        LOG.warn(String.format("Shut down procedure has been triggered"));
        return "OK";
    }
    @GET
    @Path("/healthcheck")
    @Descriptions({ @Description("Healthcheck of MIF instance") })
    public String healthcheck() {
        return "OK";
    }
    
    public void setShutdownTask(ShutdownTask shutdownTask) {
        this.shutdownTask = shutdownTask;
    }
    
    public ShutdownTask getShutdownTask() {
        return shutdownTask;
    }
}
