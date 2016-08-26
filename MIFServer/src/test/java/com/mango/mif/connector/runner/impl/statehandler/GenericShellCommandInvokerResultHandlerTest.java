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
package com.mango.mif.connector.runner.impl.statehandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.mango.mif.connector.runner.impl.statehandler.GenericShellCommandInvokerResultHandler;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;


/**
 * Tests GenericShellCommandInvokerResultHandler
 */
public class GenericShellCommandInvokerResultHandlerTest {
	/**
	 * Submit result handler
	 */
	private GenericShellCommandInvokerResultHandler resultHandler;
	/**
	 * job
	 */
	@Mock Job job;
	/**
	 * job
	 */
	@Mock InvokerResult invokerResult;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		resultHandler = new GenericShellCommandInvokerResultHandler("TEST.EVENT");
		resultHandler.setJob(job);
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfJobIsNotSet() throws ExecutionException {
		resultHandler.setJob(null);
		resultHandler.handle(invokerResult);
	}

	@Test(expected=NullPointerException.class)
	public void shouldThrowExceptionIfInvokerResultHandlerIsNotSet() throws ExecutionException {
		resultHandler.handle(null);
	}

	@Test(expected=ExecutionException.class)
	public void shouldThrowExceptionIfInvokerResultRepresentsFailedExecution() throws ExecutionException {
		when(invokerResult.getExitStatus()).thenReturn(1); //error exit code
		resultHandler.handle(invokerResult);
	}
}
