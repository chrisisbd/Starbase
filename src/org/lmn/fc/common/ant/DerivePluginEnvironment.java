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

/***************************************************************************************************
 */

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.model.registry.InstallationFolder;

import java.io.File;


/***************************************************************************************************
 * DerivePluginEnvironment.
 */

public class DerivePluginEnvironment extends Task
    {
    // Properties to be used by the Task
    private static final String PROJECT_ROOT = "project-root";
    private static final String PLUGIN_BASEDIR = "plugin-basedir";
    private static final String PLUGIN_PATH = "plugin-path";
    private static final String PLUGIN_PACKAGE = "plugin-package";
    private static final String PLUGIN_FOLDER = "plugin-folder";
    private static final String PLUGIN_CLASSNAME = "plugin-classname";

    private static final String SOURCE = "src";


    /***********************************************************************************************
     * Set the basedir for the plugin.
     *
     * @param basedir
     */

    public void setBasedir(final String basedir)
        {
        getProject().setNewProperty(PLUGIN_BASEDIR, basedir);
        }


    /***********************************************************************************************
     *  Execute the Task.
     *
     * @throws BuildException
     */

    public void execute() throws BuildException
        {
        final String strRoot;
        final String strPluginPackage;
        final String strPluginFolder;
        final String strClassname;
        final StringBuffer buffer;
        final File dir;
        final File [] files;
        int intPluginsIndex;

        if (getProject() != null)
            {
            // Retrieve the basedir from the Project
            buffer = new StringBuffer(getProject().getProperty(PLUGIN_BASEDIR));

            if (buffer.indexOf(System.getProperty("file.separator").charAt(0) + SOURCE) > 0)
                {
                strRoot = buffer.substring(0, buffer.indexOf(System.getProperty("file.separator").charAt(0) + SOURCE));
                getProject().setNewProperty(PROJECT_ROOT, strRoot);

                strPluginPackage = buffer.substring((getProject().getProperty(PROJECT_ROOT)
                                                        + System.getProperty("file.separator").charAt(0)
                                                        + SOURCE
                                                        + System.getProperty("file.separator").charAt(0)).length());
                getProject().setNewProperty(PLUGIN_PATH,
                                            strPluginPackage.replace(System.getProperty("file.separator").charAt(0), '/'));

                getProject().setNewProperty(PLUGIN_PACKAGE,
                                            strPluginPackage.replace(System.getProperty("file.separator").charAt(0), '.'));

                intPluginsIndex = buffer.indexOf(InstallationFolder.PLUGINS.getName());
                intPluginsIndex += InstallationFolder.PLUGINS.getName().length() + 1;

                strPluginFolder = buffer.substring(intPluginsIndex);
                getProject().setNewProperty(PLUGIN_FOLDER, strPluginFolder);

        //        System.out.println("Property project-root=" + getProject().getProperty(PROJECT_ROOT));
        //        System.out.println("Property plugin-basedir=" + getProject().getProperty(PLUGIN_BASEDIR));
        //        System.out.println("Property plugin-package=" + getProject().getProperty(PLUGIN_PACKAGE));
//                System.out.println("Property plugin-folder=" + getProject().getProperty(PLUGIN_FOLDER));

                // Find the lower-case name of the current folder
                if (buffer.lastIndexOf(System.getProperty("file.separator")) + 1 >= 0)
                    {
                    strClassname = buffer.substring(buffer.lastIndexOf(System.getProperty("file.separator")) + 1);

                    // Scan all Java files, and find the one with the same name as the current folder
                    dir = new File(getProject().getProperty(PLUGIN_BASEDIR));

                    if (dir != null)
                        {
                        files = dir.listFiles();

                        if (files != null)
                            {
                            for (File file : files)
                                {
                                if ((file != null)
                                    && (file.isFile()))
                                    {
                                    final String strFilename;

                                    if (file.getName().endsWith(FileUtilities.java))
                                        {
                                        if (file.getName().lastIndexOf(FileUtilities.java) - 1 >= 0)
                                            {
                                            strFilename = file.getName().substring(0, file.getName().lastIndexOf(FileUtilities.java) - 1);

                                            if (strClassname.equalsIgnoreCase(strFilename))
                                                {
                                                getProject().setNewProperty(PLUGIN_CLASSNAME, strFilename);
                                                //System.out.println("Property plugin-classname=" + getProject().getProperty(PLUGIN_CLASSNAME));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        else
                            {
                            throw new BuildException("There are no Plugin Java files in the basedir");
                            }
                        }
                    else
                        {
                        throw new BuildException("The Plugin basedir does not exist");
                        }
                    }
                else
                    {
                    throw new BuildException("The Plugin basedir is incorrectly terminated");
                    }
                }
            else
                {
                throw new BuildException("The Plugin basedir does not contain '" + SOURCE + "'");
                }
            }

        // ToDo check return from getProject() for no data
        if ((getProject() == null)
            || (getProject().getProperty(PROJECT_ROOT) == null)
            || (getProject().getProperty(PLUGIN_BASEDIR) == null)
            || (getProject().getProperty(PLUGIN_PATH) == null)
            || (getProject().getProperty(PLUGIN_PACKAGE) == null)
            || (getProject().getProperty(PLUGIN_CLASSNAME) == null)
            || (getProject().getProperty(PLUGIN_FOLDER) == null))
            {
            throw new BuildException("Unable to derive the plugin environment");
            }
        }
    }


