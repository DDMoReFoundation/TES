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

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Charsets;
import com.mango.mif.core.exec.invoker.InvokerParameters;

/**
 * Collect together everything needed, and everything optional, to create a JSCH session.
 * This is here because of the way in which JSCH sessions are pooled by the JschSessionPool
 * and it allows the JschSessionPoolObjectFactory to create new sessions via the parameters
 * which contain all the information we could possibly need.
 */
public class JschParameters implements InvokerParameters {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 22;
    private static final int SECONDS = 1000;
    private static final int DEFAULT_TIMEOUT = 10 * SECONDS;
    private static final String DEFAULT_CHARSET = Charsets.UTF_8.name();
    private static final String DEFAULT_PROTOCOLS = "publickey,keyboard-interactive,password";
    private static final String DEFAULT_SCRIPT_DIRECTORY = "";

    private String userName;
    private String clearTextPassword;
    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private String charSet = DEFAULT_CHARSET;
    private int timeoutInMilliseconds = DEFAULT_TIMEOUT;
    private String protocols = DEFAULT_PROTOCOLS;
    private String scriptDirectory = DEFAULT_SCRIPT_DIRECTORY;

    public JschParameters() {
        // default construction
    }

    public JschParameters(String userName, String clearTextPassword) {
        this.userName = userName;
        this.clearTextPassword = clearTextPassword;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public InvokerParameters userName(String userName) {
        setUserName(userName);
        return this;
    }

    @Override
    public String getClearTextPassword() {
        return clearTextPassword;
    }

    @Override
    public void setClearTextPassword(String clearTextPassword) {
        this.clearTextPassword = clearTextPassword;
    }

    @Override
    public InvokerParameters clearTextPassword(String clearTextPassword) {
        setClearTextPassword(clearTextPassword);
        return this;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        if (host != null) {
            this.host = host;
        }
    }

    @Override
    public InvokerParameters host(String host) {
        setHost(host);
        return this;
    }

    @Override
    public String getCharSet() {
        return charSet;
    }

    @Override
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    @Override
    public InvokerParameters charSet(String charSet) {
        setCharSet(charSet);
        return this;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public InvokerParameters port(int port) {
        setPort(port);
        return this;
    }

    @Override
    public int getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    @Override
    public void setTimeoutInMilliseconds(int timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    @Override
    public InvokerParameters timeoutInMilliseconds(int timeoutInMilliseconds) {
        setTimeoutInMilliseconds(timeoutInMilliseconds);
        return this;
    }

    @Override
    public String getProtocols() {
        return protocols;
    }

    @Override
    public void setProtocols(String protocols) {
        if (!StringUtils.isBlank(protocols)) {
            this.protocols = protocols;
        }
    }

    @Override
    public InvokerParameters protocols(String protocols) {
        setProtocols(protocols);
        return this;
    }

    /**
     * @return the scriptDirectory
     */
    @Override
    public String getScriptDirectory() {
        return scriptDirectory;
    }

    /**
     * @param scriptDirectory the scriptDirectory to set
     */
    @Override
    public void setScriptDirectory(String scriptDirectory) {
        this.scriptDirectory = scriptDirectory;
    }

    @Override
    public InvokerParameters scriptDirectory(String scriptDirectory) {
        setScriptDirectory(scriptDirectory);
        return this;
    }

    //=========================================================================


    @Override
    public String toString() {
        return "[" + userName
                //+ ", password: " + clearTextPassword
                + ", " + host
                + ", " + port + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JschParameters other = (JschParameters) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }

    //=========================================================================

    /**
     * merges the invoker parameters with the given one
     * @param invokerParameters
     */
    public JschParameters merge(JschParameters invokerParameters) {
        userName = (invokerParameters.userName != null) ? invokerParameters.userName : userName;
        clearTextPassword = (invokerParameters.clearTextPassword != null) ? invokerParameters.clearTextPassword : clearTextPassword;
        host = (invokerParameters.host != null) ? invokerParameters.host : host;
        charSet = (invokerParameters.charSet != null) ? invokerParameters.charSet : charSet;
        port = (invokerParameters.port > 0) ? invokerParameters.port : port;
        timeoutInMilliseconds = (invokerParameters.timeoutInMilliseconds > 0) ? invokerParameters.timeoutInMilliseconds : timeoutInMilliseconds;
        protocols = (invokerParameters.protocols != null) ? invokerParameters.protocols : protocols;
        return this;
    }

    public InvokerParameters cloneParameters() {
        try {
            return (InvokerParameters) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen as the interface implements Cloneable
            throw new IllegalStateException("Could not clone the parameters", e);
        }
    }
}
