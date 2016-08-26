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

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.utils.TestProperties;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.encrypt.DesEncrypter;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * Jsch session pool test.
 * 
 */
public class JschSessionTest {

	private final static Logger LOG = Logger.getLogger(JschSessionTest.class);

    private String host;
    private int port;

    private String userName;
    private String encryptedPassword;

    @BeforeClass
    public static void loadProperties() throws IOException {
        TestPropertiesConfigurator.initProperties();
    }
    
	/**
	 * Set-up tasks prior to each test being run.
	 */
    @Before
    public void setup() throws ExecutionException, EncryptionException {
        userName = System.getProperty(TestProperties.MIF_OTHER_CLIENT_USER_NAME);
        encryptedPassword = System.getProperty(TestProperties.MIF_OTHER_CLIENT_USER_PASSWORD);
        String portProp = System.getProperty(TestProperties.MIF_JSCH_PORT);
        host = System.getProperty(TestProperties.MIF_JSCH_HOST);
        Preconditions.checkNotNull(userName, TestProperties.MIF_OTHER_CLIENT_USER_NAME + " property is not set.");
        Preconditions.checkNotNull(encryptedPassword, TestProperties.MIF_OTHER_CLIENT_USER_PASSWORD + " property is not set.");
        Preconditions.checkNotNull(portProp, TestProperties.MIF_JSCH_PORT + " property is not set.");
        if (host == null) {
            host = "localhost";
        }
        port = Integer.parseInt(portProp);
    }

    @Test
    public void shouldReturnAJschSessionIfOneAlreadyExistsForAGivenUser() throws ExecutionException, EncryptionException {
        JschParameters parameters1 = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
        parameters1.setHost(host);
        parameters1.setPort(port);
        JschParameters parameters2 = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
        parameters2.setHost(host);
        parameters2.setPort(port);

        // same user,host, port then equal parameters
        assertEquals(parameters1, parameters2);
        
        JschSession jschSession1 = new JschSession(parameters1);
        JschSession jschSession2 = new JschSession(parameters1);
        // Same parameters but different sessions instances
        assertTrue(!jschSession1.equals(jschSession2));
    }
    
    @Test
    public void testRun() throws ExecutionException, EncryptionException {
    	final JschParameters parameters = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
    	parameters.setHost(host);
    	parameters.setPort(port);
    	
    	JschSession jschSession = null;
        try {
            jschSession = new JschSession(parameters);
            run(jschSession, "echo \"echo I Woz Ere >/tmp/fromJSCHSessionTest.txt\" >/tmp/fromJSCHSessionTest.sh");
            run(jschSession, "chmod +x /tmp/fromJSCHSessionTest.sh");
            run(jschSession, "/tmp/fromJSCHSessionTest.sh"); // Testing the execution of a script directly
            
            final InvokerResult getOutputResult = jschSession.run("cat /tmp/fromJSCHSessionTest.txt");
            assertEquals("Command should have successfully executed", 0, getOutputResult.getExitStatus());
            LOG.info("Final Output stream:\n" + getOutputResult.getOutputStream());
            LOG.info("Final Error stream:\n" + getOutputResult.getErrorStream());
            assertEquals("Error stream should be empty", "", getOutputResult.getErrorStream());
            assertEquals("Checking the correct output was produced", "I Woz Ere\n", getOutputResult.getOutputStream());
        } finally {
        	jschSession.run("rm -f /tmp/fromJSCHSessionTest.*");
        	jschSession.close();
        }
    }
    
    private void run(final JschSession session, final String cmd) throws ExecutionException {
        final InvokerResult getOutputResult = session.run(cmd);
        LOG.info("Output stream:\n" + getOutputResult.getOutputStream());
        LOG.info("Error stream:\n" + getOutputResult.getErrorStream());
        assertEquals("Command should have successfully executed", 0, getOutputResult.getExitStatus());
        assertEquals("Error stream should be empty", "", getOutputResult.getErrorStream());
        assertEquals("Output stream should be empty", "", getOutputResult.getOutputStream());
    }
    
}
