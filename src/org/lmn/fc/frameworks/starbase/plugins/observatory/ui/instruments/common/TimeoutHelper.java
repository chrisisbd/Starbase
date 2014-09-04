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
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Controller;
import org.lmn.fc.model.xmlbeans.instruments.PluginType;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;

import java.awt.*;


/***************************************************************************************************
 * TimeoutHelper.
 */

public final class TimeoutHelper implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            FrameworkSingletons,
                                            ResourceKeys
    {
    // String Resources
    private static final String KEY_INSTRUMENT = "Instrument";
    private static final String TIMEOUT = "Timeout";

    // Five retries should be enough!
    public static final int RETRY_COUNT = 5;

    // A long timeout to allow e.g. repeated GOES image capture
    public static final long TIMEOUT_MAX_MILLISECONDS = 3600000;


    /***********************************************************************************************
     * Get the custom Command timeout from the CommandMetadata if possible,
     * or use the specified default.
     *
     * @param module
     * @param command
     * @param defaulttimeoutmillis
     *
     * @return int
     */

    public static int getCommandTimeout(final XmlObject module,
                                        final CommandType command,
                                        final int defaulttimeoutmillis)
        {
        int intTimeoutMillis;

        intTimeoutMillis = defaulttimeoutmillis;

        // See if we have a specific timeout for this Command
        if ((module != null)
            && (command != null)
            && (command.getCommandMetadataList() != null)
            && (!command.getCommandMetadataList().isEmpty()))
            {
            final StringBuffer bufferCommandIdentifier;
            final StringBuffer bufferModuleIdentifier;
            final StringBuffer bufferMetadataKey;
            final Metadata metadataTimeout;

            bufferCommandIdentifier = new StringBuffer(command.getIdentifier());
            // The first character of the Identifier must be uppercase for use as a Key
            // There must be an easier way?
            bufferCommandIdentifier.setCharAt(0, command.getIdentifier().toUpperCase().charAt(0));

            // Identify Controller or Plugin from the XmlObject
            bufferModuleIdentifier = new StringBuffer();

            if (module instanceof Controller)
                {
                bufferModuleIdentifier.append(((Controller)module).getIdentifier());
                }
            else if (module instanceof PluginType)
                {
                bufferModuleIdentifier.append(((PluginType)module).getIdentifier());
                }
            else
                {
                // This will fail the lookup below, but should never occur!
                bufferModuleIdentifier.append(QUERY);
                }

            // Look for the Timeout as Instrument.<Module>.<Command>.Timeout
            bufferMetadataKey = new StringBuffer();
            bufferMetadataKey.append(KEY_INSTRUMENT);
            bufferMetadataKey.append(DOT);
            bufferMetadataKey.append(bufferModuleIdentifier);
            bufferMetadataKey.append(DOT);
            bufferMetadataKey.append(bufferCommandIdentifier);
            bufferMetadataKey.append(DOT);
            bufferMetadataKey.append(TIMEOUT);

            metadataTimeout = MetadataHelper.getMetadataByKey(command.getCommandMetadataList(),
                                                              bufferMetadataKey.toString());
            if (metadataTimeout != null)
                {
                try
                    {
                    intTimeoutMillis = Integer.parseInt(metadataTimeout.getValue()) * (int)ChronosHelper.SECOND_MILLISECONDS;
                    LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                              "TimeoutHelper.getCommandTimeout() Activated custom Command Timeout [delay=" + intTimeoutMillis + "msec]");
                    }

                catch (NumberFormatException exception)
                    {
                    LOGGER.error("TimeoutHelper.getCommandTimeout() Invalid timeout metadata [key="
                                 + bufferMetadataKey.toString() + "] [value=" + metadataTimeout.getValue() + "]");
                    }
                }
            }

        return (intTimeoutMillis);
        }


    /***********************************************************************************************
     * Restart the DAO Timeout Timer (not the Rx Queue wait loop),
     * using the specified timeout in milliseconds.
     *
     * @param dao
     * @param timeoutmillis
     */

    public static void restartDAOTimeoutTimer(final ObservatoryInstrumentDAOInterface dao,
                                              final int timeoutmillis)
        {
        final String SOURCE = "TimeoutHelper.restartDAOTimeoutTimer() ";

        // This does not change the value stored in the Registry property KEY_DAO_TIMEOUT_DEFAULT

        if ((dao != null)
            && (dao.getTimeoutTimer() != null))
            {
            // Sets the Timer's initial delay, the time in milliseconds to wait after
            // the timer is started before firing the first event.
            // Upon construction, this is set to be the same as the between-event delay,
            // but then its value is independent and remains unaffected by changes to the between-event delay.
            dao.getTimeoutTimer().setInitialDelay(timeoutmillis);

            // Sets the Timer's between-event delay, the number of milliseconds between
            // successive action events. This does not affect the initial delay property,
            // which can be set by the setInitialDelay method.
            dao.getTimeoutTimer().setDelay(timeoutmillis);

            // Restarts the Timer, cancelling any pending firings and causing it to fire with its initial delay.
            dao.getTimeoutTimer().restart();

            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      SOURCE + "DAO Timeout restarted [delay=" + timeoutmillis + " msec]");
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to restart DAO Timeout because DAO is NULL");
            }
        }


    /***********************************************************************************************
     * Restart the DAO Timeout Timer (not the Rx Queue wait loop),
     * setting it to run forever, i.e. not time out.
     *
     * @param dao
     */

    public static void restartDAOTimeoutTimerInfinite(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "TimeoutHelper.restartDAOTimeoutTimerInfinite() ";

        if ((dao != null)
            && (dao.getTimeoutTimer() != null))
            {
            // Set the timeout to run forever...
            dao.getTimeoutTimer().setInitialDelay(Integer.MAX_VALUE);

            // Sets the Timer's between-event delay,
            // the number of milliseconds between successive action events
            dao.getTimeoutTimer().setDelay(Integer.MAX_VALUE);

            // Restarts the Timer, cancelling any pending firings and causing it to fire with its initial delay
            dao.getTimeoutTimer().restart();

            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      SOURCE + "Timeout Timer restarted [delay=infinite]");
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to restart Timeout because DAO is NULL");
            }
        }


    /***********************************************************************************************
     * Stop the DAO Timeout Timer (not the Rx Queue wait loop).
     *
     * @param dao
     */

    public static void stopDAOTimeoutTimer(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "TimeoutHelper.stopDAOTimeoutTimer() ";

        if ((dao != null)
            && (dao.getTimeoutTimer() != null))
            {
            // Stops the Timer, causing it to stop sending action events
            dao.getTimeoutTimer().stop();

            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      SOURCE + "DAO Timeout stopped");
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to stop DAO Timeout because DAO is NULL");
            }
        }


    /***********************************************************************************************
     * Update the DAO Timeout Timer delay in case the RegistryModel has changed...
     * Stop any running Timer, but do not restart the Timer.
     *
     * @param dao
     */

    public static void resetDAOTimeoutTimerFromRegistryDefault(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "TimeoutHelper.resetDAOTimeoutTimerFromRegistryDefault() ";

        stopDAOTimeoutTimer(dao);

        if (dao != null)
            {
            int intTimeoutMillis;

            // Timeout Period from the Registry default
            intTimeoutMillis = REGISTRY.getIntegerProperty(dao.getResourceKey() + KEY_DAO_TIMEOUT_DEFAULT)
                                                           * (int) ChronosHelper.SECOND_MILLISECONDS;

             // Trap the careless use of the Timeout Period  {1sec ... 30min}
            if ((intTimeoutMillis < ChronosHelper.SECOND_MILLISECONDS)
                || (intTimeoutMillis > (TIMEOUT_MAX_MILLISECONDS)))
                {
                intTimeoutMillis = 10 * (int) ChronosHelper.SECOND_MILLISECONDS;
                LOGGER.error(SOURCE + KEY_DAO_TIMEOUT_DEFAULT + " is set incorrectly. Using a value of 10sec.");
                }

            // Apply the new Timeout
            dao.setTimeoutDefaultMillis(intTimeoutMillis);

            if (dao.getTimeoutTimer() != null)
                {
                dao.getTimeoutTimer().setInitialDelay(dao.getTimeoutDefaultMillis());
                dao.getTimeoutTimer().setDelay(dao.getTimeoutDefaultMillis());
                }

            LOGGER.debugStaribusEvent(LOADER_PROPERTIES.isStaribusDebug(),
                                      SOURCE + "Timeout Timer reset to Registry default [delay=" + intTimeoutMillis + " msec]");
            }
        else
            {
            LOGGER.error(SOURCE + "Unable to reset to default Timeout because DAO is NULL");
            }
        }


    /**********************************************************************************************/
    /* TODO REVIEW ALL OF THE BELOW                                                               */
    /***********************************************************************************************
     * Reset the DAO Timeout Timer.
     * This is called after every Socket timeout in SpectraCyberClientTxStream write().
     *
     * @param dao
     * @param retryid
     * @param retrycount
     * @param sockettimeout
     * @param retrywait
     */

    public static void adjustDaoTimeoutTimerSpectraCyber(final ObservatoryInstrumentDAOInterface dao,
                                                         final int retryid,
                                                         final int retrycount,
                                                         final long sockettimeout,
                                                         final long retrywait)
        {
        final String SOURCE = "SpectraCyberClientTxStream.adjustDaoTimeoutTimerSpectraCyber() ";

        // The timeout sequence could be:
        //
        // ----------------------------------daotimeout--------------------------------------->X
        // ---retry0---
        //             ---retrywait---
        //                            ---retry1---
        //                                        ---retrywait---
        //                                                       ...and.so.on...
        // So at any one time, the DAO timeout is **at least**
        // (the number of retries left to run) * (retry_timeout_time + retry_wait_time)
        // This does not change the value stored in the Registry property KEY_DAO_TIMEOUT_DEFAULT,
        // which is used to drive the timeout of the remote bus 'inner' commands.
        // This time is reloaded into the Timer when the command is finished.

        if (dao.getTimeoutTimer() != null)
            {
            int intDaoTimerDelayMillis;

            intDaoTimerDelayMillis = (retrycount - retryid) * (int)(sockettimeout + retrywait);

            // Don't allow the 'outer' timeout to be any shorter than the original single-command value,
            // since the 'inner' single-command must time out *before* the 'outer' command
//            if (intDaoTimerDelayMillis <= dao.getTimeoutDefaultMillis())
//                {
//                // Make a safety margin of one second per retry
//                intDaoTimerDelayMillis = dao.getTimeoutDefaultMillis() + (int)(ChronosHelper.SECOND_MILLISECONDS * retrycount);
//                }

            //--------------------------------------------------------------------------------------
            // Reset the DAO timer

            // Sets the Timer's initial delay, the time in milliseconds to wait after
            // the timer is started before firing the first event.
            // Upon construction, this is set to be the same as the between-event delay,
            // but then its value is independent and remains unaffected by changes to the between-event delay.
            dao.getTimeoutTimer().setInitialDelay(intDaoTimerDelayMillis);

            // Sets the Timer's between-event delay, the number of milliseconds between
            // successive action events. This does not affect the initial delay property,
            // which can be set by the setInitialDelay method.
            dao.getTimeoutTimer().setDelay(intDaoTimerDelayMillis);

            // Restarts the Timer, cancelling any pending firings and causing it to fire with its initial delay.
            dao.getTimeoutTimer().restart();

//            LOGGER.debugProtocolEvent(SOURCE + "Timeout Timer reset [retryid=" + retryid
//                                            + "] [sockettimeout=" + sockettimeout
//                                            + "msec] [retrywait=" + retrywait
//                                            + "msec] [daotimerdelay=" + intDaoTimerDelayMillis + " msec]");
            }
        else
            {
            // This should never happen!
            LOGGER.error(SOURCE + "DAO is NULL");
            }
        }


    /***********************************************************************************************
     * Reset the DAO Timeout Timer.
     * This is called after every Socket timeout in StarinetUDPTxStream write().
     *
     * @param dao
     * @param retryid
     * @param retrycount
     * @param sockettimeout
     * @param retrywait
     */

    public static void adjustDaoTimeoutTimerStarinet(final ObservatoryInstrumentDAOInterface dao,
                                                     final int retryid,
                                                     final int retrycount,
                                                     final long sockettimeout,
                                                     final long retrywait)
        {
        final String SOURCE = "StarinetUdpTxStream.adjustDaoTimeoutTimerSpectraCyber() ";

        // The timeout sequence could be:
        //
        // ----------------------------------daotimeout--------------------------------------->X
        // ---retry0---
        //             ---retrywait---
        //                            ---retry1---
        //                                        ---retrywait---
        //                                                       ...and.so.on...
        // So at any one time, the DAO timeout is **at least**
        // (the number of retries left to run) * (retry_timeout_time + retry_wait_time)
        // This does not change the value stored in the Registry property KEY_DAO_TIMEOUT_DEFAULT,
        // which is used to drive the timeout of the remote bus 'inner' commands.
        // This time is reloaded into the Timer when the command is finished.

        if (dao.getTimeoutTimer() != null)
            {
            int intDaoTimerDelayMillis;

            intDaoTimerDelayMillis = (retrycount - retryid) * (int)(sockettimeout + retrywait);

            // Don't allow the 'outer' timeout to be any shorter than the original single-command value,
            // since the 'inner' single-command must time out *before* the 'outer' command
//            if (intDaoTimerDelayMillis <= dao.getTimeoutDefaultMillis())
//                {
//                // Make a safety margin of one second per retry
//                intDaoTimerDelayMillis = dao.getTimeoutDefaultMillis() + (int)(ChronosHelper.SECOND_MILLISECONDS * retrycount);
//                }

            //--------------------------------------------------------------------------------------
            // Reset the DAO timer

            // Sets the Timer's initial delay, the time in milliseconds to wait after
            // the timer is started before firing the first event.
            // Upon construction, this is set to be the same as the between-event delay,
            // but then its value is independent and remains unaffected by changes to the between-event delay.
            dao.getTimeoutTimer().setInitialDelay(intDaoTimerDelayMillis);

            // Sets the Timer's between-event delay, the number of milliseconds between
            // successive action events. This does not affect the initial delay property,
            // which can be set by the setInitialDelay method.
            dao.getTimeoutTimer().setDelay(intDaoTimerDelayMillis);

            // Restarts the Timer, cancelling any pending firings and causing it to fire with its initial delay.
            dao.getTimeoutTimer().restart();

//            LOGGER.debugProtocolEvent(SOURCE + "Timeout Timer reset [retryid=" + retryid
//                                            + "] [sockettimeout=" + sockettimeout
//                                            + "msec] [retrywait=" + retrywait
//                                            + "msec] [daotimerdelay=" + intDaoTimerDelayMillis + " msec]");
            }
        else
            {
            // This should never happen!
            LOGGER.error(SOURCE + "DAO is NULL");
            }
        }


    /***********************************************************************************************
     * Write a warning to the EventLog to say that a retry has occurred.
     *
     * @param dao
     * @param starscript
     * @param retryid
     */

    public static void logRetryEvent(final ObservatoryInstrumentDAOInterface dao,
                                     final String starscript,
                                     final int retryid)
        {
        if ((dao != null)
            && (retryid > 0))
            {
            // We need to know that a retry occurred
            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.TIMEOUT,
                                               METADATA_TARGET_COMMAND
                                                    + METADATA_ACTION_EXECUTE
                                                    + METADATA_WARNING + "Unexpected retry invoked" + TERMINATOR_SPACE
                                                    + METADATA_RETRY + retryid + TERMINATOR_SPACE
                                                    + METADATA_STARSCRIPT + starscript + TERMINATOR_SPACE
                                                    + METADATA_STATUS + ResponseMessageStatus.expandResponseStatusCodes(dao.getResponseMessageStatusList()) + TERMINATOR,
                                               dao.getLocalHostname(),
                                               dao.getObservatoryClock());

            Toolkit.getDefaultToolkit().beep();
            }
        }


    /**********************************************************************************************/
    /* BELIEVED TO BE REDUNDANT                                                                   */
    /***********************************************************************************************
     * Reset the DAO Timeout Timer.
     * This is called after every timeout, in StaribusLoggerDAO getData()..
     *
     * @param dao
     * @param blockcount
     * @param blockid
     * @param retryid
     */

