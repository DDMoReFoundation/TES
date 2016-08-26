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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.StringOptionHandler;

import com.google.common.base.Preconditions;

/**
 * Command generating a file containing DES key
 */
public class GenerateDesKeyFile implements DesEncrypterCommand {

    private static final Logger logger = Logger.getLogger(GenerateDesKeyFile.class);
    private CmdLineParser parser = null;
    
    @Argument(index=0, required=true, metaVar="PASS_PHRASE", handler=StringOptionHandler.class, usage = "Pass phrase to be used as a key")
    private String key;
    
    @Argument(index=1, handler=StringOptionHandler.class, usage = "Key file name")
    private String keyFile;

    public GenerateDesKeyFile() {
        parser = new CmdLineParser(this);
    }

    @Override
    public void printUsageInfo(OutputStream outputStream) {
        parser.printUsage(outputStream);
    }
    
    @Override
    public void execute(String[] args) {
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            throw new RuntimeException(e.getMessage());
        }
        Preconditions.checkArgument(!new File(keyFile).exists(), "Specified key file already exists");
        
        generateKeyFile(key, keyFile);
    }
    /**
     * Generates a key for given pass phrase and stores it in the given file
     * @param key
     * @param keyFileName
     */
    static void generateKeyFile(String key, String keyFileName) {
        SecretKey secretKey = generateKey(key);
        ObjectOutputStream oos = null;
        FileOutputStream fos = null;
        try {
            File keyFile = new File(keyFileName);
            fos = new FileOutputStream(keyFile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(secretKey);
            System.out.println("Key saved to file " + keyFile);
        } catch (IOException e) {
            logger.error(e);
            System.exit(-1);
        } finally {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(oos);
        }
    }

    /**
     * Generates SecretKey for the given pass phrase
     * @param passPhrase
     * @return
     */
    static SecretKey generateKey(String passPhrase) {
        SecretKeyFactory keyFactory = null;
        try {
            keyFactory = SecretKeyFactory.getInstance("DES");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            throw new RuntimeException("DES algorithm not availiable.",e);
        }

        byte[] in = null;
        try {
            in = passPhrase.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Key phrase could not be converted to bytes", e);
            throw new RuntimeException("Key phrase could not be converted to bytes.",e);
        }

        SecretKey secretKey = null;
        try {
            secretKey = keyFactory.generateSecret(new DESKeySpec(in));
        } catch (Exception e) {
            logger.error("Could not generate key from the given pass phrase",e);
            throw new RuntimeException("Could not generate key from the given pass phrase.",e);
        }
        return secretKey;
    }
}
