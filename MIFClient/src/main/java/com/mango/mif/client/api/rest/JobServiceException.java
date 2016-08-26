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


/**
 * Exception thrown by JobService
 */
public class JobServiceException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;
	private static final String MSG = "Lookup of entry (%s) failed: %s";

	public JobServiceException(Long entryId, String reason) {
		super(getMsg(entryId, reason));
	}

	public JobServiceException(Long entryId, Throwable cause) {
		super(getMsg(entryId, (cause != null) ? cause.getMessage() : ""));
	}
	
	public JobServiceException(String msg) {
		super(msg);
	}

	private static String getMsg(Long entryId, String reason) {
		return String.format(MSG, entryId, reason);
	}

}
