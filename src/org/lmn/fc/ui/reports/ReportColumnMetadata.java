// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  03-03-04    LMN created file from Swing book
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.reports;

import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;


/***************************************************************************************************
 * ReportColumnData.
 */

public final class ReportColumnMetadata
    {
    private String metadataName;
    private SchemaDataType.Enum metadataDataType;
    private SchemaUnits.Enum metadataUnits;
    private String metadataDescription;
    private int intColumnAlignment;


    /***********************************************************************************************
     * Construct a ReportColumnData.
     *
     * @param name
     * @param datatype
     * @param units
     * @param description
     * @param alignment
     */

    public ReportColumnMetadata(final String name,
                                final SchemaDataType.Enum datatype,
                                final SchemaUnits.Enum units,
                                final String description,
                                final int alignment)
        {
        this.metadataName = name;
        this.metadataDataType = datatype;
        this.metadataUnits = units;
        this.metadataDescription = description;
        intColumnAlignment = alignment;
        }


    /***********************************************************************************************
     * Get the Column Name.
     *
     * @return String
     */

    public String getName()
        {
        return (this.metadataName);
        }


    /***********************************************************************************************
     * Set the Column Name.
     *
     * @param name
     */

    public void setName(final String name)
        {
        this.metadataName = name;
        }


    /***********************************************************************************************
     * Get the Column DataType.
     *
     * @return SchemaDataType.Enum
     */

    public SchemaDataType.Enum getDataType()
        {
        return (this.metadataDataType);
        }


    /***********************************************************************************************
     * Set the Column DataType.
     *
     * @param datatype
     */

    public void setDataType(final SchemaDataType.Enum datatype)
        {
        this.metadataDataType = datatype;
        }


    /***********************************************************************************************
     * Get the Column Units.
     *
     * @return SchemaUnits.Enum
     */

    public SchemaUnits.Enum getUnits()
        {
        return (this.metadataUnits);
        }


    /***********************************************************************************************
     * Set the Column Units.
     *
     * @param units
     */

    public void setUnits(final SchemaUnits.Enum units)
        {
        this.metadataUnits = units;
        }


    /***********************************************************************************************
     * Get the Column Description.
     *
     * @return String
     */

    public String getDescription()
        {
        return (this.metadataDescription);
        }


    /***********************************************************************************************
     * Set the Column Description.
     *
     * @param description
     */

    public void setDescription(final String description)
        {
        this.metadataDescription = description;
        }


    /***********************************************************************************************
     * Get the Column Alignment.
     *
     * @return int
     */

    public int getColumnAlignment()
        {
        return (this.intColumnAlignment);
        }


    /***********************************************************************************************
     * Set the Column Alignment.
     *
     * @param alignment
     */

    public void setColumnAlignment(final int alignment)
        {
        this.intColumnAlignment = alignment;
        }
    }
