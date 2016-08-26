#!/bin/bash

<#-- Retrieve Handler Freemarker Template -->


#  Utility scripts are available to be called.
echo Generic utility shell scripts directory = ${GENERIC_UTILS}
echo Mango utility shell scripts directory = ${MANGO_UTILS}
export PATH="${MANGO_UTILS}:${GENERIC_UTILS}:$PATH"


HIDDENDIR="${job.jobDirectory}/.MIF"


source "mango_initialise.sh" "hello-world-connector/retrieveHandler.ftl" ${job.jobId}
logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "START SCRIPT"


MIF_LIST="$HIDDENDIR/MIF.list"
TARGET_HIDDEN_DIR="${job.jobMifHiddenDir}"
SOURCE="${job.jobDirectory}"
TARGET="${job.jobDirectory}"

if [[ ! -f $MIF_LIST ]]; then
    echo "hello-world-connector/retrieveHandler.ftl: FATAL ERROR: Cannot locate MIF.list file" >&2
    exit 1
fi

# We need to switch this off, otherwise the grep below can cause the script to bomb out if no matching
# pattern is found
set +e


logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "START IGNORE"

if [[ -n "$FILE_IGNORE_PATTERN" || -n "$DIR_IGNORE_PATTERN" ]]; then
    log "retrieveHandler.ftl: File ignore pattern: $FILE_IGNORE_PATTERN"
    log "retrieveHandler.ftl: Dir ignore pattern: $DIR_IGNORE_PATTERN"
    originalCount=$(cat $MIF_LIST | wc -l)
    log "retrieveHandler.ftl: Original list of files has $originalCount entries"
    ignorePattern=$(constructIgnorePattern "" "$FILE_IGNORE_PATTERN", "$DIR_IGNORE_PATTERN")
    grep -E -v "$ignorePattern" $MIF_LIST > $MIF_LIST.1
    newCount=$(cat $MIF_LIST.1 | wc -l)
    log "retrieveHandler.ftl: Modified list now has $newCount entries (original $originalCount)"
    mv $MIF_LIST.1 $MIF_LIST
else
    log "retrieveHandler.ftl: Have neither FILE_IGNORE_PATTERN nor DIR_IGNORE_PATTERN"
fi

logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "END IGNORE"

set -e


logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "START RETRIEVE"

#  Do the file-retrieval.

CFLAG="-c"

cat "$MIF_LIST" | grep -v 'MIF.list' | retrieveHandler.sh -h ".MIF" \
    -e ".MIF" -s "$SOURCE" -l "${mifModifiedListFileName}" \
    -d "$TARGET" $CFLAG

logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "END RETRIEVE"


logMetrics "${job.jobId}" "hello-world-connector/retrieveHandler.ftl" "END SCRIPT"
