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
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.Epoch;
import org.lmn.fc.frameworks.starbase.plugins.observatory.astronomy.utilities.ReferenceFrame;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DecimalFormatPattern;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.datatypes.HourMinSecInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * VLSRMetadataFactory.
 */

public final class VLSRMetadataFactory implements FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkRegex,
                                                  FrameworkSingletons,
                                                  ResourceKeys
    {
    /***********************************************************************************************
     * Calculate the Velocity of the Local Standard of Rest and set in ObservationMetadata.
     *
     * @param hmsra
     * @param dmsdec
     * @param epoch
     * @param frame
     * @param vlsr
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createVLSRObservationMetadata(final HourMinSecInterface hmsra,
                                                               final DegMinSecInterface dmsdec,
                                                               final Epoch epoch,
                                                               final ReferenceFrame frame,
                                                               final double vlsr)
        {
        final List<Metadata> listMetadata;

        listMetadata = new ArrayList<Metadata>(5);

        // Each item of Metadata must have {Key, Value, DataType, Units, Description}

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_VLSR_RA.getKey(),
                                      hmsra.toString(),
                                      REGEX_RIGHT_ASCENSION_HMS,
                                      DataTypeDictionary.RIGHT_ASCENSION,
                                      SchemaUnits.HOUR_MIN_SEC,
                                      "The Right Ascension of the VLSR calculation");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_VLSR_DEC.getKey(),
                                      dmsdec.toString(),
                                      REGEX_DECLINATION_DMS_SIGNED,
                                      DataTypeDictionary.DECLINATION,
                                      SchemaUnits.DEG_MIN_SEC,
                                      "The Declination of the VLSR calculation");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_VLSR_EPOCH.getKey(),
                                      epoch.getName(),
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.YEARS,
                                      "The Epoch of the VLSR calculation in Years");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_VLSR_FRAME.getKey(),
                                      frame.getName(),
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Reference Frame of the VLSR calculation");

        MetadataHelper.addNewMetadata(listMetadata,
                                      MetadataDictionary.KEY_OBSERVATION_VLSR_VALUE.getKey(),
                                      DecimalFormatPattern.VLSR.format(vlsr),
                                      REGEX_SIGNED_DECIMAL_DOUBLE,
                                      DataTypeDictionary.DECIMAL_DOUBLE,
                                      SchemaUnits.M_S_1,
                                      "The result of the VLSR calculation in metres per second");
        return (listMetadata);
        }
    }
