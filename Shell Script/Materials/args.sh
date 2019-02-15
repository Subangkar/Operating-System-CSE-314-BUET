#!/bin/bash
#Arguments
echo "Number of arguments: $#"
echo "First Argument: $1"
echo "Second Argument: $2"

#Sum of arguments
mysum=0
for val in $*
do
	mysum=$(( mysum+val ))
done
echo "Sum of arguments: $mysum"
