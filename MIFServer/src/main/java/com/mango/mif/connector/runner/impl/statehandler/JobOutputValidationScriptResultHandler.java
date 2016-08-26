package com.mango.mif.connector.runner.impl.statehandler;

import com.google.common.base.Preconditions;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.invoker.InvokerHelper;

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
public class JobOutputValidationScriptResultHandler extends GenericShellCommandInvokerResultHandler {
    
    /**
     * Constructor
     * @param exitEvent
     */
    public JobOutputValidationScriptResultHandler(String stateExitEvent) {
        super(stateExitEvent);
    }

    @Override
    public void handle(InvokerResult invokerResult) throws ExecutionException {
        Preconditions.checkNotNull(getJob());
        Preconditions.checkNotNull(invokerResult);
        
        if (InvokerHelper.failed(invokerResult)) {
            LOG.info("The job validation script returned " + invokerResult.getExitStatus() + " (INVALID OUTPUT)");
            LOG.info("Output Stream: " + invokerResult.getOutputStream());

            throw new ValidationFailedException("The job validation script exited with " + invokerResult.getExitStatus() + ". OUTPUT IS INVALID.",
                    invokerResult.getOutputStream(),
                    invokerResult.getErrorStream());
        } else {
            LOG.debug("The job validation script returned \"true\", the output is VALID.");
        }
    }

}
