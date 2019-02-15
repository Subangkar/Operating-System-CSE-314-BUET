#!/bin/bash

#if-then-else
var=11
if [ $var -lt 5 ]; then
	echo 'Variable has a value less than 5'
fi

if [ $((var+5)) -gt 15 ]; then
	echo 'Variable has a value greater than 10'
fi



#if-then-else
# var=10
#
# if [[ $var < $((5)) ]]; then
# 	echo 'Variable has a value less than 5'
# fi
#
# if [[ $((var+5)) > 15  ]]; then
# 	echo 'Variable has a value > than 10'
# fi
# if [[ $((var >= 15)) ]]; then
#     echo greater equal
# fi




#case statement in script
#No break statement required
var=5
var1=3
case $((var%5)) in
	0) echo 'Remainder 0'
		# var1=$(( var1 + 3 ))
		echo $(( var1 + 3 ));;
	1) echo 'Remainder 1';;
	2) echo 'Remainder 2';;
	*) echo 'Remainder Greater than 1';;
esac
