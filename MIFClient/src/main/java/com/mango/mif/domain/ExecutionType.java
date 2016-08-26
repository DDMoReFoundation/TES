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
package com.mango.mif.domain;

/**
 * Holds a list of execution types
 */
public enum ExecutionType {

    /**
     * NMFE execution type.
     */
    NMFE,

    /**
     * PsN Exec .
     */
    PsN_EXEC,

    /**
     * PsN VPC.
     */
    PsN_VPC,

    /**
     * PsN SCM.
     */
    PsN_SCM,

    /**
     * PsN Bootstrap.
     */
    PsN_Bootstrap,

    /**
     * R Script.
     */
    R_Script,

    /**
     * SAS Script.
     */
    SAS_Script,

    /**
     * MATLAB Script.
     */
    MATLAB_Script,

    /**
     * Shell Command.
     */
    COMMAND_LINE,

    /**
     * Unknown Profile type
     */
    Unknown;

}
