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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  08-11-04    LMN created file from BackupDaemon
//  04-04-05    LMN tidying logging
//  31-01-07    LMN rewriting for new structure, more properties, updated mail.jar
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.frameworks.starbase.tasks;

//--------------------------------------------------------------------------------------------------
// Imports

import com.sun.mail.smtp.SMTPTransport;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.impl.TaskData;
import org.lmn.fc.ui.components.BlankUIComponent;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;


/***************************************************************************************************
 * The MailDaemon FrameworkTask. This Task sends any Emails in the Outbox, on a configurable Timer.
 */

public final class MailDaemon extends TaskData
    {
    // String Resources
    private static final String STATUS_OUTBOX = "Showing the Email Outbox for";

    private static final String KEY_MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String KEY_MAIL_SMTP_AUTH = "mail.smtp.auth";

    private Timer timerMail;
    private SwingWorker workerMail;

    // MailDaemon Properties
    private boolean boolEnableSendmail;
    private boolean boolEnableSSL;
    private boolean boolEnableAuthentication;
    private String strHostName;
    private String strUserName;
    private String strPassword;
    private String strToAddress;
    private String strFromAddress;
    private int intPeriod;


    /***********************************************************************************************
     * Send an Email.
     *
     * @param plugin
     * @param host
     * @param username
     * @param password
     * @param addressTo
     * @param addressFrom
     * @param message
     * @param ssl
     * @param authentication
     * @param debug
     */

    private static void sendMail(final TaskPlugin plugin,
                                 final String host,
                                 final String username,
                                 final String password,
                                 final String addressTo,
                                 final String addressFrom,
                                 final String message,
                                 final boolean ssl,
                                 final boolean authentication,
                                 final boolean debug)
        {
        final SMTPTransport transport;
        final Properties props;
        final Session session;

        // Create some properties and get the default mail Session
        props = new Properties();

        if (host != null)
            {
            props.put(KEY_MAIL_SMTP_HOST, host);
            }

        props.put(KEY_MAIL_SMTP_AUTH, "true");

        session = Session.getInstance(props, null);
        session.setDebug(debug);

        try
            {
            // create a message
            final Message msg = new MimeMessage(session);

            if (addressFrom != null)
                {
                msg.setFrom(new InternetAddress(addressFrom));
                }
            else
                {
                msg.setFrom();
                }

            final InternetAddress[] address =
                {
                new InternetAddress(addressTo)
                };

            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(REGISTRY.getFramework().getName() + SPACE + plugin.getName());
            msg.setSentDate(Chronos.getSystemDateNow());

            // If the desired charset is known, you can use
            // setText(text, charset)
            msg.setText(message);

            /*
             * The simple way to send a message is this:
             *
            Transport.send(msg);
             *
             * But we're going to use some SMTP-specific features for
             * demonstration purposes so we need to manage the Transport
             * object explicitly.
             */
            if (ssl)
                {
                transport = (SMTPTransport)session.getTransport("smtps");
                }
            else
                {
                transport = (SMTPTransport)session.getTransport("smtp");
                }

            if (authentication)
                {
                transport.connect(host, username, password);
                }
            else
                {
                transport.connect();
                }

            transport.sendMessage(msg, msg.getAllRecipients());
            LOGGER.debug("Mail Server Response: " + transport.getLastServerResponse());
            transport.close();
            }

        catch (NoSuchProviderException exception)
            {
            LOGGER.debug("Unable to find Mail transport");
            exception.printStackTrace();
            }

        catch (MessagingException exception)
            {
            LOGGER.debug("Exception handling in MailDaemon");

            exception.printStackTrace();

            Exception ex = exception;
            do
                {
                if (ex instanceof SendFailedException)
                    {
                    final SendFailedException sfex = (SendFailedException) ex;
                    final Address[] invalid = sfex.getInvalidAddresses();
                    if (invalid != null)
                        {
                        LOGGER.debug("    ** Invalid Addresses");
                        for (int i = 0; i < invalid.length; i++)
                            {
                            LOGGER.debug("         " + invalid[i]);
                            }
                        }
                    final Address[] validUnsent = sfex.getValidUnsentAddresses();
                    if (validUnsent != null)
                        {
                        LOGGER.debug("    ** ValidUnsent Addresses");
                        for (int i = 0; i < validUnsent.length; i++)
                            {
                            LOGGER.debug("         " + validUnsent[i]);
                            }
                        }
                    final Address[] validSent = sfex.getValidSentAddresses();
                    if (validSent != null)
                        {
                        LOGGER.debug("    ** ValidSent Addresses");
                        for (int i = 0; i < validSent.length; i++)
                            {
                            LOGGER.debug("         " + validSent[i]);
                            }
                        }
                    }

                if (ex instanceof MessagingException)
                    {
                    ex = ((MessagingException) ex).getNextException();
                    }
                else
                    {
                    ex = null;
                    }
                }
            while (ex != null);
            }
        }


    /**********************************************************************************************
     * Construct a MailDaemon FrameworkTask.
     */

    private MailDaemon()
        {
        super(5992850446318352010L, REGISTRY.getFramework());

        timerMail = null;
        workerMail = null;

        boolEnableSendmail = true;
        boolEnableSSL = false;
        boolEnableAuthentication = false;
        strHostName = "";
        strUserName = "";
        strPassword = "";
        strToAddress = "";
        strFromAddress = "";
        intPeriod = 10000;
        }


    /***********************************************************************************************
     * Initialise the MailDaemon. Task state changes are handled by setInstrumentState() of the parent
     * Framework.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean initialiseTask()
        {
        final TaskPlugin pluginTask;

        pluginTask = this;

        // Get the latest Resources
        readResources();

        // Stop any existing Timer
        if (timerMail != null)
            {
            timerMail.stop();
            }

        // Stop any existing SwingWorker
        SwingWorker.disposeWorker(workerMail, true, SWING_WORKER_STOP_DELAY);
        workerMail = null;

        // Set up a Timer to do the SendMail
        timerMail = new Timer(intPeriod, new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                LOGGER.debugTimerTick("MailDaemon: Timer");

                // Stop any existing SwingWorker
                SwingWorker.disposeWorker(workerMail, true, SWING_WORKER_STOP_DELAY);
                workerMail = null;

                // Prepare another thread to do the SendMail
                workerMail = new SwingWorker(REGISTRY.getThreadGroup(),
                                             "SwingWorker MailDaemon")
                    {
                    public Object construct()
                        {
                        LOGGER.debugSwingWorker("MailDaemon");
                        // Get the latest Resources
                        readResources();

                        final String msgText = Chronos.getSystemDateNow() + " This is a message body.\nHere's the second line.";

                        // Check to see if updates are allowed
                        if ((boolEnableSendmail)
                            && (!isStopping()))
                            {
                            sendMail(pluginTask,
                                     strHostName,
                                     strUserName,
                                     strPassword,
                                     strToAddress,
                                     strFromAddress,
                                     msgText,
                                     boolEnableSSL,
                                     boolEnableAuthentication,
                                     getDebugMode());
                            }

                        // Update the Timer delay in case the RegistryModel has changed...
                        timerMail.setDelay(intPeriod);

                        // There is no result to pass to the Event Dispatching Thread
                        return (null);
                        }

                    // Display updates occur on the Event Dispatching Thread
                    public void finished()
                        {
                        // There is nothing to do on the Event Dispatching Thread
                        }
                    };

                // When the Timer goes off, start the Thread we have prepared...
                workerMail.start();
                }
            });

        return (true);
        }


    /***********************************************************************************************
     * Run the MailDaemon.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean runTask()
        {
        // Get the latest Resources
        readResources();

        if (timerMail != null)
            {
            // Set up the UI of an OutboxReport
//            try
//                {
                // Remove any previous OutboxReport
                if (getUIComponent() != null)
                    {
                    getUIComponent().disposeUI();
                    }

                // Create an OutboxReport for the Framework
                // The ResourceKey is always that of the host Framework,
                // since this is a general utility
                //setUIComponent(new OutboxReport(this, REGISTRY.getFramework().getResourceKey()));
                setUIComponent(new BlankUIComponent("Email Outbox Panel"));
                getUIComponent().initialiseUI();

                // There is no Editor, and we are always in Browse mode
                setEditorComponent(null);
                setBrowseMode(true);
//                }
//
//            catch (ReportException exception)
//                {
//                handleException(exception,
//                                "runTask()",
//                                EventStatus.WARNING);
//                }

            timerMail.setCoalesce(false);
            timerMail.restart();

            return (true);
            }
        else
            {
            return (false);
            }
        }


    /***********************************************************************************************
     * Park the MailDaemon in Idle.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean idleTask()
        {
        // Clear the OutboxReport data, and stop the refresh Timer
        stopUI();

        // Clear the ContextActionGroups for the Task
        clearUserObjectContextActionGroups();

        if (timerMail != null)
            {
            // Make sure that the Timer has stopped running,
            // but leave the reference to it, so that runTask() can use it
            timerMail.stop();
            }

        SwingWorker.disposeWorker(workerMail, true, SWING_WORKER_STOP_DELAY);
        workerMail = null;

        return (true);
        }


    /***********************************************************************************************
     * Shutdown the MailDaemon after use.
     *
     * @return boolean Flag indicating success or failure
     */

    public final boolean shutdownTask()
        {
        stopUI();

        // Remove the OutboxReport
        if (getUIComponent() != null)
            {
            getUIComponent().disposeUI();
            setUIComponent(null);
            }

        // Stop the Timer
        if (timerMail != null)
            {
            // Make sure that the Timer has stopped running
            timerMail.stop();
            timerMail = null;
            }

        SwingWorker.disposeWorker(workerMail, true, SWING_WORKER_STOP_DELAY);
        workerMail = null;

        return (true);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     */

    public final void runUI()
        {
        if (getUIComponent() != null)
            {
            getUIComponent().runUI();
            }

        setCaption(getPathname());
        setStatus(STATUS_OUTBOX + SPACE + REGISTRY.getFramework().getPathname());
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        if (getUIComponent() != null)
            {
            // Reduce resources as far as possible
            getUIComponent().stopUI();
            }
        }


    /**********************************************************************************************
     * Read all the Resources required by the MailDaemon.
     */

    public final void readResources()
        {
        // getResourceKey() returns 'Starbase.MailDaemon.'
        setDebugMode(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));

        boolEnableSendmail = REGISTRY.getBooleanProperty(getResourceKey() + KEY_MAIL_ENABLE_SEND_MAIL);
        boolEnableSSL = REGISTRY.getBooleanProperty(getResourceKey() + KEY_MAIL_ENABLE_SSL);
        boolEnableAuthentication = REGISTRY.getBooleanProperty(getResourceKey() + KEY_MAIL_ENABLE_AUTHENTICATION);
        strHostName = REGISTRY.getStringProperty(getResourceKey() + KEY_MAIL_HOST);
        strUserName = REGISTRY.getStringProperty(getResourceKey() + KEY_MAILUSERNAME);
        strPassword = REGISTRY.getStringProperty(getResourceKey() + KEY_MAIL_PASSWORD);
        strToAddress = REGISTRY.getStringProperty(getResourceKey() + KEY_MAIL_TO_ADDRESS);
        strFromAddress = REGISTRY.getStringProperty(getResourceKey() + KEY_MAIL_FROM_ADDRESS);
        intPeriod = REGISTRY.getIntegerProperty(getResourceKey() + KEY_MAIL_PERIOD);

        // ToDo Resource validation
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
