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
package com.mango.mif.connector.runner.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.mango.mif.connector.ConnectorExceptions;
import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.connector.runner.impl.statehandler.StateHandlerException;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.domain.ObjectStateUpdater;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.MIFProperties;
import com.mango.mif.utils.MIFTestProperties;
import com.mango.mif.utils.TestsHelper;


/**
 * Tests default job runner implementation
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJobRunnerTest {
    private final static Logger LOG = Logger.getLogger(DefaultJobRunnerTest.class);

    @Mock JmsTemplate jmsTemplate;
    @Mock SCXMLDriver driver;
    @Mock Job job;
    @Mock DetailedStatus detailedStatus;
    @Mock JobManagementService jobManagementService;

    /**
     * instance being tested
     */
    private DefaultJobRunner jobRunner;

    /**
     * Job results queue name used in tests.
     */
    private final static String MOCK_JOB_RESULT_QUEUE_NAME = "MOCK.DESTINATION";

    private static final String MOCK_JOB_ID = "JOB_ID";

    private File tmpDir;

    @Before
    public void setUp() throws Exception {
        tmpDir = TestsHelper.createTmpDirectory();
        Properties properties = TestsHelper.createPropertiesFromStream(DefaultJobRunnerTest.class.getResourceAsStream(MIFTestProperties.MIF_CORE_TEST_CONTEXT));
        MIFProperties.loadProperties(properties);
        jobRunner = new DefaultJobRunner();
        jobRunner.setDriver(driver);
        jobRunner.setJmsTemplate(jmsTemplate);
        jobRunner.setJobManagementService(jobManagementService);
        jobRunner.setResultDestination(MOCK_JOB_RESULT_QUEUE_NAME);
        jobRunner.setJobId(MOCK_JOB_ID);
        when(job.getDetailedStatus()).thenReturn(detailedStatus);
        when(detailedStatus.getSummary()).thenReturn("SUMMARY_MESSAGE");
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.deleteDirectory(tmpDir);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Test(expected = NullPointerException.class)
    public void main_shouldThrowExceptionWhenJobIsNotSet() throws InterruptedException {
        jobRunner.setJobId(null);
        jobRunner.main();
    }

    @Test(expected = NullPointerException.class)
    public void main_shouldThrowExceptionWhenDriverIsNotSet() throws InterruptedException {
        jobRunner.setDriver(null);
        jobRunner.main();
    }

    @Test(expected = NullPointerException.class)
    public void main_shouldThrowExceptionWhenResultDestinationIsNotSet() throws InterruptedException {
        jobRunner.setResultDestination(null);
        jobRunner.main();
    }

    @Test
    public void startProcessing_should_trigger_processing_on_the_driver() {
        jobRunner.startProcessing();
        verify(driver).start();
        verify(jobManagementService).update(eq(MOCK_JOB_ID), (ObjectStateUpdater<Job>)any());
    }

    @Test
    public void cancelProcessing_should_trigger_cancelation_on_the_driver() {
        jobRunner.cancelProcessing();
        verify(driver).cancel();
    }

    @Test
    public void shouldPublishResultMessageOnJobResultQueue() {
        jobRunner.publishResultsMessage();
        verify(jobManagementService).update(eq(MOCK_JOB_ID), (ObjectStateUpdater<Job>)any());
    }

    @Test
    public void shouldCreateJobUpdaterSubmittingJMSMessageAndPerssitingAJobWithAppropriateState() {
        String mockResultMessage = "MOCK MESSAGE";
        ObjectStateUpdater<Job> updater = jobRunner.createUpdater(jmsTemplate, MOCK_JOB_RESULT_QUEUE_NAME, mockResultMessage);
        updater.update(job);

        verify(jmsTemplate).send(eq(MOCK_JOB_RESULT_QUEUE_NAME), (MessageCreator) any());
        verify(job).setJobStatus(JobStatus.PROCESSING_FINISHED);
    }

    @Test
    public void shouldPrepareCancelResultMsgIfJobHasBeenCancelled() throws JAXBException {
        when(driver.getProcessingStatus()).thenReturn(DriverProcessingStatus.CANCELLED);

        String resultMsg = jobRunner.prepareResponse();

        ExecutionResponse response = unmarshallExecutionResponse(resultMsg);

        assertNotNull(response);
        assertEquals(JobStatus.CANCELLED.name(),response.getStatus());
    }

    @Test
    public void shouldPrepareFailureResultMsgIfJobHasFailed() throws JAXBException {
        when(driver.getProcessingStatus()).thenReturn(DriverProcessingStatus.FAILED);

        String resultMsg = jobRunner.prepareResponse();

        ExecutionResponse response = unmarshallExecutionResponse(resultMsg);

        assertNotNull(response);
        assertEquals(JobStatus.FAILED.name(),response.getStatus());

    }

    @Test
    public void shouldPrepareResultMsgIfJobHasBeenCompleted() throws JAXBException {
        when(driver.getProcessingStatus()).thenReturn(DriverProcessingStatus.FINISHED);

        String resultMsg = jobRunner.prepareResponse();

        ExecutionResponse response = unmarshallExecutionResponse(resultMsg);
        assertNotNull(response);
        assertEquals(JobStatus.COMPLETED.name(),response.getStatus());
    }

    @Test
    public void shouldIncludeAnyErrorsThatWereThrownDuringExecutionInTheResultElement() throws JAXBException {
        when(driver.getProcessingStatus()).thenReturn(DriverProcessingStatus.FAILED);
        when(driver.getException()).thenReturn(buildExceptionTree());
        String resultMsg = jobRunner.prepareResponse();

        ExecutionResponse response = unmarshallExecutionResponse(resultMsg);
        assertNotNull(response);
        assertEquals(JobStatus.FAILED.name(),response.getStatus());
        assertNotNull(response.getResult());
    }

    @Test
    public void shouldReturnAMessageFromExecutionException() {
        Exception e = buildExceptionTree();

        String expected = "State Handler Exception message" +
                "\n" +
                "Any interim exception"+
                "\n" +
                "This is the real cause to be displayed to the user";

        String actual = jobRunner.prepareResultMessage(e);

        // This over elaborate rubbish because assertEqual outputs utter gibberish if
        // the strings don't match.
        if (!expected.equals(actual)) {
            System.out.println("expected: \"" + expected + "\"");
            System.out.println("actual: \"" + actual + "\"");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void shouldReturnADetailedMessageFromExecutionException() {
        Exception e = buildExceptionTree();
        assertNotNull(jobRunner.prepareDetailedResultMessage(e));
    }

    /**
     * Builds example exception tree
     * @return
     */
    private Exception buildExceptionTree() {
        String clientErrorMessage = "This is the real cause to be displayed to the user";
        Exception e = new Exception("A cause");
        e = new ExecutionException(clientErrorMessage,e);
        e = new Exception("Any interim exception",e);
        e = new StateHandlerException("State Handler Exception message", e);
        return e;
    }

    @Test
    public void shouldReturnMostParentExceptionMessageIfExecutionExceptionNotFound() {
        String clientErrorMessage = "State Handler Exception message.";
        Exception e = new Exception("Random Exception");
        e = new Exception("Any interim exception",e);
        e = new ExecutionException(clientErrorMessage, e);

        String expected = clientErrorMessage;
        String actual = jobRunner.prepareResultMessage(e);

        if (!expected.equals(actual)) {
            System.out.println("expected: \"" + expected + "\"");
            System.out.println("actual: \"" + actual + "\"");
            assertEquals(expected, actual);
        }
    }

    /**
     * unmarshalls response message
     * @param jobRunnerResponse
     * @return
     */
    private ExecutionResponse unmarshallExecutionResponse(String jobRunnerResponse) {
        ExecutionResponse response = null;
        try {
            response = JAXBUtils.unmarshall(ExecutionResponse.class , jobRunnerResponse);
        } catch (JAXBException e) {
            LOG.error("JAXB Exception for the string " + jobRunnerResponse);
            throw new RuntimeException(ConnectorExceptions.INVALID_RESPONSE_MESSAGE_FORMAT.getMessage());
        }
        return response;
    }
}
