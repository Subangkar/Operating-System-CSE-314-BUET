#!/bin/bash

deleteFolder() {
  if [ -d "$1" ]; then
    rm -d -r -f "$1"
  fi
}

# TODO createFolder's description
# @param  folderName
#
createFolder() {
  deleteFolder $1
  mkdir "$1"
}



deleteFile() {
  if [ -f "$1" ]; then
    rm -f "$1"
  fi
}

# TODO createFolder's description
# @param  fileName
#
createFile() {
  deleteFile $1
  touch "$1"
}


# @param contents fileName
appendToFile() {
  if [ ! -f "$2" ]; then
    touch $2
  fi
  echo "$1" >> "$2"
}


# @param contents fileName
deleteFromFileI() {
  newContents=`grep -i -v "$1" $2`;
  echo "$newContents" > $2
}


# TODO extractZip's description
# @param zipname
#
extractZip() {
  zipname="$1";
  path="$2"
  echo "Extracting: $zipname"
  unzip -qq -o "$zipname" -d "$path/"
}
