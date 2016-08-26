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
package com.mango.mif.client.err;

public enum TaskExecutorExceptions {
    INVALID_REQUEST_MESSAGE(1, "The Request message is invalid."),
    INVALID_REQUEST_MESSAGE_FORMAT(2, "Navigator request is not in the expected format."),
    INVALID_RESPONSE_MESSAGE_FORMAT(3, "Job Runner Response is not in the expected format."),
    INVALID_RESPONSE_MESSAGE(4, "Job Runner Response is not in the expected format."),
    INVALID_REQUEST_ID(5, "Request ID not present in the request."),
    CONNECTOR_NOT_PRESENT(6, "Connector not present for the key."),
    SERVER_BASE_URL_NOT_PRESENT(7, "Server URL not present."),
    ERROR_DURING_SUBMISSION(8, "Error during the submission of the request."),
    REQUEST_ID_NOT_PRESENT(20, "Request ID not present in the request."); 
    //... more here ...

    private final int id;
    private final String message;

    TaskExecutorExceptions(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() { return id; }
    public String getMessage() { return message; }

}
