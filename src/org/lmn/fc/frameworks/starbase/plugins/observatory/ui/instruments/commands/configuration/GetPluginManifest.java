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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.configuration;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ResponseMessageHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;

import java.util.Iterator;


/***************************************************************************************************
 * GetPluginManifest.
 */

public final class GetPluginManifest implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkSingletons,
                                                ObservatoryConstants
    {
    /***********************************************************************************************
     * doGetPluginManifest().
     *
     * @param dao
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface doGetPluginManifest(final ObservatoryInstrumentDAOInterface dao,
                                                               final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "GetPluginManifest.getPluginManifest() ";
        final CommandType cmdGetManifest;
        final ResponseMessageInterface responseMessage;
        final StringBuffer buffer;

        buffer = new StringBuffer();

        // Don't affect the CommandType of the incoming Command
        cmdGetManifest = (CommandType)commandmessage.getCommandType().copy();

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            if ((dao.getHostInstrument().getInstrument().getController() != null)
                && (dao.getHostInstrument().getInstrument().getControllable()))
                {
                if ((dao.getHostInstrument().getInstrument().getController().getPluginManifest() != null)
                    && (dao.getHostInstrument().getInstrument().getController().getPluginManifest().getPrimaryResourceKey() != null))
                    {
                    // It looks like we can read a Manifest
                    buffer.append(dao.getHostInstrument().getInstrument().getIdentifier());
                    buffer.append(" Plugin Manifest\n\n\r");
                    buffer.append("    Primary: ");
                    buffer.append(dao.getHostInstrument().getInstrument().getController().getPluginManifest().getPrimaryResourceKey());
                    buffer.append("\n");

                    // Are there any Secondary PLugins?
                    if ((dao.getHostInstrument().getInstrument().getController().getPluginManifest().getResourceKeyList() != null)
                        && (!dao.getHostInstrument().getInstrument().getController().getPluginManifest().getResourceKeyList().isEmpty()))
                        {
                        final Iterator<String> iterKeys;

                        iterKeys = dao.getHostInstrument().getInstrument().getController().getPluginManifest().getResourceKeyList().iterator();

                        while (iterKeys.hasNext())
                            {
                            final String strKey;

                            strKey = iterKeys.next();

                            if ((strKey != null)
                                && (!EMPTY_STRING.equals(strKey)))
                                {
                                buffer.append("    Secondary: ");
                                buffer.append(strKey);
                                buffer.append("\n");
                                }
                            else
                                {
                                buffer.append("    Secondary Plugin key unexpectedly NULL\n");
                                }
                            }
                        }
                    else
                        {
                        buffer.append("    There are no Secondary Plugins\n");
                        }
                    }
                else
                    {
                    buffer.append(dao.getHostInstrument().getInstrument().getIdentifier());
                    buffer.append(" Plugin Manifest is not specified\n");
                    }
                }
            else
                {
                buffer.append(dao.getHostInstrument().getInstrument().getIdentifier());
                buffer.append(" does not have a Controller\n");
                buffer.append("No manifest report can be generated\n");
                }

            LOGGER.debug(dao.isDebugMode(), buffer.toString());
            }
        else
            {
            buffer.append("Instrument unexpectedly NULL\n");
            buffer.append("No manifest report can be generated\n");
            }

        // Put the version in the Response
        cmdGetManifest.getResponse().setValue(buffer.toString());

        // Create the ResponseMessage
        responseMessage = ResponseMessageHelper.constructSuccessfulResponse(dao,
                                                                            commandmessage,
                                                                            cmdGetManifest);
        return (responseMessage);
        }
    }
