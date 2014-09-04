#!/bin/bash -x

## Script to auto set Starbase rxtx library 

MACHINE=`uname -m`

case $MACHINE in
unknown) /bin/mv $1/platform/linux/x86 $1/platform/linux/i386 ;;
i386|i486|i586|i686) /bin/mv $1/platform/linux/x86 $1/platform/linux/i386 ;;
esac

echo "${MACHINE}" > $1/platform/rxtx.log

