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
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.InvokerResultHandler;
import com.mango.mif.core.exec.invoker.InvokerHelper;

/**
 * Generic result handler implementation that checks the exit code of the invoker result and
 * if it does not represent successful execution it throws an exception with a message
 * from the error stream property
 */
public class ShellCommandInvokerResultHandler implements InvokerResultHandler {
    /**
     * Logger
     */
    protected final static Logger LOG = Logger.getLogger(ShellCommandInvokerResultHandler.class);

    @Override
    public void handle(InvokerResult invokerResult) throws ExecutionException {
        Preconditions.checkNotNull(invokerResult);
        LOG.debug("Command: " + invokerResult.getCommand());
        LOG.debug("Return code: " + invokerResult.getExitStatus());
        LOG.debug("Error Stream: " + invokerResult.getErrorStream());
        LOG.debug("Output Stream: " + invokerResult.getOutputStream());
        if (InvokerHelper.failed(invokerResult)) {
            throw new ExecutionException(buildErrorMessage("An external process exited with code " + invokerResult.getExitStatus() + ". ", invokerResult));
        }
    }

    /**
     * Builds error message
     * @param message
     * @param invokerResult
     * @return
     */
    protected String buildErrorMessage(String message, InvokerResult invokerResult) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(invokerResult.getCommand())) {
            message = "The following command failed with status "
                    + invokerResult.getExitStatus()
                    + ": "
                    + invokerResult.getCommand();
        } else {
            sb.append(message);
        }
        sb.append("\n");

        if (StringUtils.isNotBlank(invokerResult.getOutputStream())) {
            sb.append("Output stream\n")
                    .append(invokerResult.getOutputStream())
                    .append("\n");
        } else {
            sb.append("[stdout empty]\n");
        }

        if (StringUtils.isNotBlank(invokerResult.getErrorStream())) {
            sb.append("Error stream\n")
                    .append(invokerResult.getErrorStream())
                    .append("\n");
        } else {
            sb.append("[stderr empty]\n");
        }
        return sb.toString();
    }
}
