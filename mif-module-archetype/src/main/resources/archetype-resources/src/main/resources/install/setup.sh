#*******************************************************************************
# Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the
# Free Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License along
# with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
#*******************************************************************************
#!/usr/bin/env bash
source "$INSTALLER_LIB"
#
# You should just use the variables below, you should not make assumptions on the locations in MIF Installation directory
#
# The following properties are available:
# * MIF_CONF_DIR - MIF configuration directory
# * MIF_PROPERTIES_FILE - MIF properties file
# * PLUGIN_ID - plugin ID (artifact id)
# * PLUGIN_INSTALL_DIR - this plugin's install location
# The script is executed in the 'install' directory where MIF setup extracted plugin's files. The files available in the directory
# -> PLUGIN_SETUP_DIR
# ---->*.jar
# ---->runtime\
# ---->install\script.sh    (* that is the current working directory)
#
# Plugin files that are registered in MIF are:
# -> PLUGIN_INSTALL_DIR
# ---->configuration
# ---->scripts
# ---->templates
# ---->lib
#
# default validation
#
props=("MIF_CONF_DIR" "PLUGIN_INSTALL_DIR" "MIF_PROPERTIES_FILE" "PLUGIN_ID")
noOfFailures=0
for prop in "${props[@]}"; do
    verifyProperty "${prop}"
    noOfFailures=$((noOfFailures + $? ))
done
if (( noOfFailures != 0 )); then
    exit 100
fi

#
# Setup
#
