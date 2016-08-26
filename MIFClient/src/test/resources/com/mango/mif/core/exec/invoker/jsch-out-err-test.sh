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

function output {
    if (( $# == 0 )); then
        return
    fi
    typeset fd=$1
    shift
    eval echo "$@" >&$fd
    if [[ -n $outputFileBase ]]; then
        echo "$@" >> $outputFileBase.$fd
    fi
    if [[ -n $confirmLog ]]; then
        echo "$$: $fd: $@" >> $confirmLog
    fi
}

outputStdout=1
outputStderr=1
outputBeforeSleep=0
input=0
error=0
delay=0
exitStatus=0
loopForSeconds=0
loopTotal=0
outputFileBase=
confirmLog=
while getopts 'id:sebE:S:T:f:C:' arg "$@"; do
  case $arg in
    i)  input=1 ;;
    d)  delay=$OPTARG ;;
    b)  outputBeforeSleep=1 ;;
    s)  outputStderr=0 ;;
    E)  exitStatus=$OPTARG ;;
    e)  outputStdout=0 ;;
    S)  loopForSeconds=$OPTARG ;;
    T)  loopTotal=$OPTARG ;;
    C)  confirmLog=$OPTARG ;;
    f)  outputFileBase=$OPTARG ;;
    ?)  error=1 ;;
  esac
done

let x=OPTIND-1
shift $x

if (( error )); then
    echo "Usage: $0 [-d delay] [-b] [-i] [-e] [-s] [-E exit_status] [-S loop_seconds] [-T loop_times] [-f file] [-C confirm-log] [expected output]"
    echo "-d delay    Sleep for \"delay\" seconds before producing output"
    echo "-b          output different things before the sleep"
    echo "-i          Read from stdin and output whatever is read"
    echo "-E exit     Exit with value \"exit\" rather than 0"
    echo "-S seconds  Loop for \"seconds\" seconds producing as much output as possible"
    echo "-T times    Loop, producing \"times\" lines of output"
    echo "-e          Write only to stderr"
    echo "-s          Write only to stdout"
    echo "-f file     Write your choice of stdout and/or stderr to a file as well"
    echo "-C file     Confirmation log file"
    echo
    echo "This script is invoked by the JSCH test harness."
    echo "It may seem quirky, unusual and even pointless."
    exit 9
fi

if [[ -n $confirmLog ]]; then
    if ! echo > $confirmLog; then
         confirmLog=
    fi
fi

startSeconds=$(date +%s)

if (( $# == 0 )); then
    STDOUT="This to stdout"
    STDERR="This to stderr"
else
    STDOUT="stdout: $@"
    STDERR="stderr: $@"
fi

if (( input )); then
    read stdin
    STDOUT="stdout: $stdin"
    STDERR="stderr: $stdin"
fi

# If we're outputting to a file, clean out the files beforehand.
if [[ -n $outputFileBase ]]; then
    /bin/rm -f $outputFileBase.[12]
fi

if (( outputBeforeSleep )); then
    typeset -u UPPER
    if (( outputStdout )); then
        UPPER=$STDOUT
        output 1 "BEFORE SLEEP $UPPER"
    fi
    if (( outputStderr )); then
        UPPER=$STDERR
        output 2 "BEFORE SLEEP $UPPER"
    fi
fi

if (( delay )); then
    sleep $delay
fi

if (( loopForSeconds > 0 )); then
    (( stopSeconds = startSeconds + loopForSeconds ))
    total=0
    while :
    do
        now=$(date +%s)
        if (( now > stopSeconds )); then
            break;
        fi
        (( interval = now - startSeconds ))
        (( total++ ))
        if (( outputStdout )); then
            output 1 "$interval: $total: $STDOUT"
        fi
        if (( outputStderr )); then
            output 2 "$interval: $total: $STDERR"
        fi
    done
elif (( loopTotal > 0 )); then
    total=0
    while (( total < loopTotal ))
    do
        (( total++ ))
        if (( outputStdout )); then
            output 1 "$total: $STDOUT"
        fi
        if (( outputStderr )); then
            output 2 "$total: $STDERR"
        fi
    done
else
    if (( outputStdout )); then
        output 1 "$STDOUT"
    fi
    if (( outputStderr )); then
        output 2 "$STDERR"
    fi
fi

exit $exitStatus
