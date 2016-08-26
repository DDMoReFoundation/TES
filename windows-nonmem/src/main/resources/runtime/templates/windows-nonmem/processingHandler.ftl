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


<#assign executionFile = job.executionFileName>
<#if (job.executionFileName.endsWith(".xml")) >
    <#assign executionFile = job.executionFileName.replace(".xml",".ctl")>
</#if>

<#assign outputFile = (job.executionFileName)?replace("\\.[A-Za-z0-9]{1,3}$", ".lst", "r")>


echo STDOUT = ${job.jobOutputStreamFile}
echo STDERR = ${job.jobErrorStreamFile}

<#if (job.executionRequest.executionParameters)?? && (job.executionRequest.executionParameters)!="">
    <#assign extraParams = job.executionRequest.executionParameters >
<#else>
    <#assign extraParams = "-tprdefault">
</#if>


rem  Command to execute NONMEM.
CALL "${job.commandExecutionTarget.toolExecutablePath}" "${executionFile}" "${outputFile}" ${extraParams} ^
    1>>"${job.jobOutputStreamFile}" 2>>"${job.jobErrorStreamFile}"


IF EXIST "${outputFile}" (
    exit 0
) else (
    exit 1
)

