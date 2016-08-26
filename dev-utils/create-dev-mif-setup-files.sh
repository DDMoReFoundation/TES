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
# This script prepares directory containing mif-installer configuration files valid to navws environment
#
export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh

if [[ ! -d $DEV_DIR ]]; then
	echo "$DEV_DIR does not exist, run $DEV_UTILS_DIR/prepare-directories.sh first"
	exit 100
fi

mifServiceUser=$1
mifServicePassword=$2
mifServiceGroup=$3
echo $mifServiceUser $mifServicePassword $mifServiceGroup
cd $DEV_DIR

if [[ -d $MIF_DEV_INSTALLER_SETUP ]]; then
	echo "Installer setup dir already exists, removing"
	rm -rf $MIF_DEV_INSTALLER_SETUP
fi 

mkdir $MIF_DEV_INSTALLER_SETUP

cd $MIF_DEV_INSTALLER_SETUP


DEFAULT_PROPERTIES=mif-default.properties
echo "" > mif-default.properties
setProperty "mif.authorization.enabled" "false" $DEFAULT_PROPERTIES
setProperty "mif.hibernate.hbm2ddl.auto" "validate" $DEFAULT_PROPERTIES

SETUP_PROPERTIES=setup.properties
cp $MIF_ROOT/mif-distribution/src/main/scripts/installer/setup.properties .
setProperty "TOMCAT_WEBAPPS_DIR" "$TOMCAT_HOME/webapps" $SETUP_PROPERTIES
setProperty "MIF_ENCRYPTION_KEY" "MIF-encryption-key-default.key" $SETUP_PROPERTIES

# increasing amount of time the setup script will spend pinging MIF before failing the installation
setProperty "PING_TIMEOUT" "20" $SETUP_PROPERTIES


while [[ -z $mifServiceUser ]]; do 
	read -p "Please provide MIF Service Account (i.e. of tomcat user - yourself or navplus):" mifServiceUser
done
setProperty "MIF_SERVICE_USER" "$mifServiceUser" $SETUP_PROPERTIES

while [[ -z $mifServicePassword ]]; do
        read -p "Please provide MIF Service Account Password (i.e. of tomcat user - yours or navplus'):" mifServicePassword
done
setProperty "MIF_SERVICE_PASSWORD" "$mifServicePassword" $SETUP_PROPERTIES

while [[ -z $mifServiceGroup ]]; do
        read -p "Please provide MIF Service Account Password (i.e. of tomcat user - yours or navplus') [default = \"Domain Users\"]:" mifServicePassword
	if [[ -z $mifServiceGroup ]]; then
		mifServiceGroup="Domain Users"
	fi
done
setProperty "MIF_SERVICE_USER_GROUP" "\"$mifServiceGroup\"" $SETUP_PROPERTIES

echo "Created $DEFAULT_PROPERTIES file with the following contents"
cat $DEFAULT_PROPERTIES

echo "Created $SETUP_PROPERTIES file with the following contents"
cat $SETUP_PROPERTIES

cd $currentDir
