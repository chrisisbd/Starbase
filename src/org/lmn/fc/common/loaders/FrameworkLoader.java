// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
// Laurence Newell
// starbase@ukraa.com
// radio.telescope@btinternet.com
//
// This file is part of Starbase.
//
// Starbase is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Starbase is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Starbase.  If not, see http://www.gnu.org/licenses.

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  08-10-03    LMN created file from original Framework
//  17-10-03    LMN added LoginPanel
//  16-06-04    LMN added TimeZone
//  22-10-04    LMN added SQL controls
//  08-11-04    LMN added getSystemReport() from FrameworkData
//  09-11-04    LMN converted for single Framework bootstrap Query
//  28-11-05    LMN converted to a Singleton!
//  21-12-05    LMN making major changes for XML --> database persistence
//  11-03-06    LMN finally made new login work, from XML!
//  24-07-06    LMN added the EventLog!
//  07-10-06    LMN changing for identification of OperatingSystem
//  20-08-11    LMN changed structure of platform folders
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.common.loaders;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.os.OperatingSystem;
import org.lmn.fc.common.utilities.files.ClassPathLoader;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.dao.mysql.FrameworkMySqlDAO;
import org.lmn.fc.model.registry.InstallationFolder;
import sun.reflect.Reflection;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/***************************************************************************************************
 * The FrameworkLoader Singleton class is the root of the whole application.
 */

