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
package com.mango.mif.core.exec.template;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;


/**
 * Default implementation of {@link FreemarkerConfigurationProvider}
 * 
 * creates a freemarker configuration which resolves templates from the list of locations
 */
public class FreemarkerConfigurationProviderImpl implements FreemarkerConfigurationProvider {
    private static final Logger LOG = Logger.getLogger(FreemarkerConfigurationProviderImpl.class);
    private final Configuration configuration;
    
    public FreemarkerConfigurationProviderImpl() {
        this.configuration = new Configuration();
    }
    
    public FreemarkerConfigurationProviderImpl(Configuration configuration) {
        this.configuration = configuration;
    }
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void setTemplateLocations(String... locations) {
        TemplateLoader[] loaders = new TemplateLoader[locations.length];
        for(int i=0; i<locations.length;i++) {
            try {
                loaders[i] = createLoader(locations[i]);
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format("Location %s could not be used as template directory",locations[i]), e);
            }
        }
        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
        configuration.setTemplateLoader(mtl);
    }

    /**
     * @param location string
     * @return a template loader for given location string
     * @throws IOException if the location couldn't be used
     */
    @VisibleForTesting
    TemplateLoader createLoader(String location) throws IOException {
        LOG.debug(String.format("Using templates location %s", location));
        final String classpathProtocol = "classpath:";
        if(location.startsWith(classpathProtocol)) {
            String classpathLocation = StringUtils.removeStart(location,classpathProtocol);
            return new ClassTemplateLoader(getClass(), classpathLocation);
        } else {
            return new FileTemplateLoader(new File(location));
        }
    }

}
