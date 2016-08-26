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

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.mango.mif.client.api.rest.ConnectorService;


/**
 * A component responsible for discovering connector-specific services
 */
public class ServiceBeansProvider {
    
    private final static Logger LOG = Logger.getLogger(ServiceBeansProvider.class);
    
    private List<Object> mifCoreServiceBeans;
    
    @Autowired(required=false)
    private Collection<ConnectorService> connectorServices;
    
    
    public ServiceBeansProvider(List<Object> mifCoreServiceBeans) {
        this.mifCoreServiceBeans = mifCoreServiceBeans;
    }
    

    public synchronized void setConnectorServices(Collection<ConnectorService> connectorServices) {
        this.connectorServices = connectorServices;
    }
    
    public synchronized Collection<ConnectorService> getConnectorServices() {
        if(connectorServices==null) {
            LOG.warn("No additional connector REST services were discovered");
            connectorServices = Lists.newArrayList();
        }
        return connectorServices;
    }
    /**
     * 
     * @return a list containing MIF core service beans and connector services
     */
    public List<Object> createServiceBeans() {
        List<Object> result = Lists.newArrayList();
        result.addAll(mifCoreServiceBeans);
        result.addAll(getConnectorServices());
        return result;
    }
}
