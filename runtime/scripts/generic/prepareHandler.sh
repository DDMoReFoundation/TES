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

# Note that obviously by the time we are invoked, mango_initialise.sh and mango_copier.sh
# are expected to be on the PATH

source mango_initialise.sh "$0" "$JOBID" "$@"

logMetrics "$JOBID" "$0" "START SCRIPT"
mango_copier.sh "$@"
logMetrics "$JOBID" "$0" "END SCRIPT"
