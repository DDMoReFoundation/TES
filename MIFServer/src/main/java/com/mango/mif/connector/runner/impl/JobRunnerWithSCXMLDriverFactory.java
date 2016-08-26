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

import java.io.IOException;
import java.util.List;

import javax.jms.IllegalStateException;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.google.common.base.Preconditions;
import com.mango.mif.connector.runner.JobRunner;
import com.mango.mif.connector.runner.impl.statehandler.DefaultStateHandler;
import com.mango.mif.core.domain.Job;

/**
 * A factory responsible for creating job runners which execution is driven by SCXMLDriver
 */
public abstract class JobRunnerWithSCXMLDriverFactory extends BasicJobRunnerFactory {
    /**
     * Logger
     */
    public final static Logger LOG = Logger.getLogger(JobRunnerWithSCXMLDriverFactory.class);
    /**
     * SCXML definition file
     */
    private Resource scxmlDefinition;

    @Override
    protected abstract DefaultJobRunner createJobRunner();
    /**
     * Method injected by spring
     * @return new instances of drivers
     */
    public abstract SCXMLDriver createDriver();
    /**
     * Method injected by spring
     * @return new instances of cancellation handler
     */
    public abstract SCXMLProcessingCancellationHandlerImpl createCancellationHandler();
    /**
     * 
     * @return new instances of a list of state handlers
     */
    public abstract List<DefaultStateHandler> getStateHandlers();

    @Override
    public JobRunner createJobRunner(Job job) {
        DefaultJobRunner jobRunner = (DefaultJobRunner)super.createJobRunner(job);
        buildDriver(jobRunner, job);
        return jobRunner;
    }
    /**
     * Registers handlers
     */
    void buildDriver(DefaultJobRunner jobRunner, Job job) {
        List<DefaultStateHandler> stateHandlers = getStateHandlers();
        Preconditions.checkNotNull(stateHandlers, "Driver factory incorrectly configured - no state handlers registered.");
        Preconditions.checkNotNull(scxmlDefinition, "Driver factory incorrectly configured - SCXML definition not set.");

        SCXMLDriver driver = createDriver();
        try {
            driver.setSCXMLDocument(scxmlDefinition.getURL());
        } catch (IOException e) {
            throw new RuntimeException("Could not load JobRunner SCXML definition file",e);
        } catch (IllegalStateException e) {
            //the driver is defined as a prototype, so the exception should never be thrown, if so it is runtime exception
            throw new RuntimeException("Could not initialize the driver.",e);
        }
        for(DefaultStateHandler handler : stateHandlers) {
            handler.setJobRunner(jobRunner);
            driver.registerHandler(handler);
        }
        SCXMLProcessingCancellationHandlerImpl cancellationHandler = createCancellationHandler();
        cancellationHandler.setJobRunner(jobRunner);
        driver.setCancellationHandler(cancellationHandler);
        
        jobRunner.setDriver(driver);
    }

    /**
     * @param scxmlDefinition the scxmlDefinition to set
     */
    public void setScxmlDefinition(Resource scxmlDefinition) {
        this.scxmlDefinition = scxmlDefinition;
    }

}
