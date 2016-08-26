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

# This script is SOURCED by every important script we run.
# YOU MUST ENSURE there are NO syntax errors in this file
# or literally EVERYTHING will fail
#
# Fortunately there are no preambles inserted into this, so
# there is less chance of something set in navigator (via an
# application profile etc.) of upsetting it

set -e

# Enable debugging while installing and testing.  Don't forget to remove
# this when everything is working, otherwise the /var/log/mif/shell.log
# file will grow without limit.
#
export MANGO_LOGGING=true

# The templates set the job id as the second parameter (the
# name of the template being the first parameter).
# This allows us to relate logged messages back to the job.
#
export JOBID="$2"

source mango_functions.fn.sh

log "$@"
logMetrics "$JOBID" "$0" "SCRIPT START"

umask ${UMASK:-002}

logMetrics "$JOBID" "$0" "END SCRIPT"
