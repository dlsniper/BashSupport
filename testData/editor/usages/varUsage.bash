export <caret>a=1

# usages
$a
${a}
echo $a
echo "$a"
eval '$a'
$((a + 1))
[[ $a ]]

# no usages
'$a'
echo '$a'
a