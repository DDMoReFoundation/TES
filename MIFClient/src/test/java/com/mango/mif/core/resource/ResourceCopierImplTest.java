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
package com.mango.mif.core.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.connector.TestsHelper;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.invoker.InvokerFactory;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.template.FreemarkerConfigurationProviderFactory;
import com.mango.mif.core.exec.template.FreemarkerTemplateCommandBuilder;
import com.mango.mif.utils.TestProperties;
import com.mango.mif.utils.TestPropertiesConfigurator;

/**
 * Tests {$link {@link ResourceCopierImpl}
 */
public class ResourceCopierImplTest {

    private final static Logger LOG = Logger.getLogger(ResourceCopierImplTest.class);
    
    private File sourceDirectory;

    private File sourceSubDirectory1;

    private File sourceHiddenSubDirectory;

    private File targetDirectory;

    private File targetSubDirectory1;

    private File targetHiddenSubDirectory;

    private FreemarkerTemplateCommandBuilder commandBuilder;

    private Invoker invoker;
    private InvokerHelper invokerHelper;

    private ResourceCopierParameters params;

    private boolean topLevelFileAExists;
    private boolean topLevelFileMIFAExists;
    private boolean subLevelFileAExists;
    private boolean subLevelFileBExists;
    private boolean hiddenSubLevelFileAExists;
    private boolean hiddenSubLevelFileMIFBExists;

    private String commonScripts;

    private String genericScripts;

    private String templatesDirectory;
    /**
     * Temporary directories must be created as the user, otherwise things screw up further along the line.
     * @param path The directory to create.
     * @param invoker The invoker to create it with.
     * @return File object containig path of directory.
     * @throws ExecutionException If we sc
     */
    private File createTmpDirectory(String path, InvokerHelper invokerHelper) throws ExecutionException {
        invokerHelper.mkdir(path, 0755);
        return new File(path);
    }

    @After
    public void tearDown() {
        try {
            invokerHelper.rmdir(sourceDirectory.getAbsolutePath());
        } catch (ExecutionException e) {
            LOG.error(String.format("couldn't remove directory %s",sourceDirectory),e);
        }
        try {
            invokerHelper.rmdir(targetDirectory.getAbsolutePath());
        } catch (ExecutionException e) {
            LOG.error(String.format("couldn't remove directory %s",targetDirectory),e);
        }
    }
    
    @Before
    public void setup() throws Exception {
        TestPropertiesConfigurator.initProperties();

        templatesDirectory = System.getProperty(TestProperties.TEMPLATES_DIRECTORY_LOCATION);
        commonScripts = System.getProperty(TestProperties.MIF_COMMON_SCRIPTS_LOCATION);
        genericScripts = System.getProperty(TestProperties.MIF_GENERIC_SCRIPTS_LOCATION);

        invoker = new InvokerFactory().getShellInvoker();
        invokerHelper = new InvokerHelper(invoker);
        sourceDirectory = createTmpDirectory(TestsHelper.generateTempDirectoryPath("-SRC"), invokerHelper);
        targetDirectory = createTmpDirectory(TestsHelper.generateTempDirectoryPath("-DEST"), invokerHelper);

        sourceSubDirectory1 = createTmpDirectory(new File(sourceDirectory, "subDir1").getAbsolutePath(), invokerHelper);
        targetSubDirectory1 = new File(targetDirectory, "subDir1");
        sourceHiddenSubDirectory = createTmpDirectory(new File(sourceDirectory, ".MIF").getAbsolutePath(), invokerHelper);
        targetHiddenSubDirectory = new File(targetDirectory, ".MIF");

        invokerHelper.createFileFromContents("This is the content of file A.", new File(sourceDirectory, "A.txt").getAbsolutePath(), 0644);
        invokerHelper.createFileFromContents("This is the content of file MIF-A.txt.", new File(sourceDirectory, "MIF-A.txt").getAbsolutePath(), 0611);
        invokerHelper.createFileFromContents("File A in sub directory subDir1.", new File(sourceSubDirectory1, "A.txt").getAbsolutePath(), 0500);
        invokerHelper.createFileFromContents("File B in sub directory subDir1.", new File(sourceSubDirectory1, "B.txt").getAbsolutePath(), 0433);
        invokerHelper.createFileFromContents("File A in hidden sub directory .MIF.", new File(sourceHiddenSubDirectory, "A.txt").getAbsolutePath(), 0673);
        invokerHelper.createFileFromContents("File MIF-B.txt in hidden sub directory .MIF.", new File(sourceHiddenSubDirectory, "MIF-B.txt").getAbsolutePath(), 0612);


        commandBuilder = new FreemarkerTemplateCommandBuilder();
        commandBuilder.setFreemarkerConfigurationProvider(FreemarkerConfigurationProviderFactory.createFreemarkerConfigurationProvider(templatesDirectory));
        commandBuilder.setTemplate("common/resourceCopier.ftl");
        commandBuilder.setVariable("CONNECTOR_UTILS", genericScripts);
        commandBuilder.setVariable("MANGO_UTILS", commonScripts);
        commandBuilder.setVariable("REQUEST_ATTRIBUTE_BLOCK", "");
        commandBuilder.setVariable("SCRIPT_PREAMBLE", "");

        params = new ResourceCopierParameters();
        params.setCommandBuilder(commandBuilder);
        params.setSourceDirectory(sourceDirectory);
        params.setDestDirectory(targetDirectory);
        params.setInvoker(invoker);
        params.setCommandBuilder(commandBuilder);
    }

