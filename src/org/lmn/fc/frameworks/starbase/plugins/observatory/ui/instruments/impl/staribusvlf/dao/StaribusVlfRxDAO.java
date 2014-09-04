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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.staribusvlf.dao;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.audio.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.publishers.PublishRealtime;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.CaptureCommandHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.StaribusHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.OscillatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusCoreDAO;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;


/***************************************************************************************************
 * StaribusVlfRxDAO.
 */

public final class StaribusVlfRxDAO extends StaribusCoreDAO
                                    implements ObservatoryInstrumentDAOInterface
    {
    private OscillatorInterface oscillator;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubclassToCommandPool(final CommandPoolList pool)
        {
        // Signal Processor
        pool.add("runOscillator");
        pool.add("measureFrequencyResponse");
        pool.add("configureOscillator");
        pool.add("setWaveformType");
        pool.add("setWaveformSampleRate");
        pool.add("setOscillatorFrequency");
        pool.add("setOscillatorAmplitude");
        pool.add("playAudioFile");
        pool.add("importAudioFile");
        }


    /***********************************************************************************************
     * Construct a StaribusVlfRxDAO.
     *
     * @param hostinstrument
     */

    public StaribusVlfRxDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        this.oscillator = new AudioOscillator();

        addSubclassToCommandPool(getCommandPool());
        }


    /***********************************************************************************************
     * Initialise the DAO.
     *
     * @param resourcekey
     */

    public boolean initialiseDAO(final String resourcekey)
        {
        final String SOURCE = "StaribusVlfRxDAO.initialiseDAO() ";

        LOGGER.logTimedEvent(SOURCE + "[resourcekey=" + resourcekey + "]");

        super.initialiseDAO(resourcekey);

        DAOHelper.loadSubClassResourceBundle(this);

        return (true);
        }


    /***********************************************************************************************
     * Shut down the DAO and dispose of all Resources.
     */

    public void disposeDAO()
        {
        final String SOURCE = "StaribusVlfRxDAO.disposeDAO() ";

        if (getOscillator() != null)
            {
            getOscillator().dispose();
            this.oscillator = null;
            }

        super.disposeDAO();
        }


    /**********************************************************************************************/
    /* DAO Local Commands                                                                         */
    /***********************************************************************************************
     * captureRawDataRealtime().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface captureRawDataRealtime(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.captureRawDataRealtime()";

        // Only generate a ResponseMessage when completed
        return (CaptureCommandHelper.doIteratedStaribusMultichannelDataCaptureCommand(this,
                                                                                      commandmessage,
                                                                                      StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Logger",
                                                                                                                                           "Time (UT)",
                                                                                                                                           "VLF Receiver Output"),
                                                                                      SOURCE,
                                                                                      false));
        }


    /***********************************************************************************************
     * publishChartRealtime().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface publishChartRealtime(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.publishChartRealtime() ";

        return (PublishRealtime.doPublishChartRealtime(this,
                                                       commandmessage,
                                                       StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Publisher",
                                                                                                            "Time (UT)",
                                                                                                            "VLF Receiver Output"),
                                                       SOURCE,
                                                       false));
        }


    /***********************************************************************************************
     * publishChartRealtimeDay().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface publishChartRealtimeDay(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.publishChartRealtimeDay() ";

        return (PublishRealtime.doPublishChartRealtimeDay(this,
                                                          commandmessage,
                                                          StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Publisher",
                                                                                                               "Time (UT)",
                                                                                                               "VLF Receiver Output"),
                                                          SOURCE,
                                                          false));
        }


    /***********************************************************************************************
     * configureOscillator().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface configureOscillator(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.setWaveformType()";

        return (ConfigureOscillator.doConfigureOscillator(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * runOscillator().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface runOscillator(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.runOscillator()";

        return (RunOscillator.doRunOscillator(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * measureFrequencyResponse().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface measureFrequencyResponse(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.measureFrequencyResponse()";

        return (MeasureFrequencyResponse.doMeasureFrequencyResponse(this,
                                                                    commandmessage,
                                                                    getOscillator(),
                                                                    StaribusHelper.createMultichannelChartLegendMetadata("Staribus VLF Receiver Frequency Response",
                                                                                                                         "Frequency Hz",
                                                                                                                         "VLF Receiver Output")));
        }


    /***********************************************************************************************
     * setWaveformType().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setWaveformType(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.setWaveformType()";

        return (SetOscillatorWaveformType.doSetWaveformType(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * setWaveformSampleRate().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setWaveformSampleRate(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.setWaveformSampleRate()";

        return (SetOscillatorWaveformSampleRate.doSetWaveformSampleRate(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * setOscillatorFrequency().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setOscillatorFrequency(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.setOscillatorFrequency()";

        return (SetOscillatorFrequency.doSetOscillatorFrequency(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * setOscillatorAmplitude().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface setOscillatorAmplitude(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.setOscillatorAmplitude()";

        return (SetOscillatorAmplitude.doSetOscillatorAmplitude(this, getOscillator(), commandmessage));
        }


    /***********************************************************************************************
     * playAudioFile().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface playAudioFile(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.playAudioFile()";

        return (PlayAudioFile.doPlayAudioFile(this, commandmessage));
        }


    /***********************************************************************************************
     * importAudioFile().
     *
     * @param commandmessage
     *
     * @return ResponseMessageInterface
     */

    public ResponseMessageInterface importAudioFile(final CommandMessageInterface commandmessage)
        {
        final String SOURCE = "StaribusVlfRxDAO.importAudioFile() ";

        return (ImportAudioFile.doImportAudioFile(this, commandmessage));
        }


    /***********************************************************************************************
     * Get the Audio Oscillator.
     *
     * @return OscillatorInterface
     */

    private OscillatorInterface getOscillator()
        {
        return (this.oscillator);
        }


    /***********************************************************************************************
     *  Read all the Resources required by the DAO.
     *
     * KEY_DAO_ONERROR_CONTINUE
     * KEY_DAO_TIMEOUT_DEFAULT
     */

    public void readResources()
        {
        final String SOURCE = "StaribusVlfRxDAO.readResources() ";

        //LOGGER.debugTimedEvent("StaribusVlfRxDAO.readResources() [ResourceKey=" + getResourceKey() + "]");

        super.readResources();
        }
    }
