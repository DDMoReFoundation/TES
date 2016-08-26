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
#!/bin/bash

# Common Mango File and Directory Copying Utility
#
#
# Flags were added to this script in a bit of a haphazard manner, but here is an example usage:
#
#   -r fromDir toDir -d dir1 -d dir2 -d dir3 -c fromFile1 toFile1 -c fromFile2 toFile2
#
# Usage:
#   -r from-d to-d      Recursively copy the directory structure (only) from "from-d" to "to-d",
#                       creating the directory "to-d", and any other directories, as necessary
#   -R from-d to-d      Recursively copy the directory structure and files from "from-d" to "to-d",
#                       creating the directory "to-d", and any other directories, as necessary
#   -d dir              Create the directory "dir"
#   -c from-f1 to-f1    Copy the file "from-f1" to "to-f1", creating any intermediate directories
#                       in the path of "to-f1" as necessary
#
# IMPORTANT NOTE: arguments MUST be separated from the flags, so "-d dir" is fine, but "-ddir" is
# absolutely not.
# No other arguments (apart from flags and arguments to flags) are expected.  If they are given,
# they are treated as an error.
# This is called from AT LEAST TWO state handlers.
#
# Note the exit values are random, I just tried to make sure they were different, so when the exit
# status is logged we can see WHAT it was that exited.
#
# Also note that COPY_MODE was invented to bypass the excessive copying of empty directories.  If
# COPY_MODE=all (the default) then we copy (potentially) thousands of empty directories.  Conversely
# if COPY_MODE=file then we abandon the copying of empty directories and just copy files.
#

# Pull in log, logError and logErrorAndExit.
#
source mango_functions.fn.sh

# NAVDEV-5292: Running a bootstrap overwrites original model files, marking the original run as deleted
# NAVDEV-6759: Executing bootstrap on a .ctl file re-runs initial execution
#
# This function is called on the fly for every file we copy across.  When we are in bootstrap mode
# (as signified by MANGO_BOOTSTRAP having non zero length), we keep a look out for the control file,
# remembering that it may have a .ctl, .mod or .nmctl suffix in any case variation.
#
# If we do see the control file, we additionally copy the list file.  However, even this isn't
# straightforward, although we may be dealing with foo.ctl and foo.lst, a "feature" of bootstrap
# says that the list file must be called "foo.ctl.lst".  The same goes for foo.nmctl.  However,
# if your control file is called foo.mod the game changes again, with the list file needing to
# be called "foo.lst".  This function deals with all these things.
#
function bootstrap_copy_lst_file {

    # Only do this if we are in bootstrap mode
    #
    if [[ -z "$MANGO_BOOTSTRAP" ]]; then
        return
    fi

    local fromFile="$1"; shift
    local toFile="$1"; shift
    local proceed=0
    local rename=0
    case $fromFile in
      *.[Mm][Oo][Dd])
            proceed=1
            ;;
      *.[Cc][Tt][Ll]|*.[Nn][Mm][Cc][Tt][Ll]|*.[Cc][Oo][Nn]|*.[Nn][Mm])
            proceed=1
            rename=1
            ;;
    esac

    if (( ! proceed )); then
        # not dealing with a control file, bail out.
        return
    fi

    log "MANGO_COPIER IS IN MANGO BOOTSTRAP MODE with file $fromFile (rename is $rename)"

    local sourceDir=$(dirname $fromFile)
    local controlFileName=$(basename $fromFile)
    local baseFile="${controlFileName%.*}"
    local suffix="${controlFilename##*.}"
    local listFile=$(cd $sourceDir; ls -1 $baseFile.* 2> /dev/null | grep -i '.lst$')

    # If there is a list file, copy it.
    #
    if [[ -n "$listFile" ]]; then
        local targetDir=$(dirname $toFile)
        local targetName=$listFile

        # do we need to rename it as we copy?
        #
        if (( rename )); then
            targetName=$controlFileName.lst
        fi

        log "MANGO_COPIER: cp $sourceDir/$listFile $targetDir/$targetName"
        mkdir -p $targetDir
        cp $sourceDir/$listFile $targetDir/$targetName
        logMetrics "$JOBID" "$0" "$(stat -c "%n %s" "$sourceDir/$listFile")"
    else
        log "MANGO_COPIER: There is no LST file to copy"
    fi
}

logArgs "$0" "$@"
logMetrics "$JOBID" "$0" "START SCRIPT"

# Convert the DIR_IGNORE_PATTERN into the format we need it.
#
# and no, the ordering of the parameters is NOT a mistake
#
dirIgnorePattern=$(constructIgnorePattern "" "$DIR_IGNORE_PATTERN" "")
fullIgnorePattern=$(constructIgnorePattern "" "$FILE_IGNORE_PATTERN" "$DIR_IGNORE_PATTERN")

