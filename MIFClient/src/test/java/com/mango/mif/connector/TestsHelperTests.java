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
package com.mango.mif.connector;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;


/**
 * Tests Helper class test
 * 
 */
public class TestsHelperTests {

	File tmpDir;
	
	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tmpDir);
	}
	
    @Test
    public void shouldCreateATemporaryDirectory() {
        tmpDir = TestsHelper.createTmpDirectory("tmpDir");
        
        assertTrue(tmpDir.exists());
    }

}
