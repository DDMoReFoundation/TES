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
package com.mango.mif.nonmem.gridengine.domain;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.mango.mif.domain.DetailedStatus;

/**
 * JAXB wrapper for NonMem metrics we get back from the REST Service method.
 */
@XmlRootElement(name = "nmMetrics")
public class NmMetrics extends DetailedStatus{
	
	/**
	 * 
	 */
	private Map<String, Double[]> metrics = new HashMap<String, Double[]>();
	

	/**
	 * 
	 */
	private String errors;
	
	/**
	 * 
	 * @return
	 */
	public Map<String, Double[]> getMetrics() {
		return metrics;
	}

	/**
	 * 
	 * @param parameters
	 */
	public void setMetrics(Map<String, Double[]> metrics) {
		this.metrics = metrics;
	}

	/**
	 * 
	 * @return
	 */
	public String getErrors() {
		return errors;
	}

	/**
	 * 
	 * @param errors
	 */
	public void setErrors(String errors) {
		this.errors = errors;
	}
	
	
	

}
