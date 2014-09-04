REM Modify this line to point to the root of your own Java JRE installation
REM set JAVA_HOME="C:\Program Files\Java\jre6"

REM Comment out the above line and enable the line below for Windows 7
REM set JAVA_HOME="C:\Program Files (x86)\Java\jre6"

REM Note that if you use e.g. Windows Server, Java may not have been installed in ProgramFiles

REM Modify this with the location of the extracted Starbase distribution folder
REM SET SB_HOME="C:\Java\Starbase\dist"

REM CD %SB_HOME%

REM Set DOS Window width to 150
REM mode con:cols=150

REM You should not need to change anything below here
REM -------------------------------------------------------------------------------------------------

REM set Path=%PATH%;%JAVA_HOME%\bin;

REM Note that SSL default is *true*, so set it to false...

REM Java options
REM -Xmx512m specifies the size of the heap in Mbyte
REM -Xms512m is how much of the heap is allocated at startup
REM Setting -Xms and -Xmx to the same value increases predictability by removing the most important sizing decision from the virtual machine.
REM On the other hand, the virtual machine can't compensate if you make a poor choice.
REM Be sure to increase the memory as you increase the number of processors, since allocation can be parallelized.
REM Unless you have problems with pauses, try granting as much memory as possible to the virtual machine.
REM The default size (64MB) is often too small.

REM White Paper about tuning the Garbage Collector
REM http://java.sun.com/javase/technologies/hotspot/gc/gc_tuning_6.html

REM White Paper about Memory Management
REM http://java.sun.com/javase/technologies/hotspot/gc/memorymanagement_whitepaper.pdf

REM Options for incremental mode garbage collection
REM See http://java.sun.com/docs/hotspot/gc5.0/gc_tuning_5.html#0.0.0.0.Incremental%20mode%7Coutline
REM See http://java.sun.com/docs/hotspot/gc1.4.2/faq.html
REM See http://www.sun.com/bigadmin/content/submitted/cms_gc_logs.html
REM See http://java.sun.com/javase/6/docs/technotes/guides/vm/cms-6.html

REM Memory Configuration
REM -Xmx512m
REM -Xms512m
REM -XX:NewSize=24m                 Default size of new generation
REM -XX:MaxNewSize=24m

REM Parallel Garbage Collector
REM -XX:+UseParallelGC
REM -XX:+UseParallelOldGC

REM Concurrent Mark Sweep Collector
REM -XX:+UseConcMarkSweepGC
REM -XX:+CMSIncrementalMode
REM -XX:+CMSIncrementalPacing
REM -XX:CMSIncrementalDutyCycleMin=0
REM -XX:CMSIncrementalDutyCycle=10
REM -XX:CMSMarkStackSize=8M
REM -XX:CMSMarkStackSizeMax=32M
REM -XX:CMSInitiatingOccupancyFraction=40
REM -XX:+UseCMSCompactAtFullCollection

REM Miscellaneous
REM -XX:+DisableExplicitGC
REM -XX:+ExplicitGCInvokesConcurrent
REM -XX:-TraceClassUnloading
REM -XX:+PrintGCDetails
REM -XX:+PrintGCTimeStamps
REM -XX:+PrintGCApplicationConcurrentTime
REM -XX:+PrintGCApplicationStoppedTime

REM Add these to the java line in order to see the Garbage Collector in action!
REM -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

REM Add this to get a very verbose set of debug messages
REM -verbose: class

REM Concurrent Mark Sweep Collector 768MB Heap Size

REM Add this to stop errors of: illegalargumentexception comparison method violates its general contract
REM See: https://forums.oracle.com/forums/thread.jspa?threadID=2455538&start=15&tstart=0
REM -Djava.util.Arrays.useLegacyMergeSort=true

java -Xms768m -Xmx768m -XX:+UseConcMarkSweepGC -XX:+ExplicitGCInvokesConcurrent -XX:+CMSClassUnloadingEnabled -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -jar libraries/framework-common.jar loader.properties

REM This just keeps the DOS window visible when you have finished, so that you can see what happened
pause