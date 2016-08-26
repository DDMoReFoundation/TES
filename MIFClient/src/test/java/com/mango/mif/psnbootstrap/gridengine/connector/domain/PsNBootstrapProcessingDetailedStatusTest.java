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
package com.mango.mif.psnbootstrap.gridengine.connector.domain;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.nonmem.gridengine.domain.NONMEMProcessingDetailedStatus;
import com.mango.mif.psnbootstrap.gridengine.domain.PsNBootstrapProcessingDetailedStatus;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;


/**
 * 
 * Tests {@link PsNBootstrapProcessingDetailedStatus}
 */
public class PsNBootstrapProcessingDetailedStatusTest {
    /**
     * Logger
     */
    public final static Logger logger = Logger.getLogger(PsNBootstrapProcessingDetailedStatusTest.class);
    /**
     * Detailed Status
     */
    private PsNBootstrapProcessingDetailedStatus detailedStatus;
    
    @Before
    public void setUp() throws Exception {
        detailedStatus = new PsNBootstrapProcessingDetailedStatus();
        detailedStatus.setControlFileName("CONTROL_FILE");
        detailedStatus.setDuration("DURATION");
        detailedStatus.setUserName("USER_NAME");
        detailedStatus.setRequestId("REQUEST_ID");
        detailedStatus.setSummary("SUMMARY");
        detailedStatus.addActiveChildNONMEMProcess(createNONMEMChildProcess(1));
        detailedStatus.addActiveChildNONMEMProcess(createNONMEMChildProcess(2));
        detailedStatus.addActiveChildNONMEMProcess(createNONMEMChildProcess(3));
    }
    /**
     * Creates a child process instance
     */
    private NONMEMProcessingDetailedStatus createNONMEMChildProcess(int index) {
        NONMEMProcessingDetailedStatus childProcess = new NONMEMProcessingDetailedStatus();
        childProcess.setControlFileName("CONTROL_FILE" +index);
        childProcess.setDataFileName("DATA_FILE");
        childProcess.setDuration("DURATION");
        childProcess.setProblem("PROBLEM");
        childProcess.setUserName("USER_NAME");
        childProcess.setRequestId("REQUEST_ID");
        childProcess.setSummary("SUMMARY");
        childProcess.setStatus("STATUS");
        return childProcess;
    }

    @Test
    public void shouldMarshallAndUnmarshallDetailedStatus() throws JAXBException {
        String message = JAXBUtils.marshall(detailedStatus, ExecutionJaxbUtils.CONTEXT_CLASSES);
        logger.debug(message);
        PsNBootstrapProcessingDetailedStatus unmarshalled = JAXBUtils.unmarshall(message, ExecutionJaxbUtils.CONTEXT_CLASSES);
        
        assertEquals(detailedStatus.getControlFileName(), unmarshalled.getControlFileName());
        assertEquals(detailedStatus.getDuration(), unmarshalled.getDuration());
        assertEquals(detailedStatus.getActiveChildNONMEMProcesses().size(),unmarshalled.getActiveChildNONMEMProcesses().size() );
    }

}
