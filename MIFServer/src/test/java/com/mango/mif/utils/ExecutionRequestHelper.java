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
package com.mango.mif.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.mango.mif.core.api.ResourceUtils;
import com.mango.mif.core.resource.BaseResourcePublisher;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.exception.MIFException;

/**
 * 
 * A helper class used to prepare execution request builders that are used in tests, it encapsulates all the boilerplate code around publishing files
 */
public class ExecutionRequestHelper {
    /**
     * Creates execution request
     * @param requestID 
     * @return
     * @throws JAXBException 
     * @throws MIFException 
     */
    public static ExecutionRequestBuilder createExecutionRequestBuilderForInputDirectory(BaseResourcePublisher resourcePublisher, File dataDirectory, final String scriptFilePath) throws JAXBException, MIFException {
        ExecutionRequestBuilder requestBuilder = createRequestBuilder();
        List<File> files = ResourceUtils.listFiles(dataDirectory, new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
        String requestID = publishFiles(resourcePublisher, files, dataDirectory);
        requestBuilder.setRequestId(requestID);
        requestBuilder.setExecutionFile(scriptFilePath);
        return requestBuilder;
    }
    /**
     * Creates execution request
     * @param requestID 
     * @return
     * @throws JAXBException 
     * @throws MIFException 
     */
    public static ExecutionRequestBuilder createNONMEMExecutionRequestBuilderForInputDirectory(BaseResourcePublisher resourcePublisher, File dataDirectory) throws JAXBException, MIFException {
        ExecutionRequestBuilder requestBuilder = createRequestBuilder();
        List<File> files = ResourceUtils.listFiles(dataDirectory, new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
        String requestID = publishFiles(resourcePublisher, files, dataDirectory);
        requestBuilder.setRequestId(requestID);
        Iterable<File> scriptFiles = Iterables.filter(Arrays.asList(dataDirectory.listFiles()), new Predicate<File> () {

            @Override
            public boolean apply(File file) {
                return file.getName().toLowerCase().endsWith(".ctl");
            }
            
        });
        File scriptFile = scriptFiles.iterator().next();
        requestBuilder.setExecutionFile(scriptFile.getName());
        return requestBuilder;
    }
    /**
     * Creates request builder
     * @return
     */
    private static ExecutionRequestBuilder createRequestBuilder() {
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder();
        return requestBuilder;
    }
    /**
     * Creates request input parameters
     * @return
     * @throws MIFException 
     */
    private static String publishFiles(BaseResourcePublisher publisher, List<File> files, File rootDir) throws MIFException {
        publisher.addFiles(files);
        return publisher.publish();
    }
}
