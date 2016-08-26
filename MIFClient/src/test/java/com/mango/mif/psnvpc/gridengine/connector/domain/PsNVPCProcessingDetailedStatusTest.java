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
package com.mango.mif.psnvpc.gridengine.connector.domain;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.psnvpc.gridengine.domain.PsNVPCProcessingDetailedStatus;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;


/**
 * Tests PsNVPCProcessingDetailedStatus
 */
public class PsNVPCProcessingDetailedStatusTest {
    /**
     * Logger
     */
    public final static Logger logger = Logger.getLogger(PsNVPCProcessingDetailedStatusTest.class);
    /**
     * Detailed Status
     */
    private PsNVPCProcessingDetailedStatus detailedStatus;
    
    @Before
    public void setUp() throws Exception {
        detailedStatus = new PsNVPCProcessingDetailedStatus();
        detailedStatus.setUserName("USER_NAME");
        detailedStatus.setRequestId("REQUEST_ID");
        detailedStatus.setSummary("SUMMARY");
    }

    @Test
    public void shouldMarshallAndUnmarshallDetailedStatus() throws JAXBException {
        String message = JAXBUtils.marshall(detailedStatus, ExecutionJaxbUtils.CONTEXT_CLASSES);
        logger.debug(message);
        PsNVPCProcessingDetailedStatus unmarshalled = JAXBUtils.unmarshall(message, ExecutionJaxbUtils.CONTEXT_CLASSES);
        
        assertEquals(detailedStatus.getRequestId(), unmarshalled.getRequestId());
        assertEquals(detailedStatus.getSummary(), unmarshalled.getSummary());
    }

}