    private void testExistence() throws Exception {
        topLevelFileAExists = invokerHelper.fileExists(new File(targetDirectory, "A.txt").getAbsolutePath());
        topLevelFileMIFAExists = invokerHelper.fileExists(new File(targetDirectory, "MIF-A.txt").getAbsolutePath());
        subLevelFileAExists = invokerHelper.fileExists(new File(targetSubDirectory1, "A.txt").getAbsolutePath());
        subLevelFileBExists = invokerHelper.fileExists(new File(targetSubDirectory1, "B.txt").getAbsolutePath());
        hiddenSubLevelFileAExists = invokerHelper.fileExists(new File(targetHiddenSubDirectory, "A.txt").getAbsolutePath());
        hiddenSubLevelFileMIFBExists = invokerHelper.fileExists(new File(targetHiddenSubDirectory, "MIF-B.txt").getAbsolutePath());
    }

    @Test
    public void shouldCopyEverything() throws Exception {

        ResourceCopierImpl copier = new ResourceCopierImpl(params);
        copier.copy(Collections.<File> emptyList());

        testExistence();
        assertTrue(topLevelFileAExists);
        assertTrue(topLevelFileMIFAExists);
        assertTrue(subLevelFileAExists);
        assertTrue(subLevelFileBExists);
        assertFalse(hiddenSubLevelFileAExists);
        assertFalse(hiddenSubLevelFileMIFBExists);
    }

    @Test
    public void shouldCopyEverythingExceptHiddenDirectory() throws Exception {

        List<String> dirIgnoreList = new ArrayList<String>();
        dirIgnoreList.add(".MIF");
        params.setDirectoryIgnoreList(dirIgnoreList);

        ResourceCopierImpl copier = new ResourceCopierImpl(params);
        copier.copy(Collections.<File> emptyList());

        testExistence();
        assertTrue(topLevelFileAExists);
        assertTrue(topLevelFileMIFAExists);
        assertTrue(subLevelFileAExists);
        assertTrue(subLevelFileBExists);
        assertFalse(hiddenSubLevelFileAExists);
        assertFalse(hiddenSubLevelFileMIFBExists);
    }

    @Test
    public void shouldCopyEverythingExceptFilesWithMIFInTheirName() throws Exception {

        List<String> fileIgnoreList = new ArrayList<String>();
        fileIgnoreList.add("MIF.*");
        params.setFileRegexPatternIgnoreList(fileIgnoreList);

        ResourceCopierImpl copier = new ResourceCopierImpl(params);
        copier.copy(Collections.<File> emptyList());

        testExistence();
        assertTrue(topLevelFileAExists);
        assertFalse(topLevelFileMIFAExists);
        assertTrue(subLevelFileAExists);
        assertTrue(subLevelFileBExists);
        assertFalse(hiddenSubLevelFileAExists);
        assertFalse(hiddenSubLevelFileMIFBExists);
    }

    @Test
    public void shouldCopyOnlyFilesStartingWithB() throws Exception {

        List<String> fileIgnoreList = new ArrayList<String>();
        fileIgnoreList.add("MIF.*");
        fileIgnoreList.add("A.txt");
        params.setFileRegexPatternIgnoreList(fileIgnoreList);

        ResourceCopierImpl copier = new ResourceCopierImpl(params);
        copier.copy(Collections.<File> emptyList());

        testExistence();
        assertFalse(topLevelFileAExists);
        assertFalse(topLevelFileMIFAExists);
        assertFalse(subLevelFileAExists);
        assertTrue(subLevelFileBExists);
        assertFalse(hiddenSubLevelFileAExists);
        assertFalse(hiddenSubLevelFileMIFBExists);
    }

    @Test
    public void shouldCopyNothing() throws Exception {

        List<String> fileIgnoreList = new ArrayList<String>();
        fileIgnoreList.add("MIF.*");
        fileIgnoreList.add("A\\.txt");
        fileIgnoreList.add(".*\\.txt");
        params.setFileRegexPatternIgnoreList(fileIgnoreList);

        ResourceCopierImpl copier = new ResourceCopierImpl(params);
        copier.copy(Collections.<File> emptyList());

        testExistence();
        assertFalse(topLevelFileAExists);
        assertFalse(topLevelFileMIFAExists);
        assertFalse(subLevelFileAExists);
        assertFalse(subLevelFileBExists);
        assertFalse(hiddenSubLevelFileAExists);
        assertFalse(hiddenSubLevelFileMIFBExists);
    }

    @Test
    public void shouldCopyOnlySpecifiedFiles() throws Exception {

        List<File> copyList = new ArrayList<File>();
        copyList.add(new File(sourceDirectory, "MIF-A.txt"));
        copyList.add(new File(sourceDirectory, ".MIF/A.txt"));
        copyList.add(new File(sourceDirectory, "subDir1/B.txt"));

        ResourceCopierImpl copier = new ResourceCopierImpl(params);
        copier.copy(copyList);

        testExistence();
        assertFalse(topLevelFileAExists);
        assertTrue(topLevelFileMIFAExists);
        assertFalse(subLevelFileAExists);
        assertTrue(subLevelFileBExists);
        assertTrue(hiddenSubLevelFileAExists);
        assertFalse(hiddenSubLevelFileMIFBExists);
    }
}
