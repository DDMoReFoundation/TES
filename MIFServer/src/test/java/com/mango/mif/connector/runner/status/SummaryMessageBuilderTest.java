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
package com.mango.mif.connector.runner.status;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.ce.CeInvoker;
import com.mango.mif.core.exec.template.FreemarkerConfigurationProvider;
import com.mango.mif.core.exec.template.FreemarkerConfigurationProviderFactory;
import com.mango.mif.core.exec.template.JobAwareFreemarkerTemplateCommandBuilder;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.MIFTestProperties;
import com.mango.mif.utils.SystemPropertiesPreservingTestStub;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.TestsHelper;

/**
 * A super class containg common settup for all message builder tests
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class SummaryMessageBuilderTest extends SystemPropertiesPreservingTestStub {

    private static final Logger LOG = Logger.getLogger(SummaryMessageBuilderTest.class);
    private Invoker invoker;
    /**
     * common scripts location
     */
    private String commonScripts;
    /**
     * generic scripts location
     */
    private String genericScripts;
    /**
     * MIF core templates directory
     */
    private String templatesDirectory;
    /**
     * Job
     */
    @Mock
    private Job job;
    private JobAwareFreemarkerTemplateCommandBuilder textBuilder;
    private static final String[] EMPTY_ARRAY = new String[0];

    private void loadMIFProperties() throws IOException {
        Properties properties = TestsHelper.createPropertiesFromStream(this.getClass().getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));
        MIFProperties.loadProperties(properties);
    }
    
    @Before
    public void setUp() throws Exception {
        TestPropertiesConfigurator.initProperties();
        
        loadMIFProperties();

        templatesDirectory = System.getProperty(MIFTestProperties.TEMPLATES_DIRECTORY_LOCATION);
        commonScripts = System.getProperty(MIFTestProperties.MIF_COMMON_SCRIPTS_LOCATION);
        genericScripts = System.getProperty(MIFTestProperties.MIF_GENERIC_SCRIPTS_LOCATION);

        Preconditions.checkNotNull(commonScripts, MIFTestProperties.MIF_COMMON_SCRIPTS_LOCATION + " property is not set.");
        Preconditions.checkNotNull(genericScripts, MIFTestProperties.MIF_GENERIC_SCRIPTS_LOCATION + " property is not set.");
        Preconditions.checkNotNull(templatesDirectory, MIFTestProperties.TEMPLATES_DIRECTORY_LOCATION + " property is not set.");

        invoker = new CeInvoker();
        
        LOG.debug(String.format("MIF templates directory %s", templatesDirectory));
        LOG.debug(String.format("Common scripts directory %s", commonScripts));
        LOG.debug(String.format("Generic scripts directory %s", genericScripts));
    }

    
    protected void prepareJobMock(String testData, String ctlFileName, String excecutionParameters) {
        File testDataDir = FileUtils.toFile(SummaryMessageBuilderTest.class.getResource(testData));
        ExecutionRequest executionRequest = mock(ExecutionRequest.class);
        when(executionRequest.getExecutionFile()).thenReturn(ctlFileName);
        when(executionRequest.getExecutionParameters()).thenReturn(excecutionParameters);
        Map<String,String> requestAttributes = Maps.newHashMap();
        when(executionRequest.getRequestAttributes()).thenReturn(requestAttributes);
        
        Job job = getJob();
        when(job.getExecutionRequest()).thenReturn(executionRequest);
        when(job.getJobDirectory()).thenReturn(testDataDir);
        when(job.getJobWorkingDirectory()).thenReturn(new File(testDataDir,ctlFileName).getParentFile());
        when(job.getExecutionFile()).thenReturn(ctlFileName);
        when(job.getExecutionFileName()).thenReturn(ctlFileName);
        when(job.hasExecutionFile()).thenReturn(true);
        when(job.getJobExecutionFilePath()).thenReturn(new File(testDataDir,ctlFileName));
        when(job.getRequestUserName()).thenReturn("REQUEST_USER_NAME");
    }
    
    protected Map<String, Object> createCommandBuilderContext() {
        HashMap<String, Object> initialContext = Maps.newHashMap();
        initialContext.put("CONNECTOR_UTILS", genericScripts);
        initialContext.put("MANGO_UTILS", commonScripts);
        return initialContext;
    }

    protected JobAwareFreemarkerTemplateCommandBuilder createCommandBuilder(Map<String, Object> initialContext, FreemarkerConfigurationProvider freemarkerConfigurationProvider) throws ExecutionException {
        JobAwareFreemarkerTemplateCommandBuilder textBuilder = new JobAwareFreemarkerTemplateCommandBuilder(initialContext);
        textBuilder.setFreemarkerConfigurationProvider(freemarkerConfigurationProvider);
        return textBuilder;
    }

    protected FreemarkerConfigurationProvider createConfigurationProvider() {
        List<String> templates = getTemplatesLocations();
        return FreemarkerConfigurationProviderFactory.createFreemarkerConfigurationProvider(templates.toArray(EMPTY_ARRAY));
    }

    protected List<String> getTemplatesLocations() {
        return Lists.newArrayList(templatesDirectory);
    }
    
    public JobAwareFreemarkerTemplateCommandBuilder getCommandBuilder() {
        if(this.textBuilder==null) {
            try {
                this.textBuilder = createCommandBuilder(createCommandBuilderContext(),createConfigurationProvider());
            } catch (ExecutionException e) {
                throw new RuntimeException("Could not build command builder for testing.",e);
            }
        }
        return textBuilder;
    }
    
    public Invoker getInvoker() {
        return invoker;
    }
    
    public Job getJob() {
        return job;
    }
    
}