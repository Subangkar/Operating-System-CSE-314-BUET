#!/bin/bash

# cd output

# Strip leading and trailing white space (new line inclusive).
# trim(){
#     [[ "$1" =~ [^[:space:]](.*[^[:space:]])? ]]
#     # printf "%s" "$BASH_REMATCH"
# }

IFS=$'\n'
for student in `cut -d '"' -f 2-3 CSE_322.csv`
do
      sid=$(echo $student| cut -d'"' -f 1|cut -f 2)
      sname=$(echo $student| cut -d',' -f 2)
      echo $sid
done
