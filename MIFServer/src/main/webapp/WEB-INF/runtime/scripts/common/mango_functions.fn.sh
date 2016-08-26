#*******************************************************************************
# Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the
# Free Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License along
# with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
#*******************************************************************************

# Collected Mango functions
# This file can be sourced from whereever you like and will provide a common set of lovingly crafted
# functions for your delight
#

# Create the specified file with the specified mode, return true if successful, false otherwise
#
function create {
    local file=$1
    local mode=$2

    if [[ ! -f $file ]]; then
        if touch $file; then
            chmod $mode $file
        fi
    fi
    [[ -f $file ]]
}

# figure out where we are logging to once and for all
#
function getLogPath {
    # This function tries to validate MANGO_LOG_OUTPUT which is the location where we want
    # to log stuff.  It is supposed to contain a file name.  If it does, we have one attempt
    # at creating it, if that fails we use a default.  If the default fails, we switch off
    # debugging.
    # It is important we don't try to write messages to unwriteable log files because such
    # attempts will cause everything we try to execute to fail, especially when we have
    # "set -e" in force

    local useDefault=0

    if [[ -z "$MANGO_LOG_OUTPUT" ]]; then
        # We have not defined MANGO_LOG_OUTPUT, we want the default
        useDefault=1
    else
        if [[ ! -f "MANGO_LOG_OUTPUT" ]]; then
            # We have defined MANGO_LOG_OUTPUT, but the file which does not exist.
            if ! create "$MANGO_LOG_OUTPUT" "666"; then
                # We could not create the file, we want to use the default
                echo "Warning: could not create log file $MANGO_LOG_OUTPUT" >&2
                useDefault=1
            fi
        elif [[ ! -w "MANGO_LOG_OUTPUT" ]]; then
            # We have defined MANGO_LOG_OUTPUT, the exists but is not writeable
            # We want to use the default
            echo "Warning: log file $MANGO_LOG_OUTPUT is not writeable" >&2
            useDefault=1
        fi
    fi

    local killDebugging=0
    if (( useDefault )); then
        MANGO_LOG_OUTPUT="/var/log/mif/shell.log"
        if [[ ! -f "$MANGO_LOG_OUTPUT" ]]; then
            if ! create "$MANGO_LOG_OUTPUT" 666; then
                echo "Warning: could not create log file $MANGO_LOG_OUTPUT, disabling debugging" >&2
                killDebugging=1
            fi
        elif [[ ! -w "$MANGO_LOG_OUTPUT" ]]; then
            echo "Warning: log file $MANGO_LOG_OUTPUT is not writeable" >&2
            killDebugging=1
        fi
    fi

    if (( killDebugging )); then
        # that was your last chance
        MANGO_LOGGING=
        MANGO_LOG_OUTPUT=/dev/null
    fi

    echo $MANGO_LOG_OUTPUT
}

# The same as logging, but for the metrics instead.  Have a separate file in order to keep the
# metrics gathering as simple as possible.
#
function getMetricsLogPath {

    local useDefault=0

    # The file where the metrics go.  Currently cannot be changed.  TODO: allow this to be changed
    #
    MANGO_METRICS_FILE="/var/log/mif/shell-metrics.log"

    # Try to create the file, mode 666 so everyone can write to it
    #
    if [[ ! -f "$MANGO_METRICS_FILE" ]]; then

        if ! create "$MANGO_METRICS_FILE" "666"; then
            logError "Cannot create Mango metrics file $MANGO_METRICS_FILE"
            echo ""
            return 1
        fi
    fi

    if [[ ! -w "$MANGO_METRICS_FILE" ]]; then
        logError "Cannot write Mango metrics file $MANGO_METRICS_FILE"
        echo ""
        return 1
    fi

    echo $MANGO_METRICS_FILE
}


# Function to produce logging output.  To enable logging, set the MANGO_LOGGING environment variable
# to anything non empty.
# Logging will be written to MANGO_LOG_OUTPUT, unless this is empty in which case it will be written
# to a random file in /tmp
#
function log {
    [[ -z $MANGO_LOGGING ]] && return
    echo -e "${JOBID:-$$}: $(date +'%m-%d %H:%M:%S'): $@" >> $(getLogPath)
}


