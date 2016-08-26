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
package com.mango.mif.connector;
/**
 * 
 * This enum collects error messages and error codes used in the connector.
 * 
 */
public enum ConnectorExceptions {
	  INVALID_REQUEST_MESSAGE(1, "The message is not proper"),
	  INVALID_REQUEST_MESSAGE_FORMAT(2, "navigator Request is in not in expected format."),
	  INVALID_RESPONSE_MESSAGE_FORMAT(3, "Job Runner Response is in not in expected format."),
	  INVALID_RESPONSE_MESSAGE(4, "Job Runner Response is in not in expected format."),
	  INVALID_REQUEST_ID(5, "Request ID not present in the request."),
	  JOB_RUNNER_DOES_NOT_EXSIST_FOR_REQUEST_ID(6, "Job runner does not exist for the request."),
	  REQUEST_ALREADY_COMPLETED(7, "Request has already been cancelled."),
	  JOB_ALREADY_CONSUMED(8, "Job has already been consumed."),
	  REQUEST_ID_NOT_PRESENT(20, "Request ID not present in the request."),
	  USERNAME_NOT_PRESENT(21, "Username not present in the request."),
	  PASSWORD_NOT_PRESENT(22, "Password ID not present in the request.");
	  //... more here ...

	  private final int id;
	  private final String message;

	  ConnectorExceptions(int id, String message) {
	     this.id = id;
	     this.message = message;
	  }

	  public int getId() { return id; }
	  public String getMessage() { return message; }

}
