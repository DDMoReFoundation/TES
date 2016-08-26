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
package com.mango.mif;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class MIFHealthcheckIT {

    private static final String MIF_HOST = "localhost";
    private static final String MIF_PORT = "9000";
    
    //MIF REST URL template, it is populated with MIF_HOST, MIF_PORT and MIF_SERVICE
    private static final String TEMPLATE_URL = "http://<host>:<port>/<service>";
    private static final String MIF_SERVICE = "MIFServer/REST/services/";
    private MIFHttpRestClient restClient;
    
    @Before
    public void setUp() {

        String mifUrl = TEMPLATE_URL;
        mifUrl = mifUrl.replaceAll("<host>",MIF_HOST);
        mifUrl = mifUrl.replaceAll("<port>",MIF_PORT);
        mifUrl = mifUrl.replaceAll("<service>",MIF_SERVICE);
        
        restClient = new MIFHttpRestClient(mifUrl);
    }

    @Test
    public void shouldHealthcheckService() {
        assertTrue(restClient.healthcheck());
    }
}
