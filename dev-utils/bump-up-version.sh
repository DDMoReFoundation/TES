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
# Very basic script that updates versions of pom files
#
# This should be replaced by maven-releaes plugin once it is used
#

export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh

if [[ -z $1 ]]; then
	echo "old pom version was not supplied"
	exit 1
fi

if [[ -z $2 ]]; then
	echo "new pom version was not supplied"
	exit 2
fi

cd $MIF_ROOT

poms=$(find . -name pom.xml)



for pom in $poms
do
	echo "updating $pom"
	sed -i "s/$1/$2/g" $pom
done

sed -i "s/$1/$2/g" $DEV_UTILS_DIR/build-and-deploy.sh 

cd $currentDir

