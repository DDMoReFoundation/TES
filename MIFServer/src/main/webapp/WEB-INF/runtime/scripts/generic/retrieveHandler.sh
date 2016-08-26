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

# Note that obviously by the time we are invoked, mango_initialise.sh
# is expected to be on the PATH

# Also note: Initially this script took -c arguments which said what file
# to copy.  Unfortunately with some runs (bootstrap?) there are many hundreds
# of files, which blew the argument length and caused the script to fail.
# Now the names of the files to copy are read from the standard input.
#
# Now the -c flag seems to indicate whether to actually do the copy or not.
# But if we don't do the copy... what is the point?

source mango_initialise.sh $0 "$JOBID" "$@"

logMetrics "$JOBID" "$0" "START SCRIPT"

targetHiddenDirName=
sourceHiddenDirName=
sourceDir=
targetDir=
modifiedListFileName=
copyToOutputDir=1

i=0

while getopts 'h:e:s:l:d:c' arg "$@"; do
  case $arg in
    h)  targetHiddenDirName="$OPTARG" ;;
    e)  sourceHiddenDirName="$OPTARG" ;;
    s)  sourceDir="$OPTARG" ;;
    d)  targetDir="$OPTARG" ;;
    l)  modifiedListFileName="$OPTARG" ;;
    c)  copyToOutputDir=0 ;;
    ?)  logErrorAndExit 127 "retrieveHandler.sh saw unknown flag $OPTARG" ;;
  esac
done

let x=OPTIND-1
shift $x
# Preconditions
#
[[ -z $targetHiddenDirName ]] && logErrorAndExit 128 "$0 expected argument -h with target hidden MIF directory name"
[[ -z $sourceHiddenDirName ]] && logErrorAndExit 128 "$0 expected argument -e with source hidden MIF directory name"
[[ -z $sourceDir ]] && logErrorAndExit 128 "$0 expected argument -s with source directory path"
[[ ! -d $sourceDir ]] && logErrorAndExit 128 "$0 -s argument $sourceDir: directory is expected to exist"
[[ -z $targetDir ]] && logErrorAndExit 128 "$0 expected argument -d with request output folder"
[[ ! -d $targetDir ]] && logErrorAndExit 128 "$0 -d argument $targetDir: directory is expected to exist"
[[ -z $modifiedListFileName ]] && logErrorAndExit 128 "$0 -l argument expected"

targetHiddenDir="$targetDir/$targetHiddenDirName"
sourceHiddenDir="$sourceDir/$sourceHiddenDirName"

logMetrics "$JOBID" "$0" "START STEP1"

# Step 1
# Create the hidden/secret MIF directory with no special modes (the current umask will have to do)
#
if [[ ! -d $targetHiddenDir ]]; then
    log "$0: mkdir -p $targetHiddenDir"
    if ! mkdir -p "$targetHiddenDir"; then
        logErrorAndExit 31 "$0: FATAL ERROR: Failed to create hidden directory $targetHiddenDir"
    fi
fi

logMetrics "$JOBID" "$0" "END STEP1"
logMetrics "$JOBID" "$0" "START STEP2"

# Step 2
# Move the grid output and error files into the hidden/secret MIF directory with no special modes.
# We copy these files rather than move them so users can see them in the related files view once
# the job has finished
#
if (( copyToOutputDir )); then
      cp -r "$sourceHiddenDir/"* "$targetHiddenDir"
fi

logMetrics "$JOBID" "$0" "END STEP2"
logMetrics "$JOBID" "$0" "START STEP3"

# Step 3
# Create the file containing the list of copied files, in the hidden/secret MIF directory
#
log "$0: Creating the modified list file $modifiedListFileName"
modifiedListFile=$targetHiddenDir/$modifiedListFileName
> $modifiedListFile


logMetrics "$JOBID" "$0" "END STEP3"
logMetrics "$JOBID" "$0" "START STEP4"

# Step 4
# Copy files from the grid shared directory to the MIF output directory. Remember that the
# relative filenames are ON OUR STANDARD INPUT to avoid "arg list too long" messages.
#
while read relativePath
do
    sourcePath="$sourceDir/$relativePath"
    targetPath="$targetDir/$relativePath"
    targetParentPath=$(dirname "$targetPath")
    if (( copyToOutputDir )); then
        if [[ ! -d $targetParentPath ]]; then
            log "$0: mkdir -p $targetParentPath"
            if ! mkdir -p "$targetParentPath"; then
                logErrorAndExit 32 "$0: FATAL ERROR: $targetParentPath does not exist and cannot be created"
            fi
        fi
        if [[ ! -w "$targetParentPath" ]]; then
            logErrorAndExit 33 "$0: FATAL ERROR: $targetParentPath exists and is not writable"
        fi
        if [[ -e "$targetPath" && ! -w "$targetPath" ]]; then
            logErrorAndExit 34 "$0: FATAL ERROR: $targetPath exists and is not writable"
        fi

        # log "$0: cp $sourcePath $targetPath"
        cp "$sourcePath" "$targetPath"
    fi

    # as we are going, we keep the list of updated files up to date
    # log "$0: adding \"$targetPath\" to $modifiedListFile"
    echo "$targetPath" >> "$modifiedListFile"
done

logMetrics "$JOBID" "$0" "END STEP4"
logMetrics "$JOBID" "$0" "END SCRIPT"
