#!/bin/bash

<#-- Prepare Handler Freemarker Template -->


hiddenDir="${job.jobDirectory}/.MIF"
if [[ ! -d $hiddenDir ]]; then
    if ! mkdir -p "$hiddenDir"; then
        echo "$0: FATAL ERROR: Failed to create hidden directory $hiddenDir"
        exit 31
    fi
fi

exit 0
