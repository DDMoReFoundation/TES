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
package com.mango.mif.connector.runner.impl;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;


/**
 * 
 * Tests TaskExecutor Spring configuration
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SpringTaskExecutorConfigurationTest {
    private final static Logger logger = Logger.getLogger(SpringTaskExecutorConfigurationTest.class);
    /**
     * the task executor being tested
     */
    @Autowired
    @Qualifier("newTaskExecutor")
    private ConcurrentTaskExecutor taskExecutor;
    List<BlockingTask> tasks;
    /**
     * A task being used to block a thread in task executor
     * @author mrogalski
     *
     */
    private class BlockingTask implements Callable<String> {
        private String taskId;
        private boolean running = false;
        private boolean killed = false;
        
        public BlockingTask(String taskId) {
            this.taskId = taskId;
        }
        
        @Override
        public String call() throws Exception {
            running = true;
            while(running&&!killed) {
                logger.info("Task ID " + taskId + " running in thread " + Thread.currentThread().getName());
                Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));
            }
            return taskId;
        }
        
        public boolean isRunning() {
            return running;
        }

        
        public void stop() {
            this.killed = true;
        }
    }
    @Before
    public void setUp() {
        tasks = Lists.newArrayList();
    }
    
    @After
    public void tearDown() {
        for(BlockingTask task : tasks) {
            task.stop();
        }
    }
    
    @DirtiesContext
    @Test
    public void shouldResultIn_3_running_jobs() throws InterruptedException {
        for(int i=0;i<5;i++) {
            BlockingTask task = new BlockingTask("task "+ i);
            tasks.add(task);
            taskExecutor.submit(task);
        }
        assertEquals(3, ((ThreadPoolExecutor)taskExecutor.getConcurrentExecutor()).getPoolSize());

    }

    @DirtiesContext
    @Test
    public void shouldResultIn_5_running_jobs_and_25_on_the_queue() {
        for(int i=0;i<30;i++) {
            BlockingTask task = new BlockingTask("task "+ i);
            tasks.add(task);
            taskExecutor.submit(task);
        }

        assertEquals(5, ((ThreadPoolExecutor)taskExecutor.getConcurrentExecutor()).getPoolSize());
        assertEquals(25, ((ThreadPoolExecutor)taskExecutor.getConcurrentExecutor()).getQueue().size());
    }

    @DirtiesContext
    @Test(expected=RejectedExecutionException.class)
    public void shouldThrowAnException() {
        for(int i=0;i<35;i++) {
            BlockingTask task = new BlockingTask("task "+ i);
            tasks.add(task);
            taskExecutor.submit(task);
        }
        
        for(BlockingTask task : tasks) {
            assertTrue(task.isRunning());
        }
        assertEquals(5, ((ThreadPoolExecutor)taskExecutor.getConcurrentExecutor()).getPoolSize());
        assertEquals(25, ((ThreadPoolExecutor)taskExecutor.getConcurrentExecutor()).getQueue().size());
        
    }
}
