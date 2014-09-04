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


import org.lmn.fc.common.actions.ContextAction;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.misc.Utilities;
import org.lmn.fc.common.utilities.printing.PrintUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.reports.ReportTablePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * UIComponentHelper.
 */

public final class UIComponentHelper implements FrameworkConstants,
                                                FrameworkStrings,
                                                FrameworkMetadata,
                                                FrameworkSingletons
    {
    private static final int MAX_RETRIES = 20;


    /***********************************************************************************************
     * A convenience method to centralise all isShowing() calls.
     *
     * @param component
     *
     * @return boolean
     */

    public static boolean isComponentShowing(final JComponent component)
        {
        // The API semantics for "shown" are different from for "visible".
        // Component.isShowing() means the component is truly visible,
        // whereas Component.isVisible() does not.
        // See: http://forums.sun.com/thread.jspa?messageID=848376

        if (component != null)
            {
            //System.out.println("UIComponent SHOWING=" + component.isShowing());
            return (component.isShowing());
            }
        else
            {
            //System.out.println("UIComponent SHOWING=false");
            return (false);
            }
        }


    /***********************************************************************************************
     * A convenience method to centralise all isShowing() calls.
     *
     * @param uicomponent
     *
     * @return boolean
     */

    public static boolean isUIComponentShowing(final UIComponentPlugin uicomponent)
        {
        // The API semantics for "shown" are different from for "visible".
        // Component.isShowing() means the component is truly visible,
        // whereas Component.isVisible() does not.
        // See: http://forums.sun.com/thread.jspa?messageID=848376

        if ((uicomponent != null)
            && (uicomponent instanceof JComponent))
            {
            //System.out.println("UIComponent SHOWING=" + component.isShowing());
            return (((JComponent)uicomponent).isShowing());
            }
        else
            {
            //System.out.println("UIComponent SHOWING=false");
            return (false);
            }
        }


    /***********************************************************************************************
     * Indicate if the UI of a data tab should be refreshed.
     * Either use refreshdata directly, OR the Instrument must be selected AND the tab must be visible.
     * Check the Instrument and the UIComponent are not NULL.
     *
     * @param refreshdata
     * @param obsinstrument
     * @param visiblecomponent
     *
     * @return boolean
     */

    public static boolean shouldRefresh(final boolean refreshdata,
                                        final ObservatoryInstrumentInterface obsinstrument,
                                        final UIComponentPlugin visiblecomponent)
        {
        boolean boolRefresh;

        boolRefresh = ((obsinstrument != null) && (visiblecomponent != null));

        boolRefresh = boolRefresh
                        && ((refreshdata)
                              || ((ObservatoryUIHelper.isSelectedInstrument(obsinstrument))
                                  && (isUIComponentShowing(visiblecomponent))));
        return (boolRefresh);
        }


    /***********************************************************************************************
     * Listen for clicks on a JTabbedPane being displayed under the host UIComponent
     * of the specified UserObject.
     * Show the appropriate UIComponentPlugin when a Tab is clicked.
     * Update the toolbar buttons for the visible UIComponentPlugin and control any Timers.
     *
     * @param userobject
     * @param hostuicomponent
     * @param tabbedpane
     */

    public static void addTabListener(final UserObjectPlugin userobject,
                                      final UIComponentPlugin hostuicomponent,
                                      final JTabbedPane tabbedpane)
        {
        if ((userobject != null)
            && (userobject.validatePlugin())
            && (hostuicomponent != null)
            && (tabbedpane != null))
            {
            tabbedpane.addMouseListener(new MouseAdapter()
                {
//                public void mouseClicked(final MouseEvent event)
//                    {
//                    System.out.println("TAB CLICK! " + hostuicomponent.getClass().getName());
//                    }

                // Use mouseReleased() because there seems to be an odd state where the release happens,
                // ...but no click!
                public void mouseReleased(final MouseEvent event)
                    {
                    //System.out.println("TAB MOUSE RELEASED");
                    try
                        {
                        //System.out.println("TAB runSelectedTabComponent on " + hostuicomponent.getClass().getName());
                        runSelectedTabComponent(userobject, hostuicomponent, tabbedpane);
                        REGISTRY_MODEL.rebuildNavigation(userobject, hostuicomponent);
                        }

                    catch (final StackOverflowError exception)
                        {
                        LOGGER.error("STACK OVERFLOW IN UIComponentPlugin.addTabListener()");
                        }
                    }
                });
            }
        }


    /***********************************************************************************************
     * Initialise all UIComponentPlugins on the tabs of the specified JTabbedPane.
     *
     * @param tabbedpane
     */

    public static void initialiseAllTabComponents(final JTabbedPane tabbedpane)
        {
        final String SOURCE = "UIComponentHelper.initialiseAllTabComponents() ";

        if (tabbedpane != null)
            {
            for (int intTabIndex = 0;
                 intTabIndex < tabbedpane.getTabCount();
                 intTabIndex++)
                {
                final Component component;

                component = tabbedpane.getComponentAt(intTabIndex);

                if ((component != null)
                    && (component instanceof UIComponentPlugin))
                    {
                    LOGGER.debug(LOADER_PROPERTIES.isMasterDebug(),
                                 SOURCE + component.getClass().getName());

                    ((UIComponentPlugin)component).initialiseUI();
                    }
                }
            }
        }


    /***********************************************************************************************
     * Remove Identity of all UIComponentPlugins on the tabs of the specified JTabbedPane.
     *
     * @param tabbedpane
     */

    public static void removeIdentityOfAllTabComponents(final JTabbedPane tabbedpane)
        {
        final String SOURCE = "UIComponentHelper.removeIdentityOfAllTabComponents() ";

        if (tabbedpane != null)
            {
            for (int intTabIndex = 0;
                 intTabIndex < tabbedpane.getTabCount();
                 intTabIndex++)
                {
                final Component component;

                component = tabbedpane.getComponentAt(intTabIndex);

                if ((component != null)
                    && (component instanceof UIComponentPlugin))
                    {
                    LOGGER.debug(LOADER_PROPERTIES.isMasterDebug(),
                                 SOURCE + component.getClass().getName());

                    ((UIComponentPlugin)component).removeUIIdentity();
                    }
                }
            }
        }


    /***********************************************************************************************
     * Show the UIComponentPlugin on the currently visible Tab of a specified JTabbedPane
     * by calling runUI().
     * Stop UI of all other UIComponentPlugins on invisible Tabs of the JTabbedPane.
     * Display or hide toolbar ContextActions as appropriate.
     *
     * @param userobject
     * @param hostuicomponent
     * @param tabbedpane
     *
     * @return boolean a flag to indicate that the original SelectedComponent is still showing
     */

    public static boolean runSelectedTabComponent(final UserObjectPlugin userobject,
                                                  final UIComponentPlugin hostuicomponent,
                                                  final JTabbedPane tabbedpane)
        {
        Component component;
        Component componentShowing;
        boolean boolValidSelection;
        int intRetries;

        if ((userobject == null)
            || (hostuicomponent == null))
            {
            //System.out.println("UIComponent.runSelectedTabComponent() NULL UI COMPONENTS");
            return (false);
            }

        LOGGER.debugNavigation("UIComponent.runSelectedTabComponent() hostui=" + hostuicomponent.getClass().getName());
        //System.out.println("UIComponent.runSelectedTabComponent() hostui=" + hostuicomponent.getClass().getName());

        // Record the initial Tab selection
        if (tabbedpane != null)
            {
            componentShowing = tabbedpane.getSelectedComponent();
            //System.out.println("runSelectedTabComponent() previous tab [showing=" + componentShowing.isShowing() + "]");
            }
        else
            {
            componentShowing = null;
            }

        boolValidSelection = false;
        intRetries = MAX_RETRIES;

        while ((tabbedpane != null)
            && (!boolValidSelection)
            && (intRetries > 0))
            {
            boolean boolFoundComponent;

            // Firstly work our way through each Tab of the JTabbedPane,
            // stopping all UIComponentPlugins
            try
                {
                stopAllTabComponents(tabbedpane);
                }

            catch (final StackOverflowError exception)
                {
                LOGGER.error("STACK OVERFLOW IN runSelectedTabComponent() 0");
                }

            // Scan for the (single) Tab which is now showing
            // If it contains a UIComponentPlugin, it will rebuild the appropriate toolbar buttons
            // Step through all the Tabs again, to catch any click-ahead changes of selection?!
            boolFoundComponent = false;

            for (int i = 0;
                 (!boolFoundComponent) && (i < tabbedpane.getTabCount());
                 i++)
                {
                try
                    {
                    component = tabbedpane.getComponentAt(i);

                    // Take no action if the Tab is empty (i.e. null)
                    if ((component != null)
                        && (component.equals(tabbedpane.getSelectedComponent())))
                        //&& (component.isShowing()))
                        {
                        // Record the component which is actually showing at this moment...
                        componentShowing = component;
                        //System.out.println("runSelectedTabComponent() selected tab [showing=" + componentShowing.isShowing() + "]");

                        // ...then runUI()
                        runComponentAndTransferActions(component, hostuicomponent);

                        // There should be only one Tab showing, so don't waste time!
                        boolFoundComponent = true;
                        }
                    }

                catch (final StackOverflowError exception)
                    {
                    LOGGER.error("STACK OVERFLOW IN runSelectedTabComponent() 1");
                    }
                }

            // Return a flag to check that the correct Component is still showing
            // in case the visible Tab changed during processing...
//            if (componentShowing != null)
//                {
//                //LOGGER.debug("runSelectedTabComponent() componentShowing at end " + ((UIComponentPlugin)componentShowing).getReportUniqueName());
//                boolValidSelection = (componentShowing.equals(tabbedpane.getSelectedComponent()));
//
//                if (boolValidSelection)
//                    {
//                    //LOGGER.debug("runSelectedTabComponent() valid selection, leave now");
//                    }
//                else
//                    {
//                    //LOGGER.debug("runSelectedTabComponent() invalid selection, try again");
//                    }
//                }
//            else
//                {
//                //LOGGER.debug("null componentShowing at end, keep trying");
//                boolValidSelection = false;
//                }

            boolValidSelection = ((componentShowing != null)
                                    && (componentShowing.equals(tabbedpane.getSelectedComponent())));

            // Was this worth the effort?
            if ((intRetries < MAX_RETRIES)
                && (!boolValidSelection))
                {
                LOGGER.log("UIComponent.runSelectedTabComponent() WARNING! Trying again, Tab retries intRetries=" + intRetries);
                }

            // Try again for a while...
            intRetries--;
            }

        // If we timed out, try to recover the correct selection
        // by setting the visible Component to the one we think it should be...
        // This might waste resources if we end up with two Tabs containing UIComponentPlugins,
        // but they will be recovered on the next Tab change

        if (intRetries == 0)
            {
            try
                {
                LOGGER.debugNavigation("UIComponent.runSelectedTabComponent() timed out...");
                tabbedpane.setSelectedComponent(componentShowing);
                runComponentAndTransferActions(componentShowing, hostuicomponent);

                // TODO REVIEW VISIBLE
                //tabbedpane.getSelectedComponent().setVisible(true);
                }

            catch (final StackOverflowError exception)
                {
                LOGGER.error("STACK OVERFLOW IN runSelectedTabComponent() 2");
                }
            }
        //LOGGER.debug("runSelectedTabComponent() end " + Chronos.getSystemTimeMillis());

        return (boolValidSelection);
        }


    /***********************************************************************************************
     * Stop all UIComponentPlugins on the tabs of the specified JTabbedPane.
     *
     * @param tabbedpane
     */

    public static void stopAllTabComponents(final JTabbedPane tabbedpane)
        {
        if (tabbedpane != null)
            {
            for (int i = 0; i < tabbedpane.getTabCount(); i++)
                {
                final Component component;

                component = tabbedpane.getComponentAt(i);

                if ((component != null)
                    && (component instanceof UIComponentPlugin))
                    {
                    //System.out.println("TAB STOPPING " + component.getClass().getName());
                    ((UIComponentPlugin)component).stopUI();
                    }
                else
                    {
                    //System.out.println("TAB CONTAINS NULL COMPONENT...");
                    }
                }
            }
        }


    /***********************************************************************************************
     * Dispose of all UIComponentPlugins on the tabs of the specified JTabbedPane.
     *
     * @param tabbedpane
     */

    public static void disposeAllTabComponents(final JTabbedPane tabbedpane)
        {
        if (tabbedpane != null)
            {
            for (int i = 0; i < tabbedpane.getTabCount(); i++)
                {
                final Component component;

                component = tabbedpane.getComponentAt(i);

                // Ensure that ALL UIComponentPlugins are disposed of and ContextActions removed
                // We must do all Tabs because click-ahead might cause the selection to change
                // If it is a new Tab, the UIComponentPlugins will be uninitialised
                // if it is the same Tab, we want to leave it alone
                // Take no action if the Tab is empty (i.e. null)

                if ((component != null)
                    && (component instanceof UIComponentPlugin))
                    {
                    // This will rebuild the toolbar buttons by calling rebuildNavigation()
                    // so the next step must rebuild with the Tab which is showing..
                    ((UIComponentPlugin)component).clearUIComponentContextActionGroups();
                    ((UIComponentPlugin)component).disposeUI();

                    //LOGGER.debug(true, "UIComponent.disposeAllTabComponents() TAB DISPOSE " + component.getClass().getName());
                    //LOGGER.debug("runSelectedTabComponent() disposed i=" + i + "  " + ((UIComponentPlugin)component).getReportUniqueName());
                    }
                }
            }
        }


    /***********************************************************************************************
     * Call runUI() on the specified Component, and transfer its ContextActionGroups
     * to the specified UIComponent UI occupant (usually a container for the Component) to ensure
     * that the ContextActions are shown by setUIOccupant().
     *
     * @param componenttorun
     * @param hostuicomponent
     */

    public static void runComponentAndTransferActions(final Component componenttorun,
                                                      final UIComponentPlugin hostuicomponent)
        {
        if ((componenttorun != null)
            && (componenttorun instanceof UIComponentPlugin)
            && (hostuicomponent != null))
            {
            LOGGER.debugNavigation("UIComponent.runComponentAndTransferActions() from "
                                        + componenttorun.getClass().getName()
                                        + " to "
                                        + hostuicomponent.getClass().getName());

            ((UIComponentPlugin) componenttorun).runUI();

            // We must call assembleContextActionGroups() in order to redraw the navigation
            if (((UIComponentPlugin) componenttorun).getUIComponentContextActionGroups() != null)
                {
                if (((UIComponentPlugin) componenttorun).getUIComponentContextActionGroups().isEmpty())
                    {
                    LOGGER.debugNavigation("UIComponent The visible (source) component has no ContextActionGroups to show");
                    }

                // Replace the ContextActionGroups which will be shown later in setUIOccupant
                hostuicomponent.setUIComponentContextActionGroups(((UIComponentPlugin) componenttorun).getUIComponentContextActionGroups());
                }
            }
        else
            {
//            System.out.println("UIComponent.runComponentAndTransferActions() cannot runUI() [torun-null="
//                    + (componenttorun == null) + "] [hostui-null=" + (hostuicomponent == null) + "]");
//
//            System.out.println("COMP TO RUN " + componenttorun.getClass().getName());
            }
        }


    /***********************************************************************************************
     * Get the default PageFormat, A4 LANDSCAPE.
     *
     * @return PageFormat
     */

    public static PageFormat getDefaultPageFormat()
        {
        final PageFormat pageFormat;
        final Paper paper;
        final double dblMargin72ndInch;

        // Doing this in one place makes it easier to make changes
        // Creates a letter sized piece of paper with one inch margins
        pageFormat = new PageFormat();

        paper = pageFormat.getPaper();

        dblMargin72ndInch = Utilities.to_72nd_inch(10);

        paper.setSize(Utilities.to_72nd_inch(210.0), Utilities.to_72nd_inch(297.0));

        // Work out the new ImageableArea
        paper.setImageableArea(dblMargin72ndInch,
                               dblMargin72ndInch,
                               paper.getWidth() - (dblMargin72ndInch * 2),
                               paper.getHeight() - (dblMargin72ndInch * 2));

        // Make sure the changes made to the clone get applied to the PageFormat
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.LANDSCAPE);

        return (pageFormat);
        }


    /***********************************************************************************************
     * Build a new JToolbar and add a List of Components.
     * Never returns NULL.
     *
     * @param components
     *
     * @return JToolBar
     */

    public static JToolBar buildToolbar(final List<Component> components)
        {
        final JToolBar toolbar;

        toolbar = new JToolBar();

        // Build the Toolbar using the Components, if any
        // If the List is empty, we assume the User doesn't want any buttons...
        buildToolbar(toolbar, components);

        return (toolbar);
        }


    /***********************************************************************************************
     * Add a List of Components to the specified JToolBar.
     * Never returns NULL.
     *
     * @param toolbar
     * @param components
     */

    public static void buildToolbar(final JToolBar toolbar,
                                    final List<Component> components)
        {
        // Build the Toolbar using the Components, if any
        // If the List is empty, we assume the User doesn't want any buttons...

        if ((toolbar != null)
            && (components != null)
            && (!components.isEmpty()))
            {
            final Iterator<Component> iterComponents;

            iterComponents = components.iterator();

            while (iterComponents.hasNext())
                {
                final Component component;

                component = iterComponents.next();

                if (component != null)
                    {
                    toolbar.add(component);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Add the Toolbar Print buttons to the specified List of Components.
     * The Print button will print the specified Component.
     *
     * @param components
     * @param uicomponent
     * @param printable
     * @param title
     * @param fontdata
     * @param colourforeground
     * @param colourbackground
     * @param debug
     */

    public static void addToolbarPrintButtons(final List<Component> components,
                                              final UIComponentPlugin uicomponent,
                                              final Component printable,
                                              final String title,
                                              final FontInterface fontdata,
                                              final ColourInterface colourforeground,
                                              final ColourInterface colourbackground,
                                              final boolean debug)
        {
        final ContextAction actionPageSetup;
        final ContextAction actionPrint;
        final JButton buttonPageSetup;
        final JButton buttonPrint;

        //-------------------------------------------------------------------------------------
        // Page Setup

        buttonPageSetup = new JButton();
        buttonPageSetup.setBorderPainted(false);
        buttonPageSetup.setBorder(UIComponentPlugin.BORDER_BUTTON);
        buttonPageSetup.setHideActionText(true);

        actionPageSetup = new ContextAction(ReportTablePlugin.PREFIX_PAGE_SETUP + title,
                                            RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAGE_SETUP),
                                            ReportTablePlugin.PREFIX_PAGE_SETUP + title,
                                            KeyEvent.VK_S,
                                            false,
                                            true)
            {
            static final long serialVersionUID = 6802400471966299436L;
            static final String SOURCE = "ContextAction:PageSetup ";

            public void actionPerformed(final ActionEvent event)
                {
                if (uicomponent != null)
                    {
                    final PrinterJob printerJob;
                    final PageFormat pageFormat;

                    printerJob = PrinterJob.getPrinterJob();
                    pageFormat = printerJob.pageDialog(uicomponent.getPageFormat());

                    if (pageFormat != null)
                        {
                        uicomponent.setPageFormat(pageFormat);
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "UIComponent unexpectedly NULL");
                    }
                }
            };

        buttonPageSetup.setAction(actionPageSetup);
        buttonPageSetup.setToolTipText((String) actionPageSetup.getValue(Action.SHORT_DESCRIPTION));
        buttonPageSetup.setEnabled(true);

        //-------------------------------------------------------------------------------------
        // Printing

        buttonPrint = new JButton();
        buttonPrint.setBorderPainted(false);
        buttonPrint.setBorder(UIComponentPlugin.BORDER_BUTTON);
        buttonPrint.setHideActionText(true);

        actionPrint = new ContextAction(ReportTablePlugin.PREFIX_PRINT + title,
                                        RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PRINT),
                                        ReportTablePlugin.PREFIX_PRINT + title,
                                        KeyEvent.VK_P,
                                        false,
                                        true)
            {
            static final long serialVersionUID = 8346968631811861938L;
            static final String SOURCE = "ContextAction:Print ";

            public void actionPerformed(final ActionEvent event)
                {
                final org.lmn.fc.common.utilities.threads.SwingWorker workerPrinter;

                workerPrinter = new org.lmn.fc.common.utilities.threads.SwingWorker(REGISTRY.getThreadGroup(),
                                                "SwingWorker Printer")
                    {
                    public Object construct()
                        {
                        LOGGER.debug(uicomponent.isDebug(), SOURCE + "SwingWorker construct()");

                        // Let the user know what happened
                        return (printDialog());
                        }


                    // Display updates occur on the Event Dispatching Thread
                    public void finished()
                        {
                        final String[] strSuccess =
                            {
                            "The panel has been printed",
                            UIComponentPlugin.MSG_PRINT_CANCELLED
                            };

                        if ((get() != null)
                            && (get() instanceof Boolean)
                            && ((Boolean) get())
                            && (!isStopping()))
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[0],
                                                          "Print Panel",
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                            }
                        else
                            {
                            JOptionPane.showMessageDialog(null,
                                                          strSuccess[1],
                                                          "Print Panel",
                                                          JOptionPane.INFORMATION_MESSAGE,
                                                          RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                            }
                        }
                    };

                // Start the Print Thread
                workerPrinter.start();
                }


            /*********************************************************************************
             * Show the Print dialog.
             *
             * @return boolean
             */

            private boolean printDialog()
                {
                final boolean boolSuccess;

                // Check to see that we actually have a printer...
                if (PrinterJob.lookupPrintServices().length == 0)
                    {
                    JOptionPane.showMessageDialog(null,
                                                  ReportTablePlugin.MSG_NO_PRINTER,
                                                  ReportTablePlugin.PREFIX_PRINT + title,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_DIALOG_PRINT));
                    boolSuccess = false;
                    }
                else
                    {
                    if (uicomponent != null)
                        {
                        final PageFormat pageFormat;

                        pageFormat = uicomponent.getPageFormat();

                        if (pageFormat != null)
                            {
                            // ToDo Header & Footer MessageFormats
                            boolSuccess = PrintUtilities.printComponent(printable, pageFormat);
                            }
                        else
                            {
                            boolSuccess = false;
                            }
                        }
                    else
                        {
                        boolSuccess = false;
                        }
                    }

                return (boolSuccess);
                }
            };

        buttonPrint.setAction(actionPrint);
        buttonPrint.setToolTipText((String) actionPrint.getValue(Action.SHORT_DESCRIPTION));
        buttonPrint.setEnabled(true);

        // Put it all together
        components.add(buttonPageSetup);
        components.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        components.add(buttonPrint);
        components.add(new JToolBar.Separator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR));
        }


    // ToDo DEFAULT Header & Footer MessageFormats
    // ToDo See: http://java-sl.com/JEditorPanePrinter.html

    }



