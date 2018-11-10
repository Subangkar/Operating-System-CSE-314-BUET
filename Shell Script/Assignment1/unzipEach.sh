#!/bin/bash


. data.sh
. util.sh


./findAbsents.sh





# @param zipname folderName
resolveStdID() {
  zipname="$1"
  folderNameSub="$2"
  stdname=`echo "$zipname" | cut -d"_" -f 1`;
  renamedFolder=`grep -i "$stdname" $FILE_ABS | cut -d"," -f 1`
  numMatches=`echo -n "$renamedFolder" | grep -c '^'`;
  if [ "$renamedFolder" = "" ] || [ $numMatches -gt 1 ]; then #-o \( $numMatches -gt 1 \)
    mv "$DIR_TEMP" "$DIR_EXT/$stdname"
  else
    deleteFromFileI "$renamedFolder,$stdname" $FILE_ABS
    mv "$DIR_TEMP/$folderNameSub" "$DIR_TEMP/$renamedFolder"
    mv "$DIR_TEMP/$renamedFolder" $DIR_OUTPUT
  fi
}


# @param zipname folderName
organizeNonIDFolder() {
  zipname="$1"
  folderNameSub="$2"
  if [[ $zipname =~ ^[^_]+_[0-9]+_assignsubmission_file_[0-9]{7}.*$ ]]; then #[^_]*_[0-9]_assignsubmission_file
    renamedFolder=`echo "$zipname" | cut -d"_" -f 5 | head -c 7`
    mv "$DIR_TEMP/$folderNameSub" "$DIR_TEMP/$renamedFolder"
    mv "$DIR_TEMP/$renamedFolder" $DIR_OUTPUT
    appendToFile "$renamedFolder 0" $FILE_MARKS
  else
    resolveStdID "$zipname" "$folderNameSub"
  fi
}

# @param zipname
organizeMultipleFolder() {
  # has ID in zipname
  if [[ $zipname =~ ^[^_]+_[0-9]+_assignsubmission_file_[0-9]{7}.*$ ]]; then #[^_]*_[0-9]_assignsubmission_file
    renamedFolder=`echo "$zipname" | cut -d"_" -f 5 | head -c 7`
    mv "$DIR_TEMP" "$renamedFolder"
    mv "$renamedFolder" $DIR_EXT
    appendToFile "$renamedFolder 0" $FILE_MARKS
  else    # don't have ID in zipname
    stdname=`echo "$zipname" | cut -d"_" -f 1`;
    renamedFolder="x\ny";#`grep -i "$stdname" $FILE_ABS | cut -d"," -f 1`
    # echo $renamedFolder
    # echo "$renamedFolder" | wc -l
    numMatches=`echo -n "$renamedFolder" | grep -c '^'`;
    if [ \( $renamedFolder = "" \) -o \( $numMatches -gt 1 \) ]; then #-o $(echo "$renamedFolder" | wc -c) -gt 0
      mv "$DIR_TEMP" "$DIR_EXT/$stdname"
    else
      deleteFromFileI "$renamedFolder,$stdname" $FILE_ABS
      mv "$DIR_TEMP/$folderNameSub" "$DIR_TEMP/$renamedFolder"
      mv "$DIR_TEMP/$renamedFolder" $DIR_OUTPUT
    fi
  fi
}


# @param zip
organizeZip() {
  createFolder $DIR_TEMP
  # extractZip "$1" $DIR_TEMP
  mkdir "$DIR_TEMP/CSE_322"
  touch "$DIR_TEMP/CSE_322/a.txt"
  appendToFile "1505015,abc" $FILE_ABS
  zipname="$1";
  if [ "$(find $DIR_TEMP -maxdepth 1 -printf %y)" = "dd" ]; then
    # It has only one subdirectory and no other content
    folderNameSub=`ls $DIR_TEMP`
    if [[ $folderNameSub =~ ^[0-9]{7}$ ]]; then #$folderNameSub =~ ^[0-9]{7}$
      mv "$DIR_TEMP/$folderNameSub" $DIR_OUTPUT
      # echo $folderName
      appendToFile "$folderNameSub 10" $FILE_MARKS
    elif [[ $folderNameSub =~ ^[0-9]{7}\_.*$ ]]; then
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
zip="abc_.zip"
# echo $zip
IFS=$'\n'
# for zip in `find "output" -type f -name '*.zip'`; do
  organizeZip "$zip"
  # deleteFile "$zip"
# done
deleteFolder $DIR_TEMP
