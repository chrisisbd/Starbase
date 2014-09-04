Some notes on Starbase for Mac OS X modified from the work of Alan Melia Linux ReadMe

----------------------------------------------------------------------------------------------------
Installation

    Java
    Java comes bundled with the Mac OS X system. Use the Software Update feature (available under
    the Apple menuInstrument) to check that you have the most up-to-date version of Java for your Mac. If an
    update is needed select ti for installation and the Apple updater will install it automatically.

    To see Java version open the terminal program. The terminal program is found in the
    Utilities folder inside the Applications folder.

    Once the terminal is open type:
    java -version

    After typing java -version in the terminal you will see something like this:

    java version "1.6.0_15"
    Java(TM) SE Runtime Environment (build 1.6.0_15-b03-219)
    Java HotSpot(TM) 64-Bit Server VM (build 14.1-b02-90, mixed mode)

   Starbase
    Download dist-<build_id>.zip from the FTP server
    Unzip it by double clicking on it.
    Rename the folder Starbase.
	Drag the dist folder to wherever it is convenient (e.g. your home).

	Configuration
	Modify starbase.sh as required for your environment
	*** this should not be necessary for a standard OS X installation.

	Modify /dist/loader.properties as required
	(e.g. To set Enable.Debug to true --  "Enable.Debug=true")
	***Again, this should not be necessary unless you want to experiment***
	The properties file is a plain text file; you may use any simple editor.

	Examine /dist/imports/frameworks.xml and configure for your environment
	    (Again use any plain text editor)
	    In particular the ExportsFolder pathname will fail at runtime if not correct when you try to export
	    You will need an 'exports' folder somewhere to test the file export functions.
	    Create a folder titled exports in your Home folder.
	    Change the XML <ExportsFolder>/Users/USERNAME/exports</ExportsFolder>

	    Note: Replace USERNAME with your user name in the path above.  If you are uncertain touch
	    your desktop to go into Finder. Under the GO Menu select Home. Your user name will be in the
	    Title bar next to the home icon.

	    Remember to use forward-slash (/) in all pathnames, regardless of the host platform!
            This is because the backslash will be treated differently by the XML file.

-------------------- Must have a better look at for USB workaround------------
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
----------------------------------------------------------------------------------------------------

	Starting Starbase
	Open the terminal program if it is not already open. Type cd and a space then, without hitting
	the return key, Drag the Starbase folder onto the terminal screen.  This will automatically copy
	the proper path without typing. You will see something like this:

	cd /Users/USERNAME/Starbase/

	Hit the return key. You are now inside the Starbase folder.
|	In Terminal, either: 
|	a. if you want to leave the file as editable by your preferred text editor (default application),
|	each time you run the script, type: bash starbase.sh
|	or
|	b. Permanently set the permissions of the starbase.sh file by typing: chmod u+x starbase.sh
|	This will turn its icon into the one for a Terminal-executable file but double-clicking the
|	icon will still open it in the editor, not run it.
|	You still need to tell Terminal the correct directory
|	In the terminal type: ./starbase.sh

	Hit the return key and the program will start with rows upon rows of information rapidly
	scrolling by. After the program loads you are presented with a GUI, Graphical User's Interace
	with a log in window.

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

