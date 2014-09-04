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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.InstrumentUIComponentDecoratorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTCanvasInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTSidebarInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.fft.AveragingFFTToolbarInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.UIComponentPlugin;

import java.util.List;


/***************************************************************************************************
 * AveragingFFTUIComponentInterface.
 */

public interface AveragingFFTUIComponentInterface extends UIComponentPlugin
    {
    // String Resources
    String KEY_FFT_COUNT =          MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "FFT.Count";
    String KEY_FFT_LENGTH =         MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "FFT.Length";
    String KEY_FILE_BLOCKS =        MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "File.Blocks";
    String KEY_FILE_NAME =          MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "File.Name";
    String KEY_MODE_DISPLAY =       MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Mode.Display";
    String KEY_MODE_IQ =            MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Mode.IQ";
    String KEY_MODE_PLOT =          MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Mode.Plot";
    String KEY_OFFSET_AMPLITUDE =   MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Offset.Amplitude";
    String KEY_OFFSET_PHASE =       MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Offset.Phase";
    String KEY_SAMPLE_RATE =        MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Sample.Rate";
    String KEY_TEMPERATURE_FACTOR = MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Temperature.Factor";
    String KEY_TIME_CONSTANT =      MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Time.Constant";
    String KEY_WINDOW =             MetadataDictionary.KEY_INSTRUMENT_ROOT.getKey() + "Window";


    /**********************************************************************************************/
    /* DAO                                                                                        */
    /***********************************************************************************************
     * Set the data from the DAO finished() method.
     * Optionally refresh the UI of data tabs or update the associated Metadata.
     * This is used only on ControlPanels, InstrumentPanels and their UIComponents.
     *
     * @param daowrapper
     * @param updatedata
     * @param updatemetadata
     */

    void setWrappedData(DAOWrapperInterface daowrapper,
                        boolean updatedata,
                        boolean updatemetadata);


    /**********************************************************************************************/
    /* User Interface                                                                             */
    /***********************************************************************************************
     * Get the Canvas.
     *
     * @return AveragingFFTCanvasInterface
     */

    AveragingFFTCanvasInterface getCanvas();


    /***********************************************************************************************
     * Get the Sidebar.
     *
     * @return AveragingFFTSidebarInterface
     */

    AveragingFFTSidebarInterface getSidebar();
    }
