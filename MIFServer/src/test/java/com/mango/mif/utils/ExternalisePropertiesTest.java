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
package com.mango.mif.utils;

import static org.junit.Assert.*;

import java.io.File;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class ExternalisePropertiesTest {
	@Resource(name="notOverwritten")
	String notOverwritten;
	@Resource(name="overwritten")
	String overwritten;
	@Resource(name="userDir")
	String userDir;
	@Resource(name="testWorkingDirectory")
	File testWorkingDirectory;
	@Test
	public void shouldOverwritePropertiesFromFirstListedLocationIfOverwrittenLaterOne() {
		assertEquals("value-from-second-file",overwritten);
	}

	@Test
	public void shouldNotOverwritePropertiesFromFirstListedLocationIfLaterDoNotOverwrite() {
		assertEquals("value-from-first-file",notOverwritten);
	}

	@Test
	public void shouldInstantiateFileFromTwoStringAttributes() {
		assertNotNull(testWorkingDirectory);
	}
}
