
<#-- Processing Handler Freemarker Template -->


rem  Utility scripts are available to be called.
echo Generic utility shell scripts directory = ${GENERIC_UTILS}
echo Mango utility shell scripts directory = ${MANGO_UTILS}


set HIDDENDIR=${job.jobDirectory}\.MIF


rem  Connector-specific Custom Scripts Directory is a parameter on the Command Execution Target.
set CUSTOM_SCRIPTS=${job.commandExecutionTarget.customScriptsDirectory}
echo Custom scripts directory = %CUSTOM_SCRIPTS%


rem  logMetrics "${job.jobId}" "hello-world-connector/processingHandler.ftl" "START SCRIPT"


rem  Do environment preparation.
${job.commandExecutionTarget.environmentSetupScript} >"${job.jobDirectory}\helloWorld.out"


rem  Request Attributes from the Execution Request are available.
echo Execution Host Fileshare = ${job.executionRequest.requestAttributes.get("EXECUTION_HOST_FILESHARE")}


set MIF_TIMESTAMP_FILE=%HIDDENDIR%\.TIMESTAMP
set MIF_LIST_FILE=%HIDDENDIR%\MIF.list

echo. > "%MIF_TIMESTAMP_FILE%"


rem  We can invoke a custom script delivered as part of the Connector.
CALL "%CUSTOM_SCRIPTS%\customscript.bat" >>"${job.jobDirectory}\helloWorld.out"


rem  Do some dummy work.
echo Hello World!>>"${job.jobDirectory}\helloWorld.out"
set res=%ERRORLEVEL%


rem  logMetrics "${job.jobId}" "hello-world-connector/processingHandler.ftl" "END SCRIPT"

exit %res%

