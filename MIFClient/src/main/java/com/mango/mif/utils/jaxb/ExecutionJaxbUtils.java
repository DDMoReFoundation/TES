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
package com.mango.mif.utils.jaxb;

import com.mango.mif.client.api.rest.DetailedStatusResponse;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.nonmem.gridengine.domain.NONMEMProcessingDetailedStatus;
import com.mango.mif.psnbootstrap.gridengine.domain.BootstrapActiveChildJobs;
import com.mango.mif.psnbootstrap.gridengine.domain.PsNBootstrapProcessingDetailedStatus;
import com.mango.mif.psnscm.gridengine.domain.PsNSCMModelProcessingStatus;
import com.mango.mif.psnscm.gridengine.domain.PsNSCMProcessingDetailedStatus;
import com.mango.mif.psnvpc.gridengine.domain.PsNVPCProcessingDetailedStatus;


/**
 * The Class ExecutionJaxbUtils.
 *
 */
public class ExecutionJaxbUtils {
	
    /**
     * A classes used to build up a JAXB context for marshalling/unmarshalling beans involved in the execution process
     */
    public final static Class<?>[] CONTEXT_CLASSES = new Class[] {
        com.mango.mif.domain.ExecutionRequest.class,
        com.mango.mif.domain.ExecutionResponse.class,
        PsNBootstrapProcessingDetailedStatus.class,
        BootstrapActiveChildJobs.class,
        PsNSCMModelProcessingStatus.class,
        PsNSCMProcessingDetailedStatus.class,
        DetailedStatusResponse.class,
        DetailedStatus.class,
        PsNVPCProcessingDetailedStatus.class,
        NONMEMProcessingDetailedStatus.class,
        com.mango.mif.utils.jaxb.MapEntryType.class,
        com.mango.mif.utils.jaxb.MapType.class
    };
}
