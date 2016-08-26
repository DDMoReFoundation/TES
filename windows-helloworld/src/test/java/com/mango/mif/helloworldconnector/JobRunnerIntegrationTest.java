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
package com.mango.mif.helloworldconnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.runner.impl.DefaultJobRunner;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.impl.JobRunnerWithSCXMLDriverFactory;
import com.mango.mif.connector.runner.impl.SCXMLDriverImpl;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.resource.fileutils.FileUtilsResourcePublisher;
import com.mango.mif.core.resource.shell.PublisherParameters;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.ExecutionRequestHelper;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.MIFTestProperties;
import com.mango.mif.utils.SystemPropertiesPreservingTestStub;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.TestsHelper;


/**
 * Hello World connector job runner Test. That test is to ensure that example connector configuration that is included in documentation is valid.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/spring/mif-core-test-context.xml",
        "/runtime/configuration/windows-helloworld-context.xml",
        "/spring/test-hibernate-context.xml",
        "/spring/JmsTest-Config.xml"})
public class JobRunnerIntegrationTest extends SystemPropertiesPreservingTestStub {
    private final static Logger LOG = Logger.getLogger(JobRunnerIntegrationTest.class);
    
    private final static String EXECUTION_TYPE = "helloworld";
    private final static String CONNECTOR_ID = "HELLOWORLD_WINDOWS_LOCAL";

    @Resource
    private JobRunnerWithSCXMLDriverFactory jobRunnerFactory;

    @Resource
    private JobManagementService jobManagementService;
    
    @Resource
    private ConnectorsRegistry connectorsRegistry;

    /**
     * Used to synchronise test thread and spring TaskExecutors
     */
    private CountDownLatch eventSignal = new CountDownLatch(1);

    private String jobId;
    
    public File testFolder = new File("target", "JobRunnerIntegrationTest-folder");

    private String userPassword;

    private String userName;

    @BeforeClass
    public static void setUpClass() throws ExecutionException, IOException {
        TestPropertiesConfigurator.initProperties();
        Properties properties = TestsHelper.createPropertiesFromStream(ConnectorConfigurationTest.class.getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));
        MIFProperties.loadProperties(properties);
        
        //that test uses different location of the templates directory than production and needs to set MIF_CONNECTORS_LOCATION property
        System.setProperty("MIF_CONNECTORS_LOCATION", FileUtils.toFile(ConnectorConfigurationTest.class.getResource("/runtime/configuration")).getAbsolutePath());
    }

    @Before
    public void setUp() throws ExecutionException, IOException {
        userName = System.getProperty(MIFTestProperties.MIF_CLIENT_USER_NAME);
        userPassword = System.getProperty(MIFTestProperties.MIF_CLIENT_PASSWORD);
        Preconditions.checkNotNull(userName, "The "+MIFTestProperties.MIF_CLIENT_USER_NAME+" property is not set");
        Preconditions.checkNotNull(userPassword, "The "+MIFTestProperties.MIF_CLIENT_PASSWORD+" property is not set");
        
        testFolder.mkdirs();
    }
    
    @After
    public void tearDown() {
    	LOG.info("\n\n>>> Output from the Connector execution can be found within directory " + testFolder + " <<<\n");
    }
    
    @DirtiesContext
    @Test
    public void shouldExecuteHelloWorldConnector() throws Exception {
    	assertNotNull("Expected HelloWorld Connector to be present in the Connectors Registry", this.connectorsRegistry.getConnectorByExecutionType(EXECUTION_TYPE));
    
        DefaultJobRunner jobRunner = prepareJobRunner();

        SCXMLDriverImpl driver = (SCXMLDriverImpl)jobRunner.getDriver();

        Set<String> states = new HashSet<String>();
        states.add(JobRunnerState.FINISHED.getStateName());
        states.add(JobRunnerState.FAILED.getStateName());
        registerNofifierOnState(driver, states, eventSignal);
        driver.fireEvent(JobRunnerState.RUNNING.getTriggeringEvent());

        eventSignal.await();

        while ((!driver.isInFinalState()) && driver.getException() == null) {
        	LOG.info("Waiting for successful completion of the Job Runner Driver...");
            Thread.sleep(3000);
        }

        if (driver.getException() != null) {
        	fail("Exception thrown during job execution: " + driver.getException().getMessage());
        }

        final File tmpFileshareDirectory = testFolder;
        
        final File requestDir = new File(tmpFileshareDirectory, jobRunner.getJobId());
        File mifDirectory = new File(new File(tmpFileshareDirectory, jobRunner.getJobId()), ".MIF");
        // TODO: Reinstate this functionality
        //final File mifModifiedFileList = new File(new File(new File(tmpFileshareDirectory, jobRunner.getJobId()),".MIF"), "MIF.modified.file.list");
        final File mifTimestampFile = new File(new File(new File(tmpFileshareDirectory, jobRunner.getJobId()),".MIF"), ".TIMESTAMP");
        final File resultFile = new File(new File(new File(tmpFileshareDirectory, jobRunner.getJobId()), "output"), "helloWorld.out");
        
        assertTrue(String.format("Request dir %s does not exist", requestDir.getAbsolutePath()), requestDir.exists());
        assertTrue(String.format("MIF metadata dir %s does not exist", mifDirectory.getAbsolutePath()), mifDirectory.exists());
        assertTrue(String.format("MIF timestamp file %s does not exist", mifTimestampFile.getAbsolutePath()), mifTimestampFile.exists());
        // TODO: Reinstate this functionality
        //assertTrue(String.format("MIF modified file list file %s does not exist", mifModifiedFileList.getAbsolutePath()), mifModifiedFileList.exists());
        assertTrue(String.format("Result file %s does not exist", resultFile.getAbsolutePath()), resultFile.exists());
        
        final List<String> outputLines = FileUtils.readLines(resultFile);
        assertEquals("Checking the output from the custom script", "Hello from my custom script!", outputLines.get(2));
        assertEquals("Checking the output from the template-generated script", "Hello World!", outputLines.get(3));
        
    }
    
    /**
     * Prepares a job runner for a default request
     * @return
     * @throws MIFException
     * @throws JAXBException
     * @throws ExecutionException 
     */
    private DefaultJobRunner prepareJobRunner() throws MIFException, JAXBException, ExecutionException {
        File testDataDir = FileUtils.toFile(JobRunnerIntegrationTest.class.getResource("/mockRequestData"));
        File tmpFileshareDirectory = testFolder;
        Map<String,String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), tmpFileshareDirectory.getAbsolutePath());
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), tmpFileshareDirectory.getAbsolutePath());
        
        PublisherParameters publisherParameters = new PublisherParameters();
        publisherParameters.setRequestAttributes(requestAttributes);
        publisherParameters.setRootDirectory(testDataDir);
        FileUtilsResourcePublisher publisher = new FileUtilsResourcePublisher(publisherParameters);
        ExecutionRequestBuilder executionRequestBuilder = ExecutionRequestHelper.createExecutionRequestBuilderForInputDirectory(publisher,testDataDir,"empty");
        executionRequestBuilder.setUserName(userName).setUserPassword(userPassword).setSubmitAsUserMode(true).setExecutionType(EXECUTION_TYPE);
        
        for(Map.Entry<String, String> en : requestAttributes.entrySet()) {
            executionRequestBuilder.addAttribute(en.getKey(), en.getValue());
        }
        Job job = jobManagementService.createNewJob();
        job.setConnectorId(CONNECTOR_ID);
        job.setExecutionRequestMsg(executionRequestBuilder.getExecutionRequestMsg());
        jobManagementService.saveJob(job);
        DefaultJobRunner jobRunner = (DefaultJobRunner)jobRunnerFactory.createJobRunner(job);
        jobId = job.getJobId();
        return jobRunner;
    }
    /**
     * Registers a state machine listener that on given state notifies a thread that waits on semaphore
     * @param eventSignal
     * @param stateId
     */
    private void registerNofifierOnState(SCXMLDriverImpl driver, final Set<String> statesId, final CountDownLatch eventSignal) {
        driver.getEngine().addListener(driver.getEngine().getStateMachine(), new SCXMLListener() {

            @Override
            public void onEntry(TransitionTarget state) {
                LOG.debug("onEntry " + state.getId());
                if(statesId.contains(state.getId())) {
                    eventSignal.countDown();
                }
            }

            @Override
            public void onExit(TransitionTarget state) {
            }

            @Override
            public void onTransition(TransitionTarget from, TransitionTarget to, Transition transition) {
            }

        });
    }
}
