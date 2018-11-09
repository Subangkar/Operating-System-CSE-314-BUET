#!/bin/bash

digit='[0-9]'
integer="$digit\{1,\}"

stdname="[^_]\{1,\}"
# stdid=$1;#"$digit\{7\}"
# echo $stdid
# find output -type f -regextype sed -regex "$stdname\_$integer\_assignsubmission_file_$stdid.*\.zip"
# find output -type f -regextype sed -not -regex "$stdname\_$integer\_assignsubmission_file_1405015.*\.zip"

RET_VAL_BOOL=1
function hasStdID() {
  stdid=$1;
  res=`find output -type f -regextype sed -regex "$stdname\_$integer\_assignsubmission_file_$stdid.*\.zip"`
  if [ -z "$res" ]; then
    # echo 'No'
    RET_VAL_BOOL=0;
  else
    # echo 'Yes'
    RET_VAL_BOOL=1;
  fi
}

hasStdID $1
# echo $RET_VAL_BOOL
