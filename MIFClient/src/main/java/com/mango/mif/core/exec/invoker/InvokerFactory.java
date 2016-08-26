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
package com.mango.mif.core.exec.invoker;

import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.ce.CeInvoker;
import com.mango.mif.core.exec.jsch.JschInvoker;
import com.mango.mif.core.exec.jsch.JschParameters;

/**
 * <p>Provides Invoker implementations</p>
 * <p>This class should be used as a singleton, it is up to the client to enforce this</p>
 * <p>Not a final class to enable mocking</p> 
 */
public class InvokerFactory {

    /**
     * Gets an invoker that can be used to invoke commands over SSH
     * 
     * @param parameters a parameter object which will be used to configure the invoker, cannot be null
     * @return the invoker
     */
    public Invoker getSSHInvoker(final InvokerParameters parameters) {
        if (parameters instanceof JschParameters) {
            return new JschInvoker((JschParameters) parameters);
        }
        throw new IllegalArgumentException("Currently only JschParameters are accepted");
    }

    /**
     * Gets an invoker that can be used to invoke commands on the local Shell
     * 
     * @return the invoker
     */
    public Invoker getShellInvoker() {
        return new CeInvoker();
    }
}
