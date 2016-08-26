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
#!/bin/sh

JAVA_OPTS=" -Dlog4j.configuration=file:$(pwd)/lib/log4j.properties -cp ./*:./lib/* com.mango.mif.utils.encrypt.DesEncrypterCLI "

options=()
options+=("$1")
options+=("$2")

if [ ! -z $3 ]; then
    options+=("$3")
fi

echo "$JAVA_OPTS"
echo "$PROGRAM_OPTS"
java $JAVA_OPTS "${options[@]}"
