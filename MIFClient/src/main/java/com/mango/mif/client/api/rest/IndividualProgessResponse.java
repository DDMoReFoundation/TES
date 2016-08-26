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

import javax.xml.bind.annotation.XmlRootElement;


/**
 *This is REST response object for individual estimation progress 
 *and it returns individual estimation progress information in form of string. 
 *
 */
@XmlRootElement(name = "individualResponse")
public class IndividualProgessResponse extends MIFResponse {

    /**
     * String used to store individual estimation progress results.
     */
    private String individualProgressData = "";

    /**
     * returns individual estimation progress results.
     * 
     * @return individualProgressData
     */
    public String getIndividualProgressData() {
        return individualProgressData;
    }
    
    /**
     * sets value for individual estimation progress results.
     * 
     * @param individualProgressData
     */
    public void setIndividualProgressData(String individualProgressData) {
        this.individualProgressData = individualProgressData;
    }
}
