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
#
# Script which is responsible for performing any required transformation of script files before they reach obfuscation
#


#
# Ensures that file given as parameter ends with newline character
#
function ensureFileEndsWithNewLine {
    c=$(tail -c 1 $1)
    cat $1
    if [[ "$c" != "\n" ]]; then
        echo -e "\n"
    fi
}

#
# Transformations
#
ensureFileEndsWithNewLine $1