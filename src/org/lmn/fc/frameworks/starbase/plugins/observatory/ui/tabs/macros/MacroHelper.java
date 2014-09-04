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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.macros;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.model.xmlbeans.instruments.MacroType;

import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * MacroHelper.
 */

public final class MacroHelper implements FrameworkConstants,
                                          FrameworkStrings,
                                          FrameworkMetadata,
                                          FrameworkSingletons
    {
    /***********************************************************************************************
     * Get a Macro, given its Identifier.
     * Return null if the Identifier cannot be found in the List.
     *
     * @param macrolist
     * @param identifier
     *
     * @return MacroType
     */

    public static MacroType getMacroByIdentifier(final List<MacroType> macrolist,
                                                 final String identifier)
        {
        MacroType macroType;

        macroType = null;

        if ((macrolist != null)
            && (!macrolist.isEmpty())
            && (identifier != null)
            && (!EMPTY_STRING.equals(identifier)))
            {
            final Iterator<MacroType> iterMacros;

            iterMacros = macrolist.iterator();

            // Just iterate over all Macros (there are unlikely to be many...)
            while ((iterMacros.hasNext())
                && (macroType == null))
                {
                final MacroType macro;

                macro = iterMacros.next();

                // TODO Review equalsIgnoreCase()
                if ((macro != null)
                    && (identifier.equals(macro.getIdentifier())))
                    {
                    macroType = macro;
                    }
                }
            }

        return (macroType);
        }








    }
