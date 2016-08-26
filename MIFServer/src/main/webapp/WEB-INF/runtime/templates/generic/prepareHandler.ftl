#!${SHELL}

<#--
  Prepare Handler Freemarker Template

  SHELL                   preferred execution shell
  REQUEST_ATTRIBUTE_BLOCK variables from the execution request attributes all the way from Navigator's execution profile
  MANGO_UTILS             the directory containing the mango utility shell scripts
  CONNECTOR_UTILS         the directory containing the publishHandler.sh script, which is connector specific
  job                     the current job
-->

export PATH="${CONNECTOR_UTILS}:${MANGO_UTILS}:$PATH"

SGE_INITIALISATION_SCRIPT_PATH=${SGE_INITIALISATION_SCRIPT_PATH!}; export SGE_INITIALISATION_SCRIPT_PATH
# REQUEST_ATTRIBUTE_BLOCK
${REQUEST_ATTRIBUTE_BLOCK}

source "mango_initialise.sh" "generic/prepareHandler.ftl" ${job.getJobId()}
logMetrics "${job.getJobId()}" "generic/prepareHandler.ftl" "START SCRIPT"


hiddenDir="${job.getJobMifHiddenDir()}"

if [[ ! -d $hiddenDir ]]; then
    log "$0: mkdir -p $hiddenDir"
    if ! mkdir -p "$hiddenDir"; then
        logErrorAndExit 31 "$0: FATAL ERROR: Failed to create hidden directory $hiddenDir"
    fi
fi

logMetrics "${job.getJobId()}" "generic/prepareHandler.ftl" "END SCRIPT"

exit 0
