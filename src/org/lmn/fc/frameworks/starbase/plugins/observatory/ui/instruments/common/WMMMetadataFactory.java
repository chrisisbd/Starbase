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
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * WMMMetadataFactory.
 */

public final class WMMMetadataFactory implements FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkRegex,
                                                 FrameworkSingletons,
                                                 ResourceKeys
    {
    private static final int METRES_TO_KM = 1000;


    /***********************************************************************************************
     * Create the World Magnetic Model Observation Metadata.
     *
     * @param	latitude	        Latitude in decimal degrees
     * @param 	longitude           Longitude in decimal degrees
     * @param	year		        Date of the calculation in decimal years
     * @param	altitude_metres	    Altitude of the calculation in METRES
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createWMMObservationMetadata(final double latitude,
                                                              final double longitude,
                                                              final double year,
                                                              final double altitude_metres)
        {
        final String SOURCE = "WMMMetadataFactory.createWMMObservationMetadata() ";
        final List<Metadata> listMetadata;
        final WorldMagneticModel wmm;
        final double dblAltitude_km;

        listMetadata = new ArrayList<Metadata>(11);

        dblAltitude_km = altitude_metres / METRES_TO_KM;

        wmm = new WorldMagneticModel();

        // Each item of Metadata must have {Key, Value, DataType, Units, Description}
        // Regex is optional

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_LATITUDE.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(latitude),
                                      REGEX_LATITUDE_DEG_SIGNED,
                                      DataTypeDictionary.LATITUDE,
                                      SchemaUnits.DEGREES,
                                      "The Latitude of the WMM calculation");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_LONGITUDE.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(longitude),
                                      REGEX_LONGITUDE_DEG_SIGNED,
                                      DataTypeDictionary.SIGNED_LONGITUDE,
                                      SchemaUnits.DEGREES,
                                      "The Longitude of the WMM calculation");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_EPOCH.getKey(),
                                      Double.toString(year),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.YEARS,
                                      "The Year of the WMM calculation");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_ALTITUDE.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(dblAltitude_km),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.KM,
                                      "The Altitude of the WMM calculation in km");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_DECLINATION.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(wmm.getDeclination(latitude, longitude, year, dblAltitude_km)),
                                      REGEX_ANGLE_SIGNED,
                                      DataTypeDictionary.ANGLE,
                                      SchemaUnits.DEGREES,
                                      "The WMM Declination component in degrees");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_DIP.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(wmm.getDip(latitude, longitude, year, dblAltitude_km)),
                                      REGEX_ANGLE_SIGNED,
                                      DataTypeDictionary.ANGLE,
                                      SchemaUnits.DEGREES,
                                      "The WMM Dip component in degrees");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_INTENSITY_TOTAL.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(wmm.getTotalIntensity(latitude, longitude, year, dblAltitude_km)),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.N_T,
                                      "The WMM Total Intensity component in nanoTesla");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_INTENSITY_EAST.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(wmm.getEastIntensity(latitude, longitude, year, dblAltitude_km)),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.N_T,
                                      "The WMM East Intensity component in nanoTesla");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_INTENSITY_NORTH.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(wmm.getNorthIntensity(latitude, longitude, year, dblAltitude_km)),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.N_T,
                                      "The WMM North Intensity component in nanoTesla");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_INTENSITY_HORIZONTAL.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(wmm.getHorizontalIntensity(latitude, longitude, year, dblAltitude_km)),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.N_T,
                                      "The WMM Horizontal Intensity component in nanoTesla");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_WMM_INTENSITY_VERTICAL.getKey(),
                                      DecimalFormatPattern.DECIMAL_DOUBLE.format(wmm.getVerticalIntensity(latitude, longitude, year, dblAltitude_km)),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.N_T,
                                      "The WMM Vertical Intensity component in nanoTesla");

        return (listMetadata);
        }
    }
