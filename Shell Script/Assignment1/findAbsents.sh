#!/bin/bash

# GLOBAL VARIABLES

RET_VAL_BOOL=1
RET_VAL=''

LOG_DIR="output/log"

ABS_FILE="$LOG_DIR/absent.txt"
ROSTER_FILE="CSE_322.csv"

##################


#findFiles
digit='[0-9]'
integer="$digit\{1,\}"
stdname="[^_]\{1,\}"
# stdid=$1;#"$digit\{7\}"
# echo $stdid
# find output -type f -regextype sed -regex "$stdname\_$integer\_assignsubmission_file_$stdid.*\.zip"
# find output -type f -regextype sed -not -regex "$stdname\_$integer\_assignsubmission_file_1405015.*\.zip"

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







# Generates initial absent log
# 1) ROSTER_FILE 2) ABS_FILE
function genAbsList() {
  IFS=$'\n'
  for student in `cut -d '"' -f 2-3 $1`
  do
    sid=$(echo $student| cut -d'"' -f 1|cut -f 2)
    sname=$(echo $student| cut -d',' -f 2)
    # echo $sid
    hasStdID $sid

    if [[ $RET_VAL_BOOL == 0 ]]; then
       echo "$sid,$sname,$RET_VAL_BOOL" >> "$2"
    fi
    
  done
}

##############



# if [ ! -d "$directory" ]; then
mkdir -p $LOG_DIR
# fi

> "$ABS_FILE"

genAbsList $ROSTER_FILE $ABS_FILE
