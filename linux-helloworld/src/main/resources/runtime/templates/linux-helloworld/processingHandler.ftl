#!/bin/bash

<#-- Processing Handler Freemarker Template -->


#  Utility scripts are available to be called.
echo Generic utility shell scripts directory = ${GENERIC_UTILS}
echo Mango utility shell scripts directory = ${MANGO_UTILS}
export PATH="${MANGO_UTILS}:$PATH"


HIDDENDIR="${job.jobDirectory}/.MIF"


#  Connector-specific Custom Scripts Directory is a parameter on the Command Execution Target.
export CUSTOM_SCRIPTS=${job.commandExecutionTarget.customScriptsDirectory}
echo Custom scripts directory = $CUSTOM_SCRIPTS


source "mango_initialise.sh" "hello-world-connector/processingHandler.ftl" ${job.jobId}

logMetrics "${job.jobId}" "hello-world-connector/processingHandler.ftl" "START SCRIPT"


#  Do environment preparation.
${job.commandExecutionTarget.environmentSetupScript} >"${job.jobDirectory}/helloWorld.out"


#  Request Attributes from the Execution Request are available.
echo Execution Host Fileshare = ${job.executionRequest.requestAttributes.get("EXECUTION_HOST_FILESHARE")}


MIF_TIMESTAMP_FILE="$HIDDENDIR/.TIMESTAMP"
MIF_LIST_FILE="$HIDDENDIR/MIF.list"

if ! touch $MIF_TIMESTAMP_FILE; then
    echo "$0: failed to create $MIF_TIMESTAMP_FILE file" >&2
    exit 3
fi
# We need to sleep for 1 second here because some versions of find can't cope with
# files less than a second newer.
sleep 1


#  We can invoke a custom script delivered as part of the Connector.
source "$CUSTOM_SCRIPTS/customscript.sh" | tee -a "${job.jobDirectory}/helloWorld.out"


#  Do some dummy work.
echo "Hello World!" | tee -a "${job.jobDirectory}/helloWorld.out"
res=$?


# Determine the files that have been modified as part of execution.
cd "${job.jobDirectory}"
if ! find . -newer $MIF_TIMESTAMP_FILE -type f -print > $MIF_LIST_FILE; then
    exit $?
fi


logMetrics "${job.jobId}" "hello-world-connector/processingHandler.ftl" "END SCRIPT"

exit $res
