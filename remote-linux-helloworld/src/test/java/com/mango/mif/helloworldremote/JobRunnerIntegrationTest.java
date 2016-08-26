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
package com.mango.mif.helloworldremote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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
 * Remote Linux Hello World connector job runner integration test.
 * <p>
 * This test essentially performs an end-to-end Connector execution, building/resolving Freemarker
 * template scripts on the local host running MIF, but executing them on a specified configured
 * (Linux) remote host. The tests check that the correct output files were created by the scripts.
 * <p>
 * The following configuration needs to be present in your local <code>build-env.properties</code> file: <pre>
 * mif.client.userName={username}
 * mif.client.password={password, encrypted using MIF-encryption-key-default.key provided with MIF-exec}
 * mif.jsch.port=22
 * mif.jsch.host=navws-{xx}
 * connector.jschInvoker.host=navws-{xx}
 * remote-linux-helloworld-connector.execution.host.fileshare=V:\\mifshare
 * remote-linux-helloworld-connector.execution.host.fileshare.remote=~/mifshare</pre>
 * The local and remote fileshare properties must resolve to the same physical location on the same host.
 * This is intended to be achieved by mounting a virtual filesystem on the local host, using a protocol
 * such as SSH/SCP to map a directory from the remote host. A tool such as
 * <a href="https://code.google.com/p/win-sshfs/">win-sshfs</a> can map such virtual filesystems.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/spring/mif-core-test-context.xml",
        "/runtime/configuration/remote-linux-helloworld-context.xml",
        "/spring/test-hibernate-context.xml",
        "/spring/JmsTest-Config.xml"})
public class JobRunnerIntegrationTest extends SystemPropertiesPreservingTestStub {
    private final static Logger LOG = Logger.getLogger(JobRunnerIntegrationTest.class);
    
    private final static String EXECUTION_TYPE = "remote-linux-helloworld";
    private final static String CONNECTOR_ID = "HELLOWORLD_LINUX_REMOTE";
    
    /**
     * A location of the fileshare accessible by MIF 
     * (must resolve to the same shared directory as EXECUTION_HOST_FILESHARE_REMOTE).
     */
    private static String EXECUTION_HOST_FILESHARE;
    /**
     * A location of the fileshare accessible by Execution Host (e.g. Grid Node)
     *  (must resolve to the same shared directory as EXECUTION_HOST_FILESHARE).
     */
    private static String EXECUTION_HOST_FILESHARE_REMOTE;
    
    private DefaultJobRunner jobRunner;

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

    private String userName;
    private String userPassword;

    @BeforeClass
    public static void setUpClass() throws ExecutionException, IOException {
        TestPropertiesConfigurator.initProperties();
        Properties properties = TestsHelper.createPropertiesFromStream(JobRunnerIntegrationTest.class.getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));
        MIFProperties.loadProperties(properties);
        
        System.getProperties().list(System.out);
        
    	EXECUTION_HOST_FILESHARE = System.getProperty(ConnectorProperties.REMOTE_LINUX_HELLOWORLD_EXECUTION_HOST_FILESHARE);
    	EXECUTION_HOST_FILESHARE_REMOTE = System.getProperty(ConnectorProperties.REMOTE_LINUX_HELLOWORLD_EXECUTION_HOST_FILESHARE_REMOTE);
        
        Preconditions.checkNotNull(EXECUTION_HOST_FILESHARE, "The remote-linux-helloworld-connector.execution.host.fileshare property is not set");
        Preconditions.checkNotNull(EXECUTION_HOST_FILESHARE_REMOTE, "The remote-linux-helloworld.connector.execution.host.fileshare.remote property is not set");
        
        //that test uses different location of the templates directory than production and needs to set MIF_CONNECTORS_LOCATION property
        System.setProperty("MIF_CONNECTORS_LOCATION", FileUtils.toFile(ConnectorConfigurationTest.class.getResource("/runtime/configuration")).getAbsolutePath());
    }

    @Before
    public void setUp() throws ExecutionException, IOException {
        userName = System.getProperty(MIFTestProperties.MIF_CLIENT_USER_NAME);
        userPassword = System.getProperty(MIFTestProperties.MIF_CLIENT_PASSWORD);
        Preconditions.checkNotNull(userName, "The "+MIFTestProperties.MIF_CLIENT_USER_NAME+" property is not set");
        Preconditions.checkNotNull(userPassword, "The "+MIFTestProperties.MIF_CLIENT_PASSWORD+" property is not set");
    }
    
    @After
    public void tearDown() {
    	LOG.info("\n\n>>> Output from the Connector execution can be found within directory "
    			+ new File(EXECUTION_HOST_FILESHARE, this.jobRunner.getJobId()) + " <<<\n");
    }
    
    @DirtiesContext
    @Test
    public void shouldExecuteHelloWorldConnector() throws Exception {
    	assertNotNull("Expected HelloWorld Connector to be present in the Connectors Registry", this.connectorsRegistry.getConnectorByExecutionType(EXECUTION_TYPE));
    
        this.jobRunner = prepareJobRunner();

        SCXMLDriverImpl driver = (SCXMLDriverImpl) this.jobRunner.getDriver();

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

        final File fileshareDirectory = new File(EXECUTION_HOST_FILESHARE);
        
        final File requestDir = new File(fileshareDirectory, this.jobRunner.getJobId());
        final File mifDirectory = new File(new File(fileshareDirectory, this.jobRunner.getJobId()), ".MIF");
        final File mifTimestampFile = new File(new File(new File(fileshareDirectory, this.jobRunner.getJobId()),".MIF"), ".TIMESTAMP");
        final File mifModifiedFileList = new File(new File(fileshareDirectory, this.jobRunner.getJobId()), "MIF-list-of-files.out");
        final File resultFile = new File(new File(fileshareDirectory, this.jobRunner.getJobId()), "helloWorld.out");
        
        assertTrue(String.format("Request dir %s does not exist", requestDir.getAbsolutePath()), requestDir.exists());
        assertTrue(String.format("MIF metadata dir %s does not exist", mifDirectory.getAbsolutePath()), mifDirectory.exists());
        assertTrue(String.format("MIF timestamp file %s does not exist", mifTimestampFile.getAbsolutePath()), mifTimestampFile.exists());
        assertTrue(String.format("MIF modified file list file %s does not exist", mifModifiedFileList.getAbsolutePath()), mifModifiedFileList.exists());
        assertTrue(String.format("Result file %s does not exist", resultFile.getAbsolutePath()), resultFile.exists());
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
        Map<String,String> requestAttributes = Maps.newHashMap();
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), EXECUTION_HOST_FILESHARE);
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), EXECUTION_HOST_FILESHARE_REMOTE);
        
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
        this.jobManagementService.saveJob(job);
        DefaultJobRunner jobRunner = (DefaultJobRunner) this.jobRunnerFactory.createJobRunner(job);
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
