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

mifDevDirName=$(basename $MIF_DEV_DIR)
resolved=$(cd $MIF_INSTALL_DIR; pwd -P)

mifConfDirName=$(basename $resolved)

if [[ $mifConfDirName == $mifDevDirName ]]; then
	echo "You are running development setup"
	echo "MIF installation dir $resolved"
else
	echo "You are running production setup"
	echo "MIF installation dir $resolved"
fi


