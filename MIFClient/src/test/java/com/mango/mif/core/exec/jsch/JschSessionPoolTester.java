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
package com.mango.mif.core.exec.jsch;

import org.apache.log4j.Logger;

import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.invoker.InvokerHelper;

/**
 * A runnable that executes a sleep command using the invoker helper and quits once processing returns from the shell
 *
 */
public class JschSessionPoolTester implements Runnable {
    private final static Logger logger = Logger.getLogger(JschSessionPoolTester.class);
    private int counter;
    private int time;
    private InvokerHelper invokerHelper;

    private volatile boolean done = false;
    /**
     * Constructor
     * @param invokerHelper
     * @param counter the tester's id
     * @param time the time that it should run the shell command
     */
    public JschSessionPoolTester(InvokerHelper invokerHelper, int counter, int time) {
        this.counter = counter;
        this.time = time;
        this.invokerHelper = invokerHelper;
    }

    public void run() {
        try {
            logger.debug("Hello from thread #" + counter);
            InvokerResult results = invokerHelper.run("sleep " + time + "; echo \"#" + counter + " $$ finished\"");
            if (InvokerHelper.success(results)) {
                logger.debug("Result from thread #" + counter + ": " + results.getStdout());
            } else {
                logger.debug("Thread #" + counter + " FAILED: " + InvokerHelper.errors(results));
            }
        } catch (ExecutionException ee) {
            logger.debug("Thread #" + counter + " caught exception " + ee);
        } finally {
            done = true;
        }
    }

    public boolean isDone() {
        return done;
    }
}
