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
export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh
export DEV_SETUP_PLUGINS_DIR=$MIF_DEV_DIR/etc/plugins
export DEV_BIN_PLUGINS_DIR=$MIF_DEV_DIR/bin/plugins
export BUILD_ENV=$MIF_ROOT/build-env.properties

echo "MIF ROOT directory: $MIF_ROOT"
echo "MIF DEV setup directory: $MIF_DEV_DIR"

if [[ -d $MIF_DEV_DIR ]]; then
	echo "$MIF_DEV_DIR exists, deleting it"
	rm -rf $MIF_DEV_DIR
fi

echo "Creating MIF installation directory structure in $MIF_DEV_DIR"
mkdir $MIF_DEV_DIR
mkdir $MIF_DEV_DIR/etc
mkdir $MIF_DEV_DIR/log
mkdir $MIF_DEV_DIR/metadata
mkdir $MIF_DEV_DIR/bin
mkdir $DEV_BIN_PLUGINS_DIR


echo "Creating symlinks to runtime directories of mif modules"
mkdir $DEV_SETUP_PLUGINS_DIR

#
# Creates a link from mif installation directory to module's configuration directory
#
function createConfigurationLink {
	local moduleName=$1
	echo "Creating symlink to $moduleName"
	ln -s $MIF_ROOT/$moduleName/$TARGET_CLASS_DIR/runtime/configuration $DEV_SETUP_PLUGINS_DIR/$moduleName
}
#
# Creates a link from mif installation directory to module's runtime directories
#
function createRuntimeLinks {
        local moduleName=$1
	local moduleBinDir=$DEV_BIN_PLUGINS_DIR/$moduleName
	if [[ -d $moduleBinDir ]]; then
		rm -rf "$moduleBinDir"
	fi
	mkdir $moduleBinDir
        echo "Creating symlinks for templates and scripts of $moduleName"
        ln -s $MIF_ROOT/$moduleName/$TARGET_CLASS_DIR/runtime/scripts $moduleBinDir/scripts
        ln -s $MIF_ROOT/$moduleName/$TARGET_CLASS_DIR/runtime/templates $moduleBinDir/templates
}


for module in "${modules[@]}"; do
	createConfigurationLink "${module}"
	createRuntimeLinks "${module}"
done

echo "Preparing mif.properties"

export MIF_CONF_DIR=$MIF_DEV_DIR
export MIF_PROPERTIES_FILE=$MIF_DEV_DIR/etc/mif.properties

echo "" > $MIF_PROPERTIES_FILE
echo "Copying properties from $BUILD_ENV file"

copyProperty 'mif.serviceAccount.userName' $BUILD_ENV $MIF_PROPERTIES_FILE
copyProperty 'mif.serviceAccount.userPassword' $BUILD_ENV $MIF_PROPERTIES_FILE
copyProperty 'mif.transportconnector.url' $BUILD_ENV $MIF_PROPERTIES_FILE

SETUP_PROP_TEMPLATE=$MIF_ROOT/mif-distribution/src/main/scripts/installer/setup.properties
echo "Copying properties from $SETUP_PROP_TEMPLATE file"
copyProperty 'MIF_DB_USER' $SETUP_PROP_TEMPLATE $MIF_PROPERTIES_FILE 'mif.hibernate.user'
copyProperty 'MIF_DB_PASSWORD' $SETUP_PROP_TEMPLATE $MIF_PROPERTIES_FILE 'mif.hibernate.password'
copyProperty 'MIF_DB_URL' $SETUP_PROP_TEMPLATE $MIF_PROPERTIES_FILE 'mif.hibernate.jdbcUrl'
copyProperty 'MIF_DB_DIALECT' $SETUP_PROP_TEMPLATE $MIF_PROPERTIES_FILE 'mif.hibernate.dialect'
copyProperty 'MIF_DB_DRIVER' $SETUP_PROP_TEMPLATE $MIF_PROPERTIES_FILE 'mif.hibernate.driverClass'

echo "Setting properties"
setProperty 'mif.working.dir' '/opt/mango/mif/metadata' $MIF_PROPERTIES_FILE
setProperty 'mif.authorization.enabled' 'false' $MIF_PROPERTIES_FILE
MIF_RUNTIME_DIRECTORY="$MIF_ROOT/MIFServer/target/runtime"
# scripts and templates locations
setProperty "mif.templatesDirectory" "$MIF_RUNTIME_DIRECTORY/templates" $MIF_PROPERTIES_FILE
setProperty "mif.commonScriptsDirectory" "$MIF_RUNTIME_DIRECTORY/scripts/common" $MIF_PROPERTIES_FILE
setProperty "mif.genericScriptsDirectory" "$MIF_RUNTIME_DIRECTORY/scripts/generic" $MIF_PROPERTIES_FILE

export INSTALLER_LIB=$MIF_ROOT/mif-distribution/src/main/scripts/installer/installer-lib.sh

echo "running MIF modules setup scripts against the mif.properties file"

for module in "${modules[@]}"; do
	PLUGIN_ID=$module
	PLUGIN_INSTALL_DIR=$MIF_ROOT/$module/$TARGET_CLASS_DIR/runtime
	SETUP_SCRIPT_DIR=$PLUGIN_INSTALL_DIR/../install
	setupScript=./setup.sh
	if [[ ! -d $PLUGIN_INSTALL_DIR ]]; then
		echo "$PLUGIN_INSTALL_DIR does not exist, you must run mvn build first on all MIF modules"
		exit 100
	fi
	if [[ -d $SETUP_SCRIPT_DIR && -f $SETUP_SCRIPT_DIR/$setupScript ]]; then
		OLDPWD=$(pwd)
		cd $SETUP_SCRIPT_DIR
		echo "Executing $setupScript"
		$(export MIF_CONF_DIR; export PLUGIN_INSTALL_DIR; export MIF_PROPERTIES_FILE; export PLUGIN_ID; ./$setupScript)
		if (( $? != 0 )); then
			echo "Error when running plugin installer"
			exit 100
		fi
		cd $OLDPWD
	else
		echo "Skipping $PLUGIN_ID setup script because it does not exist $SETUP_SCRIPT_DIR"
	fi
done

echo "Creating setappenv.sh script"
echo -e "MIF_CONNECTORS_LOCATION=$MIF_INSTALL_DIR/etc/plugins\n" > "$MIF_CONF_DIR/etc/setappenv.sh"

#START http://jira.mango.local/browse/MIFDEV-502
MIF_OLD_LOG_DIR=/var/log/mif
MIF_OLD_WORKING_DIR=/usr/share/mif

if [[ -e "$MIF_OLD_WORKING_DIR" ]]; then
        rm -r "$MIF_OLD_WORKING_DIR"
fi

if [[ -e "$MIF_OLD_LOG_DIR" ]]; then
        rm -r "$MIF_OLD_LOG_DIR"
fi

if [[ 'root' == $(whoami) ]]; then
	ln -s "$MIF_INSTALL_DIR/metadata" "$MIF_OLD_WORKING_DIR"
	ln -s "$MIF_INSTALL_DIR/log" "$MIF_OLD_LOG_DIR"
else
	echo "WARNING!!!!"
	echo "please log in as root and execute the following commands:"
	echo "rm -r \"$MIF_OLD_WORKING_DIR\""
	echo "rm -r \"$MIF_OLD_LOG_DIR\""
	echo "ln -s \"$MIF_INSTALL_DIR/metadata\" \"$MIF_OLD_WORKING_DIR\""
	echo "ln -s \"$MIF_INSTALL_DIR/log\" \"$MIF_OLD_LOG_DIR\""
fi
