#!/bin/bash

OLD_NAME=$1
NEW_NAME=$2

function renameMV
{
  mv drawable-$1/$OLD_NAME mipmap-$1/$NEW_NAME;
}

renameMV hdpi;
renameMV mdpi;
renameMV xhdpi;
renameMV xxhdpi;
renameMV xxxhdpi;
