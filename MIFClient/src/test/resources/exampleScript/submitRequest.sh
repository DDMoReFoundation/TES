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
#
# Submits a request to MIF instance. Update submitRequest.properties file with MIF instance details and user credentials. 
#

dataDir=$1
scriptFile=$2
executionType=$3
executable=$4

java -Dlog4j.configuration="file:$(pwd)/lib/log4j.properties"  -Dmif.invoker.properties="$(pwd)/submitRequest.properties" -cp ./*:./lib/* com.mango.mif.client.cli.MIFInvoker $dataDir $scriptFile $executionType $executable
