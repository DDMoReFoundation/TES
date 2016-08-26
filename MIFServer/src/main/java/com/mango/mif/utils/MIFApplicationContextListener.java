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
package com.mango.mif.utils;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.mango.mif.connector.Connector;
import com.mango.mif.core.services.ConnectorsRegistry;

/**
 * MIFApplicationContextListener provides a hook specifically for post Spring context startup.
 */
public class MIFApplicationContextListener implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger LOG = Logger.getLogger(MIFApplicationContextListener.class);
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent cre) {

        LOG.info("MIFApplicationContextListener: handling post Spring start events");
        
        displayLogFileLocation();
        
        doFailureRecovery(cre);
        displayStartupCompletedMsg();
    }

    private void displayStartupCompletedMsg() {
        LOG.info("\n\n\n - MIF is Running -\n\n");
    }

    private void doFailureRecovery(ContextRefreshedEvent cre) {
        LOG.info("\n\n\n - MIF Failure Recovery -\n\n");
        
        ApplicationContext ac = cre.getApplicationContext();
        ConnectorsRegistry connectorsRegistry = (ConnectorsRegistry) ac.getBean("connectorsRegistry");
        
        Preconditions.checkState(connectorsRegistry!=null, "Connectors registry missing from application context.");
        
        Stopwatch watch = new Stopwatch();
        watch.start();
        for (Connector connector : connectorsRegistry.getConnectors()) {
            connector.doFailureRecovery();
        }
        long duration = watch.elapsedMillis();
        LOG.debug("\n\nFailure recovery took " + duration + " ms / " + TimeUnit.MILLISECONDS.toSeconds(duration) + " secs\n\n");
        watch.stop();
    }

    /**
     * Display the log4j log file location, this must match the appender in MIF's internal log4j.properties.
     */
    private void displayLogFileLocation() {
        Logger rootLogger = Logger.getRootLogger();
        RollingFileAppender rfa = (RollingFileAppender) rootLogger.getAppender("file");
        if (rfa != null) {
            LOG.warn("Location of MIF log file: " + rfa.getFile());
        }
    }
}
