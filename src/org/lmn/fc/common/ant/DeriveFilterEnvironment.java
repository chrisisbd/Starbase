// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

import java.io.File;

//<!--<taskdef name="filter-environment"-->
//        <!--classname="org.lmn.fc.common.ant.DeriveFilterEnvironment"-->
//        <!--classpath="${framework.output.dir}"/>-->
//        <!-- Run the Filter helper class to find out various paths etc. -->
//<!--<filter-environment baseSrc="${framework.src.dir}"-->
//<!--baseClasses="${framework.output.dir}"-->
//<!--rootFilters="org/lmn/fc/common/datafilters/impl"/>-->
//
//<!-- The helper class fills in the following properties -->
//<!--<echo message="filter-base-src=${filter-base-src}"/>-->
//<!--<echo message="filter-base-classes=${filter-base-classes}"/>-->
//<!--<echo message="filter-root=${filter-root}"/>-->
//
//<!--<echo message="project-root=${project-root}"/>-->
//<!--<echo message="filter-path-src=${filter-path-src}"/>-->
//<!--<echo message="filter-path-classes=${filter-path-classes}"/>-->
//<!--<echo message="filter-package=${filter-package}"/>-->
//<!--<echo message="filter-list=${filter-list}"/>-->




/***************************************************************************************************
 * DeriveFilterEnvironment.
 */

