REM Modify this line to point to the root of your own Java installation
set JAVA_HOME="C:\Program Files\Java\jre1.6.0_03"

REM You should not need to change anything below here
REM-------------------------------------------------------------------------------------------------

set Path=%PATH%;%JAVA_HOME%\bin;D:\Program Files\YourKit Java Profiler 6.0.16\bin\win32;

REM Note that SSL default is *true*, so set it to false...

REM Java options
REM -Xmx400m specifies the size of the heap in Mbyte
REM -Xms400m is how much of the heap is allocated at startup
REM Setting -Xms and -Xmx to the same value increases predictability by removing the most important sizing decision from the virtual machine.
REM On the other hand, the virtual machine can't compensate if you make a poor choice.
REM Be sure to increase the memory as you increase the number of processors, since allocation can be parallelized.
REM Unless you have problems with pauses, try granting as much memory as possible to the virtual machine.
REM The default size (64MB) is often too small.
REM -XX:NewSize=16m Default size of new generation

REM Options for incrementral mode garbage collection
REM See http://java.sun.com/docs/hotspot/gc5.0/gc_tuning_5.html#0.0.0.0.Incremental%20mode%7Coutline
REM See http://java.sun.com/docs/hotspot/gc1.4.2/faq.html
REM See http://www.sun.com/bigadmin/content/submitted/cms_gc_logs.html

REM -XX:+DisableExplicitGC
REM -XX:+UseConcMarkSweepGC
REM -XX:+CMSIncrementalMode
REM -XX:+CMSIncrementalPacing 
REM -XX:CMSIncrementalDutyCycleMin=0
REM -XX:CMSIncrementalDutyCycle=10
REM -XX:CMSMarkStackSize=8M
REM -XX:CMSMarkStackSizeMax=32M
REM -XX:CMSInitiatingOccupancyFraction=40

REM -XX:+PrintGCDetails
REM -XX:+PrintGCTimeStamps
REM -XX:-TraceClassUnloading
REM -XX:+PrintGCApplicationConcurrentTime
REM -XX:+PrintGCApplicationStoppedTime

REM java -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-TraceClassUnloading -Xms400m -Xmx500m -XX:NewSize=16m -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -agentlib:yjpagent -jar libraries/framework-common.jar loader.properties

java -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSInitiatingOccupancyFraction=40 -XX:CMSIncrementalDutyCycle=10 -XX:CMSMarkStackSize=8M -XX:CMSMarkStackSizeMax=32M -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-TraceClassUnloading -Xms700m -Xmx700m -XX:NewSize=16m -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -jar libraries/framework-common.jar loader.properties


REM This just keeps the DOS window visible when you have finished, so that you can see what happened
pause