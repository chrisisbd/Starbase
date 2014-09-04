// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.impl.clock.clocks.SimplePlatformClock;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.GroupRearrangerMenu;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.InstrumentRearrangerMenu;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.xmlbeans.attributes.AttributesDocument;
import org.lmn.fc.model.xmlbeans.groups.Definition;
import org.lmn.fc.model.xmlbeans.groups.ObservatoryGroupDefinitionsDocument;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.SpringUtilities;
import org.lmn.fc.ui.components.UIComponent;
import org.lmn.fc.ui.components.UIComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


/***************************************************************************************************
 * The ObservatoryUI.
 */

public final class ObservatoryUI extends UIComponent
                                 implements ObservatoryUIInterface
    {
    // Injections
    private final InstrumentsDocument docInstruments;
    private final AttributesDocument docAttributes;
    private final ObservatoryGroupDefinitionsDocument docGroups;
    private final UserPlugin userPlugin;
    private final AtomPlugin hostAtom;
    private final String strResourceKey;

    // Groups and Instruments Management
    private List<ObservatoryInstrumentInterface> listObservatoryInstruments;
    private int intCurrentGroupID;
    private final List<Integer> listGroupIDs;
    private final List<Integer> listGroupSortIndexes;
    private final Hashtable<Integer, Integer> hashtableSortIndexToGroupID;
    private final Hashtable<Integer, Definition> hashGroupDefinitionsTable;
    private final Hashtable<Integer, java.util.List<Instrument>> hashGroupsToInstruments;
    private final Hashtable<Integer, InstrumentSelector> hashInstrumentSelectors;

    // This is the only reference to the ObservatoryClock
    private final ObservatoryClockInterface platformClock;
    private ObservatoryClockInterface observatoryClock;
    private boolean boolFoundObservatoryClock;
    private ObservatoryLogInterface logInterface;

    // UI
    private JTabbedPane tabbedPane;
    private JComponent uiPanel;
    private UIComponentPlugin uiOccupant;
    private Font fontObservatory;
    private GroupRearrangerMenu groupRearrangerMenu;
    private InstrumentRearrangerMenu instrumentRearrangerMenu;
    private boolean boolRackVisible;


    /***********************************************************************************************
     * Construct an ObservatoryUI for the set of specified Instruments.
     *
     * @param instrumentsdoc
     * @param attributesdoc
     * @param groupsdoc
     * @param userplugin
     * @param hostatom
     * @param resourcekey
     */

    public ObservatoryUI(final InstrumentsDocument instrumentsdoc,
                         final AttributesDocument attributesdoc,
                         final ObservatoryGroupDefinitionsDocument groupsdoc,
                         final UserPlugin userplugin,
                         final AtomPlugin hostatom,
                         final String resourcekey)
        {
        super();

        // It is essential that we pass all of these tests...
        if ((instrumentsdoc == null)
            || (!XmlBeansUtilities.isValidXml(instrumentsdoc))
            || (attributesdoc == null)
            || (!XmlBeansUtilities.isValidXml(attributesdoc))
            || (groupsdoc == null)
            || (!XmlBeansUtilities.isValidXml(groupsdoc))
            || (userplugin == null)
            || (!userplugin.validatePlugin())
            || (hostatom == null)
            || (!hostatom.validatePlugin())
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        docInstruments = instrumentsdoc;
        docAttributes = attributesdoc;
        docGroups = groupsdoc;
        userPlugin = userplugin;
        hostAtom = hostatom;
        strResourceKey = resourcekey;

        listObservatoryInstruments = null;
        intCurrentGroupID = 0;

        listGroupIDs = new ArrayList<Integer>(MAX_GROUPS);
        listGroupSortIndexes = new ArrayList<Integer>(MAX_GROUPS);
        hashtableSortIndexToGroupID = new Hashtable<Integer, Integer>(MAX_GROUPS);
        hashGroupDefinitionsTable = new Hashtable<Integer, Definition>(MAX_GROUPS);
        hashGroupsToInstruments = new Hashtable<Integer,java.util.List<Instrument>>(50);
        hashInstrumentSelectors = new Hashtable<Integer, InstrumentSelector>(MAX_GROUPS);

        // Use this Clock only if all else fails, i.e. there's no DAO,
        // since this clock won't be able to notify any listeners that it has changed...
        platformClock = new SimplePlatformClock(null);

        boolFoundObservatoryClock = false;
        logInterface = null;

        this.uiPanel = null;
        this.fontObservatory = FontInterface.DEFAULT_FONT_INTERFACE.getFont();
        this.groupRearrangerMenu = null;
        this.instrumentRearrangerMenu = null;
        this.boolRackVisible = true;

        // This must never be null, to ensure correct operation of setUIOccupant
        uiOccupant = new BlankUIComponent();
        }


    /***********************************************************************************************
     * Initialise this UIComponent.
     */

    public final void initialiseUI()
        {
        final JPanel panelSelectors;
        final JPanel panelDivider;
        final JPanel panelUI;
        //final JPanel panelStop;
        final ObservatoryUI thisUI;
        final Icon iconSelectorHide;
        final Icon iconSelectorShow;

        super.initialiseUI();

        thisUI = this;

        // Get the latest Resources
        readResources();

        // Create the UI Font, just once
        // This is mostly used on the Group tabs and Rearranger menu
        setObservatoryFont(ObservatoryUIHelper.createFont(FONT_TTF_OBSERVATORY));

        // This UIComponent contains the GroupSelectors and the UIPanel,
        // horizontally in a SpringLayout
        setLayout(new SpringLayout());

        // The TabbedPane is in the Selectors Panel
        setTabbedPane(new JTabbedPane(JTabbedPane.LEFT));
        getTabbedPane().setFont(getObservatoryFont().deriveFont(FONT_SIZE_TAB));
        getTabbedPane().setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
        getTabbedPane().setAlignmentX(Component.LEFT_ALIGNMENT);

        // Set size limits for the TabbedPane
        getTabbedPane().setMinimumSize(InstrumentSelector.DIM_GROUP_TAB_SIZE_MIN);
        getTabbedPane().setMaximumSize(InstrumentSelector.DIM_GROUP_TAB_SIZE_MAX);
        getTabbedPane().setPreferredSize(InstrumentSelector.DIM_GROUP_TAB_SIZE_MAX);
        NavigationUtilities.updateComponentTreeUI(getTabbedPane());

        // Ensure that we get a new ObservatoryClock and ObservatoryLog following initialisation
        setObservatoryClock(null);
        setObservatoryLog(null);

        //------------------------------------------------------------------------------------------
        // Create the data structures for selection via Groups

        // Make the List of GroupIDs and Group SortIndexes
        // Make the Hashtable of GroupDefinitions keyed by GroupID
        ObservatoryUIHelper.initialiseGroupDefinitionsTable(getObservatoryGroupsDoc(),
                                                            getGroupIDs(),
                                                            getGroupSortIndexes(),
                                                            getSortIndexToGroupIDTable(),
                                                            getGroupDefinitionsTable());

        // Make a Hashtable of empty Lists of Instruments, one for each Group
        ObservatoryUIHelper.initialiseGroupsToInstrumentsTable(getObservatoryGroupsDoc(),
                                                               getGroupsToInstrumentsTable());

        // Link Instruments to their parent Group Lists in the Instruments Hashtable,
        // filtered by UserRole
        ObservatoryUIHelper.linkInstrumentsToGroups(getInstrumentsDoc(),
                                                    getAttributesDoc(),
                                                    getUserPlugin(),
                                                    getGroupsToInstrumentsTable());

        // Sort on the GroupSortIndexes so that the Groups are rendered in sequence
        Collections.sort(getGroupSortIndexes());

        // Iterate over the ordered GroupSortIndexes and make an InstrumentSelector for each Group
        // Record the List of ObservatoryInstruments now in the Observatory
        setObservatoryInstruments(ObservatoryUIHelper.createInstrumentSelectors(this,
                                                                                getHostAtom(),
                                                                                getResourceKey(),
                                                                                getGroupIDs(),
                                                                                getGroupSortIndexes(),
                                                                                getSortIndexToGroupIDTable(),
                                                                                getGroupDefinitionsTable(),
                                                                                getGroupsToInstrumentsTable(),
                                                                                getInstrumentSelectorsTable(),
                                                                                getTabbedPane()));
        // Listen for clicks on the JTabbedPane
        // On click, this calls InstrumentSelector.runUI() for the selected tab
        ObservatoryUIHelper.addGroupTabListener((TaskPlugin)getHostAtom().getRootTask(),
                                                this,
                                                getTabbedPane(),
                                                getGroupIDs(),
                                                getGroupSortIndexes(),
                                                getSortIndexToGroupIDTable(),
                                                getInstrumentSelectorsTable());

        //------------------------------------------------------------------------------------------
        // Build the Stop and Selectors Panels

//        panelStop = new JPanel();
//        panelStop.setFont(getObservatoryFont());
//        panelStop.setBorder(BorderFactory.createLineBorder(Color.red));
//        panelStop.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
//        panelStop.setMinimumSize(InstrumentSelector.DIM_GROUP_STOP_SIZE);
//        panelStop.setMaximumSize(InstrumentSelector.DIM_GROUP_STOP_SIZE);
//        panelStop.setPreferredSize(InstrumentSelector.DIM_GROUP_STOP_SIZE);
//        panelStop.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelSelectors = new JPanel();
        panelSelectors.setMinimumSize(InstrumentSelector.DIM_GROUP_TAB_SIZE_MIN);
        panelSelectors.setMaximumSize(InstrumentSelector.DIM_GROUP_TAB_SIZE_MAX);
        panelSelectors.setPreferredSize(InstrumentSelector.DIM_GROUP_TAB_SIZE_MAX);
        //panelSelectors.setLayout(new BoxLayout(panelSelectors, BoxLayout.Y_AXIS));
        panelSelectors.setLayout(new BorderLayout());
//        panelSelectors.setBorder(BorderFactory.createLineBorder(Color.red, 3));
        panelSelectors.setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        panelSelectors.add(getTabbedPane());
//        panelSelectors.add(panelStop);

        // Set the current Group selection to the first Group Tab
        // There must be at least one Group in the Observatory,
        // so take the first item in the SortIndex table
        // We don't need to know the absolute SortIndex
        setCurrentGroupID(getSortIndexToGroupIDTable().get(INDEX_INITIAL_GROUP_SORT_INDEX));

        // Set up a Popup Menu to allow Group Tab rearrangements
        setGroupRearrangerMenu(ObservatoryUIHelper.createGroupRearrangerMenu(getHostAtom(),
                                                                             this,
                                                                             getObservatoryFont(),
                                                                             DEFAULT_COLOUR_TEXT.getColor()));
        // Set up a Popup Menu to allow Group restructuring
        setInstrumentRearrangerMenu(ObservatoryUIHelper.createInstrumentRearrangerMenu(getHostAtom(),
                                                                                       this,
                                                                                       getObservatoryFont(),
                                                                                       DEFAULT_COLOUR_TEXT.getColor()));
        // Create and initialise the UI Panel to contain the InstrumentPanels
        panelUI = new JPanel(new BorderLayout());
        panelUI.setMaximumSize(DIM_UNIVERSE);
        panelUI.setPreferredSize(DIM_UNIVERSE);
        setUIPanel(panelUI);

        panelDivider = new JPanel();
        panelDivider.setLayout(new BorderLayout());
        panelDivider.setMinimumSize(InstrumentSelector.DIM_DIVIDER);
        panelDivider.setMaximumSize(InstrumentSelector.DIM_DIVIDER);
        panelDivider.setPreferredSize(InstrumentSelector.DIM_DIVIDER);
        panelDivider.setBorder(BorderFactory.createRaisedBevelBorder());
        panelDivider.setToolTipText(TOOLTIP_HIDE_RACK);

        iconSelectorHide = RegistryModelUtilities.getAtomIcon(getHostAtom(), ICON_SELECTOR_HIDE);
        iconSelectorShow = RegistryModelUtilities.getAtomIcon(getHostAtom(), ICON_SELECTOR_SHOW);

        // Add a thumb to the selector divider
        panelDivider.add(new JLabel(iconSelectorHide), BorderLayout.CENTER);

        panelDivider.addMouseListener(new MouseAdapter()
            {
            @Override
            public void mouseClicked(final MouseEvent event)
                {
                if (isRackVisible())
                    {
                    panelDivider.setToolTipText(TOOLTIP_SHOW_RACK);
                    setRackVisible(false);
                    removeAll();
                    panelDivider.removeAll();
                    panelDivider.add(new JLabel(iconSelectorShow), BorderLayout.CENTER);
                    add(panelDivider);
                    add(getUIPanel());

                    SpringUtilities.makeCompactGrid(thisUI, 1, 2, 0, 0, 1, 1);
                    }
                else
                    {
                    panelDivider.setToolTipText(TOOLTIP_HIDE_RACK);
                    setRackVisible(true);
                    removeAll();
                    panelDivider.removeAll();
                    panelDivider.add(new JLabel(iconSelectorHide), BorderLayout.CENTER);
                    add(panelSelectors);
                    add(panelDivider);
                    add(getUIPanel());

                    SpringUtilities.makeCompactGrid(thisUI, 1, 3, 0, 0, 1, 1);
                    }
                }
            });

        // Put together the horizontal screen layout of
        // GroupSelector TabbedPane, Divider, UIPanel{UIOccupant}
        removeAll();
        add(panelSelectors);
        add(panelDivider);
        add(getUIPanel());

        SpringUtilities.makeCompactGrid(thisUI, 1, 3, 0, 0, 1, 1);
        }


    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin.
     * Driven indirectly by ObservatoryUIHelper.addGroupTabListener().
     */

    public final void runUI()
        {
        // Get the latest Resources
        readResources();

        // Show all ControlPanels
        // This will run the UI of the currently visible occupant of the UIPanel,
        // so we don't need to getUIOccupant().runUI() here
        if (getCurrentGroupInstrumentSelector() != null)
            {
            getCurrentGroupInstrumentSelector().runUI();
            }

        // Update all components of the UI
        updateUIComponents();

        super.runUI();
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin.
     */

    public final void stopUI()
        {
        // Stop the UI of the currently visible occupant of the UIPanel
        if (getUIOccupant() != null)
            {
            getUIOccupant().stopUI();
            }

        // Stop all UIComponents on all Group Tabs regardless
        if (getInstrumentSelectorsTable() != null)
            {
            final Enumeration<InstrumentSelector> enumSelectors;

            enumSelectors = getInstrumentSelectorsTable().elements();

            while (enumSelectors.hasMoreElements())
                {
                final InstrumentSelector selector;

                selector = enumSelectors.nextElement();

                if (selector != null)
                    {
                    selector.stopUI();
                    }
                }
            }

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        super.disposeUI();

        getGroupIDs().clear();
        getGroupSortIndexes().clear();
        getSortIndexToGroupIDTable().clear();
        getGroupDefinitionsTable().clear();
        getGroupsToInstrumentsTable().clear();
        getInstrumentSelectorsTable().clear();
        }


    /**********************************************************************************************/
    /* UI Occupant                                                                                */
    /***********************************************************************************************
     * Get the current UIComponentPlugin occupant of the UI.
     *
     * @return UIComponentPlugin
     */

    public UIComponentPlugin getUIOccupant()
        {
        return (uiOccupant);
        }


    /***********************************************************************************************
     * Set the UI occupant to whatever UIComponentPlugin is being used for the UI.
     * Perform any extra actions to make it visible with runUI().
     *
     * @param newoccupant
     */

    public void setUIOccupant(final UIComponentPlugin newoccupant)
        {
        // Make sure that there's somewhere to put the new occupant,
        // and that there is an occupant to replace
        if ((getUIPanel() != null)
            && (getUIOccupant() != null)
            && (newoccupant != null)
            && (getHostAtom() != null))
            {
            // Only set the new occupant if it is different from the old one
            if (!getUIOccupant().equals(newoccupant))
                {
                // Hide the current occupant and remove it from the UIPanel
                //getUIOccupant().setVisible(false);
                getUIPanel().removeAll();
                getUIPanel().revalidate();

                // Save the new occupant
                uiOccupant = newoccupant;

                // Show the UIOccupant on the UIPanel
                getUIPanel().add((Component)getUIOccupant());

                // The validate method is used to cause a container to lay out
                // its subcomponents again. It should be invoked when this container's
                // subcomponents are modified (added to or removed from the container,
                // or layout-related information changed) after the container has been displayed.
                getUIPanel().revalidate();
                NavigationUtilities.updateComponentTreeUI(getUIPanel());
                getUIPanel().revalidate();

                // The occupant has changed, so show the new Context Actions, if any
                // This must do the same job as UIComponent.runSelectedTabComponent()
                // but only if the InstrumentPanel is based on a JTabbedPane, when
                // runSelectedTabComponent() eventually calls runUI() on the visible UIComponent
                // e.g. a ReportTable
                // This ensures that context actions are kept in step with panel changes
                // All Instrument Panels have a constructor parameter of the host Task, the UserObject
                // which is always (TaskPlugin)getHostAtom().getRootTask()
                // The host Atom is always the Observatory

                if ((newoccupant instanceof InstrumentUIComponentDecoratorInterface)
                    && (((InstrumentUIComponentDecoratorInterface)newoccupant).getTabbedPane() != null))
                    {
                    UIComponentHelper.runSelectedTabComponent((TaskPlugin) getHostAtom().getRootTask(),
                                                              newoccupant,
                                                              ((InstrumentUIComponentDecoratorInterface) newoccupant).getTabbedPane());
                    }

                REGISTRY_MODEL.rebuildNavigation((TaskPlugin)getHostAtom().getRootTask(), newoccupant);
                }
            else
                {
                // Nothing has changed, just reselecting the same panel
                //LOGGER.debugProtocolEvent("ObservatoryUI.setUIOccupant [no change]");
                }
            }
        else
            {
            // This should never happen...
            throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
            }
        }


    /***********************************************************************************************
     * Update the UI of all components.
     */

    private void updateUIComponents()
        {
        NavigationUtilities.updateComponentTreeUI(this);

        if (getCurrentGroupInstrumentSelector() != null)
            {
            getCurrentGroupInstrumentSelector().validate();
            }

        if (getUIPanel() != null)
            {
            NavigationUtilities.updateComponentTreeUI(getUIPanel());
            }

        if (getUIOccupant() != null)
            {
            getUIOccupant().validate();
            }
        }


    /***********************************************************************************************
     * Get the UIPanel.
     *
     * @return JComponent
     */

    public JComponent getUIPanel()
        {
        return (this.uiPanel);
        }


    /***********************************************************************************************
     * Set the UIPanel.
     *
     * @param panel
     */

    private void setUIPanel(final JPanel panel)
        {
        this.uiPanel = panel;
        }


    /***********************************************************************************************
     * Get the JTabbedPane for the GroupSelector.
     *
     * @return JTabbedPane
     */

    private JTabbedPane getTabbedPane()
        {
        return (this.tabbedPane);
        }


    /***********************************************************************************************
     * Set the JTabbedPane for the GroupSelector.
     *
     * @param tabbedpane
     */

    private void setTabbedPane(final JTabbedPane tabbedpane)
        {
        this.tabbedPane = tabbedpane;
        }


    /**********************************************************************************************/
    /* InstrumentSelection                                                                        */
    /***********************************************************************************************
     * Get the List of all Instruments in the Observatory.
     *
     * @return
     */

    public List<ObservatoryInstrumentInterface> getObservatoryInstruments()
        {
        return (this.listObservatoryInstruments);
        }


    /***********************************************************************************************
     * Set the List of all Instruments in the Observatory.
     *
     * @param instruments
     */

    private void setObservatoryInstruments(final List<ObservatoryInstrumentInterface> instruments)
        {
        this.listObservatoryInstruments = instruments;
        }


    /***********************************************************************************************
     * Get the currently selected GroupID.
     *
     * @return int
     */

    public int getCurrentGroupID()
        {
        return (this.intCurrentGroupID);
        }


    /***********************************************************************************************
     * Set the currently selected GroupID.
     *
     * @param groupid
     */

    public void setCurrentGroupID(final int groupid)
        {
        this.intCurrentGroupID = groupid;
        }


    /***********************************************************************************************
     * Get the InstrumentSelector panel for the current Group.
     *
     * @return InstrumentSelector
     */

    public final InstrumentSelector getCurrentGroupInstrumentSelector()
        {
        return (getInstrumentSelectorsTable().get(getCurrentGroupID()));
        }


    /***********************************************************************************************
     * Get the List of GroupIDs.
     *
     * @return List<Integer>
     */

    public List<Integer> getGroupIDs()
        {
        return (this.listGroupIDs);
        }


    /***********************************************************************************************
     * Get the List of GroupSortIndexes.
     *
     * @return List<Integer>
     */

    private List<Integer> getGroupSortIndexes()
        {
        return (this.listGroupSortIndexes);
        }


    /***********************************************************************************************
     * Get the Hashtable of Group SortIndexes to GroupID.
     *
     * @return Hashtable<Integer, Integer>
     */

    private Hashtable<Integer, Integer> getSortIndexToGroupIDTable()
        {
        return (this.hashtableSortIndexToGroupID);
        }


    /***********************************************************************************************
     * Get the Hashtable of Instrument Group Definitions, indexed by GroupID.
     *
     * @return Hashtable<Integer, Definition>
     */

    public Hashtable<Integer, Definition> getGroupDefinitionsTable()
        {
        return (this.hashGroupDefinitionsTable);
        }


    /***********************************************************************************************
     * Get the Hashtable of Lists of Instruments in the whole Observatory,indexed by GroupID.
     *
     * @return Hashtable<Integer, Instrument>
     */

    public Hashtable<Integer, java.util.List<Instrument>> getGroupsToInstrumentsTable()
        {
        return (this.hashGroupsToInstruments);
        }


    /***********************************************************************************************
     * Get the table of InstrumentSelectors, indexed by GroupID.
     *
     * @return List<InstrumentSelector>
     */

    public Hashtable<Integer, InstrumentSelector> getInstrumentSelectorsTable()
        {
        return (this.hashInstrumentSelectors);
        }


    /**********************************************************************************************/
    /* Provide access to useful Instruments                                                       */
    /***********************************************************************************************
     * Get the ObservatoryClock.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getObservatoryClock()
        {
        return (this.observatoryClock);
        }


    /***********************************************************************************************
     * Set the ObservatoryClock.
     *
     * @param clock
     */

    public void setObservatoryClock(final ObservatoryClockInterface clock)
        {
        this.observatoryClock = clock;
        }


    /***********************************************************************************************
     * Indicate if the ObservatoryClock has been found, prior to using PlatformClock if not found.
     *
     * @return boolean
     */

    public boolean hasObservatoryClock()
        {
        return (this.boolFoundObservatoryClock);
        }

    /***********************************************************************************************
     * Indicate if the ObservatoryClock has been found, prior to using PlatformClock if not found.
     */

    public void foundObservatoryClock(final boolean found)
        {
        this.boolFoundObservatoryClock = found;
        }


    /***********************************************************************************************
     * Get the PlatformClock.
     * A simple clock driven by the host platform clock, for use when no other clock is available.
     *
     * @return ObservatoryClockInterface
     */

    public ObservatoryClockInterface getPlatformClock()
        {
        return (this.platformClock);
        }


    /***********************************************************************************************
     * Get the ObservatoryLog.
     *
     * @return ObservatoryLogInterface
     */

    public ObservatoryLogInterface getObservatoryLog()
        {
        return (this.logInterface);
        }


    /***********************************************************************************************
     * Set the ObservatoryLog.
     *
     * @param log
     */

    public void setObservatoryLog(final ObservatoryLogInterface log)
        {
        this.logInterface = log;
        }


    /***********************************************************************************************
     * Get the Observatory Font.
     *
     * @return Font
     */

    public Font getObservatoryFont()
        {
        return (this.fontObservatory);
        }


    /***********************************************************************************************
     * Set the Observatory Font.
     *
     * @param font
     */

    public void setObservatoryFont(final Font font)
        {
        this.fontObservatory = font;
        }


    /***********************************************************************************************
     * Get the PopupMenu used to rearrange Group Tabs.
     *
     * @return GroupRearrangerMenu
     */

    public GroupRearrangerMenu getGroupRearrangerMenu()
        {
        return (this.groupRearrangerMenu);
        }


    /***********************************************************************************************
     * Set the PopupMenu used to rearrange Group Tabs.
     *
     * @param menu
     */

    public void setGroupRearrangerMenu(final GroupRearrangerMenu menu)
        {
        this.groupRearrangerMenu = menu;
        }


    /***********************************************************************************************
     * Get the PopupMenu used to rearrange Instruments in the Rack.
     *
     * @return InstrumentRearrangerMenu
     */

    public InstrumentRearrangerMenu getInstrumentRearrangerMenu()
        {
        return (this.instrumentRearrangerMenu);
        }


    /***********************************************************************************************
     * Set the PopupMenu used to rearrange Instruments in the Rack.
     *
     * @param menu
     */

    public void setInstrumentRearrangerMenu(final InstrumentRearrangerMenu menu)
        {
        this.instrumentRearrangerMenu = menu;
        }


    /***********************************************************************************************
     * Indicate if the Rack is visible.
     *
     * @return boolean
     */

    public boolean isRackVisible()
        {
        return (this.boolRackVisible);
        }


    /***********************************************************************************************
     * Indicate if the Rack is visible.
     *
     * @param visible
     */

    public void setRackVisible(final boolean visible)
        {
        this.boolRackVisible = visible;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the Instruments document.
     *
     * @return InstrumentsDocument
     */

    public InstrumentsDocument getInstrumentsDoc()
        {
        return (this.docInstruments);
        }


    /***********************************************************************************************
     * Get the Attributes document.
     *
     * @return AttributesDocument
     */

    public AttributesDocument getAttributesDoc()
        {
        return (this.docAttributes);
        }


    /***********************************************************************************************
     * Get the ObservatoryGroups document.
     *
     * @return ObservatoryGroupDefinitionsDocument
     */

    public ObservatoryGroupDefinitionsDocument getObservatoryGroupsDoc()
        {
        return (this.docGroups);
        }


    /***********************************************************************************************
     * Get the UserPlugin.
     *
     * @return UserPlugin
     */

    public UserPlugin getUserPlugin()
        {
        return (this.userPlugin);
        }


    /***********************************************************************************************
     * Get the host AtomPlugin.
     *
     * @return AtomPlugin
     */

    public AtomPlugin getHostAtom()
        {
        return (this.hostAtom);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (this.strResourceKey);
        }


    /***********************************************************************************************
     * Read all the Resources required by the ObservatoryUI.
     */

    private void readResources()
        {
        setDebug(REGISTRY.getBooleanProperty(REGISTRY.getFrameworkResourceKey() + KEY_ENABLE_DEBUG));
        }
    }
