#!/bin/bash

#findFiles
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

# hasStdID $1
#####

ABS_FILE="output/log/absent.txt"
ROSTER_FILE="CSE_322.csv"
> "$ABS_FILE"

IFS=$'\n'
for student in `cut -d '"' -f 2-3 CSE_322.csv`
do
      sid=$(echo $student| cut -d'"' -f 1|cut -f 2)
      sname=$(echo $student| cut -d',' -f 2)
      # echo $sid
      hasStdID $sid
      echo "$sid,$sname,$RET_VAL_BOOL" >> "$ABS_FILE"
done

# function genAbsList() {
#   #statements
# }
