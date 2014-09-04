// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009,
//           2010, 2011, 2012, 2013, 2014
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.dao;

import org.lmn.fc.common.datafilters.DataFilterHelper;
import org.lmn.fc.common.datatranslators.hex.HexFileHelper;
import org.lmn.fc.common.utilities.misc.Semaphore;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.threads.SwingWorker;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.MetadataChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.CommandPoolList;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.DAOWrapper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.MetadataHelper;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.BasicInstrumentChildDAO;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.AveragingFFTUIComponentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.*;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.xmlbeans.instruments.CommandCategory;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * AveragingFFTDAO.
 */

public class AveragingFFTDAO extends BasicInstrumentChildDAO
                             implements AveragingFFTDAOInterface
    {
    // Averager Thread
    private final Semaphore semaphore;
    private SwingWorker workerAverager;


    /***********************************************************************************************
     * Build the CommandPool using method names in this DAO.
     *
     * @param pool
     */

    private static void addSubSubclassToCommandPool(final CommandPoolList pool)
        {
        }


    /***********************************************************************************************
     * Read one input block from the specified filename into the block buffer.
     *
     * @param file
     * @param blockindex
     * @param blockbuffer
     * @param buffersize
     *
     * @throws IOException
     */

    private static void readIQBlockFromFile(final RandomAccessFile file,
                                            final long blockindex,
                                            final int[] blockbuffer,
                                            final long buffersize) throws IOException
        {
        // Seek to the start of the requested block
        file.seek(blockindex * buffersize);

        for (int intBufferIndex = 0;
             intBufferIndex < buffersize;
             intBufferIndex++)
            {
            //final byte dataByte;

            // Reads a signed eight-bit value from the file
            blockbuffer[intBufferIndex] = file.readByte();

//            dataByte = file.readByte();
//
//            if (dataByte < 0)
//                {
//                blockbuffer[intBufferIndex] = dataByte + 128;
//                }
//            else
//                {
//                blockbuffer[intBufferIndex] = dataByte - 128;
//                }
            }
        }


    /***********************************************************************************************
     * AveragingFFTDAO.
     *
     * @param hostinstrument
     */

    public AveragingFFTDAO(final ObservatoryInstrumentInterface hostinstrument)
        {
        super(hostinstrument);

        addSubSubclassToCommandPool(getCommandPool());

        this.semaphore = new Semaphore(false);
        this.workerAverager = null;

        // Set up RawData only when the FFT length is known
        setRawData(null);
        setRawDataChannelCount(1);
        }


    /***********************************************************************************************
     * Process the data from the file named in Metadata File.Name.
     * Update progress on the specified JComponent.
     * The host Instrument DOES NOT require the data produced by this DAO.
     *
     * @param hostframeui
     * @param monitor
     * @param startrun
     */

    public void processData(final AveragingFFTFrameUIComponentInterface hostframeui,
                            final JComponent monitor,
                            final boolean startrun)
        {
        if ((!isRunning())
            && (startrun))
            {
            // We've been asked to Run a stopped Averager
            initialiseAverager(hostframeui, this, monitor);
            getAveragerWorker().start();
            setRunning(true);
            }
        else if ((!isRunning())
             && (!startrun))
            {
            // We've been asked to Stop a stopped Averager, which must fail, with no action taken
            setRunning(false);
            disposeAverager();
            }
        else if ((isRunning())
             && (startrun))
            {
            // We've been asked to Run a running Averager, which must fail, with no action taken
            }
        else if ((isRunning())
             && (!startrun))
            {
            // We've been asked to Stop a running Averager
            setRunning(false);
            disposeAverager();
            }
        else
            {
            // We must try to stop the Averager
            setRunning(false);
            disposeAverager();
            }
        }


    /***********************************************************************************************
     * Initialise the Averager, to provide data for the specified DAO.
     *
     * @param hostframeui
     * @param dao
     * @param monitor
     */

    private void initialiseAverager(final AveragingFFTFrameUIComponentInterface hostframeui,
                                    final ObservatoryInstrumentDAOInterface dao,
                                    final JComponent monitor)
        {
        final String SOURCE = "AveragingFFTDAO.initialiseAverager() ";

        setRunning(false);

        setAveragerWorker(new SwingWorker(REGISTRY.getThreadGroup(), SOURCE)
            {
            /*******************************************************************************
             * Run the Averager
             *
             * @return Object
             */

            public Object construct()
                {
                // The DAO output data
                double[] arrayMagnitudes;

                arrayMagnitudes = new double[1];

                // Update all Resources
                readResources();

                // If we get here without Exception, we should be running...
                setRunning(true);

                try
                    {
                    Metadata metadata;
                    final List<Metadata> listMetadata;

                    // Input Parameters
                    final String strFilename;
                    final int intFFTLength;
                    final WindowingFunction windowingFunction;
                    final FFTDisplayMode displayMode;
                    final boolean boolIQ;
                    final String strPlotMode;
                    float floatAmplitudeCorrection;
                    float floatPhaseCorrection;

                    // Calculated
                    final int intSampleRate;

                    //-----------------------------------------------------------------------------
                    // Set up the algorithm parameters from the UI sidebar Metadata

                    // Input Parameters
                    // KEY_FILE_NAME
                    // KEY_FFT_LENGTH
                    // KEY_WINDOW
                    // KEY_MODE_DISPLAY
                    // KEY_MODE_IQ
                    // KEY_MODE_PLOT
                    // KEY_OFFSET_AMPLITUDE
                    // KEY_OFFSET_PHASE

                    // Calculated
                    // KEY_FILE_BLOCKS
                    // KEY_FFT_COUNT
                    // KEY_SAMPLE_RATE
                    // KEY_TEMPERATURE_FACTOR
                    // KEY_TIME_CONSTANT

                    listMetadata = hostframeui.getMetadataList();

                    //-----------------------------------------------------------------------------
                    // Input Parameters

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_FILE_NAME);
                    if (metadata != null)
                        {
                        strFilename = metadata.getValue();
                        }
                    else
                        {
                        strFilename = "iq.dat";
                        }

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_FFT_LENGTH);
                    if ((metadata != null)
                        && (FFTLength.getFFTLengthForName(metadata.getValue()) != null))
                        {
                        intFFTLength = FFTLength.getFFTLengthForName(metadata.getValue()).getLength();
                        }
                    else
                        {
                        intFFTLength = FFTLength.FFT_256.getLength();
                        }

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_WINDOW);
                    if ((metadata != null)
                        && (WindowingFunction.getWindowingFunctionForName(metadata.getValue()) != null))
                        {
                        windowingFunction = WindowingFunction.getWindowingFunctionForName(metadata.getValue());
                        }
                    else
                        {
                        windowingFunction = WindowingFunction.WINDOW_NONE;
                        }

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_MODE_DISPLAY);
                    if ((metadata != null)
                        && (FFTDisplayMode.getDisplayModeForName(metadata.getValue()) != null))
                        {
                        displayMode = FFTDisplayMode.getDisplayModeForName(metadata.getValue());
                        }
                    else
                        {
                        displayMode = FFTDisplayMode.SINGLE_SPECTRUM;
                        }

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_MODE_IQ);
                    boolIQ = (metadata != null)
                             && ("I".equals(metadata.getValue()));

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_MODE_PLOT);
                    if ((metadata != null)
                        && (("Lin".equals(metadata.getValue())) || ("Log".equals(metadata.getValue()))))
                        {
                        strPlotMode = metadata.getValue();
                        }
                    else
                        {
                        strPlotMode = "Lin";
                        }

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_OFFSET_AMPLITUDE);
                    if (metadata != null)
                        {
                        floatAmplitudeCorrection = Float.parseFloat(metadata.getValue());

                        if (floatAmplitudeCorrection < AveragingFFTUIHelper.AMPLITUDE_MIN)
                            {
                            floatAmplitudeCorrection = (float)AveragingFFTUIHelper.AMPLITUDE_MIN;
                            }
                        else if (floatAmplitudeCorrection > AveragingFFTUIHelper.AMPLITUDE_MAX)
                            {
                            floatAmplitudeCorrection = (float)AveragingFFTUIHelper.AMPLITUDE_MAX;
                            }

                        metadata.setValue(AveragingFFTUIHelper.PATTERN_AMPLITUDE.format(floatAmplitudeCorrection));
                        }
                    else
                        {
                        floatAmplitudeCorrection = 1.0f;
                        }

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_OFFSET_PHASE);
                    if (metadata != null)
                        {
                        floatPhaseCorrection = Float.parseFloat(metadata.getValue());

                        if (floatPhaseCorrection < AveragingFFTUIHelper.PHASE_MIN)
                            {
                            floatPhaseCorrection = (float)AveragingFFTUIHelper.PHASE_MIN;
                            }
                        else if (floatPhaseCorrection > AveragingFFTUIHelper.PHASE_MAX)
                            {
                            floatPhaseCorrection = (float)AveragingFFTUIHelper.PHASE_MAX;
                            }

                        metadata.setValue(AveragingFFTUIHelper.PATTERN_PHASE.format(floatPhaseCorrection));
                        }
                    else
                        {
                        floatPhaseCorrection = 0.0f;
                        }

                    //-----------------------------------------------------------------------------
                    // Calculated

                    metadata = MetadataHelper.getMetadataByKey(listMetadata,
                                                               AveragingFFTUIComponentInterface.KEY_SAMPLE_RATE);
                    if (metadata != null)
                        {
                        intSampleRate = Integer.parseInt(metadata.getValue());
                        }
                    else
                        {
                        intSampleRate = 2048;
                        }

                    //-----------------------------------------------------------------------------
                    final RandomAccessFile randomAccessFile;
                    final long longBlockCount;
                    int[] arrayData;
                    double[] arrayReal;
                    double[] arrayImaginary;

                    randomAccessFile = new RandomAccessFile(new File(strFilename), "r");

                    // The factor of 2 is because the IQ data are in pairs
                    // ToDo REVIEW how to support integers in the data file
                    longBlockCount = randomAccessFile.length() / (intFFTLength << 1);

                    // Allocate the workspace arrays just once
                    arrayData = new int[(intFFTLength << 1)];  // Space for I and Q pairs
                    arrayReal = new double[intFFTLength];
                    arrayImaginary = new double[intFFTLength];
                    arrayMagnitudes = new double[intFFTLength];

                    // Establish the identity of this Instrument using Metadata
                    // from the Framework, Observatory and Observer
                    dao.establishDAOIdentityForCapture(CommandCategory.CAPTURE,
                                                       DAO_CHANNEL_COUNT,
                                                       false,
                                                       null,
                                                       listMetadata);

                    // Configure the progress monitor
                    if (monitor instanceof JProgressBar)
                        {
                        final JProgressBar progressBar;

                        progressBar = (JProgressBar) monitor;
                        progressBar.setStringPainted(true);
                        }

                    //-----------------------------------------------------------------------------
                    // Get all sample blocks and average the FFT output magnitudes
                    // Allow this loop to be interrupted
                    // Broadly similar to CaptureCommandHelper.doSteppedDataCaptureCommand()
                    // but does not use a ResponseMessage to return results

                    for (long longBlockIndex = 0;
                         ((isRunning())
                          && (Utilities.workerCanProceed(dao, getAveragerWorker()))
                          && (longBlockIndex < longBlockCount));
                         longBlockIndex++)
                        {
                        // Show some progress on each block
                        if (monitor instanceof JProgressBar)
                            {
                            final JProgressBar progressBar;

                            progressBar = (JProgressBar) monitor;
                            progressBar.setValue((int) ((double) (longBlockIndex + 1) * 100.0 / (double) longBlockCount));
                            progressBar.setString(Long.toString(longBlockIndex));
                            }

                        // Get the next block into the data array, ready for the FFT
                        readIQBlockFromFile(randomAccessFile,
                                            longBlockIndex,
                                            arrayData,
                                            intFFTLength << 1);

                        // Process each IQ pair in the block to a single data point,
                        // applying any amplitude or phase adjustments
                        for (int intDataIndex = 0;
                             intDataIndex < intFFTLength;
                             intDataIndex++)
                            {
                            if (boolIQ)
                                {
                                arrayReal[intDataIndex] = ((float) (arrayData[2 * intDataIndex]) / 128.0);
                                                            //- (1 * mnr);

                                arrayImaginary[intDataIndex] = ((((arrayData[2 * intDataIndex + 1])  * floatAmplitudeCorrection) / 128.0)
                                                                + (arrayReal[intDataIndex] * floatPhaseCorrection));
                                                                //- (1 * mni);
                                }
                            else
                                {
                                arrayImaginary[intDataIndex] = ((arrayData[2 * intDataIndex]) / 128.0);
                                                                //- mni;

                                arrayReal[intDataIndex] = ((((arrayData[2 * intDataIndex + 1]) * floatAmplitudeCorrection) / 128.0)
                                                            + (arrayImaginary[intDataIndex] * floatPhaseCorrection));
                                                            //- (1 * mnr);
                                }
                            }

                        // Possibly do a windowing function
                        if ((windowingFunction != null)
                            && (!WindowingFunction.WINDOW_NONE.equals(windowingFunction)))
                            {
                            for (int intIndex = 0;
                                 intIndex < intFFTLength;
                                 intIndex++)
                                {
                                final double dblWindow;

                                dblWindow = windowingFunction.getWindowingFunction().window(intIndex, intFFTLength);

                                arrayReal[intIndex] = arrayReal[intIndex] * dblWindow;
                                arrayImaginary[intIndex] = arrayImaginary[intIndex] * dblWindow;
                                }
                            }

                        //-------------------------------------------------------------------------
                        // Now do the transform for the current block, and tell the world

                        MathsHelper.fft(arrayReal, arrayImaginary, intFFTLength, -1);
                        dao.setUnsavedData(true);

                        //-------------------------------------------------------------------------
                        // Accumulate the Magnitude outputs mag = real^2 + imag^2
                        // Running averager iterative algorithm
                        // av[n] = av[n-1] + (av[n]-av[n-1]) / n = Sum(av[n])/n

                        for (int intIndex = 0;
                             intIndex < intFFTLength;
                             intIndex++)
                            {
                            final double dblMagnitude;

                            dblMagnitude = (arrayReal[intIndex] * arrayReal[intIndex])
                                           + (arrayImaginary[intIndex] * arrayImaginary[intIndex]);

                            // ToDo Averaging...
                            arrayMagnitudes[intIndex] += dblMagnitude;
                            }
                        }

                    if (monitor instanceof JProgressBar)
                        {
                        final JProgressBar progressBar;

                        progressBar = (JProgressBar) monitor;
                        progressBar.setStringPainted(false);
                        }

                    System.out.println("LEAVING FFT LOOP");
