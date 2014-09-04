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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.Observatory;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.selectors.RackCabinet;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.CompositeIcon;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.GroupRearrangerMenu;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.InstrumentRearrangerMenu;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.VerticalTextIcon;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.xmlbeans.attributes.AttributesDocument;
import org.lmn.fc.model.xmlbeans.attributes.Configuration;
import org.lmn.fc.model.xmlbeans.attributes.UserRoles;
import org.lmn.fc.model.xmlbeans.groups.Definition;
import org.lmn.fc.model.xmlbeans.groups.Group;
import org.lmn.fc.model.xmlbeans.groups.ObservatoryGroupDefinitionsDocument;
import org.lmn.fc.model.xmlbeans.groups.ObservatoryGroups;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.InstrumentsDocument;
import org.lmn.fc.model.xmlbeans.roles.RoleName;
import org.lmn.fc.ui.components.BlankUIComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;


/***************************************************************************************************
 * ObservatoryUIHelper.
 */

public final class ObservatoryUIHelper implements FrameworkConstants,
                                                  FrameworkStrings,
                                                  FrameworkMetadata,
                                                  FrameworkSingletons,
                                                  FrameworkXpath,
                                                  ResourceKeys
    {
    private static final int INITIAL_INSTRUMENT_COUNT = 50;
    private static final int MAX_INSTRUMENTS_PER_GROUP = 20;
    private static final int XML_INDENT = 4;

    private static final String MENU_MOVE_UP = "Move Up";
    private static final String MENU_MOVE_DOWN = "Move Down";
    private static final String MENU_MOVE_TO = "Move to ";
    private static final String MENU_GROUP_RENAME = "Rename Group";
    private static final String MENU_GROUP_DELETE = "Delete Group";
    private static final String MENU_GROUP_CREATE = "Create New Group";
    private static final String MENU_LOCK_INSTRUMENT = "Toggle Instrument Lock";
    private static final String MENU_CLONE_INSTRUMENT = "Clone Instrument";
    private static final String MENU_UNINSTALL_INSTRUMENT = "Uninstall Instrument";
    private static final String MENU_SAVE_ALL_ARRANGEMENTS = "Save All Arrangements";

    private static final String TOOLTIP_ARRANGE_INSTRUMENTS = "Use this menu to rearrange this Group, or Move and Copy Instruments between Groups";
    private static final String TOOLTIP_GROUP_MOVE_UP = "Move the Group up one place";
    private static final String TOOLTIP_GROUP_MOVE_DOWN = "Move the Group down one place";
    private static final String TOOLTIP_GROUP_RENAME = "Rename the Group";
    private static final String TOOLTIP_GROUP_DELETE = "Delete the Group";
    private static final String TOOLTIP_GROUP_CREATE = "Create a new Group";
    private static final String TOOLTIP_INSTRUMENT_MOVE_UP = "Move the Instrument up one place";
    private static final String TOOLTIP_INSTRUMENT_MOVE_DOWN = "Move the Instrument down one place";
    private static final String TOOLTIP_INSTRUMENT_LOCK = "Toggle the lock of the Instrument (ctrl-right-click to reveal menu of locked Instrument)";
    private static final String TOOLTIP_SAVE_ALL = "Save the arrangements of all Groups";

    private static final String ICON_MOVE_UP = "move-up.png";
    private static final String ICON_MOVE_DOWN = "move-down.png";
    private static final String ICON_GROUP_RENAME = "rename-group.png";
    private static final String ICON_GROUP_DELETE = "delete-group.png";
    private static final String ICON_GROUP_CREATE = "create-group.png";
    private static final String ICON_LOCK_INSTRUMENT = "lock-instrument.png";
    private static final String ICON_CLONE_INSTRUMENT = "clone-instrument.png";
    private static final String ICON_UNINSTALL_INSTRUMENT = "uninstall-instrument.png";
    private static final String ICON_SAVE_ARRANGEMENTS = "save-arrangements.png";


    /***********************************************************************************************
     * Indicate if the specified Instrument is the one currently selected on the ObservatoryUI.
     *
     * @param instrument
     *
     * @return boolean
     */

    public static boolean isSelectedInstrument(final ObservatoryInstrumentInterface instrument)
        {
        return ((instrument != null)
                && (instrument.getHostUI() != null)
                && (instrument.getHostUI().getInstrumentSelectorsTable() != null)
                && (instrument.getHostUI().getCurrentGroupInstrumentSelector() != null)
                && (instrument.getHostUI().getCurrentGroupInstrumentSelector().getSelectedInstrument() != null)
                && (instrument.equals(instrument.getHostUI().getCurrentGroupInstrumentSelector().getSelectedInstrument())));
        }


    /***********************************************************************************************
     * Create a Font from a TrueType font file on the classpath.
     * Java fonts look terrible because by default they do no antialiasing and ignore the hints.
     * You can improve them with anti-aliasing.
     *
     * @param fontfilename
     *
     * @return Font
     */

    public static Font createFont(final String fontfilename)
        {
        Font fontHeader;

        // Use the platform default Font intially
        fontHeader = FontInterface.DEFAULT_FONT_INTERFACE.getFont();

        try
            {
            final InputStream fontStream;

            fontStream = InstrumentUIComponentDecorator.class.getResourceAsStream(RegistryModelUtilities.getCommonFontsRoot() + fontfilename);

            if (fontStream != null)
                {
                final Font onePoint;

                onePoint = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                fontStream.close();
                fontHeader = onePoint.deriveFont(Font.PLAIN,
                                                 (float) FontInterface.DEFAULT_FONT_INTERFACE.getFontSize());
                }
            else
                {
                LOGGER.error(FrameworkStrings.EXCEPTION_RESOURCE_NOTFOUND);
                }
            }

        catch (final FontFormatException exception)
            {
            LOGGER.error(FrameworkStrings.EXCEPTION_INVALID_FONTDATA + FrameworkMetadata.METADATA_EXCEPTION + exception.getMessage() + FrameworkMetadata.TERMINATOR);
            }

        catch (final IOException exception)
            {
            LOGGER.error(FrameworkStrings.EXCEPTION_INVALID_FONTDATA + FrameworkMetadata.METADATA_EXCEPTION + exception.getMessage() + FrameworkMetadata.TERMINATOR);
            }

        return (fontHeader);
        }


    /***********************************************************************************************
     * Make the Hashtable of Groups keyed by GroupID, and a List of GroupIDs.
     *
     * @param groupsdoc
     * @param groupids
     * @param groupsortindexes
     * @param sortindextogroupid
     * @param groups
     */

    public static void initialiseGroupDefinitionsTable(final ObservatoryGroupDefinitionsDocument groupsdoc,
                                                       final List<Integer> groupids,
                                                       final List<Integer> groupsortindexes,
                                                       final Hashtable<Integer, Integer> sortindextogroupid,
                                                       final Hashtable<Integer, Definition> groups)
        {
        final String SOURCE = "ObservatoryUIHelper.initialiseGroupDefinitionsTable() ";

        if ((groupsdoc != null)
            && (groupsdoc.getObservatoryGroupDefinitions() != null)
            && (groupids != null)
            && (groupsortindexes != null)
            && (sortindextogroupid != null)
            && (groups != null))
            {
            final List<Definition> listDefinitions;

            // Start with nothing
            groupids.clear();
            groupsortindexes.clear();
            sortindextogroupid.clear();
            groups.clear();

            listDefinitions = groupsdoc.getObservatoryGroupDefinitions().getDefinitionList();

            if (listDefinitions != null)
                {
                for (int i = 0;
                     i < listDefinitions.size();
                     i++)
                    {
                    final Definition definition;

                    definition = listDefinitions.get(i);

                    // Have only one entry!
                    if ((!groupids.contains(definition.getGroupID()))
                        && (!groupsortindexes.contains(definition.getSortIndex())))
                        {
                        groupids.add(definition.getGroupID());
                        groupsortindexes.add(definition.getSortIndex());

                        sortindextogroupid.put(definition.getSortIndex(), definition.getGroupID());
                        groups.put(definition.getGroupID(), definition);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Skipping Group because of duplicate GroupID or SortIndex");
                        }
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "There are no Group Definitions");
                }
            }
        }


    /***********************************************************************************************
     * Make a Hashtable of *empty* Lists of Instruments, one for each Group.
     *
     * @param groupsdoc
     * @param groupstoinstruments
     */

    public static void initialiseGroupsToInstrumentsTable(final ObservatoryGroupDefinitionsDocument groupsdoc,
                                                          final Hashtable<Integer, List<Instrument>> groupstoinstruments)
        {
        final String SOURCE = "ObservatoryUIHelper.initialiseGroupsToInstrumentsTable() ";

        if ((groupsdoc != null)
            && (groupsdoc.getObservatoryGroupDefinitions() != null)
            && (groupstoinstruments != null))
            {
            final List<Definition> listDefinitions;

            groupstoinstruments.clear();
            listDefinitions = groupsdoc.getObservatoryGroupDefinitions().getDefinitionList();

            if (listDefinitions != null)
                {
                for (int i = 0;
                     i < listDefinitions.size();
                     i++)
                    {
                    final Definition definition;

                    definition = listDefinitions.get(i);

                    // Have only one entry!
                    if (!groupstoinstruments.containsKey(definition.getGroupID()))
                        {
                        final List<Instrument> listInstrumentsInGroup;

                        listInstrumentsInGroup = new ArrayList<Instrument>(MAX_INSTRUMENTS_PER_GROUP);

                        // Initialise the group List with NULLs
                        for (int j = 0;
                             j < MAX_INSTRUMENTS_PER_GROUP;
                             j++)
                            {
                            listInstrumentsInGroup.add(null);
                            }

                        groupstoinstruments.put(definition.getGroupID(), listInstrumentsInGroup);
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Skipping Group because of duplicate GroupID or SortIndex");
                        }
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "There are no Group Definitions");
                }
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Groups document or GroupsToInstruments table");
            }
        }


    /***********************************************************************************************
     * Link Instruments to their parent Group Lists in the GroupsToInstruments table.
     *
     * @param instrumentsdoc
     * @param attributesdoc
     * @param userplugin
     * @param groupstoinstruments
     */

    public static void linkInstrumentsToGroups(final InstrumentsDocument instrumentsdoc,
                                               final AttributesDocument attributesdoc,
                                               final UserPlugin userplugin,
                                               final Hashtable<Integer, List<Instrument>> groupstoinstruments)
        {
        final String SOURCE = "ObservatoryUIHelper.linkInstrumentsToGroups() ";

        if ((instrumentsdoc != null)
            && (instrumentsdoc.getInstruments() != null)
            && (attributesdoc != null)
            && (userplugin != null)
            && (groupstoinstruments != null))
            {
            final List<Instrument> listInstruments;

            // Process all Instruments in the Observatory
            listInstruments = instrumentsdoc.getInstruments().getInstrumentList();

            if (listInstruments != null)
                {
                for (int i = 0;
                     i < listInstruments.size();
                     i++)
                    {
                    final Instrument instrument;
                    final StringBuffer expression;
                    final XmlObject[] selection;

                    instrument = listInstruments.get(i);

                    // Find the Configuration for this Instrument Identifier in the Attributes Document
                    expression = new StringBuffer();
                    expression.append(FrameworkXpath.XPATH_ATTRIBUTES_NAMESPACE);
                    expression.append(FrameworkXpath.XPATH_ATTRIBUTES_CONFIGURATION_FOR_IDENTIFIER);
                    expression.append(instrument.getIdentifier());
                    expression.append(FrameworkXpath.XPATH_QUOTE_TERMINATOR);

                    selection = attributesdoc.selectPath(expression.toString());

                    // There must be a single Configuration for this Identifier
                    if ((selection != null)
                        && (selection instanceof Configuration[])
                        && (selection.length == 1)
                        && (selection[0] != null)
                        && (selection[0] instanceof Configuration))
                        {
                        final Configuration configuration;
                        final UserRoles userRoles;
                        boolean boolRoleOk;

                        configuration = (Configuration)selection[0];

                        // Find if the current User has an appropriate Role for this Instrument
                        userRoles = configuration.getUserRoles();
                        boolRoleOk = false;

                        if (userRoles.getRoleNameList() != null)
                            {
                            final List<RoleName.Enum> listRoleNames;

                            listRoleNames = userRoles.getRoleNameList();

                            for (int j = 0;
                                 ((!boolRoleOk) && (j < listRoleNames.size()));
                                 j++)
                                {
                                final RoleName.Enum rolename;

                                rolename = listRoleNames.get(j);

                                if (rolename.toString().equals(userplugin.getRoleName()))
                                    {
                                    // Found the User Role, so leave now
                                    boolRoleOk = true;
                                    }
                                }
                            }

                        // Did we find the User Role?
                        if (boolRoleOk)
                            {
                            final ObservatoryGroups groups;

                            // Obtain the List of ObservatoryGroups relevant to this Instrument
                            groups = configuration.getObservatoryGroups();

                            if (groups != null)
                                {
                                final List<Group> listGroups;

                                listGroups = groups.getGroupList();

                                for (int j = 0;
                                     j < listGroups.size();
                                     j++)
                                    {
                                    final Group group;

                                    group = listGroups.get(j);

                                    // Do we know about this GroupID in the GroupsToInstruments table?
                                    if ((group != null)
                                        && (groupstoinstruments.containsKey(group.getGroupID())))
                                        {
                                        final int intSortIndex;

                                        // If so, add this Instrument to the Group's List
                                        // at the entry for the SortIndex, if in range
                                        intSortIndex = group.getSortIndex();

                                        if ((intSortIndex >= 0)
                                            && (intSortIndex < MAX_INSTRUMENTS_PER_GROUP))
                                            {
//                                            System.out.println(instrument.getIdentifier()
//                                                                + " Add to group " + group.getGroupID()
//                                                                + " at index " + intSortIndex);
                                            groupstoinstruments.get(group.getGroupID()).set(intSortIndex, instrument);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "Invalid Instrument Configuration in XML [xpath=" + expression + "]");
                        }
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "There are no Instruments");
                }
            }
        }


    /***********************************************************************************************
     * Create the InstrumentSelector for each Instrument Group,
     * and add to the specified JTabbedPane.
     *
     * @param hostui
     * @param hostatom
     * @param resourcekey
     * @param groupids
     * @param groupsortindexes
     * @param sortindextogroupid
     * @param groupstable
     * @param groupstoinstruments
     * @param selectorstable
     * @param groupspane
     *
     * @return List<ObservatoryInstrumentInterface>
     */

    public static List<ObservatoryInstrumentInterface> createInstrumentSelectors(final ObservatoryUIInterface hostui,
                                                                                 final AtomPlugin hostatom,
                                                                                 final String resourcekey,
                                                                                 final List<Integer> groupids,
                                                                                 final List<Integer> groupsortindexes,
                                                                                 final Hashtable<Integer, Integer> sortindextogroupid,
                                                                                 final Hashtable<Integer, Definition> groupstable,
                                                                                 final Hashtable<Integer, List<Instrument>> groupstoinstruments,
                                                                                 final Hashtable<Integer, InstrumentSelector> selectorstable,
                                                                                 final JTabbedPane groupspane)
        {
        final String SOURCE = "ObservatoryUIHelper.createInstrumentSelectors() ";
        final Hashtable<String, ObservatoryInstrumentInterface> hashtableInstantiatedInstruments;
        final List<ObservatoryInstrumentInterface> listInstantiatedInstruments;

        // The table of ObservatoryInstruments currently instantiated, keyed by Identifier
        hashtableInstantiatedInstruments = new Hashtable<String, ObservatoryInstrumentInterface>(INITIAL_INSTRUMENT_COUNT);

        if ((hostui != null)
            && (hostatom != null)
            && (resourcekey != null)
            && (groupids != null)
            && (!groupids.isEmpty())
            && (groupsortindexes != null)
            && (!groupsortindexes.isEmpty())
            && (sortindextogroupid != null)
            && (!sortindextogroupid.isEmpty())
            && (groupids.size() == groupsortindexes.size())
            && (groupids.size() == sortindextogroupid.size())
            && (groupstable != null)
            && (groupstoinstruments != null)
            && (selectorstable != null)
            && (groupspane != null))
            {
            final Iterator<Integer> iterSortIndexes;

            // The SortIndexes should be sorted into numerical order...
            iterSortIndexes = groupsortindexes.iterator();

            // No real ObservatoryClock yet...
            hostui.foundObservatoryClock(false);
            hostui.setObservatoryClock(hostui.getPlatformClock());

            // Create a Tab for each Group, even if empty...
            while (iterSortIndexes.hasNext())
                {
                final Integer intSortIndex;
                final Integer intGroupID;
                final ImageIcon graphicIcon;
                final VerticalTextIcon textIcon;
                final CompositeIcon compositeIcon;
                final List<Instrument> listInstrumentsInGroup;
                final InstrumentSelector selector;
                final JScrollPane scrollPane;

                // Iterate over the SortIndexes (assumed to be sorted)
                intSortIndex = iterSortIndexes.next();

                // Map the SortIndex to a GroupID
                intGroupID = sortindextogroupid.get(intSortIndex);

                graphicIcon = RegistryModelUtilities.getAtomIcon(hostatom,
                                                                 groupstable.get(intGroupID).getIconFilename());
                textIcon = new VerticalTextIcon(groupspane,
                                                groupstable.get(intGroupID).getName(),
                                                VerticalTextIcon.ROTATE_DEFAULT);
                compositeIcon = new CompositeIcon(graphicIcon, textIcon);

                // Retrieve the List of Instruments for this Group, which may contain all NULLs
                listInstrumentsInGroup = groupstoinstruments.get(intGroupID);

                // Create and initialise the InstrumentSelector for each Group
                // Keep track of all ObservatoryInstruments which are instantiated
                // This is the only construction of RackCabinet
                selector = new RackCabinet(hostui,
                                           groupstable.get(intGroupID),
                                           listInstrumentsInGroup,
                                           hashtableInstantiatedInstruments,
                                           hostatom,
                                           resourcekey);

                if (!selectorstable.containsKey(intGroupID))
                    {
                    selectorstable.put(intGroupID, selector);
                    }
                else
                    {
                    // This should never happen!
                    LOGGER.error(SOURCE + "Duplicate GroupID found in selectors table");
                    }

                // This will identify the ObservatoryClock and the ObservatoryLog, if present
                selector.initialiseUI();

                // Make a scrolling pane for the InstrumentSelector, because it will get large...
                scrollPane = new JScrollPane((Component) selector);
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                // Set size limits for the scroll pane
                scrollPane.setMinimumSize(InstrumentSelector.DIM_SELECTOR_SIZE_MIN);
                scrollPane.setMaximumSize(InstrumentSelector.DIM_SELECTOR_SIZE_MAX);
                scrollPane.setPreferredSize(InstrumentSelector.DIM_SELECTOR_SIZE_MAX);

                // The JScrollPane is added to the Tab given by the index into the GroupIDs List
                groupspane.addTab(null,
                                  compositeIcon,
                                  scrollPane,
                                  groupstable.get(intGroupID).getTooltip());
                }

            // Check that we now have a real ObservatoryClock,
            // if not, substitute calls to the host platform clock
            if (!hostui.hasObservatoryClock())
                {
                LOGGER.logTimedEvent("The ObservatoryClock could not be found, so using the platform Clock");

                hostui.setObservatoryClock(hostui.getPlatformClock());
                }
            }
        else
            {
            LOGGER.error(SOURCE + "There are no Instruments in the Observatory!");
            }

        listInstantiatedInstruments = new ArrayList<ObservatoryInstrumentInterface>(hashtableInstantiatedInstruments.size());
        listInstantiatedInstruments.addAll(hashtableInstantiatedInstruments.values());

        return (listInstantiatedInstruments);
        }


    /***********************************************************************************************
     * Listen for clicks on the Group JTabbedPane being displayed under the host UIComponent
     * of the specified UserObject.
     * Show the appropriate UIComponentPlugin when a Tab is clicked.
     * Update the toolbar buttons for the visible UIComponentPlugin and control any Timers.
     *
     * @param userobject
     * @param hostui
     * @param tabbedpane
     * @param groupids
     * @param groupsortindexes
     * @param sortindextogroupid
     * @param selectorstable
     */

    public static void addGroupTabListener(final UserObjectPlugin userobject,
                                           final ObservatoryUIInterface hostui,
                                           final JTabbedPane tabbedpane,
                                           final List<Integer> groupids,
                                           final List<Integer> groupsortindexes,
                                           final Hashtable<Integer, Integer> sortindextogroupid,
                                           final Hashtable<Integer, InstrumentSelector> selectorstable)
        {
        final String SOURCE = "ObservatoryUIHelper.addGroupTabListener() ";

        if ((userobject != null)
            && (userobject.validatePlugin())
            && (hostui != null)
            && (tabbedpane != null)
            && (groupids != null)
            && (!groupids.isEmpty())
            && (groupsortindexes != null)
            && (!groupsortindexes.isEmpty())
            && (sortindextogroupid != null)
            && (!sortindextogroupid.isEmpty())
            && (groupids.size() == groupsortindexes.size())
            && (groupids.size() == sortindextogroupid.size())
            && (selectorstable != null))
            {
            // MouseListener didn't seem to give the correct behaviour...
            tabbedpane.addChangeListener(new ChangeListener()
                {
                public void stateChanged(final ChangeEvent event)
                    {
                    final InstrumentSelector selectorPrevious;
                    final InstrumentSelector selectorSelected;

                    selectorPrevious = getPreviousSelector(hostui,
                                                           groupids,
                                                           selectorstable);

                    // The selector will be null if there are no Instruments in this Group
                    if ((selectorPrevious != null)
                        && (selectorPrevious.getSelectorPanelContainer() != null))
                        {
                        selectorPrevious.getSelectorPanelContainer().removeAll();
                        }

                    // Beware! The Tabs are located by SortIndex, not GroupID
                    selectorSelected = getSelectedSelector(hostui,
                                                           tabbedpane,
                                                           groupids,
                                                           groupsortindexes,
                                                           sortindextogroupid,
                                                           selectorstable);

                    // The selector will be null if there are no Instruments in this Group
                    if (selectorSelected != null)
                        {
                        // Run the currently selected InstrumentSelector,
                        // which rebuilds the UI for that Group
                        // This will call UIComponent.runSelectedTabComponent()
                        // for the currently selected InstrumentPanel,
                        // and so will also handle the menus and toolbar changes
                        selectorSelected.runUI();
                        }
                    else
                        {
                        // Otherwise just show the User that the Group is empty
                        // We can't find the Group Name from the Selector because it is null
                        hostui.setUIOccupant(new BlankUIComponent(tabbedpane.getTitleAt(tabbedpane.getSelectedIndex())));
                        hostui.getUIOccupant().runUI();
                        }
                    }
                });

            tabbedpane.addMouseListener(new MouseAdapter()
                {
                // Use mouseReleased() because there seems to be an odd state where the release happens,
                // ...but no click!
                public void mouseReleased(final MouseEvent event)
                    {
                    if ((hostui.getInstrumentRearrangerMenu() != null)
                        && (SwingUtilities.isRightMouseButton(event))
                        && (!event.isShiftDown())
                        && (!event.isAltDown())
                        && (event.getClickCount() == 1)
                        && (tabbedpane.getSelectedIndex() >= 0))
                        {
                        try
                            {
                            final Rectangle rectTab;

                            rectTab = tabbedpane.getBoundsAt(tabbedpane.getSelectedIndex());

                            if (rectTab != null)
                                {
                                // Show a Popup Menu to allow Group restructuring
                                // Show the menu to the right of the Group Tab,
                                hostui.getGroupRearrangerMenu().show(hostui,
                                                                     tabbedpane,
                                                                     (int)(rectTab.getX() + rectTab.getWidth()),
                                                                     (int)rectTab.getY());
                                }
                            else
                                {
                                Toolkit.getDefaultToolkit().beep();
                                }
                            }

                        catch (final IndexOutOfBoundsException exception)
                            {
                            Toolkit.getDefaultToolkit().beep();
                            }
                        }
                    }
                });

            }
        else
            {
            LOGGER.error(SOURCE + "Called with Invalid Parameters");
            }
        }


    /***********************************************************************************************
     * Get the previously selected InstrumentSelector.
     *
     * @param hostui
     * @param groupids
     * @param selectorstable
     *
     * @return InstrumentSelector
     */

    private static InstrumentSelector getPreviousSelector(final ObservatoryUIInterface hostui,
                                                          final List<Integer> groupids,
                                                          final Hashtable<Integer, InstrumentSelector> selectorstable)
        {
        final String SOURCE = "ObservatoryUIHelper.getPreviousSelector() ";
        InstrumentSelector selector;
        final int intCurrentGroupID;

        selector = null;

        // The HostUI still thinks the previous selection is current
        intCurrentGroupID = hostui.getCurrentGroupID();

        // Is it a valid GroupID?
        // Other parameters were checked by the caller
        if ((groupids.size() > groupids.indexOf(intCurrentGroupID))
            && (groupids.get(groupids.indexOf(intCurrentGroupID)) != null))
            {
            // Retrieve the InstrumentSelector given the current GroupID,
            // i.e. the selection before the click
            selector = selectorstable.get(intCurrentGroupID);
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Current GroupID [groupid=" + intCurrentGroupID + "]");
            }

        return (selector);
        }


    /***********************************************************************************************
     * Get the selected InstrumentSelector.
     *
     * @param hostui
     * @param tabbedpane
     * @param groupids
     * @param groupsortindexes
     * @param sortindextogroupid
     * @param selectorstable
     *
     * @return InstrumentSelector
     */

    private static InstrumentSelector getSelectedSelector(final ObservatoryUIInterface hostui,
                                                          final JTabbedPane tabbedpane,
                                                          final List<Integer> groupids,
                                                          final List<Integer> groupsortindexes,
                                                          final Hashtable<Integer, Integer> sortindextogroupid,
                                                          final Hashtable<Integer, InstrumentSelector> selectorstable)
        {
        final String SOURCE = "ObservatoryUIHelper.getSelectedSelector() ";
        InstrumentSelector selector;
        final int intSelectedTabIndex;

        selector = null;

        // The ChangeEvent on the Tab has produced a new selection
        intSelectedTabIndex = tabbedpane.getSelectedIndex();

        // Returns -1 if there is no currently selected tab
        // Other parameters were checked by the caller
        if ((intSelectedTabIndex != -1)
            && (groupsortindexes.size() > intSelectedTabIndex)
            && (groupsortindexes.get(intSelectedTabIndex) != null))
            {
            final Integer intSelectedGroupSortIndex;
            final Integer intSelectedGroupID;

            // Beware! The Tabs are rendered in order of GroupSortIndex, not GroupID
            // Therefore the SelectedTabIndex indexes into the GroupSortIndexes
            // The SelectedTabIndexes will run {0...n-1} BUT the Group SortIndexes may not!
            // Therefore we need the SelectedGroupSortIndex BEFORE looking up the SelectedGroupID
            intSelectedGroupSortIndex = groupsortindexes.get(intSelectedTabIndex);
            intSelectedGroupID = sortindextogroupid.get(intSelectedGroupSortIndex);

            // Record the new Group selection
            hostui.setCurrentGroupID(intSelectedGroupID);

            // Retrieve the selected InstrumentSelector given the GroupID
            selector = selectorstable.get(intSelectedGroupID);
            }
        else
            {
            LOGGER.error(SOURCE + "Invalid Tab Index [index=" + intSelectedTabIndex + "]");
            }

        return (selector);
        }


    /***********************************************************************************************
     * Identify the ObservatoryClock and the ObservatoryLog Instruments, if present.
     *
     * @param instrument
     */

    public static void identifyClockAndLog(final ObservatoryUIInterface hostui,
                                           final ObservatoryInstrumentInterface instrument)
        {
        // Provide access to useful Instruments
        // This is the only place where these assignments are made...
        if ((hostui != null)
            && (instrument != null))
            {
            if ((!hostui.hasObservatoryClock())
                && (ObservatoryInstrumentHelper.isObservatoryClock(instrument)))
                {
                hostui.foundObservatoryClock(true);
                hostui.setObservatoryClock((ObservatoryClockInterface)instrument);
                LOGGER.login("ObservatoryClock identified");
                }
            else if ((hostui.getObservatoryLog() == null)
                && (ObservatoryInstrumentHelper.isObservatoryLog(instrument)))
                {
                hostui.setObservatoryLog((ObservatoryLogInterface)instrument);

                LOGGER.login("ObservatoryLog identified");
                }
            else
                {
                // Show some activity during a quiet period...
                LOGGER.login(instrument.getInstrument().getIdentifier() + " installed");
                }
            }
        else
            {
            LOGGER.error("ObservatoryUIHelper.identifyClockAndLog() Could not check for Clock and Log");
            }
        }


    /***********************************************************************************************
     * Show the count of Components on each Group InstrumentSelector.
     *
     * @param title
     * @param selectorstable
     */

    public static void showSelectorComponentCounts(final String title,
                                                   final Hashtable<Integer, InstrumentSelector> selectorstable)
        {
        LOGGER.log("\n" + title + " Group Selector Component Counts");

        if (selectorstable != null)
            {
            final Enumeration<Integer> enumGroupIDs;

            enumGroupIDs = selectorstable.keys();

            while (enumGroupIDs.hasMoreElements())
                {
                final Integer intGroupID;

                intGroupID = enumGroupIDs.nextElement();

                if (intGroupID != null)
                    {
                    final InstrumentSelector selector;

                    selector = selectorstable.get(intGroupID);

                    if (selector != null)
                        {
                        LOGGER.log("GroupID=" + intGroupID + " Count=" + selector.getSelectorPanelContainer().getComponentCount());
                        }
                    else
                        {
                        LOGGER.log("GroupID=" + intGroupID + "NULL SELECTOR");
                        }
                    }
                }
            }
        else
            {
            LOGGER.log("NULL SELECTORS TABLE");
            }
        }


    /***********************************************************************************************
     * Create a singleton GroupRearrangerMenu to allow Group Tab rearrangements.
     *
     * @param hostatom
     * @param hostui
     * @param font
     * @param color
     *
     * @return GroupRearrangerMenu
     */

    public static GroupRearrangerMenu createGroupRearrangerMenu(final AtomPlugin hostatom,
                                                                final ObservatoryUIInterface hostui,
                                                                final Font font,
                                                                final Color color)
        {
        final GroupRearrangerMenu popupMenuGroup;
        JMenuItem menuItem;

        popupMenuGroup = new GroupRearrangerMenu();
        popupMenuGroup.setFont(font);
        popupMenuGroup.setForeground(color);

        //------------------------------------------------------------------------------------------
        // Move Up

        menuItem = new JMenuItem(MENU_MOVE_UP);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_MOVE_UP));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        //menuItem.setToolTipText(TOOLTIP_GROUP_MOVE_UP);
        menuItem.setToolTipText(AWAITING_DEVELOPMENT);
        menuItem.setEnabled(false);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                }
            });

        popupMenuGroup.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Move Down

        menuItem = new JMenuItem(MENU_MOVE_DOWN);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_MOVE_DOWN));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        //menuItem.setToolTipText(TOOLTIP_GROUP_MOVE_DOWN);
        menuItem.setToolTipText(AWAITING_DEVELOPMENT);
        menuItem.setEnabled(false);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                }
            });

        popupMenuGroup.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Rename

        menuItem = new JMenuItem(MENU_GROUP_RENAME);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_GROUP_RENAME));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        //menuItem.setToolTipText(TOOLTIP_GROUP_RENAME);
        menuItem.setToolTipText(AWAITING_DEVELOPMENT);
        menuItem.setEnabled(false);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                }
            });

        popupMenuGroup.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Delete

        menuItem = new JMenuItem(MENU_GROUP_DELETE);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_GROUP_DELETE));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        //menuItem.setToolTipText(TOOLTIP_GROUP_DELETE);
        menuItem.setToolTipText(AWAITING_DEVELOPMENT);
        menuItem.setEnabled(false);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                }
            });

        popupMenuGroup.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Create

        menuItem = new JMenuItem(MENU_GROUP_CREATE);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_GROUP_CREATE));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        //menuItem.setToolTipText(TOOLTIP_GROUP_CREATE);
        menuItem.setToolTipText(AWAITING_DEVELOPMENT);
        menuItem.setEnabled(false);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                }
            });

        popupMenuGroup.add(menuItem);

        return (popupMenuGroup);
        }


    /***********************************************************************************************
     * Create a singleton InstrumentRearrangerMenu to allow Group restructuring.
     *
     * @param hostatom
     * @param hostui
     * @param font
     * @param color
     *
     * @return InstrumentRearrangerMenu
     */

    public static InstrumentRearrangerMenu createInstrumentRearrangerMenu(final AtomPlugin hostatom,
                                                                          final ObservatoryUIInterface hostui,
                                                                          final Font font,
                                                                          final Color color)
        {
        final InstrumentRearrangerMenu popupMenuInstrument;
        final List<Integer> listGroupIDs;
        JMenuItem menuItem;

        popupMenuInstrument = new InstrumentRearrangerMenu();
        popupMenuInstrument.setFont(font);
        popupMenuInstrument.setForeground(color);

        //------------------------------------------------------------------------------------------
        // Move Up

        menuItem = new JMenuItem(MENU_MOVE_UP);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_MOVE_UP));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        menuItem.setToolTipText(TOOLTIP_INSTRUMENT_MOVE_UP);
        menuItem.setEnabled(true);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final InstrumentSelector selector;
                final Vector<ObservatoryInstrumentInterface> vecInstrumentsOnSelector;
                final int intIndexInstrument;

                selector = hostui.getCurrentGroupInstrumentSelector();
                vecInstrumentsOnSelector = selector.getInstrumentsOnSelector();
                intIndexInstrument = vecInstrumentsOnSelector.indexOf(popupMenuInstrument.getObservatoryInstrument());

                // Can we move up one?
                if (intIndexInstrument > 0)
                    {
                    try
                        {
                        final ObservatoryInstrumentInterface previousInstrument;

                        previousInstrument = vecInstrumentsOnSelector.get(intIndexInstrument - 1);

                        // Swap the Instruments in the Selector
                        vecInstrumentsOnSelector.set(intIndexInstrument - 1, popupMenuInstrument.getObservatoryInstrument());
                        vecInstrumentsOnSelector.set(intIndexInstrument, previousInstrument);

                        // No need to update the Selection (GroupID hasn't changed)

                        // Rewrite SortIndexes of all Groups
                        rewriteAllGroupSortIndexes(hostui);

                        // Redisplay the current Group
                        selector.runUI();
                        }

                    catch (final ArrayIndexOutOfBoundsException exception)
                        {
                        LOGGER.error("ObservatoryUIHelper.createInstrumentRearrangerMenu() MoveUp "
                                        + METADATA_EXCEPTION
                                        + exception.getMessage()
                                        + TERMINATOR);
                        }
                    }
                else
                    {
                    // Can't Move Up
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });
        popupMenuInstrument.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Move Down

        menuItem = new JMenuItem(MENU_MOVE_DOWN);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_MOVE_DOWN));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        menuItem.setToolTipText(TOOLTIP_INSTRUMENT_MOVE_DOWN);
        menuItem.setEnabled(true);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final InstrumentSelector selector;
                final Vector<ObservatoryInstrumentInterface> vecInstrumentsOnSelector;
                final int intIndexSelectedInstrument;

                selector = hostui.getCurrentGroupInstrumentSelector();
                vecInstrumentsOnSelector = selector.getInstrumentsOnSelector();
                intIndexSelectedInstrument = vecInstrumentsOnSelector.indexOf(popupMenuInstrument.getObservatoryInstrument());

                // Can we move down one?
                if (intIndexSelectedInstrument < (vecInstrumentsOnSelector.size()-1))
                    {
                    try
                        {
                        final ObservatoryInstrumentInterface nextInstrument;

                        nextInstrument = vecInstrumentsOnSelector.get(intIndexSelectedInstrument + 1);

                        // Swap the Instruments in the Selector
                        vecInstrumentsOnSelector.set(intIndexSelectedInstrument, nextInstrument);
                        vecInstrumentsOnSelector.set(intIndexSelectedInstrument + 1, popupMenuInstrument.getObservatoryInstrument());

                        // No need to update the Selection (GroupID hasn't changed)

                        // Rewrite SortIndexes of all Groups
                        rewriteAllGroupSortIndexes(hostui);

                        // Redisplay the current Group
                        selector.runUI();
                        }

                    catch (final ArrayIndexOutOfBoundsException exception)
                        {
                        LOGGER.error("ObservatoryUIHelper.createInstrumentRearrangerMenu() MoveDown "
                                        + METADATA_EXCEPTION
                                        + exception.getMessage()
                                        + TERMINATOR);
                        }
                    }
                else
                    {
                    // Can't Move Down
                    Toolkit.getDefaultToolkit().beep();
                    }
                }
            });
        popupMenuInstrument.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Show the Move or Copy menu items

        popupMenuInstrument.addSeparator();
        listGroupIDs = hostui.getGroupIDs();

        // Sort the GroupIDs so that the Groups are rendered in sequence (should be unnecessary)
        Collections.sort(listGroupIDs);

        for (int i = 0;
             i < listGroupIDs.size();
             i++)
            {
            final Hashtable<Integer, Definition> tableGroupDefinitions;
            final int intGroupID;
            final Definition groupOnMenuItem;
            final JMenuItem item;

            tableGroupDefinitions = hostui.getGroupDefinitionsTable();
            intGroupID = listGroupIDs.get(i);
            groupOnMenuItem = tableGroupDefinitions.get(intGroupID);

            // The Groups are not filtered by User Role
            item = new JMenuItem(MENU_MOVE_TO + groupOnMenuItem.getName());
            item.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, groupOnMenuItem.getIconFilename()));
            item.setFont(font);
            item.setForeground(color);
            item.setToolTipText(groupOnMenuItem.getTooltip());
            item.setEnabled(true);

            item.addActionListener(new ActionListener()
                {
                public void actionPerformed(final ActionEvent event)
                    {
                    final Hashtable<Integer, Definition> tblGroupDefinitions;
                    final Definition selectedGroupDefinition;
                    final int intChoice;

                    tblGroupDefinitions = hostui.getGroupDefinitionsTable();
                    selectedGroupDefinition = tblGroupDefinitions.get(hostui.getCurrentGroupID());

                    // Do nothing if trying to move to the currently selected Group
                    if (!selectedGroupDefinition.getName().equals(groupOnMenuItem.getName()))
                        {
                        final String [] strMessage =
                            {
                            "Are you sure that you wish to Move or Copy the Instrument",
                            "from the " + selectedGroupDefinition.getName()
                                + " Group to the " + groupOnMenuItem.getName() + " Group?"
                            };
                        final Object[] options = {"Move", "Copy", "Cancel"};

                        intChoice = JOptionPane.showOptionDialog(null,
                                                                 strMessage,
                                                                 "Group Rearrangement",
                                                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 null,
                                                                 options,
                                                                 options[2]);
                        // YES = MOVE
                        if (intChoice == JOptionPane.YES_OPTION)
                            {
                            final Hashtable<Integer, List<Instrument>> tblGroupsToInstruments;
                            final List<Instrument> listCurrentGroupToInstruments;
                            final InstrumentSelector selectorInCurrentGroup;
                            final InstrumentSelector selectorInTargetGroup;
                            //final Vector<ObservatoryInstrumentInterface> vecInstrumentsOnCurrentSelector;
                            final ObservatoryInstrumentInterface movingInstrument;

                            // Retrieve the List of Instruments for the current Group, which may contain all NULLs
                            tblGroupsToInstruments = hostui.getGroupsToInstrumentsTable();
                            listCurrentGroupToInstruments = tblGroupsToInstruments.get(hostui.getCurrentGroupID());

                            // Find the visible Instruments
                            selectorInCurrentGroup = hostui.getCurrentGroupInstrumentSelector();
                            //vecInstrumentsOnCurrentSelector = selectorInCurrentGroup.getInstrumentsOnSelector();

                            // Save the Instrument associated with the menu, which is to be moved
                            movingInstrument = popupMenuInstrument.getObservatoryInstrument();

                            // Remove from current Group
                            if ((movingInstrument != null)
                                && (movingInstrument.getInstrument() != null)
                                && (listCurrentGroupToInstruments.contains(movingInstrument.getInstrument()))
                                && (selectorInCurrentGroup != null)
                                && (selectorInCurrentGroup.getInstrumentsOnSelector() != null)
                                && (selectorInCurrentGroup.getInstrumentsOnSelector().contains(movingInstrument)))
                                {
                                //System.out.println("removing instrument... " + movingInstrument.getInstrument().getIdentifier());

                                // Remove from current GroupToInstruments
                                listCurrentGroupToInstruments.remove(movingInstrument.getInstrument());

                                // Remove from selector in CurrentGroup
                                selectorInCurrentGroup.removeInstrumentFromSelector(movingInstrument);

                                // Set the selection to the first Instrument on the Selector
                                if (selectorInCurrentGroup.getInstrumentsOnSelector().size() > 0)
                                    {
                                    selectorInCurrentGroup.setSelectedInstrument(selectorInCurrentGroup.getInstrumentsOnSelector().get(0));
                                    }
                                else
                                    {
                                    selectorInCurrentGroup.setSelectedInstrument(null);
                                    }

                                // Redisplay the current Group
                                selectorInCurrentGroup.runUI();
                                }
                            else
                                {
                                LOGGER.error("ObservatoryUIHelper.createInstrumentRearrangerMenu() Instrument not found in selected Group");
                                }

                            // Find the target Group
                            selectorInTargetGroup = hostui.getInstrumentSelectorsTable().get(groupOnMenuItem.getGroupID());

                            // Add the Instrument to be moved to the end of the target Group
                            // but only if it is not already present
                            if (selectorInTargetGroup != null)
                                {
                                if ((selectorInTargetGroup.getInstrumentsOnSelector() != null)
                                && (!selectorInTargetGroup.getInstrumentsOnSelector().contains(movingInstrument))
                                && (movingInstrument != null)
                                && (movingInstrument.getInstrument() != null))
                                    {
                                    final List<Instrument> listTargetGroupToInstruments;

                                    //System.out.println("move instrument to target group " + groupOnMenuItem.getName());

                                    // Add to target GroupToInstruments
                                    listTargetGroupToInstruments = tblGroupsToInstruments.get(groupOnMenuItem.getGroupID());
                                    listTargetGroupToInstruments.add(movingInstrument.getInstrument());

                                    // Add to target Selector
                                    selectorInTargetGroup.addInstrumentToSelector(movingInstrument);
                                    }
                                }
                            else
                                {
                                // There is no Selector in the target Group
                                LOGGER.error("ObservatoryUIHelper.createInstrumentRearrangerMenu() There is no Selector in the target Group");
                                }

                            // Rewrite SortIndexes of all Groups
                            rewriteAllGroupSortIndexes(hostui);
                            }
                        else if (intChoice == JOptionPane.NO_OPTION) // NO = COPY
                            {
                            final Hashtable<Integer, List<Instrument>> tblGroupsToInstruments;
                            final InstrumentSelector selectorInTargetGroup;
                            final ObservatoryInstrumentInterface copyingInstrument;

                            // Retrieve the List of Instruments for the current Group, which may contain all NULLs
                            tblGroupsToInstruments = hostui.getGroupsToInstrumentsTable();

                            // Save the Instrument associated with the menu, which is to be copied
                            copyingInstrument = popupMenuInstrument.getObservatoryInstrument();

                            // Find the target Group
                            selectorInTargetGroup = hostui.getInstrumentSelectorsTable().get(groupOnMenuItem.getGroupID());

                            // Add the Instrument to be copied to the end of the target Group
                            // but only if it is not already present
                            if ((selectorInTargetGroup != null)
                                && (selectorInTargetGroup.getInstrumentsOnSelector() != null)
                                && (!selectorInTargetGroup.getInstrumentsOnSelector().contains(copyingInstrument))
                                && (copyingInstrument != null)
                                && (copyingInstrument.getInstrument() != null))
                                {
                                final List<Instrument> listTargetGroupToInstruments;

                                //System.out.println("copy instrument to target group " + groupOnMenuItem.getName());

                                // Add to target GroupToInstruments
                                listTargetGroupToInstruments = tblGroupsToInstruments.get(groupOnMenuItem.getGroupID());
                                listTargetGroupToInstruments.add(copyingInstrument.getInstrument());

                                // Add to target Selector
                                selectorInTargetGroup.addInstrumentToSelector(copyingInstrument);
                                }

                            // Rewrite SortIndexes of all Groups
                            rewriteAllGroupSortIndexes(hostui);
                            }
                        }
                    else
                        {
                        Toolkit.getDefaultToolkit().beep();
                        //System.out.println("can't Move or Copy to self, selected group =" + selectedGroupDefinition.getName()
                                           // + " click on target group " + groupOnMenuItem.getName());
                        }
                    }
                });

            popupMenuInstrument.add(item);
            }

        popupMenuInstrument.addSeparator();

        //------------------------------------------------------------------------------------------
        // Lock

        menuItem = new JMenuItem(MENU_LOCK_INSTRUMENT);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_LOCK_INSTRUMENT));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        menuItem.setToolTipText(TOOLTIP_INSTRUMENT_LOCK);
        menuItem.setEnabled(true);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final AttributesDocument.Attributes attributes;
                final ObservatoryInstrumentInterface lockInstrument;
                final boolean boolLocked;

                attributes = hostui.getAttributesDoc().getAttributes();

                // Save the Instrument associated with the menu, which is to be locked or unlocked
                lockInstrument = popupMenuInstrument.getObservatoryInstrument();

                boolLocked = InstrumentRearrangerMenu.isLocked(attributes,
                                                               hostui.getCurrentGroupID(),
                                                               lockInstrument);
