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
package com.mango.mif.nonmem.gridengine.connector.domain;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.nonmem.gridengine.domain.NONMEMProcessingDetailedStatus;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;


/**
 * 
 * Tests NONMEMProcessingDetailedStatus
 */
public class NONMEMProcessingDetailedStatusTest {
    /**
     * Logger
     */
    public final static Logger logger = Logger.getLogger(NONMEMProcessingDetailedStatusTest.class);
    /**
     * Detailed Status
     */
    private NONMEMProcessingDetailedStatus detailedStatus;
    
    @Before
    public void setUp() throws Exception {
        detailedStatus = new NONMEMProcessingDetailedStatus();
        
        detailedStatus.setControlFileName("CONTROL_FILE");
        detailedStatus.setDataFileName("DATA_FILE");
        detailedStatus.setDuration("DURATION");
        detailedStatus.setProblem("PROBLEM");
        detailedStatus.setUserName("USER_NAME");
        detailedStatus.setRequestId("REQUEST_ID");
        detailedStatus.setSummary("SUMMARY");
        detailedStatus.setStatus("STATUS");
    }

    @Test
    public void shouldMarshallAndUnmarshallDetailedStatus() throws JAXBException {
        String message = JAXBUtils.marshall(detailedStatus, ExecutionJaxbUtils.CONTEXT_CLASSES);
        logger.debug(message);
        NONMEMProcessingDetailedStatus unmarshalled = JAXBUtils.unmarshall(message, ExecutionJaxbUtils.CONTEXT_CLASSES);
        
        assertEquals(detailedStatus.getControlFileName(), unmarshalled.getControlFileName());
        assertEquals(detailedStatus.getDataFileName(), unmarshalled.getDataFileName());
    }

}
