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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.utils.TestProperties;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.encrypt.DesEncrypter;

public class JschSessionPoolTest {

    private final static Logger logger = Logger.getLogger(JschSessionPoolTest.class);

    private final static int MAX_ACTIVE = 10;

    private final static int MAX_IDLE = 5;
    /**
     * JSCH invoker parameters
     */
    private Invoker invoker;

    private InvokerHelper invokerHelper;

    private JschSessionPool pool;

    @BeforeClass
    public static void loadProperties() throws IOException {
        TestPropertiesConfigurator.initProperties();
    }

    @Before
    public void setup() throws Exception {
        String userName = System.getProperty(TestProperties.MIF_OTHER_CLIENT_USER_NAME);
        String encryptedPassword = System.getProperty(TestProperties.MIF_OTHER_CLIENT_USER_PASSWORD);
        String portProp = System.getProperty(TestProperties.MIF_JSCH_PORT);
        String host = System.getProperty(TestProperties.MIF_JSCH_HOST);
        Preconditions.checkNotNull(userName, TestProperties.MIF_OTHER_CLIENT_USER_NAME + " property is not set.");
        Preconditions.checkNotNull(encryptedPassword, TestProperties.MIF_OTHER_CLIENT_USER_PASSWORD + " property is not set.");
        if (host == null) {
            host = "localhost";
        }
        if (portProp == null) {
            portProp = "22";
        }
        int port = Integer.parseInt(portProp);

        JschSessionPoolParameters parameters = new JschSessionPoolParameters();
        parameters.setMaxActive(MAX_ACTIVE);
        parameters.setMaxIdle(MAX_IDLE);
        parameters.setMaxTotal(100);
        parameters.setMinIdle(0);
        parameters.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        parameters.setTimeBetweenEvictionRunsMillis(TimeUnit.SECONDS.toMillis(5));
        parameters.setMinEvictableIdleTimeMillis(TimeUnit.SECONDS.toMillis(120));
        parameters.setNumTestsPerEvictionRun(50);
        parameters.setTestOnReturn(false);
        parameters.setTestWhileIdle(true);
        parameters.setLifo(false);

        pool = new JschSessionPool();
        pool.setJschSessionPoolParameters(parameters);

        JschParameters params = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
        params.setPort(port);
        params.setHost(host);
        invoker = new JschInvoker(params);

        invokerHelper = new InvokerHelper(invoker);

    }

    @Test(timeout = 300000)
    public void shouldExecuteANumberOfCommandsInTheSameTimeAndEndUpWithMaxIdleNumberOfConnections() throws Exception {

        int max = 15;
        int time = 10;
        Thread[] children = new Thread[max];
        JschSessionPoolTester[] testers = new JschSessionPoolTester[max];
        logger.info(buildJschSessionMetrics(pool));

        for (int i = 0; i < max; i++) {
            testers[i] = new JschSessionPoolTester(invokerHelper, i, time);
            children[i] = new Thread(testers[i]);
            children[i].start();
        }

        while (!allCompleted(testers)) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            logger.info(buildJschSessionMetrics(pool));

            assertTrue(pool.getPool().getNumActive() <= MAX_ACTIVE);
            assertTrue(pool.getPool().getNumIdle() <= MAX_IDLE);
        }

        logger.info(buildJschSessionMetrics(pool));

        // there should just be sessions in the pool that had been created at the beginning
        assertEquals(MAX_IDLE, JschSessionPool.getPoolObjectFactory().getSwimmers().size());
    }
    
    /**
     * 
     * @param testers
     * @return true if all testers have completed their processing
     */
    private boolean allCompleted(JschSessionPoolTester[] testers) {
        for (JschSessionPoolTester tester : testers) {
            if (!tester.isDone()) {
                return false;
            }
        }
        return true;
    }

    private String buildJschSessionMetrics(JschSessionPool sessionPool) {

        StringBuilder info = new StringBuilder();
        info.append("JSch Session Pool:\n");
        info.append("NumIdle: ");
        info.append(sessionPool.getPool().getNumIdle());
        info.append(" NumActive: ");
        info.append(sessionPool.getPool().getNumActive());
        info.append(" MaxIdle: ");
        info.append(sessionPool.getPool().getMaxIdle());
        info.append(" MaxActive (-1 = infinite): ");
        info.append(sessionPool.getPool().getMaxActive());
        info.append(" MinEvictTime: ");
        info.append(sessionPool.getPool().getMinEvictableIdleTimeMillis());
        info.append(" EvictionRunInterval: ");
        info.append(sessionPool.getPool().getTimeBetweenEvictionRunsMillis());
        info.append(" TestWhenIdle: ");
        info.append(sessionPool.getPool().getTestWhileIdle());
        info.append(" Lifo: ");
        info.append(sessionPool.getPool().getLifo());
        info.append("\n\n");

        if (sessionPool != null) {
            buildSwimmerStats(info);
        }
        return info.toString();
    }

    private void buildSwimmerStats(StringBuilder info) {

        JschSessionPoolObjectFactory sessionPool = JschSessionPool.getPoolObjectFactory();

        info.append(String.format(" SSH Swimmers NumSessions [%s]  ", sessionPool.getSwimmers().size()));
        info.append("\n");

        for (JschSession session : sessionPool.getSwimmers().keySet()) {
            info.append(" Connected: ");
            info.append(session.isConnected());
            info.append(" In Use: ");
            info.append(session.isInUse());
            info.append(" Username: ");
            info.append(session.getParameters().getUserName());
            info.append(" Port: ");
            info.append(session.getParameters().getPort());
            info.append(" ID: ");
            info.append(session.toString());
            info.append(" Command: ");
            info.append(session.getLastCommand());
            info.append("\n");
        }
    }
}
