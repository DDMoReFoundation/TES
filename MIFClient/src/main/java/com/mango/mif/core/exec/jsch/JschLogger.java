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


/**
 * 
 * Logger that is passed to jcraft jsch classes to route logging requests to 
 * MIF logging library
 */
public class JschLogger implements org.vngx.jsch.util.Logger {
	/**
	 * Logger
	 */
    private final static Logger LOG = Logger.getLogger(JschLogger.class);

    @Override
    public boolean isEnabled(org.vngx.jsch.util.Logger.Level level) {
    	org.apache.log4j.Level log4jLevel = mapToLog4j(level);
    	return log4jLevel.isGreaterOrEqual(LOG.getEffectiveLevel());
    }

    @Override
    public void log(org.vngx.jsch.util.Logger.Level level, String message) {
    	LOG.log(mapToLog4j(level), message);
    }
    /**
     * maps jcraft's level number to Log4j level
     * 
     * @param level
     * @return
     */
    org.apache.log4j.Level mapToLog4j(org.vngx.jsch.util.Logger.Level level) {
    	switch(level) {
	    	case DEBUG:
	    		return org.apache.log4j.Level.DEBUG;
	    	case INFO:
	    		return org.apache.log4j.Level.INFO;
	    	case WARN:
	    		return org.apache.log4j.Level.WARN;
	    	case ERROR:
	    		return org.apache.log4j.Level.ERROR;
	    	case FATAL:
	    		return org.apache.log4j.Level.FATAL;
    	}
    	return org.apache.log4j.Level.DEBUG; 
    }

	@Override
	public void log(org.vngx.jsch.util.Logger.Level level, String message, Object... args) {
		LOG.log(mapToLog4j(level), message);
	}

	@Override
	public void log(org.vngx.jsch.util.Logger.Level level, String message, Throwable exception) {
		LOG.log(mapToLog4j(level), message, exception);
	}
}
