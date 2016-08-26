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

SERVICE_HOME=$(pwd)

export MIF_CONNECTORS_LOCATION=$SERVICE_HOME/connectors
export MIF_HOME=$SERVICE_HOME/mif

MIF_CONF=$MIF_HOME/etc
MIF_WD=$MIF_HOME/metadata
MIF_TEMPLATES=$SERVICE_HOME/templates
MIF_COMMON_SCRIPTS=$SERVICE_HOME/scripts
MIF_GEN_SCRIPTS=$SERVICE_HOME/scripts

MIF_ENC_KEY=file:$MIF_CONF/MIF-encryption-key-default.key

params=" -D\"mif.configuration.dir=$MIF_CONF\" -D\"mif.working.dir=$MIF_WD\" -D\"MIF_HOME=$MIF_HOME\" \
 -D\"mif.connectors.location=$MIF_CONNECTORS_LOCATION\" \
 -D\"mif.templatesDirectory=$MIF_TEMPLATES\" -D\"mif.commonScriptsDirectory=$MIF_COMMON_SCRIPTS\" -D\"mif.genericScriptsDirectory=$MIF_GEN_SCRIPTS\" \
 -D\"mif.encryption.key=$MIF_ENC_KEY\" ";
#  If MIF is running standalone e.g. on a server then the Connectors will need to be provided
#  with the (generally absolute) paths to their respective executables. This is achieved by
#  adding the appropriate parameters to the list above. The parameter names follow the convention
#  "<connector-name>.executable". Therefore to set the path to the NONMEM Connector's executable,
#  setting the parameter would be done via e.g.: -D\"nonmem.executable=/path/to/NONMEM/nm_7.2.0_g/run/nmfe72.sh\"
#  The converter.toolbox.executable property will also need to be set in this manner.

# Windows parameters - may or may not be required for Linux :-
#  -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file=%SERVICE_HOME%/.extract/conf/logging.properties

echo Starting up MIF with parameters: $params

java -jar ./MIFServer-executable.jar $params -httpPort=9000
