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

import org.lmn.fc.ui.UIComponentPlugin;

import java.awt.print.Printable;
import java.net.URL;


/***************************************************************************************************
 * PublisherUIComponentInterface.
 */

public interface PublisherUIComponentInterface extends UIComponentPlugin
    {
    /**********************************************************************************************/
    /* UI                                                                                         */
    /***********************************************************************************************
     * Get the URL providing the Publisher content.
     *
     * @return URL
     */

    URL getContentURL();


    /***********************************************************************************************
     * Set the URL providing the Publisher content.
     *
     * @param url
     */

    void setContentURL(URL url);


    /***********************************************************************************************
     * Update the content to be displayed.
     */

    void updateContent();


    /***********************************************************************************************
     * Zoom Out by the specified increment, starting from the current scale factor.
     *
     * @param zoommin
     * @param zoommax
     * @param zoomincrement
     * @param scalefactor
     */

    void zoomOut(float zoommin,
                 float zoommax,
                 float zoomincrement,
                 float scalefactor);


    /***********************************************************************************************
     * Zoom In by the specified increment, starting from the current scale factor..
     *
     * @param zoommin
     * @param zoommax
     * @param zoomincrement
     * @param scalefactor
     */

    void zoomIn(float zoommin,
                float zoommax,
                float zoomincrement,
                float scalefactor);


    /***********************************************************************************************
     * Reload the current document.
     */

    void reloadDocument();


    /***********************************************************************************************
     * Remove the current document.
     */

    void removeDocument();


    /***********************************************************************************************
     * Get the Printable Publication.
     *
     * @return Printable
     */

    Printable getPrintable();


    /***********************************************************************************************
     * Get the CurrentPage.
     *
     * @return int
     */

    int getCurrentPage();


    /***********************************************************************************************
     * Set the CurrentPage.
     *
     * @param page
     */

    void setCurrentPage(int page);


    /***********************************************************************************************
     * Get the Page Count.
     *
     * @return int
     */

    int getPageCount();


    /***********************************************************************************************
     * Get the display ScaleFactor.
     *
     * @return float
     */

    float getScaleFactor();


    /***********************************************************************************************
     * Set the display ScaleFactor.
     *
     * @param scale
     */

    void setScaleFactor(float scale);
    }
