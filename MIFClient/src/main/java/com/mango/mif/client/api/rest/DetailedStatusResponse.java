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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.mango.mif.domain.DetailedStatus;



/**
 * A REST response containing a detailed status of a job
 */
@XmlRootElement(name = "detailedStatusResponse")
public class DetailedStatusResponse extends MIFResponse {
    /**
     * detailed status
     */
    private DetailedStatus detailedStatus;
    
    public void setDetailedStatus(DetailedStatus detailedStatus) {
        this.detailedStatus = detailedStatus;
    }

    @XmlElement
    public DetailedStatus getDetailedStatus() {
        return detailedStatus;
    }
}
