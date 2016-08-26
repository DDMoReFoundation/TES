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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.Charsets;
import org.apache.cxf.helpers.IOUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.utils.jaxb.XmlGenericMapAdapter;

/**
 * Generates the execution request and response schema.
 */
public class MIFSchemaGen {
    
    private static final Logger LOG = Logger.getLogger(MIFSchemaGen.class);
    
    private static final MIFSchemaGen schemaGen;
    
    static {
        schemaGen = new MIFSchemaGen();
    }
    
    private MIFSchemaGen() {
        
    }       
    
    /**
     * Generate the XML schema for execution requests and responses. 
     */
    public String generateSchema() throws JAXBException, IOException {
        
        JAXBContext context = JAXBContext.newInstance(ExecutionRequest.class, 
            ExecutionResponse.class, 
            XmlGenericMapAdapter.class);
        
        IOStreamResolver resolver = new IOStreamResolver();
        context.generateSchema(resolver);
        return resolver.getOutput();
    }      
    
    public static MIFSchemaGen getInstance() {
        return schemaGen;
    }
    
    /**
     * JAXB resolver for schema output.
     */
    class IOStreamResolver extends SchemaOutputResolver {
        
        private File tempFile;
        
        public IOStreamResolver() {            
        }
        
        public Result createOutput( String namespaceUri, String suggestedFileName ) throws IOException {
            tempFile = new File(Files.createTempDir(), "mifschemagen" + System.currentTimeMillis());
            return new StreamResult(tempFile);
        }
        
        public String getOutput() {
            try {
                return IOUtils.toString(new FileInputStream(tempFile), Charsets.UTF_8.name());
            } catch (FileNotFoundException e) {
                LOG.error("Exception thrown ", e);
            } catch (IOException e) {
                LOG.error("Exception thrown ", e);
            }
            return "";
        }
    }
}
