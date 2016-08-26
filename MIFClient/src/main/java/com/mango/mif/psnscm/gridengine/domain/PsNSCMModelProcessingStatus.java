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
package com.mango.mif.psnscm.gridengine.domain;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * 
 * scmlog file model row content
 */
@XmlRootElement(name = "PsNSCMModelProcessingStatus")
public class PsNSCMModelProcessingStatus {
    /*
     * columns found in the scmlog file
     */
    private String model;
    private String test;
    private String baseOFV;
    private String newOFV;
    private String testOFV;
    private String goal;
    private String dDF;
    private String significant;
    private String pVAL;
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getTest() {
        return test;
    }
    
    public void setTest(String test) {
        this.test = test;
    }
    
    public String getBaseOFV() {
        return baseOFV;
    }
    
    public void setBaseOFV(String baseOFV) {
        this.baseOFV = baseOFV;
    }
    
    public String getNewOFV() {
        return newOFV;
    }
    
    public void setNewOFV(String newOFV) {
        this.newOFV = newOFV;
    }
    
    public String getTestOFV() {
        return testOFV;
    }
    
    public void setTestOFV(String testOFV) {
        this.testOFV = testOFV;
    }
    
    public String getGoal() {
        return goal;
    }
    
    public void setGoal(String goal) {
        this.goal = goal;
    }
    
    public String getdDF() {
        return dDF;
    }
    
    public void setdDF(String dDF) {
        this.dDF = dDF;
    }
    
    public String getSignificant() {
        return significant;
    }
    
    public void setSignificant(String significant) {
        this.significant = significant;
    }
    
    public String getpVAL() {
        return pVAL;
    }
    
    public void setpVAL(String pVAL) {
        this.pVAL = pVAL;
    }
    
}
