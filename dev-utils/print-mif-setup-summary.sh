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
echo "Setup"
echo "###############################################"
$DEV_UTILS_DIR/which-mif-conf-dir-i-use.sh

echo "MIF ROOT $MIF_ROOT"

echo "###############################################"
echo "mif.properties"
echo "<start>"
cat $MIF_INSTALL_DIR/etc/mif.properties
echo "<end>"

echo "###############################################"
echo "MIF plugins"
echo "<start>"
ls $MIF_INSTALL_DIR/etc/plugins
echo "<end>"

