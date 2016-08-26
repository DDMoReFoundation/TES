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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.mango.mif.core.exec.jsch.JschParameters;

public class InvokerFactoryTest {

    private InvokerFactory invokerFactory;
    
    @Before
    public void setup() {
        invokerFactory = new InvokerFactory();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetSSHInvokerNull() {
        invokerFactory.getSSHInvoker(null);
    }

    @Test
    public void testGetSSHInvoker() {
        assertNotNull("SSH invoker instance was not returned", invokerFactory.getSSHInvoker(new JschParameters()));
    }
    
    @Test
    public void testGetSSHInvokerParams() {
        InvokerParameters ip = new JschParameters().charSet("UTF-8").clearTextPassword("PASSWORD").host("localhost").port(80).timeoutInMilliseconds(5000).protocols("password");
        
        assertNotNull("SSH invoker instance was not returned", invokerFactory.getSSHInvoker(ip));
    }

    @Test
    public void testGetShellInvoker() {
        assertNotNull("Shell invoker instance was not returned", invokerFactory.getShellInvoker());
    }

}
