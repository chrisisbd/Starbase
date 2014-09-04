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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentHelper;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandProcessorContextInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.reports.ReportIcon;

import javax.swing.*;


/***************************************************************************************************
 * MacroUIComponentInterface.
 */

public interface MacroUIComponentInterface extends UIComponentPlugin,
                                                   FrameworkConstants,
                                                   FrameworkStrings,
                                                   FrameworkSingletons,
                                                   FrameworkMetadata
    {
    ImageIcon IMAGE_ICON_SUCCESS = RegistryModelUtilities.getCommonIcon(ResponseMessageStatus.SUCCESS.getEventStatus().getIconFilename());
    ImageIcon IMAGE_ICON_WARNING = RegistryModelUtilities.getCommonIcon(EventStatus.WARNING.getIconFilename());
    ImageIcon IMAGE_ICON_FATAL = RegistryModelUtilities.getCommonIcon(EventStatus.FATAL.getIconFilename());
    ImageIcon IMAGE_ICON_PLAIN = RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename());

    ImageIcon IMAGE_ICON_MACRO = ReportIcon.getIcon(InstrumentHelper.METADATA_ICON);
    ImageIcon IMAGE_ICON_MACROSTEP = RegistryModelUtilities.getCommonIcon(EventStatus.PLAIN.getIconFilename());


    /***********************************************************************************************
     * Get the MacroContext being displayed.
     * Return NULL if none.
     *
     * @return CommandProcessorContextInterface
     */

    CommandProcessorContextInterface getMacroContext();


    /***********************************************************************************************
     * Set the MacroContext to display.
     *
     * @param context
     */

    void setMacroContext(final CommandProcessorContextInterface context);


    /***********************************************************************************************
     * Indicate that there has been a change of Macro.
     */

    void macroChanged();


    /***********************************************************************************************
     * Set the TextEntry field.
     *
     * @param field
     */

    void setTextEntryField(JTextField field);
    }
