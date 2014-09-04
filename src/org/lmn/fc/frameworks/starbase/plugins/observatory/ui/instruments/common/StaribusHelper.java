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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * StaribusHelper.
 */

public final class StaribusHelper implements FrameworkStrings,
                                             FrameworkMetadata,
                                             FrameworkRegex,
                                             FrameworkSingletons,
                                             ResourceKeys
    {
    // Mantis, Subversion and Jenkins for Starinet
    public static final String MANTIS_RSS_NAME_STARINET = "UKRAA-Starinet-Mantis";
    // Starinet is Project ID = 6
    public static final String MANTIS_RSS_URL_STARINET = "http://www.ukraa.com/bt/issues_rss.php?project_id=6";
    public static final String SUBVERSION_RSS_NAME_STARINET = "UKRAA-Starinet-Subversion";
    public static final String SUBVERSION_RSS_URL_STARINET = "http://www.ukraa.com/websvn/rss.php?repname=Starinet&path=%2F&isdir=1&";
    public static final String JENKINS_RSS_NAME_STARINET = "UKRAA-Starinet-Jenkins";
    public static final String JENKINS_RSS_URL_STARINET = "http://jenkins.ukraa.com:8080/jenkins/rssAll";


    /***********************************************************************************************
     * Create the Metadata to describe a MultiChannel Chart.
     *
     * @param title
     * @param xaxis
     * @param yaxis
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createMultichannelChartLegendMetadata(final String title,
                                                                       final String xaxis,
                                                                       final String yaxis)
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(3);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      title,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      FrameworkStrings.EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      xaxis,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      FrameworkStrings.EMPTY_STRING);

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      yaxis,
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      FrameworkStrings.EMPTY_STRING);
        return (listMetadata);
        }


    /***********************************************************************************************
     * Indicate if the Staribus Port is in debug mode.
     *
     * @param dao
     *
     * @return boolean
     */

    public static boolean isStaribusDebugMode(final ObservatoryInstrumentDAOInterface dao)
        {
        final boolean boolDebugMode;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (dao.getPort() != null)
            && (dao.getPort().isStaribusPort()))
            {
            final String strPortResourceKey;

            // Read the debug state of the Staribus Port
            // Common Port ResourceKey = Starbase.Observatory.PortName.
            strPortResourceKey = dao.getPort().getResourceKey();

            boolDebugMode = REGISTRY.getBooleanProperty(strPortResourceKey + KEY_ENABLE_DEBUG);
            }
        else
            {
            boolDebugMode = false;
            }

        return (boolDebugMode);
        }


    /***********************************************************************************************
     * Indicate if the Starinet Port is in debug mode.
     *
     * @param dao
     *
     * @return boolean
     */

    public static boolean isStarinetDebugMode(final ObservatoryInstrumentDAOInterface dao)
        {
        final boolean boolDebugMode;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (dao.getPort() != null))
            {
            final String strPortResourceKey;

            // Read the debug state of the DAO's Port
            strPortResourceKey = dao.getPort().getResourceKey();

            boolDebugMode = REGISTRY.getBooleanProperty(strPortResourceKey + KEY_ENABLE_DEBUG);
            }
        else
            {
            boolDebugMode = false;
            }

        return (boolDebugMode);
        }
    }
