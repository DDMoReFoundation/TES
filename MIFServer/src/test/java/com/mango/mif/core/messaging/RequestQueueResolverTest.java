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
package com.mango.mif.core.messaging;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.messaging.RequestQueueResolver;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.TestsHelper;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;

/**
 * 
 * Tests Execution Request queue resolver
 */
public class RequestQueueResolverTest {
	/**
	 * Tested instance
	 */
	private RequestQueueResolver queueResolver;
	/**
	 * connectors registry
	 */
	@Mock ConnectorsRegistry connectorsRegistry;
	/**
	 * job
	 */
	Job job;
	/**
	 * connector
	 */
	@Mock Connector connector;
	/**
	 * jobID
	 */
	String jobID;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		queueResolver = new RequestQueueResolver();
		queueResolver.setConnectorsRegistry(connectorsRegistry);
		jobID = UUID.randomUUID().toString();
		job = TestsHelper.createJob(jobID.toString(),jobID );
		job.getExecutionRequest().setType("TEST-EXECUTION-TYPE");
		when(connectorsRegistry.getConnectorByExecutionType("TEST-EXECUTION-TYPE")).thenReturn(connector);
		when(connector.getRequestQueue()).thenReturn("MOCK-QUEUE");
	}

	@Test
	public void shouldResolveToAConnectorSpecificQueue() throws JAXBException {
		String queue = queueResolver.resolve(JAXBUtils.marshall(job.getExecutionRequest(), ExecutionJaxbUtils.CONTEXT_CLASSES));
		assertEquals("MOCK-QUEUE",queue);
	}

	@Test
	public void shouldResolveToEmptyQueueNameIfExecutionTypeNotFound() throws JAXBException {
		job.getExecutionRequest().setType("TEST-EXECUTION-TYPE-DOES-NOT-EXIST");
		String queue = queueResolver.resolve(JAXBUtils.marshall(job.getExecutionRequest(), ExecutionJaxbUtils.CONTEXT_CLASSES));
		assertTrue(StringUtils.isEmpty(queue));
	}
}