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



#### 1
./findAbsents.sh

# ./unzipEach.sh
