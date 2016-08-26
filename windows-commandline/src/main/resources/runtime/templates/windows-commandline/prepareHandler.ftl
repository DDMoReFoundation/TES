md "${job.jobDirectory}\\.MIF"

<#if (job.executionFileName.endsWith(".xml")) >
CALL "${job.commandExecutionTarget.converterToolboxPath}" "${job.jobDirectory}/${job.executionFile}" "${job.jobWorkingDirectory}" PharmML 0.3.0 NMTRAN 7.2.0
</#if>

exit /b %ERRORLEVEL%
