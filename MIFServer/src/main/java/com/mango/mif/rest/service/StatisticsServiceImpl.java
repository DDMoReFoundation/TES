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
package com.mango.mif.rest.service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

import javax.management.MBeanServerConnection;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import com.mango.mif.client.api.rest.StatisticsService;
import com.mango.mif.connector.Connector;
import com.mango.mif.core.exec.jsch.JschSession;
import com.mango.mif.core.exec.jsch.JschSessionPool;
import com.mango.mif.core.exec.jsch.JschSessionPoolObjectFactory;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.utils.MIFProperties;

/**
 * Simple statistics service for monitoring core MIF operations for performance.
 */
public class StatisticsServiceImpl implements StatisticsService {

	private static final Logger LOG = Logger
			.getLogger(StatisticsServiceImpl.class);

	private ConnectorsRegistry connectorsRegistry;

	private JobManagementService jobManagementService;

	private JschSessionPool sessionPool;

	/**
	 * {@inheritDoc}.
	 */
	public String getStatistics() {

		StringBuilder info = new StringBuilder();
		info.append("<font face='Courier' size='1'>");

		systemConfig(info);

		systemStats(info);

		for (Connector connector : connectorsRegistry.getConnectors()) {
		    info.append(connector.getRuntimeMetrics());
		}

		buildJschSessionMetrics(info);
		info.append("<font/>");
		return info.toString();
	}

	private void systemConfig(StringBuilder info) {

		MIFProperties props = MIFProperties.getInstance();
		info.append("<b>MIF Config</b><br><br>");
        appendKeyValue("MIF Version", props.getProperty("mif.version"),
            info);
        appendKeyValue("MIF Build Number", props.getProperty("mif.buildnumber"),
            info);

        appendKeyValue("MIF Configuration Directory", props.getProperty("mif.configuration.dir"),
            info);
		
        info.append("<br><br>");
	}

	private void appendKeyValue(String key, String value, StringBuilder info) {
		info.append(key);
		info.append(": ");
		info.append(value);
		info.append("<br>");
	}

	/**
	 * {@inheritDoc}.
	 */
	public String getHeapDump() {
		FullThreadDump dump = new FullThreadDump();
		dump.dump();
		Logger rootLogger = Logger.getRootLogger();
		RollingFileAppender rfa = (RollingFileAppender) rootLogger
				.getAppender("file");
		if (rfa != null) {
			return "Heap dump initiated to MIF log: "
					+ rfa.getFile()
					+ " from line beginning 'Full Java thread dump with locks info'";
		} else {
			return "Heap dump initiated to MIF log";
		}
	}

	private void systemStats(StringBuilder info) {

		MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

		try {
			OperatingSystemMXBean os = ManagementFactory
					.newPlatformMXBeanProxy(mbsc,
							ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,
							OperatingSystemMXBean.class);

			MemoryMXBean mem = ManagementFactory.newPlatformMXBeanProxy(mbsc,
					ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);

			ThreadMXBean thread = ManagementFactory.newPlatformMXBeanProxy(
					mbsc, ManagementFactory.THREAD_MXBEAN_NAME,
					ThreadMXBean.class);

			info.append("<b>System Info</b><br>");

			info.append(String.format("NumCPUs: [%s]<br>",
					os.getAvailableProcessors()));
			info.append(String.format("Load Average: [%s]<br>",
					os.getSystemLoadAverage()));
			info.append(String
					.format("Threads: [%s], Peak: [%s], Total: [%s], DaemonCount: [%s] <br>",
							thread.getThreadCount(),
							thread.getPeakThreadCount(),
							thread.getTotalStartedThreadCount(),
							thread.getDaemonThreadCount()));
			info.append(String.format("Heap Memory: [%s]<br>",
					mem.getHeapMemoryUsage()));
			info.append(String.format("Non Heap Memory: [%s]<br>",
					mem.getNonHeapMemoryUsage()));
			info.append("<br>");

		} catch (IOException e) {
			LOG.error("Exception thrown ", e);
		}
	}

	private void buildJschSessionMetrics(StringBuilder info) {

		info.append("<b>JSch Session Pool:</b>");
		info.append(" NumIdle: ");
		info.append(sessionPool.getPool().getNumIdle());
		info.append(" NumActive: ");
		info.append(sessionPool.getPool().getNumActive());
		info.append(" MaxIdle: ");
		info.append(sessionPool.getPool().getMaxIdle());
		info.append(" Max (-1 = infinite): ");
		info.append(sessionPool.getPool().getMaxActive());
		info.append(" MinEvictTime: ");
		info.append(sessionPool.getPool().getMinEvictableIdleTimeMillis());
		info.append(" EvictionRunInterval: ");
		info.append(sessionPool.getPool().getTimeBetweenEvictionRunsMillis());
		info.append(" TestWhenIdle: ");
		info.append(sessionPool.getPool().getTestWhileIdle());
		info.append(" Lifo: ");
		info.append(sessionPool.getPool().getLifo());

		info.append("<br><br>");

		if (sessionPool != null) {
			buildSwimmerStats(info);
		}
	}

	private void buildSwimmerStats(StringBuilder info) {

		JschSessionPoolObjectFactory sessionPool = JschSessionPool
				.getPoolObjectFactory();

		info.append(String.format(" <b>SSH Swimmers</b> NumSessions [%s]  ",
				sessionPool.getSwimmers().size()));
		info.append("<br>");

		for (JschSession session : sessionPool.getSwimmers().keySet()) {
			info.append(" Connected: ");
			info.append(session.isConnected());
			info.append(" InUse: ");
			info.append(session.isInUse());
			info.append(" Username: ");
			info.append(session.getParameters().getUserName());
			info.append(" Port: ");
			info.append(session.getParameters().getPort());						
			info.append("<br>");			
		}
	}

	public ConnectorsRegistry getConnectorsRegistry() {
		return connectorsRegistry;
	}

	public void setConnectorsRegistry(ConnectorsRegistry connectorsRegistry) {
		this.connectorsRegistry = connectorsRegistry;
	}

	public JobManagementService getJobManagementService() {
		return jobManagementService;
	}

	public void setJobManagementService(
			JobManagementService jobManagementService) {
		this.jobManagementService = jobManagementService;
	}

	public JschSessionPool getSessionPool() {
		return sessionPool;
	}

	public void setSessionPool(JschSessionPool sessionPool) {
		this.sessionPool = sessionPool;
	}
}
