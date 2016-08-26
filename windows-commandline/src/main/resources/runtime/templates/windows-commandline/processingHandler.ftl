@echo off


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


rem  Execute the command.
"${job.commandExecutionTarget.toolExecutablePath}" ${job.executionRequest.executionParameters!} 1> "${job.jobDirectory}\.MIF\MIF.stdout" 2> "${job.jobDirectory}\.MIF\MIF.stderr"


exit %ERRORLEVEL%
