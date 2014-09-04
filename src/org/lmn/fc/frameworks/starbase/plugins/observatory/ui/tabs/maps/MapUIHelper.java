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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.maps;

import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.PointOfInterestType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.PointOfInterestHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MapUIComponentPlugin;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


/***************************************************************************************************
 * MapUIHelper.
 */

public final class MapUIHelper implements FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons,
                                          ObservatoryConstants
    {
    // String Resources
    private static final String TOOLTIP_POI_IMPORT = "Import Points of Interest";
    private static final String TOOLTIP_POI_REMOVE = "Remove Points of Interest";

    // Adjust the Toolbar to suit 24x24px Icons
    private static final int HEIGHT_TOOLBAR_ICON = 24;
    private static final int WIDTH_DROPDOWN = 100;


    /***********************************************************************************************
     * Create a drop-down of POI sources to Import.
     *
     * @param obsinstrument
     * @param map
     * @param font
     * @param colourforeground
     * @param colourbackground
     *
     * @return JComboBox
     */

    public static JComboBox createComboImportPOI(final ObservatoryInstrumentInterface obsinstrument,
                                                 final MapUIComponentPlugin map,
                                                 final FontInterface font,
                                                 final ColourInterface colourforeground,
                                                 final ColourInterface colourbackground)
        {
        final JComboBox comboImportPOI;
        final ActionListener choiceListener;

        comboImportPOI = new JComboBox();
        comboImportPOI.setFont(font.getFont());
        comboImportPOI.setForeground(colourforeground.getColor());
        comboImportPOI.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                               font,
                                                               colourforeground,
                                                               colourbackground));

        // Do NOT allow the combo box to take up all the remaining space!
        comboImportPOI.setPreferredSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboImportPOI.setMaximumSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboImportPOI.setAlignmentX(0);
        comboImportPOI.setToolTipText(TOOLTIP_POI_IMPORT);
        comboImportPOI.setEnabled(true);
        comboImportPOI.setEditable(false);

        // Enumerate the PointOfInterestType to build the combo box
        // First entry is blank, to force a selection
        comboImportPOI.addItem(EMPTY_STRING);
        comboImportPOI.addItem(PointOfInterestType.FRAMEWORK);
        comboImportPOI.addItem(PointOfInterestType.OBSERVATORY);
        comboImportPOI.addItem(PointOfInterestType.INSTRUMENT);
        comboImportPOI.setMaximumRowCount(4);
        comboImportPOI.setSelectedIndex(0);
        comboImportPOI.revalidate();

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final String SOURCE = "MapHelper.createComboImportPOI() actionPerformed() ";

                // This method is only ever called from the UI, so it is ok to show a MessageDialog
                if (InstrumentState.isOff(obsinstrument))
                    {
                    final String [] message =
                        {
                        "The Instrument must be switched on in order to import a POI.",
                        "Please click the small green button on the control panel and try again."
                        };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  message,
                                                  "Import Point of Interest",
                                                  JOptionPane.WARNING_MESSAGE);
                    // This is the easiest way!
                    return;
                    }

                try
                    {
                    if ((comboImportPOI.getSelectedItem() != null)
                        && (comboImportPOI.getSelectedItem() instanceof PointOfInterestType)
                        && (obsinstrument != null))
                        {
                        switch ((PointOfInterestType)comboImportPOI.getSelectedItem())
                            {
                            case FRAMEWORK:
                                {
                                PointOfInterestHelper.importFrameworkPOIandLOI(PointOfInterestType.FRAMEWORK,
                                                                               REGISTRY.getFramework(),
                                                                               obsinstrument.getDAO());
                                break;
                                }

                            case OBSERVATORY:
                                {
                                PointOfInterestHelper.importObservatoryPOIandLOI(PointOfInterestType.OBSERVATORY,
                                                                                 REGISTRY.getFramework(),
                                                                                 obsinstrument.getDAO());
                                break;
                                }

                            case INSTRUMENT:
                                {
                                PointOfInterestHelper.importInstrumentPOIandLOI(PointOfInterestType.INSTRUMENT,
                                                                                REGISTRY.getFramework(),
                                                                                obsinstrument.getDAO());
                                break;
                                }

                            default:
                                {
                                // An unrecognised PointOfInterestType
                                throw new XmlException(SOURCE + EXCEPTION_UNRECOGNISED_POITYPE);
                                }
                            }
                        }

                    // Refresh the Map display
                    if (map != null)
                        {
                        map.runUI();
                        }
                    }

                catch (XmlException exception)
                    {
                    LOGGER.error(SOURCE + "XmlException = " + exception.getMessage());
                    }

                catch (IOException exception)
                    {
                    LOGGER.error(SOURCE + "IOException = " + exception.getMessage());
                    }
                }
            };

        comboImportPOI.addActionListener(choiceListener);

        return (comboImportPOI);
        }


    /***********************************************************************************************
     * Create a drop-down of POI sources to Remove.
     *
     * @param obsinstrument
     * @param map
     * @param font
     * @param colourforeground
     * @param colourbackground
     *
     * @return JComboBox
     */

    public static JComboBox createComboRemovePOI(final ObservatoryInstrumentInterface obsinstrument,
                                                 final MapUIComponentPlugin map,
                                                 final FontInterface font,
                                                 final ColourInterface colourforeground,
                                                 final ColourInterface colourbackground)
        {
        final JComboBox comboRemovePOI;
        final ActionListener choiceListener;

        comboRemovePOI = new JComboBox();
        comboRemovePOI.setFont(font.getFont());
        comboRemovePOI.setForeground(colourforeground.getColor());
        comboRemovePOI.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                               font,
                                                               colourforeground,
                                                               colourbackground));

        // Do NOT allow the combo box to take up all the remaining space!
        comboRemovePOI.setPreferredSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboRemovePOI.setMaximumSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboRemovePOI.setAlignmentX(0);
        comboRemovePOI.setToolTipText(TOOLTIP_POI_REMOVE);
        comboRemovePOI.setEnabled(true);
        comboRemovePOI.setEditable(false);

        // Enumerate the PointOfInterestType to build the combo box
        // First entry is blank, to force a selection
        comboRemovePOI.addItem(EMPTY_STRING);
        comboRemovePOI.addItem(PointOfInterestType.FRAMEWORK);
        comboRemovePOI.addItem(PointOfInterestType.OBSERVATORY);
        comboRemovePOI.addItem(PointOfInterestType.INSTRUMENT);
        comboRemovePOI.setMaximumRowCount(4);
        comboRemovePOI.setSelectedIndex(0);
        comboRemovePOI.revalidate();

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final String SOURCE = "MapHelper.createComboRemovePOI() actionPerformed() ";

                // This method is only ever called from the UI, so it is ok to show a MessageDialog
                if (InstrumentState.isOff(obsinstrument))
                    {
                    final String [] message =
                        {
                        "The Instrument must be switched on in order to remove a POI.",
                        "Please click the small green button on the control panel and try again."
                        };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  message,
                                                  "Remove Point of Interest",
                                                  JOptionPane.WARNING_MESSAGE);
                    // This is the easiest way!
                    return;
                    }

                try
                    {
                    if ((comboRemovePOI.getSelectedItem() != null)
                        && (comboRemovePOI.getSelectedItem() instanceof PointOfInterestType)
                        && (obsinstrument != null))
                        {
                        switch ((PointOfInterestType)comboRemovePOI.getSelectedItem())
                            {
                            case FRAMEWORK:
                                {
                                PointOfInterestHelper.removeFrameworkPOIandLOI(PointOfInterestType.FRAMEWORK,
                                                                               REGISTRY.getFramework(),
                                                                               obsinstrument.getDAO());
                                break;
                                }

                            case OBSERVATORY:
                                {
                                PointOfInterestHelper.removeObservatoryPOIandLOI(PointOfInterestType.OBSERVATORY,
                                                                                 obsinstrument.getDAO());
                                break;
                                }

                            case INSTRUMENT:
                                {
                                // Clear the Instrument Composite POIs and LOIs (NOT those from the schema)
                                PointOfInterestHelper.removeInstrumentPOIandLOI(PointOfInterestType.INSTRUMENT,
                                                                                obsinstrument.getDAO());
                                break;
                                }

                            default:
                                {
                                // An unrecognised PointOfInterestType
                                throw new XmlException(SOURCE + EXCEPTION_UNRECOGNISED_POITYPE);
                                }
                            }
                        }

                    // Refresh the Map display
                    if (map != null)
                        {
                        map.runUI();
                        }
                    }

                catch (XmlException exception)
                    {
                    LOGGER.error(SOURCE + "XmlException = " + exception.getMessage());
                    }
                }
            };

        comboRemovePOI.addActionListener(choiceListener);

        return (comboRemovePOI);
        }
    }