//                System.out.println("Lock: [status=" + boolLocked
//                                        + "] [groupid=" + hostui.getCurrentGroupID()
//                                        + "] [instrument=" + lockInstrument.getInstrument().getIdentifier() + "]");

                // Toggle the lock status
                InstrumentRearrangerMenu.setLocked(attributes,
                                                   hostui.getCurrentGroupID(),
                                                   lockInstrument,
                                                   !boolLocked);
                }
            });

        popupMenuInstrument.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Clone

        menuItem = new JMenuItem(MENU_CLONE_INSTRUMENT);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_CLONE_INSTRUMENT));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        menuItem.setToolTipText(AWAITING_DEVELOPMENT);
        menuItem.setEnabled(false);
        popupMenuInstrument.add(menuItem);

        //------------------------------------------------------------------------------------------
        // Uninstall

        menuItem = new JMenuItem(MENU_UNINSTALL_INSTRUMENT);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_UNINSTALL_INSTRUMENT));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        menuItem.setToolTipText(AWAITING_DEVELOPMENT);
        menuItem.setEnabled(false);
        popupMenuInstrument.add(menuItem);

        popupMenuInstrument.addSeparator();

        //------------------------------------------------------------------------------------------
        // Save All Arrangements

        menuItem = new JMenuItem(MENU_SAVE_ALL_ARRANGEMENTS);
        menuItem.setIcon(RegistryModelUtilities.getAtomIcon(hostatom, ICON_SAVE_ARRANGEMENTS));
        menuItem.setFont(font);
        menuItem.setForeground(color);
        menuItem.setToolTipText(TOOLTIP_SAVE_ALL);
        menuItem.setEnabled(true);
        menuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final int intChoice;
                final String [] strMessage =
                    {
                    "Are you sure that you wish to save the Group arrangements?"
                    };

                intChoice = JOptionPane.showOptionDialog(null,
                                                         strMessage,
                                                         "Save All Instrument Group Arrangements",
                                                         JOptionPane.YES_NO_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null,
                                                         null,
                                                         null);
                if (intChoice == JOptionPane.YES_OPTION)
                    {
                    // Save instruments-attributes.xml
                    saveAttributes(hostui.getAttributesDoc());
                    }
                }
            });

        popupMenuInstrument.add(menuItem);

        return (popupMenuInstrument);
        }


    /***********************************************************************************************
     * Rewrite SortIndexes of all Groups.
     *
     * @param hostui
     */

    private static void rewriteAllGroupSortIndexes(final ObservatoryUIInterface hostui)
        {
        if ((hostui != null)
            && (hostui.getAttributesDoc() != null)
            && (hostui.getAttributesDoc().getAttributes() != null)
            && (hostui.getAttributesDoc().getAttributes().getConfigurationList() != null))
            {
            final AttributesDocument docAttributes;
            final List<Configuration> listConfigurations;
            final AttributesDocument.Attributes attributes;
            final List<Integer> listGroupIDs;

            docAttributes = hostui.getAttributesDoc();
            listConfigurations = docAttributes.getAttributes().getConfigurationList();
            attributes = AttributesDocument.Attributes.Factory.newInstance();
            listGroupIDs = hostui.getGroupIDs();

            // Iterate over the current Configuration of each individual Instrument
            for (int indexInstrument = 0;
                 indexInstrument < listConfigurations.size();
                 indexInstrument++)
                {
                final Configuration oldConfiguration;
                final Configuration newConfiguration;

                // Read the Configuration currently in the file
                oldConfiguration = listConfigurations.get(indexInstrument);

                //System.out.println("Processing Configuration for Instrument = " + oldConfiguration.getIdentifier());

                // The Identifier and UserRoles haven't changed as a result of the move operation,
                // so just copy them over...
                newConfiguration = attributes.addNewConfiguration();
                newConfiguration.setIdentifier(oldConfiguration.getIdentifier());
                newConfiguration.setUserRoles(oldConfiguration.getUserRoles());

                // Find if the current User has an appropriate Role for this Instrument Configuration
                if (isRoleValid(oldConfiguration, REGISTRY_MODEL.getLoggedInUser().getRole().getName()))
                    {
                    ObservatoryGroups newObservatoryGroups;
                    int intSortIndex;

                    newObservatoryGroups = null;

                    // Iterate over all Groups, to see which Selectors contain the target Instrument
                    for (int indexGroup = 0;
                         indexGroup < listGroupIDs.size();
                         indexGroup++)
                        {
                        final Integer intGroupID;
                        final InstrumentSelector selectorInTargetGroup;

                        // Find the target GroupID from the index
                        intGroupID = listGroupIDs.get(indexGroup);
                        //System.out.println("Searching in Group " + intGroupID);

                        // Get the List of Instruments on this Group's Selector
                        selectorInTargetGroup = hostui.getInstrumentSelectorsTable().get(intGroupID);

                        if (selectorInTargetGroup != null)
                            {
                            final Vector<ObservatoryInstrumentInterface> vecInstrumentsOnSelector;

                            vecInstrumentsOnSelector = selectorInTargetGroup.getInstrumentsOnSelector();

                            if ((vecInstrumentsOnSelector != null)
                                && (vecInstrumentsOnSelector.size() > 0))
                                {
                                boolean boolFoundInstrument;

                                intSortIndex = 0;
                                boolFoundInstrument = false;

                                // Search for the target Instrument in Selector
                                for (int indexSearchInstrument = 0;
                                     ((!boolFoundInstrument) && (indexSearchInstrument < vecInstrumentsOnSelector.size()));
                                     indexSearchInstrument++)
                                    {
                                    final ObservatoryInstrumentInterface instrument;

                                    instrument = vecInstrumentsOnSelector.get(indexSearchInstrument);

                                    if (instrument != null)
                                        {
                                        if ((instrument.getInstrument() != null)
                                            && (instrument.getInstrument().getIdentifier().equals(oldConfiguration.getIdentifier())))
                                            {
                                            final Group newGroup;

                                            // Create the Groups container only if required
                                            if (newConfiguration.getObservatoryGroups() == null)
                                                {
                                                newObservatoryGroups = newConfiguration.addNewObservatoryGroups();
                                                }

                                            // This Group contains the Instrument
                                            // in the Configuration currently being assembled
                                            if (newObservatoryGroups != null)
                                                {
                                                newGroup = newObservatoryGroups.addNewGroup();
                                                newGroup.setGroupID(intGroupID);
                                                newGroup.setSortIndex(intSortIndex);

                                                // See if this Instrument is locked for rearrangements
                                                // as defined in the OLD configuration Attributes
                                                if (InstrumentRearrangerMenu.isLocked(docAttributes.getAttributes(),
                                                                                      intGroupID,
                                                                                      instrument))
                                                    {
                                                    newGroup.setLocked(true);
                                                    }
                                                }

                                            // No need to search the rest
                                            //System.out.println("GROUPID=" + intGroupID + " SORTINDEX=" + intSortIndex + " FOUND " + instrument.getInstrument().getIdentifier());

                                            boolFoundInstrument = true;
                                            }
                                        else
                                            {
                                            //System.out.println("Skipping Instrument " + instrument.getInstrument().getIdentifier());
                                            }

                                        // Don't count any Null entries
                                        intSortIndex++;
                                        }
                                    }
                                }
                            else
                                {
                                //System.out.println("skipping empty selector");
                                }
                            }
                        else
                            {
                            //System.out.println("no selector");
                            }
                        //System.out.println("END INNER FOR");
                        }
                    //System.out.println("END OUTER FOR");
                    }
                else
                    {
                    // If the current User Role does not permit this Instrument,
                    // then we must copy over the existing ObservatoryGroups unchanged
                    // TODO Fix anomaly in Group SortIndex after rearrangements in different UserRoles?
                    newConfiguration.setObservatoryGroups(oldConfiguration.getObservatoryGroups());
                    }
                }

            // Finally, replace the Attributes with the new arrangement
            hostui.getAttributesDoc().setAttributes(attributes);
            }
        else
            {
            LOGGER.error("ObservatoryUIHelper.rewriteAllGroupSortIndexes() Unable to rewrite SortIndexes");
            }
        }


    /***********************************************************************************************
     * Write the Instrument Attributes XML file.
     *
     * @param doc
     */

    private static void saveAttributes(final AttributesDocument doc)
        {
        try
            {
            final String strFolderAttributes;
            final File file;
            final OutputStream outputStream;

            strFolderAttributes = InstallationFolder.getTerminatedUserDir()
                                   + ObservatoryInstrumentDAOInterface.PATHNAME_PLUGINS_OBSERVATORY
                                   + DataStore.CONFIG.getLoadFolder()
                                   + System.getProperty("file.separator")
                                   + Observatory.FILENAME_INSTRUMENTS_ATTRIBUTES;

            file = new File(strFolderAttributes);
            FileUtilities.overwriteFile(file);
            outputStream = new FileOutputStream(file);

            // Write the whole document (even if empty) to the output stream
            doc.save(outputStream, getXmlOptions(false));
            outputStream.flush();
            outputStream.close();
            }

        catch (final FileNotFoundException exception)
            {
            LOGGER.error("ObservatoryUIHelper.saveAttributes() Unable to save " + Observatory.FILENAME_INSTRUMENTS_ATTRIBUTES);
            }

        catch (final IOException exception)
            {
            LOGGER.error("ObservatoryUIHelper.saveAttributes() Unable to save " + Observatory.FILENAME_INSTRUMENTS_ATTRIBUTES);
            }
        }


    /***********************************************************************************************
     * Get the XmlOptions to use for saving.
     *
     * @param compressed
     *
     * @return XmlOptions
     */

    public static XmlOptions getXmlOptions(final boolean compressed)
        {
        final XmlOptions xmlOptions;

        xmlOptions = new XmlOptions();

        // If this option is set, the saver will try to use
        // the default namespace for the most commonly used URI.
        // If it is not set the saver will always created named prefixes.
        xmlOptions.setUseDefaultNamespace();

        // Causes the saver to reduce the number of namespace prefix declarations.
        // The saver will do this by passing over the document twice,
        // first to collect the set of needed namespace declarations,
        // and then second to actually save the document with the declarations collected at the root.
        xmlOptions.setSaveAggressiveNamespaces();

        // Reformat sensibly if not compressed
        if (!compressed)
            {
            // This option will cause the saver to reformat white space for easier reading
            xmlOptions.setSavePrettyPrint();
            xmlOptions.setSavePrettyPrintIndent(XML_INDENT);
            }

        // This option controls whether saving begins on the element or its contents
        xmlOptions.setSaveOuter();

        return (xmlOptions);
        }


    /***********************************************************************************************
     * Find if the current User has an appropriate Role for this Instrument Configuration.
     *
     * @param configuration
     * @param userrolename
     *
     * @return boolean
     */

    private static boolean isRoleValid(final Configuration configuration,
                                       final String userrolename)
        {
        final UserRoles userRoles;
        boolean boolRoleOk;

        // Get the UserRoles for this Configuration
        userRoles = configuration.getUserRoles();
        boolRoleOk = false;

        if (userRoles.getRoleNameList() != null)
            {
            final List<RoleName.Enum> listRoleNames;

            listRoleNames = userRoles.getRoleNameList();

            for (int i = 0;
                 ((!boolRoleOk) && (i < listRoleNames.size()));
                 i++)
                {
                final RoleName.Enum rolename;

                rolename = listRoleNames.get(i);

                if (rolename.toString().equals(userrolename))
                    {
                    // Found the User Role, so leave now
                    boolRoleOk = true;
                    }
                }
            }

        return (boolRoleOk);
        }


    /***********************************************************************************************
     * Add the specified Instrument to the Observatory, on the specified InstrumentSelector.
     * Initialise the Instrument.
     *
     * @param hostui
     * @param obsinstrument
     */

    public static void addInstrumentToObservatory(final ObservatoryUIInterface hostui,
                                                  final InstrumentSelector selector,
                                                  final ObservatoryInstrumentInterface obsinstrument)
        {
        final String SOURCE = "ObservatoryUIHelper.addInstrumentToObservatory() ";

        if ((hostui != null)
            && (selector != null)
            && (obsinstrument != null)
            && (obsinstrument.getInstrument() != null))
            {
            final Hashtable<Integer, List<Instrument>> hashGroupsToInstruments;
            final Hashtable<Integer, InstrumentSelector> hashInstrumentSelectors;

            //-----------------------------------------------------------------------------------------
            // Add to the master list in ObservatoryUI
            // There is only one reference here

            hostui.getObservatoryInstruments().add(obsinstrument);

            //-----------------------------------------------------------------------------------------
            // Add the Instrument to the CurrentGroup in the GroupsToInstrumentsTable

            if (hostui.getCurrentGroupID() >= 0)
                {
                final List<Instrument> listCurrentInstruments;

                hashGroupsToInstruments = hostui.getGroupsToInstrumentsTable();
                listCurrentInstruments = hashGroupsToInstruments.get(hostui.getCurrentGroupID());
                listCurrentInstruments.add(obsinstrument.getInstrument());
                }
            else
                {
                LOGGER.error(SOURCE + "Unable to add Instrument to GroupsToInstrumentsTable because there is no Group selection");
                }

            //-----------------------------------------------------------------------------------------
            // Add the Instrument to the current visible InstrumentSelector

            if (hostui.getCurrentGroupID() >= 0)
                {
                final InstrumentSelector selectorCurrent;

                hashInstrumentSelectors = hostui.getInstrumentSelectorsTable();
                selectorCurrent = hashInstrumentSelectors.get(hostui.getCurrentGroupID());
                selectorCurrent.addInstrumentToSelector(obsinstrument);
                }
            else
                {
                LOGGER.error(SOURCE + "Unable to add Instrument to Current InstrumentSelector because there is no Group selection");
                }

            //-----------------------------------------------------------------------------------------
            // Create the Instrument Configuration in Instrument Attributes

            if ((hostui.getAttributesDoc() != null)
                && (hostui.getAttributesDoc().getAttributes() != null)
                && (hostui.getAttributesDoc().getAttributes().getConfigurationList() != null)
                && (obsinstrument.getInstrument().getIdentifier() != null))
                {
                final AttributesDocument docAttributes;
                final List<Configuration> listConfigurations;
                final Configuration configurationToAdd;
                final UserRoles roles;
                final ObservatoryGroups groups;
                final Group group;

                docAttributes = hostui.getAttributesDoc();
                listConfigurations = docAttributes.getAttributes().getConfigurationList();
                configurationToAdd = Configuration.Factory.newInstance();
                configurationToAdd.setIdentifier(obsinstrument.getInstrument().getIdentifier());

                roles = configurationToAdd.addNewUserRoles();
                roles.addNewRoleName().setStringValue("Administrator");
                roles.addNewRoleName().setStringValue("Builder");
                roles.addNewRoleName().setStringValue("Simplified");
                roles.addNewRoleName().setStringValue("Developer");

                groups = configurationToAdd.addNewObservatoryGroups();
                group = groups.addNewGroup();
                group.setGroupID(hostui.getCurrentGroupID());
                group.setSortIndex(50);
                group.setLocked(false);

                if ((configurationToAdd != null)
                    && (XmlBeansUtilities.isValidXml(configurationToAdd)))
                    {
                    listConfigurations.add(configurationToAdd);
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Unable to add Configuration to Instrument Attributes");
                    }
                }

            //-----------------------------------------------------------------------------------------
            // Rewrite SortIndexes of all Groups

            rewriteAllGroupSortIndexes(hostui);

            obsinstrument.initialise();

            // Force the selection to the first Instrument on the Selector, if possible
            hostui.getCurrentGroupInstrumentSelector().setSelectedInstrument(null);

            // Redisplay the current Group
            hostui.getCurrentGroupInstrumentSelector().runUI();
            }
        else
            {
            LOGGER.error(SOURCE + "Instrument is NULL");
            }
        }


    /***********************************************************************************************
     * Remove the specified Instrument from the Observatory.
     *
     * @param hostui
     * @param obsinstrument
     */

    public static void removeInstrumentFromObservatory(final ObservatoryUIInterface hostui,
                                                       final ObservatoryInstrumentInterface obsinstrument)
        {
        final String SOURCE = "ObservatoryUIHelper.removeInstrumentFromObservatory() ";

        if ((hostui != null)
            && (obsinstrument != null)
            && (obsinstrument.getInstrument() != null))
            {
            final Hashtable<Integer, List<Instrument>> tblGroupsToInstruments;
            final Collection<List<Instrument>> collInstrumentsInGroups;
            final Iterator<List<Instrument>> iterInstrumentLists;
            final Hashtable<Integer, InstrumentSelector> hashInstrumentSelectors;
            final Collection<InstrumentSelector> collInstrumentSelectors;
            final Iterator<InstrumentSelector> iterInstrumentSelectors;

            //-----------------------------------------------------------------------------------------
            // Remove the Instrument from all InstrumentSelectors, including the current visible Group

            hashInstrumentSelectors = hostui.getInstrumentSelectorsTable();
            collInstrumentSelectors = hashInstrumentSelectors.values();
            iterInstrumentSelectors = collInstrumentSelectors.iterator();

            while (iterInstrumentSelectors.hasNext())
                {
                final InstrumentSelector instrumentSelector;

                instrumentSelector = iterInstrumentSelectors.next();

                if (instrumentSelector != null)
                    {
                    instrumentSelector.removeInstrumentFromSelector(obsinstrument);
                    }
                }

            //-------------------------------------------------------------------------------------
            // Remove the Instrument from all Lists in the GroupsToInstrumentsTable

            tblGroupsToInstruments = hostui.getGroupsToInstrumentsTable();
            collInstrumentsInGroups = tblGroupsToInstruments.values();
            iterInstrumentLists = collInstrumentsInGroups.iterator();

            while (iterInstrumentLists.hasNext())
                {
                final List<Instrument> listInstruments;

                listInstruments = iterInstrumentLists.next();

                if (listInstruments != null)
                    {
                    listInstruments.remove(obsinstrument.getInstrument());
                    }
                }

            //-------------------------------------------------------------------------------------
            // Remove the Instrument Configuration in Instrument Attributes

            if ((hostui.getAttributesDoc() != null)
                && (hostui.getAttributesDoc().getAttributes() != null)
                && (hostui.getAttributesDoc().getAttributes().getConfigurationList() != null)
                && (obsinstrument.getInstrument().getIdentifier() != null))
                {
                final AttributesDocument docAttributes;
                final List<Configuration> listConfigurations;
                Configuration configurationToRemove;

                docAttributes = hostui.getAttributesDoc();
                listConfigurations = docAttributes.getAttributes().getConfigurationList();
                configurationToRemove = null;

                for (int intConfigIndex = 0;
                     ((intConfigIndex < listConfigurations.size())
                        && (configurationToRemove == null));
                     intConfigIndex++)
                    {
                    final Configuration configuration;

                    configuration = listConfigurations.get(intConfigIndex);

                    if (obsinstrument.getInstrument().getIdentifier().equals(configuration.getIdentifier()))
                        {
                        configurationToRemove = configuration;
                        }
                    }

                if (configurationToRemove != null)
                    {
                    listConfigurations.remove(configurationToRemove);
                    }
                }

            //-------------------------------------------------------------------------------------
            // Remove from the master list in ObservatoryUI
            // There is only one reference here

            hostui.getObservatoryInstruments().remove(obsinstrument);

            // Rewrite SortIndexes of all Groups
            rewriteAllGroupSortIndexes(hostui);

            // Force the selection to the first Instrument on the Selector, if possible
            hostui.getCurrentGroupInstrumentSelector().setSelectedInstrument(null);

            // Redisplay the current Group
            hostui.getCurrentGroupInstrumentSelector().runUI();
            }
        else
            {
            LOGGER.error(SOURCE + "Instrument is NULL");
            }
        }
    }
