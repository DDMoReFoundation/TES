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
#!/bin/env bash
#unobfuscated scripts directory
original=$(cd "$1"; pwd)
#obfuscated scripts directory
obfuscated=$(cd "$2"; pwd)

cd "$original"

echo "<html>"
echo "<body>"
echo "<table>"
echo "<tr><th>File</th><th>No of Lines</th><th>No of Changes</th></tr>"
find . -type f -name "*.sh" -o -name "*.py" | while read f; do
noOfChanges=$(diff -U 0 "$f" "$obfuscated/$f" | grep -v -c ^@)
totalLines=$(cat $f | wc -l)
echo -e "<tr><td>$f</td><td>$totalLines</td><td>$noOfChanges</td></tr>"
done;
echo "</table>"
echo "</body>"
echo "</html>"


