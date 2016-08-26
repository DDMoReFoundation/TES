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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * Test for {@link ExecutionResponse}.
 * Jaxb test for ExecutionResponse.
 */
public class ExecutionResponseJaxbTest {
	final static Logger LOG = Logger.getLogger(ExecutionResponseJaxbTest.class);
	
	private static final String REQ_STATUS = "SUCCESS";
	private static final String REQ_ID = UUID.randomUUID().toString();
	


	/**
	 * Test jaxb for execution request.
	 *
	 * @throws JAXBException the jAXB exception
	 */
	@Test
	public void testJaxbForExecutionRequest() throws JAXBException {

	        List<String> test = new ArrayList<String>();
	        test.add("file1");
	        test.add("file2");
	        test.add("file3");
	        
		   ExecutionResponse executionResponse = new ExecutionResponse();
	       executionResponse.setRequestId(REQ_ID);
	       executionResponse.setStatus(REQ_STATUS);
	       executionResponse.setJobFiles(test);
	       
	       String marshallRequest = JAXBUtils.marshall(executionResponse, ExecutionJaxbUtils.CONTEXT_CLASSES);
	       
	       ExecutionResponse reqUnmarshalled = (ExecutionResponse)JAXBUtils.unmarshall(marshallRequest, ExecutionJaxbUtils.CONTEXT_CLASSES);
	       
	       LOG.info("THE REQUEST IS NOW" + marshallRequest);
	       
	        assertThat("Unmarshalled response must not be null ",reqUnmarshalled, is(notNullValue()));
			assertThat("Unmarshalled response ID must match ",reqUnmarshalled.getRequestId(), is(equalTo(REQ_ID)));
			assertThat("Unmarshalled  response : Status ,  must not be null ",reqUnmarshalled.getStatus(), is(notNullValue()));
			assertThat("Unmarshalled  response : Status ,  must match ",reqUnmarshalled.getStatus(), is(equalTo(REQ_STATUS)));
			assertThat("Unmarshalled  response : Files Size ,  must match ",reqUnmarshalled.getJobFiles().size(), is(equalTo(3)));
	}
	
}
