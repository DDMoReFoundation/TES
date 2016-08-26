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
package com.mango.mif.core.exec.ce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;


public class CeInvokerTest {
    private final Logger LOGGER = Logger.getLogger(CeInvokerTest.class);
    
    private CeInvoker invoker;

    /**
     * Set-up tasks prior to each test being run.
     */
    @Before
    public void setUp() {
        this.invoker = new CeInvoker();
    }

    /**
     * Test method for {@link com.mango.mif.core.exec.ce.CeInvoker#execute(java.lang.String)}.
     * <p>
     * @throws ExecutionException 
     * @throws IOException 
     */
    @Test
    public void testExecute() throws ExecutionException, IOException {
        final File outputFile = new File(FileUtils.getTempDirectory(), "CeInvokerTest.out");
        final InvokerResult invokerResult = this.invoker.execute("echo \"I Woz Ere\" >" + outputFile);
        LOGGER.info("Output stream:\n" + invokerResult.getOutputStream());
        LOGGER.info("Error stream:\n" + invokerResult.getErrorStream());
        assertEquals("Exit code should be 0", 0, invokerResult.getExitStatus());
        assertTrue("Output file should have been created", outputFile.exists());
        assertTrue("Checking content of output file", FileUtils.readFileToString(outputFile).contains("I Woz Ere"));
        outputFile.delete();
    }
    
    /**
     * Test method for {@link com.mango.mif.core.exec.ce.CeInvoker#execute(java.lang.String)}.
     * <p>
     * Only relevant for Linux.
     * <p>
     * @throws ExecutionException 
     * @throws IOException 
     */
    @Test
    public void whenShellScriptHasShebangLine_thenShouldUseThisShell() throws ExecutionException, IOException {
    	if (!SystemUtils.IS_OS_WINDOWS) {
    		final File outputFile = new File(FileUtils.getTempDirectory(), "CeInvokerTest.out");
    		final InvokerResult invokerResult = this.invoker.execute("#!/bin/false\necho \"I Woz Ere\" >" + outputFile);
    		LOGGER.info("Output stream:\n" + invokerResult.getOutputStream());
    		LOGGER.info("Error stream:\n" + invokerResult.getErrorStream());
    		assertFalse("Exit code should be nonzero", invokerResult.getExitStatus() == 0);
    		outputFile.delete();
		}
    }

}
