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
package com.mango.mif.connector.runner;

/**
 * 
 * Represents the SCXML Processing status. That is Runner status (Not Job Status)
 * 
 * Why not just use JobStatus? When there is a failure recovery, a Job might be in state Running, but a Job Runner for such a job is just being created.
 */
public enum DriverProcessingStatus {
    /**
     * The processing is performed
     */
    RUNNING("RUNNING",false),
    /**
     * The processing reached the cancelled state
     */
    CANCELLED("CANCELLED",true),
    /**
     * The processing reached the failed state
     */
    FAILED("FAILED",true),
    /**
     * The processing finished
     */
    FINISHED("FINISHED",true);

    /**
     * is the status final
     */
    private final boolean isFinal;
    /**
     * A message associated with the state
     */
    private final String message;
    
    DriverProcessingStatus(String message, boolean isFinal) {
        this.message = message;
        this.isFinal = isFinal;
    }
    
    public boolean isFinal() {
        return isFinal;
    }

    public String getMessage() { 
        return message; 
        }
}
