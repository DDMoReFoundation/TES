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
package com.mango.mif.core.exec.ce;

import com.mango.mif.core.exec.InvokerResult;

/**
 * Not really very much for this class to do (except be concrete), since all the common code was shoved into InvokerResult.
 * This is being kept just in case there really are differences between commons exec and JSCH.
 */
public class CeInvokerResult extends InvokerResult {

    public CeInvokerResult(String command, String stdout, String stderr, int exitCode) {
        super(command, stdout, stderr, exitCode);
    }
}
