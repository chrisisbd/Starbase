Starbase
========

Development Instructions.
============================

    We currently use IntelliJ Community Edition IDEA to build Starbase. 
    
    http://www.jetbrains.com/idea/download/   
    
    Note: The Framework.xml build file will not work natively with ANT from the commandline. 
    
    We're currently building against Oracle Java JDK 1.6.0_45 available from:
    
    http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html#jdk-6u45-oth-JPR
    
    Note: 
        1.) You will need to register with Oracle to download the JDK.
        2.) You can not use any other version of JDK such as OpenJDK
         
    
    
    
For your first build using IntelliJ IDEA you will need to make a couple of corrections to
framework.properties and the Projects Setting as detailed below.

    1.) Edit the file framework.properties and set the jdk.home.1.6 path to your JDK home
    
        e.g. jdk.home.1.6=/home/mark/opt/jdk1.6.0_45
        
    2.) If this is the first time you've used IntelliJ then you need to set you projects 
        JDK; press F4 to open the Projects Settings.  Set the which JDK you are using under 
        the 'Platform Settings' heading, SDKs. Use the green plus sign to select your JDK 
        installation. 
        
      
Files that must not be pushed to the master branches are as follows:

    1.) Framework.iws (This is a user specific file and is not to be shared.)
    
    2.) classes/ 
    
    3.) dist/
    
    4.)	src/org/lmn/fc/model/xmlbeans/
    
    5.)	xml/
    
Note regarding empty folders.

    Empty folders are by default ignored by git if you wish an empty folder to be included in your 
    commits and pushes you must add a .gitkeep file to the folder first. 
