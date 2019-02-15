#!/bin/bash

# IFS=$'\n'
# for line in `cat test.txt`; do
#   # if [[ $line =~ ^[^0-9]*[0-9]{3}[^0-9]*$ ]]; then #[^_]*_[0-9]_assignsubmission_file
#     echo $line
#   # fi
# done

# grep . -type f -regextype sed -regex "$_stdname\_$_integer\_assignsubmission_file_$stdid.*\.zip"



# sed -i -r 's/pattern/replacement/g' test.txt
sed -r 's/[0-9]{3}([^" "0-9]+|"\n")/t/g' test.txt #[^0-9]+[0-9]{3}[^0-9]+
# sed -i -r '/[^0-9]*[0-9]{3}[^0-9]*/d' test.txt
