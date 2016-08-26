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

package com.mango.mif.core.resource.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.TestsHelper;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.invoker.InvokerFactory;
import com.mango.mif.core.exec.template.FreemarkerConfigurationProviderFactory;
import com.mango.mif.core.exec.template.FreemarkerTemplateCommandBuilder;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.TestProperties;
import com.mango.mif.utils.TestPropertiesConfigurator;

public class ShellBasedResourcePublisherIntegrationTest {
    private final static Logger LOG = Logger.getLogger(ShellBasedResourcePublisherIntegrationTest.class);

    private FreemarkerTemplateCommandBuilder commandBuilder;

    /**
     * The script preamble.
     */
    private String submitHostPreamble;

    File tmpFileshareDirectory;

    File tmpSourceDirectory;

    private String commonScripts;
    
    private String genericScripts;

    private Invoker invoker;

    private String templatesDirectory;
    
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        TestPropertiesConfigurator.initProperties();

        templatesDirectory = System.getProperty(TestProperties.TEMPLATES_DIRECTORY_LOCATION);
        commonScripts = System.getProperty(TestProperties.MIF_COMMON_SCRIPTS_LOCATION);
        genericScripts = System.getProperty(TestProperties.MIF_GENERIC_SCRIPTS_LOCATION);
        
        UUID uuid = UUID.randomUUID();
        String base = uuid.toString();
        tmpFileshareDirectory = TestsHelper.tmpDirectoryPath(base + "-DEST");
        tmpSourceDirectory = TestsHelper.createTmpDirectory(base + "-SRC");

        commandBuilder = new FreemarkerTemplateCommandBuilder();
        commandBuilder.setFreemarkerConfigurationProvider(FreemarkerConfigurationProviderFactory.createFreemarkerConfigurationProvider(templatesDirectory));
        commandBuilder.setTemplate("generic/publishHandler.ftl");
        commandBuilder.setVariable("CONNECTOR_UTILS", genericScripts);
        commandBuilder.setVariable("MANGO_UTILS", commonScripts);
        
        submitHostPreamble = "# TEST PREAMBLE 1\n# TEST PREAMBLE 2\n# TEST PREAMBLE 3";
        
        invoker = new InvokerFactory().getShellInvoker();
        LOG.info("Resource source:" +tmpSourceDirectory );
        LOG.info("Resource destination:" +tmpFileshareDirectory );
    }

    /**
     * Try to clean up the temporary directories.  Since they were created by another user
     * it is doubtful whether we will be able to do this - hence the ignored exceptions
     */
    @After
    public void tearDown() {
            FileUtils.deleteQuietly(tmpFileshareDirectory);
            FileUtils.deleteQuietly(tmpSourceDirectory);
    }

    @Test
    public void shouldPublishFilesToRequestDir() throws IOException, MIFException {
        String requestID = publishFiles();
        File requestInputDirectory = new File(tmpFileshareDirectory,requestID);
        assertEquals(requestID,requestInputDirectory.getName());
    }

    public String publishFiles() throws IOException, MIFException {
        PublisherParameters parameters = new PublisherParameters();

        Map<String,String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), tmpFileshareDirectory.getAbsolutePath());
        parameters.setRequestAttributes(requestAttributes);
        parameters.setRootDirectory(tmpSourceDirectory);
        parameters.setInvoker(invoker);
        parameters.setSubmitHostPreamble(submitHostPreamble);
        parameters.setCommandBuilder(commandBuilder);

        ShellBasedResourcePublisher publisher = new ShellBasedResourcePublisher(parameters);

        File dirA = new File(tmpSourceDirectory, "A");
        dirA.mkdir();
        File dirB = new File(tmpSourceDirectory, "B");
        dirB.mkdir();
        File dirC = new File(tmpSourceDirectory, "C");
        dirC.mkdir();

        File aFile = new File(dirA, "a.txt");
        FileUtils.writeStringToFile(aFile, "This is test content of a.txt file.");

        File bFile = new File(dirB, "b.txt");
        FileUtils.writeStringToFile(bFile, "This is test content of b.txt file.");

        List<File> files = Lists.newArrayList(aFile, bFile);

        publisher.addFiles(files);

        File requestInputDirectory = null;
        String requestID = null;
        
        requestID = publisher.publish();
        requestInputDirectory = new File(tmpFileshareDirectory,requestID);

        assertTrue(requestInputDirectory != null);
        assertTrue(new File(requestInputDirectory, dirA.getName()).exists());
        assertTrue(new File(requestInputDirectory, dirB.getName()).exists());
        assertTrue(new File(requestInputDirectory, dirC.getName()).exists());
        assertTrue(new File(requestInputDirectory, dirA.getName() + "/" + aFile.getName()).exists());
        assertTrue(new File(requestInputDirectory, dirB.getName() + "/" + bFile.getName()).exists());
        assertTrue(requestID != null);
        
        return requestID;
    }

}
