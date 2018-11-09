#!/bin/bash


. data.sh
. util.sh


./findAbsents.sh






zip=`find "output" -type f -name '*1405011.zip'`;
# echo $zip
# IFS=$'\n'
# for zip in `find "output" -type f -name '*.zip'`; do

  createFolder $DIR_TEMP
  #
  # extractZip "$zip" $DIR_TEMP
  mkdir "$DIR_TEMP/1405015_CSE_322"
  touch "$DIR_TEMP/1405015_CSE_322/a.txt"

  if [ "$(find $DIR_TEMP -maxdepth 1 -printf %y)" = "dd" ]; then
    # It has only one subdirectory and no other content
    folderNameSub=`ls $DIR_TEMP`
    # echo $folderNameSub
    if [[ $folderNameSub =~ ^[0-9]{7}$ ]]; then
      mv "$DIR_TEMP/$folderNameSub" $DIR_OUTPUT
    elif [[ $folderNameSub =~ ^[0-9]{7}\_*$ ]]; then
      echo "$DIR_TEMP/$folderNameSub"
    fi
  fi
# done
# deleteFolder $DIR_TEMP
