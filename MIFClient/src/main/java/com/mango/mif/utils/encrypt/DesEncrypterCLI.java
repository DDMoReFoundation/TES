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

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;

/**
 * 
 * DES command line encrypter
 */
public class DesEncrypterCLI {
    /**
     * Possible commands to be executed by Des Encrypter
     * @author mrogalski
     *
     */
    static enum Command {
        encryptMessage
        {
            DesEncrypterCommand encrypterCommand = new EncryptMessageDes();
            @Override
            DesEncrypterCommand getEncrypterCommand() {
                return encrypterCommand;
            }
        },
        generateKeyFile {
            DesEncrypterCommand encrypterCommand = new GenerateDesKeyFile();
            @Override
            DesEncrypterCommand getEncrypterCommand() {
                return encrypterCommand;
            }
        };
        
        abstract DesEncrypterCommand getEncrypterCommand();
    }
    
    @Argument(index=0, required=true, metaVar="COMMAND", usage = "Command that should be invoked [\"encryptMessage\"|\"generateKeyFile\"]")
    private Command command;

    @Argument(index=1, required=true, metaVar="PARAMETERS", usage = "Command parameters")
    private List<String> commandParameters;
    /**
     * Used to generate a key file
     * @param args
     */
    public static void main(String[] args) {
        DesEncrypterCLI cli = new DesEncrypterCLI();
        CmdLineParser parser = new CmdLineParser(cli);
        
        try {
            parser.parseArgument(args);
        } catch (ArrayIndexOutOfBoundsException e) {
            //Args4j doesn't handle enums properly
            exit(parser,"Command not recognized");
        } catch (Exception e) {
            exit(parser,e.getMessage());
        }
        
        DesEncrypterCommand encrypterCommand = cli.command.getEncrypterCommand();

        try {
            encrypterCommand.execute(cli.commandParameters.toArray(new String[cli.commandParameters.size()]));
        } catch(Exception e) {
            exit(parser,e.getMessage());
        }
    }
    /**
     * Exits
     * @param parser
     * @param msg
     */
    private static void exit(CmdLineParser parser, String msg) {        
        System.err.println(msg);
        parser.printUsage(System.err);
        for(Command command : Command.values()) {
            System.err.println("COMMAND: " + command.name());
            command.getEncrypterCommand().printUsageInfo(System.err);
        }
        System.exit(0);
    }
}
