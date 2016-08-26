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
package com.mango.mif.connector.runner.impl.statehandler;


/**
 * An exception thrown by state handlers on a validation exception
 */
public class StateHandlerValidationException extends StateHandlerWithOutputException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     * @param cause
     */
    public StateHandlerValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public StateHandlerValidationException(String message) {
        super(message);
    }

    /**
     * @param message
     */
    public StateHandlerValidationException(String message, String stdout, String stderr) {
        super(message, stdout, stderr);
    }

}
