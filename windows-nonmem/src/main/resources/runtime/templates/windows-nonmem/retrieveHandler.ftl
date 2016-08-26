@echo off


rem  The setup-NONMEM batch file also sets up paths to Perl which is what we need here
set PREAMBLE="${job.commandExecutionTarget.environmentSetupScript}"
for %%F in (%PREAMBLE%) do set PREAMBLE_DIR=%%~dpF
cd "%PREAMBLE_DIR%"

CALL %PREAMBLE%


set WORK_DIR="${job.jobWorkingDirectory}"
for %%D in (%WORK_DIR%) do set WORK_DIR_DRIVE=%%~dD
echo Job Working Directory = %WORK_DIR%
echo Job Working Directory Drive = %WORK_DIR_DRIVE%
cd %WORK_DIR%
%WORK_DIR_DRIVE%

REM Handling a PharmML and Nonmem result filename conflict
SET EXECUTION_FILE="${job.executionFile}"
SET EXECUTION_FILE=%EXECUTION_FILE:/=\%
IF EXIST "${job.jobDirectory}\%EXECUTION_FILE%.pml" (
    COPY "${job.jobDirectory}\%EXECUTION_FILE%" "${job.jobDirectory}\%EXECUTION_FILE%.bak"
    MOVE "${job.jobDirectory}\%EXECUTION_FILE%.pml" "${job.jobDirectory}\%EXECUTION_FILE%"
)

<#assign outputFile = (job.executionFileName)?replace("\\.[A-Za-z0-9]{1,3}$", ".lst", "r")>


perl "${job.commandExecutionTarget.customScriptsDirectory}\nmoutput2so\bin\nmoutput2so" "${outputFile}" ^
    1>>"${job.jobOutputStreamFile}" 2>>"${job.jobErrorStreamFile}"

