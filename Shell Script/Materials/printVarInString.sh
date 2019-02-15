#!/bin/bash

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

