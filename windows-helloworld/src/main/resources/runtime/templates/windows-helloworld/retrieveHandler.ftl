
<#-- Retrieve Handler Freemarker Template -->


rem  Utility scripts are available to be called.
echo Generic utility shell scripts directory = ${GENERIC_UTILS}
echo Mango utility shell scripts directory = ${MANGO_UTILS}


set HIDDENDIR=${job.jobDirectory}\.MIF


rem  logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "START SCRIPT"


set MIF_LIST=%HIDDENDIR%\MIF.list
set TARGET_HIDDEN_DIR=${job.jobMifHiddenDir}
set SOURCE=${job.jobDirectory}
set TARGET=${job.jobDirectory}\output


rem  logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "START RETRIEVE"

rem  Do some dummy file-retrieval.

mkdir "%TARGET%"
xcopy "%SOURCE%" "%TARGET%"

rem  logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "END RETRIEVE"


rem  logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "END SCRIPT"
