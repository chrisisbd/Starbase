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

package org.lmn.fc.ui.choosers.impl;


import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.choosers.ChooserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.File;


/***************************************************************************************************
 * PathNameChooser.
 */

public class PathNameChooser extends AbstractChooser
                             implements ChooserInterface
    {
    // String Resources
    private static final String DIALOG_TITLE = "Select a directory";
    private static final String TOOLTIP_APPROVE = "Select the directory";

    private final JFileChooser fc;


    /***********************************************************************************************
     * PathNameChooser.
     *
     * @param obsinstrument
     * @param font
     * @param colourforeground
     * @param defaultvalue
     */

    public PathNameChooser(final ObservatoryInstrumentInterface obsinstrument,
                           final FontInterface font,
                           final ColourInterface colourforeground,
                           final String defaultvalue)
        {
        super(obsinstrument, font, colourforeground, defaultvalue);

        fc = new JFileChooser();

        getChooser().setDialogTitle(DIALOG_TITLE);
        getChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        getChooser().setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
        getChooser().setFont(UIComponentPlugin.DEFAULT_FONT.getFont());

        getChooser().setApproveButtonText(ChooserInterface.BUTTON_SELECT);
        getChooser().setApproveButtonToolTipText(TOOLTIP_APPROVE);

        Utilities.setContainerHierarchyFonts(getChooser().getComponents(),
                                             UIComponentPlugin.DEFAULT_FONT);

        Utilities.setContainerHierarchyColours(getChooser().getComponents(),
                                               colourforeground,
                                               null);
        }


    /***********************************************************************************************
     * Show the Chooser, centred on the specified Component.
     *
     * @param component
     */

    public void showChooser(final Component component)
        {
        final String SOURCE = "PathNameChooser.showChooser() ";

        try
            {
            final File fileDefault;
            final int intState;

            fileDefault = new File(getDefaultValue());

            if ((fileDefault.exists())
                && (fileDefault.isDirectory()))
                {
                getChooser().setCurrentDirectory(fileDefault);
                }

            intState = getChooser().showOpenDialog(component);

            if (intState == JFileChooser.APPROVE_OPTION)
                {
                final File fileSelected;

                fileSelected = getChooser().getSelectedFile();

                if (fileSelected.isDirectory())
                    {
                    final String strPathname;

                    strPathname = fileSelected.getAbsolutePath();

                    // Normalise platform-specific slashes
                    setValue(strPathname.replace(System.getProperty("file.separator").charAt(0), '/'));
                    }
                else
                    {
                    setValue(getDefaultValue());
                    }
                }
            else
                {
                setValue(getDefaultValue());
                }
            }

        catch (SecurityException exception)
            {
            LOGGER.error(SOURCE + "SecurityException [exception=" + exception.getMessage() + "]");
            setValue(getDefaultValue());
            }

        catch (HeadlessException exception)
            {
            LOGGER.error(SOURCE + "HeadlessException [exception=" + exception.getMessage() + "]");
            setValue(getDefaultValue());
            }
        }


    /***********************************************************************************************
     * A convenience method to get the Chooser.
     *
     * @return JFileChooser
     */

    private JFileChooser getChooser()
        {
        return (this.fc);
        }
    }
