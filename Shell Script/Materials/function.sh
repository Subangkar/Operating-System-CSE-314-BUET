#!/bin/bash

#standard input
# echo -n 'Roll:'
# read roll
# echo $roll


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
	var=$1
	echo "$var In f"
	if [ $var -gt 10 ]; then
		exit
	fi
	((var++))
	f $var
}
f 1
