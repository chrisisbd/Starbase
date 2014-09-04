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

package org.lmn.fc.ui.login;

import org.lmn.fc.common.exceptions.ReportException;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * The tab containing the ConfigurationReport.
 */

final class ConfigurationTab extends UIComponent
                             implements UIComponentPlugin
    {
    private ReportTablePlugin reportConfig;


    /***********************************************************************************************
     * Construct a ConfigurationTab.
     */

    ConfigurationTab()
        {
        super();

        reportConfig = null;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        setLayout(new BorderLayout());
        try
            {
            reportConfig = new ConfigurationReport(REGISTRY.getFramework().getRootTask(),
                                                   ConfigurationReport.REPORT_NAME,
                                                   REGISTRY.getFramework().getResourceKey());
            reportConfig.initialiseUI();
            add((JComponent)reportConfig);
            super.initialiseUI();
            }

        catch (ReportException exception)
            {
            LOGGER.error(EXCEPTION_GENERATE_REPORT + SPACE + ConfigurationReport.REPORT_NAME);
            }
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        reportConfig.disposeUI();
        super.disposeUI();
        }


    /***********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        reportConfig.runUI();
        super.runUI();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public void stopUI()
        {
        reportConfig.stopUI();
        super.stopUI();
        }
    }
