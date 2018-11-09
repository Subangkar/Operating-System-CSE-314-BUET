#!/bin/bash

. ./data.sh
#findFiles

# stdid=$1;#"$_digit\{7\}"
# echo $stdid
# find output -type f -regextype sed -regex "$_stdname\_$_integer\_assignsubmission_file_$stdid.*\.zip"
# find output -type f -regextype sed -not -regex "$_stdname\_$_integer\_assignsubmission_file_1405015.*\.zip"

function hasStdID() {
  stdid=$1;
  res=`find output -type f -regextype sed -regex "$_stdname\_$_integer\_assignsubmission_file_$stdid.*\.zip"`
  if [ -z "$res" ]; then
    # echo 'No'
    RET_VAL_BOOL=0;
  else
    # echo 'Yes'
    RET_VAL_BOOL=1;
  fi
}

# hasStdID $1
#####