//        LOGGER.debug("pageFormat after adjustment");
//        LOGGER.debug("Paper width=" + Utilities.to_mm(pageFormat.getPaper().getWidth()));
//        LOGGER.debug("Paper height=" + Utilities.to_mm(pageFormat.getPaper().getHeight()));
//        LOGGER.debug("Paper imageable width=" + Utilities.to_mm(pageFormat.getPaper().getImageableWidth()));
//        LOGGER.debug("Paper imageable height=" + Utilities.to_mm(pageFormat.getPaper().getImageableHeight()));
//        LOGGER.debug("Paper imageable X=" + Utilities.to_mm(pageFormat.getPaper().getImageableX()));
//        LOGGER.debug("Paper imageable Y=" + Utilities.to_mm(pageFormat.getPaper().getImageableY()));
//            LOGGER.debug("Paper width=" + to_mm(pageFormatNew.getPaper().getWidth()));
//            LOGGER.debug("Paper height=" + to_mm(pageFormatNew.getPaper().getHeight()));
//            LOGGER.debug("Paper imageable width=" + to_mm(pageFormatNew.getPaper().getImageableWidth()));
//            LOGGER.debug("Paper imageable height=" + to_mm(pageFormatNew.getPaper().getImageableHeight()));
//            LOGGER.debug("Paper imageable X=" + to_mm(pageFormatNew.getPaper().getImageableX()));
//            LOGGER.debug("Paper imageable Y=" + to_mm(pageFormatNew.getPaper().getImageableY()));