public final class DeriveFilterEnvironment extends Task
    {
    // Properties to be used by the Task, these correspond to Ant parameters
    private static final String FILTER_BASE_SRC = "filter-base-src";
    private static final String FILTER_BASE_CLASSES = "filter-base-classes";
    private static final String FILTER_ROOT = "filter-root";

    private static final String PROJECT_ROOT = "project-root";
    private static final String FILTER_PATH_SRC = "filter-path-src";
    private static final String FILTER_PATH_CLASSES = "filter-path-classes";
    private static final String FILTER_PACKAGE = "filter-package";
    private static final String FILTER_LIST = "filter-list";

    private static final String CLASSES_ROOT = "/classes";

    private static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);
    private static final char FORWARD_SLASH = '/';


    /***********************************************************************************************
     * Set the base of the Source for the Filters, e.g. C:/Java/Starbase/src.
     *
     * @param basesource
     */

    public void setBaseSrc(final String basesource)
        {
        // Standardise the slashes from the beginning
        getProject().setNewProperty(FILTER_BASE_SRC, basesource.replace(FILE_SEPARATOR, FORWARD_SLASH));
        }


    /***********************************************************************************************
     * Set the base of the Classes for the Filters, e.g. C:/Java/Starbase/classes.
     *
     * @param baseclasses
     */

    public void setBaseClasses(final String baseclasses)
        {
        // Standardise the slashes from the beginning
        getProject().setNewProperty(FILTER_BASE_CLASSES, baseclasses.replace(FILE_SEPARATOR, FORWARD_SLASH));
        }


    /***********************************************************************************************
     * Set the root for the Filters, e.g. org/lmn/fc/common/datafilters/impl.
     *
     * @param rootfilters
     */

    public void setRootFilters(final String rootfilters)
        {
        // Standardise the slashes from the beginning
        getProject().setNewProperty(FILTER_ROOT, rootfilters.replace(FILE_SEPARATOR, FORWARD_SLASH));
        }


    /***********************************************************************************************
     *  Execute the Task.
     *
     * @throws BuildException
     */

    public void execute() throws BuildException
        {
        final String SOURCE = "DeriveFilterEnvironment.execute() ";

        if (getProject() != null)
            {
            final StringBuffer bufferBaseClasses;

            // Retrieve the basedir from the Project, and standardise the slashes
            // This could be C:/Java/Starbase/classes
            bufferBaseClasses = new StringBuffer(getProject().getProperty(FILTER_BASE_CLASSES));

            // Make sure we really do have the path to the classes root - does this path contain "/classes" ?
            if (bufferBaseClasses.indexOf(CLASSES_ROOT) > 0)
                {
                final StringBuffer bufferPathToFilters;
                final String strProjectRoot;
                String strFilterPath;
                String strFilterPackage;
                final File dirFilters;

                // Record the path to the Project, i.e. up to the start of /classes
                strProjectRoot = bufferBaseClasses.substring(0, bufferBaseClasses.indexOf(CLASSES_ROOT));

                // This could be e.g. C:/Java/Starbase
                getProject().setNewProperty(PROJECT_ROOT, strProjectRoot.replace(FILE_SEPARATOR, FORWARD_SLASH));

                // Filters are compiled into e.g. C:/Java/Starbase/classes/org/lmn/fc/common/datafilters/impl
                bufferPathToFilters = new StringBuffer();
                bufferPathToFilters.append(getProject().getProperty(FILTER_BASE_CLASSES));
                bufferPathToFilters.append(FILE_SEPARATOR);
                bufferPathToFilters.append(getProject().getProperty(FILTER_ROOT));

                // Point to the parent directory of the Filters
                strFilterPath = bufferPathToFilters.toString();

                // Make sure all slashes point the right way
                getProject().setNewProperty(FILTER_PATH_CLASSES,
                                            strFilterPath.replace(FILE_SEPARATOR, FORWARD_SLASH));

                // Correct the syntax for a package name
                strFilterPackage = strFilterPath.substring(strFilterPath.indexOf(CLASSES_ROOT) + CLASSES_ROOT.length() + 1);
                strFilterPackage = strFilterPackage.replace(FILE_SEPARATOR, FORWARD_SLASH);
                strFilterPackage = strFilterPackage.replace(FORWARD_SLASH, '.');

                getProject().setNewProperty(FILTER_PACKAGE,
                                            strFilterPackage);

                // And again for the Source
                bufferPathToFilters.setLength(0);
                bufferPathToFilters.append(getProject().getProperty(FILTER_BASE_SRC));
                bufferPathToFilters.append(FILE_SEPARATOR);
                bufferPathToFilters.append(getProject().getProperty(FILTER_ROOT));

                // Point to the parent directory of the Filters
                strFilterPath = bufferPathToFilters.toString();

                // Make sure all slashes point the right way
                getProject().setNewProperty(FILTER_PATH_SRC,
                                            strFilterPath.replace(FILE_SEPARATOR, FORWARD_SLASH));

                // Incoming
//                System.out.println(SOURCE + "Property filter-base-src=" + getProject().getProperty(FILTER_BASE_SRC));
//                System.out.println(SOURCE + "Property filter-base-classes=" + getProject().getProperty(FILTER_BASE_CLASSES));
//                System.out.println(SOURCE + "Property filter-root=" + getProject().getProperty(FILTER_ROOT));

                // Derived
//                System.out.println(SOURCE + "Property project-root=" + getProject().getProperty(PROJECT_ROOT));
//                System.out.println(SOURCE + "Property filter-path-src=" + getProject().getProperty(FILTER_PATH_SRC));
//                System.out.println(SOURCE + "Property filter-path-classes=" + getProject().getProperty(FILTER_PATH_CLASSES));
//                System.out.println(SOURCE + "Property filter-package=" + getProject().getProperty(FILTER_PACKAGE));

                // Scan all Class files in FILTER_PATH_CLASSES for Filters
                dirFilters = new File(getProject().getProperty(FILTER_PATH_CLASSES));

                // http://ant.apache.org/manual/tutorial-tasks-filesets-properties.html
                // Lists as property values are not supported by Ant natively.

                if (dirFilters != null)
                    {
                    final File [] files;
                    final StringBuffer bufferFileNames;

                    files = dirFilters.listFiles();
                    bufferFileNames = new StringBuffer();

                    if (files != null)
                        {
                        boolean boolFirstFile;

                        boolFirstFile = true;

                        for (final File file : files)
                            {
                            if ((file != null)
                                && (file.isFile()))
                                {
                                if (file.getName().endsWith("Filter.class"))
                                    {
                                    if (boolFirstFile)
                                        {
                                        boolFirstFile = false;
                                        }
                                    else
                                        {
                                        bufferFileNames.append(",");
                                        }

                                    bufferFileNames.append(file.getName().substring(0, file.getName().length() - "class".length() - 1));
                                    }
                                }
                            }

                        // Now return the List for filenames
                        getProject().setNewProperty(FILTER_LIST,
                                                    bufferFileNames.toString());
                        }
                    else
                        {
                        throw new BuildException("No Filter files found");
                        }
                    }
                else
                    {
                    throw new BuildException("The Filter directory does not exist");
                    }
                }
            else
                {
                throw new BuildException("The Filter basedir does not contain '" + CLASSES_ROOT + "'");
                }
            }

        // ToDo check return from getProject() for no data
        if ((getProject() == null)
            || (getProject().getProperty(FILTER_BASE_SRC) == null)
            || (getProject().getProperty(FILTER_BASE_CLASSES) == null)
            || (getProject().getProperty(FILTER_ROOT) == null)
            || (getProject().getProperty(PROJECT_ROOT) == null)
            || (getProject().getProperty(FILTER_PATH_SRC) == null)
            || (getProject().getProperty(FILTER_PATH_CLASSES) == null)
            || (getProject().getProperty(FILTER_PACKAGE) == null)
            || (getProject().getProperty(FILTER_LIST) == null))
            {
            throw new BuildException("Unable to derive the Filter environment");
            }
        }
    }