//                    System.out.println(HexFileHelper.dumpHex(arrayData, 16));
//
                    // Help the gc by removing all arrays used in the FFT
                    // Keep the magnitudes array!
                    arrayData = null;
                    arrayReal = null;
                    arrayImaginary = null;

                    randomAccessFile.close();
                    }

                catch (final FileNotFoundException exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    //LOGGER.log();
                    }

                catch (final NumberFormatException exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    }

                catch (final IllegalArgumentException exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    //LOGGER.log();
                    }

                catch (final EOFException exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    }

                catch (final IOException exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    }

                catch (final Exception exception)
                    {
                    setRunning(false);
                    exception.printStackTrace();
                    }

                return (arrayMagnitudes);
                }


            /**************************************************************************************
             * When the Thread stops.
             */

            public void finished()
                {
                if ((get() != null)
                    && (get() instanceof double[]))
                    {
                    final double[] arrayMagnitudeResults;
                    final List<DataTypeDictionary> listDataTypes;
                    final List<Metadata> listMetaData;

                    System.out.println("FINISHED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    arrayMagnitudeResults = (double[])get();

                    setRunning(false);

                    System.out.println("LEAVING FFT LOOP arrayMagnitudes");
                    System.out.println(HexFileHelper.dumpDoubles(arrayMagnitudeResults,
                                                                 DecimalFormatPattern.DECIMAL_DOUBLE,
                                                                 16));

                    // Configure the DAO Channel DataType and Metadata
                    listDataTypes = new ArrayList<DataTypeDictionary>(1);
                    listDataTypes.add(DataTypeDictionary.DECIMAL_DOUBLE);

                    listMetaData = new ArrayList<Metadata>(1);
                    MetadataHelper.addNewMetadata(listMetaData,
                                                  MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                                  "Channel 0",
                                                  REGEX_STRING,
                                                  DataTypeDictionary.STRING,
                                                  SchemaUnits.DIMENSIONLESS,
                                                  "The Channel Name");
                    // ToDo Finish Metadata

                    dao.setRawDataChannelCount(DAO_CHANNEL_COUNT);
                    dao.setTemperatureChannel(false);
                    dao.setFilter(null);
                    dao.setTranslator(null);

                    dao.setRawData(new Vector<Object>(arrayMagnitudeResults.length));

                    System.out.println("PUT MAG IN VECTOR");
                    for (int intIndex = 0;
                         intIndex < arrayMagnitudeResults.length;
                         intIndex++)
                        {
                        final Vector<Object> vecData;

                        // We know we have *one* data channel for the FFT magnitudes (WRAPPER_CHANNEL_COUNT)
                        // The data output must be one Numeric for the X-axis Frequency,
                        // and one Numeric for the Channel data Value, known to the Filters
                        vecData = new Vector<Object>(2);

                        // The current Index is the X-axis
                        // ToDo Adjust for frequency +/- relative to origin
                        vecData.add(intIndex);

                        // Don't take the square root, because we need the Power Spectrum
                        vecData.add(arrayMagnitudeResults[intIndex]);

                        // Accumulate the data we have collected into the RawData of this DAO,
                        // but only if we can...
                        if (dao.getRawData() != null)
                            {
                            dao.getRawData().add(vecData);
                            //System.out.println("'[index=" + intIndex + "] [value=" + dblMagnitude + "]");
                            }
                        }


                    System.out.println("AFTER VECTOR, BEFORE COPY");
                    // Finally produce the complete XYDataset, with no filtering
                    // The supplied Metadata MUST contain the Observation.Channel.Name
                    DataFilterHelper.copyColumnarRawDataToXYDataset(dao,
                                                                    listDataTypes,
                                                                    listMetaData,
                                                                    isDebugMode(),
                                                                    SOURCE);
                    dao.setRawDataChanged(true);
                    dao.setProcessedDataChanged(true);
                    dao.setUnsavedData(true);

                    System.out.println("BEFORE WRAP++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

                    hostframeui.setWrappedData((new DAOWrapper(dao)),
                                                true,
                                                true);

                    //                ObservatoryInstrumentHelper.runGarbageCollector();
                    //
                    //                REGISTRY.getFramework().notifyFrameworkChangedEvent(dao.getHostInstrument());
                    //                InstrumentHelper.notifyInstrumentChanged(dao.getHostInstrument());
                    }
                }
            });
        }


    /***********************************************************************************************
     * Stop the Averager and dispose of all Resources.
     */

    private void disposeAverager()
        {
        final String SOURCE = "AveragingFFTDAO.disposeAverager() ";

        setRunning(false);

        SwingWorker.disposeWorker(getAveragerWorker(), true, SWING_WORKER_STOP_DELAY);
        setAveragerWorker(null);
        }


    /***********************************************************************************************
     * Get the state of the Semaphore controlling the Averager Thread.
     *
     * @return boolean
     */

    public boolean isRunning()
        {
        return (this.semaphore.getState());
        }


    /***********************************************************************************************
     * Set the state of the Semaphore controlling the Averager Thread.
     *
     * @param state
     */

    public void setRunning(final boolean state)
        {
        this.semaphore.setState(state);
        }


    /***********************************************************************************************
     * Get the SwingWorker which handles the Averager.
     *
     * @return SwingWorker
     */

    private SwingWorker getAveragerWorker()
        {
        return (this.workerAverager);
        }


    /***********************************************************************************************
     * Set the SwingWorker which handles the Averager.
     *
     * @param worker
     */

    private void setAveragerWorker(final SwingWorker worker)
        {
        this.workerAverager = worker;
        }


    /*********************************************************************************************/
    /* Events                                                                                    */
    /**********************************************************************************************
     * Indicate that the Metadata has changed.
     *
     * @param event
     */

    public void metadataChanged(final MetadataChangedEvent event)
        {
        System.out.println("DAO DETECTED CHANGED METADATA!!!! [key="
                               + event.getMetadataKey()
                               + "] [value=" + event.getMetadataValue()
                               + "] [state=" + event.getItemState().getName() + "]");
        }
    }
