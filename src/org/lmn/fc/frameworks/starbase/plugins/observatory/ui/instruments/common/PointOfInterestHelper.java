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


import org.apache.xmlbeans.XmlException;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ObservatoryInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.PointOfInterestType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.DAOWrapperInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.logs.SimpleEventLogUIComponent;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeHelper;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.types.DegMinSec;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.POIDocument;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * PointOfInterestHelper.
 */

public final class PointOfInterestHelper implements FrameworkConstants,
                                                    FrameworkStrings,
                                                    FrameworkMetadata,
                                                    FrameworkSingletons,
                                                    ResourceKeys
    {
    // String Resources
    private static final String FILENAME_POI_SUFFIX = "-poi.xml";


    /***********************************************************************************************
     * Import the Framework POI and LOI.
     *
     * @param poitype
     * @param framework
     * @param dao
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importFrameworkPOIandLOI(final PointOfInterestType poitype,
                                                   final FrameworkPlugin framework,
                                                   final ObservatoryInstrumentDAOInterface dao) throws XmlException, IOException
        {
        final String SOURCE = "PointOfInterestHelper.importFrameworkPOIandLOI() ";
        final List<String> errors;
        final boolean boolSuccess;

        errors = new ArrayList<String>(10);

        // Imported Framework POIs and LOIs end up in the FrameworkData
        if (poitype != null)
            {
            final String strFilename;
            final String strPathnamePOI;

            // The filename is Framework-poi.xml
            strFilename = poitype.getName() + FILENAME_POI_SUFFIX;

            // The file is in the Framework imports folder
            strPathnamePOI = InstallationFolder.getTerminatedUserDir()
                                   + DataStore.CONFIG.getLoadFolder()
                                   + System.getProperty("file.separator")
                                   + strFilename;

            if ((framework != null)
                && (dao != null)
                && (dao.getHostInstrument() != null)
                && (dao.getHostInstrument().getContext() != null)
                && (dao.getHostInstrument().getContext().getObservatory() != null))
                {
                final List<Metadata> listMetadata;

                listMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                             dao.getHostInstrument().getContext().getObservatory(),
                                                                             dao.getHostInstrument(),
                                                                             dao, null,
                                                                             SOURCE,
                                                                             LOADER_PROPERTIES.isMetadataDebug());

                boolSuccess = readPOIandLOIFile(framework.getPointOfInterestList(),
                                                framework.getLineOfInterestList(),
                                                strPathnamePOI,
                                                listMetadata,
                                                errors);
                if (boolSuccess)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET
                                                       + poitype.getName() + TERMINATOR
                                                       + METADATA_ACTION_IMPORT_POI,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }
                }
            else
                {
                // It may be that the Instrument is not switched on, etc.
                errors.add(SOURCE + "Unable to Import Framework POI and LOI - is the Instrument switched off?");
                boolSuccess = false;
                }
            }
        else
            {
            errors.add(SOURCE + "Invalid POI Type");
            boolSuccess = false;
            }

        if ((!boolSuccess)
            && (LOADER_PROPERTIES.isMetadataDebug()))
            {
            LOGGER.errors(SOURCE, errors);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Observatory POI and LOI.
     *
     * @param poitype
     * @param framework
     * @param dao
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importObservatoryPOIandLOI(final PointOfInterestType poitype,
                                                     final FrameworkPlugin framework,
                                                     final ObservatoryInstrumentDAOInterface dao) throws XmlException, IOException
        {
        final String SOURCE = "PointOfInterestHelper.importObservatoryPOIandLOI() ";
        final List<String> errors;
        final boolean boolSuccess;

        errors = new ArrayList<String>(10);

        if (poitype != null)
            {
            final String strFilename;
            final String strPathnamePOI;

            // The filename is Observatory-poi.xml
            strFilename = poitype.getName() + FILENAME_POI_SUFFIX;

            // The file is in the Observatory imports folder
            strPathnamePOI = InstallationFolder.getTerminatedUserDir()
                                   + PATH_PLUGINS_OBSERVATORY
                                   + DataStore.CONFIG.getLoadFolder()
                                   + System.getProperty("file.separator")
                                   + strFilename;

            // Imported Observatory POIs and LOIs end up in the Observatory
            if ((framework != null)
                && (dao != null)
                && (dao.getHostInstrument() != null)
                && (dao.getHostInstrument().getContext() != null)
                && (dao.getHostInstrument().getContext().getObservatory() != null))
                {
                final List<Metadata> listMetadata;

                listMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                             dao.getHostInstrument().getContext().getObservatory(),
                                                                             dao.getHostInstrument(),
                                                                             dao, null,
                                                                             SOURCE,
                                                                             LOADER_PROPERTIES.isMetadataDebug());

                boolSuccess = readPOIandLOIFile(dao.getHostInstrument().getContext().getObservatory().getPointOfInterestList(),
                                                dao.getHostInstrument().getContext().getObservatory().getLineOfInterestList(),
                                                strPathnamePOI,
                                                listMetadata,
                                                errors);
                if (boolSuccess)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET
                                                       + poitype.getName() + TERMINATOR
                                                       + METADATA_ACTION_IMPORT_POI,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }
                }
            else
                {
                // It may be that the Instrument is not switched on, etc.
                errors.add(SOURCE + "Unable to Import Observatory POI and LOI - is the Instrument switched off?");
                boolSuccess = false;
                }
            }
        else
            {
            errors.add(SOURCE + "Invalid POI Type");
            boolSuccess = false;
            }

        if ((!boolSuccess)
            && (LOADER_PROPERTIES.isMetadataDebug()))
            {
            LOGGER.errors(SOURCE, errors);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Import the Instrument POI and LOI.
     *
     * @param poitype
     * @param framework
     * @param dao
     *
     * @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    public static boolean importInstrumentPOIandLOI(final PointOfInterestType poitype,
                                                    final FrameworkPlugin framework,
                                                    final ObservatoryInstrumentDAOInterface dao) throws XmlException, IOException
        {
        final String SOURCE = "PointOfInterestHelper.importInstrumentPOIandLOI() ";
        final List<String> errors;
        final boolean boolSuccess;

        errors = new ArrayList<String>(10);

        // We need the DAO in order to get the Instrument Identifier
        if ((poitype != null)
            && (dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            final String strIdentifier;
            final String strFilename;
            final String strPathnamePOI;

            // The filename is <Instrument.Identifier>-poi.xml
            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();
            strFilename = strIdentifier + FILENAME_POI_SUFFIX;

            // The file is in the Observatory imports folder
            strPathnamePOI = InstallationFolder.getTerminatedUserDir()
                                   + PATH_PLUGINS_OBSERVATORY
                                   + DataStore.CONFIG.getLoadFolder()
                                   + System.getProperty("file.separator")
                                   + strFilename;

            // Imported Instrument POIs and LOIs end up in the Instrument *DAO*
            // Those in the Instrument Schema must remain unaffected
            // so that they can be re-collected at any time
            if ((dao.getHostInstrument() != null)
                && (dao.getHostInstrument().getContext() != null)
                && (dao.getHostInstrument().getContext().getObservatory() != null))
                {
                final List<Metadata> listMetadata;

                listMetadata = MetadataHelper.collectAggregateMetadataTraced(framework,
                                                                             dao.getHostInstrument().getContext().getObservatory(),
                                                                             dao.getHostInstrument(),
                                                                             dao, null,
                                                                             SOURCE,
                                                                             LOADER_PROPERTIES.isMetadataDebug());

                boolSuccess = readPOIandLOIFile(dao.getPointOfInterestList(),
                                                dao.getLineOfInterestList(),
                                                strPathnamePOI,
                                                listMetadata,
                                                errors);
                if (boolSuccess)
                    {
                    SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                                       EventStatus.INFO,
                                                       METADATA_TARGET
                                                       + poitype.getName() + TERMINATOR
                                                       + METADATA_ACTION_IMPORT_POI,
                                                       SOURCE,
                                                       dao.getObservatoryClock());
                    }
                }
            else
                {
                // It may be that the Instrument is not switched on, etc.
                errors.add(SOURCE + "Unable to Import Instrument POI and LOI - is the Instrument switched off?");
                boolSuccess = false;
                }
            }
        else
            {
            errors.add(SOURCE + "Invalid POI Type, or is the Instrument switched off?");
            boolSuccess = false;
            }

        if ((!boolSuccess)
            && (LOADER_PROPERTIES.isMetadataDebug()))
            {
            LOGGER.errors(SOURCE, errors);
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Read the File of POI data and update the POI and LOI Lists.
     *
     * @param pois
     * @param lois
     * @param pathname
     * @param metadatalist
     * @param errors @return boolean
     *
     * @throws XmlException
     * @throws IOException
     */

    private static boolean readPOIandLOIFile(final List<PointOfInterest> pois,
                                             final List<LineOfInterest> lois,
                                             final String pathname,
                                             final List<Metadata> metadatalist,
                                             final List<String> errors) throws XmlException, IOException
        {
        final String SOURCE = "PointOfInterestHelper.readPOIandLOIFile() ";
        final File filePOI;
        final POIDocument docPOI;
        final boolean boolSuccess;

        filePOI = new File(pathname);

        docPOI = POIDocument.Factory.parse(filePOI);

        if (XmlBeansUtilities.isValidXml(docPOI))
            {
            // See if we have any POI to add
            if (docPOI.getPOI() != null)
                {
                // Assume that LOI cannot exist without some POI
                if ((docPOI.getPOI().getPointOfInterestList() != null)
                    && (!docPOI.getPOI().getPointOfInterestList().isEmpty()))
                    {
                    addPOIandLOIfromDocument(pois, lois, docPOI.getPOI(), metadatalist, errors);
                    boolSuccess = true;
                    }
                else
                    {
                    boolSuccess = false;
                    }
                }
            else
                {
                boolSuccess = false;
                }
            }
        else
            {
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Add the POIs and LOIs found in the specified document POI to the specified Lists.
     *
     * @param pois
     * @param lois
     * @param poi
     * @param metadatalist
     * @param errors
     */

    private static void addPOIandLOIfromDocument(final List<PointOfInterest> pois,
                                                 final List<LineOfInterest> lois,
                                                 final POIDocument.POI poi,
                                                 final List<Metadata> metadatalist,
                                                 final List<String> errors)
        {
        final String SOURCE = "PointOfInterestHelper.addPOIandLOIfromDocument() ";

        if (poi != null)
            {
            // First process the PointsOfInterest
            if ((poi.getPointOfInterestList() != null)
                && (pois != null))
                {
                final List<PointOfInterest> listPOI;
                final Iterator<PointOfInterest> iterPOI;

                listPOI = poi.getPointOfInterestList();
                iterPOI = listPOI.iterator();

                if (!pois.isEmpty())
                    {
                    while (iterPOI.hasNext())
                        {
                        final PointOfInterest instrPOI;

                        instrPOI = iterPOI.next();

                        // See if there is a conflict with existing POIs
                        addUniquePointOfInterest(pois, instrPOI, metadatalist, errors);
                        }
                    }
                else
                    {
                    // The PointOfInterest List is empty, so just add POI directly
                    while (iterPOI.hasNext())
                        {
                        final PointOfInterest instrPOI;

                        instrPOI = iterPOI.next();

                        pois.add((instrPOI));
                        }
                    }
                }

            // Now process the LinesOfInterest
            if ((poi.getLineOfInterestList() != null)
                && (lois != null))
                {
                final List<LineOfInterest> listLOI;
                final Iterator<LineOfInterest> iterLOI;

                listLOI = poi.getLineOfInterestList();
                iterLOI = listLOI.iterator();

                if (!lois.isEmpty())
                    {
                    while (iterLOI.hasNext())
                        {
                        final LineOfInterest instrLOI;

                        instrLOI = iterLOI.next();

                        // See if there is a conflict with existing LOIs
                        addUniqueLineOfInterest(lois, instrLOI, metadatalist, errors);
                        }
                    }
                else
                    {
                    // The LineOfInterest List is empty, so just add LOI directly
                    while (iterLOI.hasNext())
                        {
                        final LineOfInterest instrLOI;

                        instrLOI = iterLOI.next();

                        lois.add((instrLOI));
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Clear the Framework POI and LOI.
     *
     * @param poitype
     * @param framework
     * @param dao
     *
     * @return boolean
     */

    public static boolean removeFrameworkPOIandLOI(final PointOfInterestType poitype,
                                                   final FrameworkPlugin framework,
                                                   final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "PointOfInterestHelper.removeFrameworkPOIandLOI() ";
        boolean boolSuccess;

        boolSuccess = false;

        // Clear the Framework POIs and LOIs
        if ((poitype != null)
            && (framework != null)
            && (dao != null))
            {
            if (framework.getPointOfInterestList() != null)
                {
                framework.getPointOfInterestList().clear();
                boolSuccess = true;
                }

            if (framework.getLineOfInterestList() != null)
                {
                framework.getLineOfInterestList().clear();
                boolSuccess = true;
                }

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                   + poitype.getName() + TERMINATOR
                                                   + METADATA_ACTION_REMOVE_POI,
                                               SOURCE,
                                               dao.getObservatoryClock());
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Clear the Observatory POI and LOI.
     *
     * @param poitype
     * @param dao
     *
     * @return boolean
     */

    public static boolean removeObservatoryPOIandLOI(final PointOfInterestType poitype,
                                                     final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "PointOfInterestHelper.removeObservatoryPOIandLOI() ";
        boolean boolSuccess;

        boolSuccess = false;

        // Clear the Observatory POIs and LOIs
        if ((poitype != null)
            && (dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getContext() != null)
            && (dao.getHostInstrument().getContext().getObservatory() != null))
            {
            if (dao.getHostInstrument().getContext().getObservatory().getPointOfInterestList() != null)
                {
                dao.getHostInstrument().getContext().getObservatory().getPointOfInterestList().clear();
                boolSuccess = true;
                }

            if (dao.getHostInstrument().getContext().getObservatory().getLineOfInterestList() != null)
                {
                dao.getHostInstrument().getContext().getObservatory().getLineOfInterestList().clear();
                boolSuccess = true;
                }

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                   + poitype.getName() + TERMINATOR
                                                   + METADATA_ACTION_REMOVE_POI,
                                               SOURCE,
                                               dao.getObservatoryClock());
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Clear the Instrument, DAO and DaoWrapper POI and LOI.
     *
     * @param poitype
     * @param dao
     *
     * @return boolean
     */

    public static boolean removeInstrumentPOIandLOI(final PointOfInterestType poitype,
                                                    final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "PointOfInterestHelper.removeInstrumentPOIandLOI() ";
        boolean boolSuccess;

        boolSuccess = false;

        if ((poitype != null)
            && (dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null))
            {
            final String strIdentifier;

            strIdentifier = dao.getHostInstrument().getInstrument().getIdentifier();

            if (dao.getHostInstrument().getCompositePointOfInterestList() != null)
                {
                dao.getHostInstrument().getCompositePointOfInterestList().clear();
                boolSuccess = true;
                }

            if (dao.getHostInstrument().getCompositeLineOfInterestList() != null)
                {
                dao.getHostInstrument().getCompositeLineOfInterestList().clear();
                boolSuccess = true;
                }

            // DAO Wrapper
            if ((dao.getWrappedData() != null)
                && (dao.getWrappedData().getPointOfInterestList() != null))
                {
                dao.getWrappedData().getPointOfInterestList().clear();
                boolSuccess = true;
                }

            if ((dao.getWrappedData() != null)
                && (dao.getWrappedData().getLineOfInterestList() != null))
                {
                dao.getWrappedData().getLineOfInterestList().clear();
                boolSuccess = true;
                }

            // DAO
            if (dao.getPointOfInterestList() != null)
                {
                dao.getPointOfInterestList().clear();
                boolSuccess = true;
                }

            if (dao.getLineOfInterestList() != null)
                {
                dao.getLineOfInterestList().clear();
                boolSuccess = true;
                }

            SimpleEventLogUIComponent.logEvent(dao.getEventLogFragment(),
                                               EventStatus.INFO,
                                               METADATA_TARGET
                                                   + strIdentifier + TERMINATOR
                                                   + METADATA_ACTION_REMOVE_POI,
                                               SOURCE,
                                               dao.getObservatoryClock());
            }

        return (boolSuccess);
        }


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Add the specified POI to the List, but only if it is unique, i.e. not already in the List.
     * The POI are compared by {Longitude, Latitude}.
     *
     * @param pois
     * @param poi
     * @param metadatalist
     * @param errors
     */

    private static void addUniquePointOfInterest(final List<PointOfInterest> pois,
                                                 final PointOfInterest poi,
                                                 final List<Metadata> metadatalist,
                                                 final List<String> errors)
        {
        final String SOURCE = "PointOfInterestHelper.addUniquePointOfInterest() ";

        if ((pois != null)
            && (!findPointOfInterest(pois, poi, metadatalist, errors)))
            {
            pois.add(poi);
            }
        }


    /***********************************************************************************************
     * See if a PointOfInterest is contained in a List, return FALSE if the POI is not in the List.
     * Compares {Longitude, Latitude}.
     *
     * @param pois
     * @param poi
     * @param metadatalist
     * @param errors
     *
     * @return boolean
     */

    private static boolean findPointOfInterest(final List<PointOfInterest> pois,
                                               final PointOfInterest poi,
                                               final List<Metadata> metadatalist,
                                               final List<String> errors)
        {
        final String SOURCE = "PointOfInterestHelper.findPointOfInterest() ";
        final Iterator<PointOfInterest> iterPOI;
        boolean boolFoundIt;

        // Check each POI in turn for a complete match
        iterPOI = pois.iterator();
        boolFoundIt = false;

        while ((poi != null)
            && (!boolFoundIt)
            && (iterPOI.hasNext()))
            {
            final PointOfInterest pointOfInterest;

            pointOfInterest = iterPOI.next();

            // Compare {Deg Min Sec} but not the decimal fractions of Seconds
            boolFoundIt = poiSeemSameLocation(pointOfInterest, poi, metadatalist, SOURCE, errors);
            }

        if ((!boolFoundIt)
            && (LOADER_PROPERTIES.isMetadataDebug()))
            {
            LOGGER.errors(SOURCE, errors);
            }

        return (boolFoundIt);
        }


    /***********************************************************************************************
     * Indicate if the two specified POI appear to be at or near the same location.
     * Compares {Longitude and Latitude}.
     * Compare {Deg Min Sec} but not the decimal fractions of Seconds.
     *
     * @param poi0
     * @param poi1
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return boolean
     */

    private static boolean poiSeemSameLocation(final PointOfInterest poi0,
                                               final PointOfInterest poi1,
                                               final List<Metadata> metadatalist,
                                               final String SOURCE,
                                               final List<String> errors)
        {
        boolean boolTheSame;

        // Expect to fail...
        boolTheSame = false;

        if ((poi0 != null)
            && (poi1 != null))
            {
            final DegMinSecInterface dmsLongitude0;
            final DegMinSecInterface dmsLatitude0;

            final DegMinSecInterface dmsLongitude1;
            final DegMinSecInterface dmsLatitude1;

            // Retrieve all of the {Longitude, Latitude} pairs
            dmsLongitude0 = getPOILongitude(poi0, metadatalist, SOURCE, errors);
            dmsLatitude0 = getPOILatitude(poi0, metadatalist, SOURCE, errors);

            dmsLongitude1 = getPOILongitude(poi1, metadatalist, SOURCE, errors);
            dmsLatitude1 = getPOILatitude(poi1, metadatalist, SOURCE, errors);

            // If all points are valid, compare them for closeness
            if ((dmsLongitude0 != null)
                && (dmsLatitude0 != null)
                && (dmsLongitude1 != null)
                && (dmsLatitude1 != null))
                {
                final boolean boolLongitudesMatch;
                final boolean boolLatitudesMatch;

                // Compare {Deg Min Sec} but not the decimal fractions of Seconds
                boolLongitudesMatch = DegMinSec.dmsAreVeryClose(dmsLongitude0, dmsLongitude1);
                boolLatitudesMatch = DegMinSec.dmsAreVeryClose(dmsLatitude0, dmsLatitude1);

                boolTheSame = (boolLongitudesMatch && boolLatitudesMatch);
                }
            }

        return (boolTheSame);
        }


    /***********************************************************************************************
     * Gather all available PointsOfInterest from the Framework, Observatory, Instrument.
     * Combine the PointsOfInterest produced by the DAO
     * with that from the Framework, Observatory and Instrument.
     * The Wrapper data take precedence over those in the DAO,
     * but they should of course be the same as those in the DAO.
     * Assume that no duplicates exist.
     *
     * @param pois
     * @param framework
     * @param observatory
     * @param hostinstrument
     * @param wrapper
     * @param dao
     *
     * @return List<PointOfInterest>
     */

    public static List<PointOfInterest> collectPointsOfInterest(final List<PointOfInterest> pois,
                                                                final FrameworkPlugin framework,
                                                                final ObservatoryInterface observatory,
                                                                final ObservatoryInstrumentInterface hostinstrument,
                                                                final DAOWrapperInterface wrapper,
                                                                final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "PointOfInterestHelper.collectPointsOfInterest() ";

        // Framework
        if ((pois != null)
            && (framework != null))
            {
            final List<PointOfInterest> listFrameworkPOIs;
            final Iterator<PointOfInterest> iterFrameworkPOIs;

            // Add the PointsOfInterest from the Framework
            listFrameworkPOIs = framework.getPointOfInterestList();

            if ((listFrameworkPOIs != null)
                && (!listFrameworkPOIs.isEmpty()))
                {
                iterFrameworkPOIs = listFrameworkPOIs.iterator();

                while (iterFrameworkPOIs.hasNext())
                    {
                    pois.add(iterFrameworkPOIs.next());
                    }
                }
            }

        // Observatory
        if ((pois != null)
            && (observatory != null))
            {
            final List<PointOfInterest> listObservatoryPOIs;
            final Iterator<PointOfInterest> iterObservatoryPOIs;

            // Add the PointsOfInterest from the Observatory
            listObservatoryPOIs = observatory.getPointOfInterestList();

            if ((listObservatoryPOIs != null)
                && (!listObservatoryPOIs.isEmpty()))
                {
                iterObservatoryPOIs = listObservatoryPOIs.iterator();

                while (iterObservatoryPOIs.hasNext())
                    {
                    pois.add(iterObservatoryPOIs.next());
                    }
                }
            }

        // Instrument Schema
        if ((pois != null)
            && (hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            final List<PointOfInterest> listInstrumentPOIs;
            final Iterator<PointOfInterest> iterInstrumentPOIs;

            // Add the PointsOfInterest from the Instrument Schema
            listInstrumentPOIs = hostinstrument.getInstrument().getPointOfInterestList();

            if ((listInstrumentPOIs != null)
                && (!listInstrumentPOIs.isEmpty()))
                {
                iterInstrumentPOIs = listInstrumentPOIs.iterator();

                while (iterInstrumentPOIs.hasNext())
                    {
                    pois.add(iterInstrumentPOIs.next());
                    }
                }
            }

        // Wrapper
        if ((pois != null)
            && (wrapper != null))
            {
            final List<PointOfInterest> listWrapperPOIs;
            final Iterator<PointOfInterest> iterWrapperPOIs;

            // Add the PointsOfInterest from the Wrapper
            listWrapperPOIs = wrapper.getPointOfInterestList();

            if ((listWrapperPOIs != null)
                && (!listWrapperPOIs.isEmpty()))
                {
                iterWrapperPOIs = listWrapperPOIs.iterator();

                while (iterWrapperPOIs.hasNext())
                    {
                    pois.add(iterWrapperPOIs.next());
                    }
                }
            }

        // If no Wrapper present, then use the DAO data
        else if ((pois != null)
            && (dao != null))
            {
            final List<PointOfInterest> listDaoPOIs;
            final Iterator<PointOfInterest> iterDaoPOIs;

            // Add the PointsOfInterest from the DAO
            listDaoPOIs = dao.getPointOfInterestList();

            if ((listDaoPOIs != null)
                && (!listDaoPOIs.isEmpty()))
                {
                iterDaoPOIs = listDaoPOIs.iterator();

                while (iterDaoPOIs.hasNext())
                    {
                    pois.add(iterDaoPOIs.next());
                    }
                }
            }
        else
            {
            //LOGGER.warn(SOURCE + "No Wrapper or DAO POIs found");
            }

        return (pois);
        }


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Add the specified LOI to the List, but only if it is unique, i.e. not already in the List.
     * The LOI is compared by {StartLongitude, StartLatitude, EndLongitude, EndLatitude}.
     *
     * @param lois
     * @param loi
     * @param metadatalist
     * @param errors
     */

    private static void addUniqueLineOfInterest(final List<LineOfInterest> lois,
                                                final LineOfInterest loi,
                                                final List<Metadata> metadatalist,
                                                final List<String> errors)
        {
        final String SOURCE = "PointOfInterestHelper.addUniqueLineOfInterest() ";

        if ((lois != null)
            && (!findLineOfInterest(lois, loi, metadatalist, errors)))
            {
            lois.add(loi);
            }
        }


    /***********************************************************************************************
     * See if a LineOfInterest is contained in a List, return NULL if the LOI is not in the List.
     * Compares {StartLongitude, StartLatitude, EndLongitude, EndLatitude}.
     *
     * @param lois
     * @param loi
     * @param metadatalist
     * @param errors
     *
     * @return boolean
     */

    private static boolean findLineOfInterest(final List<LineOfInterest> lois,
                                              final LineOfInterest loi,
                                              final List<Metadata> metadatalist,
                                              final List<String> errors)
        {
        final String SOURCE = "PointOfInterestHelper.findLineOfInterest() ";
        final Iterator<LineOfInterest> iterLOI;
        boolean boolFoundIt;

        // Check each LOI in turn for a complete match
        iterLOI = lois.iterator();
        boolFoundIt = false;

        while ((loi != null)
            && (!boolFoundIt)
            && (iterLOI.hasNext()))
            {
            final LineOfInterest lineOfInterest;

            lineOfInterest = iterLOI.next();

            boolFoundIt = loiSeemSameLine(lineOfInterest, loi, metadatalist, SOURCE, errors);
            }

        if ((!boolFoundIt)
            && (LOADER_PROPERTIES.isMetadataDebug()))
            {
            LOGGER.errors(SOURCE, errors);
            }

        return (boolFoundIt);
        }


    /***********************************************************************************************
     * Indicate if the two specified LOI appear to be the same line.
     * Compares {StartLongitude, StartLatitude, EndLongitude, EndLatitude}.
     * Compare {Deg Min Sec} but not the decimal fractions of Seconds.
     *
     * @param loi0
     * @param loi1
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return boolean
     */

    private static boolean loiSeemSameLine(final LineOfInterest loi0,
                                           final LineOfInterest loi1,
                                           final List<Metadata> metadatalist,
                                           final String SOURCE,
                                           final List<String> errors)
        {
        boolean boolTheSame;

        // Expect to fail...
        boolTheSame = false;

        if ((loi0 != null)
            && (loi1 != null))
            {
            final DegMinSecInterface dmsStartLongitude0;
            final DegMinSecInterface dmsStartLatitude0;

            final DegMinSecInterface dmsStartLongitude1;
            final DegMinSecInterface dmsStartLatitude1;

            final DegMinSecInterface dmsEndLongitude0;
            final DegMinSecInterface dmsEndLatitude0;

            final DegMinSecInterface dmsEndLongitude1;
            final DegMinSecInterface dmsEndLatitude1;

            // Retrieve all of the {Longitude, Latitude} pairs
            dmsStartLongitude0 = getLOIStartLongitude(loi0, metadatalist, SOURCE, errors);
            dmsStartLatitude0 = getLOIStartLatitude(loi0, metadatalist, SOURCE, errors);

            dmsStartLongitude1 = getLOIStartLongitude(loi1, metadatalist, SOURCE, errors);
            dmsStartLatitude1 = getLOIStartLatitude(loi1, metadatalist, SOURCE, errors);

            dmsEndLongitude0 = getLOIEndLongitude(loi0, metadatalist, SOURCE, errors);
            dmsEndLatitude0 = getLOIEndLatitude(loi0, metadatalist, SOURCE, errors);

            dmsEndLongitude1 = getLOIEndLongitude(loi1, metadatalist, SOURCE, errors);
            dmsEndLatitude1 = getLOIEndLatitude(loi1, metadatalist, SOURCE, errors);

            // If all points are valid, compare them for closeness
            if ((dmsStartLongitude0 != null)
                && (dmsStartLatitude0 != null)
                && (dmsStartLongitude1 != null)
                && (dmsStartLatitude1 != null)
                && (dmsEndLongitude0 != null)
                && (dmsEndLatitude0 != null)
                && (dmsEndLongitude1 != null)
                && (dmsEndLatitude1 != null))
                {
                final boolean boolStartLongitudesMatch;
                final boolean boolStartLatitudesMatch;
                final boolean boolEndLongitudesMatch;
                final boolean boolEndLatitudesMatch;

                // Compare {Deg Min Sec} but not the decimal fractions of Seconds
                boolStartLongitudesMatch = DegMinSec.dmsAreVeryClose(dmsStartLongitude0, dmsStartLongitude1);
                boolStartLatitudesMatch = DegMinSec.dmsAreVeryClose(dmsStartLatitude0, dmsStartLatitude1);
                boolEndLongitudesMatch = DegMinSec.dmsAreVeryClose(dmsEndLongitude0, dmsEndLongitude1);
                boolEndLatitudesMatch = DegMinSec.dmsAreVeryClose(dmsEndLatitude0, dmsEndLatitude1);

                boolTheSame = (boolStartLongitudesMatch
                               && boolStartLatitudesMatch
                               && boolEndLongitudesMatch
                               && boolEndLatitudesMatch);
                }
            }

        return (boolTheSame);
        }


    /***********************************************************************************************
     * Gather all available LinesOfInterest from the Framework, Observatory, Instrument.
     * Combine the LinesOfInterest produced by the DAO
     * with that from the Framework, Observatory and Instrument.
     * The Wrapper data take precedence over those in the DAO,
     * but they should of course be the same as those in the DAO.
     * Assume that no duplicates exist.
     *
     * @param lois
     * @param framework
     * @param observatory
     * @param hostinstrument
     * @param wrapper
     * @param dao
     *
     * @return List<LineOfInterest>
     */

    public static List<LineOfInterest> collectLinesOfInterest(final List<LineOfInterest> lois,
                                                              final FrameworkPlugin framework,
                                                              final ObservatoryInterface observatory,
                                                              final ObservatoryInstrumentInterface hostinstrument,
                                                              final DAOWrapperInterface wrapper,
                                                              final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "PointOfInterestHelper.collectLinesOfInterest() ";

        final List<LineOfInterest> listLOI;

        listLOI = new ArrayList<LineOfInterest>(100);

        // Framework
        if ((lois != null)
            && (framework != null))
            {
            final List<LineOfInterest> listFrameworkLOIs;
            final Iterator<LineOfInterest> iterFrameworkLOIs;

            // Add the LinesOfInterest from the Framework
            listFrameworkLOIs = framework.getLineOfInterestList();

            if ((listFrameworkLOIs != null)
                && (!listFrameworkLOIs.isEmpty()))
                {
                iterFrameworkLOIs = listFrameworkLOIs.iterator();

                while (iterFrameworkLOIs.hasNext())
                    {
                    listLOI.add(iterFrameworkLOIs.next());
                    }
                }
            }

        // Observatory
        if ((lois != null)
            && (observatory != null))
            {
            final List<LineOfInterest> listObservatoryLOIs;
            final Iterator<LineOfInterest> iterObservatoryLOIs;

            // Add the LinesOfInterest from the Observatory
            listObservatoryLOIs = observatory.getLineOfInterestList();

            if ((listObservatoryLOIs != null)
                && (!listObservatoryLOIs.isEmpty()))
                {
                iterObservatoryLOIs = listObservatoryLOIs.iterator();

                while (iterObservatoryLOIs.hasNext())
                    {
                    listLOI.add(iterObservatoryLOIs.next());
                    }
                }
            }

        // Instrument Schema
        if ((lois != null)
            && (hostinstrument != null)
            && (hostinstrument.getInstrument() != null))
            {
            final List<LineOfInterest> listInstrumentLOIs;
            final Iterator<LineOfInterest> iterInstrumentLOIs;

            // Add the LinesOfInterest from the Instrument Schema
            listInstrumentLOIs = hostinstrument.getInstrument().getLineOfInterestList();

            if ((listInstrumentLOIs != null)
                && (!listInstrumentLOIs.isEmpty()))
                {
                iterInstrumentLOIs = listInstrumentLOIs.iterator();

                while (iterInstrumentLOIs.hasNext())
                    {
                    listLOI.add(iterInstrumentLOIs.next());
                    }
                }
            }

        if ((lois != null)
            && (wrapper != null))
            {
            final List<LineOfInterest> listWrapperLOIs;
            final Iterator<LineOfInterest> iterWrapperLOIs;

            // Add the LinesOfInterest from the Wrapper
            listWrapperLOIs = wrapper.getLineOfInterestList();

            if ((listWrapperLOIs != null)
                && (!listWrapperLOIs.isEmpty()))
                {
                iterWrapperLOIs = listWrapperLOIs.iterator();

                while (iterWrapperLOIs.hasNext())
                    {
                    lois.add(iterWrapperLOIs.next());
                    }
                }
            }

        // If no Wrapper present, then use the DAO data
        else if ((lois != null)
            && (dao != null))
            {
            final List<LineOfInterest> listDaoLOIs;
            final Iterator<LineOfInterest> iterDaoLOIs;

            // Add the LinesOfInterest from the DAO
            listDaoLOIs = dao.getLineOfInterestList();

            if ((listDaoLOIs != null)
                && (!listDaoLOIs.isEmpty()))
                {
                iterDaoLOIs = listDaoLOIs.iterator();

                while (iterDaoLOIs.hasNext())
                    {
                    lois.add(iterDaoLOIs.next());
                    }
                }
            }
        else
            {
            //LOGGER.warn(SOURCE + "No Wrapper or DAO LOIs found");
            }

        return (listLOI);
        }


    /**********************************************************************************************/
    /* POI and LOI Interrogators                                                                  */
    /***********************************************************************************************
     * Recover the Name of a POI, either from the POI or from Metadata linked via a Key.
     * Return EMPTY_STRING if the Name cannot be found.
     *
     * @param poi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return String
     */

    public static String getPOIName(final PointOfInterest poi,
                                    final List<Metadata> metadatalist,
                                    final String SOURCE,
                                    final List<String> errors)
        {
        final String strName;

        if (poi.getName() != null)
            {
            strName = poi.getName();
            }
        else if (poi.getNameKey() != null)
            {
            final Metadata mdName;

            // Return null if the key cannot be found in the List
            mdName = MetadataHelper.getMetadataByKey(metadatalist, poi.getNameKey());

            if (mdName != null)
                {
                strName = mdName.getValue();
                }
            else
                {
                errors.add(SOURCE + "POI Name Metadata not found [key=" + poi.getNameKey() + "]");
                strName = EMPTY_STRING;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI Name XML definition is invalid");
            strName = EMPTY_STRING;
            }

        return (strName);
        }


    /***********************************************************************************************
     * Recover the Description of a POI, either from the POI or from Metadata linked via a Key.
     * Return EMPTY_STRING if the Description cannot be found.
     *
     * @param poi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return String
     */

    public static String getPOIDescription(final PointOfInterest poi,
                                           final List<Metadata> metadatalist,
                                           final String SOURCE,
                                           final List<String> errors)
        {
        final String strDescription;

        if (poi.getDescription() != null)
            {
            strDescription = poi.getDescription();
            }
        else if (poi.getDescriptionKey() != null)
            {
            final Metadata mdDescription;

            // Return null if the key cannot be found in the List
            mdDescription = MetadataHelper.getMetadataByKey(metadatalist, poi.getDescriptionKey());

            if (mdDescription != null)
                {
                strDescription = mdDescription.getValue();
                }
            else
                {
                errors.add(SOURCE + "POI Description Metadata not found [key=" + poi.getDescriptionKey() + "]");
                strDescription = EMPTY_STRING;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI Description XML definition is invalid");
            strDescription = EMPTY_STRING;
            }

        return (strDescription);
        }


    /***********************************************************************************************
     * Recover the Longitude of a POI, either from the POI or from Metadata linked via a Key.
     * Return NULL if the Longitude cannot be found.
     *
     * @param poi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return DegMinSecInterface
     */

    public static DegMinSecInterface getPOILongitude(final PointOfInterest poi,
                                                     final List<Metadata> metadatalist,
                                                     final String SOURCE,
                                                     final List<String> errors)
        {
        final DegMinSecInterface dmsLongitude;

        if (poi.getLongitude() != null)
            {
            dmsLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(poi.getLongitude(),
                                                                                           DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                           EMPTY_STRING,
                                                                                           EMPTY_STRING,
                                                                                           errors);
            }
        else if (poi.getLongitudeKey() != null)
            {
            final Metadata mdLongitude;

            mdLongitude = MetadataHelper.getMetadataByKey(metadatalist, poi.getLongitudeKey());

            if (mdLongitude != null)
                {
                dmsLongitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(mdLongitude.getValue(),
                                                                                              DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                              EMPTY_STRING,
                                                                                              EMPTY_STRING,
                                                                                              errors);
                }
            else
                {
                errors.add(SOURCE + "POI Longitude Metadata not found [key=" + poi.getLongitudeKey() + "]");
                dmsLongitude = null;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI Longitude XML definition is invalid");
            dmsLongitude = null;
            }

        return (dmsLongitude);
        }


    /***********************************************************************************************
     * Recover the Latitude of a POI, either from the POI or from Metadata linked via a Key.
     * Return NULL if the Latitude cannot be found.
     *
     * @param poi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return DegMinSecInterface
     */

    public static DegMinSecInterface getPOILatitude(final PointOfInterest poi,
                                                    final List<Metadata> metadatalist,
                                                    final String SOURCE,
                                                    final List<String> errors)
        {
        final DegMinSecInterface dmsLatitude;

        if (poi.getLatitude() != null)
            {
            dmsLatitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(poi.getLatitude(),
                                                                                          DataTypeDictionary.LATITUDE,
                                                                                          EMPTY_STRING,
                                                                                          EMPTY_STRING,
                                                                                          errors);
            }
        else if (poi.getLatitudeKey() != null)
            {
            final Metadata mdLatitude;

            mdLatitude = MetadataHelper.getMetadataByKey(metadatalist, poi.getLatitudeKey());

            if (mdLatitude != null)
                {
                dmsLatitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(mdLatitude.getValue(),
                                                                                             DataTypeDictionary.LATITUDE,
                                                                                             EMPTY_STRING,
                                                                                             EMPTY_STRING,
                                                                                             errors);
                }
            else
                {
                errors.add(SOURCE + "POI Latitude Metadata not found [key=" + poi.getLatitudeKey() + "]");
                dmsLatitude = null;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI Latitude XML definition is invalid");
            dmsLatitude = null;
            }

        return (dmsLatitude);
        }


    /***********************************************************************************************
     * Recover the HASL of a POI, either from the POI or from Metadata linked via a Key.
     * Return BigDecimal.ZERO if the HASL cannot be found.
     *
     * @param poi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return BigDecimal
     */

    public static BigDecimal getPOIHASL(final PointOfInterest poi,
                                        final List<Metadata> metadatalist,
                                        final String SOURCE,
                                        final List<String> errors)
        {
        final BigDecimal decimalHASL;

        if (poi.getHASL() != null)
            {
            decimalHASL = poi.getHASL();
            }
        else if (poi.getHASLKey() != null)
            {
            final Metadata mdHASL;

            // Return null if the key cannot be found in the List
            mdHASL = MetadataHelper.getMetadataByKey(metadatalist, poi.getHASLKey());

            if (mdHASL != null)
                {
                decimalHASL = new BigDecimal(mdHASL.getValue());
                }
            else
                {
                errors.add(SOURCE + "POI HASL Metadata not found [key=" + poi.getHASLKey() + "]");
                decimalHASL = BigDecimal.ZERO;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI HASL XML definition is invalid");
            decimalHASL = BigDecimal.ZERO;
            }

        return (decimalHASL);
        }


    /***********************************************************************************************
     * Recover the IconFilename of a POI, either from the POI or from Metadata linked via a Key.
     * Return NULL if the IconFilename cannot be found.
     *
     * @param poi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return String
     */

    public static String getPOIIconFilename(final PointOfInterest poi,
                                            final List<Metadata> metadatalist,
                                            final String SOURCE,
                                            final List<String> errors)
        {
        final String strIconFilename;

        if (poi.getIconFilename() != null)
            {
            strIconFilename = poi.getIconFilename();
            }
        else if (poi.getIconFilenameKey() != null)
            {
            final Metadata mdIconFilename;

            // Return null if the key cannot be found in the List
            mdIconFilename = MetadataHelper.getMetadataByKey(metadatalist, poi.getIconFilenameKey());

            if (mdIconFilename != null)
                {
                strIconFilename = mdIconFilename.getValue();
                }
            else
                {
                errors.add(SOURCE + "POI IconFilename Metadata not found [key=" + poi.getIconFilenameKey() + "]");
                strIconFilename = EMPTY_STRING;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI IconFilename XML definition is invalid");
            strIconFilename = null;
            }

        return (strIconFilename);
        }


    /***********************************************************************************************
     * Recover the Label of a LOI, either from the LOI or from Metadata linked via a Key.
     * Return EMPTY_STRING if the Label cannot be found.
     *
     * @param loi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return String
     */

    public static String getLOILabel(final LineOfInterest loi,
                                     final List<Metadata> metadatalist,
                                     final String SOURCE,
                                     final List<String> errors)
        {
        final String strLabel;

        if (loi.getLabel() != null)
            {
            strLabel = loi.getLabel();
            }
        else if (loi.getLabelKey() != null)
            {
            final Metadata mdLabel;

            // Return null if the key cannot be found in the List
            mdLabel = MetadataHelper.getMetadataByKey(metadatalist, loi.getLabelKey());

            if (mdLabel != null)
                {
                strLabel = mdLabel.getValue();
                }
            else
                {
                errors.add(SOURCE + "LOI Label Metadata not found [key=" + loi.getLabelKey() + "]");
                strLabel = EMPTY_STRING;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "LOI Label XML definition is invalid");
            strLabel = EMPTY_STRING;
            }

        return (strLabel);
        }


    /***********************************************************************************************
     * Recover the Colour of a LOI, either from the LOI or from Metadata linked via a Key.
     * Return {r=0 g=0 b=0} if the Colour cannot be found.
     *
     * @param loi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return String
     */

    public static String getLOIColour(final LineOfInterest loi,
                                      final List<Metadata> metadatalist,
                                      final String SOURCE,
                                      final List<String> errors)
        {
        final String DEFAULT_COLOUR = "r=0 g=0 b=0";
        final String strColour;

        if (loi.getColour() != null)
            {
            strColour = loi.getColour();
            }
        else if (loi.getColourKey() != null)
            {
            final Metadata mdColour;

            // Return null if the key cannot be found in the List
            mdColour = MetadataHelper.getMetadataByKey(metadatalist, loi.getColourKey());

            if (mdColour != null)
                {
                strColour = mdColour.getValue();
                }
            else
                {
                errors.add(SOURCE + "LOI Colour Metadata not found [key=" + loi.getColourKey() + "]");
                strColour = DEFAULT_COLOUR;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "LOI Colour XML definition is invalid");
            strColour = DEFAULT_COLOUR;
            }

        return (strColour);
        }


    /***********************************************************************************************
     * Recover the StartLongitude of a LOI, either from the LOI or from Metadata linked via a Key.
     * Return NULL if the StartLongitude cannot be found.
     *
     * @param loi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return DegMinSecInterface
     */

    public static DegMinSecInterface getLOIStartLongitude(final LineOfInterest loi,
                                                          final List<Metadata> metadatalist,
                                                          final String SOURCE,
                                                          final List<String> errors)
        {
        final DegMinSecInterface dmsStartLongitude;

        if (loi.getStartLongitude() != null)
            {
            dmsStartLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(loi.getStartLongitude(),
                                                                                                DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                EMPTY_STRING,
                                                                                                EMPTY_STRING,
                                                                                                errors);
            }
        else if (loi.getStartLongitudeKey() != null)
            {
            final Metadata mdStartLongitude;

            mdStartLongitude = MetadataHelper.getMetadataByKey(metadatalist, loi.getStartLongitudeKey());

            if (mdStartLongitude != null)
                {
                dmsStartLongitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(mdStartLongitude.getValue(),
                                                                                                   DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                   EMPTY_STRING,
                                                                                                   EMPTY_STRING,
                                                                                                   errors);
                }
            else
                {
                errors.add(SOURCE + "LOI StartLongitude Metadata not found [key=" + loi.getStartLongitudeKey() + "]");
                dmsStartLongitude = null;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "LOI StartLongitude XML definition is invalid");
            dmsStartLongitude = null;
            }

        return (dmsStartLongitude);
        }


    /***********************************************************************************************
     * Recover the StartLatitude of a LOI, either from the LOI or from Metadata linked via a Key.
     * Return NULL if the StartLatitude cannot be found.
     *
     * @param loi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return DegMinSecInterface
     */

    public static DegMinSecInterface getLOIStartLatitude(final LineOfInterest loi,
                                                         final List<Metadata> metadatalist,
                                                         final String SOURCE,
                                                         final List<String> errors)
        {
        final DegMinSecInterface dmsStartLatitude;

        if (loi.getStartLatitude() != null)
            {
            dmsStartLatitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(loi.getStartLatitude(),
                                                                                               DataTypeDictionary.LATITUDE,
                                                                                               EMPTY_STRING,
                                                                                               EMPTY_STRING,
                                                                                               errors);
            }
        else if (loi.getStartLatitudeKey() != null)
            {
            final Metadata mdStartLatitude;

            mdStartLatitude = MetadataHelper.getMetadataByKey(metadatalist, loi.getStartLatitudeKey());

            if (mdStartLatitude != null)
                {
                dmsStartLatitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(mdStartLatitude.getValue(),
                                                                                                  DataTypeDictionary.LATITUDE,
                                                                                                  EMPTY_STRING,
                                                                                                  EMPTY_STRING,
                                                                                                  errors);
                }
            else
                {
                errors.add(SOURCE + "LOI StartLatitude Metadata not found [key=" + loi.getStartLatitudeKey() + "]");
                dmsStartLatitude = null;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "LOI StartLatitude XML definition is invalid");
            dmsStartLatitude = null;
            }

        return (dmsStartLatitude);
        }


    /***********************************************************************************************
     * Recover the EndLongitude of a LOI, either from the LOI or from Metadata linked via a Key.
     * Return NULL if the EndLongitude cannot be found.
     *
     * @param loi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return DegMinSecInterface
     */

    public static DegMinSecInterface getLOIEndLongitude(final LineOfInterest loi,
                                                        final List<Metadata> metadatalist,
                                                        final String SOURCE,
                                                        final List<String> errors)
        {
        final DegMinSecInterface dmsEndLongitude;

        if (loi.getEndLongitude() != null)
            {
            dmsEndLongitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(loi.getEndLongitude(),
                                                                                              DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                              EMPTY_STRING,
                                                                                              EMPTY_STRING,
                                                                                              errors);
            }
        else if (loi.getEndLongitudeKey() != null)
            {
            final Metadata mdEndLongitude;

            mdEndLongitude = MetadataHelper.getMetadataByKey(metadatalist, loi.getEndLongitudeKey());

            if (mdEndLongitude != null)
                {
                dmsEndLongitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(mdEndLongitude.getValue(),
                                                                                                 DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                 EMPTY_STRING,
                                                                                                 EMPTY_STRING,
                                                                                                 errors);
                }
            else
                {
                errors.add(SOURCE + "LOI EndLongitude Metadata not found [key=" + loi.getEndLongitudeKey() + "]");
                dmsEndLongitude = null;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "LOI EndLongitude XML definition is invalid");
            dmsEndLongitude = null;
            }

        return (dmsEndLongitude);
        }


    /***********************************************************************************************
     * Recover the EndLatitude of a LOI, either from the LOI or from Metadata linked via a Key.
     * Return NULL if the EndLatitude cannot be found.
     *
     * @param loi
     * @param metadatalist
     * @param SOURCE
     * @param errors
     *
     * @return DegMinSecInterface
     */

    public static DegMinSecInterface getLOIEndLatitude(final LineOfInterest loi,
                                                       final List<Metadata> metadatalist,
                                                       final String SOURCE,
                                                       final List<String> errors)
        {
        final DegMinSecInterface dmsEndLatitude;

        if (loi.getEndLatitude() != null)
            {
            dmsEndLatitude = (DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(loi.getEndLatitude(),
                                                                                             DataTypeDictionary.LATITUDE,
                                                                                             EMPTY_STRING,
                                                                                             EMPTY_STRING,
                                                                                             errors);
            }
        else if (loi.getEndLatitudeKey() != null)
            {
            final Metadata mdEndLatitude;

            mdEndLatitude = MetadataHelper.getMetadataByKey(metadatalist, loi.getEndLatitudeKey());

            if (mdEndLatitude != null)
                {
                dmsEndLatitude = (DegMinSecInterface)DataTypeHelper.parseDataTypeFromValueField(mdEndLatitude.getValue(),
                                                                                                DataTypeDictionary.LATITUDE,
                                                                                                EMPTY_STRING,
                                                                                                EMPTY_STRING,
                                                                                                errors);
                }
            else
                {
                errors.add(SOURCE + "LOI EndLatitude Metadata not found [key=" + loi.getEndLatitudeKey() + "]");
                dmsEndLatitude = null;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "LOI EndLatitude XML definition is invalid");
            dmsEndLatitude = null;
            }

        return (dmsEndLatitude);
        }


    /**********************************************************************************************/
    /* POI and LOI Setters                                                                        */
    /***********************************************************************************************
     * Set the Longitude of a POI, or of Metadata linked from the POI via a Key.
     * The specified Longitude is assumed to be syntactically correct.
     * Return false if the update failed for any reason.
     *
     * @param poi
     * @param metadatalist
     * @param longitude
     * @param SOURCE
     * @param errors
     *
     * @return boolean
     */

    public static boolean setPOILongitude(final PointOfInterest poi,
                                          final List<Metadata> metadatalist,
                                          final String longitude,
                                          final String SOURCE,
                                          final List<String> errors)
        {
        final boolean boolSuccess;

        if (poi.getLongitude() != null)
            {
            // It is a real POI, so just update it
            poi.setLongitude(longitude);
            boolSuccess = true;
            }
        else if (poi.getLongitudeKey() != null)
            {
            final Metadata mdLongitude;

            mdLongitude = MetadataHelper.getMetadataByKey(metadatalist, poi.getLongitudeKey());

            if (mdLongitude != null)
                {
                boolSuccess = MetadataHelper.setValueOnlyIfValid(mdLongitude, longitude);
                }
            else
                {
                errors.add(SOURCE + "POI Longitude Metadata not found [key=" + poi.getLongitudeKey() + "]");
                boolSuccess = false;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI Longitude XML definition is invalid");
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Set the Latitude of a POI, or of Metadata linked from the POI via a Key.
     * The specified Latitude is assumed to be syntactically correct.
     * Return false if the update failed for any reason.
     *
     * @param poi
     * @param metadatalist
     * @param latitude
     * @param SOURCE
     * @param errors
     *
     * @return boolean
     */

    public static boolean setPOILatitude(final PointOfInterest poi,
                                         final List<Metadata> metadatalist,
                                         final String latitude,
                                         final String SOURCE,
                                         final List<String> errors)
        {
        final boolean boolSuccess;

        if (poi.getLatitude() != null)
            {
            // It is a real POI, so just update it
            poi.setLatitude(latitude);
            boolSuccess = true;
            }
        else if (poi.getLatitudeKey() != null)
            {
            final Metadata mdLatitude;

            mdLatitude = MetadataHelper.getMetadataByKey(metadatalist, poi.getLatitudeKey());

            if (mdLatitude != null)
                {
                boolSuccess = MetadataHelper.setValueOnlyIfValid(mdLatitude, latitude);
                }
            else
                {
                errors.add(SOURCE + "POI Latitude Metadata not found [key=" + poi.getLatitudeKey() + "]");
                boolSuccess = false;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI Latitude XML definition is invalid");
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Set the HASL of a POI, or of Metadata linked from the POI via a Key.
     * Return false if the update failed for any reason.
     *
     * @param poi
     * @param metadatalist
     * @param hasl
     * @param SOURCE
     * @param errors
     *
     * @return boolean
     */

    public static boolean setPOIHASL(final PointOfInterest poi,
                                     final List<Metadata> metadatalist,
                                     final double hasl,
                                     final String SOURCE,
                                     final List<String> errors)
        {
        final boolean boolSuccess;

        if (poi.getHASL() != null)
            {
            // It is a real POI, so just update it
            poi.setHASL(new BigDecimal(hasl));
            boolSuccess = true;
            }
        else if (poi.getHASLKey() != null)
            {
            final Metadata mdHASL;

            // Return null if the key cannot be found in the List
            mdHASL = MetadataHelper.getMetadataByKey(metadatalist, poi.getHASLKey());

            if (mdHASL != null)
                {
                boolSuccess = MetadataHelper.setValueOnlyIfValid(mdHASL, Double.toString(hasl));
                }
            else
                {
                errors.add(SOURCE + "POI HASL Metadata not found [key=" + poi.getHASLKey() + "]");
                boolSuccess = false;
                }
            }
        else
            {
            // This should never happen!
            errors.add(SOURCE + "POI HASL XML definition is invalid");
            boolSuccess = false;
            }

        return (boolSuccess);
        }
    }
