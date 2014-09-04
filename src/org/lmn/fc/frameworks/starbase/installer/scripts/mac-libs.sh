#!/bin/sh

## Script to move the RXTX jar and Lib into the correct directory for Mac and set file permissions.

cp -f $1/Starbase.app/Contents/Resources/Java/platform/osx/x86_64/lib/librxtxSerial.jnilib /Library/Java/Extensions/
cp -f $1/Starbase.app/Contents/Resources/Java/platform/osx/x86_64/RXTXcomm.jar /Library/Java/Extensions/

# Correct file permissions on Non-Admin user installation.

# Correct file permissions on Non-Admin user installation.

mkdir  $1/Starbase.app/Contents/Resources/Java/archive

mkdir  $1/Starbase.app/Contents/Resources/Java/logs

find $1/Starbase.app/Contents/Resources -type f -print -exec chmod 666 {} \;

find $1/Starbase.app/Contents/Resources -type d -print -exec chmod 777 {} \;

