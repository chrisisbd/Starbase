Linux notes are a result of the work of Alan Melia a Linux tyro (!!) using Ubuntu 8.04

Some notes on Starbase for Linux:

----------------------------------------------------------------------------------------------------
Installation

    Java
    Ensure that you have a Java runtime environment installed on your PC.
    This *must* be Java v1.6 or later (also known as 'Java 6')
    If not, there's one available on the FTP server along with this distribution.
    Just download the JRE executable and run to install; accept all defaults unless you know what you are doing!

	On Linux download and install Sun Java 1.6 or higher from the Sun web site.
	You should be able to use the package manager to install this, but you may need to change the default "pointer".
	The easiest is to install the whole package.

	Once installed type:
	java --version  (in some distributions i.e. Ubuntu -version is required)

    This should return something like:
	java 1.6.0_06

    If it doesn't and the version shown is lower than 1.6 you will need to reset the "default" Java with:
	sudo update-alternatives --config java

    You will be presented with a numbered list and you just type the number you want to be the normally used Java.
    The current default is indicated by a star.

    It is recommended that at present you use 32-bit installs even if you have a 64-bit machine
    because there are still some issues with some drivers.

    If you have trouble installing it, don't worry you can install Java under your home folder.
    You will then need to edit the starbase.sh file.
    The command "Java" on the first uncommented line should be prefixed with the path through
    the JRE folder to the Java executable in the bin folder, for example:
	~/java6/jdk1.6.0_06/jre/bin/java

    Starbase
    Download dist-<build_id>.zip from the FTP server
    Unzip using something like WinZip
	Copy the /dist folder to wherever is convenient (e.g. the Desktop)
	Remember to use the option 'use folder names' to get the correct folder structure

	Configuration
	Modify starbase.sh as required for your environment
	***This should not be necessary unless you have had a custom installation of Java***
	***but you are advised to check that the first few lines of the file are correct***

	Modify /dist/loader.properties as required
	(e.g. To set Enable.Debug to true --  "Enable.Debug=true")
	***Again, this should not be necessary unless you want to experiment***
	The properties file is a plain text file; you may use any simple editor.

	Examine /dist/imports/frameworks.xml and configure for your environment
	    (Again use any plain text editor)
	    In particular the ExportsFolder pathname will fail at runtime if not correct when you try to export
	    You will need an 'exports' folder somewhere to test the file export functions
	    e.g. Windows: Change the XML <ExportsFolder>C:/Documents and Settings/USERNAME/Desktop/export</ExportsFolder> to suit

        Remember to use forward-slash (/) in all pathnames, regardless of the host platform!
        This is because the backslash will be treated differently by the XML file.

    Native libraries for serial ports
	Linux only **
	    Examine dist/platform/... for Linux native libraries
	        Find the serial library (.so) to suit your platform in dist/platform/linux_86/...
	        Copy the file(s) up one level into dist/platform/linux_86

    The default serial port names in the properties file in the plugins/observatory/imports folder
    will need to be changed to reflect the names for the serial ports recognised by Linux
    viz. /dev/ttyS3 in place of the Windows default name COM4.
    This needs to be done for any instrument using serial control,
    and the Terminal Emulator which can then be used to debug new instruments.
    The baud rate and other parameters may also need to be changed to reflect your requirements.
    Note that .XML files can be viewed in Firefox but NOT edited,
    you will need to "open with" a simple text editor NOT a Word Processor like Open_Office.

    Once these changes are complete you can attempt to run up Starbase by clicking the starbase.sh file.
    In Linux the first run-up is probably best initiated from the command line.
    Open a terminal, change directory to dist-buildxx, and type:
	./starbase.sh

    After a pause you will see loading information screaming up the terminal window.
    This will help to pin-point problems if it does not reach the "login" screen.
    If you have a problem you cannot spot then try again with:
	./starbase.sh > startup.txt


----------------------------------------------------------------------------------------------------
Login

    There are two users

        username=administrator       password=starbase
            This gives a split screen view, with all facilities visible
            This is intended for developers and seriously geeky technopohiles

        username=user                password=starbase
            This shows *only* the Observatory, permanently in full-screen mode
            Intended for those who just want to make observations!

----------------------------------------------------------------------------------------------------
Operation

On menuInstrument
	click on plugin starts plugin (silently) or shows a running UI
	ctrl-click on plugin offers dialog to stop plugin  !!THIS IS THE ONLY WAY TO STOP A PLUGIN!!

	click on runnable (background) task starts task or shows running UI
	ctrl-click on runnable (background) task offers dialog to stop task

	click on simple task shows UI

Administrator mode only
    On navigation tree on the left
	    click on plugin starts plugin (silently) or shows an already running UI
	    ctrl-click on plugin offers dialog to start or configure plugin

	    click on runnable (background) task offers popup to start/configure task or shows running UI

	    click on simple task offers popup to execute/configure task or shows UI

Note that stopping a plugin stops all child tasks (this may not be fully implemented yet...)

----------------------------------------------------------------------------------------------------
This part contributed by Mark Horne

Installation of Starbase on Ubuntu 10.04 Desktop Edition

I installed Ubuntu 10.04 using all the default options, however this doesn't include Sun Java 1.6.x

To make the system compatible with Starbase you'll need to add Sun Java and librxtx. The easist way to accomplish this as follows:

1.) open a terminal <Applications - Accessories - Terminal>

2.) sudo gedit /etc/apt/sources.list

3.) Scroll to end of file and add the following minus the quotes "deb <http://archive.canonical.com/> lucid partner" 

4.) Save the file 

This add the canonical repository to the system, to activate the new repository in the terminal do: 

5.) sudo apt-get update

Now you need to add the Sun Java JRE and librxtx packages.

6.) sudo apt-get install sun-java6 sun-java6-plugin sun-java6-fonts librxtx

7.) now install starbase as per starbase manual.

8.) Once starbase has been extracted to your installation path, 
    you'll need to change the serial port name in properties-StariBusPort.xml from COM3 to /dev/ttyS0
----------------------------------------------------------------------------------------------------




