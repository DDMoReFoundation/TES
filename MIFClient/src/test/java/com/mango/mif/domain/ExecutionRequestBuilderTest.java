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
package com.mango.mif.domain;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;


/**
 * 
 * Execution Request Builder unit test
 * 
 */
public class ExecutionRequestBuilderTest {

    private static final String TEST_PASSWORD = "PASSWORD";
	private static final String USER_NAME = "NAME";
	private static final String EXEC_PARAM = "EXEC_PARAM";
	private static final String CONNECTOR_ID = "CONNECTOR_ID";
    private static final String PREAMBLE = "PREAMBLE";
    private static final String EXECUTION_FILE = "EXECUTION_FILE";
	private static final String REQUEST_ID = UUID.randomUUID().toString();

	@Test
    public void shouldReturnPopulatedExecutionRequestMsg() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder().setExecutionType(CONNECTOR_ID)
        .setRequestId(REQUEST_ID).setUserName(USER_NAME).setUserPassword(TEST_PASSWORD)
        .setExecutionFile(EXECUTION_FILE).setGridHostPreamble(PREAMBLE).setSubmitHostPreamble(PREAMBLE)
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "test-value")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "test-value")
        .setSubmitAsUserMode(true);
        
        String msg = builder.getExecutionRequestMsg();
        assertNotNull(msg);
        ExecutionRequest reqUnmarshalled = (ExecutionRequest)JAXBUtils.unmarshall(msg, ExecutionJaxbUtils.CONTEXT_CLASSES);
        validateRequest(reqUnmarshalled);
    }
	private void validateRequest(ExecutionRequest executionRequest) {
        assertThat("user name must match", executionRequest.getUserName(), is(equalTo(USER_NAME)));
        assertThat("user password must match", executionRequest.getUserPassword(), is(equalTo(TEST_PASSWORD)));
        assertThat("request id must match", executionRequest.getRequestId(), is(equalTo(REQUEST_ID)));
        assertThat("execution type name must match", executionRequest.getType(), is(equalTo(CONNECTOR_ID)));
        assertThat("submit as user mode must match", executionRequest.getSubmitAsUserMode(), is(true));
        assertThat("submit host preamble must match", executionRequest.getSubmitHostPreamble(), is(equalTo(PREAMBLE)));
        assertThat("grid host preamble must match", executionRequest.getGridHostPreamble(), is(equalTo(PREAMBLE)));
        assertThat("exeucution file must match", executionRequest.getExecutionFile(), is(equalTo(EXECUTION_FILE)));
	}
    @Test
    public void shouldReturnExecutionRequest() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder().setExecutionType(CONNECTOR_ID)
        .setRequestId(REQUEST_ID).setUserName(USER_NAME).setUserPassword(TEST_PASSWORD)
        .setExecutionFile(EXECUTION_FILE).setGridHostPreamble(PREAMBLE).setSubmitHostPreamble(PREAMBLE)
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "test-value")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "test-value")
        .setSubmitAsUserMode(true);
        ExecutionRequest reqUnmarshalled = builder.getExecutionRequest();
        validateRequest(reqUnmarshalled);
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionWhenExecutionTypeIsNotSet() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder()
            .setRequestId(REQUEST_ID).addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "test-value")
            .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "test-value");
        builder.getExecutionRequestMsg();
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionWhenExecutionRequestIdIsNotSet() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder()
        .setExecutionType(CONNECTOR_ID).addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "test-value")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "test-value");
        builder.getExecutionRequestMsg();
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionWhenEXECUTION_HOST_FILESHAREExecutionRequestAttributeIsNotSet() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder()
        .setExecutionType(CONNECTOR_ID).setRequestId(REQUEST_ID).addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "test-value");
        builder.getExecutionRequestMsg();
    }


    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionWhenEXECUTION_HOST_FILESHARE_REMOTEExecutionRequestAttributeIsNotSet() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder()
        .setExecutionType(CONNECTOR_ID).setRequestId(REQUEST_ID).addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "test-value");
        builder.getExecutionRequestMsg();
    }

    @Test
    public void shouldAlwaysReturnDifferentInstanceOfARequest() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder().setExecutionType(CONNECTOR_ID)
        .setRequestId(REQUEST_ID).setExecutionParameters(EXEC_PARAM)
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "test-value")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "test-value");
        ExecutionRequest first = builder.getExecutionRequest();
        ExecutionRequest second = builder.getExecutionRequest();
        
        assertTrue("Execution request builder should always return different instance of an execution request.", first!=second);
    }

    @Test
    public void shouldOverwriteAnyExistingRequestAttributes() throws JAXBException {
        ExecutionRequestBuilder builder = new ExecutionRequestBuilder().setExecutionType("CONNECTOR_ID")
        .setRequestId(REQUEST_ID).setExecutionParameters(EXEC_PARAM).addAttribute("TEST-ATTR", "test-value")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "test-value")
        .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "test-value");
        
        Map<String, String> attributes = Maps.newHashMap();
        attributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "new-test-value");
        attributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "new-test-value");
        builder.setRequestAttributes(attributes);
        
        ExecutionRequest request = builder.getExecutionRequest();
        
        assertEquals("Attribute was not overwritten.", "new-test-value",request.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()));
        assertEquals("Attribute was not overwritten.", "new-test-value",request.getRequestAttributes().get(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName()));
        assertFalse("Attribute was not removed.", request.getRequestAttributes().containsKey("TEST-ATTR"));
    }
}
