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
package com.mango.mif.client.impl;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mango.mif.client.api.TaskExecutionManager;
import com.mango.mif.client.api.TaskExecutionMessageHandler;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.client.domain.MIFSoftwareNames;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.exception.MIFException;

/**
 * This test is to verify the mock MIF behaviour that needs to be provided to the MIF clients.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MockClientWithSpringIntegrationTest implements TaskExecutionMessageHandler {

    /** The Constant LOG. */
    final static Logger LOG = Logger.getLogger(MockClientWithSpringIntegrationTest.class);

    //To track all threads are done
    /** The Constant THREAD_COUNT. */
    private static final int THREAD_COUNT = 5;

    /** The request map. */
    static Map<String, String> requestMap = Maps.newConcurrentMap();

    /** Used to synchronize test thread and requests handling. */
    static CountDownLatch signal = new CountDownLatch(THREAD_COUNT);

    @Resource(name="taskExecutionManager")
    private TaskExecutionManager taskExecutionManager;



    /**
     * Contains consumed Execution Responses
     */
    static Map<String, ExecutionResponse> resultsMap = Maps.newConcurrentMap();

    File tmpDir;

    @After
    public void tearDown() {
        if(tmpDir!=null) {
            FileUtils.deleteQuietly(tmpDir);
        }
    }
    
    @Before
    public void beforeTest(){
        tmpDir = Files.createTempDir();
        //Setting up the resource components
        taskExecutionManager.setExecutionMessageHandler(this);
    }

    @DirtiesContext
    @Test(timeout=120000)
    public void shouldAddTheRequestToTheRegistry() throws JAXBException, InterruptedException, MIFException{

        for (int i = 0; i < THREAD_COUNT; i++) {

            ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder();

            String requestID = UUID.randomUUID().toString();
            requestBuilder.setRequestId(requestID)
            .setExecutionFile("MOCK_R_SCRIPT_FILE")
            .setExecutionType(MIFSoftwareNames.R_COMMAND_LINE.name())
            .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), tmpDir.getAbsolutePath())
            .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), tmpDir.getAbsolutePath());
            
            ExecutionRequest request = requestBuilder.getExecutionRequest();
            String requestMsg = requestBuilder.getExecutionRequestMsg();
            LOG.info("Sending the request:" + requestMsg);

            requestMap.put(request.getRequestId(), requestMsg);
            taskExecutionManager.submit(request);

        }
        signal.await();
    }

    @Override
    public void handle(ExecutionResponse executionResponse) {
        if(checkIfTheReceivedTextContainsPartOfRequest(executionResponse)){
            LOG.info("Closing the request " +  executionResponse.getRequestId() );
            signal.countDown();
        } else {
            LOG.error("Received unexpected response.");
        }
    }

    /**
     * Checks if the response text contains the unique ID which are submitted as part of requests.
     * @param executionResponse
     * @return
     */
    private boolean checkIfTheReceivedTextContainsPartOfRequest(ExecutionResponse executionResponse) {
        Preconditions.checkNotNull(executionResponse, "Received text message should not be null");
        String uuid = executionResponse.getRequestId().toString();
        for(String key : requestMap.keySet()) {
            if(uuid.equals(key.toString())) {
                resultsMap.put(uuid, executionResponse);
                return true;
            }
        }
        return false;
    }

}
