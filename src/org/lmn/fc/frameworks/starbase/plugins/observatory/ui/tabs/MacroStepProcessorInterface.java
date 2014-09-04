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

import org.lmn.fc.common.constants.*;
import org.lmn.fc.model.xmlbeans.instruments.*;


/***************************************************************************************************
 * MacroStepProcessorInterface.
 */

public interface MacroStepProcessorInterface extends FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkMetadata,
                                                     FrameworkSingletons,
                                                     FrameworkXpath,
                                                     ResourceKeys
    {
    /***********************************************************************************************
     * Find the Instrument, Plugin, Command for the MacroStep in the injected Context.
     * Return the Command for convenience.
     *
     * @return CommandType
     */

    CommandType locateStepContext();


    /***********************************************************************************************
     * Get the Instrument corresponding to the MacroStep.
     *
     * @return Instrument
     */

    Instrument getInstrument();


    /***********************************************************************************************
     * Get the Controller corresponding to the MacroStep.
     *
     * @return Controller
     */

    Controller getController();


    /***********************************************************************************************
     * Get the Plugin (Module) corresponding to the MacroStep.
     *
     * @return PluginType
     */

    PluginType getPlugin();


    /***********************************************************************************************
     * Get the Command corresponding to the MacroStep.
     *
     * @return CommandType
     */

    CommandType getCommand();


    /***********************************************************************************************
     * Indicate if the MacroStep has a Label.
     *
     * @return boolean
     */

    boolean hasLabel();


    /***********************************************************************************************
     * A convenience method to get the MacroStep label, if available.
     * Return an empty String if not.
     *
     * @return String
     */

    String getLabel();


    /***********************************************************************************************
     * Indicate if the MacroStep is a Comment.
     *
     * @return boolean
     */

    boolean isComment();


    /***********************************************************************************************
     * A convenience method to get the MacroStep Comment, if available.
     * Return an empty String if not.
     *
     * @return String
     */

    String getComment();


    /***********************************************************************************************
     * Indicate if the MacroStep is Starscript.
     *
     * @return boolean
     */

    boolean isStarscript();


    /***********************************************************************************************
     * A convenience method to get the MacroStep Starscript, if available.
     * Return a NULL if not.
     *
     * @return StepCommandType
     */

    StepCommandType getStarscript();
    }
