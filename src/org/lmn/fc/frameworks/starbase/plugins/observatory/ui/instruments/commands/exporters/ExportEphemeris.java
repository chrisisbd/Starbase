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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters;


import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.EphemerisFrameUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.EphemerisUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ephemerides.EphemerisFrameUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ephemerides.EphemerisUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;


/***************************************************************************************************
 * ExportEphemeris.
 */

public final class ExportEphemeris implements FrameworkStrings,
                                              FrameworkMetadata,
                                              FrameworkRegex,
                                              FrameworkSingletons
    {
    /***********************************************************************************************
     * doExportEphemeris().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doExportEphemeris(final ObservatoryInstrumentDAOInterface dao,
                                                             final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "ExportEphemeris.doExportEphemeris()";

        // The Ephemeris is found awkwardly via the InstrumentPanel --> EphemeridesTab --> EphemerisFrameUIComponent --> EphemerisPanel
        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrumentPanel() != null)
            && (dao.getHostInstrument().getInstrumentPanel().getEphemeridesTab() != null)
            && (dao.getHostInstrument().getInstrumentPanel().getEphemeridesTab() instanceof EphemerisFrameUIComponentInterface)
            && (((EphemerisFrameUIComponent)(dao.getHostInstrument().getInstrumentPanel().getEphemeridesTab())).getEphemerisUI() != null))
            {
            final EphemerisUIComponentInterface ephemerisUI;

            ephemerisUI = ((EphemerisFrameUIComponent) (dao.getHostInstrument().getInstrumentPanel().getEphemeridesTab())).getEphemerisUI();

            // Provide column Metadata, don't infer it
            return (dao.exportReportTable(SOURCE,
                                          commandmessage,
                                          null,
                                          EphemerisUIComponent.createEphemerisMetadata(
                                                  REGISTRY.getFramework(),
                                                                                       (ObservatoryInterface)dao.getHostInstrument().getHostAtom(),
                                                                                       dao.getHostInstrument(),
                                                                                       dao.getWrappedData(),
                                                                                       dao,
                                                                                       SOURCE,
                                                                                       LOADER_PROPERTIES.isMetadataDebug()),
                                          false,
                                          DatasetType.TABULAR,
                                          ephemerisUI));
            }
        else
            {
            final ResponseMessageInterface responseMessage;

            // If the Command failed, do not change any DAO data containers!
            // Our valuable data must remain available for export later...
            // Don't affect the CommandType of the incoming Command
            responseMessage = ResponseMessageHelper.constructFailedResponseIfNull(dao,
                                                                                  commandmessage,
                                                                                  (CommandType) commandmessage.getCommandType().copy(),
                                                                                  null);
            return (responseMessage);
            }
        }
    }
