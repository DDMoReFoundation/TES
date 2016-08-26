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
package com.mango.mif.client.api.rest;

import static org.junit.Assert.*;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.exception.MIFException;


public class MIFExceptionMappingTest {
    
    private final static Logger logger = Logger.getLogger(MIFExceptionMappingTest.class);
    
    MIFExceptionResponseMapper responseMapper;
    MIFExceptionMapper exceptionMapper;
    
    
    @Before
    public void setUp() throws Exception {
        responseMapper = new MIFExceptionResponseMapper();
        exceptionMapper = new MIFExceptionMapper();
    }

    @Test
    public void shouldMapAndUnmapExceptionFromResponse() {
        MIFException exception = new MIFException("Error message");
        
        Response response = exceptionMapper.toResponse(exception);

        MIFException unmarshalled = responseMapper.fromResponse(response);
        
        assertEquals(exception.getMessage(), unmarshalled.getMessage());
    }

}
