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
package com.mango.mif.rest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.mango.mif.client.api.rest.JobSummaryMessages;
import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.client.api.rest.ResponseStatus;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.ce.CeInvoker;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.invoker.InvokerHelperFactory;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.exception.MIFException;


/**
 * 
 * Tests implementation of JobService
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplTest {

    private static final String REQUEST_QUEUE="REQUEST_QUEUE";
    private static final String EXECUTION_TYPE = "EXECUTION_TYPE";
    
    @Mock JobManagementService jobManagementService;
    
    @Mock MessageContext messageContext;
    
    @Mock Job job;
    
    @Mock ExecutionRequest executionRequest;

    @Mock JmsTemplate jmsTemplate;
    
    @Mock ConnectorsRegistry connectorsRegistry;
    
    @Mock Connector connector;

    @Mock InvokerHelperFactory invokerHelperFactory;
    
    @InjectMocks
    JobServiceImpl jobService = new JobServiceImpl();
    
    @Before
    public void setUp() throws Exception {
        when(job.getExecutionRequest()).thenReturn(executionRequest);
        when(executionRequest.getType()).thenReturn(EXECUTION_TYPE);        
        when(connectorsRegistry.getConnectorByExecutionType(EXECUTION_TYPE)).thenReturn(connector);

        Invoker invoker = new CeInvoker();
        when(job.getInvoker()).thenReturn(invoker);
        jobService.setRequestQueue(REQUEST_QUEUE);
    }

    
    @Test
    public void getExecutionResponse_shouldReturnAnExecutionResponseForAJob() throws ExecutionException, MIFException, JAXBException {
        ExecutionResponse executionResponse = mock(ExecutionResponse.class);
        when(job.getExecutionResponse()).thenReturn(executionResponse);
        when(jobManagementService.getJob("1")).thenReturn(job);
        ExecutionResponse response = jobService.getExecutionResponse("1");
        assertNotNull(response);
    }

    @Test(expected=MIFException.class)
    public void getExecutionResponse_shouldThrowAnExceptionIfJobDoesNotExist() throws ExecutionException, MIFException, JAXBException {
        when(jobManagementService.getJob("1")).thenReturn(null);
        jobService.getExecutionResponse("1");
    }

    @Test(expected=MIFException.class)
    public void getExecutionResponse_shouldThrowAnExceptionIfExecutionResponseIsNotSet() throws ExecutionException, MIFException, JAXBException {
        when(jobManagementService.getJob("1")).thenReturn(job);
        jobService.getExecutionResponse("1");
    }
    

    @Test
    public void cancel_shouldResultInFailureIfJobIdNotSet() throws ExecutionException {
        when(jobManagementService.getJob("1")).thenReturn(job);
        MIFResponse response = jobService.cancel(null);
        assertEquals(ResponseStatus.FAILURE,response.getStatus());
        
        response = jobService.cancel("");
        assertEquals(ResponseStatus.FAILURE,response.getStatus());
    }

    @Test
    public void cancel_shouldResultInFailureIfJobNotFound() throws ExecutionException {
        when(jobManagementService.getJob("1")).thenReturn(null);
        MIFResponse response = jobService.cancel("1");
        assertEquals(ResponseStatus.FAILURE,response.getStatus());
    }
    

    @Test
    public void cancel_shouldResultInFailureIfConnectorNotFound() throws ExecutionException {
        when(jobManagementService.getJob("1")).thenReturn(job);
        when(job.getConnectorId()).thenReturn("NOT-FOUND");
        when(connectorsRegistry.getConnectorById("NOT-FOUND")).thenReturn(null);
        MIFResponse response = jobService.cancel("1");
        assertEquals(ResponseStatus.FAILURE,response.getStatus());
    }
    
    @Test
    public void cancel_shouldResultInSuccess() throws ExecutionException {
        when(jobManagementService.getJob("1")).thenReturn(job);
        when(job.getConnectorId()).thenReturn("CONNECTOR-ID");
        Connector connector = mock(Connector.class);
        when(connectorsRegistry.getConnectorById("CONNECTOR-ID")).thenReturn(connector);
        when(connector.getCancellationQueue()).thenReturn("CANCEL-QUEUE");
        MIFResponse response = jobService.cancel("1");
        verify(jmsTemplate).send(eq("CANCEL-QUEUE"),(MessageCreator)any());
        assertEquals(ResponseStatus.SUCCESS,response.getStatus());
    }

    @Test(expected=MIFException.class)
    public void execute_should_throw_exception_when_execution_request_is_empty() throws MIFException, JAXBException {
        jobService.execute("");
    }

    @Test
    public void execute_should_schedule_an_execution_request() throws MIFException, JAXBException {
        String expected = "REQUEST_ID";
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder();
        requestBuilder.setRequestId(expected).setExecutionType(EXECUTION_TYPE)
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE");

        String response = jobService.execute(requestBuilder.getExecutionRequestMsg());

        assertEquals(expected,response);
        verify(jmsTemplate).send(eq(REQUEST_QUEUE),(MessageCreator)any());
    }

    @Test(expected=MIFException.class)
    public void execute_should_throw_exception_when_connector_for_execution_type_does_not_exist() throws MIFException, JAXBException {
        String expected = "REQUEST_ID";
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder();
        requestBuilder.setRequestId(expected).setExecutionType("INVALID_EXECUTION_TYPE")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE");

        jobService.execute(requestBuilder.getExecutionRequestMsg());
    }
    
    @Test
    public void getJobStatus_shouldRetrnAJobStatus() {
        when(jobManagementService.getJob("REQUEST_ID")).thenReturn(job);
        when(job.getJobStatus()).thenReturn(JobStatus.PROCESSING);
        String status = jobService.getJobStatus("REQUEST_ID");
        assertEquals(JobStatus.PROCESSING.name(), status);
    }

    //FIXME should use HTTP 404 status
    @Test
    public void getJobStatus_shouldReturnNOT_AVAILABLEifJobDoesNotExist() {
        String status = jobService.getJobStatus("REQUEST_ID");
        assertEquals(JobStatus.NOT_AVAILABLE.name(), status);
    }

    @Test
    public void getJobDirectory_shouldReturnJobDirectory() {
        when(jobManagementService.getJob("REQUEST_ID")).thenReturn(job);
        when(job.getJobDirectory()).thenReturn(new File("/not/existing/file"));
        String result = jobService.getJobDirectory("REQUEST_ID");
        assertEquals(new File("/not/existing/file").getAbsolutePath(),result);
    }

    //FIXME should use HTTP 404 status
    @Test
    public void getJobDirectory_shouldReturnEmpty() {
        String result = jobService.getJobDirectory("REQUEST_ID");
        assertEquals("",result);
    }

    @Test
    public void getRunningJobsSummaryMessages_shouldReturnSummaryMessages() {
        Map<String,String> summaries = Maps.newHashMap();
        summaries.put("job-1", "summary-1");
        summaries.put("job-2", "summary-2");
        when(jobManagementService.getSummaryMessagesForRunningJobs()).thenReturn(summaries);
        JobSummaryMessages result = jobService.getRunningJobsSummaryMessages();
        assertEquals(summaries,result.getSummaryMessages());
        assertEquals(ResponseStatus.SUCCESS,result.getStatus());
    }

    @Test
    public void getRunningJobsSummaryMessages_shouldReturnAnEmptyResultWithFailureStatus() {
        doThrow(Exception.class).when(jobManagementService).getSummaryMessagesForRunningJobs();
        JobSummaryMessages result = jobService.getRunningJobsSummaryMessages();
        assertEquals(ResponseStatus.FAILURE,result.getStatus());
    }
    
    @Test
    public void getAcknowledgedJobsByDate_shouldReturnAcknowledgedJobs() {
        String[] ackJobs = new String[] {"job1", "job2" };
        when(jobManagementService.getAcknowledgedJobsToDate(1234l)).thenReturn(ackJobs);
        String[] result = jobService.getAcknowledgedJobsByDate(1234l);
        assertTrue(Arrays.equals(ackJobs,result));
    }

    @Test
    public void indextext_shouldGenerateSchema() {
        assertNotNull(jobService.indextext());
    }

    @Test
    public void indexweb_shouldGenerateSchema() {
        assertNotNull(jobService.indexweb());
    }

    @Test(expected = MIFException.class)
    public void getDetailedStatus_shouldThrowExceptionIfJobDoesNotExist() throws MIFException {
        jobService.getDetailedStatus("REQUEST_ID");
    }
    
    @Test
    public void getDetailedStatus_shouldReturnDetailedStatus() throws MIFException {
        when(jobManagementService.getJob("REQUEST_ID")).thenReturn(job);
        DetailedStatus detailedStatus = mock(DetailedStatus.class);
        when(job.getDetailedStatus()).thenReturn(detailedStatus);
        
        DetailedStatus result = jobService.getDetailedStatus("REQUEST_ID");
        assertTrue(detailedStatus==result);
    }

    @Test(expected=MIFException.class)
    public void getDetailedStatus_shouldThrowExceptionIfDetailedStatusForGivenJobDoesntExist() throws MIFException {
        when(jobManagementService.getJob("REQUEST_ID")).thenReturn(job);
        when(job.getDetailedStatus()).thenReturn(null);
        
        jobService.getDetailedStatus("REQUEST_ID");
    }
    
    @Test
    public void getFullFileContents_shouldReturnFullFileContents() throws MIFException, ExecutionException, UnsupportedEncodingException {
        InvokerHelper invokerHelper = mock(InvokerHelper.class);
        Invoker invoker = mock(Invoker.class);
        when(invokerHelperFactory.create((Invoker)invoker)).thenReturn(invokerHelper);
        when(job.getInvoker()).thenReturn(invoker);
        when(jobManagementService.getJob("REQUEST_ID")).thenReturn(job);
        when(invokerHelper.getFileContents((File)any())).thenReturn("mock-contents");
        byte[] fileContents = jobService.getFullFileContents("REQUEST_ID", "mock/file");
        assertTrue(Arrays.equals("mock-contents".getBytes(Charsets.UTF_8.name()),fileContents));
    }

    //FIXME should use HTTP 404 status
    @Test
    public void getFullFileContents_shouldReturnEmptyStringForMissingJob() {
        String result = jobService.getJobDirectory("REQUEST_ID");
        assertEquals("",result);
    }

    //FIXME should use HTTP 404 status
    @Test
    public void getTailedFileContents_shouldReturnEmptyFileContentIfJobDoesNotExist() throws MIFException {
        byte[] result = jobService.getTailedFileContents("REQUEST_ID","mock/path");
        assertTrue(Arrays.equals("".getBytes(),result));
    }

    @Test
    public void getTailedFileContents_shouldRetrieveTailOfAFile() throws ExecutionException, MIFException {
        InvokerHelper invokerHelper = mock(InvokerHelper.class);
        Invoker invoker = mock(Invoker.class);
        when(invokerHelperFactory.create((Invoker)invoker)).thenReturn(invokerHelper);
        when(job.getInvoker()).thenReturn(invoker);
        when(jobManagementService.getJob("REQUEST_ID")).thenReturn(job);
        InvokerResult invokerResult = mock(InvokerResult.class);
        when(invokerResult.getOutputStream()).thenReturn("Mock-Output");
        when(invokerHelper.runAndReportFailures((String)any())).thenReturn(invokerResult);
        byte[] result = jobService.getTailedFileContents("REQUEST_ID", "mock/file");
        assertTrue(Arrays.equals("Mock-Output".getBytes(),result));
    }
    
    @Test(expected=MIFException.class)
    public void listDir_shouldThrowExceptionIfJobDoesNotExist() throws MIFException {
        jobService.listDir("REQUEST_ID", "mock/end/path");
    }
    
}
