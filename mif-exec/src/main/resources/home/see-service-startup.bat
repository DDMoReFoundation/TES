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

CALL "%SERVICE_HOME%\startup.bat"

EXIT
