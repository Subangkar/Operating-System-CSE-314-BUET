#!/bin/bash
#Function
echo 'Listing files'
ls -l
echo 'Script Executed'

#system variables
echo 'User: '$USER
echo 'Path: '$PATH

#user defined variables
echo 'We are testing variable'
myvar=hello
echo $myvar
echo "Hello$myvar 123"

#doesn't replace value if we use single quote
echo 'Hello$myvar'

#\$ overrides the special meaning of $ symbol
echo \$myvar
echo "Hello\$myvar 123"

#standard input
#echo -n 'Roll:'
#read roll
#echo $roll

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


#if-then-else
var=11
if [ $var -lt 5 ]; then
	echo 'Variable has a value less than 5'
fi

if [ $((var+5)) -gt 15 ]; then
	echo 'Variable has a value greater than 10'
fi

#case statement in script
#No break statement required
var1=3
case $((var%5)) in
	0) echo 'Remainder 0'
	var1=$(( var1 + 3 ))
	echo $var1;;
	1) echo 'Remainder 1';;
	*) echo 'Remainder Greater than 1';;
esac

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

#Traditional for loop
for((i=0;i<10;i++))
do
	echo $i
done

#Shell Script Functions
hello()
{
	echo "Hello CSE"
}
hello

#Functions with arguments
factorial()
{
	n=$1
	res=1
	for((i=1;i<=n;i++))
	do
		res=$((res*i))
	done
	return $res;
}
factorial 5
echo $?
echo 'Finished Execution'

#Recursion
f()
{
	echo "In f"
	var=$1
	if [ $var -gt 10 ]; then
		exit
	fi
	((var++))
	f $var
}
f 1
