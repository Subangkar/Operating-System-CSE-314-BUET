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
