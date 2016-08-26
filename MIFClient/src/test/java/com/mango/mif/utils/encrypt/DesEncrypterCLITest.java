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
import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mango.mif.connector.TestsHelper;


/**
 * 
 * Test the DES Encrypter CLI
 */
public class DesEncrypterCLITest {

    /**
     * Key file
     */
    private File keyFile;
    private File tmpDir;

    @Before
    public void setUp() {
        tmpDir = TestsHelper.createTmpDirectory();
        keyFile = new File(tmpDir,"generatedTestKey.key");
    }
    
    @After
    public void tearDown() {
        FileUtils.deleteQuietly(tmpDir);
    }
    
    @Test
    public void shouldCreateAFileContainingGeneratedKey() {
        DesEncrypterCLI.main(new String[] {DesEncrypterCLI.Command.generateKeyFile.name(),"shouldCreateAFileContainingGeneratedKey", keyFile.getAbsolutePath()});
        assertTrue(keyFile.exists());
    }

    @Test
    public void shouldCreateAFileContainingGeneratedKeyAndUseItForEncryptionDecryption() throws EncryptionException, MalformedURLException {
        DesEncrypterCLI.main(new String[] {DesEncrypterCLI.Command.generateKeyFile.name(),"shouldCreateAFileContainingGeneratedKeyAndUseItForEncryptionDecryption", keyFile.getAbsolutePath()});
        DesEncrypter tmpEncrypter = new DesEncrypter(keyFile.toURI().toURL().toExternalForm());

        String valueToBeEncrypted = "testststststststs";
        String encrypt = tmpEncrypter.encrypt(valueToBeEncrypted);
        String decryptedValue = tmpEncrypter.decrypt(encrypt);
        assertThat("Message is null", encrypt, is(not(equalTo(valueToBeEncrypted))));
        assertThat("Message is null", decryptedValue, is(equalTo(valueToBeEncrypted)));

    }
    

    @Test
    public void shouldEncryptAMessageUsingDefaultKey() {
        DesEncrypterCLI.main(new String[] {DesEncrypterCLI.Command.encryptMessage.name(),"Message encrypted using default key"});
    }

    @Test
    public void shouldEncryptAMessageUsingNewlyGenerateKey() throws MalformedURLException {
        DesEncrypterCLI.main(new String[] {DesEncrypterCLI.Command.generateKeyFile.name(),"shouldEncryptAMessageUsingNewlyGenerateKey", keyFile.getAbsolutePath()});
        assertTrue(keyFile.exists());
        
        DesEncrypterCLI.main(new String[] {DesEncrypterCLI.Command.encryptMessage.name(),"My message to be encrypted", keyFile.toURI().toURL().toExternalForm()});
    }    

}