//    public static void resetDaoTimeoutTimerForStaribusBlock(final ObservatoryInstrumentDAOInterface dao,
//                                                            final int blockcount,
//                                                            final int blockid,
//                                                            final int retryid)
//        {
//        // We know we are reading StaribusBlocks, at 535 bytes per block.
//        // At the slowest rate of 9600 Baud, i.e. 960 characters per second,
//        // this is 535/960 = 557msec. So the theoretical minimum time to complete
//        // reading all the blocks is (557 * block_count) msec, plus the single getBlockCount() command.
//        // Allowing a factor of two for other delays (!),
//        // use the block_count directly to give the timeout delay in seconds.
//        // At faster Baud rates this will be much too long...
//        // 57k baud is 5700 characters per second, so a StaribusBlock takes about 93msec
//        // Ideally we should read the Baud rate dynamically
//
//        // The worst case time is hard to calculate, since several blocks may have timed out.
//        // so readjust after every timeout, using the number of blocks *left* in seconds,
//        // plus the full (potential) timeout of one of them?!
//
//        // This does not change the value stored in the Registry property KEY_DAO_TIMEOUT_DEFAULT,
//        // which is used to drive the timeout of the remote bus 'inner' commands.
//
//        if ((dao != null)
//            && (dao.getTimeoutTimer() != null))
//            {
//            final int intBlocksRemaining;
//            int intDelay;
//
//            intBlocksRemaining = (int)(((blockcount - blockid) + (RETRY_COUNT - retryid)) * ChronosHelper.SECOND_MILLISECONDS);
//
//            intDelay = Math.max(intBlocksRemaining, dao.getTimeoutDefaultMillis());
//
//            // Don't allow the 'outer' timeout to be any shorter than the original single-command value,
//            // since the 'inner' single-command must time out *before* the 'outer' command
//            if (intDelay <= dao.getTimeoutDefaultMillis())
//                {
//                // Just double it for a safety margin
//                intDelay = dao.getTimeoutDefaultMillis() << 1;
//                }
//
//            // Sets the Timer's initial delay, the time in milliseconds to wait after
//            // the timer is started before firing the first event.
//            // Upon construction, this is set to be the same as the between-event delay,
//            // but then its value is independent and remains unaffected by changes to the between-event delay.
//            dao.getTimeoutTimer().setInitialDelay(intDelay);
//
//            // Sets the Timer's between-event delay, the number of milliseconds between
//            // successive action events. This does not affect the initial delay property,
//            // which can be set by the setInitialDelay method.
//            dao.getTimeoutTimer().setDelay(intDelay);
//
//            // Restarts the Timer, cancelling any pending firings and causing it to fire with its initial delay.
//            dao.getTimeoutTimer().restart();
//
//            //LOGGER.debugProtocolEvent("StaribusLoggerDAO.getData() LOCAL Timeout Timer reset [delay=" + intDelay + " msec] [blocks_remaining=" + intBlocksRemaining + "]");
//            }
//        }


    /***********************************************************************************************
     * Reset the DAO Timeout Timer (not the Rx Queue wait loop).
     * This is called after every timeout.
     *
     * @param dao
     * @param commandcount
     * @param commandid
     * @param retryid
     */

