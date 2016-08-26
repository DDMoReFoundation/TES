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

import org.apache.commons.lang.StringUtils;
/**
 *
 * An exception being thrown by state handlers if an error occurs
 *
 */
public class StateHandlerWithOutputException extends StateHandlerException {

    /**
     *
     */
    private static final long	serialVersionUID	= 1L;

    private String stdout;
    private String stderr;

    /**
     * @param message
     * @param cause
     */
    public StateHandlerWithOutputException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public StateHandlerWithOutputException(String message) {
        super(message);
    }

    /**
     * @param message
     */
    public StateHandlerWithOutputException(String message, String stdout, String stderr) {
        super(message);
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public StateHandlerWithOutputException(String message, String stdout, String stderr, Throwable cause) {
        super(message, cause);
        this.stdout = stdout;
        this.stderr = stderr;
    }


    public boolean hasOutput() {
        return StringUtils.isNotBlank(stdout);
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public boolean hasErrors() {
        return StringUtils.isNotBlank(stderr);
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

}
