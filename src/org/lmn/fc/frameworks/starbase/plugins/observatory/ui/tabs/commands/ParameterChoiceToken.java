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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.commands;


import org.lmn.fc.common.constants.FrameworkStrings;


/***************************************************************************************************
 * ParameterChoiceToken.
 */

public enum ParameterChoiceToken
    {
    PLUGIN_LIST                 (0, "!PluginList",              false, false),
    MIXER_SOURCE_LIST           (1, "!MixerSourceList",         false, false),
    EPHEMERIS_TARGETS           (2, "!EphemerisTargets",        false, false),
    FILTER_LIST                 (3, "!FilterList",              true,  true),
    EPOCH_LIST                  (4, "!EpochList",               false, false),
    OSCILLATOR_WAVEFORM_LIST    (5, "!OscillatorWaveformList",  false, true),
    EXPORTABLE_TABS_LIST        (6, "!ExportableTabsList",      false, false);

    // Suffixes
    public static final String CHOICE_SUFFIX_MODULE = "_Module";


    private final int intIndex;
    private final String strToken;
    private final boolean boolHasSubParameters;
    private final boolean boolHasHelp;


    /***********************************************************************************************
     * Get the ParameterChoiceToken enum corresponding to the specified Token.
     * Return NULL if the Token is not found.
     *
     * @param token
     *
     * @return ParameterChoiceToken
     */

    public static ParameterChoiceToken getParameterChoiceToken(final String token)
        {
        final String SOURCE = "ParameterChoiceToken.getParameterChoiceToken() ";
        ParameterChoiceToken parameterChoiceToken;

        parameterChoiceToken = null;

        if ((token != null)
            && (!FrameworkStrings.EMPTY_STRING.equals(token)))
            {
            final ParameterChoiceToken[] arrayTokens;
            boolean boolFoundIt;

            arrayTokens = values();
            boolFoundIt = false;

            for (int intTokenIndex = 0;
                 (!boolFoundIt) && (intTokenIndex < arrayTokens.length);
                 intTokenIndex++)
                {
                final ParameterChoiceToken choiceToken;

                choiceToken = arrayTokens[intTokenIndex];

                if (token.equals(choiceToken.getToken()))
                    {
                    parameterChoiceToken = choiceToken;
                    boolFoundIt = true;
                    }
                }
            }

        return (parameterChoiceToken);
        }


    /***********************************************************************************************
     * ParameterChoiceToken.
     *
     * @param index
     * @param token
     * @param hassubparameters
     * @param hashelp
     */

    private ParameterChoiceToken(final int index,
                                 final String token,
                                 final boolean hassubparameters,
                                 final boolean hashelp)
        {
        this.intIndex = index;
        this.strToken = token;
        this.boolHasSubParameters = hassubparameters;
        this.boolHasHelp = hashelp;
        }


    /***********************************************************************************************
     * Get the ParameterChoiceToken Index.
     *
     * @return int
     */

    public int getIndex()
        {
        return (this.intIndex);
        }


    /***********************************************************************************************
     * Get the ParameterChoiceToken Token.
     *
     * @return String
     */

    public String getToken()
        {
        return (this.strToken);
        }


    /***********************************************************************************************
     * Indicate if this Token has associated Sub Parameters.
     *
     * @return boolean
     */

    public boolean hasSubParameters()
        {
        return (this.boolHasSubParameters);
        }


    /***********************************************************************************************
     * Indicate if this Token has associated Help information.
     *
     * @return boolean
     */

    public boolean hasHelp()
        {
        return (this.boolHasHelp);
        }


    /***********************************************************************************************
     * Get the ParameterChoiceToken Token.
     *
     * @return
     */

    public String toString()
        {
        return (this.strToken);
        }
    }
