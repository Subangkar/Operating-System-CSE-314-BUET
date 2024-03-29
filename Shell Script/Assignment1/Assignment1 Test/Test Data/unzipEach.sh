#!/bin/bash


. data.sh
. util.sh



# @param zipname folderName
resolveStdID() { # already marked 0
  local zipname="$1"
  local folderNameSub="$2"
  local stdname=`echo "$zipname" | cut -d"_" -f 1`;
  local renamedFolder=`grep -i "$stdname" $FILE_ABS | cut -d"," -f 1`
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
  local zipname="$1"
  local folderNameSub="$2"
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
  local zipname="$1"
  local folderNameSub="$2"
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
  local zipname="$1";


  if [ "$(find $DIR_TEMP -maxdepth 1 -printf %y)" = "dd" ]; then
    # It has only one subdirectory and no other content
    local folderNameSub=`ls $DIR_TEMP`
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
