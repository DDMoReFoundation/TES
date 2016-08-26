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

import com.google.common.base.Preconditions;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;

/**
 * Generic shell based command builder that uses replaceable invoker to execute commands built with the command builder.
 * It also uses a replaceable results handler, to parse the outputs and reason on the next steps of the execution
 *
 */
public class GenericShellBasedStateHandler extends BaseGenericShellBasedStateHandler {
    /**
     * @param state
     * @param exitEvent
     */
    public GenericShellBasedStateHandler(String state, String exitEvent) {
        super(state, exitEvent);
    }

    /**
     * @param state
     */
    public GenericShellBasedStateHandler(String state) {
        super(state);
    }

    @Override
    protected void doProcessing() throws StateHandlerException {

        Invoker invoker = null;
        try {
            invoker = getInvoker();
        } catch (ExecutionException ee) {
            throw new StateHandlerException("Error getting invoker from generic shell based state handler", ee);
        }

        Preconditions.checkNotNull(commandBuilder, "The command builder is not set");
        Preconditions.checkNotNull(invoker, "The invoker is not set");
        Preconditions.checkNotNull(invokerResultHandler, "The Invoker result handler is not set");

        String command = null;
        try {
            commandBuilder.setJob(getJob());
            command = commandBuilder.getCommand();
        } catch (ExecutionException e) {
            throw new StateHandlerException("Could not generate command.", e);
        }

        if (StringUtils.isEmpty(command )) {
            throw new StateHandlerException("Empty command scheduled for execution.");
        }

        LOG.debug("Command that will be executed:" + command);
        InvokerResult result = null;
        try {
            result = invoker.execute(command);
        } catch (ExecutionException e) {
            LOG.error("Execution of command threw exception: " + command);
            throw new StateHandlerException("Could not execute command.", e);
        }
        invokerResultHandler.setJob(getJob());
        invokerResultHandler.setJobUpdater(getJobUpdater());

        try {
            invokerResultHandler.handle(result);
        } catch (ValidationFailedException efe) {
            throw new StateHandlerValidationException("Output validation failed.  Are there errors in the submitted script?",
                    efe.getStdout(),
                    efe.getStderr());
        } catch (ExecutionException e) {
            LOG.error("Invoker result handler threw exception while handling results for " + command);
            throw new StateHandlerWithOutputException("Error when parsing command results for template "
                    + commandBuilder.getTemplate(),
                    result != null ? result.getOutputStream() : "",     // NOTE: this ridiculous test for null
                    result != null ? result.getErrorStream() : "",      // is required by one of the tests
                    e);
        }

    }

    @Override
    public String complete() {
        LOG.debug("Exit event: " + invokerResultHandler.getStateExitEvent());
        super.complete();
        return invokerResultHandler.getStateExitEvent();
    }
}