while (( $# > 0 )); do
    case $1 in
        -r) # Recursively copy the directory structure (i.e no files) from $1 to $2
            #
            shift
            if (( $# < 2 )); then
                logErrorAndExit 127 "$0: Bad usage: -r has less than two args"
            else
                fromDir="$1"
                shift
                toDir="$1"
                shift
            fi
            if [[ ! -d "$fromDir" ]]; then
                logErrorAndExit 100 "$0: option -r given invalid source directory $fromDir - bailing out"
            fi
            if [[ ! -d "$toDir" ]]; then
                log "$0: (-r) mkdir -p $toDir"
                mkdir -p "$toDir" || exit 5
            fi
            case $COPY_MODE in
                file*)
                    log "COPY_MODE=$COPY_MODE, ignoring \"-r $fromDir $toDir\" flag in mango_copier.sh"
                    ;;
                *)
                    log "COPY_MODE=$COPY_MODE, implementing \"-r $fromDir $toDir\" flag in mango_copier.sh"
                    if [[ -d "$toDir" ]]; then
                        log "$0: (-r) find... $fromDir piped into cpio $toDir"
                        logMetrics "$JOBID" "$0" "START (-r) copyFileStructure d $fromDir $toDir $dirIgnorePattern"
                        copyFileStructure "d" "$fromDir" "$toDir" "$dirIgnorePattern" || exit 6
                        logMetrics "$JOBID" "$0" "END (-r) copyFileStructure d $fromDir $toDir $dirIgnorePattern"
                    fi
                    ;;
            esac
            ;;
        -R) # Like -r, except we copy files too.
            shift
            if (( $# < 2 )); then
                logErrorAndExit 127 "$0: Bad usage: -R and less than two args"
            else
                fromDir="$1"
                shift
                toDir="$1"
                shift
            fi
            if [[ ! -d "$toDir" ]]; then
                log "$0: (-R) mkdir -p $toDir"
                mkdir -p "$toDir" || exit 7
            fi
            if [[ ! -d "$fromDir" ]]; then
                logErrorAndExit 2 "$0: option -R given invalid source directory $fromDir - bailing out"
            fi
            log "$0: (-R) find $fromDir... cpio $toDir"
            logMetrics "$JOBID" "$0" "START (-R) copyFileStructure $fromDir $toDir $fullIgnorePattern"
            case $COPY_MODE in
                file*)
                    log "mango_copier.sh: COPY_MODE=$COPY_MODE, copying files only"
                    copyFileStructure "f" "$fromDir" "$toDir" "$fullIgnorePattern" || exit 8 ;;
                *)
                    log "mango_copier.sh: COPY_MODE=$COPY_MODE, copying everything"
                    copyFileStructure "" "$fromDir" "$toDir" "$fullIgnorePattern" || exit 58 ;;
            esac
            logMetrics "$JOBID" "$0" "END (-R) copyFileStructure $fromDir $toDir $fullIgnorePattern"
            ;;
        -d) # Create directories, always do this, no matter what COPY_MODE we are given
            #
            shift
            if (( $# == 0 )); then
                logErrorAndExit "Bad usage: -d and no args" >&2
            else
                dir="$1"
                shift
            fi
            if [[ ! -d "$dir" ]]; then
                log "$0: (-d) mkdir -p $dir"
                mkdir -p "$dir" || exit 9
            fi
            ;;
        -c) # Copy an existing file to the specified target, creating missing directories in the target path
            # as necessary, always do this, no matter what COPY_MODE we are given
            shift
            if (( $# < 2 )); then
                logErrorAndExit 127 "$0: Bad usage: -c and less than two args"
            else
                fromFile="$1"
                shift
                toFile="$1"
                shift
            fi

            # NAVDEV-5292: Running a bootstrap overwrites original model files, marking the original run as deleted
            # NAVDEV-6759: Executing bootstrap on a .ctl file re-runs initial execution
            #
            bootstrap_copy_lst_file "$fromFile" "$toFile"

            logMetrics "$JOBID" "$0" "START (-c) copyFileStructure cp $fromFile $toFile"
            toBase=$(dirname "$toFile")
            if [[ ! -d $toBase ]]; then
                log "$0: (-c) mkdir -p $toBase"
                mkdir -p "$toBase" || exit 10
            fi
            if [[ ! -f "$fromFile" ]]; then
                logErrorAndExit 2 "$0: option -c given source file $fromFile which does not exist"
            fi
            log "$0: (-c) cp $fromFile $toFile"
            cp "$fromFile" "$toFile" || exit 11
            logMetrics "$JOBID" "$0" "END (-c) $(stat -c "%n %s" "$fromFile")"
            logMetrics "$JOBID" "$0" "END (-c) copyFileStructure cp $fromFile $toFile"
            ;;
        -*)
            flag="$1"
            shift
            logErrorAndExit 127 "Bad usage: unknown flag $flag (remaining args $@)"
            ;;
        *)
            unknown="$1"
            shift
            logErrorAndExit 127 "Bad usage: unknown arg $unknown (remaining args $@)"
            ;;
    esac
done

logMetrics "$JOBID" "$0" "END SCRIPT"
exit
