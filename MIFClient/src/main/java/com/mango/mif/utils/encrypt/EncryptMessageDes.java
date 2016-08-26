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

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.StringOptionHandler;


/**
 * 
 * Command encrypting a given message using DES encryption
 * 
 */
public class EncryptMessageDes implements DesEncrypterCommand {

    private static final Logger logger = Logger.getLogger(EncryptMessageDes.class);
    
    private CmdLineParser parser = null;
    
    @Argument(index=0, required=true, metaVar="MESSAGE", handler=StringOptionHandler.class, usage = "Message to be encrypted")
    private String message;
    
    @Argument(index=1, metaVar="KEY_FILE", handler=StringOptionHandler.class, usage = "Key file name")
    private String keyFile;

    public EncryptMessageDes() {
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
                
        Encrypter desEncrypter = null;
        try {
            desEncrypter = new DesEncrypter(keyFile);

            System.out.println("Encrypted message:");
            System.out.println(desEncrypter.encrypt(message));

        } catch (Exception e) {
            logger.error("Message could not be encrypted", e);
        }
    }

}
