package org.lmn.fc.model.registry.impl;

import org.lmn.fc.common.comparators.RootPluginByName;
import org.lmn.fc.common.comparators.RootPluginByResourceKey;
import org.lmn.fc.common.comparators.RootPluginBySortOrder;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.resources.impl.ResourceData;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;


@SuppressWarnings({"MethodMayBeStatic", "MagicNumber"})

/***************************************************************************************************
 * Assemble beans from the Registry for a User.
 */


public final class BeanAssembler implements FrameworkConstants,
                                            FrameworkStrings,
                                            FrameworkMetadata,
                                            ResourceKeys
    {
    private static final ExpanderFactory EXPANDER_FACTORY = ExpanderFactory.getInstance();
    private static final RegistryPlugin REGISTRY = Registry.getInstance();
    private static final RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();
    private static final Logger LOGGER = Logger.getInstance();

    private volatile static BeanAssembler ASSEMBLER_INSTANCE;


    /***********************************************************************************************
     * The BeanAssembler is a Singleton!
     *
     * @return BeanAssembler
     */

    public static BeanAssembler getInstance()
        {
        if (ASSEMBLER_INSTANCE == null)
            {
            synchronized (BeanAssembler.class)
                {
                if (ASSEMBLER_INSTANCE == null)
                    {
                    ASSEMBLER_INSTANCE = new BeanAssembler();
                    }
                }
            }

        return (ASSEMBLER_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the BeanAssembler.
     */

    private BeanAssembler()
        {
        }


    /***********************************************************************************************
     * Assemble a set of Beans appropriate to the Role of the specified User.
     *
     * @param user
     *
     * @return boolean
     */

    public final boolean assembleBeansForUser(final UserPlugin user)
        {
        boolean boolSuccess;

        if ((user == null)
            || (!user.validatePlugin()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        boolSuccess = true;

        try
            {
            // We assume that *all* UserRoles can see the Framework root,
            // otherwise there's not much point!
            if (!REGISTRY.getFramework().getUserRoles().contains(user.getRole()))
                {
                LOGGER.error("assembleBeansForUser() Role missing" + SPACE + EXCEPTION_PARAMETER_INVALID);
                }

            // This resets the size of the Registry to zero and removes any previous configuration
            REGISTRY_MODEL.initialiseModel(REGISTRY.getFramework());

            // Set the Framework as the root node
            // Recursively traverse the entire tree under each Plugin
            // Attach all items with the appropriate permissions and ResourceKey
            assemblePlugin(REGISTRY.getFramework(), user);

            // Add the Users expander for UserViewers only
            // This is not user-specific information, and so is retrieved directly from the Registry
// TODO REVIEW           if (user.getRole().isUserViewer())
//                {
//                addUserNodes(REGISTRY_MODEL.getRootNode());
//                }

            // Add the following for Developers only
            // This is not user-specific information, and so is retrieved directly from the Registry
            //Todo change to Developer
//  TODO REVIEW          if (RoleName.ADMINISTRATOR.toString().equals(user.getRole().getName()))
//                {
//                addDeveloperNodes(REGISTRY_MODEL.getRootNode());
//                }
            }

        catch (FrameworkException exception)
            {
            LOGGER.error("Unable to assemble beans for user "
                            + user.getName()
                            + " Exception="
                            + exception.getMessage());
            boolSuccess = false;
            }

        return (boolSuccess);
        }


    /***********************************************************************************************
     * Assemble the various parts of the specified AtomPlugin.
     * The parent-child bean relationship already exists in the bean pool,
     * we just need to filter this with the User Role, and link up the various host nodes.
     * So for each Task already attached to the Atom, see if this User is allowed to see it.
     * This method is used recursively!
     *
     * @param plugin
     * @param user
     */

    private void assemblePlugin(final AtomPlugin plugin,
                                final UserPlugin user)
        {
        if ((plugin == null)
            || (!plugin.validatePlugin())
            || (user == null)
            || (!user.validatePlugin()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Wipe out anything from a previous login
        plugin.getPluginExpander().removeAllChildren();
        plugin.getTaskExpander().removeAllChildren();
        plugin.getExceptionExpander().removeAllChildren();
        plugin.getPropertyExpander().removeAllChildren();
        plugin.getStringExpander().removeAllChildren();
        plugin.getQueryExpander().removeAllChildren();

        // Child Atoms of this Plugin with the appropriate User permissions
        if (user.getRole().isAtomViewer())
            {
            addPluginAtoms(plugin, user);
            }

        // Attach all Plugin Tasks with the appropriate User permissions
        if (user.getRole().isTaskViewer())
            {
            addPluginTasks(plugin, user);
            }

        // Add the Resources for this Plugin
        if (user.getRole().isResourceViewer())
            {
            addPluginResources(plugin);
            }
        }


    /***********************************************************************************************
     * Add the child Atoms of this Plugin.
     *
     * @param plugin
     * @param user
     */

    private void addPluginAtoms(final AtomPlugin plugin,
                                final UserPlugin user)
        {
        final Vector<RootPlugin> atoms;
        final Iterator<RootPlugin> iterAtoms;
        final Vector<RootPlugin> permittedAtoms;
        final Iterator<RootPlugin> iterPermittedAtoms;

        if ((plugin == null)
            || (!plugin.validatePlugin())
            || (plugin.getAtoms() == null)
            || (plugin.getPluginExpander() == null)
            || (!plugin.getPluginExpander().getAllowsChildren())
            || (user == null)
            || (!user.validatePlugin()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        atoms = plugin.getAtoms();
        iterAtoms = atoms.iterator();
        permittedAtoms = new Vector<RootPlugin>(20);

        while (iterAtoms.hasNext())
            {
            final AtomPlugin atomchild = (AtomPlugin) iterAtoms.next();

            if (atomchild.getUserRoles().contains(user.getRole()))
                {
                permittedAtoms.add(atomchild);
                REGISTRY_MODEL.incSize();
                }
            }

        // Sort the Atoms by SortOrder
        Collections.sort(permittedAtoms, new RootPluginBySortOrder());

        // Iterate over the sorted list, and add the Atoms to the navigation tree expander
        iterPermittedAtoms = permittedAtoms.iterator();

        while (iterPermittedAtoms.hasNext())
            {
            final AtomPlugin permittedAtom = (AtomPlugin)iterPermittedAtoms.next();
            plugin.getPluginExpander().add(permittedAtom.getHostTreeNode());

            // Recursively traverse into each Atom's children
            assemblePlugin(permittedAtom, user);
            }
        }


    /***********************************************************************************************
     * Add the Tasks for this Plugin.
     *
     * @param atom
     * @param user
     */

    private void addPluginTasks(final AtomPlugin atom,
                                final UserPlugin user)
        {
        final Vector<RootPlugin> tasks;
        final Iterator<RootPlugin> iterTasks;
        final Vector<RootPlugin> chosenTasks;
        final Iterator<RootPlugin> iterChosenTasks;
        final JMenu menuAtomTasks;

        if ((atom == null)
            || (atom.getTaskExpander() == null)
            || (!atom.getTaskExpander().getAllowsChildren())
            || (user == null)
            || (!user.validatePlugin()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        tasks = atom.getTasks();
        iterTasks = tasks.iterator();
        chosenTasks = new Vector<RootPlugin>(tasks.size());

        while (iterTasks.hasNext())
            {
            final TaskPlugin task = (TaskPlugin) iterTasks.next();

            if (task.getUserRoles().contains(user.getRole()))
                {
                chosenTasks.add(task);
                REGISTRY_MODEL.incSize();
                }
            }

        // Sort the Tasks by SortOrder
        Collections.sort(chosenTasks, new RootPluginBySortOrder());

        // Create a menu for the Tasks
        menuAtomTasks = new JMenu(atom.getLevel());

        // Iterate over the sorted list, and add the Tasks to the expander
        iterChosenTasks = chosenTasks.iterator();

        while (iterChosenTasks.hasNext())
            {
            final RootPlugin plugin;
            final JMenuItem menuItem;

            plugin = iterChosenTasks.next();
            atom.getTaskExpander().add(plugin.getHostTreeNode());
            menuItem = new JMenuItem(plugin.getName());
            menuAtomTasks.add(menuItem);
            }
        }


    /***********************************************************************************************
     * Add the Resources for the specified Plugin.
     *
     * @param plugin
     */

    private void addPluginResources(final AtomPlugin plugin)
        {
        if (plugin == null)
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        addResourcesToExpander(plugin.getExceptions(), plugin.getExceptionExpander());
        addResourcesToExpander(plugin.getProperties(), plugin.getPropertyExpander());
        addResourcesToExpander(plugin.getStrings(), plugin.getStringExpander());
        addResourcesToExpander(plugin.getQueries(), plugin.getQueryExpander());
        }


    /***********************************************************************************************
     * Add the specified Resources to the specified Expander.
     *
     * @param resources
     * @param rootexpander
     */

    private void addResourcesToExpander(final Vector<ResourcePlugin> resources,
                                        final DefaultMutableTreeNode rootexpander)
        {
        final Iterator<ResourcePlugin> iterResources;
        final Vector<ResourcePlugin> sortedResources;
        final Iterator<ResourcePlugin> iterSortedResources;
        final Hashtable<String, DefaultMutableTreeNode> mapExpanders;

        if ((resources == null)
            || (rootexpander == null)
            || (!rootexpander.getAllowsChildren()))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        if (!resources.isEmpty())
            {
            // Add the 'root' expander to the map
            mapExpanders = new Hashtable<String, DefaultMutableTreeNode>(10);
            mapExpanders.put(RegistryModelPlugin.DELIMITER_RESOURCE, rootexpander);

            // Obtain the required ResourcePlugins
            sortedResources = new Vector<ResourcePlugin>(resources.size());
            iterResources = resources.iterator();

            while (iterResources.hasNext())
                {
                sortedResources.add(iterResources.next());
                REGISTRY_MODEL.incSize();
                }

            // Sort the ResourcePlugins by ResourceKey
            Collections.sort(sortedResources, new RootPluginByResourceKey());

            // Iterate over the sorted list,
            // and add the Resources to the Resources expander one by one
            iterSortedResources = sortedResources.iterator();

            while (iterSortedResources.hasNext())
                {
                // Retrieve each Resource in the list
                // Add each one to the appropriate expander
                // The 'root' expander is the first item in the map, keyed with DELIMITER_RESOURCE
                addResourcePlugin(mapExpanders, rootexpander, iterSortedResources.next());

                // Debug the map after each traversal
                //debugExpanderMap(mapExpanders);
                }
            }
        else
            {
            // ToDo indicate empty with a different expander?
            }
        }


    /***********************************************************************************************
     * Add the specified ResourcePlugin to the appropriate expander in the map,
     * starting at the specified root expander.
     * If an expander does not exist, create as many in the chain as are required.
     *
     * @param expanders
     * @param plugin
     */

    private void addResourcePlugin(final Hashtable<String, DefaultMutableTreeNode> expanders,
                                   final DefaultMutableTreeNode rootexpander,
                                   final ResourcePlugin plugin)
        {
        final String strRequiredExpanderKey;

        // Create the required key for the expander for this Resource
        strRequiredExpanderKey = ResourceData.createExpanderKey(plugin).toString();
//        LOGGER.debug("----->Adding resource {" + plugin.getPathname() + "}");
//        LOGGER.debug("toString=" + ((ResourcePlugin)(plugin.getHostTreeNode().getUserObject())).toString());
//        LOGGER.debug("Required expander {" + strRequiredExpanderKey + "}");

        if (!expanders.containsKey(strRequiredExpanderKey))
            {
            final List<String> listExpanderKeys;

//            LOGGER.debug("The expander key is not yet in the map...");

            // Obtain a *copy* of the list of ResourceKeys for this plugin
            // because we need to remove the leaf node at the end
            listExpanderKeys = new ArrayList<String>(plugin.getResourceKeys());

            if ((listExpanderKeys.size() > 1)
                || (!RegistryModelPlugin.DELIMITER_RESOURCE.equals(listExpanderKeys.get(0))))
                {
                final Iterator<String> iterKeys;
                final StringBuffer bufferKey;
                DefaultMutableTreeNode parentexpander;
                boolean boolFoundExpander;

                // Exclude the leaf node
                listExpanderKeys.remove(listExpanderKeys.size() - 1);
                iterKeys = listExpanderKeys.iterator();
                bufferKey = new StringBuffer(EMPTY_STRING);
                boolFoundExpander = false;

                // Start by attaching nodes to the root expander
                parentexpander = rootexpander;

                // Traverse the expander chain...
                // Make a series of expanders, excluding the last leaf node
                // Add each one to the hashtable of expanders, and to its parent node
                // Each expander must allow children
                // Remember that higher level expanders may already exist
                while ((iterKeys.hasNext())
                    && (!boolFoundExpander))
                    {
                    final String strKey;

                    strKey = iterKeys.next();
                    bufferKey.append(strKey);

                    if (!expanders.containsKey(bufferKey.toString()))
                        {
                        final DefaultMutableTreeNode childexpander = new DefaultMutableTreeNode();
                        childexpander.setAllowsChildren(true);
                        // ToDo path and icon....
                        final AtomPlugin userObject = EXPANDER_FACTORY.getExpander("path",
                                                                       strKey,
                                                                       "icon");
                        childexpander.setUserObject(userObject);
                        parentexpander.add(childexpander);

                        // Keep this new expander in the map
                        expanders.put(bufferKey.toString(), childexpander);
                        }

                    // Check again to see if we now have all we need...
                    if (expanders.containsKey(strRequiredExpanderKey))
                        {
                        boolFoundExpander = true;
                        }
                    else
                        {
                        // Reset the parent expander for the next pass
                        parentexpander = expanders.get(bufferKey.toString());

                        // Form the key for the next level...
                        bufferKey.append(RegistryModelPlugin.DELIMITER_RESOURCE);
                        }
                    }
                }
            else
                {
                // Something has gone wrong with the key!
                LOGGER.error("Incorrect ResourceKey {" + listExpanderKeys.get(0) + "}");
                throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
                }
            }

        // Double check that we now have the right key in the map
        if (expanders.containsKey(strRequiredExpanderKey))
            {
            final DefaultMutableTreeNode expander;

            // We have the expander, so just add the leaf node
            expander = expanders.get(strRequiredExpanderKey);
            expander.add(plugin.getHostTreeNode());
//            LOGGER.debug("Added plugin leaf node to existing expander from map name=" + plugin.getName() + "  key=" + strRequiredExpanderKey);
//            LOGGER.debug("user object=" + ((ResourcePlugin)(plugin.getHostTreeNode().getUserObject())).getPathname());
//            LOGGER.debug("toString=" + ((ResourcePlugin)(plugin.getHostTreeNode().getUserObject())).toString());
            }
        else
            {
            // Something went very wrong!
            LOGGER.error("Unable to add Resources to tree expander");
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Debug the expander map.
     *
     * @param expanders
     */

    private void debugExpanderMap(final Hashtable<String, DefaultMutableTreeNode> expanders)
        {
        final List<String> listSortedKeys;
        final Enumeration<String> enumKeys;
        final Iterator<String> iterSortedKeys;

        listSortedKeys = new ArrayList<String>(expanders.size());
        enumKeys = expanders.keys();

        while(enumKeys.hasMoreElements())
            {
            listSortedKeys.add(enumKeys.nextElement());
            }

        Collections.sort(listSortedKeys);
        iterSortedKeys = listSortedKeys.iterator();

        LOGGER.debug("Resource expander keys");

        while (iterSortedKeys.hasNext())
            {
            LOGGER.debug(INDENT + iterSortedKeys.next());

            }
        }


    /***********************************************************************************************
     * Add the Users.
     */

    private void addUserNodes(final DefaultMutableTreeNode node)
        {
        final DefaultMutableTreeNode nodeUsersExpander;
        final Vector<RootPlugin> users;
        final Enumeration<UserPlugin> enumUsers;
        final Iterator<RootPlugin> iterUsers;

        nodeUsersExpander = new DefaultMutableTreeNode();
        // ToDo path and icon....
        nodeUsersExpander.setUserObject(EXPANDER_FACTORY.getExpander("path",
                                                                     ExpanderFactory.EXPANDER_USERS,
                                                                     "icon"));
        nodeUsersExpander.setAllowsChildren(true);
        node.add(nodeUsersExpander);

        // Record the fact that we need to remove this node at the next login...
        //REGISTRY_MODEL.getExtraNodes().add(nodeUsersExpander);

        users = new Vector<RootPlugin>(20);
        enumUsers = REGISTRY.getUsers().elements();

        while (enumUsers.hasMoreElements())
            {
            users.add(enumUsers.nextElement());
            REGISTRY_MODEL.incSize();
            }

        // Sort the Users by Name
        Collections.sort(users, new RootPluginByName());

        // Iterate over the sorted list, and add the Users to the expander
        iterUsers = users.iterator();

        while (iterUsers.hasNext())
            {
            nodeUsersExpander.add(iterUsers.next().getHostTreeNode());
            }
        }


    /***********************************************************************************************
     * Show nodes which may be useful for developers.
     */

    private void addDeveloperNodes(final DefaultMutableTreeNode node)
        {
        final DefaultMutableTreeNode nodeRolesExpander;
        final Vector<RootPlugin> roles;
        final Iterator<RootPlugin> iterRoles;
        final DefaultMutableTreeNode nodeCountriesExpander;
        final Vector<RootPlugin> countries;
        final Iterator<RootPlugin> iterCountries;
        final DefaultMutableTreeNode nodeLanguagesExpander;
        final Vector<RootPlugin> languages;
        final Iterator<RootPlugin> iterLanguages;
        final DefaultMutableTreeNode nodeLookAndFeelsExpander;
        final Vector<RootPlugin> lookandfeels;
        final Iterator<RootPlugin> iterLookAndFeels;
//        final DefaultMutableTreeNode nodeDataTypesExpander;
//        final Vector<RootPlugin> datatypes;
//        final Iterator<RootPlugin> iterDataTypes;

        //------------------------------------------------------------------------------------------
        // For Developers only, add the Roles expander

        nodeRolesExpander = new DefaultMutableTreeNode();
        // ToDo path and icon....
        nodeRolesExpander.setUserObject(EXPANDER_FACTORY.getExpander("path",
                                                                     ExpanderFactory.EXPANDER_ROLES,
                                                                     "icon"));
        nodeRolesExpander.setAllowsChildren(true);
        node.add(nodeRolesExpander);

        // Record the fact that we need to remove this node at the next login...
        //REGISTRY_MODEL.getExtraNodes().add(nodeRolesExpander);

        roles = new Vector<RootPlugin>(20);
        final Enumeration<RolePlugin> enumRoles = REGISTRY.getRoles().elements();

        while (enumRoles.hasMoreElements())
            {
            roles.add(enumRoles.nextElement());
            REGISTRY_MODEL.incSize();
            }

        // Sort the Roles by Name
        Collections.sort(roles, new RootPluginByName());

        // Iterate over the sorted list, and add the Roles to the expander
        iterRoles = roles.iterator();

        while (iterRoles.hasNext())
            {
            nodeRolesExpander.add(iterRoles.next().getHostTreeNode());
            }

        //------------------------------------------------------------------------------------------
        // For Developers only, add the Countries expander

        nodeCountriesExpander = new DefaultMutableTreeNode();
        // ToDo path and icon....
        nodeCountriesExpander.setUserObject(EXPANDER_FACTORY.getExpander("path",
                                                                         ExpanderFactory.EXPANDER_COUNTRIES,
                                                                         "icon"));
        nodeCountriesExpander.setAllowsChildren(true);
        node.add(nodeCountriesExpander);

        // Record the fact that we need to remove this node at the next login...
        //REGISTRY_MODEL.getExtraNodes().add(nodeCountriesExpander);

        countries = new Vector<RootPlugin>(20);
        final Enumeration<CountryPlugin> enumCountries = REGISTRY.getCountries().elements();

        while (enumCountries.hasMoreElements())
            {
            countries.add(enumCountries.nextElement());
            REGISTRY_MODEL.incSize();
            }

        // Sort the Countries by Name
        Collections.sort(countries, new RootPluginByName());

        // Iterate over the sorted list, and add the Countries to the expander
        iterCountries = countries.iterator();

        while (iterCountries.hasNext())
            {
            nodeCountriesExpander.add(iterCountries.next().getHostTreeNode());
            }

        //------------------------------------------------------------------------------------------
        // For Developers only, add the Languages expander

        nodeLanguagesExpander = new DefaultMutableTreeNode();
        // ToDo path and icon....
        nodeLanguagesExpander.setUserObject(EXPANDER_FACTORY.getExpander("path",
                                                                         ExpanderFactory.EXPANDER_LANGUAGES,
                                                                         "icon"));
        nodeLanguagesExpander.setAllowsChildren(true);
        node.add(nodeLanguagesExpander);

        // Record the fact that we need to remove this node at the next login...
        //REGISTRY_MODEL.getExtraNodes().add(nodeLanguagesExpander);

        languages = new Vector<RootPlugin>(200);
        final Enumeration<LanguagePlugin> enumLanguages = REGISTRY.getLanguages().elements();

        while (enumLanguages.hasMoreElements())
            {
            languages.add(enumLanguages.nextElement());
            REGISTRY_MODEL.incSize();
            }

        // Sort the Languages by Name
        Collections.sort(languages, new RootPluginByName());

        // Iterate over the sorted list, and add the Languages to the expander
        iterLanguages = languages.iterator();

        while (iterLanguages.hasNext())
            {
            nodeLanguagesExpander.add(iterLanguages.next().getHostTreeNode());
            }

        //------------------------------------------------------------------------------------------
        // For Developers only, add the LookAndFeels expander

        nodeLookAndFeelsExpander = new DefaultMutableTreeNode();
        // ToDo path and icon....
        nodeLookAndFeelsExpander.setUserObject(EXPANDER_FACTORY.getExpander("path",
                                                                            ExpanderFactory.EXPANDER_LOOKANDFEELS,
                                                                            "icon"));
        nodeLookAndFeelsExpander.setAllowsChildren(true);
        node.add(nodeLookAndFeelsExpander);

        // Record the fact that we need to remove this node at the next login...
        //REGISTRY_MODEL.getExtraNodes().add(nodeLookAndFeelsExpander);

        lookandfeels = new Vector<RootPlugin>(200);
        final Enumeration<LookAndFeelPlugin> enumLookAndFeels = REGISTRY.getLookAndFeels().elements();

        while (enumLookAndFeels.hasMoreElements())
            {
            lookandfeels.add(enumLookAndFeels.nextElement());
            REGISTRY_MODEL.incSize();
            }

        // Sort the LookAndFeels by Name
        Collections.sort(lookandfeels, new RootPluginByName());

        // Iterate over the sorted list, and add the LookAndFeels to the expander
        iterLookAndFeels = lookandfeels.iterator();

        while (iterLookAndFeels.hasNext())
            {
            nodeLookAndFeelsExpander.add(iterLookAndFeels.next().getHostTreeNode());
            }

        //------------------------------------------------------------------------------------------
        // For Developers only, add the DataTypes expander

//        nodeDataTypesExpander = new DefaultMutableTreeNode();
//        // ToDo path and icon....
//        nodeDataTypesExpander.setUserObject(EXPANDER_FACTORY.getExpander("path",
//                                                                         ExpanderFactory.EXPANDER_DATATYPES,
//                                                                         "icon"));
//        nodeDataTypesExpander.setAllowsChildren(true);
//        node.add(nodeDataTypesExpander);
//
//        // Record the fact that we need to remove this node at the next login...
//        //REGISTRY_MODEL.getExtraNodes().add(nodeDataTypesExpander);
//
//        datatypes = new Vector<RootPlugin>(200);
//        final Enumeration<DataTypeParserInterface> enumDataTypes = REGISTRY.getDataTypes().elements();
//
//        while (enumDataTypes.hasMoreElements())
//            {
//            datatypes.add(enumDataTypes.nextElement());
//            REGISTRY_MODEL.incSize();
//            }
//
//        // Sort the DataTypes by Name
//        Collections.sort(datatypes, new RootPluginByName());
//
//        // Iterate over the sorted list, and add the DataTypes to the expander
//        iterDataTypes = datatypes.iterator();
//
//        while (iterDataTypes.hasNext())
//            {
//            nodeDataTypesExpander.add(iterDataTypes.next().getHostTreeNode());
//            }
        }
    }
