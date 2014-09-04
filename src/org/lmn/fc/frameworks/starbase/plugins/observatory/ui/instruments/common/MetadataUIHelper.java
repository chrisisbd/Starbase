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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;


import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.ui.AlignedListCellRenderer;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataItemState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.MetadataExplorerFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.CommandProcessorUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands.ParameterChoiceToken;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Controller;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * MetadataUIHelper.
 */

public final class MetadataUIHelper implements FrameworkConstants,
                                               FrameworkStrings,
                                               FrameworkMetadata,
                                               FrameworkSingletons,
                                               ObservatoryConstants
    {
    // Adjust the Toolbar to suit 24x24px Icons
    private static final int HEIGHT_TOOLBAR_ICON = 24;

    // Allow for Plugin expanded names
    private static final int WIDTH_DROPDOWN = 150;


    /***********************************************************************************************
     * Create the ComboBox of MetadataTypes for Import.
     *
     * @param obsinstrument
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     *
     * @return JComboBox
     */

    public static JComboBox createComboImportMetadata(final ObservatoryInstrumentInterface obsinstrument,
                                                      final FontInterface fontdata,
                                                      final ColourInterface colourforeground,
                                                      final ColourInterface colourbackground)
        {
        final String SOURCE = "MetadataUIHelper.createComboImportMetadata() ";
        final JComboBox comboImportMetadata;
        final ActionListener choiceListener;

        comboImportMetadata = new JComboBox();
        comboImportMetadata.setFont(fontdata.getFont());
        comboImportMetadata.setForeground(colourforeground.getColor());
        comboImportMetadata.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                    fontdata,
                                                                    colourforeground,
                                                                    colourbackground));

        // Do NOT allow the combo box to take up all the remaining space!
        comboImportMetadata.setPreferredSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboImportMetadata.setMaximumSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboImportMetadata.setAlignmentX(0);
        comboImportMetadata.setToolTipText(MetadataExplorerFrameUIComponentInterface.TOOLTIP_METADATA_IMPORT);
        comboImportMetadata.setEnabled(true);
        comboImportMetadata.setEditable(false);

        // Enumerate the MetadataType to build the combo box
        populateCombo(comboImportMetadata, obsinstrument);
        comboImportMetadata.setSelectedIndex(0);
        comboImportMetadata.revalidate();

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // This method is only ever called from the UI, so it is ok to show a MessageDialog
                if (InstrumentState.isOff(obsinstrument))
                    {
                    final String [] message =
                        {
                        "The Instrument must be switched on in order to import Metadata.",
                        "Please click the small green button on the control panel and try again."
                        };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  message,
                                                  "Import Metadata",
                                                  JOptionPane.WARNING_MESSAGE);
                    // This is the easiest way!
                    return;
                    }

                try
                    {
                    if ((comboImportMetadata.getSelectedItem() != null)
                        && (comboImportMetadata.getSelectedItem() instanceof String)
                        && (obsinstrument != null)
                        && (obsinstrument.getDAO() != null))
                        {
                        final String strComboChoice;
                        final MetadataType metadataType;
                        boolean boolSuccess;

                        strComboChoice = (String)comboImportMetadata.getSelectedItem();

                        // Map the choice to a MetadataType if possible,
                        // i.e. this finds the non-expanded entries
                        metadataType = MetadataType.getMetadataTypeForName(strComboChoice);

                        boolSuccess = false;

                        if ((metadataType != null)
                            && (!metadataType.isExpandable()))
                            {
                            switch (metadataType)
                                {
                                // Framework Metadata end up in the Framework
                                case FRAMEWORK:
                                    {
                                    boolSuccess = MetadataHelper.importFrameworkMetadata(metadataType,
                                                                                         REGISTRY.getFramework());
                                    break;
                                    }

                                // Observatory end up in the Observatory
                                case OBSERVATORY:
                                    {
                                    final boolean boolLoaded;

                                    boolLoaded = MetadataHelper.reloadObservatoryDefaultMetadata(obsinstrument.getDAO().getHostInstrument(),
                                                                                                 LOADER_PROPERTIES.isMetadataDebug());
                                    // If the Metadata were reloaded, tell the Observatory listeners
                                    if (boolLoaded)
                                        {
                                        obsinstrument.getContext().getObservatory().notifyObservatoryMetadataChangedEvent(obsinstrument.getDAO(),
                                                                                                                          EMPTY_STRING,
                                                                                                                          MetadataItemState.ADD);
                                        }
                                    else
                                        {
                                        LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                                                     SOURCE + "Observatory Metadata already loaded");
                                        }

                                    // Now add references to the Master ObservatoryMetadata to the current Instrument DAO containers,
                                    // as in AbstractObservatoryInstrumentDAO.establishDAOIdentityForCapture()
                                    if (obsinstrument.getContext().getObservatory().getObservatoryMetadata() != null)
                                        {
                                        // Remove all traces of any previous Observatory Metadata from this DAO
                                        obsinstrument.getDAO().getCurrentObservatoryMetadata().clear();

                                        obsinstrument.getDAO().addAllMetadataToContainersTraced(obsinstrument.getContext().getObservatory().getObservatoryMetadata(),
                                                                                                SOURCE + "Adding reference to Observatory Metadata to DAO",
                                                                                                LOADER_PROPERTIES.isMetadataDebug());
                                        boolSuccess = true;
                                        }

                                    break;
                                    }

                                // Observer (current User?) end up in the Observatory
                                case OBSERVER:
                                    {
                                    final boolean boolLoaded;

                                    boolLoaded = MetadataHelper.reloadObserverDefaultMetadata(obsinstrument.getDAO().getHostInstrument(),
                                                                                              LOADER_PROPERTIES.isMetadataDebug());
                                    // If the Metadata were reloaded, tell the Observatory listeners
                                    if (boolLoaded)
                                        {
                                        obsinstrument.getContext().getObservatory().notifyObserverMetadataChangedEvent(obsinstrument.getDAO(),
                                                                                                                       EMPTY_STRING,
                                                                                                                       MetadataItemState.ADD);
                                        }
                                    else
                                        {
                                        LOGGER.debug(LOADER_PROPERTIES.isMetadataDebug(),
                                                     SOURCE + "Observer Metadata already loaded");
                                        }

                                    // Now add references to the Master ObserverMetadata to the current Instrument DAO containers,
                                    // as in AbstractObservatoryInstrumentDAO.establishDAOIdentityForCapture()
                                    if (obsinstrument.getContext().getObservatory().getObserverMetadata() != null)
                                        {
                                        // Remove all traces of any previous Observer Metadata from this DAO
                                        obsinstrument.getDAO().getCurrentObserverMetadata().clear();

                                        obsinstrument.getDAO().addAllMetadataToContainersTraced(obsinstrument.getContext().getObservatory().getObserverMetadata(),
                                                                                                SOURCE + "Adding reference to Observer Metadata to DAO",
                                                                                                LOADER_PROPERTIES.isMetadataDebug());
                                        boolSuccess = true;
                                        }

                                    break;
                                    }

                                // All of the following have the filename prefixed by the Instrument Identifier,
                                // and so end up in the DAO Metadata

                                case OBSERVATION:
                                    {
                                    boolSuccess = MetadataHelper.importObservationMetadata(metadataType, obsinstrument.getDAO());
                                    break;
                                    }

                                case INSTRUMENT:
                                    {
                                    boolSuccess = MetadataHelper.importInstrumentMetadata(metadataType, obsinstrument.getDAO());
                                    break;
                                    }

                                case CONTROLLER:
                                    {
                                    boolSuccess = MetadataHelper.importControllerMetadata(metadataType, obsinstrument.getDAO());
                                    break;
                                    }

                                default:
                                    {
                                    // An unrecognised MetadataType
                                    throw new XmlException(SOURCE + "actionPerformed() " + EXCEPTION_UNRECOGNISED_METADATATYPE);
                                    }
                                }

                            if (boolSuccess)
                                {
                                SimpleEventLogUIComponent.logEvent(obsinstrument.getDAO().getEventLogFragment(),
                                                                   EventStatus.INFO,
                                                                   METADATA_TARGET
                                                                       + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                                       + METADATA_ACTION_IMPORT_METADATA
                                                                       + METADATA_CATEGORY + metadataType.getName() + TERMINATOR,
                                                                   SOURCE,
                                                                   obsinstrument.getDAO().getObservatoryClock());

                                // We know that the Instrument and the DAO are not null
                                // Only refresh the data if visible
                                obsinstrument.setWrappedData(obsinstrument.getDAO().getWrappedData(), false, true);

                                REGISTRY.getFramework().notifyFrameworkChangedEvent(obsinstrument);
                                InstrumentHelper.notifyInstrumentChanged(obsinstrument);
                                }
                            else
                                {
                                // The User probably attempted to import something that didn't exist
                                Toolkit.getDefaultToolkit().beep();
                                }
                            }
                        else
                            {
                            // Assume we have been given a Plugin Identifier in strComboChoice
                            if ((strComboChoice != null)
                                && (strComboChoice.endsWith(ParameterChoiceToken.CHOICE_SUFFIX_MODULE)))
                                {
                                final String strPluginIdentifier;

                                // Remove the "_Module" from the Plugin choice
                                strPluginIdentifier = strComboChoice.substring(0, strComboChoice.length() - ParameterChoiceToken.CHOICE_SUFFIX_MODULE.length());

                                boolSuccess = MetadataHelper.importPluginMetadata(MetadataType.PLUGIN,
                                                                                  strPluginIdentifier,
                                                                                  obsinstrument.getDAO());
                                if (boolSuccess)
                                    {
                                    SimpleEventLogUIComponent.logEvent(obsinstrument.getDAO().getEventLogFragment(),
                                                                       EventStatus.INFO,
                                                                       METADATA_TARGET
                                                                           + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                                           + METADATA_ACTION_IMPORT_METADATA
                                                                           + METADATA_CATEGORY + obsinstrument.getDAO().getHostInstrument().getInstrument().getIdentifier() + DOT
                                                                           + strPluginIdentifier + TERMINATOR,
                                                                       SOURCE,
                                                                       obsinstrument.getDAO().getObservatoryClock());

                                    // We know that the Instrument and the DAO are not null
                                    // Only refresh the data if visible
                                    obsinstrument.setWrappedData(obsinstrument.getDAO().getWrappedData(), false, true);

                                    REGISTRY.getFramework().notifyFrameworkChangedEvent(obsinstrument);
                                    InstrumentHelper.notifyInstrumentChanged(obsinstrument);
                                    }
                                }
                            else
                                {
                                // The User probably attempted to import something that didn't exist
                                Toolkit.getDefaultToolkit().beep();

                                // Some kind of configuration error?
                                LOGGER.error(SOURCE + "actionPerformed() Import choice not recognised");
                                }
                            }
                        }
                    }

                catch (XmlException exception)
                    {
                    LOGGER.error(SOURCE + "actionPerformed() XmlException = " + exception.getMessage());
                    }

                catch (IOException exception)
                    {
                    LOGGER.error(SOURCE + "actionPerformed() IOException = " + exception.getMessage());
                    }
                }
            };

        comboImportMetadata.addActionListener(choiceListener);

        return (comboImportMetadata);
        }


    /***********************************************************************************************
     * Create the ComboBox of MetadataTypes for Removal.
     * This has the same logic as class RemoveMetadata.doRemoveMetadata().
     *
     * @param obsinstrument
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     *
     * @return JComboBox
     */

    public static JComboBox createComboRemoveMetadata(final ObservatoryInstrumentInterface obsinstrument,
                                                      final FontInterface fontdata,
                                                      final ColourInterface colourforeground,
                                                      final ColourInterface colourbackground)
        {
        final String SOURCE = "MetadataUIHelper.createComboRemoveMetadata() ";
        final JComboBox comboRemoveMetadata;
        final ActionListener choiceListener;

        comboRemoveMetadata = new JComboBox();
        comboRemoveMetadata.setFont(fontdata.getFont());
        comboRemoveMetadata.setForeground(colourforeground.getColor());
        comboRemoveMetadata.setRenderer(new AlignedListCellRenderer(SwingConstants.LEFT,
                                                                    fontdata,
                                                                    colourforeground,
                                                                    colourbackground));

        // Do NOT allow the combo box to take up all the remaining space!
        comboRemoveMetadata.setPreferredSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboRemoveMetadata.setMaximumSize(new Dimension(WIDTH_DROPDOWN, HEIGHT_TOOLBAR_ICON - 4));
        comboRemoveMetadata.setAlignmentX(0);
        comboRemoveMetadata.setToolTipText(MetadataExplorerFrameUIComponentInterface.TOOLTIP_METADATA_REMOVE);
        comboRemoveMetadata.setEnabled(true);
        comboRemoveMetadata.setEditable(false);

        // Enumerate the MetadataType to build the combo box
        comboRemoveMetadata.addItem(EMPTY_STRING);
        comboRemoveMetadata.addItem(MetadataType.FRAMEWORK.getName());
        comboRemoveMetadata.addItem(MetadataType.OBSERVATORY.getName());
        comboRemoveMetadata.addItem(MetadataType.OBSERVER.getName());
        comboRemoveMetadata.addItem(MetadataType.OBSERVATION.getName());
        comboRemoveMetadata.addItem(MetadataType.INSTRUMENT.getName());
        comboRemoveMetadata.addItem(MetadataType.METADATA.getName());
        comboRemoveMetadata.setMaximumRowCount(7);
        comboRemoveMetadata.setSelectedIndex(0);
        comboRemoveMetadata.revalidate();

        choiceListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // This method is only ever called from the UI, so it is ok to show a MessageDialog
                if (InstrumentState.isOff(obsinstrument))
                    {
                    final String [] message =
                        {
                        "The Instrument must be switched on in order to remove Metadata.",
                        "Please click the small green button on the control panel and try again."
                        };

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null,
                                                  message,
                                                  "Remove Metadata",
                                                  JOptionPane.WARNING_MESSAGE);
                    // This is the easiest way!
                    return;
                    }

                try
                    {
                    if ((comboRemoveMetadata.getSelectedItem() != null)
                        && (comboRemoveMetadata.getSelectedItem() instanceof String)
                        && (obsinstrument != null)
                        && (obsinstrument.getDAO() != null))
                        {
                        final String strComboChoice;
                        final MetadataType metadataType;
                        boolean boolSuccess;

                        strComboChoice = (String)comboRemoveMetadata.getSelectedItem();

                        // Map the choice to a MetadataType if possible,
                        // i.e. this finds the non-expanded entries
                        metadataType = MetadataType.getMetadataTypeForName(strComboChoice);

                        boolSuccess = false;

                        if ((metadataType != null)
                            && (!metadataType.isExpandable()))
                            {
                            switch (metadataType)
                                {
                                case FRAMEWORK:
                                    {
                                    if ((REGISTRY.getFramework() != null)
                                        && (REGISTRY.getFramework().getFrameworkMetadata() != null))
                                        {
                                        final FrameworkPlugin framework;

                                        framework = REGISTRY.getFramework();

                                        // The Framework holds the Framework Metadata
                                        framework.getFrameworkMetadata().clear();

                                        // ... but we can't go on like this, so force creation of the basics
                                        framework.getLongitude();
                                        framework.getLatitude();
                                        framework.getHASL();
                                        framework.getTimeZoneCode();

                                        SimpleEventLogUIComponent.logEvent(obsinstrument.getDAO().getEventLogFragment(),
                                                                           EventStatus.INFO,
                                                                           METADATA_FRAMEWORK_RESET
                                                                                + METADATA_LONGITUDE + framework.getLongitude().toString() + TERMINATOR_SPACE
                                                                                + METADATA_LATITUDE + framework.getLatitude().toString() + TERMINATOR_SPACE
                                                                                + METADATA_HASL + Double.toString(framework.getHASL()) + TERMINATOR_SPACE
                                                                                + METADATA_TIMEZONE + framework.getTimeZoneCode() + TERMINATOR,
                                                                           SOURCE,
                                                                           obsinstrument.getDAO().getObservatoryClock());
                                        boolSuccess = true;
                                        }

                                    break;
                                    }

                                case OBSERVATORY:
                                    {
                                    if ((obsinstrument.getContext().getObservatory() != null)
                                        && (obsinstrument.getContext().getObservatory().getObservatoryMetadata() != null))
                                        {
                                        // Remove the DAO references
                                        if (obsinstrument.getDAO().getCurrentObservatoryMetadata() != null)
                                            {
                                            obsinstrument.getDAO().getCurrentObservatoryMetadata().clear();
                                            }

                                        // Remove the Wrapper references
                                        if ((obsinstrument.getDAO().getWrappedData() != null)
                                            && (obsinstrument.getDAO().getWrappedData().getCurrentObservatoryMetadata() != null))
                                            {
                                            obsinstrument.getDAO().getWrappedData().getCurrentObservatoryMetadata().clear();
                                            }

                                        // If there are no ObservatoryMetadata listeners, remove the underlying master data also
                                        // This is very unlikely to occur!
                                        if (obsinstrument.getContext().getObservatory().getObservatoryMetadataChangedListeners().isEmpty())
                                            {
                                            // The Observatory holds the Observatory Metadata, so clear it
                                            obsinstrument.getContext().getObservatory().getObservatoryMetadata().clear();
                                            obsinstrument.getContext().getObservatory().setObservatoryMetadataLoaded(false);
                                            }

                                        // Tell the Observatory listeners
                                        obsinstrument.getContext().getObservatory().notifyObservatoryMetadataChangedEvent(obsinstrument.getDAO(),
                                                                                                                          EMPTY_STRING,
                                                                                                                          MetadataItemState.REMOVE);
                                        boolSuccess = true;
                                        }

                                    break;
                                    }

                                case OBSERVER:
                                    {
                                    if ((obsinstrument.getContext().getObservatory() != null)
                                        && (obsinstrument.getContext().getObservatory().getObserverMetadata() != null))
                                        {
                                        // Remove the DAO references
                                        if (obsinstrument.getDAO().getCurrentObserverMetadata() != null)
                                            {
                                            obsinstrument.getDAO().getCurrentObserverMetadata().clear();
                                            }

                                        // Remove the Wrapper references
                                        if ((obsinstrument.getDAO().getWrappedData() != null)
                                            && (obsinstrument.getDAO().getWrappedData().getCurrentObserverMetadata() != null))
                                            {
                                            obsinstrument.getDAO().getWrappedData().getCurrentObserverMetadata().clear();
                                            }

                                        // If there are no ObserverMetadata listeners, remove the underlying master data also
                                        // This is very unlikely to occur!
                                        if (obsinstrument.getContext().getObservatory().getObserverMetadataChangedListeners().isEmpty())
                                            {
                                            // The Observatory holds the Observer Metadata, so clear it
                                            obsinstrument.getContext().getObservatory().getObserverMetadata().clear();
                                            obsinstrument.getContext().getObservatory().setObserverMetadataLoaded(false);
                                            }

                                        // Tell the Observatory listeners
                                        obsinstrument.getContext().getObservatory().notifyObserverMetadataChangedEvent(obsinstrument.getDAO(),
                                                                                                                       EMPTY_STRING,
                                                                                                                       MetadataItemState.REMOVE);
                                        boolSuccess = true;
                                        }

                                    break;
                                    }

                                case OBSERVATION:
                                    {
                                    // The DAO holds the Observation Metadata
                                    if (obsinstrument.getDAO().getObservationMetadata() != null)
                                        {
                                        obsinstrument.getDAO().getObservationMetadata().clear();
                                        boolSuccess = true;
                                        }

                                    // Clear Metadata in the DAO Wrapper,
                                    // whose references point back to the DAO so they should have been cleared above
                                    if ((obsinstrument.getDAO().getWrappedData() != null)
                                        && (obsinstrument.getDAO().getWrappedData().getObservationMetadata() != null))
                                        {
                                        obsinstrument.getDAO().getWrappedData().getObservationMetadata().clear();
                                        boolSuccess = true;
                                        }

                                    break;
                                    }

                                case INSTRUMENT:
                                    {
                                    // The DAO holds the Instrument, Controller, Plugin Metadata
                                    // Clear all for Instrument, Controller, Plugin for simplicity
                                    if (obsinstrument.getDAO().getInstrumentMetadata() != null)
                                        {
                                        obsinstrument.getDAO().getInstrumentMetadata().clear();
                                        boolSuccess = true;
                                        }

                                    if (obsinstrument.getDAO().getControllerMetadata() != null)
                                        {
                                        obsinstrument.getDAO().getControllerMetadata().clear();
                                        boolSuccess = true;
                                        }

                                    if (obsinstrument.getDAO().getPluginMetadata() != null)
                                        {
                                        obsinstrument.getDAO().getPluginMetadata().clear();
                                        boolSuccess = true;
                                        }

                                    // Clear Metadata in the DAO Wrapper,
                                    // whose references point back to the DAO so they should have been cleared above
                                    if (obsinstrument.getDAO().getWrappedData() != null)
                                        {
                                        if (obsinstrument.getDAO().getWrappedData().getInstrumentMetadata() != null)
                                            {
                                            obsinstrument.getDAO().getWrappedData().getInstrumentMetadata().clear();
                                            boolSuccess = true;
                                            }

                                        if (obsinstrument.getDAO().getWrappedData().getControllerMetadata() != null)
                                            {
                                            obsinstrument.getDAO().getWrappedData().getControllerMetadata().clear();
                                            boolSuccess = true;
                                            }

                                        if (obsinstrument.getDAO().getWrappedData().getPluginMetadata() != null)
                                            {
                                            obsinstrument.getDAO().getWrappedData().getPluginMetadata().clear();
                                            boolSuccess = true;
                                            }
                                        }

                                    break;
                                    }

                                case METADATA:
                                    {
                                    if (obsinstrument.getDAO().getMetadataMetadata() != null)
                                        {
                                        obsinstrument.getDAO().getMetadataMetadata().clear();
                                        boolSuccess = true;
                                        }

                                    // Clear MetadataMetadata in the DAO Wrapper,
                                    // whose references point back to the DAO so they should have been cleared above
                                    if ((obsinstrument.getDAO().getWrappedData() != null)
                                       && (obsinstrument.getDAO().getWrappedData().getMetadataMetadata() != null))
                                        {
                                        obsinstrument.getDAO().getWrappedData().getMetadataMetadata().clear();
                                        boolSuccess = true;
                                        }

                                    break;
                                    }

                                default:
                                    {
                                    // An unrecognised MetadataType
                                    throw new XmlException(SOURCE + EXCEPTION_UNRECOGNISED_METADATATYPE);
                                    }
                                }

                            if (boolSuccess)
                                {
                                SimpleEventLogUIComponent.logEvent(obsinstrument.getDAO().getEventLogFragment(),
                                                                   EventStatus.INFO,
                                                                   METADATA_TARGET
                                                                       + obsinstrument.getInstrument().getIdentifier() + TERMINATOR
                                                                       + METADATA_ACTION_REMOVE_METADATA
                                                                       + METADATA_CATEGORY + metadataType.getName() + TERMINATOR,
                                                                   SOURCE,
                                                                   obsinstrument.getDAO().getObservatoryClock());

                                // We know that the Instrument and the DAO are not null
                                // Only refresh the data if visible
                                obsinstrument.setWrappedData(obsinstrument.getDAO().getWrappedData(), false, true);

                                REGISTRY.getFramework().notifyFrameworkChangedEvent(obsinstrument);
                                InstrumentHelper.notifyInstrumentChanged(obsinstrument);
                                }
                            else
                                {
                                // The User probably attempted to remove something that didn't exist
                                Toolkit.getDefaultToolkit().beep();
                                }
                            }
                        }
                    }

                catch (XmlException exception)
                    {
                    LOGGER.error(SOURCE + "actionPerformed() XmlException = " + exception.getMessage());
                    }
                }
            };

        comboRemoveMetadata.addActionListener(choiceListener);

        return (comboRemoveMetadata);
        }


    /***********************************************************************************************
     * Populate the ComboBox with choices.
     * The combo menu items are Strings because we need to use the Plugin Identifiers
     * with a suffix _Module, rather then the MetadataType directly.
     *
     * @param combobox
     * @param obsinstrument
     *
     * @return JComboBox
     */

    private static JComboBox populateCombo(final JComboBox combobox,
                                           final ObservatoryInstrumentInterface obsinstrument)
        {
        final String SOURCE = "MetadataUIHelper.populateCombo() ";

        if ((combobox !=  null)
            && (obsinstrument !=  null))
            {
            final MetadataType[] arrayTypes;
            int intComboRowCount;

            intComboRowCount = 0;

            // First entry is blank, to force a selection
            combobox.addItem(EMPTY_STRING);
            intComboRowCount++;

            // Enumerate the MetadataType without the Plugins to build the combo box
            arrayTypes = MetadataType.values();

            for (int intIndexMetadataType = 0;
                 intIndexMetadataType < arrayTypes.length;
                 intIndexMetadataType++)
                {
                final MetadataType metadataType;

                metadataType = arrayTypes[intIndexMetadataType];

                // Only use MetadataTypes which will not have a suffix or name change.
                if (!metadataType.isExpandable())
                    {
                    combobox.addItem(metadataType.getName());
                    intComboRowCount++;
                    }
                }

            // Add in the Plugin names
            intComboRowCount += addPluginList(combobox, obsinstrument);

            combobox.setMaximumRowCount(intComboRowCount);
            }

        return (combobox);
        }


    /***********************************************************************************************
     * Build the List of Plugins to add to the choices drop-down.
     *
     * @param combobox
     * @param obsinstrument
     *
     * @return int
     */

    private static int addPluginList(final JComboBox combobox,
                                     final ObservatoryInstrumentInterface obsinstrument)
        {
        final String SOURCE = "MetadataUIHelper.addPluginList() ";
        int intRowCount;

        intRowCount = 0;

        if ((combobox != null)
            && (obsinstrument != null)
            && (obsinstrument.getContext() != null)
            && (obsinstrument.getInstrument() != null)
            && (CommandProcessorUtilities.hasController(obsinstrument.getContext())))
            {
            final Controller controller;

            controller = obsinstrument.getInstrument().getController();

            // Now add Choices to the ComboBox for each of the Controller's Plugins
            if ((controller.getPluginList() != null)
                && (!controller.getPluginList().isEmpty()))
                {
                final List<PluginType> plugins;

                plugins = controller.getPluginList();

                if ((plugins != null)
                    && (!plugins.isEmpty()))
                    {
                    final StringBuffer bufferChoice;
                    final Iterator<PluginType> iterPlugins;

                    bufferChoice = new StringBuffer(25);
                    iterPlugins = plugins.iterator();

                    while (iterPlugins.hasNext())
                        {
                        final PluginType plugin;

                        plugin = iterPlugins.next();
                        bufferChoice.setLength(0);

                        if (plugin != null)
                            {
                            bufferChoice.append(plugin.getIdentifier());
                            bufferChoice.append(ParameterChoiceToken.CHOICE_SUFFIX_MODULE);
                            combobox.addItem(bufferChoice.toString());
                            intRowCount++;
                            }
                        }
                    }
                }
            else
                {
                // No Controller Plugins were found, which is acceptable
                }
            }
        else
            {
            // No Controller was found, which is acceptable
            }

        return (intRowCount);
        }
    }
