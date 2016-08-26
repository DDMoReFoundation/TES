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
package com.mango.mif.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.core.messaging.MessagingHelper;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.MIFTestProperties;
import com.mango.mif.utils.TestsHelper;


/**
 * 
 * Tests {@link Job}
 */
@RunWith(MockitoJUnitRunner.class)
public class JobTest {
    
    final static Logger logger = Logger.getLogger(JobTest.class);
    
    @Mock ExecutionRequest executionRequest;

    @Mock ExecutionResponse executionResponse;
    
    Job job;
    
    @Before
    public void setUp() throws Exception {
        Properties properties = TestsHelper.createPropertiesFromStream(JobTest.class.getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));
        MIFProperties.loadProperties(properties);
        job = new Job();
        when(executionRequest.getRequestId()).thenReturn("JOB_ID");
    }

    @Test
    public void setExecutionRequest_should_set_execution_request_msg() throws MIFException {
        job.setExecutionRequest(executionRequest);
        assertNotNull(job.getExecutionRequestMsg());
    }
    
    @Test
    public void setExecutionResponse_should_set_execution_response_msg() throws MIFException {
        job.setExecutionResponse(executionResponse);
        assertNotNull(job.getExecutionResponseMsg());
    }
    
    @Test
    public void setCommandExecutionTarget_should_set_command_execution_target() {
    	final CommandExecutionTarget commandExecutionTarget = new CommandExecutionTarget();
        job.setCommandExecutionTarget(commandExecutionTarget);
        assertSame(commandExecutionTarget, job.getCommandExecutionTarget());
    }

    @Test
    public void setExecutionRequestMsg_should_set_execution_request_and_execution_request_msg() throws MIFException {
        when(executionRequest.getRequestId()).thenReturn("REQUEST_ID");
        job.setExecutionRequest(executionRequest);
        job.setExecutionRequestMsg(MessagingHelper.marshallExecutionRequest(executionRequest));
        assertNotNull(job.getExecutionRequestMsg());
        assertTrue(job.getExecutionRequest()!=executionRequest);
    }
    
    @Test
    public void setExecutionResponseMsg_should_set_execution_resoponse_msg_and_reset_execution_response() throws MIFException {
        when(executionResponse.getRequestId()).thenReturn("REQUEST_ID");
        job.setExecutionResponse(executionResponse);
        job.setExecutionResponseMsg(MessagingHelper.marshallExecutionResponse(executionResponse));
        assertNotNull(job.getExecutionResponseMsg());
        assertTrue(job.getExecutionResponse()!=executionResponse);
    }
    @Test
    public void populateJobWithExecutionMessageData_should_populate_job_with_user_credentials_when_submitAsUser_on() throws MIFException {
        when(executionRequest.getSubmitAsUserMode()).thenReturn(true);
        when(executionRequest.getUserName()).thenReturn("USER");
        when(executionRequest.getUserPassword()).thenReturn("PASSWORD");
        when(executionRequest.getRequestId()).thenReturn("REQUEST_ID");
        job.populateJobWithExecutionMessageData(executionRequest);
        assertEquals(job.getUserName(),"USER");
        assertEquals(job.getPassword(),"PASSWORD");
        assertEquals(job.getJobId(),"REQUEST_ID");
    }

    @Test
    public void populateJobWithExecutionMessageData_should_populate_job_with_user_credentials_when_submitAsUser_off() throws MIFException {
        when(executionRequest.getSubmitAsUserMode()).thenReturn(false);
        when(executionRequest.getUserName()).thenReturn("USER");
        when(executionRequest.getUserPassword()).thenReturn("PASSWORD");
        when(executionRequest.getRequestId()).thenReturn("REQUEST_ID");
        job.populateJobWithExecutionMessageData(executionRequest);
        assertNull(job.getUserName());
        assertNull(job.getPassword());
        assertEquals(job.getJobId(),"REQUEST_ID");
    }

    @Test(expected=NullPointerException.class)
    public void getRemoteJobDirectory_shouldThrowExceptionIfExecutionRequestIsNotSet() {
        job.getRemoteJobDirectory();
    }

    @Test(expected=NullPointerException.class)
    public void getRemoteJobDirectory_shouldThrowExceptionIf_EXECUTION_HOST_FILESHARE_REMOTE_attributeIsNotSetOnExecutionRequest() throws MIFException {
        job.setExecutionRequest(executionRequest);
        job.getRemoteJobDirectory();
    }

    /**
     * Note the forward-slashes rather than back-slashes in the path since this directory must resolve cross-platform
     * if a connector is performing remote execution of template-generated scripts.
     */
    @Test
    public void getRemoteJobDirectory_shouldReturnRemoteJobDirectory() throws MIFException {
        Map<String,String> mockAttributes = Maps.newHashMap();
        mockAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "mock-location");
        when(executionRequest.getRequestAttributes()).thenReturn(mockAttributes);
        when(executionRequest.getRequestId()).thenReturn("REQUEST_ID");
        job.setExecutionRequest(executionRequest);
        assertEquals("mock-location/" + job.getJobId(), job.getRemoteJobDirectory());
    }

    @Test(expected=NullPointerException.class)
    public void getJobDirectory_shouldThrowExceptionIf_EXECUTION_HOST_FILESHARE_attributeIsNotSetOnExecutionRequest() throws MIFException {
        job.setExecutionRequest(executionRequest);
        job.getRemoteJobDirectory();
    }

    @Test
    public void getJobDirectory_shouldReturnJobDirectory() throws MIFException {
        Map<String,String> mockAttributes = Maps.newHashMap();
        mockAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "mock-location");
        when(executionRequest.getRequestAttributes()).thenReturn(mockAttributes);
        when(executionRequest.getRequestId()).thenReturn("REQUEST_ID");
        job.setExecutionRequest(executionRequest);
        File file = job.getJobDirectory();
        assertEquals(new File("mock-location/"+ job.getJobId()).getPath(), file.getPath());
    }
    
    @Test
    public void testGetJobMifHiddenDirPaths() {
        Map<String,String> mockAttributes = Maps.newHashMap();
        mockAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "mock-location");
        when(executionRequest.getRequestAttributes()).thenReturn(mockAttributes);
        when(executionRequest.getRequestId()).thenReturn("REQUEST_ID");
        job.setExecutionRequest(executionRequest);
    	assertEquals("Checking jobMifHiddenDir property", new File("mock-location/" + job.getJobId() + "/.MIF"), job.getJobMifHiddenDir());
    	assertEquals("Checking jobOutputStreamFile property", new File("mock-location/" + job.getJobId() + "/.MIF/MIF.stdout"), job.getJobOutputStreamFile());
    	assertEquals("Checking jobErrorStreamFile property", new File("mock-location/" + job.getJobId() + "/.MIF/MIF.stderr"), job.getJobErrorStreamFile());
    }
    
    /**
     * Note the forward-slashes rather than back-slashes in the paths since these directories must resolve cross-platform
     * if a connector is performing remote execution of template-generated scripts.
     */
    @Test
    public void testGetRemoteJobMifHiddenDirPaths() {
        Map<String,String> mockAttributes = Maps.newHashMap();
        mockAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "mock-location");
        when(executionRequest.getRequestAttributes()).thenReturn(mockAttributes);
        when(executionRequest.getRequestId()).thenReturn("REQUEST_ID");
        job.setExecutionRequest(executionRequest);
        
    	assertEquals("Checking remoteJobMifHiddenDir property", "mock-location/" + job.getJobId() + "/.MIF", job.getRemoteJobMifHiddenDir());
    	assertEquals("Checking remoteJobOutputStreamFile property", "mock-location/" + job.getJobId() + "/.MIF/MIF.stdout", job.getRemoteJobOutputStreamFile());
    	assertEquals("Checking remoteJobErrorStreamFile property", "mock-location/" + job.getJobId() + "/.MIF/MIF.stderr", job.getRemoteJobErrorStreamFile());
    }
    
    @Test(expected=IllegalStateException.class)
    public void hasExecutionFile_shouldThrowExceptionIfExecutionRequestIsNull() throws MIFException {
        job.hasExecutionFile();
    }

    @Test
    public void hasExecutionFile_shouldReturnTrueIfExecutionFileIsSet() throws MIFException {
        when(executionRequest.getExecutionFile()).thenReturn("file-name");
        job.setExecutionRequest(executionRequest);
        assertTrue(job.hasExecutionFile());
    }

    @Test
    public void hasExecutionFile_shouldReturnFalseIfExecutionFileIsNOTSet() {
        job.setExecutionRequest(executionRequest);
        assertFalse(job.hasExecutionFile());
    }
    

    @Test(expected = IllegalStateException.class)
    public void getExecutionFile_shouldThrowExceptionIfExecutionFileIsNotSet() {
        job.setExecutionRequest(executionRequest);
        job.getExecutionFile();
    }

    @Test(expected = IllegalStateException.class)
    public void getExecutionFileName_shouldThrowExceptionIfExecutionFileIsNotSet() {
        job.setExecutionRequest(executionRequest);
        job.getExecutionFileName();
    }

    @Test
    public void getExecutionFileName_shouldReturnExecutionFileName() {
        when(executionRequest.getExecutionFile()).thenReturn("file-path/file-name");
        job.setExecutionRequest(executionRequest);
        assertEquals("file-name",job.getExecutionFileName());
    }

    @Test(expected = IllegalStateException.class)
    public void getJobExecutionFilePath_shouldThrowExceptionIfExecutionFileIsNotSet() {
        job.setExecutionRequest(executionRequest);
        job.getJobExecutionFilePath();
    }
    
    @Test
    public void getJobExecutionFilePath_shouldReturnFullPathToExecutionFile() {
        when(executionRequest.getExecutionFile()).thenReturn("/file-path/file-name");
        Map<String,String> mockAttributes = Maps.newHashMap();
        mockAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "mock-location");
        when(executionRequest.getRequestAttributes()).thenReturn(mockAttributes);
        
        job.setExecutionRequest(executionRequest);
        assertEquals(new File("mock-location/JOB_ID/file-path/file-name"),job.getJobExecutionFilePath());
    }
    

    @Test
    public void getJobWorkingDirectory_shouldReturnFullPathToDirectoryContainingExecutionFile() {
        when(executionRequest.getExecutionFile()).thenReturn("/file-path/file-name");
        Map<String,String> mockAttributes = Maps.newHashMap();
        mockAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "mock-location");
        when(executionRequest.getRequestAttributes()).thenReturn(mockAttributes);
        job.setExecutionRequest(executionRequest);
        assertEquals(new File("mock-location/JOB_ID/file-path"),job.getJobWorkingDirectory());
    }

    @Test
    public void getJobWorkingDirectory_shouldReturnFullPathToJobDirectory() {
        Map<String,String> mockAttributes = Maps.newHashMap();
        mockAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "mock-location");
        when(executionRequest.getRequestAttributes()).thenReturn(mockAttributes);
        job.setExecutionRequest(executionRequest);
        assertEquals(new File("mock-location/JOB_ID"),job.getJobWorkingDirectory());
    }

    public void setProperty_shouldSetProperty() {
        job.setProperty("property","value");
        assertEquals(job.getData().get("property"),"value");
    }

    public void setProperty_shouldGetProperty() {
        Map<String,String> data = Maps.newHashMap();
        data.put("property", "value");
        job.setData(data);
        assertEquals(job.getProperty("property"),"value");
    }
    
    @Test(expected=NullPointerException.class)
    public void setProperty_shouldThrowExceptionIfPropertyIsNull() {
        job.setProperty(null,"value");
    }

    
    @Test(expected=NullPointerException.class)
    public void setProperty_shouldThrowExceptionIfValueIsNull() {
        job.setProperty("property",null);
        
    }

    @Test(expected=IllegalArgumentException.class)
    public void setProperty_shouldThrowExceptionIfPropertyIsBlank() {
        job.setProperty("  ","value");
    }

    
    @Test(expected=NullPointerException.class)
    public void getProperty_shouldThrowExceptionIfPropertyIsNull() {
        job.getProperty(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void getProperty_shouldThrowExceptionIfPropertyIsBlank() {
        job.getProperty("  ");
    }
    
}
