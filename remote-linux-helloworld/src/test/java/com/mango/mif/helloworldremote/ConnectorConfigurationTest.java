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

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mango.mif.connector.Connector;
import com.mango.mif.connector.impl.CommandExecutionConnector;
import com.mango.mif.connector.runner.JobRunnerFactory;
import com.mango.mif.connector.runner.impl.DefaultJobRunner;
import com.mango.mif.connector.runner.impl.JobRunnerState;
import com.mango.mif.connector.runner.impl.SCXMLDriverImpl;
import com.mango.mif.connector.runner.impl.statehandler.BaseGenericShellBasedStateHandler;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementServiceImpl;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.MIFTestProperties;
import com.mango.mif.utils.SystemPropertiesPreservingTestStub;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.TestsHelper;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * Test checking spring configuration of the Hello World connector. That test is to ensure that example connector configuration that is included in documentation is valid.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/spring/mif-core-test-context.xml",
        "/runtime/configuration/remote-linux-helloworld-context.xml",
        "/spring/test-hibernate-context.xml",
        "/spring/JmsTest-Config.xml"})
public class ConnectorConfigurationTest extends SystemPropertiesPreservingTestStub {
    
    private final static Logger LOG = Logger.getLogger(ConnectorConfigurationTest.class);

    @Resource
    private JobRunnerFactory helloWorldJobRunnerFactory;

    @Resource
    private JobManagementServiceImpl jobManagementService;
    
    @Resource
    private ConnectorsRegistry connectorsRegistry;
    

    @BeforeClass
    public static void setUp() throws ExecutionException, IOException {
        TestPropertiesConfigurator.initProperties();
        Properties properties = TestsHelper.createPropertiesFromStream(ConnectorConfigurationTest.class.getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));
        MIFProperties.loadProperties(properties);
        
        //that test uses different location of the templates directory than production and needs to set MIF_CONNECTORS_LOCATION property
        System.setProperty("MIF_CONNECTORS_LOCATION", FileUtils.toFile(ConnectorConfigurationTest.class.getResource("/runtime/configuration")).getAbsolutePath());
    }
    
    private Job createJob() throws MIFException, EncryptionException {
        Job testJob = TestsHelper.createJob("JOB_ID", UUID.randomUUID().toString());
        
        Job job = jobManagementService.createNewJob();
        
        final Connector connector = this.connectorsRegistry.getConnectorByExecutionType("remote-linux-helloworld");
        assertNotNull("Connector should have been found from the autowired ConnectorsRegistry", connector);
        job.setCommandExecutionTarget(((CommandExecutionConnector) connector).getCommandExecutionTarget());
        
        job.setExecutionRequestMsg(testJob.getExecutionRequestMsg());
        
        return job;
    }
    
    /**
     * Test configuration
     * @throws MIFException
     * @throws EncryptionException 
     */
    @DirtiesContext
    @Test
    public void shouldCreateValidJobRunnerAndStateHandlersForHelloWorldExecution() throws ExecutionException, MIFException, EncryptionException {
    	Job job = createJob();
        job.getExecutionRequest().setSubmitAsUserMode(true);
        DefaultJobRunner jobRunner = (DefaultJobRunner) helloWorldJobRunnerFactory.createJobRunner(job);
        assertNotNull(jobRunner);
        assertNotNull(jobRunner.getDriver());
        assertNotNull(jobRunner.getJobId());
        SCXMLDriverImpl driver = (SCXMLDriverImpl)jobRunner.getDriver();

        verifyStateHandlers(driver,job);
    }
    
    /**
     * Verifies that the state handlers have been correctly configured
     * @param driver
     * @param job
     * @throws ExecutionException
     */
    private void verifyStateHandlers(SCXMLDriverImpl driver, Job job) throws ExecutionException {
        verifyStateHandlerCommandBuilder(((BaseGenericShellBasedStateHandler)driver.getStateHandlers().get(JobRunnerState.TASK_PREPARING.getStateName())),job);
        verifyStateHandlerCommandBuilder(((BaseGenericShellBasedStateHandler)driver.getStateHandlers().get(JobRunnerState.TASK_PROCESSING.getStateName())),job);
        verifyStateHandlerCommandBuilder(((BaseGenericShellBasedStateHandler)driver.getStateHandlers().get(JobRunnerState.TASK_RETRIEVING.getStateName())),job);
        verifyStateHandlerDetailedStatusBuilder(((BaseGenericShellBasedStateHandler)driver.getStateHandlers().get(JobRunnerState.TASK_PROCESSING.getStateName())),job);
        verifyStateHandlerDetailedStatusBuilder(((BaseGenericShellBasedStateHandler)driver.getStateHandlers().get(JobRunnerState.CANCELLED.getStateName())),job);
        verifyStateHandlerDetailedStatusBuilder(((BaseGenericShellBasedStateHandler)driver.getStateHandlers().get(JobRunnerState.FAILED.getStateName())),job);
        verifyStateHandlerDetailedStatusBuilder(((BaseGenericShellBasedStateHandler)driver.getStateHandlers().get(JobRunnerState.FINISHED.getStateName())),job);
        
    }
    
    private void verifyStateHandlerDetailedStatusBuilder(BaseGenericShellBasedStateHandler stateHandler, Job job) throws ExecutionException {
        assertNotNull("State handler was null", stateHandler);
        assertNotNull(String.format("%s state handler's detailed status builder was not set",stateHandler.getState()), stateHandler.getDetailedStatusBuilder());
    }
    
    private void verifyStateHandlerCommandBuilder(BaseGenericShellBasedStateHandler stateHandler, Job job) throws ExecutionException {
        assertNotNull("State handler was null", stateHandler);
        assertNotNull(String.format("%s state handler's command builder was not set",stateHandler.getState()), stateHandler.getCommandBuilder());
        stateHandler.getCommandBuilder().setJob(job);
        String command = stateHandler.getCommandBuilder().getCommand();
        LOG.debug(command);
        assertNotNull(String.format("%s state handler's command was null",stateHandler.getState()), command);
        assertNotNull(String.format("%s state handler's invoker result handler was not set",stateHandler.getState()), stateHandler.getInvokerResultHandler());
    }

}
