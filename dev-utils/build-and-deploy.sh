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
set -e
set -o pipefail

export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh
if (( $($DEV_UTILS_DIR/which-mif-conf-dir-i-use.sh | grep "production" | wc -l) > 0 )); then
	echo "Success - detected production configuration dir";
else
	echo "Failure - detected development configuration dir";
	exit 100;
fi

cd $MIF_ROOT

if [[ "$1" != "skip-build" ]]; then
	skipTests=""
	extraParams=""
	if [[ "$1" == "skip-tests" ]]; then
		skipTests="-DskipTests"
	else 
	        extraParams=$1
	fi
	mvn clean install $skipTests $extraParams | tee build-dump.txt
	if (( $(cat build-dump.txt | grep "BUILD FAILURE" | wc -l) > 0 )); then
		echo "Failed do build MIF"
		exit 100
	fi
fi

if [[ -d "./mif-installer" ]]; then
	rm -rf "./mif-installer"
fi

mifPremiumArchive=$(find mif-distribution/target/ -name *-premium.zip)

if [[ -z $mifPremiumArchive ]]; then
	echo "MIF distribution not found" 
	exit 100
fi

unzip $mifPremiumArchive  -d . 

cp "$MIF_DEV_INSTALLER_SETUP/setup.properties" "./mif-installer"
cp "$MIF_DEV_INSTALLER_SETUP/mif-default.properties" "./mif-installer"


cd mif-installer

./retrieve-mysql-driver.sh

./setup.sh | tee install-dump.txt

cd ..


cd $currentDir
