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
package com.mango.mif.connector.runner.impl.statehandler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Preconditions;
import com.mango.mif.connector.runner.impl.DefaultJobRunner;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.impl.SCXMLDriver;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.ce.CeInvoker;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.template.FreemarkerConfigurationProviderFactory;
import com.mango.mif.core.exec.template.JobAwareFreemarkerTemplateCommandBuilder;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.MIFTestProperties;
import com.mango.mif.utils.SystemPropertiesPreservingTestStub;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.TestsHelper;

/**
 * Tests Shell Based Retrieve Handler
 */
@RunWith(MockitoJUnitRunner.class)
public class ShellBasedRetrieveHandlerTemplateTest extends SystemPropertiesPreservingTestStub {
    private final static Logger logger = Logger.getLogger(ShellBasedRetrieveHandlerTemplateTest.class);

    private String tptStdOutputFileName = "MIF-output.txt";

    private String tptStdErrorFileName = "MIF-error.txt";

    private String finalStatusFileName = "MIF-final-status.txt";

    private String userName;

    private Invoker invoker;

    private InvokerHelper invokerHelper;

    private String commonScripts;

    private String genericScripts;

    private String retrieveScriptTemplate = "generic/retrieveHandler.ftl";

    @Mock
    JobManagementService jobManagementService;

    @Mock
    Job job;

    @Mock
    SCXMLDriver driver;

    private String templatesDirectory;

    private File tmpJobDirectory;


