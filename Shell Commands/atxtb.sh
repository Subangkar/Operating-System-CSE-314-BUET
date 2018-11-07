x=`find . -name '*.txt'`
# `$x` >> stdout
echo $x
echo '>>'
grep -r -i -n a $x

# find . -name '*.txt' | grep -i -n -c a
