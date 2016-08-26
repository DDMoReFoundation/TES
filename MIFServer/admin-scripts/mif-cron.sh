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

SCRIPT_DIR=$(cd $(dirname $0); /bin/pwd)
SCRIPT_NAME=$(basename $0)
SCRIPT_PATH="$SCRIPT_DIR/$SCRIPT_NAME"

# Our first problem is the logging is a bit too flexible in that you can log anywhere
# you want by defining MANGO_LOG_OUTPUT practically anywhere (e.g. in the Navigator
# execution profiles, or at the top of mango_functions.fn.sh).  If the logging directory
# is defined as something other than the default of /var/log/mif, it should be changed
# here too.
#
MIF_LOG_DIR=/var/log/mif

# Define this as the number of days of logs you want to keep
#
KEEP_LOGS_FOR_N_DAYS=5

if ! cd $MIF_LOG_DIR > /dev/null 2>&1
    echo "cd to MIF log directory \"$MIF_LOG_DIR\" failed." >&2
    echo "Please update MIF_LOG_DIR in $SCRIPT_PATH to reflect the actual mif log directory location." >&2
    exit 5
fi

# Remove all the old stuff
#
find . -type f -mtime +$KEEP_LOGS_FOR_N_DAYS -exec rm -f {} \;

# Treat the shell.log and the shell-metrics.log in a similar way
#
for log in shell.log shell-metrics.log; do

    # Move the current log sideways...
    #
    if [[ -f $log ]]; then

        # ... but only if the log has something in it, otherwise leave it where it is
        #
        if [[ -s $log ]]; then
            DATE=$(date "+%Y-%m-%d")
            mv $log $DATE-$log
            gzip -9 $DATE-$log
            chmod 644 $DATE-$log
        fi
        # Create a new, empty one
        #
        > $log

        # With a mode allowing anyone to write to it
        chmod 666 $log
    fi
done