    private void loadMIFProperties() throws IOException {
        Properties properties = TestsHelper.createPropertiesFromStream(ShellBasedRetrieveHandlerTemplateTest.class.getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));
        MIFProperties.loadProperties(properties);
    }
    @After
    public void tearDown() {
        try {
            invokerHelper.rmdir(tmpJobDirectory.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e);
        }
    }
    
    @Before
    public void setUp() throws ExecutionException, IOException {
        TestPropertiesConfigurator.initProperties();

        loadMIFProperties();

        templatesDirectory = System.getProperty(MIFTestProperties.TEMPLATES_DIRECTORY_LOCATION);
        commonScripts = System.getProperty(MIFTestProperties.MIF_COMMON_SCRIPTS_LOCATION);
        genericScripts = System.getProperty(MIFTestProperties.MIF_GENERIC_SCRIPTS_LOCATION);


        userName = System.getProperty(MIFTestProperties.MIF_OTHER_CLIENT_USER_NAME);
        String encryptedPassword = System.getProperty(MIFTestProperties.MIF_OTHER_CLIENT_PASSWORD);
        String portProp = System.getProperty(MIFTestProperties.MIF_JSCH_PORT);

        Preconditions.checkNotNull(userName, MIFTestProperties.MIF_OTHER_CLIENT_USER_NAME + " property is not set.");
        Preconditions.checkNotNull(encryptedPassword, MIFTestProperties.MIF_OTHER_CLIENT_PASSWORD + " property is not set.");
        Preconditions.checkNotNull(portProp, MIFTestProperties.MIF_JSCH_PORT + " property is not set.");
        Preconditions.checkNotNull(commonScripts, MIFTestProperties.MIF_COMMON_SCRIPTS_LOCATION + " property is not set.");
        Preconditions.checkNotNull(genericScripts, MIFTestProperties.MIF_GENERIC_SCRIPTS_LOCATION + " property is not set.");

        invoker = new CeInvoker();
        invokerHelper = new InvokerHelper(invoker);
    }

    @Test
    public void shouldSuccessfulyExecuteRetrieveScript() throws Exception {

        GenericShellBasedStateHandler handler = new GenericShellBasedStateHandler("task-retrieving", "task.postprocess");
        handler.setJobManagementService(jobManagementService);
        handler.setSCXMLDriver(driver);
        handler.setInvokerResultHandler(new GenericShellCommandInvokerResultHandler(SCXMLDriver.NULL_EVENT));

        JobAwareFreemarkerTemplateCommandBuilder commandBuilder = new JobAwareFreemarkerTemplateCommandBuilder();
        commandBuilder.setFreemarkerConfigurationProvider(FreemarkerConfigurationProviderFactory.createFreemarkerConfigurationProvider(templatesDirectory));
        commandBuilder.setTemplate(retrieveScriptTemplate);
        tmpJobDirectory = TestsHelper.createTmpDirectory(TestsHelper.generateTempDirectoryPath("ShellBasedRetrieveHandlerTemplateTest"),invokerHelper);
        tmpJobDirectory.deleteOnExit();
        File tmpMIFHiddenDir = new File(tmpJobDirectory,MIFProperties.MIF_HIDDEN_DIRECTORY_NAME);
        invokerHelper.mkdir(tmpMIFHiddenDir.getAbsolutePath(), 0755);

        logger.info("Job directory directory:  " + tmpJobDirectory);

        DefaultJobRunner jobRunner = new DefaultJobRunner();
        when(job.getJobId()).thenReturn("999");
        ExecutionRequest er = new ExecutionRequest();
        when(job.getExecutionRequest()).thenReturn(er);
        when(job.getInvoker()).thenReturn(invoker);
        when(job.getJobDirectory()).thenReturn(tmpJobDirectory);
        when(job.getJobMifHiddenDir()).thenReturn(tmpMIFHiddenDir);
        when(jobManagementService.getJob((String) any())).thenReturn(job);
        when(jobManagementService.saveJob(job)).thenReturn(job);
        when(jobManagementService.saveJobRunnerState(eq(job.getJobId()), (JobRunnerState)any())).thenReturn(job);
        when(jobManagementService.update(eq(job.getJobId()),(ObjectStateUpdater<Job>) any())).thenReturn(job);

        jobRunner.setJobId(job.getJobId());

        final String TOO_OLD_1 = "tooOldToCopy1.txt";
        final String SHOULD_COPY_1 = "shouldBeCopied1.txt";

        invokerHelper.createFileFromContents("This content should be copied.", new File(tmpJobDirectory, TOO_OLD_1).getAbsolutePath(), 0644);
        invokerHelper.createFileFromContents("This content should be copied.", new File(tmpJobDirectory, SHOULD_COPY_1).getAbsolutePath(), 0644);
        invokerHelper.createFileFromContents("Output generated by command.", new File(tmpJobDirectory, "generatedOutput.txt").getAbsolutePath(), 0644);

        invokerHelper.createFileFromContents("./" + SHOULD_COPY_1 + "\n", new File(tmpMIFHiddenDir, MIFProperties.MIF_TPT_MODIFIED_LIST_FILE_NAME).getAbsolutePath(), 0644);
        invokerHelper.createFileFromContents("STDOUT of TPT.", new File(tmpMIFHiddenDir, tptStdOutputFileName).getAbsolutePath(), 0644);
        invokerHelper.createFileFromContents("STDERR of TPT.", new File(tmpMIFHiddenDir, tptStdErrorFileName).getAbsolutePath(), 0644);
        invokerHelper.createFileFromContents("Final QACCT call.", new File(tmpMIFHiddenDir, finalStatusFileName).getAbsolutePath(), 0644);

        handler.setJobRunner(jobRunner);
        commandBuilder.setVariable("CONNECTOR_UTILS", genericScripts);
        commandBuilder.setVariable("MANGO_UTILS", commonScripts);
        commandBuilder.setVariable("SCRIPT_PREAMBLE", "");
        commandBuilder.setVariable("gridModifiedListFileName", MIFProperties.MIF_TPT_MODIFIED_LIST_FILE_NAME);
        commandBuilder.setVariable("mifModifiedListFileName", MIFProperties.MIF_MODIFIED_LIST_FILE_NAME);
        commandBuilder.setVariable("mifHiddenDirectoryName",MIFProperties.MIF_HIDDEN_DIRECTORY_NAME);
        commandBuilder.setVariable("gridMifHiddenDirectoryName", MIFProperties.MIF_HIDDEN_DIRECTORY_NAME);

        handler.setCommandBuilder(commandBuilder);
        handler.call();

        boolean oldFileExists = invokerHelper.fileExists(new File(job.getJobDirectory(), TOO_OLD_1).getAbsolutePath());
        boolean youngFileExists = invokerHelper.fileExists(new File(job.getJobDirectory(), SHOULD_COPY_1).getAbsolutePath());
        boolean outputInWrongPlaceExists = invokerHelper.fileExists(new File(job.getJobDirectory(), tptStdOutputFileName).getAbsolutePath());
        boolean errorInWrongPlaceExists = invokerHelper.fileExists(new File(job.getJobDirectory(), tptStdErrorFileName).getAbsolutePath());
        boolean outputInRightPlaceExists = invokerHelper.fileExists(new File(job.getJobMifHiddenDir(), tptStdOutputFileName).getAbsolutePath());
        boolean errorInRightPlaceExists = invokerHelper.fileExists(new File(job.getJobMifHiddenDir(), tptStdErrorFileName).getAbsolutePath());
        
        final String mifModifiedListFileNameAbsPath = new File(job.getJobMifHiddenDir(), MIFProperties.MIF_MODIFIED_LIST_FILE_NAME).getAbsolutePath();
        
        assertTrue("Modified Files List did not exist", invokerHelper.fileExists(mifModifiedListFileNameAbsPath));
        
        assertTrue("Modified Files List should contain new file path", invokerHelper.getFileContents(mifModifiedListFileNameAbsPath).contains(SHOULD_COPY_1));
        assertFalse("Modified Files List should not contain old file path", invokerHelper.getFileContents(mifModifiedListFileNameAbsPath).contains(TOO_OLD_1));
        
        assertTrue("Expected oldFile was not found", oldFileExists);
        assertTrue("Expected youngFile was not found", youngFileExists);
        assertFalse("Std Output Stream File was in wrong directory.", outputInWrongPlaceExists);
        assertFalse("Std Error Stream File was in wrong directory.",errorInWrongPlaceExists);
        assertTrue("Std Output Stream File was not found.", outputInRightPlaceExists);
        assertTrue("Std Error Stream File was not found.", errorInRightPlaceExists);
    }
}
