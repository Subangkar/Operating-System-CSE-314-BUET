#!/bin/bash

# GLOBAL VARIABLES

_digit='[0-9]'
_integer="$_digit\{1,\}"
_stdname="[^_]\{1,\}"


RET_VAL_BOOL=1
RET_VAL=''

DIR_OUTPUT="output"
DIR_LOG="$DIR_OUTPUT/log"
DIR_TEMP="$DIR_OUTPUT/temp"
DIR_EXT="$DIR_OUTPUT/extra"

FILE_MARKS="$DIR_LOG/marks.txt"
FILE_ABS="$DIR_LOG/absent.txt"
FILE_ROSTER="CSE_322.csv"

##################
#!/bin/bash

deleteFolder() {
  if [ -d "$1" ]; then
    rm -d -r -f "$1"
  fi
}

# TODO createFolder's description
# @param  folderName
#
createFolder() {
  deleteFolder $1
  mkdir "$1"
}

deleteFile() {
  if [ -f "$1" ]; then
    rm -f "$1"
  fi
}

# TODO createFolder's description
# @param  fileName
#
createFile() {
  deleteFile $1
  touch "$1"
}

# @param contents fileName
appendToFile() {
  if [ ! -f "$2" ]; then
    touch $2
  fi
  echo "$1" >> "$2"
}

# @param contents fileName
deleteFromFileI() {
  newContents=`grep -i -v "$1" $2`;
  echo "$newContents" > $2
}

# TODO extractZip's description
# @param zipname
#
extractZip() {
  zipname="$1";
  path="$2"
  echo "Extracting: $zipname"
  unzip -qq -o "$zipname" -d "$path/"
}


#######################


#### 3

createFolder $DIR_OUTPUT
createFolder $DIR_LOG
createFolder $DIR_EXT

createFile $FILE_ABS
createFile $FILE_MARKS



extractZip SubmissionsAll.zip $DIR_OUTPUT
# deletes RARs
find $DIR_OUTPUT -type f -name '*.rar' -exec rm {} \;




##########################

##############
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
#########################


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
       appendToFile "$sid 0" "$3"
    fi

  done
}

genAbsList $FILE_ROSTER $FILE_ABS $FILE_MARKS

##############





# @param zipname folderName
resolveStdID() { # already marked 0
  # zipname="$1"
  # folderNameSub="$2"
  stdname=`echo "$zipname" | cut -d"_" -f 1`;
  renamedFolder=`grep -i "$stdname" $FILE_ABS | cut -d"," -f 1`
  numMatches=`echo -n "$renamedFolder" | grep -c '^'`;
  if [ "$renamedFolder" = "" ] || [ $numMatches -gt 1 ]; then
    echo "No match or multiple match"
    mv "$DIR_TEMP" "$DIR_EXT/$stdname"
  else
    deleteFromFileI "$renamedFolder,$stdname" $FILE_ABS
    echo "id single match"
    if [ -z "$folderNameSub" ]; then
      echo "multiple subdirectory"
      mv "$DIR_TEMP/" "$DIR_OUTPUT/$renamedFolder"
    else
      echo "single subdirectory"
      mv "$DIR_TEMP/$folderNameSub" "$DIR_OUTPUT/$renamedFolder"
    fi
  fi
}


# @param zipname folderName
organizeNonIDFolder() {
  # zipname="$1"
  # folderNameSub="$2"
  if [[ $zipname =~ ^[^_]+_[0-9]+_assignsubmission_file_[0-9]{7}.*$ ]]; then #[^_]*_[0-9]_assignsubmission_file
    renamedFolder=`echo "$zipname" | cut -d"_" -f 5 | head -c 7`
    # mv "$DIR_TEMP/$folderNameSub" "$DIR_TEMP/$renamedFolder"
    # mv "$DIR_TEMP/$renamedFolder" $DIR_OUTPUT
    mv "$DIR_TEMP/$folderNameSub" "$DIR_OUTPUT/$renamedFolder"
    appendToFile "$renamedFolder 0" $FILE_MARKS
    echo $renamedFolder
  else
    resolveStdID "$zipname" "$folderNameSub"
  fi
}

# @param zipname folderName
organizeMultipleFolder() {
  # zipname="$1"
  # folderNameSub="$2"
  # has ID in zipname
  if [[ $zipname =~ ^[^_]+_[0-9]+_assignsubmission_file_[0-9]{7}.*$ ]]; then #[^_]*_[0-9]_assignsubmission_file
    renamedFolder=`echo "$zipname" | cut -d"_" -f 5 | head -c 7`
    mv "$DIR_TEMP" "$DIR_OUTPUT/$renamedFolder"
    # mv "$renamedFolder" $DIR_EXT
    appendToFile "$renamedFolder 0" $FILE_MARKS
  else    # don't have ID in zipname
    resolveStdID "$zipname" "";
  fi
}


# @param zip
organizeZip() {
  createFolder $DIR_TEMP
  extractZip "$1" $DIR_TEMP
  # mkdir "$DIR_TEMP/CSE_322"
  # mkdir "$DIR_TEMP/CSE_322_1"
  # touch "$DIR_TEMP/CSE_322/a.txt"
  # appendToFile "1505015,abc" $FILE_ABS
  # appendToFile "1505016,abc" $FILE_ABS
  zipname="$1";


  if [ "$(find $DIR_TEMP -maxdepth 1 -printf %y)" = "dd" ]; then
    # It has only one subdirectory and no other content
    folderNameSub=`ls $DIR_TEMP`
    if [[ $folderNameSub =~ ^[0-9]{7}$ ]]; then
      #perfect Ok
      mv "$DIR_TEMP/$folderNameSub" $DIR_OUTPUT
      appendToFile "$folderNameSub 10" $FILE_MARKS
    elif [[ $folderNameSub =~ ^[0-9]{7}\_.*$ ]]; then
      # half Ok
      renamedFolder=`echo "$folderNameSub" | cut -d"_" -f 1`
      # echo $renamedFolder
      mv "$DIR_TEMP/$folderNameSub" "$DIR_TEMP/$renamedFolder"
      mv "$DIR_TEMP/$renamedFolder" $DIR_OUTPUT
      appendToFile "$renamedFolder 5" $FILE_MARKS
    else
      organizeNonIDFolder $zipname $folderNameSub;
    fi
  else
    organizeMultipleFolder $zipname $folderNameSub
  fi

}




# zip=`find "output" -type f -name '*1405001.zip'`;
# zip="abc_.zip"
# echo $zip
IFS=$'\n'
for zip in `find "output" -type f -name '*.zip'`; do
  organizeZip "$zip"
  deleteFile "$zip"
done
deleteFolder $DIR_TEMP

##################




echo "All done"

# sortedID=`cat "$FILE_MARKS" | sort`
sort -o "$FILE_MARKS" "$FILE_MARKS"
sort -o "$FILE_ABS" "$FILE_ABS"
# echo "$sortedID" > $FILE_MARKS
# sortedID=`cat "$FILE_ABS" | sort`
# echo "$sortedID" > $FILE_ABS
# cut -d"," -f 1 |
echo "$(sed "s/[^0-9]//g" "$FILE_ABS")" > "$FILE_ABS"

echo "Results sorted"
