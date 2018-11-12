#!/bin/bash

cd test
fileName="$1"

IFS=$'\n'
for line in `cat $fileName`; do
    echo "Searching for $line ..."
    # item="test2.txt"
    for item in `find . -type f -not -name "$fileName" | sort`; do
        
        lines=`grep -n "$line" "$item"`

        if [ -n "$lines" ]; then
            echo `echo "$item" | cut -d "/" -f 2`
            lineNos=`echo $lines | cut -d ":" -f 1`
            for n in "$lineNos"; do
                echo "Copy Found in line $n";
            done            
        fi
        
    done
done
