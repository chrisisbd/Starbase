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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments;

import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.common.ObservatoryConstants;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ConfigurationHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.ViewingMode;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros.MacroManagerUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.SerialConfigurationHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.*;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.DaoPort;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.PortController;
import org.lmn.fc.frameworks.starbase.portcontroller.impl.StreamUtilities;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.PortType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Pattern;


/***************************************************************************************************
 * ObservatoryInstrumentHelper.
 */

public final class ObservatoryInstrumentHelper implements FrameworkConstants,
                                                          FrameworkStrings,
                                                          FrameworkMetadata,
                                                          FrameworkSingletons,
                                                          FrameworkRegex,
                                                          ResourceKeys,
                                                          ObservatoryConstants
    {
    // This cannot be in FrameworkSingletons because it won't be loaded at the right time...
    private static final PortControllerInterface PORT_CONTROLLER = PortController.getInstance();

    // String Resources
    private static final String MSG_NO_DAO = "It has not been possible to establish a data connection with this Instrument";
    private static final String MSG_OFF_LINE = "The Instrument is now in off-line mode and cannot respond to commands";
    private static final String MSG_STREAM_INSTANTIATE = "Unable to instantiate Streams";
    private static final String MSG_PORT_OPEN = "Unable to open the Port";
    private static final String MSG_PORT_CLOSE = "Unable to close the Port";
    private static final String MSG_STREAM_CLASSNAME = "Stream classname incorrect";
    private static final String MSG_DATA_ACCESS_UNAVAILABLE = " Data Access Unavailable";
    private static final String MSG_COMMON_PORT = "The StaribusPort has already been attached to the PortController";


    /***********************************************************************************************
     * Try to instantiate an ObservatoryInstrument from the classname in the Xml.
     *
     * @param instrumentxml
     * @param hostatom
     * @param hostui
     * @param resourcekey
     *
     * @return ObservatoryInstrumentInterface
     */

    public static ObservatoryInstrumentInterface instantiateInstrument(final Instrument instrumentxml,
                                                                       final AtomPlugin hostatom,
                                                                       final ObservatoryUIInterface hostui,
                                                                       final String resourcekey)
        {
        ObservatoryInstrumentInterface instrument;

        instrument = null;

        if ((instrumentxml != null)
            && (XmlBeansUtilities.isValidXml(instrumentxml)))
            {
            final String strClassname;

            strClassname = instrumentxml.getInstrumentClassname();

            try
                {
                final Class classObject;
                final Class[] interfaces;
                final String strInterface;
                boolean boolLoaded;

                classObject = Class.forName(strClassname);

                // Does the target implement the ObservatoryInstrumentInterface?
                interfaces = classObject.getInterfaces();
                strInterface = ObservatoryInstrumentInterface.class.getName();
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
                                // We have found the correct interface
                                //LOGGER.debugTimedEvent("[" + strClassname + " implements " + strInterface + "]");

                                // Prove that the real Instrument is a subclass of AbstractObservatoryInstrument
                                final Class superClass = classObject.getSuperclass();

                                if (superClass != null)
                                    {
                                    if (AbstractObservatoryInstrument.class.getName().equals(superClass.getName()))
                                        {
                                        final Constructor constructor;
                                        final Class[] parameterTypes =
                                            {
                                            Instrument.class,
                                            AtomPlugin.class,
                                            ObservatoryUIInterface.class,
                                            String.class
                                            };

                                        // Now get hold of the Constructor
                                        constructor = classObject.getDeclaredConstructor(parameterTypes);

                                        if (constructor != null)
                                            {
                                            final Object objArguments[];

                                            objArguments = new Object[4];
                                            objArguments[0] = instrumentxml;
                                            objArguments[1] = hostatom;
                                            objArguments[2] = hostui;
                                            objArguments[3] = resourcekey;

                                            instrument = (ObservatoryInstrumentInterface)constructor.newInstance(objArguments);
                                            boolLoaded = true;
                                            }
                                        else
                                            {
                                            FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "Constructor not found");
                                            }
                                        }
                                    else
                                        {
                                        FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "Class is not a subclass of " + AbstractObservatoryInstrument.class.getName());
                                        }
                                    }
                                else
                                    {
                                    FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "Class has no superclass");
                                    }
                                }
                            else
                                {
                                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "Incorrect interface " + interfaces[i].getName());
                                }
                            }
                        }
                    else
                        {
                        FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "Class is an interface only");
                        }
                    }
                else
                    {
                    FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "No interfaces found");
                    }
                }

            catch(NoSuchMethodException exception)
                {
                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "NoSuchMethodException [classname=" + strClassname + "]");
                }

            catch(SecurityException exception)
                {
                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "SecurityException [classname=" + strClassname + "]");
                }

            catch (InstantiationException exception)
                {
                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "InstantiationException [classname=" + strClassname + "]");
                }

            catch (IllegalAccessException exception)
                {
                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "IllegalAccessException [classname=" + strClassname + "]");
                }

            catch (IllegalArgumentException exception)
                {
                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "IllegalArgumentException [classname=" + strClassname + "]");
                }

            catch (InvocationTargetException exception)
                {
                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "InvocationTargetException [classname=" + strClassname + "]");
                }

            catch (ClassNotFoundException e)
                {
                FrameworkSingletons.LOGGER.error(ObservatoryInstrumentInterface.INSTRUMENT_NOT_LOADED + "ClassNotFoundException [classname=" + strClassname + "]");
                }
            }

        return (instrument);
        }


    /***********************************************************************************************
     * Instantiate the DAO used by this Instrument to access its Controller.
     *
     * @param instrument
     * @param instrumentxml
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public static ObservatoryInstrumentDAOInterface instantiateDao(final ObservatoryInstrumentInterface instrument,
                                                                   final Instrument instrumentxml)
        {
        ObservatoryInstrumentDAOInterface dao;

        dao = null;

        if ((instrument != null)
            && (instrumentxml != null)
            && (XmlBeansUtilities.isValidXml(instrumentxml))
            && (instrumentxml.getDAO() != null))
            {
            dao = DAOHelper.instantiateDAO(instrument, instrumentxml.getDAO().getDaoClassname());
            }

        return (dao);
        }


    /***********************************************************************************************
     * Instantiate a DaoPort, given its Streams.
     *
     * @param instrument
     * @param portname
     * @param description
     * @param resourcekey
     * @param txstream
     * @param rxstream
     * @param obsclock
     *
     * @return DaoPortInterface
     */

    public static DaoPortInterface instantiatePort(final Instrument instrument,
                                                   final String portname,
                                                   final String description,
                                                   final String resourcekey,
                                                   final String txstream,
                                                   final String rxstream,
                                                   final ObservatoryClockInterface obsclock)
        {
        DaoPortInterface daoPort;

        daoPort = null;

        // Validate the Port name and Stream classnames, and try to instantiate the Streams
        if ((portname != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(portname))
            && (description != null)
            && (txstream != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(txstream))
            && (rxstream != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(rxstream))
            && (resourcekey != null))
            {
            final PortTxStreamInterface txStream;
            final PortRxStreamInterface rxStream;

            txStream = StreamUtilities.instantiateTxStream(txstream, resourcekey);
            rxStream = StreamUtilities.instantiateRxStream(rxstream, resourcekey);

            if ((txStream != null)
                && (rxStream != null))
                {
                daoPort = new DaoPort(PORT_CONTROLLER,
                                      portname,
                                      description,
                                      resourcekey,
                                      txStream,
                                      rxStream,
                                      obsclock);

                // Now we have the Port and its Streams, link them together
                txStream.setHostPort(daoPort);
                rxStream.setHostPort(daoPort);
                }
            else
                {
                cannotInstantiateStream(instrument);
                }
            }
        else
            {
            incorrectStreamClassname(instrument);
            }

        return (daoPort);
        }


    /***********************************************************************************************
     * Check that if the Controller has Commands, then there is also a DAO.
     *
     * @param instrument
     */

    public static void checkOrphanCommands(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_NO_DAO,
            MSG_OFF_LINE
            };

        // There is no DAO, but are there any Commands?
        // If not, fail silently, since no DAO and no Commands is probably intentional
        if ((instrument.getController() != null)
            && (instrument.getController().getCommandList()) != null
            && (!instrument.getController().getCommandList().isEmpty()))
            {
            // There are Commands which we can't execute because there's no DAO instance,
            // so that's an error, so tell the User
            // ToDo check commands are remote...
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                                          message,
                                          instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                          JOptionPane.WARNING_MESSAGE);
            }
        else
            {
            FrameworkSingletons.LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                       "AbstractObservatoryInstrument.checkOrphanCommands() has no orphans");
            }
        }


    /***********************************************************************************************
     * Indicate if the DAO cannot be initialised.
     *
     * @param instrument
     */

    public static void cannotInitialiseDao(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_NO_DAO,
            MSG_OFF_LINE
            };

        // There is a DAO, but we can't initialise it, so that's an error
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Indicate if a stream classname is incorrect in some way.
     *
     * @param instrument
     */

    public static void incorrectStreamClassname(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_NO_DAO,
            MSG_STREAM_CLASSNAME
            };

        // There is a DAO, but we can't instantiate the streams, so that's an error
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Indicate if a stream cannot be instantiated.
     *
     * @param instrument
     */

    public static void cannotInstantiateStream(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_NO_DAO,
            MSG_STREAM_INSTANTIATE
            };

        // There is a DAO, but we can't instantiate the streams, so that's an error
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Indicate if a Port cannot be instantiated.
     *
     * @param instrument
     */

    public static void cannotInstantiatePort(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_NO_DAO,
            MSG_STREAM_INSTANTIATE
            };

        // There is a DAO, but we can't instantiate the streams, so that's an error
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Indicate if a Port cannot be opened.
     *
     * @param instrument
     */

    public static void cannotOpenPort(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_NO_DAO,
            MSG_PORT_OPEN
            };

        // There is a DAO, but we can't open the Port, so that's an error
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Indicate if a Port cannot be closed.
     *
     * @param instrument
     */

    public static void cannotClosePort(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_PORT_CLOSE
            };

        // There is a DAO, but we can't close the Port, so that's an error
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Indicate if the StaribusPort is already assigned.
     *
     * @param instrument
     */

    public static void staribusPortAlreadyAssigned(final Instrument instrument)
        {
        final String [] message =
            {
            MSG_COMMON_PORT
            };

        // There is a DAO, but we can't instantiate the streams, so that's an error
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null,
                                      message,
                                      instrument.getIdentifier() + MSG_DATA_ACCESS_UNAVAILABLE,
                                      JOptionPane.WARNING_MESSAGE);
        }


    /***********************************************************************************************
     * Return a flag to indicate if this Instrument is an ObservatoryClock.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isObservatoryClock(final ObservatoryInstrumentInterface instrument)
        {
        final boolean boolClock;

        boolClock = (instrument instanceof ObservatoryClockInterface);

        return (boolClock);
        }


    /***********************************************************************************************
     * Return a flag to indicate if this Instrument is an ObservatoryLog.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isObservatoryLog(final ObservatoryInstrumentInterface instrument)
        {
        final boolean boolLog;

        boolLog = (instrument instanceof ObservatoryLogInterface);

        return (boolLog);
        }


    /***********************************************************************************************
     * Create the panel containing the Start and Stop buttons.
     * See the code in AbstractObservatoryInstrumentDAO and AbstractObservatoryInstrument,
     * start() and stop().
     *
     * @param instrument
     * @param buttonon
     * @param buttonoff
     *
     * @return JPanel
     */

    public static JPanel createButtonPanel(final ObservatoryInstrumentInterface instrument,
                                           final JButton buttonon,
                                           final JButton buttonoff)
        {
        // Buttons
        final JPanel panelButtons;

        panelButtons = new JPanel();
        //panelButtons.setBorder(BorderFactory.createEtchedBorder());
        panelButtons.setLayout(new BoxLayoutFixed(panelButtons, BoxLayoutFixed.X_AXIS));
        panelButtons.setOpaque(false);
        panelButtons.setMinimumSize(InstrumentSelector.DIM_BUTTON_PANEL);
        panelButtons.setMaximumSize(InstrumentSelector.DIM_BUTTON_PANEL);
        panelButtons.setPreferredSize(InstrumentSelector.DIM_BUTTON_PANEL);

        buttonon.setBackground(Color.green);
        buttonon.setMinimumSize(InstrumentSelector.DIM_BUTTON);
        buttonon.setMaximumSize(InstrumentSelector.DIM_BUTTON);
        buttonon.setPreferredSize(InstrumentSelector.DIM_BUTTON);
        buttonon.addActionListener(new ActionListener()
           {
           public void actionPerformed(final ActionEvent event)
               {
               //-----------------------------------------------------------------------------------
               // Select this Instrument immediately, since this is driven from the UI

               if ((instrument.getHostUI() != null)
                   && (instrument.getInstrumentPanel() != null))
                   {
                   // Hide the existing UI, if different from the requested UI
                   // Show the new UI in its previous state
                   instrument.getHostUI().setUIOccupant(instrument.getInstrumentPanel());

                   // Select the Instrument
                   // See similar code in AbstractObservatoryInstrument.initialise()  mouseClicked()
                   if (instrument.getHostUI().getCurrentGroupInstrumentSelector() != null)
                       {
                       instrument.getHostUI().getCurrentGroupInstrumentSelector().setSelectedInstrument(instrument);
                       }
                   }

               //-----------------------------------------------------------------------------------
               // Update the ControlPanel Buttons

               buttonon.setBackground(buttonon.getBackground().brighter());
               buttonon.setEnabled(false);
               buttonon.setToolTipText(EMPTY_STRING);

               buttonoff.setBackground(buttonoff.getBackground().darker());
               buttonoff.setEnabled(true);
               buttonoff.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_STOP);

               //-----------------------------------------------------------------------------------
               // Start the Instrument

               if ((InstrumentState.INITIALISED.equals(instrument.getInstrumentState()))
                   || (InstrumentState.STOPPED.equals(instrument.getInstrumentState())))
                   {
                   //LOGGER.debugTimedEvent("Starting " + getInstrument().getName());
                   if (instrument.start())
                       {
                       // TODO REVIEW REPEATED EVENT
//                       instrument.notifyInstrumentStateChangedEvent(instrument,
//                                                                    instrument,
//                                                                    instrument.getInstrumentState(),
//                                                                    0,
//                                                                    UNEXPECTED);
                       if ((instrument.getDAO() != null)
                           && (instrument.getDAO().getWrappedData() != null))
                           {
                           SimpleEventLogUIComponent.logEvent(instrument.getDAO().getEventLogFragment(),
                                                              EventStatus.INFO,
                                                              METADATA_TARGET_INSTRUMENT
                                                                  + METADATA_ACTION_START
                                                                  + METADATA_NAME
                                                                      + instrument.getInstrument().getName()
                                                                      + TERMINATOR
                                                                  + METADATA_ORIGIN_USER,
                                                              instrument.getDAO().getLocalHostname(),
                                                              instrument.getDAO().getObservatoryClock());

                           // Force the log to update, since we are not working via a DAO
                           // Only refresh the data if visible
                           instrument.setWrappedData(instrument.getDAO().getWrappedData(), false, true);
                           }
                       }
                   else
                       {
                       // We are unable to start the Instrument
                       MODEL_CONTROLLER.unableToControlPlugin(instrument.getHostAtom());
                       }
                   }
               }
           });

        buttonoff.setBackground(Color.red);
        buttonoff.setMinimumSize(InstrumentSelector.DIM_BUTTON);
        buttonoff.setMaximumSize(InstrumentSelector.DIM_BUTTON);
        buttonoff.setPreferredSize(InstrumentSelector.DIM_BUTTON);
        buttonoff.addActionListener(new ActionListener()
           {
           public void actionPerformed(final ActionEvent event)
               {
               //-----------------------------------------------------------------------------------
               // Select this Instrument immediately, since this is driven from the UI

               if ((instrument.getHostUI() != null)
                   && (instrument.getInstrumentPanel() != null))
                   {
                   // Hide the existing UI, if different from the requested UI
                   // Show the new UI in its previous state
                   instrument.getHostUI().setUIOccupant(instrument.getInstrumentPanel());

                   // Select the Instrument
                   // See similar code in AbstractObservatoryInstrument.initialise()  mouseClicked()
                   if (instrument.getHostUI().getCurrentGroupInstrumentSelector() != null)
                       {
                       instrument.getHostUI().getCurrentGroupInstrumentSelector().setSelectedInstrument(instrument);
                       }
                   }

               //-----------------------------------------------------------------------------------
               // Update the ControlPanel Buttons

               buttonon.setBackground(buttonon.getBackground().darker());
               buttonon.setEnabled(true);
               buttonon.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_START);

               buttonoff.setBackground(buttonoff.getBackground().brighter());
               buttonoff.setEnabled(false);
               buttonoff.setToolTipText(EMPTY_STRING);

               //-----------------------------------------------------------------------------------
               // Stop the Instrument

               if (InstrumentState.isDoingSomething(instrument))
                   {
                   // We have to log before the stop(), because we are about to lose the DAO!
                   if ((instrument.getDAO() != null)
                       && (instrument.getDAO().getWrappedData() != null))
                       {
                       SimpleEventLogUIComponent.logEvent(instrument.getDAO().getEventLogFragment(),
                                                          EventStatus.INFO,
                                                          METADATA_TARGET_INSTRUMENT
                                                              + METADATA_ACTION_STOP
                                                              + METADATA_NAME
                                                                  + instrument.getInstrument().getName()
                                                                  + TERMINATOR
                                                              + METADATA_ORIGIN_USER,
                                                          instrument.getDAO().getLocalHostname(),
                                                          instrument.getDAO().getObservatoryClock());

                       // Force the log to update, since we are not working via a DAO
                       // Only refresh the data if visible
                       instrument.setWrappedData(instrument.getDAO().getWrappedData(), false, true);
                       }

                   // This issues notifyInstrumentStateChangedEvent()
                   if (!instrument.stop())
                       {
                       // We are unable to stop the Instrument
                       MODEL_CONTROLLER.unableToControlPlugin(instrument.getHostAtom());
                       }
                   }
               }
           });

        // Initialise the Button states
        buttonon.setBackground(buttonon.getBackground().darker());
        buttonon.setEnabled(true);
        buttonon.setToolTipText(ObservatoryInstrumentInterface.TOOLTIP_START);

        buttonoff.setBackground(buttonoff.getBackground().brighter());
        buttonoff.setEnabled(false);
        buttonoff.setToolTipText("");

        if (instrument.getInstrument().getControllable())
           {
           panelButtons.add(buttonon);
           panelButtons.add(Box.createHorizontalStrut(InstrumentSelector.WIDTH_BUTTON_SPACING));
           panelButtons.add(buttonoff);
           }

        return panelButtons;
        }


    /***********************************************************************************************
     * Timestamp a Command message in preparation for sending to the Port.
     *
     * @param commandmessage
     * @param clock
     *
     * @return CommandMessageInterface
     */

    public static CommandMessageInterface timestampCommandMessage(final CommandMessageInterface commandmessage,
                                                                  final ObservatoryClockInterface clock)
        {
        CommandMessageInterface commandMessage;

        commandMessage = null;

        if (commandmessage != null)
            {
            final TimeZone timeZone;
            final Locale locale;

            commandMessage = commandmessage;

            // Tag the message with the time at which it was queued
            timeZone = FrameworkSingletons.REGISTRY.getFrameworkTimeZone();
            locale = new Locale(FrameworkSingletons.REGISTRY.getFramework().getLanguageISOCode(),
                                FrameworkSingletons.REGISTRY.getFramework().getCountryISOCode());
            commandMessage.setTxCalendar(clock.getSystemCalendar(timeZone, locale));
            }

        return (commandMessage);
        }


    /***********************************************************************************************
     * Timestamp a Response Message after reception.
     *
     * @param responsemessage
     * @param clock
     *
     * @return ResponseMessageInterface
     */

    public static ResponseMessageInterface timestampResponseMessage(final ResponseMessageInterface responsemessage,
                                                                    final ObservatoryClockInterface clock)
        {
        ResponseMessageInterface responseMessage;

        responseMessage = null;

        if (responsemessage != null)
            {
            final TimeZone timeZone;
            final Locale locale;

            responseMessage = responsemessage;

            // Tag the message with the time at which it was received
            timeZone = FrameworkSingletons.REGISTRY.getFrameworkTimeZone();
            locale = new Locale(FrameworkSingletons.REGISTRY.getFramework().getLanguageISOCode(),
                                FrameworkSingletons.REGISTRY.getFramework().getCountryISOCode());
            responseMessage.setRxCalendar(clock.getSystemCalendar(timeZone, locale));
            }

        return (responseMessage);
        }


    /***********************************************************************************************
     * A simple memory diagnostic.
     *
     * @param source
     */

    public static void diagnoseMemory(final String source)
        {
        final Runtime runTime;

        runTime = Runtime.getRuntime();

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               source + " [target=memory] [action=diagnose] "
                    + "[max=" + runTime.maxMemory() + "] "
                    + "[total=" + runTime.totalMemory() + "] "
                    + "[free=" + runTime.freeMemory() + "] "
                    + "[cpus=" + runTime.availableProcessors() + "]");
        }


    /**********************************************************************************************/
    /* ConfigurationData                                                                          */
    /***********************************************************************************************
     * Get a Configuration item value from its ResourceKey.
     *
     * @param configdata
     * @param key
     *
     * @return String
     */

    public static String getConfigurationValueByKey(final Vector<Vector> configdata,
                                                    final String key)
        {
        String strConfigurationValue;

        strConfigurationValue = EMPTY_STRING;

        if ((configdata != null)
            && (!configdata.isEmpty())
            && (key != null)
            && (!EMPTY_STRING.equals(key.trim())))
            {
            final Iterator<Vector> iterConfig;
            boolean boolFoundIt;

            iterConfig = configdata.iterator();
            boolFoundIt = false;

            while ((!boolFoundIt)
                && (iterConfig.hasNext()))
                {
                final Vector<Object> vecData;

                vecData = iterConfig.next();

                if ((vecData != null)
                    && (vecData.size() == ConfigurationHelper.CONFIG_DATA_SIZE)
                    && (key.equals(vecData.get(InstrumentUIHelper.CONFIG_INDEX_KEY)))
                    && (vecData.get(InstrumentUIHelper.CONFIG_INDEX_VALUE) instanceof String))
                    {
                    strConfigurationValue = (String)vecData.get(InstrumentUIHelper.CONFIG_INDEX_VALUE);
                    boolFoundIt = true;
                    }
                }
            }

        return (strConfigurationValue);
        }


    /***********************************************************************************************
     * Indicate if the Instrument has a Virtual Controller.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isVirtualController(final Instrument instrument)
        {
        final int intAddress;
        final boolean boolIsVirtualController;

        intAddress = getVirtualAddressAsInteger(instrument);

        boolIsVirtualController = (intAddress >= 0);
//                                    && (intAddress == VIRTUAL_CONTROLLER_ADDRESS));

        return (boolIsVirtualController);
        }


    /***********************************************************************************************
     * Get the VirtualAddress as an int (allow non-zero addresses for later expansion).
     * Return -1 if the Address is null or invalid.
     *
     * @param instrument
     *
     * @return int
     */

    public static int getVirtualAddressAsInteger(final Instrument instrument)
        {
        if ((instrument != null)
            && (instrument.getController() != null)
            && (instrument.getController().getStaribusAddress() == null)
            && (instrument.getController().getIPAddress() == null)
            && (instrument.getController().getVirtualAddress() != null))
            {
            int intAddress;

            try
                {
                intAddress = Integer.parseInt(instrument.getController().getVirtualAddress());
                }

            catch (NumberFormatException exception)
                {
                LOGGER.error("VirtualAddress is not an Integer [address="
                                + instrument.getController().getVirtualAddress() + "]");

                intAddress = -1;
                }

            return (intAddress);
            }
        else
            {
            return (-1);
            }
        }


    /***********************************************************************************************
     * Indicate if the Instrument has a Staribus Controller.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isStaribusController(final Instrument instrument)
        {
        final int intAddress;
        final boolean boolIsStaribusController;

        intAddress = getStaribusAddressAsInteger(instrument);

        // Do not allow address zero, reserved for VirtualControllers
        boolIsStaribusController = ((intAddress >= 0)
                                    && (intAddress != VIRTUAL_CONTROLLER_ADDRESS));

        return (boolIsStaribusController);
        }


    /***********************************************************************************************
     * Get the StaribusAddress as an int.
     * Return -1 if the Address is null or invalid.
     *
     * @param instrument
     *
     * @return int
     */

    public static int getStaribusAddressAsInteger(final Instrument instrument)
        {
        if ((instrument != null)
            && (instrument.getController() != null)
            && (instrument.getController().getVirtualAddress() == null)
            && (instrument.getController().getIPAddress() == null)
            && (instrument.getController().getStaribusAddress() != null))
            {
            int intAddress;

            try
                {
                intAddress = Integer.parseInt(instrument.getController().getStaribusAddress());
                }

            catch (NumberFormatException exception)
                {
                LOGGER.error("StaribusAddress is not an Integer [address="
                                + instrument.getController().getStaribusAddress() + "]");

                intAddress = -1;
                }

            return (intAddress);
            }
        else
            {
            return (-1);
            }
        }


    /***********************************************************************************************
     * Indicate if the Instrument has an Ethernet Controller, i.e. has an IPAddress.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isEthernetController(final Instrument instrument)
        {
        final boolean boolIsEthernetController;

        // Assume that the XML must be valid against the Regex, and so the IPAddress is valid
        boolIsEthernetController = (instrument != null)
                                    && (instrument.getController() != null)
                                    && (instrument.getController().getVirtualAddress() == null)
                                    && (instrument.getController().getStaribusAddress() == null)
                                    && (instrument.getController().getIPAddress() != null);

        return (boolIsEthernetController);
        }


    /***********************************************************************************************
     * Remove the optional Port from an IPAddress string.
     *
     * @param ipaddress
     *
     * @return String
     */

    public static String getIPAddressWithoutPort(final String ipaddress)
        {
        final String strIPAddress;

        // The IPAddress may be 1.2.3.4:1234
        if ((ipaddress != null)
            && (!EMPTY_STRING.equals(ipaddress))
            && (Pattern.matches(REGEX_IPADDRESS, ipaddress)))
            {
            if (ipaddress.contains(COLON))
                {
                strIPAddress = ipaddress.substring(0, ipaddress.indexOf(COLON));
                }
            else
                {
                // There was no colon
                strIPAddress = ipaddress;
                }
            }
        else
            {
            // There was no IPAddress
            strIPAddress = EMPTY_STRING;
            }

        return (strIPAddress);
        }


    /***********************************************************************************************
     * Get the Port ID from an IPAddress string.
     * If the Port is not specified, return the default.
     *
     * @param ipaddress
     * @param defaultport
     *
     * @return int
     */

    public static int getPortFromIPAddress(final String ipaddress,
                                           final int defaultport)
        {
        int intPort;

        // The IPAddress may be 1.2.3.4:1234
        if ((ipaddress != null)
            && (!EMPTY_STRING.equals(ipaddress))
            && (Pattern.matches(REGEX_IPADDRESS, ipaddress))
            && (ipaddress.contains(COLON)))
            {
            try
                {
                intPort = Integer.parseInt(ipaddress.substring(ipaddress.indexOf(COLON) + 1));
                }

            catch (NumberFormatException exception)
                {
                intPort = defaultport;
                }
            }
        else
            {
            // There was no Port
            intPort = defaultport;
            }

        return (intPort);
        }


    /***********************************************************************************************
     * Start an Observatory Instrument.
     *
     * @param obsinstrument
     *
     * @return boolean
     */

    public static boolean startInstrument(final ObservatoryInstrumentInterface obsinstrument)
        {
        boolean boolSuccess;

        boolSuccess = true;

        if (obsinstrument != null)
            {
            // Update the UI
            if (obsinstrument.getControlPanel() != null)
                {
                obsinstrument.getControlPanel().runUI();
                }

            if (obsinstrument.getInstrumentPanel() != null)
                {
                boolSuccess = obsinstrument.getInstrumentPanel().start();

                if (boolSuccess)
                    {
                    obsinstrument.getInstrumentPanel().runUI();
                    REGISTRY_MODEL.rebuildNavigation(null, obsinstrument.getInstrumentPanel());
                    }
                }

            // Update the Instrument Resources
            obsinstrument.readResources();

            // Get the DAO used to communicate with the Instrument's Controller, if possible
            if ((boolSuccess)
                && (obsinstrument.getInstrument() != null)
                && (obsinstrument.getContext() != null))
                {
                obsinstrument.setDAO(instantiateDao(obsinstrument, obsinstrument.getInstrument()));

                if (obsinstrument.getDAO() != null)
                    {
                    if (obsinstrument.getDAO().initialiseDAO(obsinstrument.getResourceKey()))
                        {
                        // The DAO initialised OK
                        // Does the DAO have a Port?
                        // The Port is either a unique instance for this DAO,
                        // OR shared use of the Observatory StaribusPort  (xsd:choice)

                        // Establish a Port connection if possible, using the specified Tx and Rx streams
                        // Rely on xsd:choice not to give us both Port and StaribusPort!
                        if ((obsinstrument.getInstrument().getDAO().getPort() != null)
                            || ((obsinstrument.getInstrument().getDAO().getStaribusPort() != null)
                                && (SerialConfigurationHelper.PORT_COMMON_ID.equals(obsinstrument.getInstrument().getDAO().getStaribusPort()))))
                            {
                            final String strPortName;
                            final String strDescription;
                            final String strPortResourceKey;
                            final String strTxStreamClassname;
                            final String strRxStreamClassname;
                            final DaoPortInterface daoPort;

                            // See which kind of Port we have
                            if (obsinstrument.getInstrument().getDAO().getPort() != null)
                                {
                                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                       "ObservatoryInstrumentHelper.startInstrument() Instantiate a unique Port");

                                // We have to instantiate a new Port specifically for this Instrument,
                                // getting the port information from getInstrument().getDAO().getPort()
                                strPortName = obsinstrument.getInstrument().getDAO().getPort().getName();
                                strDescription = obsinstrument.getInstrument().getDAO().getPort().getDescription();
                                // Instrument Port ResourceKey = Starbase.Observatory.InstrumentKey.PortKey.
                                strPortResourceKey = obsinstrument.getHostAtom().getResourceKey()
                                                        + obsinstrument.getInstrument().getResourceKey()
                                                        + RegistryModelPlugin.DELIMITER_RESOURCE
                                                        + obsinstrument.getInstrument().getDAO().getPort().getResourceKey()
                                                        + RegistryModelPlugin.DELIMITER_RESOURCE;
                                strTxStreamClassname = obsinstrument.getInstrument().getDAO().getPort().getTxStream();
                                strRxStreamClassname = obsinstrument.getInstrument().getDAO().getPort().getRxStream();
                                LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                       "Port [ResourceKey=" + strPortResourceKey + "]");

                                daoPort = instantiatePort(obsinstrument.getInstrument(),
                                                          strPortName,
                                                          strDescription,
                                                          strPortResourceKey,
                                                          strTxStreamClassname,
                                                          strRxStreamClassname,
                                                          obsinstrument.getObservatoryClock());
                                if (daoPort != null)
                                    {
                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "ObservatoryInstrumentHelper.startInstrument() Unique Port assembleInstrumentConfiguration()");

                                    // Make this DAO aware of its new Port
                                    obsinstrument.getDAO().setPort(daoPort);

                                    // Make the Port aware of the parent DAO
                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "ObservatoryInstrumentHelper.startInstrument() Unique Port");
                                    daoPort.addHostDAO(obsinstrument.getDAO());

                                    // Request that a DaoPort be attached to the PortController as soon as possible.
                                    PORT_CONTROLLER.requestPortAddition(daoPort);

                                    // We have a good ordinary unique Port, so open the streams!
                                    if (!daoPort.open())
                                        {
                                        cannotOpenPort(obsinstrument.getInstrument());
                                        // We'll continue with some Commands disabled
                                        boolSuccess = true;
                                        }
                                    else
                                        {
                                        // Add DAO, Port & Stream configuration to Instrument Configuration
                                        obsinstrument.setInstrumentConfiguration(
                                                ConfigurationHelper.assembleInstrumentConfiguration(
                                                        obsinstrument));
                                        }
                                    }
                                else
                                    {
                                    cannotInstantiatePort(obsinstrument.getInstrument());
                                    // This must fail
                                    boolSuccess = false;
                                    }
                                }
                            else
                                {
                                // We are using the StaribusPort, and know it is valid
                                // Has the StaribusPort already been attached to the PortController?
                                if (PORT_CONTROLLER.getStaribusPort() == null)
                                    {
                                    final PortType staribusPort;

                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "ObservatoryInstrumentHelper.startInstrument() Instantiate Common Port");

                                    // Get the port information awkwardly from the host UI...
                                    // The InstrumentsDoc was validated when the Instrument was constructed
                                    staribusPort = obsinstrument.getHostUI().getInstrumentsDoc().getInstruments().getStaribusPort();

                                    strPortName = staribusPort.getName();
                                    strDescription = staribusPort.getDescription();
                                    // Common Port ResourceKey = Starbase.Observatory.PortKey.
                                    // i.e. there is no associated Instrument
                                    strPortResourceKey = obsinstrument.getHostAtom().getResourceKey()
                                                            + staribusPort.getResourceKey()
                                                            + RegistryModelPlugin.DELIMITER_RESOURCE;
                                    strTxStreamClassname = staribusPort.getTxStream();
                                    strRxStreamClassname = staribusPort.getRxStream();
                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "ObservatoryInstrumentHelper.startInstrument() StaribusPort [ResourceKey=" + strPortResourceKey + "]");

                                    // Instantiate the Port and the associated Streams
                                    daoPort = instantiatePort(obsinstrument.getInstrument(),
                                                              strPortName,
                                                              strDescription,
                                                              strPortResourceKey,
                                                              strTxStreamClassname,
                                                              strRxStreamClassname,
                                                              obsinstrument.getObservatoryClock());
                                    // Were we successful?
                                    if (daoPort != null)
                                        {
                                        final boolean boolFirstStaribusPort;

                                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                               "ObservatoryInstrumentHelper.startInstrument() Common Port assembleInstrumentConfiguration()");

                                        // Record the assignment of the StaribusPort
                                        daoPort.makeStaribusPort();

                                        // Make this DAO aware of its new Port
                                        obsinstrument.getDAO().setPort(daoPort);

                                        // Make the Port aware of the parent DAO
                                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                               "ObservatoryInstrumentHelper.startInstrument() Common Port");
                                        daoPort.addHostDAO(obsinstrument.getDAO());

                                        // Request that a DaoPort be attached to the PortController as soon as possible.
                                        PORT_CONTROLLER.requestPortAddition(daoPort);

                                        // Record the assignment of the StaribusPort in the PortController,
                                        boolFirstStaribusPort = PORT_CONTROLLER.setStaribusPort(daoPort);

                                        // Check to see if this is the first time round...
                                        if (boolFirstStaribusPort)
                                            {
                                            // We have a good StaribusPort, first time round, so open the streams!
                                            if (!daoPort.open())
                                                {
                                                cannotOpenPort(obsinstrument.getInstrument());
                                                // We'll continue with some Commands disabled
                                                boolSuccess = true;
                                                }
                                            else
                                                {
                                                // Add DAO, Port & Stream configuration to Instrument Configuration
                                                obsinstrument.setInstrumentConfiguration(ConfigurationHelper.assembleInstrumentConfiguration(obsinstrument));
                                                }
                                            }
                                        else
                                            {
                                            // If it isn't the first time, something has gone wrong,
                                            // so tell the User that we can't use the DAO for this Instrument
                                            staribusPortAlreadyAssigned(obsinstrument.getInstrument());
                                            // This must fail
                                            boolSuccess = false;
                                            }
                                        }
                                    else
                                        {
                                        cannotInstantiatePort(obsinstrument.getInstrument());
                                        // This must fail
                                        boolSuccess = false;
                                        }
                                    }
                                else
                                    {
                                    // There is no need to instantiate another Port (or Streams) for this Instrument,
                                    // just assign the StaribusPort to this DAO (already initialised)
                                    // We know the StaribusPort is not null...

                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "ObservatoryInstrumentHelper.startInstrument() Use existing Common Port");

                                    // Make this Instrument's DAO aware of its new StaribusPort
                                    obsinstrument.getDAO().setPort(PORT_CONTROLLER.getStaribusPort());

                                    // Make the StaribusPort aware of the parent DAO
                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "ObservatoryInstrumentHelper.startInstrument() Existing Common Port");
                                    PORT_CONTROLLER.getStaribusPort().addHostDAO(obsinstrument.getDAO());

                                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                           "ObservatoryInstrumentHelper.startInstrument() Common Port assembleInstrumentConfiguration()");

                                    // Get the StaribusPort Configuration again, just for this Instrument
                                    // Add DAO, Port & Stream configuration to Instrument Configuration
                                    obsinstrument.setInstrumentConfiguration(ConfigurationHelper.assembleInstrumentConfiguration(obsinstrument));

                                    // The Port and streams are already open, so we just continue...
                                    }
                                }
                            }
                        else
                            {
                            // There is a DAO with no Port or StaribusPort, which is permissible...
                            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                                   "ObservatoryInstrumentHelper.startInstrument() " + obsinstrument.getInstrument().getIdentifier() + " has no Port or StaribusPort");
                            }
                        }
                    else
                        {
                        // This may not matter, so leave the state alone
                        cannotInitialiseDao(obsinstrument.getInstrument());
                        }
                    }
                else
                    {
                    // It is Ok not to have a DAO
                    checkOrphanCommands(obsinstrument.getInstrument());
                    }
                }

            if (obsinstrument.getContext() != null)
                {
                // Always come back viewing Commands
                obsinstrument.getContext().setViewingMode(ViewingMode.COMMAND_LOG);
                MacroManagerUtilities.restoreCommandLifecycleLog(obsinstrument.getContext());
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Stop an Observatory Instrument.
     *
     * @param instrument
     * @param dao
     * @param port
     *
     * @return boolean
     *
     * @throws IOException
     */

    public static boolean stopInstrument(final ObservatoryInstrumentInterface instrument,
                                         final ObservatoryInstrumentDAOInterface dao,
                                         final DaoPortInterface port) throws IOException
        {
        boolean boolSuccess;

        boolSuccess = true;

        if (instrument != null)
            {
            // Update the UI
            if (instrument.getControlPanel() != null)
                {
                instrument.getControlPanel().stopUI();
                }

            if (instrument.getInstrumentPanel() != null)
                {
                instrument.getInstrumentPanel().runUI();
                REGISTRY_MODEL.rebuildNavigation(null, instrument.getInstrumentPanel());

                instrument.getInstrumentPanel().stopUI();

                // This could fail, and so return false
                boolSuccess = instrument.getInstrumentPanel().stop();
                }

            // Does the Instrument have a DAO?
            if (dao != null)
                {
                instrument.getInstrumentConfiguration().removeAll(dao.getDAOConfiguration());

                // Is there a Port to close?
                if (port != null)
                    {
                    LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                           "AbstractObservatoryInstrumentDAO.stopInstrument() REMOVE PORT CONFIG");
                    // Remove Port configuration from Instrument Configuration regardless
                    // of which type of Port
                    instrument.getInstrumentConfiguration().removeAll(port.getPortConfiguration());

                    if (port.getTxStream() != null)
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               "AbstractObservatoryInstrumentDAO.stopInstrument() REMOVE TX STREAM CONFIG");
                        // Remove Stream configuration from Instrument Configuration regardless
                        instrument.getInstrumentConfiguration().removeAll(port.getTxStream().getStreamConfiguration());

                        // Close the Stream only if it does not belong to the StaribusPort
                        if (!port.isStaribusPort())
                            {
                            port.getTxStream().close();
                            }
                        }

                    if (port.getRxStream() != null)
                        {
                        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                                               "AbstractObservatoryInstrumentDAO.stopInstrument() REMOVE RX STREAM CONFIG");
                        // Remove Stream configuration from Instrument Configuration regardless
                        instrument.getInstrumentConfiguration().removeAll(port.getRxStream().getStreamConfiguration());

                        // Close the Stream only if it does not belong to the StaribusPort
                        if (!port.isStaribusPort())
                            {
                            port.getRxStream().close();
                            }
                        }

                    // Remove this Port from the List under control, but only if not the StaribusPort!
                    // Request that the Port be removed from the PortController as soon as possible.
                    if (!port.isStaribusPort())
                        {
                        port.close();
                        PORT_CONTROLLER.requestPortRemoval(port);
                        }

                    // Remove the DAO from the List managed by the Port
                    port.removeHostDAO(dao);
                    }
                }

            if (instrument.getContext() != null)
                {
                // Always come back viewing Commands
                instrument.getContext().setViewingMode(ViewingMode.COMMAND_LOG);
                MacroManagerUtilities.restoreCommandLifecycleLog(instrument.getContext());
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Run the GarbageCollector.
     * Collected into one place, to make experiments easier.
     * A bit naughty, but it might help...
     */

    public static void runGarbageCollector()
        {
        // Do nothing!
        }

    public static void runGarbageCollector2()
        {
        final SwingWorker workerGC;

        workerGC = new SwingWorker(REGISTRY.getThreadGroup(), "ObservatoryInstrumentHelper.runGarbageCollector()")
            {
            /***************************************************************************************
             * Run the Garbage Collector.
             *
             * @return Object
             */

            public Object construct()
                {
                System.gc();

                return (null);
                }


            /***************************************************************************************
             * Tell the World.
             */

            public void finished()
                {
                final Runtime runTime;

                runTime = Runtime.getRuntime();
                LOGGER.logTimedEvent("[target=gc] [action=run] "
                                     + "[max=" + runTime.maxMemory() + "] "
                                     + "[total=" + runTime.totalMemory() + "] "
                                     + "[free=" + runTime.freeMemory() + "] "
                                     + "[cpus=" + runTime.availableProcessors() + "]");

                }
            };

        workerGC.start();
        }


    /***********************************************************************************************
     * Log the reset() Command for the Instrument.
     *
     * @param obsinstrument
     * @param instrumentxml
     * @param dao
     * @param resetmode
     * @param source
     */

    public static void logReset(final ObservatoryInstrumentInterface obsinstrument,
                                final Instrument instrumentxml,
                                final ObservatoryInstrumentDAOInterface dao,
                                final ResetMode resetmode,
                                final String source)
        {
        if ((obsinstrument != null)
            && (instrumentxml != null)
            && (dao != null))
            {
            final String strIdentifier;

            strIdentifier = instrumentxml.getIdentifier();
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                   + strIdentifier + TERMINATOR
                                               + METADATA_ACTION_RESET
                                               + METADATA_MODE
                                                   + resetmode.getName()
                                                   + TERMINATOR,
                                               source,
                                               obsinstrument.getObservatoryClock());
            }
        }


    /***********************************************************************************************
     * Either find the Current Observatory TimeZone,
     * or provide a default of the Framework TimeZone or GMT+00:00.
     * Guaranteed not to return NULL.
     *
     * @param framework
     * @param dao
     * @param debug
     *
     * @return TimeZone
     */

    public static TimeZone getCurrentObservatoryTimeZone(final FrameworkPlugin framework,
                                                         final ObservatoryInstrumentDAOInterface dao,
                                                         final boolean debug)
        {
        final String SOURCE = "ObservatoryInstrumentHelper.getCurrentObservatoryTimeZone() ";
        final TimeZone timeZone;

        if ((framework != null)
            && (dao != null)
            && (dao.getCurrentObservatoryMetadata() != null))
            {
            final Metadata metadataTimeZone;

            metadataTimeZone = MetadataHelper.getMetadataByKey(dao.getCurrentObservatoryMetadata(),
                                                               MetadataDictionary.KEY_OBSERVATORY_TIMEZONE.getKey());
            if ((metadataTimeZone != null)
                && (XmlBeansUtilities.isValidXml(metadataTimeZone)))
                {
                // This returns the GMT zone if the given ID cannot be understood
                timeZone = TimeZone.getTimeZone(metadataTimeZone.getValue());

                LOGGER.debug(debug,
                             SOURCE + "Observatory Time Zone A"
                                    + "[timezone.id=" + timeZone.getDisplayName()
                                    + "[timezone.metadata=" + metadataTimeZone.getValue()
                                    + "]");
                }
            else
                {
                // We must use the Framework TimeZone
                // This returns the GMT zone if the given ID cannot be understood
                timeZone = TimeZone.getTimeZone(framework.getTimeZoneCode());

                LOGGER.debug(debug,
                             SOURCE + "Observatory Time Zone B"
                                    + "[timezone.id=" + timeZone.getDisplayName()
                                    + "[timezone.framework=" + framework.getTimeZoneCode()
                                    + "]");
                }
            }
        else
            {
            // We must use the Greenwich TimeZone
            timeZone = TimeZone.getTimeZone(DEFAULT_TIME_ZONE_ID);

            LOGGER.debug(debug,
                         SOURCE + "Observatory Time Zone C"
                                + "[timezone.id=" + timeZone.getDisplayName()
                                + "[timezone.gmt=" + DEFAULT_TIME_ZONE_ID
                                + "]");
            }

        return (timeZone);
        }


    /***********************************************************************************************
     * Either find the Current Observatory Locale,
     * or provide a default of the Framework Locale or {en,UK}.
     * Guaranteed not to return NULL.
     *
     * @param framework
     * @param dao
     * @param debug
     *
     * @return Locale
     */

    public static Locale getCurrentObservatoryLocale(final FrameworkPlugin framework,
                                                     final ObservatoryInstrumentDAOInterface dao,
                                                     final boolean debug)
        {
        final String SOURCE = "ObservatoryInstrumentHelper.getCurrentObservatoryLocale() ";
        final Locale locale;

        if ((framework != null)
            && (dao != null)
            && (dao.getCurrentObservatoryMetadata() != null))
            {
            final Metadata metadataLanguage;
            final Metadata metadataCountry;

            metadataLanguage = MetadataHelper.getMetadataByKey(dao.getCurrentObservatoryMetadata(),
                                                               MetadataDictionary.KEY_OBSERVATORY_LANGUAGE.getKey());
            metadataCountry = MetadataHelper.getMetadataByKey(dao.getCurrentObservatoryMetadata(),
                                                              MetadataDictionary.KEY_OBSERVATORY_COUNTRY.getKey());
            if ((metadataLanguage != null)
                && (XmlBeansUtilities.isValidXml(metadataLanguage))
                && (metadataCountry != null)
                && (XmlBeansUtilities.isValidXml(metadataCountry)))
                {
                locale = new Locale(metadataLanguage.getValue(),
                                    metadataCountry.getValue());
                LOGGER.debug(debug,
                             SOURCE + "Observatory Locale defined correctly in CurrentObservatoryMetadata"
                                    + "[locale.country=" + locale.getISO3Country() + "] "
                                    + "[locale.language=" + locale.getISO3Language() + "]");
                }
            else
                {
                // We must use the Framework Locale
                locale = new Locale(framework.getLanguageISOCode(),
                                    framework.getCountryISOCode());
                LOGGER.debug(debug,
                             SOURCE + "Using Framework data for Observatory Locale"
                                    + "[locale.country=" + locale.getISO3Country() + "] "
                                    + "[locale.language=" + locale.getISO3Language() + "]");
                }
            }
        else
            {
            // We must use a default Locale
            // Note that the constructor uses the two-letter ISO 639 codes
            locale = new Locale(Locale.ENGLISH.getLanguage(),
                                Locale.UK.getCountry());
            LOGGER.debug(debug,
                         SOURCE + "Using defaults for Observatory Locale because no Framework or CurrentObservatoryMetadata"
                                + "[locale.country=" + locale.getISO3Country() + "] "
                                + "[locale.language=" + locale.getISO3Language() + "]");
            }

        return (locale);
        }


    /***********************************************************************************************
     * Either find the Current Observatory Calendar, or provide a default.
     *
     * @param framework
     * @param dao
     * @param debug
     *
     * @return Calendar
     */

    public static Calendar getCurrentObservatoryCalendar(final FrameworkPlugin framework,
                                                         final ObservatoryInstrumentDAOInterface dao,
                                                         final boolean debug)
        {
        final String SOURCE = "ObservatoryInstrumentHelper.getCurrentObservatoryCalendar() ";
        final Calendar calendar;
        final TimeZone timeZone;
        final Locale locale;

        // Either find the Current Observatory calendar, or provide a default
        timeZone = getCurrentObservatoryTimeZone(framework, dao, debug);
        locale = getCurrentObservatoryLocale(framework, dao, debug);
        calendar = new GregorianCalendar(timeZone, locale);

        debugCalendar(debug, calendar, SOURCE);

        return (calendar);
        }


    /***********************************************************************************************
     * Debug the specified Calendar.
     *
     * @param debug
     * @param calendar
     * @param message
     */

    public static void debugCalendar(final boolean debug,
                                     final Calendar calendar,
                                     final String message)
        {
        final String SOURCE = "ObservatoryInstrumentHelper.debugCalendar() ";

        // Force recalculation
        calendar.get(Calendar.HOUR_OF_DAY);

        LOGGER.debug(debug, Logger.CONSOLE_SEPARATOR_MINOR);
        LOGGER.debug(debug, SOURCE + "Calendar");
        LOGGER.debug(debug, "Calendar getTime() as formatted Date " + calendar.getTime().toString());
        LOGGER.debug(debug, "[calendar.time.in.millis.date=" + calendar.getTime().getTime() + "]");
        LOGGER.debug(debug, "[calendar.time.in.millis.calendar=" + calendar.getTimeInMillis() + "]");

        LOGGER.debug(debug, "[calendar.era=" + calendar.get(Calendar.ERA) + "]");
        LOGGER.debug(debug, "[calendar.year=" + calendar.get(Calendar.YEAR) + "]");
        LOGGER.debug(debug, "[calendar.month=" + calendar.get(Calendar.MONTH) + "]");
        LOGGER.debug(debug, "[calendar.week.of.year=" + calendar.get(Calendar.WEEK_OF_YEAR) + "]");
        LOGGER.debug(debug, "[calendar.week.of.month=" + calendar.get(Calendar.WEEK_OF_MONTH) + "]");
        LOGGER.debug(debug, "[calendar.date=" + calendar.get(Calendar.DATE) + "]");
        LOGGER.debug(debug, "[calendar.day.of.month=" + calendar.get(Calendar.DAY_OF_MONTH) + "]");
        LOGGER.debug(debug, "[calendar.day.of.year=" + calendar.get(Calendar.DAY_OF_YEAR) + "]");
        LOGGER.debug(debug, "[calendar.day.of.week=" + calendar.get(Calendar.DAY_OF_WEEK) + "]");
        LOGGER.debug(debug, "[calendar.day.of.week.inmonth=" + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) + "]");

        LOGGER.debug(debug, "[calendar.am.pm=" + calendar.get(Calendar.AM_PM) + "]");
        LOGGER.debug(debug, "[calendar.hour=" + calendar.get(Calendar.HOUR) + "]");
        LOGGER.debug(debug, "[calendar.hour.of.day=" + calendar.get(Calendar.HOUR_OF_DAY) + "]");
        LOGGER.debug(debug, "[calendar.minute=" + calendar.get(Calendar.MINUTE) + "]");
        LOGGER.debug(debug, "[calendar.second=" + calendar.get(Calendar.SECOND) + "]");
        LOGGER.debug(debug, "[calendar.millisecond=" + calendar.get(Calendar.MILLISECOND) + "]");

        LOGGER.debug(debug, "[calendar.zoneoffset=" + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000)) + "]");
        LOGGER.debug(debug, "[calendar.dstoffset=" + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000)) + "]");
        // Note: Date.getTimezoneOffset() is deprecated
        LOGGER.debug(debug, "[calendar.timezoneoffset.mins="         + (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000) + "]");
        LOGGER.debug(debug, "[calendar.timezoneoffset(recommended)=" + (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000) + "]");

        // Calendar.TimeZone
        LOGGER.debug(debug, "[calendar.timezone.id=" + (calendar.getTimeZone().getID()) + "]");
        LOGGER.debug(debug, "[calendar.timezone.name=" + (calendar.getTimeZone().getDisplayName()) + "]");
        LOGGER.debug(debug, "[calendar.timezone.rawoffset=" + (calendar.getTimeZone().getRawOffset()) + "]");
        LOGGER.debug(debug, "[calendar.timezone.dstsavings=" + (calendar.getTimeZone().getDSTSavings()) + "]");
        LOGGER.debug(debug, "[calendar.timezone.indaylight=" + (calendar.getTimeZone().inDaylightTime(calendar.getTime())) + "]");

        // Calendar.Date (Time)
        LOGGER.debug(debug, "[calendar.date.GMTstring(deprecated)=" + (calendar.getTime().toGMTString()) + "]");

        // Returns the offset, measured in minutes,
        // for the local time zone relative to UTC that is appropriate for the time represented by this Date object
        LOGGER.debug(debug, "[calendar.date.timezoneoffset(deprecated)=" + (calendar.getTime().getTimezoneOffset()) + "]");

        DateFormat dateFormat = DateFormat.getDateInstance();

        dateFormat.setTimeZone(calendar.getTimeZone());
        LOGGER.debug(debug, "[calendar.date.DateFormat=" + dateFormat.format(calendar.getTime()) + "]");

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        LOGGER.debug(debug, "[calendar.date.DateFormat.GMT=" + dateFormat.format(calendar.getTime()) + "]");
        LOGGER.debug(debug, Logger.CONSOLE_SEPARATOR_MINOR);
        }
    }
