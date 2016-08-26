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
currentDir=$(pwd)
#Directory holding development environment working files
export DEV_DIR="$MIF_ROOT/dev"
#Location of MIF install dir
export MIF_INSTALL_DIR="/opt/mango/mif"
#Location of MIF Development setup install dir
export MIF_DEV_DIR="$DEV_DIR/mif-dev"
#Location of MIF Production setup install dir
export MIF_PROD_DIR="$DEV_DIR/mif-prod"
#location of developer's utilities 
export DEV_UTILS_DIR="$MIF_ROOT/dev-utils"
#Location of setup files used by mif-installer
export MIF_DEV_INSTALLER_SETUP=$DEV_DIR/dev-installer-setup

# Tomcat home
export TOMCAT_HOME=/opt/mango/tomcat

#MIF modules
export modules=("remotehost-commons" "remotehost-matlab" "remotehost-sas" "gridengine" "gridengine-r" "gridengine-nonmem" "gridengine-psnbootstrap" "gridengine-psnvpc" "gridengine-psnscm" "helloworld")

#where the compiled classes are held
export TARGET_CLASS_DIR="target/classes"

#
# Functions
#

#
# set the property with the specified name to the specified value in the specified file
#
function setProperty {
    local name="$1"
    local value="$2"
    local file="$3"

    if [[ ! -f $file ]]; then
        echo  "Error: Can't write property $name to $file, No such file"
        return 1
    fi
    sed -i "/^\s*$name=/d" $file
    #append a new line if one does not exist
    x=$(tail -c 1 $file)
    if [ "$x" != "" ]; then
        echo >> $file
    fi

    echo "$name=$value" >> $file
}

#
# Returns given property value from a file
#
function getProperty {
    local name="$1"
    local file="$2"
    
    if [[ ! -f $file ]]; then
        echo "Error: Can't retrieve property $name from $file. No such file"
	return 1
    fi
    value=$(cat $file | sed -n "s#^\s*$name=\s*\(.*\)#\1#p" | tr -d '"')
    echo $value
}
#
# copies property with a given name from a given source file to a destingation property file. If fourth parameter is specified, the property will be copied under different name
#
function copyProperty {
    local property="$1"
    local fromFile="$2"
    local toFile="$3"
    local toProperty="$4"
    if [[ -z $toProperty ]]; then
	toProperty="$property"
    fi
    value=$(getProperty $property "$fromFile")
    setProperty "$toProperty" $value "$toFile"
}

#
# Safety net - Validation
#
if [[ ! -d $TOMCAT_HOME ]]; then
	echo "$TOMCAT_HOME does not exist"
	exit 1
fi

if [[ ! -d $MIF_ROOT ]]; then
	echo "$MIF_ROOT (MIF_ROOT variable) directory does not exist"
	exit 1
fi
