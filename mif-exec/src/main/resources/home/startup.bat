@rem ***************************************************************************
@rem Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
@rem
@rem This program is free software: you can redistribute it and/or modify it under
@rem the terms of the GNU Affero General Public License as published by the
@rem Free Software Foundation, version 3.
@rem
@rem This program is distributed in the hope that it will be useful,
@rem but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
@rem or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
@rem for more details.
@rem
@rem You should have received a copy of the GNU Affero General Public License along
@rem with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
@rem ***************************************************************************
@echo off

REM  Locations without trailing '\'
SET SERVICE_HOME=%~dp0
IF %SERVICE_HOME:~-1%==\ SET SERVICE_HOME=%SERVICE_HOME:~0,-1%

CD "%SERVICE_HOME%"

SET MIF_CONNECTORS_LOCATION=%SERVICE_HOME%\connectors
SET MIF_HOME=%SERVICE_HOME%\mif
SET MIF_CONF=%MIF_HOME%\etc
SET MIF_WD=%MIF_HOME%\metadata
SET MIF_TEMPLATES=%SERVICE_HOME%\templates
SET MIF_COMMON_SCRIPTS=%SERVICE_HOME%\scripts
SET MIF_GEN_SCRIPTS=%SERVICE_HOME%\scripts

SET MIF_ENC_KEY=file:/%MIF_CONF%/MIF-encryption-key-default.key

SET params= %MIF_CONNECTORS_ENV_PARAMS% ^
 -Dmif.configuration.dir="%MIF_CONF%" -Dmif.working.dir="%MIF_WD%" -DMIF_HOME="%MIF_HOME%" -Dmif.encryption.key="%MIF_ENC_KEY%" ^
 -Dmif.templatesDirectory="%MIF_TEMPLATES%" -Dmif.commonScriptsDirectory="%MIF_COMMON_SCRIPTS%" -Dmif.genericScriptsDirectory="%MIF_GEN_SCRIPTS%" -Dmif.connectors.location="%MIF_CONNECTORS_LOCATION%" ^
 -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file=%SERVICE_HOME%/.extract/conf/logging.properties
REM  If MIF is running standalone e.g. on a server then the Connectors will need to be provided
REM  with the (generally absolute) paths to their respective executables. This is achieved by
REM  adding the appropriate parameters to the list above. The parameter names follow the convention
REM  "<connector-name>.executable". Therefore to set the path to the NONMEM Connector's executable,
REM  setting the parameter would be done via e.g.: -Dnonmem.executable="C:\path\to\NONMEM\nm_7.2.0_g\run\nmfe72.bat"
REM  The converter.toolbox.executable property will also need to be set in this manner.

java.exe %JAVA_OPTS% -jar "%SERVICE_HOME%"\MIFServer-executable.jar %params% -httpPort=9000

EXIT
