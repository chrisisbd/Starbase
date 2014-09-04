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

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ExecutionStatus;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.MacroType;

import java.util.List;


/***************************************************************************************************
 * ExecuteMacroHelper.
 */

public final class ExecuteMacroHelper implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 ObservatoryConstants
    {
    /**********************************************************************************************/
    /* Macro   Execution                                                                          */
    /***********************************************************************************************
     * Execute a StarScript Command (assumed to be valid).
     * Return true if the Command was executed.
     *
     * @param obsinstrument
     * @param instrumentxml
     * @param module
     * @param macro
     * @param starscript
     * @param isrepeating
     * @param repeatnumber
     * @param repeattext
     * @param errors
     *
     * @return boolean
     */

    public static synchronized boolean executeMacro(final ObservatoryInstrumentInterface obsinstrument,
                                                    final Instrument instrumentxml,
                                                    final XmlObject module,
                                                    final MacroType macro,
                                                    final String starscript,
                                                    final boolean isrepeating,
                                                    final long repeatnumber,
                                                    final String repeattext,
                                                    final List<String> errors)
        {
        boolean boolSuccess;

        boolSuccess = false;

        if ((obsinstrument != null)
           && (obsinstrument.getInstrument() != null)
           && (obsinstrument.getDAO() != null)
           && (errors != null))
            {
            final ObservatoryInstrumentDAOInterface dao;

            dao = obsinstrument.getDAO();
            dao.getResponseMessageStatusList().clear();
            dao.setExecutionStatus(ExecutionStatus.WAITING);

            // TODO Review clearing of port
            if (dao.getPort() != null)
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "ExecuteMacroHelper.executeMacro() Setting Port Ready");
                dao.getPort().setPortBusy(false);

                // Clear the Queues and Streams, since we know that all activity is over for now
                // This is to trap anomalies in Timeouts
                dao.getPort().clearQueues();
                dao.getPort().getTxStream().reset();
                dao.getPort().getRxStream().reset();
                }
            else
                {
                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                       "ExecuteMacroHelper.executeMacro() Port was NULL");
                }

            errors.add("Execute Macro Not Implemented");

            // We really have finished!
            obsinstrument.getDAO().setExecutionStatus(ExecutionStatus.FINISHED);

            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      "ExecuteMacroHelper.executeMacro() ----------------------------------------------------------------------------");
            }

        // Return false if we couldn't even try to execute it
        return (boolSuccess);
        }
    }
