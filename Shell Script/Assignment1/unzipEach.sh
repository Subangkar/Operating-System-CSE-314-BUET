#!/bin/bash


. data.sh
. util.sh


./findAbsents.sh





# @param zip
organizeZip() {
  createFolder $DIR_TEMP
  extractZip "$1" $DIR_TEMP
  # mkdir "$DIR_TEMP/1405015_CSE_322"
  # touch "$DIR_TEMP/1405015_CSE_322/a.txt"

  if [ "$(find $DIR_TEMP -maxdepth 1 -printf %y)" = "dd" ]; then
    # It has only one subdirectory and no other content
    folderNameSub=`ls $DIR_TEMP`
    # echo $folderNameSub
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
    fi
  fi

}




zip=`find "output" -type f -name '*1405011.zip'`;
# echo $zip
IFS=$'\n'
for zip in `find "output" -type f -name '*.zip'`; do
  organizeZip "$zip"
  deleteFile "$zip"
done
deleteFolder $DIR_TEMP
