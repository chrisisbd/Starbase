//--------------------------------------------------------------------------------------------------
// Revision History
//
//  19-09-03    LMN created file
//  24-10-03    LMN added EditorUtilities
//  27-10-03    LMN added Editor classname
//  11-11-03    LMN extracted class RootData
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.resources.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.impl.NullData;
import org.lmn.fc.model.registry.NavigationUtilities;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.resources.ResourcePlugin;
import org.lmn.fc.model.root.RootType;
import org.lmn.fc.model.root.impl.UserObject;
import org.lmn.fc.model.xmlbeans.exceptions.ExceptionResource;
import org.lmn.fc.model.xmlbeans.properties.PropertyResource;
import org.lmn.fc.model.xmlbeans.queries.QueryResource;
import org.lmn.fc.model.xmlbeans.strings.StringResource;

import javax.swing.*;
import java.awt.*;


/***************************************************************************************************
 * ResourceData.
 */

public abstract class ResourceData extends UserObject
                                   implements ResourcePlugin
    {
    private AtomPlugin hostPlugin;

    private String strName;

    private String strResourceKey;
    private String strIconFilename;

    private boolean boolInstalled;
    private Object objResource;             // The Resource wrapped in an object
    private short shortSortOrder;



    /***********************************************************************************************
     * Create the ResourceKey by concatenating the individual keys for each level.
     *
     * @param keys
     *
     * @return StringBuffer
     */

    public static StringBuffer createResourceKey(final java.util.List<String> keys)
        {
        if ((keys != null)
            && (keys.size() > 0))
            {
            return (concatenateKeys(keys, keys.size()));
            }
        else
            {
            return (new StringBuffer(RegistryModelPlugin.DELIMITER_RESOURCE));
            }
        }


    /***********************************************************************************************
     * Create a key for a Resource Expander for the specified Plugin.
     * The key is made up of all individual keys except the last, which is the ResourceName.
     *
     * @param plugin
     *
     * @return StringBuffer
     */

    public static StringBuffer createExpanderKey(final ResourcePlugin plugin)
        {
        if ((plugin != null)
            && (plugin.getResourceKeys() != null)
            && (plugin.getResourceKeys().size() > 0))
            {
            return (concatenateKeys(plugin.getResourceKeys(),
                                    plugin.getResourceKeys().size() - 1));
            }
        else
            {
            return (new StringBuffer(RegistryModelPlugin.DELIMITER_RESOURCE));
            }
        }


    /***********************************************************************************************
     * A utility to concatenate ResourceKeys.
     *
     * @param keys
     * @param count
     *
     * @return StringBuffer
     */

    private static StringBuffer concatenateKeys(final java.util.List<String> keys,
                                                final int count)
        {
        final StringBuffer bufferKey;

        bufferKey = new StringBuffer();

        if ((keys != null)
            && (keys.size() > 0)
            && (count > 0)
            && (count <= keys.size()))
            {
            for (int i = 0;
                 i < count;
                 i++)
                {
                bufferKey.append(keys.get(i));
                bufferKey.append(RegistryModelPlugin.DELIMITER_RESOURCE);
                }

            // Remove the final DELIMITER_PATH, but only if there's one there...
            if ((bufferKey.length() > 1)
                && (bufferKey.toString().endsWith(RegistryModelPlugin.DELIMITER_RESOURCE)))
                {
                bufferKey.setLength(bufferKey.length() - 1);
                }
            }

        // Finally, make sure we return a 'root' key if none was found
        if (bufferKey.length() == 0)
            {
            bufferKey.append(RegistryModelPlugin.DELIMITER_RESOURCE);
            }

        return (bufferKey);
        }


    /***********************************************************************************************
     * Get the true length of the ResourceKey of the specified Plugin, without the Framework prefix.
     * This <b>includes</b> the leaf node at the end of the chain.
     *
     * @param plugin
     *
     * @return int
     */

    public static int getKeyLength(final ResourcePlugin plugin)
        {
        return (plugin.getResourceKeys().size());
        }


    /**********************************************************************************************/
    /* Constructors                                                                               */
    /***********************************************************************************************
     * Construct a ResourceData from an Property XMLBean.
     *
     * @param id
     * @param host
     * @param property
     * @param language
     */

    public ResourceData(final long id,
                        final AtomPlugin host,
                        final PropertyResource property,
                        final String language)
        {
        super(id);

        // Save a reference to the host Atom
        this.hostPlugin = host;

        // Initialise the Resource
        setLevel(host.getLevel());
        setType(RootType.PROPERTY);
        setXml(property);
        setResource(null);
        setInstalled(false);
        setISOLanguageCode(language);
        setDebugMode(false);

        // Reset the Dates and times
        setCreatedDate(Chronos.getCalendarDateNow());
        setCreatedTime(Chronos.getCalendarTimeNow());
        setModifiedDate(Chronos.getCalendarDateNow());
        setModifiedTime(Chronos.getCalendarTimeNow());

        // ToDo ??? Make the Resource accessible on the navigation tree
        getHostTreeNode().setUserObject(this);

        // All changes are complete, now make sure that updateRoot() is not called
        setUpdated(false);
        }


    /***********************************************************************************************
     * Construct a ResourceData from an String XMLBean.
     *
     * @param id
     * @param host
     * @param string
     * @param language
     */

    public ResourceData(final long id,
                        final AtomPlugin host,
                        final StringResource string,
                        final String language)
        {
        super(id);

        if ((string == null)
            || (!XmlBeansUtilities.isValidXml(string)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Save a reference to the host Atom
        this.hostPlugin = host;

        // Initialise the Resource
        setLevel(host.getLevel());
        setType(RootType.STRING);
        setXml(string);
        setResource(null);
        setInstalled(false);
        setISOLanguageCode(language);
        setDebugMode(false);

        // Reset the Dates and times
        setCreatedDate(Chronos.getCalendarDateNow());
        setCreatedTime(Chronos.getCalendarTimeNow());
        setModifiedDate(Chronos.getCalendarDateNow());
        setModifiedTime(Chronos.getCalendarTimeNow());

        // ToDo ??? Make the Resource accessible on the navigation tree
        getHostTreeNode().setUserObject(this);

        // All changes are complete, now make sure that updateRoot() is not called
        setUpdated(false);
        }


    /***********************************************************************************************
     * Construct a ResourceData from a Query XMLBean.
     *
     * @param id
     * @param host
     * @param query
     * @param language
     */

    public ResourceData(final long id,
                        final AtomPlugin host,
                        final QueryResource query,
                        final String language)
        {
        super(id);

        // Save a reference to the host Atom
        this.hostPlugin = host;

        // Initialise the Resource
        setLevel(host.getLevel());
        setType(RootType.QUERY);
        setXml(query);
        setResource(null);
        setInstalled(false);
        setISOLanguageCode(language);
        setDebugMode(false);

        // Reset the Dates and times
        setCreatedDate(Chronos.getCalendarDateNow());
        setCreatedTime(Chronos.getCalendarTimeNow());
        setModifiedDate(Chronos.getCalendarDateNow());
        setModifiedTime(Chronos.getCalendarTimeNow());

        // ToDo ??? Make the Resource accessible on the navigation tree
        getHostTreeNode().setUserObject(this);

        // All changes are complete, now make sure that updateRoot() is not called
        setUpdated(false);
        }


    /***********************************************************************************************
     * Construct a ResourceData from an Exception XMLBean.
     *
     * @param id
     * @param host
     * @param exception
     * @param language
     */

    public ResourceData(final long id,
                        final AtomPlugin host,
                        final ExceptionResource exception,
                        final String language)
        {
        super(id);

        // Save a reference to the host Atom
        this.hostPlugin = host;

        // Initialise the Resource
        setLevel(host.getLevel());
        setType(RootType.EXCEPTION);
        setXml(exception);
        setResource(null);
        setInstalled(false);
        setISOLanguageCode(language);
        setDebugMode(false);

        // Reset the Dates and times
        setCreatedDate(Chronos.getCalendarDateNow());
        setCreatedTime(Chronos.getCalendarTimeNow());
        setModifiedDate(Chronos.getCalendarDateNow());
        setModifiedTime(Chronos.getCalendarTimeNow());

        // ToDo ??? Make the Resource accessible on the navigation tree
        getHostTreeNode().setUserObject(this);

        // All changes are complete, now make sure that updateRoot() is not called
        setUpdated(false);
        }


    /***********************************************************************************************
     * Get the host Atom plugin.
     *
     * @return AtomPlugin
     */

    public final AtomPlugin getHostAtom()
        {
        return (this.hostPlugin);
        }


    /***********************************************************************************************
     *
     * @return short
     */

    public short getSortOrder()
        {
        return (this.shortSortOrder);
        }


    /***********************************************************************************************
     *
     * @param order
     */

    public void setSortOrder(final short order)
        {
        this.shortSortOrder = order;
        }


    /***********************************************************************************************
     * Get the full RegistryModel pathname for the Resource.
     *
     * @return String
     */

    public final String getPathname()
        {
        return (getName());
        }


    /***********************************************************************************************
     * Get the full name for the Resource formed from the ResourceKeys as stored in the database.
     *
     * @return The full name of the Resource as "One.Two.Three.Four"
     */

    public final String getName()
        {
        return (this.strName);
        }


    /***********************************************************************************************
     * Set the Name.
     *
     * @param name
     */

    public void setName(final String name)
        {
        this.strName = name;
        }


    /***********************************************************************************************
     * Get the Active field.
     *
     * @return boolean
     */

    public boolean isActive()
        {
        //Todo
        return true;
        }


    /***********************************************************************************************
     * Set the Active field.
     *
     * @param active
     */

    public void setActive(final boolean active)
        {
        //Todo
        }


    /***********************************************************************************************
     * Get the ResourceKey for the Resource.
     *
     * @return String
     */

    public final String getResourceKey()
        {
        return (this.strResourceKey);
        }


     /***********************************************************************************************
     * Set the ResourceKey for the Resource.
     * This should only be used by the ResourceLoader.
     *
     * @param resourcekey
     */

    public final void setResourceKey(final String resourcekey)
        {
        this.strResourceKey = resourcekey;
        }


    /***********************************************************************************************
     * Get the Resource as an Object
     *
     * @return Object
     */

    public final Object getResource()
        {
        return (this.objResource);
        }


    /***********************************************************************************************
     * Set the Resource as an Object
     *
     * @param resource
     */

    public final void setResource(final Object resource)
        {
        if (resource != null)
            {
            this.objResource = resource;
            updateRoot();
            }
        else
            {
            this.objResource = null;
            }
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean isInstalled()
        {
        //Todo
        return boolInstalled;
        }


    /***********************************************************************************************
     *
     * @param boolInstalled
     */

    public final void setInstalled(final boolean boolInstalled)
        {
        //Todo
        this.boolInstalled = boolInstalled;
        updateRoot();
        }


    /**********************************************************************************************
     * Get the IconFilename field.
     *
     * @return String
     */

    public String getIconFilename()
        {
        //Todo
        return (this.strIconFilename);
        }


    /**********************************************************************************************
     * Set the IconFilename field.
     *
     * @param filename
     */

    public void setIconFilename(final String filename)
        {
        //Todo
        this.strIconFilename = filename;
        updateRoot();
        }


    /***********************************************************************************************
     * Display the correct label for each Node (not very well documented!).
     * This is used by the FrameworkManager tree.
     *
     * @return String
     */

    public String toString()
        {
        return (getResourceKeys().get(getResourceKeys().size()-1));
        }


    /**********************************************************************************************/
    /* User Interface and Debugging                                                               */
    /***********************************************************************************************
     * Run the UI of this UserObjectPlugin when its tree node is selected.
     * setUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public final void runUI()
        {
        }


    /***********************************************************************************************
     * Stop the UI of this UserObjectPlugin when its tree node is deselected.
     * clearUIOccupant() uses this from the navigation tree, a menu, or a toolbar button.
     */

    public final void stopUI()
        {
        }


    /***********************************************************************************************
     * The action performed when a Context Action runs this Resource.
     * This can be called from the navigation tree, a menu, or a toolbar button.
     *
     * @param event
     * @param browsemode
     */

    public final void actionPerformed(final AWTEvent event,
                                      final boolean browsemode)
        {
        // ActionPerformed must:
        //      use FrameworkManagerUIComponentPlugin to:
        //          clear the previous UI occupant
        //          set the new UI occupant
        //      use RegistryModelController to:
        //          setPluginState() or setTaskState()
        //      use UserObject to:
        //          record the new selection

        LOGGER.debugNavigation("START RESOURCE actionPerformed browsemode=" + browsemode);

        // Set the Browse mode of the RootData
        setBrowseMode(browsemode);

        if (getBrowseMode())
            {
            // Check that there is a valid FrameworkManager and UI installed!
            if ((this.validatePlugin())
                && (REGISTRY_MODEL.getFrameworkManager() != null)
                && (REGISTRY_MODEL.getFrameworkManager().isRunning())
                && (REGISTRY_MODEL.getFrameworkManager().getUI() != null))
                {
                // Remove the previous occupant of the UI Panel
                // This calls RootData.runUI(), which sets the Caption and Status
                REGISTRY_MODEL.getFrameworkManager().getUI().clearUIOccupant(this);
                REGISTRY_MODEL.getFrameworkManager().getUI().setUIOccupant(this);

                // State change is not applicable to Resources

                // Select the node on the navigation tree
                selectNodeOnTree(getHostTreeNode());
                }
            else
                {
                // Otherwise do nothing at all..
                NavigationUtilities.unableToPerformAction();
                }
            }
        else
            {
            // We are in Edit mode
            // Check that there is a valid FrameworkManager and UI installed!
            if ((this.validatePlugin())
                && (REGISTRY_MODEL.getFrameworkManager() != null)
                && (REGISTRY_MODEL.getFrameworkManager().isRunning())
                && (REGISTRY_MODEL.getFrameworkManager().getUI() != null))
                {
                // Remove the previous occupant of the UI Panel
                REGISTRY_MODEL.getFrameworkManager().getUI().clearUIOccupant(this);

                // Install the appropriate Editor (this calls setUIOccupant())
                editUserObject();

                // Select the node on the navigation tree
                selectNodeOnTree(getHostTreeNode());
                }
            else
                {
                // Otherwise do nothing at all..
                NavigationUtilities.unableToPerformAction();
                }
            }

        LOGGER.debugNavigation("END RESOURCE actionPerformed()");
        LOGGER.debugNavigation("***************************************************************\n\n");
        }


    /***********************************************************************************************
     * The action performed when a Context Action stops this Resource.
     * This can be called from the navigation tree, a menu, or a toolbar button.
     *
     * @param event
     * @param browsemode
     */

    public void actionHalted(final AWTEvent event,
                             final boolean browsemode)
        {
        // ActionHalted must:
        //      use FrameworkManagerUIComponentPlugin to:
        //          clear the previous UI occupant
        //          set the new UI occupant to BlankUI
        //      use RegistryModelController to:
        //          setPluginState() or setTaskState()
        //      use UserObject to:
        //          record the new selection

        // Set the Browse mode of the RootData
        setBrowseMode(browsemode);

        // Check that there is a valid FrameworkManager and UI installed!
        if ((this.validatePlugin())
            && (REGISTRY_MODEL.getFrameworkManager() != null)
            && (REGISTRY_MODEL.getFrameworkManager().isRunning())
            && (REGISTRY_MODEL.getFrameworkManager().getUI() != null))
            {
            LOGGER.debugNavigation("RESOURCE actionHalted browsemode=" + browsemode);

            // Remove the previous occupant of the UI Panel
            REGISTRY_MODEL.getFrameworkManager().getUI().clearUIOccupant(new NullData());
            REGISTRY_MODEL.getFrameworkManager().getUI().setUIOccupant(new NullData());

            // State change is not applicable to Resources

            // Select the node on the navigation tree
            selectNodeOnTree(getHostTreeNode());
            }
        else
            {
            // Otherwise do nothing at all..
            NavigationUtilities.unableToPerformAction();
            }
        }


    /***********************************************************************************************
     * Show some status text and an icon from the default Common set.
     *
     * @param status
     */

    public final void setStatus(final String status)
        {
        if (status != null)
            {
            final Icon icon;

            icon = RegistryModelUtilities.getCommonIcon(getIconFilename());
            setStatus(status, icon);
            }
        else
            {
            LOGGER.debug(".setResponseStatus() Cannot show status!");
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File

