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

PASSWORD=$1

if [[ -z $PASSWORD ]]; then
	echo "ERROR: password is empty"
	exit 100
fi

export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh

set -e

cd $MIF_ROOT
cd MIFClient
mvn -DskipTests=true clean install

cd target

echo "Unzipping MIClient-binary"
unzip -qq MIFClient-binary.zip -d "./MIFClient-binary"
cd "./MIFClient-binary"

chmod a+x DesEncrypter.sh

echo "Encrypting password"
encrypterOutput=$(./DesEncrypter.sh encryptMessage "$PASSWORD")

if echo -e "$encrypterOutput" | grep -qi "Encrypted message:"; then
   MIF_CLIENT_PASSWORD_ENCRYPTED=$(echo -e "$encrypterOutput" | tail -1)
   echo $MIF_CLIENT_PASSWORD_ENCRYPTED
   exit 0
fi

echo "Error, could not encrypt password"
exit 1
