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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
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
 * Unit test class for {@link JschInvoker}.
 */
public class JschInvokerTest {

	private final static Logger LOG = Logger.getLogger(JschInvokerTest.class);

	private JschInvoker invoker;
	
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

	/**
	 * Test method for {@link com.mango.mif.core.exec.jsch.JschInvoker#execute(java.lang.String)}.
	 * <p>
	 * @throws EncryptionException 
	 * @throws ExecutionException 
	 */
	@Test
	public void testExecute() throws EncryptionException, ExecutionException {
    	final JschParameters parameters = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
    	parameters.setHost(host);
    	parameters.setPort(port);
    	
    	testExecute(parameters);
	}
	
	private void testExecute(final JschParameters parameters) throws ExecutionException {
    	this.invoker = new JschInvoker(parameters);
    	
    	final InvokerResult result1 = this.invoker.execute("echo I Woz Ere >/tmp/fromJSCHInvokerTest.txt");
        LOG.info("Output stream from first command:\n" + result1.getOutputStream());
        LOG.info("Error stream from first command:\n" + result1.getErrorStream());
        assertEquals("First command should have successfully executed", 0, result1.getExitStatus());
        assertEquals("Error stream from first command should be empty", "", result1.getErrorStream());
        assertEquals("Output stream from first command should be empty", "", result1.getOutputStream());
        
        final InvokerResult result2 = this.invoker.execute("cat /tmp/fromJSCHInvokerTest.txt");
        LOG.info("Output stream from second command:\n" + result2.getOutputStream());
        LOG.info("Error stream from second command:\n" + result2.getErrorStream());
        assertEquals("Second command should have successfully executed", 0, result2.getExitStatus());
        assertEquals("Error stream from second command should be empty", "", result2.getErrorStream());
        assertEquals("Checking the content of the output stream from second command", "I Woz Ere\n", result2.getOutputStream());
        
        final InvokerResult result3 = this.invoker.execute("ls -lta "
        		+ (StringUtils.isNotEmpty(parameters.getScriptDirectory()) ? parameters.getScriptDirectory() : "~") + "/.*.sh | head -1");
        LOG.info("Output stream from third command:\n" + result3.getOutputStream());
        LOG.info("Error stream from third command:\n" + result3.getErrorStream());
        assertEquals("Third command should have successfully executed", 0, result3.getExitStatus());
        assertEquals("Error stream from third command should be empty", "", result3.getErrorStream());
        
        final String month = new SimpleDateFormat("MMM").format(Calendar.getInstance().getTime());
        final String dayOfMonth = new SimpleDateFormat("d").format(Calendar.getInstance().getTime());
        final boolean temporaryScriptFileFound =
        		StringUtils.chop(result3.getOutputStream()).matches(".*\\s" + parameters.getUserName() + "\\s.*\\s" + month + "\\s{1,2}" + dayOfMonth + "\\s.*\\..*\\.sh");
        assertTrue("The temporary shell script should have been created in the appropriate directory", temporaryScriptFileFound);
        
	}
	
	/**
	 * Test method for {@link com.mango.mif.core.exec.jsch.JschInvoker#execute(java.lang.String)}.
	 * <p>
	 * As per {@link #testExecute()} but specifying a custom directory for the temporary shell script to be created within.
	 * <p>
	 * @throws EncryptionException 
	 * @throws ExecutionException 
	 */
	@Test
	public void testExecuteWithCustomScriptDirectory() throws EncryptionException, ExecutionException {
    	final JschParameters parameters = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
    	parameters.setHost(host);
    	parameters.setPort(port);
    	parameters.setScriptDirectory("/tmp"); // Usually defaults to the user's home directory
    	
    	testExecute(parameters);
	}

}
