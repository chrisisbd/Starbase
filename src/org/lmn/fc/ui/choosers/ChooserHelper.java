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

package org.lmn.fc.ui.choosers;


import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.choosers.impl.AbstractChooser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/***************************************************************************************************
 * ChooserHelper.
 */

public final class ChooserHelper implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ObservatoryConstants
    {
    // String Resources
    private static final String CHOOSER_NOT_INSTANTIATED = "Unable to instantiate the Chooser";

    private static final int PARAMETER_COUNT = 4;


    /***********************************************************************************************
     * Instantiate a Chooser with the specified default value.
     *
     * @param classname
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     *
     * @return ChooserInterface
     */

    public static ChooserInterface instantiateChooser(final String classname,
                                                      final ObservatoryInstrumentInterface obsinstrument,
                                                      final FontInterface font,
                                                      final ColourInterface colourforeground,
                                                      final String defaultvalue)
        {
        ChooserInterface chooserInterface;

        chooserInterface = null;

        try
            {
            final Class classObject;
            final Class[] interfaces;
            final String strInterface;
            final boolean boolLoaded;

            classObject = Class.forName(classname);

            // Does the target implement the ChooserInterface?
            interfaces = classObject.getInterfaces();
            strInterface = ChooserInterface.class.getName();
            boolLoaded = false;

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0;
                         ((i < interfaces.length) && (!boolLoaded));
                         i++)
                        {
                        if (strInterface.equals(interfaces[i].getName()))
                            {
                            final Class superClass;

                            // We have found the correct interface
                            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "AbstractChooser: [" + classname + " implements " + strInterface + "]");

                            // Prove that the real Chooser is a subclass of AbstractChooser
                            superClass = classObject.getSuperclass();

                            if (superClass != null)
                                {
                                if (AbstractChooser.class.getName().equals(superClass.getName()))
                                    {
                                    final Constructor constructor;
                                    final Class[] parameterTypes =
                                        {
                                        ObservatoryInstrumentInterface.class,
                                        FontInterface.class,
                                        ColourInterface.class,
                                        String.class
                                        };

                                    // Now get hold of the Constructor
                                    constructor = classObject.getDeclaredConstructor(parameterTypes);

                                    if (constructor != null)
                                        {
                                        final Object objArguments[];

                                        objArguments = new Object[PARAMETER_COUNT];
                                        objArguments[0] = obsinstrument;
                                        objArguments[1] = font;
                                        objArguments[2] = colourforeground;
                                        objArguments[3] = defaultvalue;

                                        chooserInterface = (ChooserInterface)constructor.newInstance(objArguments);
                                        }
                                    else
                                        {
                                        LOGGER.error(CHOOSER_NOT_INSTANTIATED + "Constructor not found");
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(CHOOSER_NOT_INSTANTIATED + "Class is not a subclass of " + AbstractChooser.class.getName());
                                    }
                                }
                            else
                                {
                                LOGGER.error(CHOOSER_NOT_INSTANTIATED + "Class has no superclass");
                                }
                            }
                        else
                            {
                            LOGGER.error(CHOOSER_NOT_INSTANTIATED + "Incorrect interface " + interfaces[i].getName());
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(CHOOSER_NOT_INSTANTIATED + "Class is an interface only");
                    }
                }
            else
                {
                LOGGER.error(CHOOSER_NOT_INSTANTIATED + "No interfaces found");
                }
            }

        catch(NoSuchMethodException exception)
            {
            LOGGER.error(CHOOSER_NOT_INSTANTIATED + "NoSuchMethodException [classname=" + classname + "]");
            }

        catch(SecurityException exception)
            {
            LOGGER.error(CHOOSER_NOT_INSTANTIATED + "SecurityException [classname=" + classname + "]");
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(CHOOSER_NOT_INSTANTIATED + "InstantiationException [classname=" + classname + "]");
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(CHOOSER_NOT_INSTANTIATED + "IllegalAccessException [classname=" + classname + "]");
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(CHOOSER_NOT_INSTANTIATED + "IllegalArgumentException [classname=" + classname + "]");
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error(CHOOSER_NOT_INSTANTIATED + "InvocationTargetException [classname=" + classname + "]");
            }

        catch (ClassNotFoundException exception)
            {
            // Suppress empty classnames, because these are probably intentional
            if ((classname != null)
                && (!FrameworkStrings.EMPTY_STRING.equals(classname.trim())))
                {
                LOGGER.error(CHOOSER_NOT_INSTANTIATED + "ClassNotFoundException [classname=" + classname + "]");
                }
            }

        return (chooserInterface);
        }
    }
