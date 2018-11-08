#!/bin/bash
# Write a shell script script1.sh  that will prompt the user and receive two integers as input. The output will be "yes" only if one of the numbers is even and the other is odd. Otherwise, output will be "no".

echo -n 'num 1:'
read num1
echo -n 'num 2:'
read num2

add=$((num1+num2))
echo $add

if [[ $((add%2)) -eq 0 ]]; then
  echo 'no'
else
  echo 'yes'
fi
