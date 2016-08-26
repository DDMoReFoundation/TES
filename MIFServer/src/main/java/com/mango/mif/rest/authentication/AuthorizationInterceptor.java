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
package com.mango.mif.rest.authentication;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;

/**
 * Provides client authorization service.
 */
@SuppressWarnings("rawtypes")
public class AuthorizationInterceptor extends AbstractPhaseInterceptor {

    private Logger logger = Logger.getLogger(AuthorizationInterceptor.class);
    private Map<String, String> userCredentials;
    private boolean authEnabled = false;

    public AuthorizationInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (isAuthEnabled()) {
            if (policy == null) {
                logger.error("Application attempted to use services without credentials");
                throw new RuntimeException("Access denied to user, credentials not provided or incorrect");
            }
            if (!checkCredentials(policy.getUserName(), policy.getPassword())) {
                logger.error("Access denied to user, credentials not provided " + policy.getUserName());
                throw new RuntimeException("Access denied to user, credentials not provided or incorrect");
            }
        } 
    }

    /**
     * Check application username and password
     * @param username
     * @param password 
     * @return true if the credentials correspond to the ones defined in the map, false otherwise
     */
    private boolean checkCredentials(String username, String password) {
        
        if (userCredentials.containsKey(username) && StringUtils.equals(userCredentials.get(username), password)) {
            logger.debug("Check credentials [SUCCESS]: " + username);
            return true;
        }
        logger.debug("Check credentials [FAILURE]: " + username);
        return false;
    }

    /**
     * @param userCredentials the userCredentials to set
     */
    public void setUserCredentials(Map<String, String> userCredentials) {
        this.userCredentials = userCredentials;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

}
