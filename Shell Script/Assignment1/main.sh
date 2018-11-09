#!/bin/bash


#### 3
# if [ -d "output" ]; then
#     rm -d 'output'
# fi
# mkdir 'output';

> "$FILE_ABS"
> "$FILE_MARKS"


#### 1
./findAbsents.sh
