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

VM_OWNER="$1"

if [[ -z $VM_OWNER ]]; then
	echo "Fail, VM owner was not specified by the first parameter"
	exit 100
fi

# Create the mif database
#
cat <<EOF | mysql -u root
DROP DATABASE IF EXISTS mif;
CREATE DATABASE mif DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_bin;
GRANT ALL ON mif.* TO 'mif'@'localhost' WITH GRANT OPTION;
GRANT ALL on mif.* to '$VM_OWNER'@'localhost' IDENTIFIED BY '$VM_OWNER';
FLUSH PRIVILEGES;
EOF

