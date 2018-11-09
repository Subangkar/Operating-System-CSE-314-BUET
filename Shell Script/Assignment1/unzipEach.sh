#!/bin/bash


. data.sh
. util.sh


./findAbsents.sh






zip=`find "output" -type f -name '*1405011.zip'`;
echo $zip
# IFS=$'\n'
# for zip in `find "output" -type f -name '*.zip'`; do

  createFolder $DIR_TEMP
  #
  unzip -o $zip -d "$DIR_TEMP/"
# done
deleteFolder $DIR_TEMP
