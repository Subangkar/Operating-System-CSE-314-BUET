#!/bin/bash

. ./data.sh
. ./util.sh

#### 3

createFolder $DIR_OUTPUT
createFolder $DIR_LOG
createFolder $DIR_EXT

createFile $FILE_ABS
createFile $FILE_MARKS



extractZip SubmissionsAll.zip $DIR_OUTPUT
# deletes RARs
find $DIR_OUTPUT -type f -name '*.rar' -exec rm {} \;




#### 1
./findAbsents.sh

./unzipEach.sh









cat "$FILE_MARKS" | sort
