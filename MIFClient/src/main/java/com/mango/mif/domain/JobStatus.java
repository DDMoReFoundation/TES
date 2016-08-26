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
package com.mango.mif.domain;
/**
 * Enum for the status of the Job.
 * 
 * This represents the state of handling MIF Execution Request
 */
public enum JobStatus {

      NOT_AVAILABLE(false),
	  NEW(false),
      SCHEDULED(false),
	  PROCESSING(false),
      PROCESSING_FINISHED(false),
	  CANCELLED(true),
	  COMPLETED(true),
      FAILED(true);
	  /**
	   * Flag indicating that the job execution status is final or not
	   */
	  private final boolean isFinal;
	  /**
	   * Constructor
	   * @param message
	   * @param isFinal
	   */
	  JobStatus(boolean isFinal) {
	     this.isFinal = isFinal;
	  }

	  public boolean isFinal() { 
		  return isFinal; 
	  }

}
