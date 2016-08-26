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

import com.google.common.base.Preconditions;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.invoker.InvokerFactory;


/**
 * 
 * Provides invokers to execute commands through a local shell
 */
public class ShellJobInvokerProvider implements JobInvokerProvider {

    private InvokerFactory invokerFactory;
    
    /* (non-Javadoc)
     * @see com.mango.mif.core.JobInvokerProvider#createInvoker(com.mango.mif.connector.domain.Job)
     */
    @Override
    public Invoker createInvoker(Job job) throws ExecutionException {
        Preconditions.checkNotNull(invokerFactory);
        
        final Invoker invoker = invokerFactory.getShellInvoker();
        job.setInvoker(invoker);
        return invoker;
    }

    public InvokerFactory getInvokerFactory() {
        return this.invokerFactory;
    }

    public void setInvokerFactory(InvokerFactory invokerFactory) {
        this.invokerFactory = invokerFactory;
    }
}
