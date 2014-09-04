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

// See: http://en.wikipedia.org/wiki/X86-64


package org.lmn.fc.common.os;

import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.model.registry.InstallationFolder;

import java.util.Arrays;
import java.util.List;


/***************************************************************************************************
 * Details of the OperatingSystem and Architecture for the supported platforms.
 */

public enum OperatingSystem
    {
    // BEWARE! If architectures are added to the arrays below, you MUST:
    //
    //      add appropriately named folders under ALL platform folders (there's quite a few!)
    //  OR
    //      write code to map the new names to the existing sub-folder names,
    //      if the contents will be identical

    WINDOWS ("windows",
             new String[]{"x86", "amd64", "x86_64"},
             InstallationFolder.PLATFORM_WINDOWS,
             FileUtilities.dll,
             ";"),

    LINUX ("linux",
           new String[]{"x86", "amd64", "x86_64", "i386", "i486", "i586", "i686", "ia64", "arm"},
           InstallationFolder.PLATFORM_LINUX,
           FileUtilities.so,
           ":"),

    APPLE_MAC ("mac os x",
               new String[]{"x86_64", "amd64"},
               InstallationFolder.PLATFORM_APPLE_MAC,
               FileUtilities.jnilib,
               ";");

    private final String strName;
    private final String[] arrayArchitecture;
    private final InstallationFolder installationFolder;
    private final String strLibraryExt;
    private final String strClasspathSeparator;


    /***********************************************************************************************
     * OperatingSystem.
     *
     * @param name
     * @param architecture
     * @param folder
     * @param libraryext
     * @param pathseparator
     */

    private OperatingSystem(final String name,
                            final String[] architecture,
                            final InstallationFolder folder,
                            final String libraryext,
                            final String pathseparator)
        {
        strName = name;
        arrayArchitecture = architecture;
        installationFolder = folder;
        strLibraryExt = libraryext;
        strClasspathSeparator = pathseparator;
        }


    /***********************************************************************************************
     * Get the Platform Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the Architecture as a List, to enable use of contains().
     *
     * @return List<String>
     */

    public List<String> getArchitectureList()
        {
        return (Arrays.asList(this.arrayArchitecture));
        }


    /***********************************************************************************************
     * Get the Platform Installation Folder.
     *
     * @return InstallationFolder
     */

    public InstallationFolder getInstallationFolder()
        {
        return (this.installationFolder);
        }


    /***********************************************************************************************
     * Get the Extension for the Library file for this platform.
     *
     * @return String
     */

    public String getLibraryExt()
        {
        return (this.strLibraryExt);
        }


    /***********************************************************************************************
     * Get the Classpath Separator for this platform.
     *
     * @return String
     */

    public String getClasspathSeparator()
        {
        return (this.strClasspathSeparator);
        }
    }
