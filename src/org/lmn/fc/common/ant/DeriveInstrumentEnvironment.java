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

package org.lmn.fc.common.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.model.registry.InstallationFolder;

import java.io.File;


/***************************************************************************************************
 * DeriveInstrumentEnvironment.
 */

public class DeriveInstrumentEnvironment extends Task
    {
    // Properties to be used by the Task
    private static final String PROJECT_ROOT = "project-root";
    private static final String BASEDIR = "instrument-basedir";
    private static final String PATH = "instrument-path";
    private static final String PACKAGE = "instrument-package";
    private static final String FOLDER = "instrument-folder";
    private static final String INSTRUMENT_JARNAME = "instrument-jarname";

    private static final String SOURCE = "src";


    /***********************************************************************************************
     * Set the basedir for the Instrument.
     *
     * @param basedir
     */

    public void setBasedir(final String basedir)
        {
        getProject().setNewProperty(BASEDIR, basedir);
        }


    /***********************************************************************************************
     *  Execute the Task.
     *
     * @throws BuildException
     */

    public void execute() throws BuildException
        {
        final String strRoot;
        final String strPackage;
        final String strFolder;
        final StringBuffer buffer;
        final File dir;
        final File [] files;
        int intIndex;

        if (getProject() != null)
            {
            // Retrieve the basedir from the Project
            buffer = new StringBuffer(getProject().getProperty(BASEDIR));

            if (buffer.indexOf(System.getProperty("file.separator").charAt(0) + SOURCE) > 0)
                {
                // Root
                strRoot = buffer.substring(0, buffer.indexOf(System.getProperty("file.separator").charAt(0) + SOURCE));
                getProject().setNewProperty(PROJECT_ROOT, strRoot);

                // Path delimited by '/'
                strPackage = buffer.substring((getProject().getProperty(PROJECT_ROOT)
                                                        + System.getProperty("file.separator").charAt(0)
                                                        + SOURCE
                                                        + System.getProperty("file.separator").charAt(0)).length());
                getProject().setNewProperty(PATH,
                                            strPackage.replace(System.getProperty("file.separator").charAt(0), '/'));

                // Package delimited by '.'
                getProject().setNewProperty(PACKAGE,
                                            strPackage.replace(System.getProperty("file.separator").charAt(0), '.'));

                // Folder
                intIndex = buffer.indexOf(InstallationFolder.PLUGINS.getName());
                intIndex += InstallationFolder.PLUGINS.getName().length() + 1;

                strFolder = buffer.substring(intIndex);
                getProject().setNewProperty(FOLDER, strFolder);

//                System.out.println("Instrument project-root=" + getProject().getProperty(PROJECT_ROOT));
//                System.out.println("Instrument basedir=" + getProject().getProperty(BASEDIR));
//                System.out.println("Instrument package=" + getProject().getProperty(PACKAGE));
//                System.out.println("Instrument folder=" + getProject().getProperty(FOLDER));

                // Find the lower-case name of the current folder, to find the Instrument Jarname
                if (buffer.lastIndexOf(System.getProperty("file.separator")) + 1 >= 0)
                    {
                    // Scan all Java files, and find the one which doesn't end in 'Panel'
                    dir = new File(getProject().getProperty(BASEDIR));

                    if (dir != null)
                        {
                        files = dir.listFiles();

                        if (files != null)
                            {
                            for (final File file : files)
                                {
                                // Try to uniquely identify the Instrument classname for the Jar
                                // Skip non-Java files
                                if ((file != null)
                                    && (file.isFile())
                                    && (file.getName().endsWith(FileUtilities.java))
                                    && (file.getName().lastIndexOf(FileUtilities.java) - 1 >= 0))
                                    {
                                    final String strFilenameWithoutExtension;

                                    strFilenameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf(FileUtilities.java) - 1);

                                    // Find the one which doesn't end in 'Panel', 'Helper', 'Interface', or 'Decorator'
                                    // Check that the property is not set, otherwise it will try to override (and fail)
                                    // So this takes the *first*, e.g. in a case like StaribusVlfReceiver and StaribusVlfReceiverMK,
                                    // it only makes StaribusVlfReceiver, which is intended
                                    if ((getProject().getProperty(INSTRUMENT_JARNAME) == null)
                                        && (!strFilenameWithoutExtension.endsWith("Panel"))
                                        && (!strFilenameWithoutExtension.endsWith("Helper"))
                                        && (!strFilenameWithoutExtension.endsWith("Interface"))
                                        && (!strFilenameWithoutExtension.endsWith("Decorator")))
                                        {
                                        getProject().setNewProperty(INSTRUMENT_JARNAME, strFilenameWithoutExtension);
                                        //System.out.println("Instrument jarname=" + getProject().getProperty(INSTRUMENT_JARNAME));
                                        }
                                    }
                                }
                            }
                        else
                            {
                            throw new BuildException("There are no Instrument Java files in the basedir");
                            }
                        }
                    else
                        {
                        throw new BuildException("The Instrument basedir does not exist");
                        }
                    }
                else
                    {
                    throw new BuildException("The Instrument basedir is incorrectly terminated");
                    }
                }
            else
                {
                throw new BuildException("The Instrument basedir does not contain '" + SOURCE + "'");
                }

            // Check return from getProject() for no data
            if (getProject() == null)
                {
                throw new BuildException("Unable to derive the Instrument environment: Project unexpectedly NULL");
                }
            else if (getProject().getProperty(PROJECT_ROOT) == null)
                {
                throw new BuildException("Unable to derive the Instrument environment: PROJECT_ROOT NULL");
                }
            else if (getProject().getProperty(BASEDIR) == null)
                {
                throw new BuildException("Unable to derive the Instrument environment: BASEDIR NULL");
                }
            else if (getProject().getProperty(PATH) == null)
                {
                throw new BuildException("Unable to derive the Instrument environment: PATH NULL");
                }
            else if (getProject().getProperty(PACKAGE) == null)
                {
                throw new BuildException("Unable to derive the Instrument environment: PACKAGE NULL");
                }
            else if (getProject().getProperty(INSTRUMENT_JARNAME) == null)
                {
                throw new BuildException("Unable to derive the Instrument environment: INSTRUMENT_JARNAME NULL");
                }
            else if (getProject().getProperty(FOLDER) == null)
                {
                throw new BuildException("Unable to derive the Instrument environment: FOLDER NULL");
                }
            }
        else
            {
            throw new BuildException("Unable to derive the Instrument environment: Project is NULL");
            }
        }
    }
