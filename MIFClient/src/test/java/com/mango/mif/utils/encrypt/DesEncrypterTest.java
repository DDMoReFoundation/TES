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
package com.mango.mif.utils.encrypt;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.connector.TestsHelper;

/**
 * Tests DesEncrypter
 */
public class DesEncrypterTest {
	/**
	 * Logger
	 */
	private final static Logger LOG = Logger.getLogger(DesEncrypterTest.class);

	private Encrypter encrypter;
	
    @Before
    public void setUp() throws EncryptionException  {
        // use the test key
        encrypter = new DesEncrypter(DesEncrypterTest.class.getResource("testDesKey.key").toExternalForm());
    }

    @Test
    public void shouldEncryptAndDecryptAString() throws EncryptionException {        
        String valueToBeEncrypted = "tstsststss";
        String encrypt = encrypter.encrypt(valueToBeEncrypted);
        String decryptedValue = encrypter.decrypt(encrypt);
        LOG.debug("encrypt: " + encrypt);
        LOG.debug("decrypt: " + decryptedValue);
        assertThat("Encrypted message is plaintext", encrypt, is(not(equalTo(valueToBeEncrypted))));
        assertThat("Decrypted message does not match original", decryptedValue, is(equalTo(valueToBeEncrypted)));
    }

    @Test
    public void shouldEncryptAndDecryptAStringFromGeneratedFile() throws EncryptionException, FileNotFoundException, IOException {

        // setup the new keyfile
        SecretKey key = GenerateDesKeyFile.generateKey("testPhrase");
        File tmpDir = TestsHelper.createTmpDirectory();
        try {
            File keyFile = new File(tmpDir,"generatedTestKey.key");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(keyFile));
            oos.writeObject(key);
            
            Encrypter encrypter2 = new DesEncrypter(keyFile.toURI().toURL().toExternalForm());
            
            String valueToBeEncrypted = "tstsststss";
            String encrypt = encrypter2.encrypt(valueToBeEncrypted);
            String decryptedValue = encrypter2.decrypt(encrypt);
            LOG.debug("encrypt: " + encrypt);
            LOG.debug("decrypt: " + decryptedValue);
            assertThat("Encrypted message is plaintext", encrypt, is(not(equalTo(valueToBeEncrypted))));
            assertThat("Decrypted message does not match original", decryptedValue, is(equalTo(valueToBeEncrypted)));
            assertThat("Encrypted message the same for different keys", encrypt, is(not(equalTo(encrypter.encrypt(valueToBeEncrypted)))));
        } catch(Exception e) {
            LOG.error(e);
        } finally {
            FileUtils.deleteQuietly(tmpDir);
        }
    }
    
    @Test
    public void defaultConstructionWorks() throws EncryptionException {
        Encrypter encrypter2 = new DesEncrypter();
        
        String valueToBeEncrypted = "tstsststss";
        String encrypt = encrypter2.encrypt(valueToBeEncrypted);
        String decryptedValue = encrypter2.decrypt(encrypt);
        LOG.debug("encrypt: " + encrypt);
        LOG.debug("decrypt: " + decryptedValue);
        assertThat("Encrypted message is plaintext", encrypt, is(not(equalTo(valueToBeEncrypted))));
        assertThat("Decrypted message does not match original", decryptedValue, is(equalTo(valueToBeEncrypted)));
    }
    
    @Test(expected=EncryptionException.class)
    public void testInvalidURLThrowsEx() throws EncryptionException {
        new DesEncrypter("I am not a URL");
    }
}
