#!/bin/bash

#any variable is a string here. 
echo '1 + 1'

#expr is used for simple arithmetic operations
echo `expr 1 + 1`

#echo `expr 1+1` does not work, due to lack of space between two numbers
echo `expr 2 \* 3`

#Another way of evaluating arithmetic expression
var1=$(( (4+5)*3 ))
echo $var1
(( var1++ ))
echo $var1
((var1++))
echo $var1


#String Length
var1='Hello World'
echo ${#var1}

#https://ryanstutorials.net/bash-scripting-tutorial/bash-arithmetic.php
