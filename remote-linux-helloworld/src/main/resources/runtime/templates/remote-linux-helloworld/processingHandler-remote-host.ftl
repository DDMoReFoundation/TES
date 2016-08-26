#!/bin/bash

<#-- Processing Handler Freemarker Template.
     The Spring configuration is configured such that this template is resolved locally but executed remotely (via JSCH, incorporated within MIF).
     The script is specific to a Linux host (it might also run successfully on a UNIX host). -->


# The job working directory needs to be accessible from both client and server
JOB_DIRECTORY="${job.remoteJobDirectory}"


echo "Job Directory = ${job.jobDirectory}"
echo "Remote Job Directory = ${job.remoteJobDirectory}"
echo "Remote Job MIF Hidden Directory = ${job.remoteJobMifHiddenDir}"

#  Request Attributes from the Execution Request are also available, but in general shouldn't be used directly
echo "Execution Host Fileshare = ${job.executionRequest.requestAttributes.get("EXECUTION_HOST_FILESHARE")}"
echo "Execution Host Fileshare Remote = ${job.executionRequest.requestAttributes.get("EXECUTION_HOST_FILESHARE_REMOTE")}"


HIDDEN_DIR="${job.remoteJobMifHiddenDir}"


OUTPUT_FILE="$JOB_DIRECTORY/helloWorld.out"


MIF_TIMESTAMP_FILE="$HIDDEN_DIR/.TIMESTAMP"
MIF_LIST_FILE="$HIDDEN_DIR/MIF.list"

if ! touch "$MIF_TIMESTAMP_FILE"; then
    echo "$0: failed to create $MIF_TIMESTAMP_FILE file" >&2
    exit 3
fi
# We need to sleep for 1 second here because some versions of find can't cope with
# files less than a second newer.
sleep 1



#  Do some dummy work.
echo 'Hello World!' | tee -a "$OUTPUT_FILE"
"${job.commandExecutionTarget.environmentSetupScript}" | tee -a "$OUTPUT_FILE"
"${job.commandExecutionTarget.toolExecutablePath}" | tee -a "$OUTPUT_FILE"
res=$?


# Determine the files that have been modified as part of execution.
cd "$JOB_DIRECTORY"
if ! find . -newer "$MIF_TIMESTAMP_FILE" -type f -print >"$MIF_LIST_FILE"; then
    exit $?
fi


exit $res
