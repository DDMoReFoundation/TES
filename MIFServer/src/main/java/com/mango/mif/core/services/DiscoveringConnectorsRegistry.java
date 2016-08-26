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
package com.mango.mif.core.services;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mango.mif.connector.Connector;
import com.mango.mif.connector.ConnectorDescriptor;

/**
 * A Connector Registry implementation that discovers {@link Connector}s using their {@link ConnectorDescriptor}s.
  */
public class DiscoveringConnectorsRegistry implements ConnectorsRegistry {
    
    @Autowired(required=false) 
    private Collection<ConnectorDescriptor> connectorDescriptors;
    
    public Collection<ConnectorDescriptor> getConnectorDescriptors() {
        return this.connectorDescriptors;
    }
    
    public void setConnectorDescriptors(Collection<ConnectorDescriptor> connectorDescriptors) {
        this.connectorDescriptors = connectorDescriptors;
    }

    @Override
    public Collection<Connector> getConnectors() {
    
        return Collections2.transform(this.connectorDescriptors, new Function<ConnectorDescriptor, Connector>() {

            @Override
            public Connector apply(ConnectorDescriptor descriptor) {
                return descriptor.getConnector();
            }

        });
    }
    
    @Override
    public Collection<String> getExecutionTypes() {
        final Collection<String> result = Lists.newArrayList();
        for (final ConnectorDescriptor connectorDescriptor : this.connectorDescriptors) {
            result.addAll(connectorDescriptor.getExecutionTypes());
        }
        return result;
    }
    
    @Override
    public Connector getConnectorByExecutionType(final String executionType) {
    	return getConnectorBy(new ByExecutionTypePredicate(executionType),
    			"Found %d connectors supporting execution type %s. An execution type must uniquely identify a connector.");
    }

    @Override
    public Connector getConnectorById(final String connectorId) {
    	return getConnectorBy(new ByConnectorIdPredicate(connectorId),
    			"Found %d connectors with Id %s. Connector IDs must be unique.");
    }
    
    private Connector getConnectorBy(final Predicate<ConnectorDescriptor> predicate, final String multipleMatchesExceptionStringFormat) {
        final Collection<ConnectorDescriptor> result =  Collections2.filter(this.connectorDescriptors, predicate);
        
        if (result.isEmpty()) {
            return null;
        } else if (result.size() > 1) {
            throw new IllegalStateException(String.format(multipleMatchesExceptionStringFormat, result.size(), predicate.toString()));
        } else {
            return Iterables.getLast(result).getConnector();
        }
    }
    
    /**
     * Match {@link ConnectorDescriptor}s on executionType.
     */
    private class ByExecutionTypePredicate implements Predicate<ConnectorDescriptor> {
    	
        private String executionType;

		ByExecutionTypePredicate(final String executionType) {
	        this.executionType = executionType;
        }

        @Override
        public boolean apply(ConnectorDescriptor descriptor) {
            return descriptor.getExecutionTypes().contains(this.executionType);
        }
        
        @Override
        public String toString() {
            return this.executionType;
        }
    }
    
    /**
     * Match {@link ConnectorDescriptor}s on connectorId of their {@link Connector}s.
     */
    private class ByConnectorIdPredicate implements Predicate<ConnectorDescriptor> {
    	
        private String connectorId;

		ByConnectorIdPredicate(final String connectorId) {
	        this.connectorId = connectorId;
        }

        @Override
        public boolean apply(ConnectorDescriptor descriptor) {
            return descriptor.getConnector().getConnectorId().equals(this.connectorId);
        }
        
        @Override
        public String toString() {
            return this.connectorId;
        }
    }

}