public final class FrameworkLoader implements FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkSingletons,
                                              ResourceKeys
    {
    private static final LoaderProperties PROPERTIES = LoaderProperties.getInstance();

    private volatile static FrameworkLoader LOADER_INSTANCE;

    // String Resources
    private static final String LOADER_NAME                     = "FrameworkLoader";
    private static final String VERSION                         = "1.30";

    private static final String MSG_LOADER_VERSION              = LOADER_NAME + " v";
    private static final String MSG_PLATFORM_UNKNOWN            = "Unable to identify the Operating System and Architecture";
    private static final String MSG_CLASS_LOADER                = "Unable to construct a ClassLoader";
    private static final String MSG_REGISTRATION_FAILED         = "Unable to Register Beans";
    private static final String MSG_BEAN_POOL_VALIDATION_FAILED = "Unable to validate the Framework Bean Pool";
    private static final String MSG_LOADER_HELP                 = "Please specify the properties file to be used";
    private static final String TITLE_LOADER_NO_PROPERTIES      = LOADER_NAME + " - missing Properties";
    private static final String MSG_MISSING_PROPERTIES          = "Properties file was missing [filename=";
    private static final String MSG_DEFAULT_PROPERTIES          = "continuing with default Properties";


    // TODO WARNING! Sun might remove sun.reflect.Reflection
    // TODO and so this won't work...
    private static final int INDEX_INTO_STACK = 3;

    private static final String ROOT_FOLDER = "./";


    /***********************************************************************************************
     * Create a new FrameworkLoader, connect to the database, and load the RegistryModel.
     *
     * @param args The first and only parameter should be the name of the configuration properties file
     */

    public static void main(final String[] args)
        {
        // This is where it all happens!
        if ((args == null)
            || (args.length != 1)
            || (args[0].length() == 0))
            {
            LOGGER.log("\n" + MSG_LOADER_VERSION + VERSION);
            LOGGER.log(MSG_LOADER_HELP);

            System.exit(0);
            }
        else
            {
            LOGGER.login(MSG_LOADER_VERSION + VERSION);

            LOGGER.login(LOADER_NAME + " os.name=" + System.getProperty("os.name") );
            LOGGER.login(LOADER_NAME + " os.arch=" + System.getProperty("os.arch") );
            LOGGER.login(LOADER_NAME + " user.home=" + System.getProperty(ResourceKeys.KEY_SYSTEM_USER_HOME) );
            LOGGER.login(LOADER_NAME + " user.dir=" + System.getProperty(ResourceKeys.KEY_SYSTEM_USER_DIR) );
            LOGGER.login(LOADER_NAME + " java.library.path=" + System.getProperty("java.library.path") );
            LOGGER.login(LOADER_NAME + " sun.boot.library.path=" + System.getProperty("sun.boot.library.path") );
            LOGGER.login(LOADER_NAME + " java.ext.dirs=" + System.getProperty("java.ext.dirs") );
            LOGGER.login(LOADER_NAME + " java.class.path=" + System.getProperty(PROPERTY_JAVA_CLASS_PATH) );

            loadFramework(InstallationFolder.LIBRARIES.getName(),
                          InstallationFolder.PLATFORM.getName(),
                          InstallationFolder.PLUGINS.getName(),
                          args);
            }
        }


    /***********************************************************************************************
     * Initialise the FrameworkLoader and load the Framework.
     * Load the Framework and Applications marked as <code>LoadAtStart</code>.
     * Run the Tasks marked 'RunAtStart'.
     * All Resources must be available in the Language specified in the Framework.
     *
     * @param frameworklibraries    The folder containing the framework libraries
     * @param frameworkplatform     The folder containing the framework platform-specific libraries
     * @param plugins               The folder containing the framework plugins
     * @param args                  arg[0] specifies the loader.properties file
     */

    private static void loadFramework(final String frameworklibraries,
                                      final String frameworkplatform,
                                      final String plugins,
                                      final String[] args)
        {
        final OperatingSystem os;

        // Attempt to read the Loader properties file from the root folder
        // Use defaults if any are missing
        loadProperties(args);

        // Identify the current operating system, to enable loading of any plugins and native libraries
        os = identifyOS(PROPERTIES.isMasterDebug());

        // Did we correctly identify an OperatingSystem?
        if (os != null)
            {
            URLClassLoader loader;
            final DataStore dataStore;

            // Use just one ClassLoader for all loading
            if (FrameworkLoader.class.getClassLoader() instanceof URLClassLoader)
                {
                loader = (URLClassLoader)FrameworkLoader.class.getClassLoader();
                }
            else
                {
                try
                    {
                    loader = new URLClassLoader(new URL[]{new URL("file:///dummy.jar")},
                                                FrameworkLoader.class.getClassLoader());
                    }

                catch (MalformedURLException exception)
                    {
                    // This should not occur!
                    loader = null;
                    }
                }

            if (loader != null)
                {
                LOGGER.login("Using ClassLoader " + loader.getClass().getName());
                ClassPathLoader.showClassLoaderSearchPaths(PROPERTIES.isMasterDebug());

                // ToDo Check that we can do dynamic loading via Reflection on ClassLoader etc.
                // by ensuring current version of Java is hackable...

                // Load the Framework libraries and platform-specific items

                // All JARs in dist/libraries
                loadLibraries(loader,
                              frameworklibraries,
                              FileUtilities.jar,
                              os.getClasspathSeparator(),
                              PROPERTIES.isMasterDebug());

                // All dll, so, jnilib from dist/platform/<os>/<architecture>/lib
                loadNativeLibraries(loader,
                                    frameworkplatform,
                                    os,
                                    System.getProperty("os.arch").toLowerCase(),
                                    PROPERTIES.isMasterDebug());

                // All JARs in platform/<os>/<architecture>  (usually only RXTXcomm.jar)
                loadLibraries(loader,
                              frameworkplatform
                                  + System.getProperty("file.separator")
                                  + os.getInstallationFolder().getName()
                                  + System.getProperty("file.separator")
                                  + System.getProperty("os.arch").toLowerCase(),
                              FileUtilities.jar,
                              os.getClasspathSeparator(),
                              PROPERTIES.isMasterDebug());

                // Load the Framework jar (in the root) on to the classpath
                // (the framework-common.jar is already loaded from dist/libraries as above)
                LOGGER.login(EMPTY_STRING);
                loadLibraries(loader,
                              ROOT_FOLDER,
                              FileUtilities.jar,
                              os.getClasspathSeparator(),
                              PROPERTIES.isMasterDebug());

                // Recursively load the Plugins and their libraries on to the classpath
                LOGGER.login("Loading plugins recursively [folder=" + plugins + "]");
                loadPluginsRecursively(loader,
                                       plugins,
                                       os,
                                       PROPERTIES.isMasterDebug());
                LOGGER.login(EMPTY_STRING);

                // Set up the FrameworkDatabase using the requested database and credentials
                DATABASE.setDatabaseOptions(PROPERTIES.getDatabaseOptions());

                // Attempt to connect to the requested database
                // or drop back to the default
                DATABASE.openConnection();

                //  ToDo work out if we are installing or loading from database
                // Install or reload the Framework and its Resources
                // Discover if the requested database already knows about this Framework...
                dataStore = DataStore.CONFIG;

                if (REGISTRY_MANAGER.registerBeans(PROPERTIES, dataStore))
                    {
                    // Validate the Framework installation bean pool before we go any further...
                    if (REGISTRY_MANAGER.validateBeanPool())
                        {
                        // Record the OperatingSystem discovered earlier
                        REGISTRY.getFramework().setOperatingSystem(os);

                        // We now have a valid Framework and persistent Logger,
                        // and we know how to make a RegistryModel for a specific User
                        MODEL_CONTROLLER.loginFramework(REGISTRY.getFramework());
                        }
                    else
                        {
                        // Stop here if the validation fails!
                        LOGGER.login(MSG_BEAN_POOL_VALIDATION_FAILED);
                        exitLoader();
                        }
                    }
                else
                    {
                    // Stop here if the registration fails!
                    LOGGER.login(MSG_REGISTRATION_FAILED);
                    exitLoader();
                    }
                }
            else
                {
                LOGGER.login(MSG_CLASS_LOADER);
                exitLoader();
                }
            }
        else
            {
            LOGGER.login(MSG_PLATFORM_UNKNOWN);
            exitLoader();
            }
        }


    /***********************************************************************************************
     * Identify the host Operating System.
     *
     * @param debug
     *
     * @return OperatingSystem
     */

    private static OperatingSystem identifyOS(final boolean debug)
        {
        String strOsName;
        String strOsArch;
        OperatingSystem os;

        // Return null if we can't find a recognisable OS
        os = null;

        // Determine the operating system and architecture
        strOsName = System.getProperty("os.name");
        strOsArch = System.getProperty("os.arch");

        if ((strOsName != null
            && strOsArch != null))
            {
            strOsName = strOsName.toLowerCase();
            strOsArch = strOsArch.toLowerCase();

            if (debug)
                {
                LOGGER.debug("os name=" + strOsName);
                LOGGER.debug("os arch=" + strOsArch);
                }

            // Try to identify the platform
            if ((strOsName.startsWith(OperatingSystem.LINUX.getName()))
                && (OperatingSystem.LINUX.getArchitectureList().contains(strOsArch)))
                {
                os = OperatingSystem.LINUX;
                }
            else if ((strOsName.startsWith(OperatingSystem.WINDOWS.getName()))
                && (OperatingSystem.WINDOWS.getArchitectureList().contains(strOsArch)))
                {
                os = OperatingSystem.WINDOWS;
                }
            else if ((strOsName.startsWith(OperatingSystem.APPLE_MAC.getName()))
                && (OperatingSystem.APPLE_MAC.getArchitectureList().contains(strOsArch)))
                {
                os = OperatingSystem.APPLE_MAC;
                }

            if (os != null)
                {
                LOGGER.login("Identified platform "
                                + "[os=" + strOsName + "] "
                                + "[architecture=" + strOsArch + "]");
                }
            }

        return (os);
        }


    /***********************************************************************************************
     * Load all native libraries in the platform-specific sub-folder of the specified folder,
     * i.e. platform/<os>/<architecture>/lib.
     * Windows DLL, Linux SO, Apple Mac JNILIB.
     *
     * @param loader
     * @param folder
     * @param os
     * @param architecture
     * @param debug
     */

    private static synchronized void loadNativeLibraries(final URLClassLoader loader,
                                                         final String folder,
                                                         final OperatingSystem os,
                                                         final String architecture,
                                                         final boolean debug)
        {
        // If we found a platform, load the libraries...
        if ((loader != null)
            && (folder != null)
            && (!EMPTY_STRING.equals(folder))
            && (os != null)
            && (architecture != null)
            && (!EMPTY_STRING.equals(architecture)))
            {
            String strLibraryToLoad;

            LOGGER.login("Loading native libraries"
                            + " [folder="
                                + folder
                                + System.getProperty("file.separator")
                                + os.getInstallationFolder().getName()
                                + System.getProperty("file.separator")
                                + architecture
                                + System.getProperty("file.separator")
                                + "lib"
                                + "]"
                            + " [type=" + os.getLibraryExt() + "]");

            strLibraryToLoad = EMPTY_STRING;

            try
                {
                final File dirLibrary;

                dirLibrary = new File(new File(folder),
                                      os.getInstallationFolder().getName()
                                          + System.getProperty("file.separator")
                                          + architecture
                                          + System.getProperty("file.separator")
                                          + "lib");

                // If this abstract pathname does not denote a directory,
                // then this method returns null.
                if (dirLibrary != null)
                    {
                    final File [] arrayFiles;

                    // System.load() Loads a code file with the specified filename
                    // from the local file system as a dynamic library.
                    // System.loadLibrary() Loads the system library specified by the libname argument.
                    // The manner in which a library name is mapped to the actual system library is system dependent.

                    // Absolute references in System.load() do not use java.library.path
                    // but later calls to System.loadLibrary() will!
                    // So... that is why we modify java.library.path

                    System.setProperty(PROPERTY_JAVA_LIBRARY_PATH,
                                       System.getProperty(PROPERTY_JAVA_LIBRARY_PATH)
                                                               + os.getClasspathSeparator()
                                                               + dirLibrary.getAbsolutePath());
                    ClassPathLoader.forceReloadOfJavaLibraryPaths();

                    if (debug)
                        {
                        LOGGER.debug(INDENT + "Native library path=" + dirLibrary.getAbsolutePath());
                        LOGGER.debug(INDENT + "Library ext=" + os.getLibraryExt());
                        }

                    ClassPathLoader.showClassLoaderSearchPaths(debug);

                    // If this abstract pathname does not denote a directory,
                    // then this method returns null.
                    // The array will be empty if the directory is empty
                    arrayFiles = dirLibrary.listFiles();

                    if ((arrayFiles != null)
                        && (arrayFiles.length > 0))
                        {
                        for (final File library : arrayFiles)
                            {
                            strLibraryToLoad = EMPTY_STRING;

                            // List all platform-specific libraries in the folder
                            // Ignore everything else...
                            if ((library != null)
                                && (library.isFile())
                                && (library.getName().toLowerCase().endsWith(os.getLibraryExt()))
                                && (library.canRead()))
                                {
                                // Check the third item on the stack to get the ClassLoader
                                // and check that it is the one we think it is...

                                // TODO WARNING! Sun might remove sun.reflect.Reflection
                                // TODO and so this won't work...
                                if ((Reflection.getCallerClass(INDEX_INTO_STACK) != null)
                                    && (loader.equals(Reflection.getCallerClass(INDEX_INTO_STACK).getClassLoader())))
                                    {
                                    // We must use the SAME ClassLoader as for the JARs!
                                    // System.load() uses the **caller's** ClassLoader
                                    // Absolute references in System.load() do not use java.library.path
                                    // but later calls to System.loadLibrary() will!
                                    // So... that is why we modify java.library.path

                                    LOGGER.login(INDENT + "Attempt to load [" + os.getLibraryExt() + EQUALS + library + "]");

                                    strLibraryToLoad = library.getAbsolutePath();
                                    System.load(strLibraryToLoad);
                                    ClassPathLoader.showClassLoaderSearchPaths(debug);

                                    LOGGER.login(INDENT + "Loaded OK! [" + os.getLibraryExt() + EQUALS + library + "]");
                                    }
                                else
                                    {
                                    LOGGER.login(INDENT + "--------------------------------------------------------------------------------------------------");
                                    LOGGER.login(INDENT + "WARNING! Reflection failed to find ClassLoader [path=" + library.getAbsolutePath() + "]");
                                    LOGGER.login(INDENT + "Please notify Starbase author immediately on starbase@ukraa.com!");
                                    LOGGER.login(INDENT + "--------------------------------------------------------------------------------------------------");
                                    }
                                }
                            }
                        }
                    else
                        {
                        LOGGER.login(INDENT + "WARNING! Library folder is empty");
                        }
                    }
                else
                    {
                    LOGGER.login(INDENT + "WARNING! Attempting to load libraries from an invalid folder");
                    }
                }

            catch (UnsatisfiedLinkError exception)
                {
                LOGGER.login(INDENT + "WARNING! UnsatisfiedLinkError during load of native library " + strLibraryToLoad);
                ClassPathLoader.showClassLoaderSearchPaths(LOADER_PROPERTIES.isMasterDebug());
                }

            catch (SecurityException exception)
                {
                LOGGER.login(INDENT + "WARNING! SecurityException during load of native library " + strLibraryToLoad);
                }

            catch (NullPointerException exception)
                {
                LOGGER.login(INDENT + "WARNING! NullPointerException during load of native library " + strLibraryToLoad);
                }
            }
        else
            {
            LOGGER.login(INDENT + "WARNING! Unable to load native libraries");
            }
        }


    /***********************************************************************************************
     * Load all libraries in the specified folder with the specified file extension.
     *
     * @param loader
     * @param folder
     * @param extension
     * @param classpathseparator
     * @param debug
     */

    private static void loadLibraries(final URLClassLoader loader,
                                      final String folder,
                                      final String extension,
                                      final String classpathseparator,
                                      final boolean debug)
        {
        final File dir;
        final File [] arrayFiles;

        if ((loader != null)
            && (folder != null)
            && (!EMPTY_STRING.equals(folder))
            && (extension != null)
            && (!EMPTY_STRING.equals(extension)))
            {
            LOGGER.login("Loading libraries [folder=" + folder + "] [type=" + extension + "]");

            dir = new File(folder);

            if ((dir.exists())
                && (dir.isDirectory()))
                {
                // If this abstract pathname does not denote a directory,
                // then this method returns null.
                // The array will be empty if the directory is empty
                arrayFiles = dir.listFiles();

                if ((arrayFiles != null)
                    && (arrayFiles.length > 0))
                    {
                    // List all libraries (usually JARs) in the folder
                    // Ignore everything else...
                    for (final File library : arrayFiles)
                        {
                        if ((library != null)
                            && (library.isFile())
                            && (library.getName().endsWith(extension)))
                            {
                            try
                                {
                                LOGGER.login(INDENT + extension + EQUALS + library);

                                ClassPathLoader.addFileToClassLoader(loader, library);
                                System.setProperty(PROPERTY_JAVA_CLASS_PATH,
                                                   System.getProperty(PROPERTY_JAVA_CLASS_PATH)
                                                                           + classpathseparator
                                                                           + library);

                                // Handle the special cases of loading the **Framework** and its Javadoc
                                // in order to get version.number, build.number and build.status
                                readRootConfiguration(folder,
                                                      library,
                                                      FileUtilities.SUFFIX_PLUGIN_JAR, true);
                                readRootConfiguration(folder,
                                                      library,
                                                      FileUtilities.SUFFIX_JAVADOC_JAR, false);
                                }

                            catch (IOException exception)
                                {
                                LOGGER.error(INDENT
                                                + "Unable to add library "
                                                + library
                                                + " [exception="
                                                + exception.getMessage()
                                                + "]");
                                }
                            }
                        }
                    }
                else
                    {
                    LOGGER.login(INDENT + "WARNING! Library folder is empty");
                    }
                }
            else
                {
                // Fail silently if the Instruments folder does not exist,
                // since for most Plugins it won't be there...
//                if (!folder.endsWith(RegistryModelPlugin.FOLDER_INSTRUMENTS))
//                    {
//                    LOGGER.error(INDENT + "Instruments folder does not exist [" + folder + "]");
//                    }

                LOGGER.error(INDENT + "Libraries folder does not exist [" + folder + "]");
                }

            LOGGER.debug(debug, "java.class.path=" + System.getProperty(PROPERTY_JAVA_CLASS_PATH));
            }
        else
            {
            LOGGER.login(INDENT + "Unable to load libraries");
            }
        }


    /***********************************************************************************************
     * Recursively load all plugins from the specified folder, and all child folders.
     *
     * @param loader
     * @param folder
     * @param os
     * @param debug
     */

    private static void loadPluginsRecursively(final URLClassLoader loader,
                                               final String folder,
                                               final OperatingSystem os,
                                               final boolean debug)
        {
        if ((loader != null)
            && (folder != null)
            && (!EMPTY_STRING.equals(folder))
            && (folder.endsWith(InstallationFolder.PLUGINS.getName())))
            {
            final File dir;
            final File [] files;

            dir = new File(folder);

            if ((dir != null)
                && (dir.exists())
                && (dir.isDirectory()))
                {
                // If this abstract pathname does not denote a directory,
                // then this method returns null.
                files = dir.listFiles();

                if (files != null)
                    {
                    // List all *subdirectories* in the Plugins folder
                    // Each subdirectory may contain a child plugin
                    // Ignore everything else...
                    for (final File plugindir : files)
                        {
                        if ((plugindir != null)
                            && (plugindir.isDirectory()))
                            {
                            try
                                {
                                final File [] pluginrootfiles;
                                boolean boolFoundPlugin;
                                boolean boolFoundJavadoc;
                                StringBuffer subfolder;
                                StringBuffer pluginName;

                                // There should be only one plugin and one javadoc in the plugin root
                                // Scan all files, and look for JARs
                                boolFoundPlugin = false;
                                boolFoundJavadoc = false;
                                subfolder = new StringBuffer();
                                pluginName = new StringBuffer();

                                // For each plugin folder, list all jars in the root
                                pluginrootfiles = plugindir.listFiles();

                                for (final File rootfile : pluginrootfiles)
                                    {
                                    Manifest manifest;
                                    Attributes mainAttributes;
                                    URL urlJar;
                                    JarURLConnection connJar;

                                    //--------------------------------------------------------------
                                    // Check for the Plugin JAR

                                    if ((!boolFoundPlugin)
                                        && (rootfile != null)
                                        && (rootfile.isFile())
                                        && (rootfile.getName().endsWith(FileUtilities.SUFFIX_PLUGIN_JAR)))
                                        {
                                        //System.out.println("check for plugin: rootfile=" + rootfile);

                                        // See if we can read the manifest for this plugin
                                        // before loading the Jar
                                        urlJar = new URL("jar:file:"
                                                             + plugindir
                                                             + "/"
                                                             + rootfile.getName()
                                                             + "!/");

                                        if (urlJar != null)
                                            {
                                            //System.out.println("URL=" + urlJar);

                                            connJar = (JarURLConnection)urlJar.openConnection();

                                            if (connJar != null)
                                                {
                                                String strBuildSpec;

                                                //System.out.println("connJar=" + connJar.getJarFileURL());
                                                manifest = connJar.getManifest();
                                                mainAttributes = manifest.getMainAttributes();
                                                //LOGGER.log(Attributes.Name.MAIN_CLASS + "=" + mainAttributes.getValue(Attributes.Name.MAIN_CLASS));

                                                try
                                                    {
                                                    final String strKey;

                                                    strKey = rootfile.getName().substring(0, rootfile.getName().length() - FileUtilities.SUFFIX_PLUGIN_JAR.length());

                                                    // IllegalArgumentException if the attribute name is invalid
                                                    REGISTRY.addVersionNumber(strKey, mainAttributes.getValue("Version-Number"));
                                                    REGISTRY.addBuildNumber(strKey, mainAttributes.getValue("Build-Number"));
                                                    REGISTRY.addBuildStatus(strKey, mainAttributes.getValue("Build-Status"));

                                                    strBuildSpec = " [key=" + strKey
                                                                           + "] [version.number=" + mainAttributes.getValue("Version-Number")
                                                                           + "] [build.number=" + mainAttributes.getValue("Build-Number")
                                                                           + "] [build.status=" + mainAttributes.getValue("Build-Status") + "]";
                                                    }

                                                catch (IllegalArgumentException exception)
                                                    {
                                                    strBuildSpec = EMPTY_STRING;
                                                    }

                                                // Todo check that the loaded class has the package/folders expected

                                                if (true)
                                                    {
                                                    // We must load only one plugin ending in -plugin.jar!
                                                    ClassPathLoader.addFileToClassLoader(loader, rootfile);
                                                    System.setProperty(PROPERTY_JAVA_CLASS_PATH,
                                                                       System.getProperty(PROPERTY_JAVA_CLASS_PATH)
                                                                                               + os.getClasspathSeparator()
                                                                                               + rootfile);
                                                    boolFoundPlugin = true;

                                                    // Separate each Plugin load sequence
                                                    LOGGER.login(EMPTY_STRING);
                                                    LOGGER.login("Loading Plugin");
                                                    LOGGER.login(INDENT
                                                                     + FileUtilities.jar
                                                                     + EQUALS
                                                                     + plugindir
                                                                     + System.getProperty("file.separator")
                                                                     + rootfile.getName());
                                                    LOGGER.login(INDENT + "[plugin=" + rootfile.getName() + "]" + strBuildSpec);

                                                    // Derive the name of the child plugins sub-folder from the name of the rootfile just loaded
                                                    // This double-checks the structure!
                                                    // ... again, we do this only once, for the chosen plugin jar
                                                    subfolder = new StringBuffer(rootfile.getName());

                                                    // Remove the -plugins.jar suffix to leave <plugin_name>
                                                    subfolder.setLength(subfolder.length() - FileUtilities.SUFFIX_PLUGIN_JAR.length());
                                                    pluginName = new StringBuffer(subfolder.toString().toLowerCase());

                                                    // Prepend the folder in which this plugin is kept
                                                    // to give <folder>/<plugin_name>
                                                    subfolder.insert(0, System.getProperty("file.separator"));
                                                    subfolder.insert(0, folder);

                                                    // Create the folder for the next level down
                                                    // as <folder>/<plugin_name>/plugins
                                                    subfolder.append(System.getProperty("file.separator"));
                                                    subfolder.append(InstallationFolder.PLUGINS.getName());
                                                    }
                                                else
                                                    {
                                                    // ToDo Analyse fault
                                                    LOGGER.error(INDENT + "Plugin " + plugindir + " not loaded because ????");
                                                    }
                                                }
                                            }
                                        }

                                    //--------------------------------------------------------------
                                    // Check for the Javadoc JAR

                                    if ((!boolFoundJavadoc)
                                        && (rootfile != null)
                                        && (rootfile.isFile())
                                        && (rootfile.getName().endsWith(FileUtilities.SUFFIX_JAVADOC_JAR)))
                                        {
                                        //System.out.println("check for javadoc: rootfile=" + rootfile);

                                        // See if we can read the manifest for this Javadoc
                                        // before loading the Jar
                                        urlJar = new URL("jar:file:"
                                                             + plugindir
                                                             + "/"
                                                             + rootfile.getName()
                                                             + "!/");

                                        if (urlJar != null)
                                            {
                                            //System.out.println("javadoc URL=" + urlJar);
                                            connJar = (JarURLConnection)urlJar.openConnection();

                                            if (connJar != null)
                                                {
                                                String strBuildSpec;

                                                //System.out.println("connJar=" + connJar.getJarFileURL());
                                                manifest = connJar.getManifest();
                                                mainAttributes = manifest.getMainAttributes();

                                                try
                                                    {
                                                    strBuildSpec = " [version.number=" + mainAttributes.getValue("Version-Number")
                                                                       + "] [build.number=" + mainAttributes.getValue("Build-Number")
                                                                       + "] [build.status=" + mainAttributes.getValue("Build-Status") + "]";
                                                    }

                                                catch (IllegalArgumentException exception)
                                                    {
                                                    strBuildSpec = EMPTY_STRING;
                                                    }

                                                if (true)
                                                    {
                                                    // We must load only one plugin ending in -javadoc.jar!
                                                    ClassPathLoader.addFileToClassLoader(loader, rootfile);
                                                    boolFoundJavadoc = true;
                                                    LOGGER.login(INDENT + "[javadoc=" + rootfile.getName() + "]" + strBuildSpec);
                                                    }
                                                else
                                                    {
                                                    LOGGER.error(INDENT + "Javadoc in " + plugindir + " not loaded because ????");
                                                    }
                                                }
                                            else
                                                {
                                                LOGGER.login(INDENT + "Could not connect to javadoc jar");
                                                }
                                            }
                                        }
                                    }

                                // If we found a valid -plugin.jar and -javadoc.jar??
                                // load the Libraries, NativeLibraries and child Plugins
                                if ((boolFoundPlugin)
                                    && (subfolder.length() > 0)
                                    && (pluginName.length() > 0))
                                    // TODO REVIEW JAVADOC && (boolFoundJavadoc))
                                    {
                                    final String strPath;

                                    strPath = folder
                                                  + System.getProperty("file.separator")
                                                  + pluginName
                                                  + System.getProperty("file.separator");

                                    // Load the Plugin Libraries from the libraries folder
                                    loadLibraries(loader,
                                                  strPath + InstallationFolder.LIBRARIES.getName(),
                                                  FileUtilities.jar,
                                                  os.getClasspathSeparator(),
                                                  debug);

                                    // The special case for Observatory.Instruments
//                                    loadLibraries(loader,
//                                                  strPath + RegistryModelPlugin.FOLDER_INSTRUMENTS,
//                                                  os,
//                                                  FileUtilities.jar,
//                                                  debug);

                                    // Load the Plugin NativeLibraries from the platform folder
                                    loadNativeLibraries(loader,
                                                        strPath + InstallationFolder.PLATFORM.getName(),
                                                        os,
                                                        System.getProperty("os.arch").toLowerCase(),
                                                        debug);

                                    // Now recursively load the plugin's child plugins
                                    loadPluginsRecursively(loader,
                                                           subfolder.toString().toLowerCase(),
                                                           os,
                                                           debug);
                                    }
                                }

                            catch (IOException exception)
                                {
                                throw new FrameworkException("Unable to load plugin in [" + folder + "]",
                                                             exception);
                                }
                            }
                        }

                    //LOGGER.log("java.class.path=" + System.getProperty(PROPERTY_JAVA_CLASS_PATH));
                    }
                else
                    {
                    LOGGER.login(INDENT + "No plugin Jar to load in [" + folder + "]");
                    }
                }
            else
                {
                LOGGER.login(INDENT + "Plugin folder does not exist [" + folder + "]");
                }
            }
        else
            {
            LOGGER.login(INDENT + "Unable to load Plugins");
            }
        }


    /***********************************************************************************************
     * Read the Version Number, Build Number from a library in the root folder.
     * Optionally record the version.number, build.number and build.status in the Registry.
     *
     * @param folder
     * @param library
     * @param type
     * @param register
     *
     * @throws IOException
     */

    private static void readRootConfiguration(final String folder,
                                              final File library,
                                              final String type,
                                              final boolean register) throws IOException
        {
        if ((ROOT_FOLDER.equals(folder))
            && (library.getName().endsWith(type)))
            {
            final Manifest manifest;
            final Attributes mainAttributes;
            final URL urlJar;
            final JarURLConnection connJar;

            urlJar = new URL("jar:file:"
                                 + ROOT_FOLDER
                                 + library.getName()
                                 + "!/");

            if (urlJar != null)
                {
                connJar = (JarURLConnection)urlJar.openConnection();

                if (connJar != null)
                    {
                    String strBuildSpec;

                    manifest = connJar.getManifest();
                    mainAttributes = manifest.getMainAttributes();

                    try
                        {
                        final String strKey;

                        strKey = library.getName().substring(0, library.getName().length() - type.length());
                        strBuildSpec = " [key=" + strKey
                                       + "] [version.number=" + mainAttributes.getValue("Version-Number")
                                       + "] [build.number=" + mainAttributes.getValue("Build-Number")
                                       + "] [build.status=" + mainAttributes.getValue("Build-Status") + "]";

                        if (register)
                            {
                            REGISTRY.addVersionNumber(strKey, mainAttributes.getValue("Version-Number"));
                            REGISTRY.addBuildNumber(strKey, mainAttributes.getValue("Build-Number"));
                            REGISTRY.addBuildStatus(strKey, mainAttributes.getValue("Build-Status"));
                            LOGGER.login(INDENT + "[plugin=" + library.getName() + "]" + strBuildSpec);
                            }
                        }

                    catch (IllegalArgumentException e)
                        {
                        //System.out.println("IllegalArgumentException Build-Number not found");
                        strBuildSpec = EMPTY_STRING;
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Attempt to read the Properties file
     * <br>Use defaults for any missing properties
     * <br>The user could be told of missing properties...
     *
     * @param args The properties filename relative to the application root
     */

    private static void loadProperties(final String[] args)
        {
        PropertyResourceBundle resourceBundle;
        final boolean debug;

        LOGGER.login("Loading properties from [" + args[0] + "]");
        resourceBundle= null;

        // Open the Properties file
        try
            {
            resourceBundle = new PropertyResourceBundle(new FileInputStream(args[0]));
            }

        catch (IOException exception)
            {
            final String [] strMessage =
                {
                MSG_MISSING_PROPERTIES + args[0] + FrameworkMetadata.TERMINATOR,
                MSG_DEFAULT_PROPERTIES
                };

            // The Properties file was missing, so just use the default values
            JOptionPane.showMessageDialog(null,
                                          strMessage,
                                          TITLE_LOADER_NO_PROPERTIES,
                                          JOptionPane.ERROR_MESSAGE);
            }

        try
            {
            PROPERTIES.setJmxUsername(resourceBundle.getString(KEY_JMX_USERNAME));
            PROPERTIES.setJmxPassword(resourceBundle.getString(KEY_JMX_PASSWORD));
            PROPERTIES.setJmxPort(resourceBundle.getString(KEY_JMX_PORT));

            FrameworkDatabase.configureDatabaseOptions(PROPERTIES.getDatabaseOptions(),
                                                       resourceBundle);

            PROPERTIES.setLoadFrameworkQuery(resourceBundle.getString(FrameworkMySqlDAO.SELECT_FRAMEWORK_LOADATSTART));

            PROPERTIES.setMasterDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG)));
            PROPERTIES.setStaribusDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG_STARIBUS)));
            PROPERTIES.setStarinetDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG_STARINET)));
            PROPERTIES.setTimingDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG_TIMING)));
            PROPERTIES.setStateDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG_STATE)));
            PROPERTIES.setMetadataDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG_METADATA)));
            PROPERTIES.setChartDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG_CHART)));
            PROPERTIES.setThreadsDebug(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_DEBUG_THREADS)));
            PROPERTIES.setCommandMacros(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_COMMAND_MACROS)));
            PROPERTIES.setToolbarDisplayed(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_TOOLBAR)));
            PROPERTIES.setValidationXML(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_VALIDATION_XML)));
            PROPERTIES.setCommandVariant(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_COMMAND_VARIANT)));
            PROPERTIES.setSqlTrace(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_SQL_TRACE)));
            PROPERTIES.setSqlTiming(Boolean.valueOf(resourceBundle.getString(KEY_ENABLE_SQL_TIMING)));
            }

        catch (NullPointerException exception)
            {
            LOGGER.error("NullPointerException in loadProperties.readResources() " + exception);
            exitLoader();
            }

        catch (MissingResourceException exception)
            {
            LOGGER.error("MissingResourceException in loadProperties.readResources() " + exception);
            exitLoader();
            }

        catch (ClassCastException exception)
            {
            LOGGER.error("ClassCastException in loadProperties.readResources() " + exception);
            exitLoader();
            }


        // Do some debugging...
        debug = PROPERTIES.isMasterDebug();

        LOGGER.debug(debug, INDENT + PREFIX + KEY_JMX_USERNAME + EQUALS + PROPERTIES.getJmxUsername() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_JMX_PASSWORD + EQUALS + PROPERTIES.getJmxPassword() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_JMX_PORT + EQUALS + PROPERTIES.getJmxPort() + TERMINATOR);

        LOGGER.debug(debug, INDENT + PREFIX + FrameworkMySqlDAO.SELECT_FRAMEWORK_LOADATSTART + EQUALS + PROPERTIES.getLoadFrameworkQuery() + TERMINATOR);

        PROPERTIES.getDatabaseOptions().debugOptions(debug);

        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG + EQUALS + PROPERTIES.isMasterDebug() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG_STARIBUS + EQUALS + PROPERTIES.isStaribusDebug() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG_STARINET + EQUALS + PROPERTIES.isStarinetDebug() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG_TIMING + EQUALS + PROPERTIES.isTimingDebug() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG_STATE  + EQUALS + PROPERTIES.isStateDebug() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG_METADATA  + EQUALS + PROPERTIES.isMetadataDebug() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG_CHART  + EQUALS + PROPERTIES.isChartDebug() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_DEBUG_THREADS  + EQUALS + PROPERTIES.isThreadsDebug() + TERMINATOR);

        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_COMMAND_MACROS + EQUALS + PROPERTIES.isCommandMacros() + TERMINATOR);

        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_VALIDATION_XML + EQUALS + PROPERTIES.isValidationXML() + TERMINATOR);

        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_COMMAND_VARIANT + EQUALS + PROPERTIES.isCommandVariant() + TERMINATOR);

        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_SQL_TRACE + EQUALS + PROPERTIES.isSqlTrace() + TERMINATOR);
        LOGGER.debug(debug, INDENT + PREFIX + KEY_ENABLE_SQL_TIMING + EQUALS + PROPERTIES.isSqlTiming() + TERMINATOR);
        }


    /***********************************************************************************************
     * Get the Version of the FrameworkLoader.
     *
     * @return String version number (e.g. 1.00)
     */

    public static String getVersion()
        {
        return (VERSION);
        }


    /***********************************************************************************************
     * Leave the FrameworkLoader gracefully.
     */

    private static void exitLoader()
        {
        MODEL_CONTROLLER.disposeLoginDialog();
        DATABASE.closeConnection();
        System.exit(-1);
        }


    /***********************************************************************************************
     * The FrameworkLoader is a Singleton!
     *
     * @return FrameworkLoader
     */

    public static FrameworkLoader getInstance()
        {
        if (LOADER_INSTANCE == null)
            {
            synchronized (FrameworkLoader.class)
                {
                if (LOADER_INSTANCE == null)
                    {
                    LOADER_INSTANCE = new FrameworkLoader();
                    }
                }
            }

        return (LOADER_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the FrameworkLoader Singleton.
     */

    private FrameworkLoader()
        {
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
