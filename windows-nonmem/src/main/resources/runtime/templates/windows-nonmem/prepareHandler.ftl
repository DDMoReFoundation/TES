@echo off

md "${job.jobDirectory}\\.MIF"

<#if (job.executionFileName.endsWith(".xml")) >
REM Handling a PharmML and Nonmem result filename conflict
SET EXECUTION_FILE="${job.executionFile}"
SET EXECUTION_FILE=%EXECUTION_FILE:/=\%
COPY "${job.jobDirectory}\%EXECUTION_FILE%" "${job.jobDirectory}\%EXECUTION_FILE%.pml"

CALL "${job.commandExecutionTarget.converterToolboxPath}" "${job.jobDirectory}/${job.executionFile}" "${job.jobWorkingDirectory}" ^
    PharmML 0.8.1 NMTRAN 7.3.0 ^
    1>>"${job.jobOutputStreamFile}" 2>>"${job.jobErrorStreamFile}"
</#if>

exit /b %ERRORLEVEL%
