#!/bin/bash

digit='[0-9]'
integer="$digit\{1,\}"

stdname="[^_]\{1,\}"
stdid="$digit\{7\}"

find workingDir -type f -regextype sed -not -regex "$stdname\_$integer\_assignsubmission_file_$stdid.*\.zip"
