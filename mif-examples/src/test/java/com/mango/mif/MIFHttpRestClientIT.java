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
package com.mango.mif;

import static com.mango.mif.MIFJobStatusHelper.complete;
import static com.mango.mif.MIFJobStatusHelper.running;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.client.api.rest.ResponseStatus;
import com.mango.mif.domain.ClientAvailableConnectorDetails;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.encrypt.DesEncrypter;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * Integration Test exemplifying REST integration with MIF.
 */
public class MIFHttpRestClientIT {
    private static final Logger LOG = Logger.getLogger(MIFHttpRestClientIT.class);

    private static String MIF_USER_NAME = "";
    
    private static String ENV_SETUP_SCRIPT;

    private static final String MIF_HOST = "localhost";
    private static final String MIF_PORT = "9000";
    
    //MIF REST URL template, it is populated with MIF_HOST, MIF_PORT and MIF_SERVICE
    private static final String TEMPLATE_URL = "http://<host>:<port>/<service>";
    private static final String MIF_SERVICE = "MIFServer/REST/services/";

    /**
     * A location of the fileshare accessible by MIF 
     * (must resolve to the same <NFS> resource as EXECUTION_HOST_FILESHARE_REMOTE)
     */
    private String EXECUTION_HOST_FILESHARE;
    /**
     * A location of the fileshare accessible by Execution Host (e.g. Grid Node)
     *  (must resolve to the same <NFS> resource as EXECUTION_HOST_FILESHARE_REMOTE)
     */
    private String EXECUTION_HOST_FILESHARE_REMOTE;
    
    private DesEncrypter encrypter;
    private MIFHttpRestClient restClient;

    private boolean submitAsUser = false;
    
    public static File TEMP_FOLDER = new File("target", "MIFHttpRestClientIT-WorkingDir");
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        Properties properties = new Properties();
        properties.load(MIFHttpRestClientIT.class.getResourceAsStream("/tests.properties"));
        
        ENV_SETUP_SCRIPT = resolvePath(properties, "mif.env.setup.script.path");
        MIF_USER_NAME = properties.getProperty("mif.user.name");
        if (StringUtils.isBlank(MIF_USER_NAME)) {
            MIF_USER_NAME=System.getProperty("user.name");
        }
        