//    public static void restartDAOTimeoutTimer(final ObservatoryInstrumentDAOInterface dao,
//                                              final int commandcount,
//                                              final int commandid,
//                                              final int retryid)
//        {
//        System.out.println("TimeoutHelper.restartDAOTimeoutTimer()");
//
//        // This does not change the value stored in the Registry property KEY_DAO_TIMEOUT_DEFAULT,
//        // which is used to drive the timeout of the remote bus 'inner' commands.
//
//        if ((dao != null)
//            && (dao.getTimeoutTimer() != null))
//            {
//            final int intMsecRemaining;
//            int intDelay;
//
//            // Assume that each command takes a maximum of one second (way over the top...)
//            intMsecRemaining = (int)(((commandcount - commandid) + (RETRY_COUNT - retryid)) * ChronosHelper.SECOND_MILLISECONDS);
//
//            intDelay = Math.max(intMsecRemaining, dao.getTimeoutDefaultMillis());
//
//            // Don't allow the 'outer' timeout to be any shorter than the original single-command value,
//            // since the 'inner' single-command must time out *before* the 'outer' command
//            if (intDelay <= dao.getTimeoutDefaultMillis())
//                {
//                // Just double it for a safety margin
//                intDelay = dao.getTimeoutDefaultMillis() << 1;
//                }
//
//            // Sets the Timer's initial delay, the time in milliseconds to wait after
//            // the timer is started before firing the first event.
//            // Upon construction, this is set to be the same as the between-event delay,
//            // but then its value is independent and remains unaffected by changes to the between-event delay.
//            dao.getTimeoutTimer().setInitialDelay(intDelay);
//
//            // Sets the Timer's between-event delay, the number of milliseconds between
//            // successive action events. This does not affect the initial delay property,
//            // which can be set by the setInitialDelay method.
//            dao.getTimeoutTimer().setDelay(intDelay);
//
//            // Restarts the Timer, cancelling any pending firings and causing it to fire with its initial delay.
//            dao.getTimeoutTimer().restart();
//
//            //LOGGER.debugProtocolEvent("DAOCommandHelper.restartDAOTimeoutTimer() Timeout Timer (Local) reset [delay=" + intDelay + " msec] [msec_remaining=" + intMsecRemaining + "]");
//            }
//        }
    }
