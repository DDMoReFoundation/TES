#!${SHELL}

<#--
  Retrieve Handler Freemarker Template

  SHELL                      preferred execution shell
  REQUEST_ATTRIBUTE_BLOCK    variables from the execution request attributes from the navigator execution profile
  MANGO_UTILS                directory containing the mango utility shell scripts
  CONNECTOR_UTILS            directory containing the publishHandler.sh script, which is connector specific
  gridMifHiddenDirectoryName - (DEPRECATED - use mifHiddenDirectoryName name instead.) Name of the hidden directory
  mifModifiedListFileName    name of the file (not an absolute path) of the file which will contain a list of files copied by us
  job                        the (now dead) job
-->

export PATH="${CONNECTOR_UTILS}:${MANGO_UTILS}:$PATH"

SGE_INITIALISATION_SCRIPT_PATH=${SGE_INITIALISATION_SCRIPT_PATH!}; export SGE_INITIALISATION_SCRIPT_PATH
# REQUEST_ATTRIBUTE_BLOCK
${REQUEST_ATTRIBUTE_BLOCK}

source "mango_initialise.sh" "generic/retrieveHandler.ftl" ${job.getJobId()}
logMetrics "${job.getJobId()}" "generic/retrieveHandler.ftl" "START SCRIPT"

GRID_MODIFIED_LIST=${job.getJobMifHiddenDir()}/${gridModifiedListFileName}

if [[ ! -f $GRID_MODIFIED_LIST ]]; then
    echo "generic/retrieveHandler.ftl: FATAL ERROR: $GRID_MODIFIED_LIST: No such file" >&2
    exit 1
fi

# We need to switch this off, otherwise the grep below can cause the script to bomb out if no matching
# pattern is found
set +e

logMetrics "${job.getJobId()}" "generic/retrieveHandler.ftl" "START IGNORE"

# Apply an accept or ignore pattern with lots of debugging
#
function applyPattern {
    local type="$1"; shift
    local file_pattern="$1"; shift
    local dir_pattern="$1"; shift
    local list_file="$1"; shift
    local grep_flags="$1"; shift
    local pattern
    local originalCount
    local newCount

    log "type=$type"
    log "file_pattern=$file_pattern"
    log "dir_pattern=$dir_pattern"
    log "list_file=$list_file"
    log "grep_flags=$grep_flags"

    if [[ -n "$file_pattern" || -n "$dir_pattern" ]]; then
        log "retrieveHandler.ftl: File $type pattern: $file_pattern"
        log "retrieveHandler.ftl: Dir $type pattern: $dir_pattern"
        originalCount=$(cat $list_file | wc -l)
        log "retrieveHandler.ftl: $originalCount entries in list $list_file:\n$(cat $list_file)"
        pattern=$(constructIgnorePattern "" "$file_pattern" "$dir_pattern")
        log "constructed pattern: $pattern"
        log "grep \"$grep_flags\" -E \"$pattern\" $list_file > $list_file.1"
        grep $grep_flags -E "$pattern" $list_file > $list_file.1
        newCount=$(cat $list_file.1 | wc -l)
        log "retrieveHandler.ftl: $type modified list now has $newCount entries (original $originalCount):\n$(cat $list_file.1)"
        mv $list_file.1 $list_file
    fi
}

# Apply an accept pattern
#
function applyAcceptPattern {
    local file_pattern="$1"; shift
    local dir_pattern="$1"; shift
    local list_file="$1"; shift
    
    applyPattern "accept" "$file_pattern" "$dir_pattern" "$list_file" ""
}

# Apply an ignore pattern
#
function applyIgnorePattern {
    local file_pattern="$1"; shift
    local dir_pattern="$1"; shift
    local list_file="$1"; shift
    
    applyPattern "ignore" "$file_pattern" "$dir_pattern" "$list_file" "-v"
}

applyAcceptPattern "$FILE_ACCEPT_PATTERN" "$DIR_ACCEPT_PATTERN" "$GRID_MODIFIED_LIST"
applyIgnorePattern "$FILE_IGNORE_PATTERN" "$DIR_IGNORE_PATTERN" "$GRID_MODIFIED_LIST"

logMetrics "${job.getJobId()}" "generic/retrieveHandler.ftl" "END IGNORE"
logMetrics "${job.getJobId()}" "generic/retrieveHandler.ftl" "START RETRIEVE"

CFLAG="-c"

cat "$GRID_MODIFIED_LIST" | grep -v '${mifModifiedListFileName}' | retrieveHandler.sh -h "${mifHiddenDirectoryName}" \
     -e "${gridMifHiddenDirectoryName}" -s ${job.getJobDirectory()} -l "${mifModifiedListFileName}" \
     -d ${job.getJobDirectory()} $CFLAG


logMetrics "${job.getJobId()}" "generic/retrieveHandler.ftl" "END RETRIEVE"
logMetrics "${job.getJobId()}" "generic/retrieveHandler.ftl" "END SCRIPT"
