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

package org.lmn.fc.frameworks.starbase.ui.manager;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.datatypes.types.FontDataType;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.ExpanderInterface;
import org.lmn.fc.model.plugins.impl.NullData;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.tasks.TaskState;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.*;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.manager.FrameworkManagerUIComponentPlugin;
import org.lmn.fc.ui.status.StatusIndicatorInterface;
import org.lmn.fc.ui.status.StatusIndicatorKey;
import org.lmn.fc.ui.status.impl.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;


/***************************************************************************************************
 * The FrameworkManagerPanel.
 */

public final class FrameworkManagerPanel extends UIComponent
                                         implements FrameworkManagerUIComponentPlugin
    {
    private static final String TOOLTIP_STATUS = "The Status of the selected item";
    private static final String TOOLTIP_DATE = "Date";
    private static final String TOOLTIP_TIME = "Time";
    private static final String TOOLTIP_TIMEZONE = "TimeZone";
    private static final String TOOLTIP_LONGITUDE = "Longitude";
    private static final String TOOLTIP_LATITUDE = "Latitude";
    private static final String TOOLTIP_HASL = "Height Above Sea Level";
    private static final String TOOLTIP_USER = "The logged-in User";
    private static final String TOOLTIP_LOCALE = "The current Language and Country";
    private static final String TOOLTIP_REGISTRY = "The number of Beans (Registry, User)";
    private static final String TOOLTIP_DATASTORE = "The Framework DataStore";
    private static final String TOOLTIP_MEMORY = "Memory usage";
    private static final String STATUS_NOT_IMPLEMENTED = "This function is not yet implemented";
    private static final String UPDATED_FLAG = "*";
    private static final String DEFAULT_FM_FONT = "font=Dialog style=Plain size=11";
    private static final String DEFAULT_FM_COLOUR = "r=0 g=0 b=200";

    private static final double DEFAULT_DIVIDER_LOCATION = 50.0;

    private TaskPlugin pluginHost;
    private String strResourceKey;

    private UIComponentPlugin fullScreen;          // The root component used for UI in full-screen mode
    private JComponent splitPane;                  // The root component used for UI in split-screen mode
    private UserObjectPlugin uiOccupant;           // The UserObjectPlugin occupant of the root component

    private JTree treeManager;                     // The JTree used for navigation in split-screen mode
    private DefaultTreeModel treeModel;
    private TreeCellRenderer treeCellRenderer;
    private DefaultMutableTreeNode treenodeSelected;
    private JScrollPane scrollpaneTree;
    private JSplitPaneAdapter adapter;

    // The Status Indicators at the bottom of the frame
    private JComponent statusBar;
    private EnumMap<StatusIndicatorKey, StatusIndicatorInterface> mapIndicators;

    // Resources
    private FontInterface fontStatusBar;         // Font used on the status bar
    private ColourInterface colourStatusBar;     // Colour used for status bar text
    private double dblDividerLocation;      // The position of the divider as a percentage of width
    private boolean boolCustomIcons;        // Controls customs icons in the JTree
    private boolean boolFullScreen;         // true if running full screen


    /***********************************************************************************************
     * Construct a FrameworkManagerPanel.
     *
     * @param plugin
     * @param resourcekey
     */

    public FrameworkManagerPanel(final TaskPlugin plugin,
                                 final String resourcekey)
        {
        super();

        if ((plugin == null)
            || (!plugin.validatePlugin())
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        this.pluginHost = plugin;
        this.strResourceKey = resourcekey;

        // Initialise the individual UI components which are assembled into the UIComponent
        setFullScreenComponent(null);
        setSplitScreenComponent(null);
        setNavigationTree(null);

        // Prepare an empty StatusBar and somewhere to hold StatusIndicators
        setStatusBar(null);

        // This map is automatically the same size as the Enum
        mapIndicators = new EnumMap<StatusIndicatorKey, StatusIndicatorInterface>(StatusIndicatorKey.class);

        // Initialise the Resources to some defaults
        fontStatusBar = new FontDataType(DEFAULT_FM_FONT);
        colourStatusBar = new ColourDataType(DEFAULT_FM_COLOUR);
        dblDividerLocation = DEFAULT_DIVIDER_LOCATION;
        boolCustomIcons = false;

        // The initial screen mode
        boolFullScreen = false;
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        // Get the latest Resources
        readResources();

        // Create all screen components, regardless of screen mode,
        // but don't connect them together just yet
        createComponents();

        // Create a Status bar
        createStatusBar();
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin.
     */
    @Override

    public final void runUI()
        {
        final Collection<StatusIndicatorInterface> indicators;
        final Iterator iterIndicators;

        if ((REGISTRY_MODEL.getUserInterface() != null)
            && (REGISTRY_MODEL.getUserInterface().getUI() != null)
            && (REGISTRY_MODEL.getUserInterface().getUI().getContainer() != null))
            {
            // Get the latest Resources
            readResources();

            // Install the FrameworkManager into the ContentPane of the JFrame provided by the UserInterface...
            // The UI of the FrameworkManager is the UIComponent
            // Remember to remove the BlankUIComponent from the UserInterface!
            REGISTRY_MODEL.getUserInterface().getUI().getContainer().removeAll();
            REGISTRY_MODEL.getUserInterface().getUI().getContainer().add(this, BorderLayout.CENTER);

            // See if this User expects to start in FullScreen mode
            setFullScreen(REGISTRY_MODEL.getLoggedInUser().getRole().isFullScreen());

            // Initialise the selected UI occupant to that rendered by the Framework root
            // since we will always start by having the tree root node selected
            // This needs to know the Screen Mode
            setUIOccupant(REGISTRY.getFramework());

            // Set the initial Tree selection to the Framework root node
            // The Tree selection must stay in step, even though we can't see it in full-screen mode
            // so that it re-appears correctly in split-screen mode
            setSelectedTreeNode(getUIOccupant().getHostTreeNode());

            // Run all StatusIndicators
            LOGGER.debugNavigation("Run all StatusIndicators");
            indicators = getStatusIndicators().values();
            iterIndicators = indicators.iterator();

            while (iterIndicators.hasNext())
                {
                final StatusIndicatorInterface indicator;

                indicator = (StatusIndicatorInterface) iterIndicators.next();
                indicator.runUI();
                }

            // Say hello to the User - this must appear after setUIOccupant()
            // Use the Framework's setResponseStatus() in order to get the correct Icon
            REGISTRY.getFramework().setStatus(MSG_WELCOME + SPACE + REGISTRY.getFramework().getName());

            // Set the SplitPane divider location as a percentage of the screen width,
            // from data saved in the RegistryModel the last time we were here
            // Reset the position of the divider, but not until we are visible
            setAndSaveDividerLocation(dblDividerLocation);

            // Start up with the navigation tree hidden...
            //setAndSaveDividerLocation(0.0);

            // Update all components of the UI
            validateAndUpdateUI();
            }
        else
            {
            LOGGER.error("Unable to run the Framework Manager because the UserInterface is not available");
            }
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     */

    public final void stopUI()
        {
        final Collection<StatusIndicatorInterface> indicators;
        final Iterator iterIndicators;

        // Stop all StatusIndicators
        indicators = getStatusIndicators().values();
        iterIndicators = indicators.iterator();

        while (iterIndicators.hasNext())
            {
            final StatusIndicatorInterface indicator;

            indicator = (StatusIndicatorInterface) iterIndicators.next();
            indicator.stopUI();
            }

        // Remove the full screen
        setFullScreenComponent(null);

        // Remove the Split Pane
        setSplitScreenComponent(null);

        // Remove the navigation tree
        setNavigationTree(null);
        setSelectedTreeNode(null);

        // Remove the StatusIndicatorKey and all StatusIndicators
        disposeStatusBar(getStatusBar(), getStatusIndicators());
        }


    /***********************************************************************************************
     * Create and set all UI components for both full-screen and split-screen modes.
     * Calls setUIComponent(), setUIPanel(), setFullScreenComponent()
     * setMetadataTree().
     */

    private void createComponents()
        {
        // Create the JTree for the split-screen mode, and register it
        setNavigationTree(createNavigationTree());
        ToolTipManager.sharedInstance().registerComponent(getNavigationTree());

        // Allow customized node icons if enabled
        // Do this every time the panel is requested, to ensure state is up to date
        resetCellRenderer(getNavigationTree());

        // Now set up the TreeModel to allow examination of the data
        // and make sure that the tree uses only DefaultMutableTreeNodes
        setTreeModel(new DefaultTreeModel(REGISTRY_MODEL.getRootNode()));
        getTreeModel().setAsksAllowsChildren(true);
        getNavigationTree().setModel(getTreeModel());

        // Make a scrolling pane for the tree, because it will get large
        scrollpaneTree = new JScrollPane(getNavigationTree());
        scrollpaneTree.setBackground(Color.WHITE);

        // Set size limits for the split
        //getScrollPaneTree().setMinimumSize(DIM_TREE_SIZE_MIN);
        // TODO REVIEW NOW
        getScrollPaneTree().setMinimumSize(new Dimension(200, 150));
        getScrollPaneTree().setPreferredSize(new Dimension(200, 150));


//        // build buttonpanel
//JPanel panel = new JPanel(new FlowLayout());
//panel.add(new JButton("->"));
//panel.add(new JButton("<-"));
//
//// get the divider
//BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
//
//// set layout or the "DividerLayout" will be used
//divider.setLayout(new FlowLayout());
//
//divider.add(panel);
//
//// set the divider size to the width of the panel
//divider.setDividerSize(panel.getPreferredSize().width);

        UIComponentPlugin blankUI = new BlankUIComponent();

        blankUI.setMinimumSize(new Dimension(200, 150));
        //blankUI.setPreferredSize(new Dimension(200, 150));


        // Add the scrolling pane & a blank panel to the split pane, and initialise
        final JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                     getScrollPaneTree(),
                                                     (Component)blankUI);
//        {
//        public void repaint()
//            {
//            //System.out.println("REPAINT SPLIT PANE! loc=" + getDividerLocation());
//            super.repaint();
//
//            //System.out.println("After repaint() loc=" + getDividerLocation());
//
//            }
//        };
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setContinuousLayout(false);
        jSplitPane.setDividerSize(SIZE_SPLIT_PANE_DIVIDER);
        // TODO REVIEW NOW
        jSplitPane.setResizeWeight(0.0);
        setSplitScreenComponent(jSplitPane);

        // Listen for changes in the Split Pane divider location
        adapter = new JSplitPaneAdapter(getSplitScreenComponent(),
                                        getScrollPaneTree(),
                                        REGISTRY,
                                        getResourceKey() + KEY_DIMENSION_DIVIDER_LOCATION);
        getSplitScreenComponent().addPropertyChangeListener(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY,
                                                            getAdapter());

        //------------------------------------------------------------------------------------------
        // Set up for selections from the Split Pane tree

        // Only one Node at a time may be selected
        getNavigationTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Listen for Mouse events
        final MouseListener listenerMouse = new MouseAdapter()
            {
            public void mousePressed(final MouseEvent mouseEvent)
                {
                final DefaultMutableTreeNode treenodeSelected;
                final int rowClicked;

                // Returns -1 if the location is not within the bounds of a displayed cell
                rowClicked = getNavigationTree().getRowForLocation(mouseEvent.getX(),
                                                                   mouseEvent.getY());

                if (rowClicked != -1)
                    {
                    final TreePath treePath = getNavigationTree().getPathForRow(rowClicked);
                    treenodeSelected = (DefaultMutableTreeNode) treePath.getLastPathComponent();

                    LOGGER.debugNavigation("NavigationTree potential click....");
                    if (SwingUtilities.isLeftMouseButton(mouseEvent)
                            && (!mouseEvent.isShiftDown())
                            && (!mouseEvent.isAltDown())
                            && (mouseEvent.getClickCount() == 1)
                            && (treenodeSelected != null))
                        {
                        // Make the new selection
                        treeSelection(mouseEvent,
                                      treenodeSelected,
                                      getNavigationTree().getRowBounds(rowClicked));
                        }
                    }
                }
            };

        getNavigationTree().addMouseListener(listenerMouse);

        //------------------------------------------------------------------------------------------
        // Create the panel used for display in full-screen mode, and register it

        //setFullScreenComponent(new BlankUIComponent("FULL SCREEN MODE"));
        setFullScreenComponent(new PlainUIComponent());
        ToolTipManager.sharedInstance().registerComponent((JComponent)getFullScreenComponent());
        }


    /***********************************************************************************************
     * Create the Navigation JTree.
     * This does not assume any specific screen layout mode.
     *
     * @return JTree
     */

    private JTree createNavigationTree()
        {
        final JTree treeNavigation;

        treeNavigation = new JTree(REGISTRY_MODEL.getRootNode())
            {
            // Override the JTree.getToolTipText(), to give separate text for each Node
            public String getToolTipText(final MouseEvent mouseEvent)
                {
                // Returns -1 if any of the elements in path are hidden under a collapsed parent
                if (getRowForLocation(mouseEvent.getX(), mouseEvent.getY()) == -1)
                    {
                    return (null);
                    }
                else
                    {
                    // There's a valid Node under the mouse
                    final TreePath curPath = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

                    // Get the last Node of the path
                    final DefaultMutableTreeNode lastPathComponent;
                    lastPathComponent = (DefaultMutableTreeNode) curPath.getLastPathComponent();

                    // Recover ToolTip text from the UserObjects
                    final Object userObject = lastPathComponent.getUserObject();
                    String strUpdated = EMPTY_STRING;
                    final String strStatus;

                    if (userObject instanceof AtomPlugin)
                        {
                        final AtomPlugin pluginAtom = (AtomPlugin) lastPathComponent.getUserObject();

                        if (pluginAtom.isUpdated())
                            {
                            strUpdated = UPDATED_FLAG;
                            }
                        return (strUpdated + pluginAtom.getDescription());
                        }
                    else if (userObject instanceof TaskPlugin)
                        {
                        final TaskPlugin pluginTask = (TaskPlugin) lastPathComponent.getUserObject();

                        if (pluginTask.isUpdated())
                            {
                            strUpdated = UPDATED_FLAG;
                            }

                        if (!pluginTask.isPublic())
                            {
                            strStatus = SPACE + PRIVATE_TASK;
                            }
                        else
                            {
                            // It is a public Task, so it must have a status
                            strStatus = " [" + pluginTask.getState().getStatus() + ']';
                            }

                        return (strUpdated + pluginTask.getDescription() + strStatus);
                        }
                    else if (userObject instanceof ResourcePlugin)
                        {
                        final ResourcePlugin pluginResource = (ResourcePlugin) lastPathComponent.getUserObject();

                        if (pluginResource.isUpdated())
                            {
                            strUpdated = UPDATED_FLAG;
                            }

                        return (strUpdated + pluginResource.getDescription());
                        }
                    else if (userObject instanceof UserObjectPlugin)
                        {
                        final UserObjectPlugin plugin = (UserObjectPlugin) lastPathComponent.getUserObject();

                        if (plugin.isUpdated())
                            {
                            strUpdated = UPDATED_FLAG;
                            }

                        return (strUpdated + plugin.getDescription());
                        }
                    else
                        {
                        // TOdo exception
                        LOGGER.debugNavigation("FAILURE IN TOOLTIP ON TREE");
                        throw new FrameworkException(REGISTRY.getException("TODO_MODEL_TOOLTIP"));
                        }
                    }
                }
            };

        treeNavigation.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Always start at the root, which must be on row 0
        treeNavigation.scrollRowToVisible(0);
        treeNavigation.setSelectionPath(treeNavigation.getPathForRow(0));

        return (treeNavigation);
        }


    /***********************************************************************************************
     * Create the StatusBar from a selection of StatusIndicators.
     */

    private void createStatusBar()
        {
        final Collection<StatusIndicatorInterface> indicators;
        final Iterator<StatusIndicatorInterface> iterIndicators;

        setStatusBar(new JPanel());
        getStatusBar().setLayout(new BoxLayoutFixed(getStatusBar(), BoxLayoutFixed.X_AXIS));
        getStatusBar().setMinimumSize(StatusIndicatorInterface.DIM_STATUS_MIN);
        getStatusBar().setPreferredSize(StatusIndicatorInterface.DIM_STATUS_PREF);
        getStatusBar().setMaximumSize(StatusIndicatorInterface.DIM_STATUS_PREF);
        getStatusBar().setBorder(BorderFactory.createEtchedBorder());

        // Create some StatusIndicators
        clearStatusIndicators(getStatusIndicators());

        addStatusIndicator(StatusIndicatorKey.STATUS,
                           new StatusText(REGISTRY.getFramework(),
                                          colourStatusBar,
                                          fontStatusBar,
                                          MSG_WELCOME + SPACE + REGISTRY.getFramework().getName(),
                                          TOOLTIP_STATUS));
        addStatusIndicator(StatusIndicatorKey.DATE,
                           new StatusDate(REGISTRY.getFramework(),
                                          colourStatusBar,
                                          fontStatusBar,
                                          "0000-00-00",
                                          TOOLTIP_DATE));
        addStatusIndicator(StatusIndicatorKey.TIME,
                           new StatusTime(REGISTRY.getFramework(),
                                          colourStatusBar,
                                          fontStatusBar,
                                          "00:00:00",
                                          TOOLTIP_TIME));
        addStatusIndicator(StatusIndicatorKey.TIMEZONE,
                           new StatusTimeZone(REGISTRY.getFramework(),
                                              colourStatusBar,
                                              fontStatusBar,
                                              "GMT",
                                              TOOLTIP_TIMEZONE));
        addStatusIndicator(StatusIndicatorKey.LONGITUDE,
                           new StatusLongitude(REGISTRY.getFramework(),
                                               colourStatusBar,
                                               fontStatusBar,
                                               "00:00:00",
                                               TOOLTIP_LONGITUDE));
        addStatusIndicator(StatusIndicatorKey.LATITUDE,
                           new StatusLatitude(REGISTRY.getFramework(),
                                              colourStatusBar,
                                              fontStatusBar,
                                              "00:00:00",
                                              TOOLTIP_LATITUDE));
        addStatusIndicator(StatusIndicatorKey.HASL,
                           new StatusHASL(REGISTRY.getFramework(),
                                          colourStatusBar,
                                          fontStatusBar,
                                          "0.0",
                                          TOOLTIP_HASL));
        addStatusIndicator(StatusIndicatorKey.USER,
                           new StatusUser(colourStatusBar,
                                          fontStatusBar,
                                          EMPTY_STRING,
                                          TOOLTIP_USER
                                              + SPACE
                                              + LEFT_PARENTHESIS
                                              + REGISTRY_MODEL.getLoggedInUser().getRole().getName()
                                              + SPACE
                                              + ROLE
                                              + RIGHT_PARENTHESIS));
        addStatusIndicator(StatusIndicatorKey.LOCALE,
                           new StatusLocale(colourStatusBar,
                                            fontStatusBar,
                                            EMPTY_STRING,
                                            TOOLTIP_LOCALE));
        addStatusIndicator(StatusIndicatorKey.REGISTRY,
                           new StatusRegistry(colourStatusBar,
                                              fontStatusBar,
                                              EMPTY_STRING,
                                              TOOLTIP_REGISTRY));
        addStatusIndicator(StatusIndicatorKey.DATASTORE,
                           new StatusDataStore(colourStatusBar,
                                               fontStatusBar,
                                               EMPTY_STRING,
                                               TOOLTIP_DATASTORE));
        addStatusIndicator(StatusIndicatorKey.MEMORY,
                           new StatusMemory(UIComponentPlugin.DEFAULT_COLOUR_CANVAS,
                                            fontStatusBar,
                                            "0%",
                                            TOOLTIP_MEMORY));
        addStatusIndicator(StatusIndicatorKey.GARBAGE,
                           new StatusGarbage(colourStatusBar,
                                             fontStatusBar,
                                             EMPTY_STRING,
                                             TOOLTIP_ACTION_GC));

        rebuildStatusBar(getStatusBar(), getStatusIndicators());

        // Initialise all StatusIndicators
        indicators = getStatusIndicators().values();
        iterIndicators = indicators.iterator();

        while (iterIndicators.hasNext())
            {
            final StatusIndicatorInterface indicator;

            indicator = iterIndicators.next();
            indicator.initialiseUI();
            }
        }


    /**********************************************************************************************/
    /* Atom, Task and Resource Selection                                                          */
    /***********************************************************************************************
     * Process the Tree selection.
     * The selection must be kept in step in full-screen and split-screen modes.
     *
     * @param event The MouseEvent causing the selection
     * @param treenode The tree node being selected
     * @param rectangle The selected area
     */

    private void treeSelection(final MouseEvent event,
                               final DefaultMutableTreeNode treenode,
                               final Rectangle rectangle)
        {
        LOGGER.debugNavigation("treeSelection() NAVIGATION TREE CLICK!");

        // Update the FrameworkManager resources on every click...
        readResources();

        // Remove any status message immediately, while we work out what to do...
        // Use the Framework's setResponseStatus() in order to get the correct Icon
        REGISTRY.getFramework().setStatus("<html><i>Please make a selection</i></html>");

        try
            {
            if ((treenode != null)
                && (getSelectedTreeNode() != null))
                {
                final Object objectSelected = treenode.getUserObject();

                if ((objectSelected != null)
                    && (objectSelected instanceof UserObjectPlugin))
                    {
                    // Are we trying to recurse into ourselves?!
                    // The FrameworkManager can exist only if there is also a UserInterface installed
                    // so we must check that the User is not trying to select
                    // the Framework, FrameworkManager or UserInterface
                    // TODO review selection of Framework itself
                    if ((getHostTask().equals(objectSelected))
                        || (REGISTRY_MODEL.getFramework().equals(objectSelected))
                        || (REGISTRY_MODEL.getFrameworkManager().equals(objectSelected))
                        || (REGISTRY_MODEL.getUserInterface().equals(objectSelected)))
                        {
                        // We can go no further, so borrow the failure code in executeOrBlockRecursion()
                        NavigationUtilities.executeOrBlockRecursion((UserObjectPlugin)objectSelected,
                                                                    event,
                                                                    ((UserObjectPlugin)objectSelected).getBrowseMode());
                        }
                    else
                        {
                        // Handle any subclasses of UserObjectPlugin, including possible later enhancements?
                        // Retrieve the selected object
                        final UserObjectPlugin pluginUserObject = (UserObjectPlugin) objectSelected;

                        // Remind the user of the current selection until actionPerformed()
                        ((UserObjectPlugin)objectSelected).setCaption(pluginUserObject.getPathname());

                        // Create a new JPopupMenu every time
                        final JPopupMenu popupMenu = new JPopupMenu();

                        // ToDo find a better way to do this!
                        if (pluginUserObject instanceof ExpanderInterface)
                            {
                            LOGGER.debugNavigation("Expander");
                            notImplemented(pluginUserObject, objectSelected, treenode);
                            }
                        else if (pluginUserObject instanceof AtomPlugin)
                            {
                            atomSelected(pluginUserObject, event, rectangle, popupMenu);
                            }
                        else if (pluginUserObject instanceof TaskPlugin)
                            {
                            taskSelected(pluginUserObject, event, rectangle, popupMenu);
                            }
                        else if (pluginUserObject instanceof ResourcePlugin)
                            {
                            resourceSelected(pluginUserObject, event, rectangle, popupMenu);
                            }
                        else if (pluginUserObject instanceof UserPlugin)
                            {
                            LOGGER.debugNavigation("USER");
                            notImplemented(pluginUserObject, objectSelected, treenode);
                            }
                        else if (pluginUserObject instanceof RolePlugin)
                            {
                            LOGGER.debugNavigation("ROLE");
                            notImplemented(pluginUserObject, objectSelected, treenode);
                            }
                        else if (pluginUserObject instanceof CountryPlugin)
                            {
                            LOGGER.debugNavigation("COUNTRY");
                            notImplemented(pluginUserObject, objectSelected, treenode);
                            }
                        else if (pluginUserObject instanceof LanguagePlugin)
                            {
                            LOGGER.debugNavigation("LANGUAGE");
                            notImplemented(pluginUserObject, objectSelected, treenode);
                            }
                        else if (pluginUserObject instanceof LookAndFeelPlugin)
                            {
                            LOGGER.debugNavigation("L&F");
                            notImplemented(pluginUserObject, objectSelected, treenode);
                            }
                        else if (pluginUserObject instanceof RootPlugin)
                            {
                            LOGGER.debugNavigation("pluginUserObject instanceof RootPlugin???");

                            ((UserObjectPlugin)objectSelected).setStatus(pluginUserObject.getPathname());
                            // Remove the previous occupant of the UI Panel
                            clearUIOccupant(pluginUserObject);

                            // Use a blank panel which shows the node name in the caption
                            setUIOccupant(new NullData());

                            // Record the new selection
                            getNavigationTree().setSelectionPath(new TreePath(treenode.getPath()));
                            setSelectedTreeNode(treenode);
                            }
                        else
                            {
                            // The UserObjectPlugin is not of the correct type...
                            LOGGER.debugNavigation(".treeSelection() Unrecognised subclass of UserObjectPlugin");
                            // ToDO exception
                            throw new FrameworkException(REGISTRY.getException("TODO_MODEL_CORRUPT"));
                            }
                        }
                    }
                else
                    {
                    LOGGER.debugNavigation(".treeSelection() can't read a UserObjectPlugin");
                    // ToDO exception
                    throw new FrameworkException(REGISTRY.getException("TODO_MODEL_CORRUPT"));
                    }
                }
            else
                {
                LOGGER.debugNavigation(".treeSelection() treenode is null");
                // ToDO exception
                throw new FrameworkException(REGISTRY.getException("TODO_MODEL_CORRUPT"));
                }
            }

        catch (ClassCastException exception)
            {
            // ToDO exception
            throw new FrameworkException(REGISTRY.getException("TODO_CLASS_CAST"), exception);
            }

        catch (FrameworkException exception)
            {
            // ToDO exception
            throw new FrameworkException(REGISTRY.getException("RegistryModel.Get.UserObjectPlugin"), exception);
            }
        }


    /***********************************************************************************************
     * An Atom is selected.
     * Execute immediately, unless the Control key is down, in which case provide a Menu.
     *
     * @param userobject
     * @param event
     * @param rectangle
     * @param menu
     */

    private void atomSelected(final UserObjectPlugin userobject,
                              final MouseEvent event,
                              final Rectangle rectangle,
                              final JPopupMenu menu)
        {
        JMenuItem menuItem;

        LOGGER.debugNavigation("Atom selected");
        if (!event.isControlDown())
            {
            LOGGER.debugNavigation("Control NOT down!");

            // Is the Atom already visible?
            if ((!getUIOccupant().equals(userobject))
                && userobject.isActive())
                {
                LOGGER.debugNavigation("Atom made visible");
                // This calls RootData.runUI()
                NavigationUtilities.executeOrBlockRecursion(userobject,
                                                            event,
                                                            MODE_BROWSE);
                }
            }
        else
            {
            // Provide a Popup menu
            // Just Execute this Atom immediately, unless it's already there
            menuItem = new JMenuItem("Execute Atom");

            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    // This calls RootData.runUI()
                    NavigationUtilities.executeOrBlockRecursion(userobject,
                                                                event,
                                                                MODE_BROWSE);
                    }
                });

            // Modify the menu item to suit the Atom state
            // Is the Atom active?
            if (userobject.isActive())
                {
                // The Atom may already be visible
                // So check if this is a click on the same Atom as before
                if (getUIOccupant().equals(userobject))
                    {
                    // If visible, do nothing
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("This Atom is already visible");
                    }
                else
                    {
                    // Otherwise, allow execution
                    menuItem.setEnabled(true);
                    menuItem.setToolTipText("Execute this Atom now");
                    }
                }
            else
                {
                menuItem.setEnabled(false);
                menuItem.setToolTipText("The Atom is inactive");
                }

            menu.add(menuItem);

            //--------------------------------------------------------------------------------------
            // Prepare the Atom Editor menu

            menuItem = new JMenuItem("Configure");

            // Is there an editor for this Atom?
            if (((userobject.getEditorClassname() != null)
                && (!EMPTY_STRING.equals(userobject.getEditorClassname().trim()))))
                {
                menuItem.addActionListener(new ActionListener()
                  {
                  public void actionPerformed(final ActionEvent event)
                      {
                      // Run the Editor
                      NavigationUtilities.executeOrBlockRecursion(userobject,
                                                                  event,
                                                                  MODE_EDIT);
                      }
                  });

                // Check if this is a click on the same Atom Editor as before
                if ((getUIOccupant().equals(userobject))
                    && (getUIOccupant() instanceof EditorUIComponent))
                    {
                    // If visible, do nothing
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("The Atom Editor is already visible");
                    }
                else
                    {
                    // Otherwise, allow edits
                    menuItem.setEnabled(true);
                    menuItem.setToolTipText("Edit this Atom");
                    }
                }
            else
                {
                // There is no Editor installed...
                menuItem.setEnabled(false);
                menuItem.setToolTipText("There is no Editor installed");
                clearUIOccupant(new NullData());
                setUIOccupant(new NullData());
                }

            // Position the menu relative to the selected node
            menu.add(menuItem);
            NavigationUtilities.positionPopupMenu(menu,
                                                  event,
                                                  rectangle,
                                                  ((JSplitPane) getSplitScreenComponent()).getDividerLocation());

            // Is it a new selection?
            // If so, show a blank panel until user has made a decision using the Popup menu
            if (!getUIOccupant().equals(userobject))
                {
                clearUIOccupant(new NullData());
                setUIOccupant(new NullData());
                }
            }
        }


    /***********************************************************************************************
     * A Task is selected.
     * Execute immediately, unless the Control key is down, in which case provide a Menu.
     *
     * @param userobject
     * @param event
     * @param rectangle
     * @param popupMenu
     */

    private void taskSelected(final UserObjectPlugin userobject,
                              final MouseEvent event,
                              final Rectangle rectangle,
                              final JPopupMenu popupMenu)
        {
        // Firstly, decide what to do with the UI's current occupant...
        // Running runnable Tasks will show their UI
        // Editors will be replaced by their parent Task's running runnable UI
        // or a blank panel if the Task is not running
        // Otherwise show a blank panel

        if (((TaskPlugin)userobject).isRunnable())
            {
            if (TaskState.RUNNING.equals(((TaskPlugin)userobject).getState()))
                {
                // If the new selection is a runnable Task, and it is running, show its Component
                // Check to see if the UI is already visible
                if (getUIOccupant().equals(userobject))
                    {
                    // Now check to see if the Editor is visible, or the Task UI
                    // If it is the Editor, it must be replaced by the Task UI
                    if (userobject.getBrowseMode())
                        {
                        LOGGER.debugNavigation(".treeSelection() RUNNING Runnable already visible, do nothing");
                        }
                    else
                        {
                        // Must be in Edit mode, so replace with running panel
                        LOGGER.debugNavigation(".treeSelection() RUNNING Runnable Edit mode, so replace with running panel");
                        userobject.setBrowseMode(MODE_BROWSE);
                        setUIOccupant(userobject);
                        }
                    }
                else
                    {
                    LOGGER.debugNavigation(".treeSelection() RUNNING Runnable show new Component");
                    // Remove the previous occupant of the UI Panel
                    clearUIOccupant(userobject);

                    // ...and show the new one
                    userobject.setBrowseMode(MODE_BROWSE);
                    setUIOccupant(userobject);
                    }
                }
            else
                {
                // If not already running, just show a blank panel until the user has made a selection
                LOGGER.debugNavigation(".treeSelection() NOT RUNNING Runnable show Blank panel until the user has made a selection");
                clearUIOccupant(new NullData());
                setUIOccupant(new NullData());
                }
            }
        else
            {
            // The Task is not runnable, but check to see if the UI is already visible
            if (getUIOccupant().equals(userobject))
                {
                // Now check to see if the Editor is visible, or the Task UI
                // If it is the Editor, it must be replaced by a blank panel
                // until the user chooses to run the Task again
                if (userobject.getBrowseMode())
                    {
                    LOGGER.debugNavigation(".treeSelection() NOT Runnable already visible, do nothing");
                    }
                else
                    {
                    // Must be in Edit mode, so replace with a blank panel
//                  LOGGER.debugNavigation(".treeSelection() NOT Runnable Edit mode, so replace with blank panel");
                    clearUIOccupant(new NullData());
                    setUIOccupant(new NullData());
                    }
                }
            else
                {
                // If not already running, just show a blank panel until the user has made a selection
                LOGGER.debugNavigation(".treeSelection() NOT Runnable show Blank panel until the user has made a selection");
                clearUIOccupant(new NullData());
                setUIOccupant(new NullData());
                }
            }

        // Now build and show the Task menu for what to do next...
        NavigationUtilities.buildTaskPopupMenu(userobject, this, popupMenu, event, rectangle);
        }


    /***********************************************************************************************
     * A Resource is selected.
     *
     * @param userobject
     * @param event
     * @param rectangle
     * @param menu
     */

    private void resourceSelected(final UserObjectPlugin userobject,
                                  final MouseEvent event,
                                  final Rectangle rectangle,
                                  final JPopupMenu menu)
        {
        final JMenuItem menuItem;

        if (!event.isControlDown())
            {
            // Is the Resource already visible?
            // Is there an editor for this Resource?
            if ((!getUIOccupant().equals(userobject))
                && (userobject.getEditorClassname() != null)
                && (!EMPTY_STRING.equals(userobject.getEditorClassname().trim())))
                {
                // Run the Editor
                NavigationUtilities.executeOrBlockRecursion(userobject,
                                                            event,
                                                            FrameworkManagerUIComponentPlugin.MODE_EDIT);
                }
            }
        else
            {
            // Control is down, so we need to build a Popup menu
            // Resources may only be edited...
            menuItem = new JMenuItem("Edit Resource");

            // Is there an editor for this Resource?
            if (((userobject.getEditorClassname() != null)
                && (!EMPTY_STRING.equals(userobject.getEditorClassname().trim()))))
                {
                menuItem.addActionListener(new ActionListener()
                  {
                  public void actionPerformed(final ActionEvent event)
                      {
                      // Run the Editor
                      NavigationUtilities.executeOrBlockRecursion(userobject,
                                                                  event,
                                                                  FrameworkManagerUIComponentPlugin.MODE_EDIT);
                      }
                  });

                // Check if this is a click on the same Resource as before
                if (getUIOccupant().equals(userobject))
                    {
                    // If visible, do nothing
                    menuItem.setEnabled(false);
                    menuItem.setToolTipText("The Resource Editor is already visible");
                    }
                else
                    {
                    // Otherwise, show a blank panel until user has made a decision
                    menuItem.setEnabled(true);
                    menuItem.setToolTipText("Edit this Resource");
                    }
                }
            else
                {
                // There is no Editor installed...
                menuItem.setEnabled(false);
                menuItem.setToolTipText("There is no Editor installed");
                }

            // Position the menu relative to the selected node
            menu.add(menuItem);
            NavigationUtilities.positionPopupMenu(menu,
                                                  event,
                                                  rectangle,
                                                  ((JSplitPane) getSplitScreenComponent()).getDividerLocation());
            }
        }


    /**********************************************************************************************/
    /* UI Occupant                                                                                */
    /***********************************************************************************************
     * Get the current UserObjectPlugin occupant of the UI.
     * The display shows the UIComponent of the UserObjectPlugin.
     *
     * @return UserObjectPlugin
     */

    public UserObjectPlugin getUIOccupant()
        {
        return (uiOccupant);
        }


    /***********************************************************************************************
     * Set the UI occupant to whatever UserObjectPlugin is being used for the UI.
     * Perform any extra actions to make it visible with runUI().
     * The incoming UserObjectPlugin will be in Browse or Editor mode.
     * Browse mode shows the UIComponent, Editor mode shows a configuration editor.
     *
     * @param newoccupant
     */

    public final void setUIOccupant(final UserObjectPlugin newoccupant)
        {
        if ((newoccupant == null)
            || (REGISTRY_MODEL.getUserInterface() == null)
            || (REGISTRY_MODEL.getUserInterface().getUI() == null))
            {
            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant do nothing, NULL");
            return;
            }

        LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant() start: new occupant=" + newoccupant.getClass().getName());

        // ToDo polymorphic method calls?

        if (newoccupant instanceof AtomPlugin)
            {
            final AtomPlugin pluginAtom = (AtomPlugin) newoccupant;

            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant() fmpanel rebuild for ATOM " + pluginAtom.getName());

            // Retrieve the component and place into the UI
            // This calls RootData.runUI()
            installUIComponentForScreenMode(NavigationUtilities.getVisibleComponentOfUserObjectAndRunUI(pluginAtom));

            // Install the new Actions (if any)
            // NOTE do not pass the Framework's ContextActionGroups!
            // They are *always* installed in REGISTRY_MODEL.getUserInterface().rebuildNavigation()
            // TODO REVIEW THIS - CONFUSING! - REGISTRY_MODEL.rebuildNavigation(pluginAtom.getContextActionGroups());
            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant rebuildNavigation for ATOM");
            REGISTRY_MODEL.rebuildNavigation(null, null);

            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant AtomPlugin completed [paneloccupant=" + pluginAtom.getName() + ']');
            }
        else if (newoccupant instanceof TaskPlugin)
            {
            final TaskPlugin pluginTask = (TaskPlugin) newoccupant;

            // Install the new Task ContextActions (if any)
            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant() fmpanel rebuild for TASK " + pluginTask.getName());

            // Retrieve the component and place into the UI
            // This may re-rebuild the navigation, for example for TabbedUIs, by calling runUI()
            installUIComponentForScreenMode(NavigationUtilities.getVisibleComponentOfUserObjectAndRunUI(pluginTask));

            // Don't show any Actions in some circumstances
            if ((pluginTask.isRootTask())
                || (!pluginTask.isPublic()))
                {
                LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant rebuildNavigation for TASK");
                REGISTRY_MODEL.rebuildNavigation(null, null);
                }
            else
                {
                LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant rebuildNavigation for TASK");
                REGISTRY_MODEL.rebuildNavigation(pluginTask, pluginTask.getUIComponent());
                }

            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant TaskPlugin completed [paneloccupant=" + pluginTask.getPathname() + "]");
            }
        else if (newoccupant instanceof ResourcePlugin)
            {
            final ResourcePlugin pluginResource = (ResourcePlugin) newoccupant;

            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant() fmpanel rebuild for RESOURCE " + pluginResource.getName());

            // Retrieve the component and place into the UI
            installUIComponentForScreenMode(NavigationUtilities.getVisibleComponentOfUserObjectAndRunUI(pluginResource));

            // No MenuBar or ToolBar for Resources
            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant rebuildNavigation for RESOURCE");
            REGISTRY_MODEL.rebuildNavigation(null, null);

            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant ResourcePlugin completed [paneloccupant=" + pluginResource.getPathname() + ']');
            }
        else
            {
            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant() fmpanel rebuild for USEROBJECT " + newoccupant.getName());

            // Retrieve the UserObject's UIComponent and place into the UI
            installUIComponentForScreenMode(NavigationUtilities.getVisibleComponentOfUserObjectAndRunUI(newoccupant));

            // No MenuBar or ToolBar for non-Atoms and Tasks
            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant rebuildNavigation for UNKNOWN");
            REGISTRY_MODEL.rebuildNavigation(null, null);

            LOGGER.debugNavigation("FrameworkManagerPanel.setUIOccupant NullData completed [paneloccupant=" + newoccupant.getPathname() + ']');
            }

        // Save the new occupant if all went well
        uiOccupant = newoccupant;
        }


    /***********************************************************************************************
     * Clear the UI component, but only if the Occupant has changed.
     * Remove the previous UI with stopUI().
     *
     * @param newoccupant
     */

    public final void clearUIOccupant(final UserObjectPlugin newoccupant)
        {
        // We only need the NewOccupant to see if it is different from the old one
        if ((getUIOccupant() != null)
            && (!getUIOccupant().equals(newoccupant))
            && (REGISTRY_MODEL.getUserInterface() != null)
            && (((getSplitScreenComponent() != null) && !getFullScreen())
                || ((getFullScreenComponent() != null) && getFullScreen())))
            {
            // The occupant has changed, so remove the current Context Actions
            //LOGGER.debugNavigation(".clearUIOccupant rebuild in fmgr panel");

            // Examine the *previous* occupant, put its panel back to a blank canvas
            // For everything except Tasks, we can safely reset the previous object's panel to Null
            // Runnable Tasks must be left in STATE_TASK_RUNNING (with no UI)
            // Non-Runnable Tasks should be moved into STATE_TASK_IDLE

            if (getUIOccupant() instanceof AtomPlugin)
                {
                final AtomPlugin pluginAtom = (AtomPlugin) getUIOccupant();
                pluginAtom.stopUI();
                pluginAtom.setEditorComponent(null);

                LOGGER.debugNavigation(".clearUIOccupant PREVIOUSLY AtomPlugin " + pluginAtom.getName());
                }

            else if (getUIOccupant() instanceof TaskPlugin)
                {
                final TaskPlugin pluginTask = (TaskPlugin) getUIOccupant();
                final AtomPlugin pluginAtom = pluginTask.getParentAtom();
//                LOGGER.debugNavigation(".clearUIOccupant PREVIOUSLY TaskPlugin " + pluginTask.getName());

                // Ensure any UI threads are stopped while the Task is not visible...
                pluginTask.stopUI();
                pluginTask.setEditorComponent(null);

                if (pluginAtom != null)
                    {
                    if (pluginAtom.isTaskRunnable(pluginTask))
                        {
                        // Runnable Tasks must be left alone
    //                    LOGGER.debugNavigation(".clearUIOccupant PREVIOUS RUNNABLE TASK leave alone");
                        LOGGER.debugNavigation(".clearUIOccupant PREVIOUS RUNNABLE TASK leave alone");
                        }
                    else
                        {
                        // Non-Runnable Tasks must be parked in STATE_TASK_IDLE
                        // Runnable Tasks cannot be made Idle in this way
                        // There must be an explicit request to stop them running
    //                    LOGGER.debugNavigation(".clearUIOccupant TaskPlugin TO IDLE");
                        if (!MODEL_CONTROLLER.setTaskState(pluginAtom, pluginTask, TaskState.IDLE))
                            {
                            // Can't hide it, so something is very wrong...
    //                        LOGGER.debugNavigation(".clearUIOccupant TaskPlugin can't set to Idle");
                            }
                        }
                    }
                else
                    {
                    LOGGER.debugNavigation(".clearUIOccupant Parent Atom is NULL");
                    }
                }
            else if (getUIOccupant() instanceof ResourcePlugin)
                {
                // We don't need to know which type of Resource it is
                final ResourcePlugin pluginResource = (ResourcePlugin) getUIOccupant();
                pluginResource.stopUI();
                pluginResource.setEditorComponent(null);

                LOGGER.debugNavigation(".clearUIOccupant PREVIOUSLY ResourcePlugin " + pluginResource.getName());
                }
            else if (getUIOccupant() != null)
                {
                final UserObjectPlugin plugin = getUIOccupant();
                plugin.stopUI();
                plugin.setEditorComponent(null);

                LOGGER.debugNavigation(".clearUIOccupant PREVIOUSLY UserObjectPlugin " + plugin.getName());
                }
            else
                {
                // This will happen only at startup, so ignore it normally
                LOGGER.debugNavigation(".clearUIOccupant [class not found for previous occupant]");
                }

            // Record the fact that there's now no occupant
            uiOccupant = null;
            }
        else
            {
            // Nothing has changed, just reselecting the same node
            // This should never happen, because the Tree doesn't make any event
            LOGGER.debugNavigation(".clearUIOccupant [no change]");
            }
        }


    /***********************************************************************************************
     * Install the specified UIComponentPlugin and reassemble the UI layout.
     * The specified UIComponentPlugin becomes the new UI,
     * either on its own in full-screen mode, or as the right hand part of a JSplit Screen.
     *
     * @param component
     */

    private void installUIComponentForScreenMode(final UIComponentPlugin component)
        {
        LOGGER.debugNavigation("FrameworkManagerPanel.installUIComponentForScreenMode=" + component.getClass().getName());
        if (getFullScreen())
            {
            //System.out.println("installUIComponentForScreenMode() FULL SCREEN MODE");
            if ((getFullScreenComponent() != null)
                && (component != null))
                {
                // We are going full-screen, so remove the split-screen
                if (getSplitScreenComponent() != null)
                    {
                    // ToDo Review setVisible()
                    getSplitScreenComponent().setVisible(false);
                    }

                // Initialise the full-screen, and add JComponents
                getFullScreenComponent().removeAll();
                //System.out.println("ADDING FULL SCREEN Component");
                getFullScreenComponent().add((Component)component, BorderLayout.CENTER);

                // Switch to full-screen
                // ToDo Review setVisible()
                getFullScreenComponent().setVisible(true);
                }
            else
                {
                LOGGER.debugNavigation("FrameworkManager: FullScreenComponent is NULL");
                }
            }
        else
            {
            //System.out.println("installUIComponentForScreenMode() SPLIT SCREEN MODE");
            if ((getSplitScreenComponent() != null)
                && (component != null))
                {
                if (getFullScreenComponent() != null)
                    {
                    // We are going split-screen, so remove the full-screen
                    // ToDo Review setVisible()
                    getFullScreenComponent().setVisible(false);
                    }

                // Initialise the split-screen, and add JComponent
                // There is no need to remove the previous RightComponent
                //System.out.println("SETTING R/H COMPONENT");
                ((JSplitPane) getSplitScreenComponent()).setRightComponent((Component)component);

                // Switch to split-screen
                // ToDo Review setVisible()
                getSplitScreenComponent().setVisible(true);
                }
            else
                {
                LOGGER.debugNavigation("FrameworkManager: SplitScreenComponent is NULL");
                }
            }

        // Put together the new screen layout
        // This is the only call...
        assembleUIComponents();

        if ((REGISTRY_MODEL.getUserInterface() != null)
            && (REGISTRY_MODEL.getUserInterface().getUI() != null))
            {
            REGISTRY_MODEL.getUserInterface().getUI().validate();
            }

        // It is now safe to reset the position of the split pane divider
        // to the last value saved to the RegistryModel
        if (!getFullScreen())
            {
            // Reset the position of the divider, but not until we are visible
            setAndSaveDividerLocation(REGISTRY.getDoubleProperty(getResourceKey() + KEY_DIMENSION_DIVIDER_LOCATION));
            }

        // Now repaint everything...
        validateAndUpdateUI();
        }


    /***********************************************************************************************
     * Assemble the appropriate set of UI components for the current screen mode.
     */

    private void assembleUIComponents()
        {
        // Remove everything from the FrameworkManager UIComponent
        removeAll();

        // Get the Toolbar and add it
        if ((REGISTRY_MODEL.getUserInterface() != null)
            && (REGISTRY_MODEL.getToolBar() != null))
            {
            // This is the only place that the Toolbar is added to the UI
            if (LOADER_PROPERTIES.isToolbarDisplayed())
                {
                add(REGISTRY_MODEL.getToolBar(), BorderLayout.NORTH);
                }
            else
                {
                LOGGER.login("FrameworkManager.assembleUIComponents() Toolbar hidden at request of User");
                }
            }

        // Add the appropriate UI component
        if (getFullScreen())
            {
            if (getFullScreenComponent() != null)
                {
                //System.out.println("assembleUIComponents() ADD FULL SCREEN COMPONENT TO FRAMEWORKMANAGERPANEL");
                add((Component)getFullScreenComponent(), BorderLayout.CENTER);
                }
            }
        else
            {
            if (getSplitScreenComponent() != null)
                {
                //System.out.println("assembleUIComponents() ADD SPLIT SCREEN COMPONENT TO FRAMEWORKMANAGERPANEL");
                add(getSplitScreenComponent(), BorderLayout.CENTER);
                }
            }

        // Finally, add the status bar
        if (getStatusBar() != null)
            {
            add(getStatusBar(), BorderLayout.SOUTH);
            }
        }


    /***********************************************************************************************
     * Validate and Update the UI of all components.
     */

    public final void validateAndUpdateUI()
        {
        NavigationUtilities.updateComponentTreeUI(this);

        // The MenuBar is strictly in the UserInterface, but repeat the validate here
        if (REGISTRY_MODEL.getMenuBar() != null)
            {
            NavigationUtilities.updateComponentTreeUI(REGISTRY_MODEL.getMenuBar());
            }

        if (REGISTRY_MODEL.getToolBar() != null)
            {
            NavigationUtilities.updateComponentTreeUI(REGISTRY_MODEL.getToolBar());
            }

        if (getFullScreen())
             {
             if (getFullScreenComponent() != null)
                 {
                 getFullScreenComponent().validate();
                 }
             }
        else
             {
             if (getSplitScreenComponent() != null)
                 {
                 NavigationUtilities.updateComponentTreeUI(getSplitScreenComponent());
                 }
             }

        if (getNavigationTree() != null)
            {
            // Re-create the FrameworkManager TreeCellRenderer()
            resetCellRenderer(getNavigationTree());

            getNavigationTree().revalidate();
            }

        if (getStatusBar() != null)
            {
            NavigationUtilities.updateComponentTreeUI(getStatusBar());
            }

        // Ensure that everything in the FrameworkManagerPanel is up to date
        NavigationUtilities.updateComponentTreeUI(this);
        }


    /***********************************************************************************************
     * Get the UIComponentPlugin used for display in full-screen mode.
     *
     * @return UIComponentPlugin
     */

    public final UIComponentPlugin getFullScreenComponent()
        {
        return (this.fullScreen);
        }


    /***********************************************************************************************
     *  Set the UIComponentPlugin used for display in full-screen mode.
     *
     * @param component
     */

    public final void setFullScreenComponent(final UIComponentPlugin component)
        {
        this.fullScreen = component;
        }


    /***********************************************************************************************
     * Get the UIComponentPlugin used for display in split-screen mode.
     *
     * @return JComponent
     */

    public final JComponent getSplitScreenComponent()
        {
        return (this.splitPane);
        }


    /***********************************************************************************************
     * Set the JComponent used for display in split-screen mode.
     *
     * @param splitpane
     */

    public final void setSplitScreenComponent(final JComponent splitpane)
        {
        // ToDo REVIEW NOW
        this.splitPane = splitpane;

//        if (getMainSplitPane() != null)
//            {
//            getMainSplitPane().setMinimumSize(new Dimension(200, 150));
//            }

        }


    /***********************************************************************************************
     * Get the ScrollPane which holds the navigation JTree.
     *
     * @return JScrollPane
     */

    private JScrollPane getScrollPaneTree()
        {
        return (this.scrollpaneTree);
        }


    /**********************************************************************************************/
    /* Status Bar                                                                                 */
    /***********************************************************************************************
     * Rebuild the StatusBar by adding all the StatusIndicators.
     *
     * @param statusbar
     * @param indicators
     */

    public final void rebuildStatusBar(final JComponent statusbar,
                                       final EnumMap<StatusIndicatorKey, StatusIndicatorInterface> indicators)
        {
        if ((statusbar != null)
            && (indicators != null)
            && (indicators.values() != null))
            {
            final Iterator<StatusIndicatorInterface> iterIndicators;

            statusbar.removeAll();
            iterIndicators = indicators.values().iterator();

            // Add all StatusIndicators to the StatusIndicatorKey...
            while (iterIndicators.hasNext())
                {
                final StatusIndicatorInterface indicator = iterIndicators.next();

                if (indicator != null)
                    {
                    statusbar.add((Component)indicator);
                    }
                }
            }
        }


    /***********************************************************************************************
     * Remove all StatusIndicators from the StatusBar.
     *
     * @param statusbar
     * @param indicators
     */

    public final void disposeStatusBar(final JComponent statusbar,
                                       final EnumMap<StatusIndicatorKey, StatusIndicatorInterface> indicators)
        {
        if ((statusbar != null)
            && (indicators != null)
            && (indicators.values() != null))
            {
            final Iterator<StatusIndicatorInterface> iterIndicators;

            iterIndicators = indicators.values().iterator();

            // Ensure that all StatusIndicators are disposed of correctly...
            while (iterIndicators.hasNext())
                {
                final StatusIndicatorInterface indicator = iterIndicators.next();

                if (indicator != null)
                    {
                    indicator.stopUI();
                    indicator.disposeUI();
                    }
                }

            // Now remove them from the map...
            clearStatusIndicators(indicators);

            // ...and from the StatusIndicatorKey itself
            statusbar.removeAll();
            setStatusBar(null);
            }
        }


    /***********************************************************************************************
     * Get the StatusBar component.
     *
     * @return JPanel
     */

    public final JComponent getStatusBar()
        {
        return (this.statusBar);
        }


    /***********************************************************************************************
     * Set the StatusBar component.
     *
     * @param bar
     */

    public final void setStatusBar(final JComponent bar)
        {
        this.statusBar = bar;
        }


    /***********************************************************************************************
     * Get the specified StatusIndicator.
     * Return <code>null</code> if the indicator is not found.
     *
     * @param key
     *
     * @return StatusIndicatorInterface
     */

    public final StatusIndicatorInterface getStatusIndicator(final StatusIndicatorKey key)
        {
        if ((getStatusIndicators() != null)
            && (key != null))
            {
            return (getStatusIndicators().get(key));
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Add a StatusIndicator to the StatusBar.
     *
     * @param indicator
     */

    public final void addStatusIndicator(final StatusIndicatorKey key,
                                         final StatusIndicatorInterface indicator)
        {
        if ((getStatusIndicators() != null)
            && (indicator != null))
            {
            getStatusIndicators().put(key, indicator);
            }
        }


    /***********************************************************************************************
     * Remove a StatusIndicator from the StatusBar.
     *
     * @param key
     */

    public final void removeStatusIndicator(final StatusIndicatorKey key)
        {
        if ((getStatusIndicators() != null)
            && (key != null))
            {
            getStatusIndicators().remove(key);
            }
        }


    /***********************************************************************************************
     * Clear all StatusIndicators from the StatusBar.
     */

    public final void clearStatusIndicators(final EnumMap<StatusIndicatorKey, StatusIndicatorInterface> indicators)
        {
        if (indicators != null)
            {
            indicators.clear();
            }
        }


    /***********************************************************************************************
     * Get the StatusIndicator map.
     *
     * @return EnumMap<StatusIndicatorKey, StatusIndicatorInterface>
     */

    public final EnumMap<StatusIndicatorKey, StatusIndicatorInterface> getStatusIndicators()
        {
        return (this.mapIndicators);
        }


    /**********************************************************************************************/
    /* Navigation Tree                                                                            */
    /***********************************************************************************************
     * Get the selected TreeNode.
     *
     * @return DefaultMutableTreeNode
     */

    public final DefaultMutableTreeNode getSelectedTreeNode()
        {
        return (this.treenodeSelected);
        }


    /***********************************************************************************************
     * Set the selected TreeNode.
     *
     * @param treenode
     */

    public final void setSelectedTreeNode(final DefaultMutableTreeNode treenode)
        {
        this.treenodeSelected = treenode;
        }


    /***********************************************************************************************
     * Get the TreeModel, used by the JTree.
     *
     * @return DefaultTreeModel
     */

    private DefaultTreeModel getTreeModel()
        {
        return (this.treeModel);
        }


    /***********************************************************************************************
     * Set the TreeModel, used by the JTree.
     *
     * @param model
     */

    private void setTreeModel(final DefaultTreeModel model)
        {
        treeModel = model;
        }


    /***********************************************************************************************
     * Get the navigation JTree.
     *
     * @return JTree
     */

    public final JTree getNavigationTree()
        {
        return (this.treeManager);
        }


    /***********************************************************************************************
     * Set the navigation JTree.
     *
     * @param tree
     */

    public final void setNavigationTree(final JTree tree)
        {
        this.treeManager = tree;
        }


    /***********************************************************************************************
     * Get the TreeCellRenderer.
     *
     * @return TreeCellRenderer
     */

    private TreeCellRenderer getTreeCellRenderer()
        {
        return (this.treeCellRenderer);
        }


    /***********************************************************************************************
     * Set the TreeCellRenderer.
     *
     * @param renderer
     */

    private void setTreeCellRenderer(final TreeCellRenderer renderer)
        {
        this.treeCellRenderer = renderer;
        }


    /***********************************************************************************************
     * Reset the TreeCellRenderer.
     * This is required when changing Look&Feel.
     *
     * @param tree
     */

    public final void resetCellRenderer(final JTree tree)
        {
        if (boolCustomIcons)
            {
            setTreeCellRenderer(new CustomCellRenderer());
            }
        else
            {
            setTreeCellRenderer(new DefaultTreeCellRenderer());
            }

        // Set the Tree cell renderer
        if ((getTreeCellRenderer() != null)
            && (tree != null))
            {
            tree.setCellRenderer(getTreeCellRenderer());
            }
        }


    /***********************************************************************************************
     * Get the JSplitPaneAdapter.
     *
     * @return JSplitPaneAdapter
     */

    private JSplitPaneAdapter getAdapter()
        {
        return (this.adapter);
        }


    /**********************************************************************************************/
    /* Framework Manager Context Actions                                                          */
    /***********************************************************************************************
     * Get the FullScreen mode.
     *
     * @return boolean
     */

    public final boolean getFullScreen()
        {
        return (this.boolFullScreen);
        }


    /***********************************************************************************************
     * Set the FullScreen mode.
     *
     * @param fullscreen
     */

    public final void setFullScreen(final boolean fullscreen)
        {
        boolFullScreen = fullscreen;
        }


    /***********************************************************************************************
     * Toggle the ScreenMode.
     */

    public final void toggleScreenMode()
        {
        setFullScreen(!getFullScreen());
        // Reload the current occupant in the new screen mode
        setUIOccupant(getUIOccupant());
        }


    /***********************************************************************************************
     * Toggle the CustomIcons.
     */

    public final void toggleCustomIcons()
        {
        boolCustomIcons = !boolCustomIcons;
        REGISTRY.setBooleanProperty(getResourceKey() + KEY_ENABLE_CUSTOM_ICONS,
                                    boolCustomIcons);

        resetCellRenderer(getNavigationTree());
        NavigationUtilities.updateComponentTreeUI(getNavigationTree());
        }


    /**********************************************************************************************/
    /* Miscellaneous                                                                              */
    /***********************************************************************************************
     *
     * @param userobject
     * @param selected
     * @param treenode
     */

    private void notImplemented(final UserObjectPlugin userobject,
                                final Object selected,
                                final DefaultMutableTreeNode treenode)
        {
        ((UserObjectPlugin)selected).setStatus(STATUS_NOT_IMPLEMENTED);

        clearUIOccupant(userobject);
        setUIOccupant(new NullData());

        getNavigationTree().setSelectionPath(new TreePath(treenode.getPath()));
        setSelectedTreeNode(treenode);
        }


    /***********************************************************************************************
     * Set the position of the SplitPane divider to the specified percentage,
     * and save in the RegistryModel.
     *
     * @param dividerpercentage
     */

    public void setAndSaveDividerLocation(final double dividerpercentage)
        {
        final double dblLocation;
        final int intDividerLocationPixels;

        // Save the percentage location of the divider
        // Trap the careless...
        if ((dividerpercentage < 0.0) || (dividerpercentage > 100.0))
            {
            dblLocation = 50.0;
            }
        else
            {
            dblLocation = dividerpercentage;
            }

        //LOGGER.debugNavigation("setAndSaveDividerLocation to " + dividerpercentage + "%");

        // Only move the divider if the JSplitPane has been rendered...
        if ((getSplitScreenComponent() != null)
            && (((JSplitPane) getSplitScreenComponent()).getDividerLocation() >= 0)
            && (getScrollPaneTree() != null))
            {
//            LOGGER.debugNavigation("UPDATING DIVIDER to "
//                                + dblLocation
//                                + " from "
//                                + ((JSplitPane) getMainSplitPane()).getDividerLocation());

            // Calculate the pixel location given the percentage
            intDividerLocationPixels = (int)(dblLocation * (getSplitScreenComponent().getWidth() / 100.0));
            //LOGGER.debugNavigation("setAndSaveDividerLocation percentage width=" + dblLocation);
            //LOGGER.debugNavigation("setAndSaveDividerLocation pixel width=" + intDividerLocationPixels);

            ((JSplitPane) getSplitScreenComponent()).setDividerLocation(intDividerLocationPixels);

            // Make sure it stays where it is put
            // There seems to be an out-by-one error, so correct it...
            getScrollPaneTree().setPreferredSize(new Dimension(intDividerLocationPixels-1, 1024));

            // Now save (only) the location percentage back in the RegistryModel
            // This probably came from the RegistryModel in the first place,
            // but we don't know for sure
            //System.out.println("FMGR setAndSaveDividerLocation() " + getResourceKey() + KEY_DIMENSION_DIVIDER_LOCATION + "=" + dblLocation);
            REGISTRY.setDoubleProperty(getResourceKey() + KEY_DIMENSION_DIVIDER_LOCATION,
                                       dblLocation);
            }
        else
            {
            LOGGER.debugNavigation("Unable to set the divider location");
            }
        }


    /***********************************************************************************************
     * Get the host TaskPlugin.
     *
     * @return TaskPlugin
     */

    private TaskPlugin getHostTask()
        {
        return (this.pluginHost);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Read all the Resources required by the FrameworkManager.
     */

    private void readResources()
        {
        setDebug(REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_DEBUG));
        setDebug(true);

        // getResourceKey() returns 'FrameworkManager.'
        colourStatusBar = (ColourInterface) REGISTRY.getProperty(getResourceKey() + KEY_STATUS_BAR_TEXT_COLOUR);
        fontStatusBar = (FontInterface) REGISTRY.getProperty(getResourceKey() + KEY_STATUS_BAR_TEXT_FONT);
        dblDividerLocation = REGISTRY.getDoubleProperty(getResourceKey() + KEY_DIMENSION_DIVIDER_LOCATION);
        boolCustomIcons = REGISTRY.getBooleanProperty(getResourceKey() + KEY_ENABLE_CUSTOM_ICONS);
        }
    }
