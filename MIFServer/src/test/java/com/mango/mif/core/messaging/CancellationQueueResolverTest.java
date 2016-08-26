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

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.messaging.CancellationQueueResolver;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.utils.TestsHelper;

/**
 * Tests cancellation queue resolver
 */
public class CancellationQueueResolverTest {
	/**
	 * Tested instance
	 */
	private CancellationQueueResolver queueResolver;
	/**
	 * Job repository
	 */
	@Mock JobManagementService jobManagementService;
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
		queueResolver = new CancellationQueueResolver();
		queueResolver.setJobManagementService(jobManagementService);
		queueResolver.setConnectorsRegistry(connectorsRegistry);
		jobID = UUID.randomUUID().toString();
		job = TestsHelper.createJob(jobID.toString(),jobID );
		job.getExecutionRequest().setType("TEST-EXECUTION-TYPE");
		when(jobManagementService.getJob(jobID.toString())).thenReturn(job);
		when(connectorsRegistry.getConnectorByExecutionType("TEST-EXECUTION-TYPE")).thenReturn(connector);
		when(connector.getCancellationQueue()).thenReturn("MOCK-CANCELLATION-QUEUE");
		
	}

	@Test
	public void shouldResolveToAConnectorSpecificCancellationQueue() {
		String queue = queueResolver.resolve(jobID.toString());
		assertNotNull(queue);
		assertTrue(!StringUtils.isEmpty(queue));
	}

	@Test
	public void shouldResolveToEmptyQueueNameIfJobWithTheGivenIdNotFound() {
		String queue = queueResolver.resolve("NOT-EXISTING-JOB-ID");
		assertNotNull(queue);
		assertTrue(StringUtils.isEmpty(queue));
	}
}