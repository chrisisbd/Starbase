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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands;

import org.lmn.fc.common.datatranslators.hex.HexFileHelper;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentState;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ResponseViewerUIComponentInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.frameworks.starbase.portcontroller.events.CommandLifecycleEventInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.DiscoveryUtilities;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;


/***************************************************************************************************
 * ResponseViewerUIComponent.
 */

public final class ResponseViewerUIComponent extends UIComponent
                                             implements ResponseViewerUIComponentInterface
    {
    // Injections
    private final ObservatoryInstrumentInterface hostInstrument;
    private final String strResourceKey;

    // UI
    private final JTextArea textArea;


    /***********************************************************************************************
     * Construct an ResponseViewerUIComponent.
     *
     * @param hostinstrument
     * @param hostresourcekey
     */

    public ResponseViewerUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                                     final String hostresourcekey)
        {
        super();

        if ((hostinstrument != null)
            && (hostresourcekey != null)
            && (!EMPTY_STRING.equals(hostresourcekey)))
            {
            this.hostInstrument = hostinstrument;
            this.strResourceKey = hostresourcekey;

            this.textArea = new JTextArea(100, 150);
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Initialise the ResponseViewerUIComponent.
     */

    public void initialiseUI()
        {
        final JScrollPane scrollPane;

        super.initialiseUI();
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        getTextArea().setText(MSG_NO_RESPONSE);
        getTextArea().setBackground(Color.white);
        getTextArea().setForeground(UIComponent.DEFAULT_COLOUR_TEXT.getColor());
        getTextArea().setFont(UIComponent.DEFAULT_FONT_MONOSPACED.getFont());
        getTextArea().setEditable(false);
        getTextArea().setTabSize(4);
        getTextArea().setToolTipText(TOOLTIP_COPY);
        getTextArea().setMargin(new Insets(10, 10, 10, 10));
        getTextArea().setLineWrap(true);

        scrollPane = new JScrollPane(getTextArea(),
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);
        }


    /***********************************************************************************************
     * Get the ObservatoryInstrument to which this UIComponent is attached.
     *
     * @return ObservatoryInstrumentInterface
     */

    private ObservatoryInstrumentInterface getHostInstrument()
        {
        return (this.hostInstrument);
        }


    /***********************************************************************************************
     * Get the JTextArea usd to display the Response Value.
     *
     * @return JTextArea
     */

    private JTextArea getTextArea()
        {
        return (this.textArea);
        }


    /***********************************************************************************************
     * Indicate that there has been a CommandLifecycleEvent.
     *
     * @param event
     */

    public void commandChanged(final CommandLifecycleEventInterface event)
        {
        // If the event is invalid, do nothing
        // Select only those messages coming from the same DAO as the host Instrument
        // Have we seen this CommandLifecycleEvent before?
        if (isValidEvent(event))
            {
            // Display the ResponseValue (if any)
            if (event.getResponseMessage() != null)
                {
                final CommandMessageInterface command;
                final ResponseMessageInterface response;
                final StringBuffer buffer;

                // Convenience calls
                command = event.getCommandMessage();
                response = event.getResponseMessage();
                buffer = new StringBuffer();

                // Response Value
                if ((response.getCommandType() != null)
                    && (event.getResponseMessage().getCommandType().getResponse() != null)
                    && (event.getResponseMessage().getCommandType().getResponse().getValue() != null))
                    {
                    buffer.append(response.getCommandType().getResponse().getValue());

                    // Show a Hex dump if the message is of a reasonable length
                    if (response.getCommandType().getResponse().getValue().length() > "NO DATA".length())
                        {
                        buffer.append(LINE);
                        //buffer.append("\n0000: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n");
                        buffer.append(HexFileHelper.dumpHex(response.getCommandType().getResponse().getValue().getBytes(),
                                                            DUMP_BYTES_PER_LINE));
                        }
                    }

                // The ResponseValue name, in its full StarScript form
                buffer.append(LINE);
                buffer.append("\nResponse Name      ");
                buffer.append(response.getStarScript());

                // Response Units
                if ((response.getCommandType() != null)
                    && (response.getCommandType().getResponse() != null)
                    && (response.getCommandType().getResponse().getUnits() != null))
                    {
                    buffer.append("\nUnits              ");
                    buffer.append(response.getCommandType().getResponse().getUnits().toString());
                    }

                // Response DataType
                if ((response.getCommandType() != null)
                    && (response.getCommandType().getResponse() != null)
                    && (response.getCommandType().getResponse().getDataTypeName() != null))
                    {
                    buffer.append("\nDataType           ");
                    buffer.append(response.getCommandType().getResponse().getDataTypeName().toString());
                    }

                // The expanded Status Codes
                if (response.getResponseMessageStatusList() != null)
                    {
                    buffer.append("\nResponse Status    ");
                    buffer.append(ResponseMessageStatus.expandResponseStatusCodes(response.getResponseMessageStatusList()));
                    }

                // Dates & Times of the CommandLifecycleEvent
                if (command != null)
                    {
                    final Calendar calendarTx;
                    final Calendar calendarRx;

                    calendarTx = command.getTxCalendar();
                    buffer.append("\nCommand Sent       ");
                    buffer.append(ChronosHelper.toDateString(calendarTx));
                    buffer.append(SPACE);
                    buffer.append(ChronosHelper.toTimeString(calendarTx));

                    calendarRx = response.getRxCalendar();
                    buffer.append("\nResponse Received  ");
                    buffer.append(ChronosHelper.toDateString(calendarRx));
                    buffer.append(SPACE);
                    buffer.append(ChronosHelper.toTimeString(calendarRx));
                    }

                // Warn about unsaved data
                if ((getHostInstrument() != null)
                    && (getHostInstrument().getDAO() != null)
                    && (getHostInstrument().getDAO().hasUnsavedData()))
                    {
                    buffer.append("\n\nWARNING! You have unsaved data, please Export!  ");
                    }

                getTextArea().setText(buffer.toString());
                }
            else
                {
                getTextArea().setText(MSG_NO_RESPONSE);
                }
            }
        }


    /***********************************************************************************************
     * Check to see if the CommandLifecycleEvent is valid for this Log.
     *
     * @param event
     *
     * @return boolean
     */

    private boolean isValidEvent(final CommandLifecycleEventInterface event)
        {
        boolean boolValid;

        // Check we have a valid event and message(s)
        boolValid = ((event != null)
                        && (event.getSource() != null)
                        && (((event.getCommandMessage() != null)
                            && (event.getCommandMessage().getInstrument() != null))
                        || ((event.getResponseMessage() != null)
                            && (event.getResponseMessage().getInstrument() != null))));

        // Only allow updates the host Instrument is 'doing something',
        // i.e. not stopped executing a Command, or waiting.
        boolValid = boolValid
                    && ((getHostInstrument() != null)
                    && (InstrumentState.isDoingSomething(getHostInstrument())));

        // If there is a READY Instrument,
        // select only those messages coming from the same Instrument as the host Instrument
        if (boolValid)
            {
            // The Instrument Identifier in the CommandMessage must come from the HostInstrument
            if (event.getCommandMessage() != null)
                {
                boolValid = (event.getCommandMessage().getInstrument().getIdentifier().equals(getHostInstrument().getInstrument().getIdentifier()));
                }

            // If the message is a result of activity by the DiscoveryController,
            // then the ResponseMessage Instrument Identifier may not be as expected
            if (event.getResponseMessage() != null)
                {
                final boolean boolInstrumentController;
                final boolean boolDiscoveryController;

                // Only one of these can be valid:
                boolInstrumentController = event.getResponseMessage().getInstrument().getIdentifier().equals(getHostInstrument().getInstrument().getIdentifier());
                boolDiscoveryController = event.getResponseMessage().getInstrument().getIdentifier().equals(DiscoveryUtilities.DISCOVERY_CONTROLLER_IDENTIFIER);
                boolValid = boolValid && (boolInstrumentController || boolDiscoveryController);
                }
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    private ObservatoryClockInterface getObservatoryClock()
        {
        final ObservatoryClockInterface clock;

        clock = getHostInstrument().getObservatoryClock();

        return (clock);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }
