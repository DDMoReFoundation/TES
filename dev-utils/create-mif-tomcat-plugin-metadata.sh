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
# Script creates MIF application context.xml additional content that is to be used in Eclipse for deploying MIF directory from workspace
#

export MIF_ROOT="$(cd $(dirname $0); cd ..; /bin/pwd)"
source $MIF_ROOT/dev-utils/commons.sh

CONTEXT_FILE=$DEV_DIR/mif-server-context-file.xml

echo '<Loader className="org.apache.catalina.loader.VirtualWebappLoader"' > $CONTEXT_FILE
echo ' virtualClasspath="' >> $CONTEXT_FILE
echo "$MIF_ROOT/MIFServer/target/MIFServer/;" >> $CONTEXT_FILE
for module in "${modules[@]}"; do
        echo "$MIF_ROOT/${module}/target/${module}.jar;" >> $CONTEXT_FILE
done

echo '"/>' >> $CONTEXT_FILE
echo '<!-- currently all modules are added to MIF webapp classpath as jars, if add mif-modules "target/classes" directory instead you will not need to repackage the module each time you want to redeploy MIF from Eclipse IDE. But you will need to ensure that all extra dependencies of given module (e.g. Mango Pharma jar in case of gridengine-nonmem module) is listed here as well -->' >> $CONTEXT_FILE 
echo '<JarScanner scanAllDirectories="true"/>' >> $CONTEXT_FILE


function urlencode { 
	python -c 'import urllib; import sys; sys.stdout.write(urllib.quote(sys.stdin.read(),safe=""))' 
}

cat $CONTEXT_FILE | urlencode > $CONTEXT_FILE.encoded


TOMCAT_PLUGIN_METADATA_FILE=$MIF_ROOT/MIFServer/.tomcatplugin.gen

echo '<?xml version="1.0" encoding="UTF-8"?>
<tomcatProjectProperties>
    <rootDir>/target/MIFServer</rootDir>
    <exportSource>false</exportSource>
    <reloadable>true</reloadable>
    <redirectLogger>false</redirectLogger>
    <updateXml>true</updateXml>
    <warLocation></warLocation>
    <extraInfo>' > $TOMCAT_PLUGIN_METADATA_FILE
cat $CONTEXT_FILE.encoded >> $TOMCAT_PLUGIN_METADATA_FILE
echo '</extraInfo>
    <webPath>/MIFServer</webPath>
</tomcatProjectProperties>' >> $TOMCAT_PLUGIN_METADATA_FILE

echo "Eclipse Tomcat Plugin metadata created in $TOMCAT_PLUGIN_METADATA_FILE"
echo "once you have set the MIFServer project as 'tomcat project' copy the contents of $TOMCAT_PLUGIN_METADATA_FILE to $MIF_ROOT/MIFServer/.tomcatplugin"