# Write directly to the log file, without the date, time, process id, mother's maiden name... 
#
function logRaw {
    [[ -z $MANGO_LOGGING ]] && return
    echo -e "$@" >> $(getLogPath)
}


# Log the flags.  Basically try to format flags nicely.  This saves trying to figure out a messy
# command line (of which we have plenty) which is just dumped to a file
#
function logArgs {
    [[ -z $MANGO_LOGGING ]] && return

    local output=
    for arg; do
       case $arg in
         -*) if [[ -n $output ]]; then
                 output="$output
    "
             fi ;;
         *)  if [[ -n $output ]]; then
                 output="$output "
             fi ;;
       esac
       output="$output$arg"
    done
    log "$output"
}

# Log the flags.  Basically try to format flags nicely.  This saves trying to figure out a messy
# command line (of which we have plenty) which is just dumped to a file
#
function logArgDetails {
    [[ -z $MANGO_LOGGING ]] && return

    local i=1
    for arg; do
        log "arg[$i]=\"$arg\""
        (( i++ ))
    done
}

# Log the contents of a file. Filename is first arg, anything else is logged as a string BEFORE the
# file contents are logged.
#
function logFile {
    [[ -z $MANGO_LOGGING ]] && return

    local file="$1"
    shift
    if (( $# > 0 )); then
       log "$@"
    fi
    if [[ -z "$file" ]]; then
       log "[[ supposed to log a file here, but there is no filename ]]"
    elif [[ -f "$file" ]]; then
       if [[ -s "$file" ]]; then
           cat "$file" >> $(getLogPath)
       else
           log "[[ $file: its EMPTY ]]"
       fi
    else
       log "[[ $file: no such file ]]"
    fi
}

# Write a message to the log and to the standard error.
#
function logError {
    echo -e "$@" >&2
    [[ -z $MANGO_LOGGING ]] && return
    log "$@"
}

# create a temp file.
#
function generateTmpFileName {
    echo $(mktemp)
}

# Write a message to the log and standard error and exit.
# The exit status is the FIRST parameter, all the remaining parameters are echoed to the logs
#
function logErrorAndExit {
     local exit_status=$1
     shift
     logError "$@"
     exit $exit_status
}

# Kill the script using the specified exit status, preserving the current standard output and
# standard error, slightly modifying the standard error with the text specified after the
# third parameter.
# 
# Params:
#  1.  The exit status you want to kill the script with
#  2.  The file containing the standard output thus far
#  3.  The file containing the standard error thus far
#  *.  Text added to the start of the standard error
#
function modifyOutputAndExit {
     local exit_status=$1
     local stdout_file=$2
     local stderr_file=$3
     shift 3
     [[ -f $stdout_file ]] && cat $stdout_file
     logError "$@\n\n"
     [[ -f $stderr_file ]] && cat $stderr_file >&2
     exit $exit_status
}

#
# counts number of occurrences of a string in a file.
# "cat file | grep pattern" obviously because "grep pattern file" gives the file name
# in the output, which we want to avoid here.
#
function countNumberOfMessages {
    if [[ -f "$1" ]]; then
        cat "$1" | grep "$2" | wc -l
    else
        echo 0
    fi
}

#
# gets file content
#
function getFileContent {
    if [[ -f "$1" ]]; then
        cat "$1"
    else
        echo ""
    fi
}

#
# Construct the ignore pattern that will be passed to the resource copier, using the
# existing value ($1), the FILE_IGNORE_PATTERN ($2) and the DIR_IGNORE_PATTERN ($3)
#
function constructIgnorePattern {
    local result="$1"

    local separator=","
    local filepatt="$2"
    local dirpatt="$3"

    if [[ -n "$filepatt" ]]; then
        filepatt="/$filepatt\$"
        filepatt=$(echo "$filepatt" | sed -e "s#$separator#$|/#g")
        if [[ -n "$result" ]]; then
            result="$result|"
        fi
        result="$result$filepatt"
    fi

    if [[ -n "$dirpatt" ]]; then
        irpatt="/$dirpatt/"
        dirpatt=$(echo "$dirpatt" | sed -e "s#$separator#/|/#g")
        if [[ -n "$result" ]]; then
            result="$result|"
        fi
        result="$result$dirpatt"
    fi

    echo "$result"
}

#
# Copy either a file or a directory structure according to parameters, taking care of
# logging and implementing the directory ignore pattern.  The "exit status" of this
# function is the exit status of the cpio command which does the copying.
# param 1: type of entry to copy: f=files, d=directories, empty=copy everything
# param 2: source directory
# param 3: target directory
# param 4: ignore pattern
# param 5: file listing file (optional)
#
function copyFileStructure {
    local type="$1"
    local sourceDir="$2"
    local targetDir="$3"
    local ignorePattern="$4"
    local filesListing="$5"
    local ex=0

    set +e

    log "$0/$FUNCNAME copying file structure from $sourceDir to $targetDir"
    logArgDetails "$@"

    local TMP=/tmp/.entries.$$
    local OLD=$(/bin/pwd)

    # If the source directory does not exist, we kill ourselves and the calling script.
    # Yes, its THAT serious.  If we don't, we stay in the user's home directory and cause
    # utter chaos.
    #
    cd "$sourceDir" || logErrorAndExit 56 "$0/$FUNCNAME: FATAL ERROR: cd \"$sourceDir\" failed, halting execution"
    trap "cd $OLD" EXIT

    # If the target directory is not writable, we kill ourselves and the calling script.
    # Yes, it too is THAT serious.  If we don't do this now, cpio tends to keep quiet about
    # errors and we end up not copying and not telling the user we have not copied.
    #
    [[ -w "$targetDir" ]] || logErrorAndExit 56 "$0/$FUNCNAME: FATAL ERROR: target directory \"$targetDir\" is not writeable, halting execution"

    if [[ -n $type ]]; then
        type="-type $type"
    fi

    # See NAVDEV-5446 and NAVDEV-5894
    #
    # A list of modified files is optionally passed in as the 5th parameter.  This was originally a
    # file full of absolute paths to the destination files. An asymetrical NFS mount at Merck caused
    # NAVDEV-5446 where the absolute paths were invalid on the GRID host, but not the WORKBENCH host.
    # The fix for that bug was to remove all reference to the file and run find to generate the list
    # of files.  That works in "double copy" mode because only modified files have been copied across
    # and the find runs on those modified files.  In "single copy" mode, no copying has yet been
    # done, so the find selects everything (including the CTL and data files, which when copied back
    # cause havoc).
    #
    # The actual fix for 5894 was to invoke this script with a handy file of relative paths which
    # we already had lying around, and use that instead of the absolute paths (each path had to be
    # modified anyway).
    #
    if [[ -f "$filesListing" ]]; then
        cat $filesListing > $TMP
    else
        find . -noleaf $type -print > $TMP
    fi

    trap "cd $OLD;rm -f $TMP" EXIT

    if [[ -s $TMP ]]; then
        log "$(basename $0)/$FUNCNAME: $(cat $TMP | wc -l) to copy"

        # If we have an ignore pattern, apply it now, whatever is left after that must be
        # what we want to copy
        #
        if [[ -n "$ignorePattern" ]]; then
            log "ignorepattern \"$ignorePattern\""
            grep -E -v "$ignorePattern" "$TMP" > $TMP.1
            mv $TMP.1 $TMP
            log "$(basename $0)/$FUNCNAME: $(cat $TMP | wc -l) files after ignorepattern"
        fi

        log "=========== files to copy (in $TMP) ================="
        log "\n$(cat $TMP)"
        log "==========================================="
        log "target directory: $targetDir"
        log "==========================================="

        if [[ -s $TMP ]]; then
            cat $TMP | cpio -pdu --quiet "$targetDir"; ex=$?

            # ID 11337: We need to ADD group write permission to the files copied back
            #           but like everything, this is configurable.
            #
            # Question:  Do we still apply the modes even if cpio failed?
            #
            applyAdditionalModes $TMP "$targetDir"
            makeShellScriptsExecutable $TMP "$targetDir"

            cat $TMP | while read file; do
                logMetrics "" "copyFileStructure" "$(stat -c "%n %s" $file)"
            done

        else
            logError "$0/$FUNCNAME: There are no files to copy after applying the ignorepattern \"$ignorepattern\""
        fi
    else
        logError "$0/$FUNCNAME: There are no files to copy"
    fi
    return $ex
}

# Apply additional modes to the files just copied, if such modes are desired.  We indicate
# the desire for such additional modes by defining the variable USER_DIR_ADDITIONAL_MODES
# which triggers them to be applied.
#
# Parameters:
#     1: a file containing the names of the files whose modes we want to change
#     2: the name of a target directory in which those files will (hopefully) reside
#
function applyAdditionalModes {

    local list=$1
    local targetDir=$2

    if [[ -n "$USER_DIR_ADDITIONAL_MODES" ]]; then

        # Don't want to bail if one of the chmods fails.
        set +e

        if [[ ! -f "$list" ]]; then
            logError "$0/$FUNCNAME: No such file \"$list\""
            return 1
        fi

        if [[ ! -d "$targetDir" ]]; then
            logError "$0/$FUNCNAME: Target directory \"$targetDir\" does not exist"
            return 1
        fi
        
        # Knock out the execute bit.  We will add it later if appropriate.
        #
        local mode=$(echo "$USER_DIR_ADDITIONAL_MODES" | sed -e 's/x//g')

        cat $list | while read entry; do

            log "checking mode of file $targetDir/$entry"

            if [[ -f "$targetDir/$entry" ]]; then
                chmod $mode "$targetDir/$entry"
            elif [[ -d "$targetDir/$entry" ]]; then
                chmod ${mode}x "$targetDir/$entry"
            else
                logError "$0/$FUNCNAME: Attempted to apply additional modes to non existent entry $targetDir/$entry"
            fi
        done
    else
        log "$0/$FUNCNAME: bypassing setting additional modes, as there are none to set"
    fi
}
# Use the variable USER_SHELL_SCRIPT_EXTENSIONS (set via request attributes) to filter the list of file passed in by extension
# to discover shell scripts which are in turn them made executable
#
# Parameters:
#     1: a file containing the names of the files from which we will discover shell scripts
#		 to be made executable (via USER_SHELL_SCRIPT_EXTENSIONS)
#     2: the name of a target directory in which those files will (hopefully) reside
#
function makeShellScriptsExecutable {

    local list=$1
    local targetDir=$2

    if [[ -n "$USER_SHELL_SCRIPT_EXTENSIONS" ]]; then

        # Don't want to bail if one of the chmods fails.
        set +e

        if [[ ! -f "$list" ]]; then
            logError "$0/$FUNCNAME: No such file \"$list\""
            return 1
        fi

        if [[ ! -d "$targetDir" ]]; then
            logError "$0/$FUNCNAME: Target directory \"$targetDir\" does not exist"
            return 1
        fi
        
		$pattern=$( echo "$USER_SHELL_SCRIPT_EXTENSIONS" | sed "s/:/\$\|/gm" )\$
        
        # filter file list for shell scripts
        shellScripts=$( cat $list | grep -E "$pattern" )

        echo $shellScripts | while read entry; do
            if [[ -f "$targetDir/$entry" ]]; then
                chmod ug+x "$targetDir/$entry"
            else
                logError "$0/$FUNCNAME: Attempted to make non existent entry $targetDir/$entry executable"
            fi
        done

    else
        log "$0/$FUNCNAME: bypassing setting shell scripts executable permission, as there are none configured"
    fi
}
# Metrics logging
#
# Parameters:
#     1: Job id.  If this is missing, nothing will be logged
#     2: Script name.  This would be $0 except for FTL templates which don't know their own name
#     *: Any remaining, relevant info which is also logged (providing job id present and metrics file exists)
#
function logMetrics {
    local jobId="${1:-unknown}"; shift
    local name="$1"; shift
    local metricsFile=$(getMetricsLogPath)
    if [[ -n "$metricsFile" && -n "$jobId" ]]; then
        echo "$(date +'%Y-%m-%d %H:%M:%S: %N') [ScriptOutput-1] INFO  com.mango.mif.s.h.e.l.l  - *** Execution Metrics : {$name} $@ [$jobId]" >> $metricsFile
    fi
}


# DO NOT EXPORT
MANGO_FUNCTIONS_LOADED=true
