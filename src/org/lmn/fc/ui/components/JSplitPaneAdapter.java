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

package org.lmn.fc.ui.components;

import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryPlugin;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/***********************************************************************************************
 * Respond to changes in <code>JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY</code>.
 * Update the specified Property Resource in the Registry to show the change.
 *
 * @see <a href="http://www.angelfire.com/tx4/cus/notes/javaxswing.html">http://www.angelfire.com/tx4/cus/notes/javaxswing.html</a>
 */

public final class JSplitPaneAdapter implements PropertyChangeListener,
                                                FrameworkStrings
    {
    private static final Logger LOGGER = Logger.getInstance();

    private JComponent splitPane;
    private JScrollPane scrollPane;
    private RegistryPlugin theRegistry;
    private String strResourceKey;

    private int intOldLocationPixels;


    /***********************************************************************************************
     * Construct a JSplitPaneAdapter.
     *
     * @param splitscreen
     * @param scrollpane
     * @param registry
     * @param resourcekey
     */

    public JSplitPaneAdapter(final JComponent splitscreen,
                             final JScrollPane scrollpane,
                             final RegistryPlugin registry,
                             final String resourcekey)
        {
        if ((splitscreen == null)
            || (scrollpane == null)
            || (registry == null)
            || (!registry.validateBeanPool())
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey.trim())))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.splitPane = splitscreen;
        this.scrollPane = scrollpane;
        this.theRegistry = registry;
        this.strResourceKey = resourcekey;

        this.intOldLocationPixels = 0;
        }


    /***********************************************************************************************
     * Respond to changes in properties of the JSplitPane.
     *
     * @param event
     */

    public void propertyChange(final PropertyChangeEvent event)
        {
        if ((this.splitPane != null)
            && (this.scrollPane != null)
            && (this.theRegistry != null)
            && (event != null)
            && (JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY.equals(event.getPropertyName()))
            && (event.getOldValue() != null)
            && (!event.getOldValue().equals(event.getNewValue())))
            {
            final int intNewLocationPixels;
            final double dblNewLocationPercent;

            // Save the pixel value of the new divider location
            intNewLocationPixels = ((JSplitPane) splitPane).getDividerLocation();

            //System.out.println("JSplitPaneAdapter PROPERTY CHANGE EVENT new loc=" + intNewLocationPixels);

            // Remember that the divider has already been moved, but we need to
            // update the preferred size of the navigation scroll pane to keep it in position...
            // There seems to be an out-by-one error, so correct it...
            scrollPane.setPreferredSize(new Dimension(intNewLocationPixels-1, 1024));

            // Recalculate the new position as a percentage, for storage in the RegistryModel
            if (splitPane.getWidth() >= 0)
                {
                dblNewLocationPercent = ((double) ((JSplitPane) splitPane).getDividerLocation() * 100.0)
                                            / (double) (splitPane.getWidth());
                }
            else
                {
                dblNewLocationPercent = 5.0;
                }

            // Has it really moved?
            if ((dblNewLocationPercent >= 0.0)
                && ((dblNewLocationPercent <= 100.0))
                && (intNewLocationPixels != intOldLocationPixels))
                {
                intOldLocationPixels = intNewLocationPixels;

                // Save the change back in the Registry, using the ResourceKey provided
                //System.out.println("JSplitPaneAdapter ADAPTER SET " + strResourceKey + "=" + dblNewLocationPercent);
                theRegistry.setDoubleProperty(strResourceKey, dblNewLocationPercent);

                LOGGER.debugNavigation("JSplitPaneAdapter.propertyChange() divider="
                                        + dblNewLocationPercent + "%  "
                                        + intNewLocationPixels + "pixels");
                }
//            else
//                {
//                System.out.println("JSplitPaneAdapter DIVIDER DID NOT MOVE! new=" + intNewLocationPixels + " old=" + intOldLocationPixels + " %=" + dblNewLocationPercent);
//                }
            }
        }


    /***********************************************************************************************
     * Get the updated DividerLocation.
     *
     * @return double
     */

    public double getDividerLocation()
        {
        return (this.intOldLocationPixels);
        }
    }

