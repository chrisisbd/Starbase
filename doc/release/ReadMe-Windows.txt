Some notes on Starbase for Windows:

----------------------------------------------------------------------------------------------------
Installation

    Java
    Ensure that you have a Java runtime environment installed on your PC.
    This *must* be Java v1.6 or later (also known as 'Java 6')
    If not, there's one available on the FTP server along with this distribution.
    Just download the JRE executable and run to install; accept all defaults unless you know what you are doing!

    Starbase
    Download dist-<build_id>.zip from the FTP server
    Unzip using something like WinZip, or Windows XP's built in unzipper
	Copy the /dist folder to wherever is convenient (e.g. the Desktop)
	Remember to use the option 'use folder names' to get the correct folder structure

	Configuration
	Modify starbase.bat as required for your environment
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

