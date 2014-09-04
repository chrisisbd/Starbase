#!/bin/sh
# Note that SSL default is *true*, so set it to false...

# You should not need to change anything below here
# -------------------------------------------------------------------------------------------------

# Note that SSL default is *true*, so set it to false...

# Java options
# -Xmx512m specifies the size of the heap in Mbyte
# -Xms512m is how much of the heap is allocated at startup
# Setting -Xms and -Xmx to the same value increases predictability by removing the most important sizing decision from the virtual machine.
# On the other hand, the virtual machine can't compensate if you make a poor choice.
# Be sure to increase the memory as you increase the number of processors, since allocation can be parallelized.
# Unless you have problems with pauses, try granting as much memory as possible to the virtual machine.
# The default size (64MB) is often too small.

# Options for incremental mode garbage collection
# See http://java.sun.com/docs/hotspot/gc5.0/gc_tuning_5.html#0.0.0.0.Incremental%20mode%7Coutline
# See http://java.sun.com/docs/hotspot/gc1.4.2/faq.html
# See http://www.sun.com/bigadmin/content/submitted/cms_gc_logs.html
# See http://java.sun.com/javase/6/docs/technotes/guides/vm/cms-6.html

# -Xmx512m
# -Xms512m
# -XX:NewSize=24m                 Default size of new generation
# -XX:MaxNewSize=24m
# -XX:+DisableExplicitGC
# -XX:+UseConcMarkSweepGC
# -XX:+CMSIncrementalMode
# -XX:+CMSIncrementalPacing
# -XX:CMSIncrementalDutyCycleMin=0
# -XX:CMSIncrementalDutyCycle=10
# -XX:CMSMarkStackSize=8M
# -XX:CMSMarkStackSizeMax=32M
# -XX:CMSInitiatingOccupancyFraction=40
# -XX:+UseCMSCompactAtFullCollection
# -XX:+ExplicitGCInvokesConcurrent

# -XX:+PrintGCDetails
# -XX:+PrintGCTimeStamps
# -XX:-TraceClassUnloading
# -XX:+PrintGCApplicationConcurrentTime
# -XX:+PrintGCApplicationStoppedTime

# Add these to the java line in order to see the Garbage Collector in action!
# -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

# Add this to get a very verbose set of debug messages
# -verbose: class

# Concurrent Mark Sweep Collector

# Add this to stop errors of: illegalargumentexception comparison method violates its general contract
# See: https://forums.oracle.com/forums/thread.jspa?threadID=2455538&start=15&tstart=0
# -Djava.util.Arrays.useLegacyMergeSort=true

STARBASE_LOGS=%{INSTALL_PATH}/logs

STARBASE_HOME=%{INSTALL_PATH}

DATE=`date +%Y%m%d.%N`

logconsole()
{

if [ -z $STARBASE_LOGS ]; then
        echo "No STARBASE_LOGS set. Exit"
        exit 1
fi

if [ -z $STARBASE_HOME ]; then
        echo "No STARBASE_HOME set. Exit"
        exit 1
fi


cd $STARBASE_LOGS

if [ $? != 0 ]; then
        echo "cd to STARBASE_LOGS failed. Exit"
        exit 1
else
        rm -f starbase.log

fi

cd $STARBASE_HOME

if [ $? != 0 ]; then
        echo "cd to STARBASE_HOME failed. Exit"
        exit 1
else
        java -Xms512m -Xmx512m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -jar libraries/framework-common.jar loader.properties 2>&1 | tee ${STARBASE_LOGS}/starbase.log
fi

}

console()
{
java -Xms512m -Xmx512m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -jar libraries/framework-common.jar loader.properties
}


which tee >> /dev/null

if [ $? -lt 1 ] ;
then
        logconsole ;
else
        echo -e "/n/n`date` -- tee not found defaulting to console output only../n/n"
        console ;
fi
