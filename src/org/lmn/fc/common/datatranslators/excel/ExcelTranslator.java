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

package org.lmn.fc.common.datatranslators.excel;

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

import java.util.List;
import java.util.Vector;


/***********************************************************************************************
 * ExcelTranslator.
 */

public final class ExcelTranslator extends DataTranslator
                                   implements DataTranslatorInterface
    {
    // String Resources
    public static final String FILENAME_EXTENSION = DOT + FileUtilities.xls;


    /***********************************************************************************************
     * Construct a ExcelTranslator.
     */

    public ExcelTranslator()
        {
        super();
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
        return (false);
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
        return (false);
        }


    /***********************************************************************************************
     * exportRawData().
     *
     * @param wrapper
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportRawData(final DAOWrapperInterface wrapper,
                                 final String filename,
                                 final boolean timestamp,
                                 final Vector<Vector> eventlog,
                                 final ObservatoryClockInterface clock)
        {
        return (false);
        }


    /***********************************************************************************************
     * exportLog().
     *
     * @param logmetadata
     * @param logdata
     * @param logwidth
     * @param filename
     * @param timestamp
     * @param log
     * @param clock
     *
     * @return boolean
     */

    public boolean exportLog(final List<Metadata> logmetadata,
                             final Vector<Vector> logdata,
                             final int logwidth,
                             final String filename,
                             final boolean timestamp,
                             final Vector<Vector> log,
                             final ObservatoryClockInterface clock)
        {
        return (false);
        }


    /***********************************************************************************************
     * exportMetadata().
     *
     * @param metadatametadata
     *@param metadata
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
*      @return boolean
     */

    public boolean exportMetadata(final List<Metadata> metadatametadata, final List<Metadata> metadata,
                                  final String filename,
                                  final boolean timestamp,
                                  final Vector<Vector> eventlog,
                                  final ObservatoryClockInterface clock)
        {
        return (false);
        }


    /***********************************************************************************************
     * exportConfiguration().
     *
     * @param configmetadata
     * @param instrument
     * @param extraconfig
     * @param parentresourcekey
     * @param resourcekey
     * @param filename
     * @param timestamp
     * @param eventlog
     * @param clock
     *
     * @return boolean
     */

    public boolean exportConfiguration(final List<Metadata> configmetadata,
                                       final Instrument instrument,
                                       final Vector<Vector> extraconfig,
                                       final String parentresourcekey,
                                       final String resourcekey,
                                       final String filename,
                                       final boolean timestamp,
                                       final Vector<Vector> eventlog,
                                       final ObservatoryClockInterface clock)
        {
        return (false);
        }


    /***********************************************************************************************
     * exportReportTable().
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
        AwaitingDevelopment.informTranslatorUser(this,
                                                 METADATA_ACTION_EXPORT,
                                                 "report",
                                                 FILENAME_EXTENSION);

        return (false);
        }
    }
