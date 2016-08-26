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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

/**
 * Simple DES encryption and decryption
 */
public class DesEncrypter implements Encrypter {

    private static final Logger LOG = Logger.getLogger(DesEncrypter.class);

    private static final URL DEFAULT_KEY_URL = DesEncrypter.class.getResource("defaultDesKey.key");

    /**
     * encryptor
     */
    private Cipher ecipher;
    /**
     * decryptor
     */
    private Cipher dcipher;

    /**
     * Default constructor uses the default key 
     * @throws EncryptionException if the encrypter cannot be initialised with the default key
     */
    public DesEncrypter() throws EncryptionException {
        initFromURL(DEFAULT_KEY_URL);
    }

    /**
     * Constructor
     * @param keyFile a string representation of a URL to a file containing the key, if null a default key will be used
     * @throws EncryptionException if the encrypter cannot be initialised with the specified or default key 
     */
    public DesEncrypter(final String keyFile) throws EncryptionException {

        URL keyFileURL = DEFAULT_KEY_URL;
        if (!StringUtils.isBlank(keyFile)) {
            try {
                keyFileURL = new URL(keyFile);
            } catch (Exception e) {
                throw new EncryptionException("The Encryption key file URL provided is invalid [URL " + keyFile + "]", e);
            }
        }

        if (DEFAULT_KEY_URL.equals(keyFileURL)) {
            logDefaultKeyWarning();
        }

        initFromURL(keyFileURL);
    }

    /**
     * Log a warning to warn against using the default key 
     */
    private void logDefaultKeyWarning() {
        LOG.warn(StringUtils.repeat("-", 25));
        LOG.warn("Using default encryption key");
        LOG.warn("THIS SHOULD NOT BE USED IN A PRODUCTION ENVIRONMENT");
        LOG.warn("To resolve this please set mif.encryption.key to a valid URL");
        LOG.warn(StringUtils.repeat("-", 25));
    }

    /**
     * Initialises the encryption and decryption ciphers using the secret key located at the URL. 
     * @param keyFileURL a non-null file containing the key
     * @throws EncryptionException if the initialisation fails
     */
    private void initFromURL(URL keyFileURL) throws EncryptionException {
        try {
            final SecretKey secretKey = getKeyFromFile(keyFileURL);
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, secretKey);
            dcipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (Exception e) {
            throw new EncryptionException("Cannot initialize DesEncrypter", e);
        }
    }

    /**
     * Loads the key from the specified location
     * @param keyFile the URL to resolve the key file
     * @return the {@link SecretKey} generated from the file
     * @throws IOException if the file could not be read
     * @throws ClassNotFoundException if the contents of the file are not a {@link SecretKey}
     */
    private SecretKey getKeyFromFile(final URL keyFile) throws IOException, ClassNotFoundException {
        SecretKey key = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(keyFile.openStream());
            key = (SecretKey) ois.readObject();
        } finally {
            IOUtils.closeQuietly(ois);
        }
        return key;
    }

    @Override
    public synchronized String encrypt(final String message) throws EncryptionException {
        Preconditions.checkNotNull(message, "Can't encrypt null message");
        try {
            byte[] utf8 = message.getBytes("UTF8");
            byte[] enc = ecipher.doFinal(utf8);
            return Base64.encodeBase64URLSafeString(enc);
        } catch (Exception e) {
            throw new EncryptionException("Could not encrypt the message", e);
        }
    }

    @Override
    public synchronized String decrypt(final String cryptogram) throws EncryptionException {
        Preconditions.checkNotNull(cryptogram, "Can't decrypt null message");
        try {
            byte[] dec = Base64.decodeBase64(cryptogram);
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, "UTF8");
        } catch (Exception e) {
            throw new EncryptionException("Could not decrypt the message", e);
        }
    }
}