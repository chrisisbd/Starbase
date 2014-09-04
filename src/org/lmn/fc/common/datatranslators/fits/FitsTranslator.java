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

package org.lmn.fc.common.datatranslators.fits;

import net.grelf.grip.Im;
import org.lmn.fc.common.datatranslators.DataTranslator;
import org.lmn.fc.common.datatranslators.DataTranslatorInterface;
import org.lmn.fc.common.datatranslators.DatasetType;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryClockInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.commands.AwaitingDevelopment;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import java.io.File;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * FitsTranslator.
 */

public final class FitsTranslator extends DataTranslator
                                  implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + FileUtilities.fits;


    /***********************************************************************************************
     * Construct a FitsTranslator.
     */

    public FitsTranslator()
        {
        super();

        setRawDataChannelCount(0);
        }


    /***********************************************************************************************
     * Import and translate data, producing RawData and RawDataMetadata.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean importRawData(final String filename,
                                 final Vector<Vector> eventlog,
                                 final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.importRawData()";
        boolean boolSuccess;

        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import an Image.
     *
     * @param filename
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean importImage(final String filename,
                               final Vector<Vector> eventlog,
                               final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.importImage()";
        boolean boolSuccess;

        LOGGER.debug("Importing FITS image from " + filename);

        if ((filename != null)
            && (filename.endsWith(FILENAME_EXTENSION))
            && (eventlog != null)
            && (clock != null))
            {
//            try
//                {
                final Im gripImage;

                gripImage = FitsHelper.load(new File(filename));

                if (gripImage != null)
                    {
                    setImage(gripImage.getImage());
                    boolSuccess = true;
                    }
                else
                    {
                    boolSuccess = false;
                    }
//                }
//
//            catch(FileNotFoundException exception)
//                {
//                addMessage("could not find the data file [exception=" + exception.getMessage() + "]");
//                boolSuccess = false;
//                }
//
//            catch(IOException exception)
//                {
//                addMessage("could not read the data file [exception=" + exception.getMessage() + "]");
//                boolSuccess = false;
//                }
            }
        else
            {
            addMessage("was given an invalid filename [filename=" + filename + "]");
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportXYDataset().
     *
     * @param wrapper
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportXYDataset(final DAOWrapperInterface wrapper,
                                   final String filename,
                                   final boolean timestamp,
                                   final Vector<Vector> eventlog,
                                   final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.exportXYDataset()";
        boolean boolSuccess;

        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportRawData().
     *
     * @param wrapper
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportRawData(final DAOWrapperInterface wrapper,
                                 final String filename,
                                 final boolean timestamp,
                                 final Vector<Vector> log,
                                 final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.exportRawData()";
        boolean boolSuccess;

        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportLog().
     *
     * @param logmetadata
     * @param logdata
     * @param logwidth
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportLog(final List<Metadata> logmetadata,
                             final Vector<Vector> logdata,
                             final int logwidth,
                             final String filename,
                             final boolean timestamp,
                             final Vector<Vector> eventlog,
                             final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.exportLog()";
        boolean boolSuccess;

        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportMetadata().
     *
     * @param metadatametadata
     * @param metadata
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportMetadata(final List<Metadata> metadatametadata,
                                  final List<Metadata> metadata,
                                  final String filename,
                                  final boolean timestamp,
                                  final Vector<Vector> log,
                                  final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.exportMetadata()";
        boolean boolSuccess;

        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportConfiguration().
     * The MetaData from the Instrument, Controller and Plugins,
     * and any extra Configuration (usually from the Instrument static configuration).
     *
     * @param configdatametadata
     * @param instrument
     * @param extraconfigdata
     * @param parentresourcekey
     * @param resourcekey
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportConfiguration(final List<Metadata> configdatametadata,
                                       final Instrument instrument,
                                       final Vector<Vector> extraconfigdata,
                                       final String parentresourcekey,
                                       final String resourcekey,
                                       final String filename,
                                       final boolean timestamp,
                                       final Vector<Vector> log,
                                       final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.exportConfiguration()";
        boolean boolSuccess;

        boolSuccess = false;

        return (boolSuccess);
        }


    /***********************************************************************************************
     * exportReportTable().
     * Export the the data from a general ReportTable.
     * Optional extra metadata may be added to the export.
     *
     * @param metadatametadata
     * @param metadata
     * @param infercolumnmetadata
     * @param datasettype
     * @param report
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportReportTable(final List<Metadata> metadatametadata,
                                     final List<Metadata> metadata,
                                     final boolean infercolumnmetadata,
                                     final DatasetType datasettype,
                                     final ReportTablePlugin report,
                                     final String filename,
                                     final boolean timestamp,
                                     final Vector<Vector> eventlog,
                                     final ObservatoryClockInterface clock)
        {
        final String SOURCE = "FitsTranslator.exportReportTable()";

        AwaitingDevelopment.informTranslatorUser(this,
                                                 METADATA_ACTION_EXPORT,
                                                 "report",
                                                 FILENAME_EXTENSION);

        return (false);
        }
    }
