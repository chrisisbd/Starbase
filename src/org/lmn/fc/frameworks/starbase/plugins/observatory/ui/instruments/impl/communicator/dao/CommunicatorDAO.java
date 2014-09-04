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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.dao;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.AbstractObservatoryInstrumentDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.ExportJenkinsBuilds;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.ExportMantisIssues;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.ExportNews;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.exporters.ExportSubversionLog;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.news.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.communicator.CommunicatorDAOInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.subscriptions.SubscriptionsDocument;

import java.io.*;


/***************************************************************************************************
 * CommunicatorDAO.
 */

public final class CommunicatorDAO extends AbstractObservatoryInstrumentDAO
                                   implements ObservatoryInstrumentDAOInterface,
                                              CommunicatorDAOInterface
    {
    // Start with 30 mins update period
    private static final int INITIAL_UPDATE_PERIOD_MILLIS = 1800000;

    private int intUpdatePeriodMillis;

    private SubscriptionsDocument docSubscriptions;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        pool.add("subscribeToRSS");
        pool.add("unsubscribeFromRSS");
        pool.add("getTwitterStatus");
        pool.add("setTwitterStatus");
        pool.add("followTwitter");
        pool.add("removeTwitter");
        pool.add("loadSubscriptions");
        pool.add("saveSubscriptions");
        pool.add("setUpdatePeriod");
        pool.add("runNewsreader");
        pool.add("exportNews");
        pool.add("exportMantisIssues");
        pool.add("exportSubversionLog");
        pool.add("exportJenkinsBuilds");
        }


    /***********************************************************************************************
     * Construct a CommunicatorDAO.
     *
     * @param hostinstrument
     */

    public CommunicatorDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        this.intUpdatePeriodMillis = INITIAL_UPDATE_PERIOD_MILLIS;

        this.docSubscriptions = null;

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "CommunicatorDAO.initialiseDAO() [resourcekey=" + resourcekey + "]");

        setSubscriptionsDoc(loadSubscriptions());

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "CommunicatorDAO.disposeDAO()");

        setSubscriptionsDoc(null);

        super.disposeDAO();
        }


    /***********************************************************************************************
     * Construct a CommandMessage appropriate to this DAO.
     *
     * @param dao
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     *
     * @return CommandMessageInterface
     */

    public CommandMessageInterface constructCommandMessage(final ObservatoryInstrumentDAOInterface dao,
                                                           final Instrument instrumentxml,
                                                           final XmlObject module,
                                                           final CommandType command,
                                                           final String starscript)
        {
        return (new CommunicatorCommandMessage(dao,
                                               instrumentxml,
                                               module,
                                               command,
                                               starscript.trim()));
        }


    /***********************************************************************************************
     * Construct a ResponseMessage appropriate to this DAO.
     *
     *
     * @param portname
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param responsestatusbits
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface constructResponseMessage(final String portname,
                                                             final Instrument instrumentxml,
                                                             final XmlObject module,
                                                             final CommandType command,
                                                             final String starscript,
                                                             final int responsestatusbits)
        {
        return (new CommunicatorResponseMessage(portname,
                                                instrumentxml,
                                                module,
                                                command,
                                                starscript.trim(),
                                                responsestatusbits));
        }


    /**********************************************************************************************/
    /* Newsreader                                                                                 */
    /***********************************************************************************************
     * subscribeToRSS().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface subscribeToRSS(final CommandMessageInterface commandmessage)
        {
        return (SubscribeToRSS.doSubscribeToRSS(this, commandmessage));
        }


    /***********************************************************************************************
     * unsubscribeFromRSS().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface unsubscribeFromRSS(final CommandMessageInterface commandmessage)
        {
        return (UnsubscribeFromRSS.doUnsubscribeFromRSS(this, commandmessage));
        }


    /***********************************************************************************************
     * getTwitterStatus().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface getTwitterStatus(final CommandMessageInterface commandmessage)
        {
        return (Twitter.doGetTwitterStatus(this, commandmessage));
        }


    /***********************************************************************************************
     * setTwitterStatus().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setTwitterStatus(final CommandMessageInterface commandmessage)
        {
        return (Twitter.doSetTwitterStatus(this, commandmessage));
        }


    /***********************************************************************************************
     * followTwitter().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface followTwitter(final CommandMessageInterface commandmessage)
        {
        return (FollowTwitter.doFollowTwitter(this, commandmessage));
        }


    /***********************************************************************************************
     * removeTwitter().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface removeTwitter(final CommandMessageInterface commandmessage)
        {
        return (RemoveTwitter.doRemoveTwitter(this, commandmessage));
        }


    /***********************************************************************************************
     * loadSubscriptions().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface loadSubscriptions(final CommandMessageInterface commandmessage)
        {
        return (LoadSubscriptions.doLoadSubscriptions(this, commandmessage));
        }


    /***********************************************************************************************
     * saveSubscriptions().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface saveSubscriptions(final CommandMessageInterface commandmessage)
        {
        return (SaveSubscriptions.doSaveSubscriptions(this, commandmessage));
        }


    /***********************************************************************************************
     * setUpdatePeriod().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setUpdatePeriod(final CommandMessageInterface commandmessage)
        {
        final ResponseMessageInterface response;

        response = SetUpdatePeriod.doSetUpdatePeriod(this, commandmessage);
        setUpdatePeriodMillis(SetUpdatePeriod.getUpdatePeriodMillis());

        return (response);
        }


    /***********************************************************************************************
     * runNewsreader().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface runNewsreader(final CommandMessageInterface commandmessage)
        {
        return (RunNewsreader.doRunNewsreader(this,
                                              commandmessage,
                                              getUpdatePeriodMillis()));
        }


    /***********************************************************************************************
     * exportNews().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportNews(final CommandMessageInterface commandmessage)
        {
        return (ExportNews.doExportNews(this, commandmessage));
        }


    /***********************************************************************************************
     * Get the Subscriptions XML document.
     *
     * @return SubscriptionsDocument
     */

    public SubscriptionsDocument getSubscriptionsDoc()
        {
        return (this.docSubscriptions);
        }


    /***********************************************************************************************
     * Set the Subscriptions XML document.
     *
     * @return SubscriptionsDocument
     */

    public void setSubscriptionsDoc(final SubscriptionsDocument doc)
        {
        this.docSubscriptions = doc;
        }


    /***********************************************************************************************
     * Load the Subscriptions.
     * Return NULL on failure.
     *
     * @return SubscriptionsDocument
     */

    public SubscriptionsDocument loadSubscriptions()
        {
        SubscriptionsDocument subscriptions;

        subscriptions = null;

        try
            {
            final String strFolderSubscriptions;
            final File fileSubscriptions;

            strFolderSubscriptions = InstallationFolder.getTerminatedUserDir()
                                       + PATHNAME_PLUGINS_OBSERVATORY
                                       + DataStore.CONFIG.getLoadFolder()
                                       + System.getProperty("file.separator")
                                       + SUBSCRIPTIONS_FILENAME;
            fileSubscriptions = new File(strFolderSubscriptions);

            subscriptions = SubscriptionsDocument.Factory.parse(fileSubscriptions);

            if (XmlBeansUtilities.isValidXml(subscriptions))
                {
                if (subscriptions.getSubscriptions() == null)
                    {
                    subscriptions = null;
                    }
                }
            else
                {
                throw new XmlException(EXCEPTION_XML_VALIDATION);
                }
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error("Failed to read RSS Subscriptions" + SPACE + exception.getMessage());
            }

        catch (IOException exception)
            {
            LOGGER.error("Failed to read News Subscriptions" + SPACE + exception.getMessage());
            }

        catch (XmlException exception)
            {
            LOGGER.error("Failed to read News Subscriptions" + SPACE + exception.getMessage());
            }

        // This may return a NULL if the above failed
        return(subscriptions);
        }


    /***********************************************************************************************
     * Save the Subscriptions.
     * Return TRUE if the save was successful.
     *
     * @return boolean
     */

    public boolean saveSubscriptions()
        {
        final String SOURCE = "CommunicatorDAO.saveSubscriptions() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((getSubscriptionsDoc() != null)
            && (getSubscriptionsDoc().getSubscriptions() != null))
            {
            try
                {
                final String strFolderSubscriptions;
                final File file;
                final OutputStream outputStream;

                strFolderSubscriptions = InstallationFolder.getTerminatedUserDir()
                                           + PATHNAME_PLUGINS_OBSERVATORY
                                           + DataStore.CONFIG.getLoadFolder()
                                           + System.getProperty("file.separator")
                                           + SUBSCRIPTIONS_FILENAME;

                file = new File(strFolderSubscriptions);
                FileUtilities.overwriteFile(file);
                outputStream = new FileOutputStream(file);

                // Write the whole document (even if empty) to the output stream
                getSubscriptionsDoc().save(outputStream, ObservatoryUIHelper.getXmlOptions(false));

                outputStream.flush();
                outputStream.close();
                boolSuccess = true;
                }

            catch (FileNotFoundException exception)
                {
                LOGGER.error(SOURCE + "Unable to save " + SUBSCRIPTIONS_FILENAME);
                }

            catch (IOException exception)
                {
                LOGGER.error(SOURCE + "Unable to save " + SUBSCRIPTIONS_FILENAME);
                }
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportMantisIssues().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportMantisIssues(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "CommunicatorDAO.exportMantisIssues()";

        return (ExportMantisIssues.doExportMantisIssues(this, commandmessage));
        }


    /***********************************************************************************************
     * exportSubversionLog().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportSubversionLog(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "CommunicatorDAO.exportSubversionLog()";

        return (ExportSubversionLog.doExportSubversionLog(this, commandmessage));
        }


    /***********************************************************************************************
     * exportJenkinsBuilds().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface exportJenkinsBuilds(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "CommunicatorDAO.exportJenkinsBuilds()";

        return (ExportJenkinsBuilds.doExportJenkinsBuilds(this, commandmessage));
        }


    /***********************************************************************************************
     * Get the period to wait before updating the Communicator News.
     *
     * @return int
     */

    private int getUpdatePeriodMillis()
        {
        return (this.intUpdatePeriodMillis);
        }


    /***********************************************************************************************
     * Set the period in milliseconds to wait before updating the Communicator News.
     *
     * @param period
     */

    private void setUpdatePeriodMillis(final int period)
        {
        this.intUpdatePeriodMillis = period;
        }


    /***********************************************************************************************
     * Read all the Resources required by the CommunicatorDAO.
     *
     * KEY_DAO_TIMEOUT_DEFAULT
     */

    public void readResources()
        {
        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "CommunicatorDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
