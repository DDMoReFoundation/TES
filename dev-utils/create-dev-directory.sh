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
# this script creates initial directory structure for holding development directories 
#
export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh

if [[ -d $DEV_DIR ]]; then
	echo "$DEV_DIR exists, cleaning it up"
	rm -r $DEV_DIR
fi 

echo "Creating $DEV_DIR"
mkdir $DEV_DIR
echo "Creating $MIF_DEV_DIR"
mkdir $MIF_DEV_DIR
echo "Creating $MIF_PROD_DIR"
mkdir $MIF_PROD_DIR

if [[ -d $MIF_INSTALL_DIR && ! -L $MIF_INSTALL_DIR ]]; then
	echo "WARNINIG!!!!!"
	echo "$MIF_INSTALL_DIR already exists"
	echo "You will need to remove/rename it manually"
	echo "And then run $DEV_UTILS_DIR/switch-mif-conf-dir-to.sh \"prod\""
else 
	$DEV_UTILS_DIR/switch-mif-conf-dir-to.sh "prod"
fi
