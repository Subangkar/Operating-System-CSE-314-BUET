#!/bin/bash


# GLOBAL VARIABLES


_digit='[0-9]'
_integer="$_digit\{1,\}"
_stdname="[^_]\{1,\}"


RET_VAL_BOOL=1
RET_VAL=''

DIR_OUTPUT="output"
DIR_LOG="$DIR_OUTPUT/log"
DIR_TEMP="$DIR_OUTPUT/temp"
DIR_EXT="$DIR_OUTPUT/extra"

FILE_MARKS="$DIR_LOG/marks.txt"
FILE_ABS="$DIR_LOG/absent.txt"
FILE_ROSTER="CSE_322.csv"

##################
