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

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;


public final class MIFJobStatusHelper {
    private MIFJobStatusHelper() {
        
    }
    public static boolean failed(String status) {
        Preconditions.checkArgument(StringUtils.isNotBlank(status),"Job status name can't be blank");
        return "FAILED".equals(status);
    }

    public static boolean complete(String status) {
        Preconditions.checkArgument(StringUtils.isNotBlank(status),"Job status name can't be blank");
        return "COMPLETED".equals(status);
    }

    public static boolean cancelled(String status) {
        Preconditions.checkArgument(StringUtils.isNotBlank(status),"Job status name can't be blank");
        return "CANCELLED".equals(status);
    }
    
    public static boolean running(String status) {
        Preconditions.checkArgument(StringUtils.isNotBlank(status),"Job status name can't be blank");
        return !failed(status)&&!complete(status)&&!cancelled(status);
    }
}
