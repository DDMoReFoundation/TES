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
package com.mango.mif.psnscm.gridengine.connector.domain;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.psnscm.gridengine.domain.PsNSCMModelProcessingStatus;
import com.mango.mif.psnscm.gridengine.domain.PsNSCMProcessingDetailedStatus;
import com.mango.mif.utils.JAXBUtils;
import com.mango.mif.utils.jaxb.ExecutionJaxbUtils;


/**
 * 
 * Tests PsNBootstrapProcessingDetailedStatus
 */
public class PsNSCMProcessingDetailedStatusTest {
    /**
     * Logger
     */
    public final static Logger logger = Logger.getLogger(PsNSCMProcessingDetailedStatusTest.class);
    /**
     * Detailed Status
     */
    private PsNSCMProcessingDetailedStatus detailedStatus;
    
    @Before
    public void setUp() throws Exception {
        detailedStatus = new PsNSCMProcessingDetailedStatus();
        detailedStatus.setControlFileName("CONTROL_FILE");
        detailedStatus.setDuration("DURATION");
        detailedStatus.setUserName("USER_NAME");
        detailedStatus.setRequestId("REQUEST_ID");
        detailedStatus.setSummary("SUMMARY");
        detailedStatus.setParameterCovariateRelation("parameterCovariateRelation");
        detailedStatus.addModel(createPsNSCMModelProcessingStatus(1));
        detailedStatus.addModel(createPsNSCMModelProcessingStatus(2));
        detailedStatus.addModel(createPsNSCMModelProcessingStatus(3));
    }
    /**
     * Creates a child process instance
     */
    private PsNSCMModelProcessingStatus createPsNSCMModelProcessingStatus(int index) {
        PsNSCMModelProcessingStatus model = new PsNSCMModelProcessingStatus();
        model.setModel("MODEL " +index);
        model.setBaseOFV("baseOFV");
        model.setdDF("dDF");
        model.setGoal("goal");
        model.setNewOFV("newOFV");
        model.setpVAL("pVAL");
        model.setSignificant("significant");
        model.setTest("test");
        model.setTestOFV("testOFV");
        return model;
    }

    @Test
    public void shouldMarshallAndUnmarshallDetailedStatus() throws JAXBException {
        String message = JAXBUtils.marshall(detailedStatus, ExecutionJaxbUtils.CONTEXT_CLASSES);
        logger.debug(message);
        PsNSCMProcessingDetailedStatus unmarshalled = JAXBUtils.unmarshall(message, ExecutionJaxbUtils.CONTEXT_CLASSES);
        
        assertEquals(detailedStatus.getControlFileName(), unmarshalled.getControlFileName());
        assertEquals(detailedStatus.getDuration(), unmarshalled.getDuration());
        assertEquals(detailedStatus.getModels().size(),unmarshalled.getModels().size() );
    }

}
