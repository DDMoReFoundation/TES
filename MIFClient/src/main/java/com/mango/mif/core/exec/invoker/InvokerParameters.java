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
/**
 * @version $Revision: $ as of $Date: $
 * <p>SVN Entry : $HeadURL: $
 * <p>SVN ID    : $Id: $
 * <p>Last edited by : $Author: $
 */
package com.mango.mif.core.exec.invoker;

/**
 * Simple bean containing all the parameters needed to configure an SSH session
 */
public interface InvokerParameters extends Cloneable {

    /**
     * @return the username, could be null
     */
    String getUserName();

    /**
     * @param userName the username to set, accepts null
     */
    void setUserName(String userName);

    /**
     * @param userName the username to set, accepts null
     * @return this object with the username updated
     */
    InvokerParameters userName(String userName);

    /**
     * @return the unencrypted password, could be null
     */
    String getClearTextPassword();

    /**
     * @param clearTextPassword the unencrypted password to set, accepts null
     */
    void setClearTextPassword(String clearTextPassword);

    /**
     * @param clearTextPassword the unencrypted password to set, accepts null
     * @return this object with the password updated
     */
    InvokerParameters clearTextPassword(String clearTextPassword);

    /**
     * @return the hostname, could be null
     */
    String getHost();

    /**
     * @param host the hostname to set, does not accept null
     */
    void setHost(String host);

    /**
     * @param host the hostname to set, does not accept null
     * @return this object with the hostname updated
     */
    InvokerParameters host(String host);

    /**
     * @return the character set, could be null
     */
    String getCharSet();

    /**
     * @param charSet the character set to set, accepts null
     */
    void setCharSet(String charSet);

    /**
     * @param charSet the character set to set, accepts null
     * @return this object with the character set updated
     */
    InvokerParameters charSet(String charSet);

    /**
     * @return the port
     */
    int getPort();

    /**
     * @param port the port number to set
     */
    void setPort(int port);

    /**
     * @param port the port number to set
     * @return this object with the port number updated
     */
    InvokerParameters port(int port);

    /**
     * @return the timeout to use for executions
     */
    int getTimeoutInMilliseconds();

    /**
     * @param timeoutInMilliseconds the timeout to use for executions
     */
    void setTimeoutInMilliseconds(int timeoutInMilliseconds);

    /**
     * @param timeoutInMilliseconds the timeout to use for executions
     * @return this object with the timeout updated
     */
    InvokerParameters timeoutInMilliseconds(int timeoutInMilliseconds);

    /**
     * @return a comma seperated list of accepted protocols
     */
    String getProtocols();

    /**
     * @param protocols a comma seperated list of accepted protocols, cannot be null
     */
    void setProtocols(String protocols);

    /**
     * @param protocols a comma seperated list of accepted protocols, cannot be null
     * @return this object with the protocols updated
     */
    InvokerParameters protocols(String protocols);

    /**
     * @return the directory into which the command will be written as a script file.
     * Blank means it will be written into the user's home directory when using JSCH.
     */
    String getScriptDirectory();

    /**
     * @param scriptDirectory the directory into which each command will be saved
     */
    void setScriptDirectory(String scriptDirectory);

    /**
     * @param scriptDirectory the directory into which each command will be saved
     */
    InvokerParameters scriptDirectory(String scriptDirectory);

    /**
     * Clones this instance and gives you a duplicate
     * @see Object#clone
     */
    InvokerParameters cloneParameters();
}