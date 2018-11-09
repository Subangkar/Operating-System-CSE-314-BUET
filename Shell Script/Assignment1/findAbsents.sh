#!/bin/bash

. ./data.sh
. ./util.sh
. ./findID.sh


# Generates initial absent log
# 1) FILE_ROSTER 2) FILE_ABS 3) FILE_MARKS
function genAbsList() {
  IFS=$'\n'
  for student in `cut -d '"' -f 2-3 $1`
  do
    sid=$(echo $student| cut -d'"' -f 1|cut -f 2)
    sname=$(echo $student| cut -d',' -f 2)
    # echo $sid
    hasStdID $sid

    if [[ $RET_VAL_BOOL == 0 ]]; then
       # echo "$sid,$sname" >> "$2"
       appendToFile "$sid,$sname" "$2"
       # echo "$sid $sname 0" >> "$3"
       appendToFile "$sid $sname 0" "$3"
    fi

  done
}

##############



genAbsList $FILE_ROSTER $FILE_ABS $FILE_MARKS
