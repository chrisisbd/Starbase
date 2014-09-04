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

package org.lmn.fc.common.datatranslators;

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.datatranslators.csv.TimestampedCommaSeparatedTranslator;
import org.lmn.fc.common.datatranslators.excel.ExcelTranslator;
import org.lmn.fc.common.datatranslators.fits.FitsTranslator;
import org.lmn.fc.common.datatranslators.goesxray.GoesXrayTranslator;
import org.lmn.fc.common.datatranslators.html.HtmlTranslator;
import org.lmn.fc.common.datatranslators.johncook.JohnCookTranslator;
import org.lmn.fc.common.datatranslators.skypipe.RadioSkyPipeTranslator;
import org.lmn.fc.common.datatranslators.stardata.CompressedStardataTranslator;
import org.lmn.fc.common.datatranslators.stardata.FormattedStardataTranslator;
import org.lmn.fc.common.datatranslators.tsv.TimestampedTabSeparatedTranslator;
import org.lmn.fc.common.utilities.files.FileUtilities;


/***************************************************************************************************
 * DataFormats used in import and export by the DataTranslators.
 */

public enum DataFormat implements FrameworkStrings
    {
    // XML

    STARDATA_FORMATTED("FormattedStardata",
                       FormattedStardataTranslator.FILENAME_EXTENSION,
                       "org.lmn.fc.common.datatranslators.stardata.FormattedStardataTranslator",
                       true,
                       true),

    STARDATA_COMPRESSED("CompressedStardata",
                        CompressedStardataTranslator.FILENAME_EXTENSION,
                        "org.lmn.fc.common.datatranslators.stardata.CompressedStardataTranslator",
                        true,
                        true),

    // Token Separated

//    TSV_INDEXED     ("IndexedTabSeparated",
//                      IndexedTabSeparatedTranslator.FILENAME_EXTENSION,
//                      "org.lmn.fc.common.datatranslators.tsv.IndexedTabSeparatedTranslator",
//                      true,
//                      true),
//
//    TSV_TIMESTAMPED ("TimestampedTabSeparated",
//                      TimestampedTabSeparatedTranslator.FILENAME_EXTENSION,
//                      "org.lmn.fc.common.datatranslators.tsv.TimestampedTabSeparatedTranslator",
//                      true,
//                      true),

    TSV             ("TabSeparated",
                     TimestampedTabSeparatedTranslator.FILENAME_EXTENSION,
                     "org.lmn.fc.common.datatranslators.tsv.TabSeparatedTranslator",
                     true,
                     true),

//    CSV_INDEXED     ("IndexedCommaSeparated",
//                      IndexedCommaSeparatedTranslator.FILENAME_EXTENSION,
//                      "org.lmn.fc.common.datatranslators.csv.IndexedCommaSeparatedTranslator",
//                      true,
//                      true),
//
//    CSV_TIMESTAMPED ("TimestampedCommaSeparated",
//                      TimestampedCommaSeparatedTranslator.FILENAME_EXTENSION,
//                      "org.lmn.fc.common.datatranslators.csv.TimestampedCommaSeparatedTranslator",
//                      true,
//                      true),

    CSV              ("CommaSeparated",
                      TimestampedCommaSeparatedTranslator.FILENAME_EXTENSION,
                      "org.lmn.fc.common.datatranslators.csv.CommaSeparatedTranslator",
                      true,
                      true),

    // Miscellaneous

    EXCEL           ("Excel",
                      ExcelTranslator.FILENAME_EXTENSION,
                      "org.lmn.fc.common.datatranslators.excel.ExcelTranslator",
                      false,
                      true),

    HTML            ("HTML",
                      HtmlTranslator.FILENAME_EXTENSION,
                      "org.lmn.fc.common.datatranslators.html.HtmlTranslator",
                      false,
                      true),

    RADIOSKYPIPE    ("RadioSkyPipe",
                      RadioSkyPipeTranslator.FILENAME_EXTENSION,
                      "org.lmn.fc.common.datatranslators.skypipe.RadioSkyPipeTranslator",
                      true,
                      true),

    GOESXRAY        ("GOESXray",
                      GoesXrayTranslator.FILENAME_EXTENSION,
                      "org.lmn.fc.common.datatranslators.goesxray.GoesXrayTranslator",
                      true,
                      false),

    JOHNCOOK        ("JohnCook",
                      JohnCookTranslator.FILENAME_EXTENSION,
                      "org.lmn.fc.common.datatranslators.johncook.JohnCookTranslator",
                      true,
                      false),

    FITS            ("FITS",
                      FitsTranslator.FILENAME_EXTENSION,
                      "org.lmn.fc.common.datatranslators.fits.FitsTranslator",
                      true,
                      false),

    // DataFormats not using a DataTranslator

    TXT             ("TXT",
                      DOT + FileUtilities.txt,
                      "",
                      false,
                      true),

    XML             ("XML",
                      DOT + FileUtilities.xml,
                      "",
                      true,
                      true),

    PDF             ("PDF",
                      DOT + FileUtilities.pdf,
                      "",
                      false,
                      true),

    H                ("h",
                      DOT + FileUtilities.h,
                      "",
                      false,
                      true),

    C                ("c",
                      DOT + FileUtilities.c,
                      "",
                      false,
                      true),

    STARIBUS        ("Staribus",
                     DOT + FileUtilities.data,
                     "",
                     false,
                     false);


    private final String strName;
    private final String strFileExtension;
    private final String strTranslatorClassname;
    private final boolean boolImportable;
    private final boolean boolExportable;


    /***********************************************************************************************
     * Get the DataFormat enum corresponding to the specified DataFormat name.
     * Return NULL if not found.
     *
     * @param name
     *
     * @return DataFormat
     */

    public static DataFormat getDataFormatForName(final String name)
        {
        DataFormat dataFormatType;

        dataFormatType = null;

        if ((name != null)
            && (!EMPTY_STRING.equals(name)))
            {
            final DataFormat[] types;
            boolean boolFoundIt;

            types = values();
            boolFoundIt = false;

            for (int i = 0;
                 (!boolFoundIt) && (i < types.length);
                 i++)
                {
                final DataFormat format;

                format = types[i];

                if (name.equals(format.getName()))
                    {
                    dataFormatType = format;
                    boolFoundIt = true;
                    }
                }
            }

        return (dataFormatType);
        }


    /***********************************************************************************************
     * DataFormat.
     *
     * @param name
     * @param extension
     * @param translator
     * @param isimportable
     * @param isexportable
     */

    private DataFormat(final String name,
                       final String extension,
                       final String translator,
                       final boolean isimportable,
                       final boolean isexportable)
        {
        this.strName = name;
        this.strFileExtension = extension;
        this.strTranslatorClassname = translator;
        this.boolImportable = isimportable;
        this.boolExportable = isexportable;
        }


    /***********************************************************************************************
     * Get the name of the DataFormat.
     *
     * @return String
     */

    public String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Get the FileExtension of the DataFormat.
     *
     * @return String
     */

    public String getFileExtension()
        {
        return (this.strFileExtension);
        }


    /***********************************************************************************************
     * Get the TranslatorClassname of the DataFormat.
     *
     * @return String
     */

    public String getTranslatorClassname()
        {
        return (this.strTranslatorClassname);
        }


    /***********************************************************************************************
     * Indicate if the DataFormat may be imported.
     *
     * @return boolean
     */

    public boolean isImportable()
        {
        return (this.boolImportable);
        }


    /***********************************************************************************************
     * Indicate if the DataFormat may be imported.
     *
     * @return boolean
     */

    public boolean isExportable()
        {
        return (this.boolExportable);
        }
    }
