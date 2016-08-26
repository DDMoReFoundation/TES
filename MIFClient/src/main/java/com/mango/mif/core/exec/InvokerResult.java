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
package com.mango.mif.core.exec;

import org.apache.commons.lang.StringUtils;

/**
 * A result of a shell command invocation.  This was an interface, but the implementations were identical for JSCH and for CE
 * so I dumped all the code here.
 */
public class InvokerResult {

    private final String command;
    private final String stdout;
    private final String stderr;
    private final int exitCode;

    public InvokerResult(String command, String stdout, String stderr, int exitCode) {
        this.command = command;
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    /**
     * Return the first line only of the specified string.
     * @param input The specified input
     * @return first line of the input, or the empty string if there is no input.
     */
    private String getFirstLineOnly(String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        int iPos = input.indexOf("\n");
        if (iPos < 0) {
            return input;
        }
        return input.substring(0, iPos);
    }

    public String getCommand() {
        return command;
    }

    public String getExitCode() {
        return "" + exitCode;
    }

    public String getErrorStream() {
        return stderr;
    }

    public String getOutputStream() {
        return stdout;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public int getExitStatus() {
        return exitCode;
    }

    /**
     * Useful for commands where we're only expecting one line of output
     * @return the first line of the output, or null if there is no output.
     */
    public String getFirstLineOutputStream() {
        return getFirstLineOnly(stdout);
    }
}