        // Recreate an empty working directory
    	FileUtils.deleteQuietly(TEMP_FOLDER);
    	TEMP_FOLDER.mkdirs();
    }
    
    @Before
    public void setUp() throws EncryptionException {
        encrypter = new DesEncrypter(MIFHttpRestClientIT.class.getResource("/mif_encryption_key_ddmore_conn_dev.key").toString());

        String mifUrl = TEMPLATE_URL;
        mifUrl = mifUrl.replaceAll("<host>",MIF_HOST);
        mifUrl = mifUrl.replaceAll("<port>",MIF_PORT);
        mifUrl = mifUrl.replaceAll("<service>",MIF_SERVICE);
        restClient = new MIFHttpRestClient(mifUrl);

        EXECUTION_HOST_FILESHARE = resolveMifSharePath();
        EXECUTION_HOST_FILESHARE_REMOTE = resolveMifSharePath();
    }
    
    private static String resolvePath(Properties properties, String property) {
        String propertyValue = properties.getProperty(property);
        File file = new File(propertyValue);
        if(file.exists()) {
            return file.getAbsolutePath();
        } else {
            return FileUtils.toFile(MIFHttpRestClientIT.class.getResource(propertyValue)).getAbsolutePath();
        }
    }
    
    private String resolveMifSharePath() {
        return TEMP_FOLDER.getAbsolutePath();
    }
    
    @Test
    public void testGetCommandExecutionTargetDetails() throws Exception {
    	final String executionType = "NONMEM";
    	final ClientAvailableConnectorDetails cacd = this.restClient.getClientAvailableConnectorDetails(executionType);
    	
    	assertTrue("Client-Available Connector Details should be returned", StringUtils.isNotEmpty(cacd.getResultsIncludeRegex()));
    }
    
    @Test
    public void testGetAllCommandExecutionTargetDetails() throws Exception {

    	// Call the method under test
    	final Map<String, ClientAvailableConnectorDetails> cacdMap = this.restClient.getClientAvailableConnectorDetails();
    	
    	assertFalse("Should be at least one Connector discovered", cacdMap.isEmpty());
    	
    	for (final Map.Entry<String, ClientAvailableConnectorDetails> cacdMapEntry : cacdMap.entrySet()) {
    		assertTrue("Client-Available Connector Details should be returned for execution type " + cacdMapEntry.getKey(),
    			StringUtils.isNotEmpty(cacdMapEntry.getValue().getResultsIncludeRegex()));
    	}
    }
    
    @Test
    public void shouldSuccesfullyExecuteNonmem() throws EncryptionException, IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(MIFHttpRestClientIT.class.getResource("/com/mango/mif/nmEnvTest/Models/run1.mod"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        File testDataDir = scriptFile.getParentFile().getParentFile();
        File scriptFileRel = new File(scriptFile.getParentFile().getName(),scriptFile.getName());
        
        String executionType = "NONMEM";
        
        // generating request ID
        String requestId = UUID.randomUUID().toString();
        LOG.info(String.format("Request ID is %s",requestId));
        File outputs = submitAndWait(testDataDir, scriptFileRel, executionType, requestId, waitTillCompletion(), printSummary());
        assertTrue("Job didn't complete successfully", complete(restClient.checkStatus(requestId)));
        assertTrue("NONMEM connector result file does not exist",new File(new File(outputs, scriptFileRel.getParent()),"run1.lst").exists());
    }

    @Test
    public void shouldExecuteNonmemAndThenCancel() throws EncryptionException, IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(MIFHttpRestClientIT.class.getResource("/com/mango/mif/nmEnvTest/Models/run1.mod"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        File testDataDir = scriptFile.getParentFile().getParentFile();
        File scriptFileRel = new File(scriptFile.getParentFile().getName(),scriptFile.getName());
        
        String executionType = "NONMEM";
        
        // generating request ID
        String requestId = UUID.randomUUID().toString();
        LOG.info(String.format("Request ID is %s",requestId));
        File outputs = submitAndWait(testDataDir, scriptFileRel, executionType, requestId, waitFor(JobStatus.PROCESSING.name()));
        
        MIFResponse response = restClient.cancel(requestId);
        assertEquals("Checking that response is SUCCESS", ResponseStatus.SUCCESS, response.getStatus());
    }

    @Test
    public void shouldSuccesfullyExecuteWarfarPkPred() throws EncryptionException, IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(MIFHttpRestClientIT.class.getResource("/com/mango/mif/nonmemTestData/warfarin_PK_PRED/inputs/warfarin_PK_PRED.ctl"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        File testDataDir = scriptFile.getParentFile();
        File scriptFileRel = new File(scriptFile.getName());
        
        String executionType = "NONMEM";
        
        // generating request ID
        String requestId = UUID.randomUUID().toString();
        LOG.info(String.format("Request ID is %s",requestId));
        File outputs = submitAndWait(testDataDir, scriptFileRel, executionType, requestId, waitTillCompletion(), printSummary());
        assertTrue("Job didn't complete successfully", complete(restClient.checkStatus(requestId)));
        assertTrue("NONMEM connector result file does not exist",new File(outputs,"warfarin_PK_PRED.lst").exists());
    }
    

    @Test
    public void shouldSuccesfullyExecuteExample3() throws EncryptionException, IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(MIFHttpRestClientIT.class.getResource("/com/mango/mif/example3/example3.xml"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        File testDataDir = scriptFile.getParentFile();
        File scriptFileRel = new File(scriptFile.getName());
        
        String executionType = "NONMEM";
        
        // generating request ID
        String requestId = UUID.randomUUID().toString();
        LOG.info(String.format("Request ID is %s",requestId));
        File outputs = submitAndWait(testDataDir, scriptFileRel, executionType, requestId, waitTillCompletion(), printSummary());
        assertTrue("Job didn't complete successfully", complete(restClient.checkStatus(requestId)));
        assertTrue("NONMEM connector result file does not exist",new File(outputs,"example3.lst").exists());
    }


    @Test
    public void shouldSuccesfullyExecuteExample3_MS() throws EncryptionException, IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(MIFHttpRestClientIT.class.getResource("/com/mango/mif/example3/example3_MS.xml"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        File testDataDir = scriptFile.getParentFile();
        File scriptFileRel = new File(scriptFile.getName());
        
        String executionType = "NONMEM";
        
        // generating request ID
        String requestId = UUID.randomUUID().toString();
        LOG.info(String.format("Request ID is %s",requestId));
        File outputs = submitAndWait(testDataDir, scriptFileRel, executionType, requestId, waitTillCompletion(), printSummary());
        assertTrue("Job didn't complete successfully", complete(restClient.checkStatus(requestId)));
        assertTrue("NONMEM connector result file does not exist",new File(outputs,"example3_MS.lst").exists());
    }

    @Test
    public void shouldSuccesfullyExecuteWarfarin_PK_PRED_from_pharmml() throws EncryptionException, IOException, InterruptedException {
        File scriptFile = FileUtils.toFile(MIFHttpRestClientIT.class.getResource("/com/mango/mif/warfarin_PK_PRED/warfarin_PK_PRED.xml"));
        Preconditions.checkArgument(scriptFile.exists(), "Script file does not exist");
        File testDataDir = scriptFile.getParentFile();
        File scriptFileRel = new File(scriptFile.getName());
        
        String executionType = "NONMEM";
        
        // generating request ID
        String requestId = UUID.randomUUID().toString();
        LOG.info(String.format("Request ID is %s",requestId));
        File outputs = submitAndWait(testDataDir, scriptFileRel, executionType, requestId, waitTillCompletion(), printSummary());
        assertTrue("Job didn't complete successfully", complete(restClient.checkStatus(requestId)));
        assertTrue("NONMEM connector result file does not exist",new File(outputs,"warfarin_PK_PRED.lst").exists());
    }

    private File submitAndWait(File testDataDir, File scriptFileRel, String executionType, String requestId, Step... steps)
            throws IOException, InterruptedException {
        // creating request directory structure
        File requestDir = new File(EXECUTION_HOST_FILESHARE, requestId);
        requestDir.mkdir();

        //publishing input files
        FileUtils.copyDirectory(testDataDir, requestDir);
        
        // submit a request
        ExecutionRequest executionRequest = buildExecutionRequest(requestId, "Nonmem job submitted from REST client", executionType, scriptFileRel.getPath());
        
        restClient.executeJob(executionRequest);
        
        for(Step step : steps) {
            step.perform(executionRequest);
        }
        
        LOG.info(String.format("Request directory is %s", requestDir.getAbsolutePath()));
        return requestDir;
    }
    
    @Test
    @Ignore("Should be explicitly invoked, since this kills MIF instance by which affecting other tests")
    public void shouldKillMif() throws Throwable  {
        assertEquals(restClient.killMif(),"OK");
    }
    
    private ExecutionRequest buildExecutionRequest(String requestId, String description,  String executionType, String scriptName) {
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder().setRequestId(requestId).setName(description)
            .setExecutionType(executionType)
                .setExecutionFile(scriptName).setSubmitAsUserMode(submitAsUser);
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE", EXECUTION_HOST_FILESHARE);
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE_REMOTE", EXECUTION_HOST_FILESHARE_REMOTE);
        requestBuilder.setUserName(MIF_USER_NAME);
        requestBuilder.setSubmitHostPreamble(ENV_SETUP_SCRIPT);
        return requestBuilder.getExecutionRequest();
    }
    
    private Step waitTillCompletion() {
        Step step = new WaitForCompletion();
        step.setRestClient(restClient);
        return step;
    }
    
    private Step printSummary() {
        Step step = new PrintJobSummary();
        step.setRestClient(restClient);
        return step;
    }

    private Step waitFor(String status) {
        Step step = new WaitForStatus(status);
        step.setRestClient(restClient);
        return step;
    }
    
    private abstract class Step {
        private MIFHttpRestClient restClient;
        public abstract void perform(ExecutionRequest request);
        
        public void setRestClient(MIFHttpRestClient restClient) {
            this.restClient = restClient;
        }
    }
    
    private class WaitForCompletion extends Step {
        public void perform(ExecutionRequest request) {
            // polling for job status
            boolean poll = true;
            while(poll) {
                String status = restClient.checkStatus(request.getRequestId());
                poll = running(status);
                LOG.debug(String.format("Job %s status is %s",request.getRequestId(), status));
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        }
    }

    private class WaitForStatus extends Step {
        private String status;
        WaitForStatus(String status) {
            this.status = status;
        }
        public void perform(ExecutionRequest request) {
            // polling for job status
            boolean poll = true;
            while(poll) {
                String status = restClient.checkStatus(request.getRequestId());
                if(!running(status)) {
                    break;
                }
                if(this.status.equals(status)) {
                    break;
                }
                LOG.debug(String.format("Job %s status is %s",request.getRequestId(), status));
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        }
    }
    private class PrintJobSummary extends Step {
        public void perform(ExecutionRequest request) {
           // retrieving execution response
            LOG.info(restClient.getExecutionResponse(request.getRequestId()));
        }
    }
}
