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
#!/bin/bash
#
# This script automates the process of switching between Production and Development setups
#
export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh

function printUsage {
	echo "The command requires an argument"
	echo "allowed arguments: 'dev' or 'prod'"
}

function cleanLocations {
	rm $MIF_INSTALL_DIR
	if [[ -d $TOMCAT_HOME/webapps/MIFServer ]]; then 
		rm -rf $TOMCAT_HOME/webapps/MIFServer
	fi
	if [[ -f $TOMCAT_HOME/conf/Catalina/localhost/MIFServer.xml ]]; then
		rm $TOMCAT_HOME/conf/Catalina/localhost/MIFServer.xml
	fi
}

if [[ -z $1 ]]; then
	printUsage
	exit 100
fi

if [[ $1 == "dev" ]]; then
	echo "switching to mif-dev"
	cleanLocations
	ln -s $MIF_DEV_DIR $MIF_INSTALL_DIR
elif [[ $1 == "prod" ]]; then
	echo "switching to mif-prod"
	cleanLocations
	ln -s $MIF_PROD_DIR $MIF_INSTALL_DIR
else
	echo "Unercognized argument $1"
	printUsage
	exit 100
fi

