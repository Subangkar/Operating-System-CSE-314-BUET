#!/bin/bash


. findAbsents.sh

if [ -d "output/extra" ]; then
    rm -d 'output/extra'
fi

mkdir 'output/extra'
