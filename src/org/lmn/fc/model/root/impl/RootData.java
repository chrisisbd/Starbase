package org.lmn.fc.model.root.impl;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.loaders.LoaderProperties;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.database.impl.FrameworkDatabase;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryManagerPlugin;
import org.lmn.fc.model.registry.RegistryModelControllerInterface;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.registry.impl.RegistryManager;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.registry.impl.RegistryModelController;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.root.RootType;
import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;


/***************************************************************************************************
 * The root of AtomData, TaskData and ResourceData.
 */

public abstract class RootData implements RootPlugin
    {
    protected static final LoaderProperties LOADER_PROPERTIES = LoaderProperties.getInstance();
    protected static final Logger LOGGER = Logger.getInstance();
    protected static final FrameworkDatabase DATABASE = FrameworkDatabase.getInstance();
    protected static final RegistryPlugin REGISTRY = Registry.getInstance();
    protected static final RegistryManagerPlugin REGISTRY_MANAGER = RegistryManager.getInstance();
    protected static final RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();
    protected static final RegistryModelControllerInterface REGISTRY_CONTROLLER = RegistryModelController.getInstance();

    private long longID;

    private String levelRoot;
    private RootType typeRoot;
    private DefaultMutableTreeNode hostTreeNode;

    // Indicates if a real class was found and instantiated
    private boolean boolClassFound;

    // The XML part of the RootData
    private XmlObject xmlRoot;

    private boolean boolUpdated;            // True if the Resource has been updated
    private boolean boolUpdatesAllowed;     // True if updates are currently allowed

    private boolean boolDebugMode;          // Controls debug messages
    private boolean boolTimingMode;         // Controls timing messages
    private long longTimerMilliseconds;     //

    private UIComponentPlugin rootUI;       // The UI panel
    private UIComponentPlugin rootEditor;   // The Editor panel
    private boolean boolBrowseMode;         // Browse or Editor modes
    private List<String> listValidationErrors;


    /***********************************************************************************************
     * Construct a RootData with the specified ID.
     *
     * @param id
     */

    public RootData(final long id)
        {
        this.longID = id;

        this.hostTreeNode = new DefaultMutableTreeNode();

        boolUpdated = false;
        boolUpdatesAllowed = false;

        // 2008-01-17 This seems to work Ok with null!
//        rootUI = new BlankUIComponent();
        rootUI = null;

        rootEditor = null;
        boolBrowseMode = true;

        boolDebugMode = false;
        boolTimingMode = false;
        listValidationErrors = new ArrayList<String>(10);
        }


    /***********************************************************************************************
     * Get the ID.
     *
     * @return long
     */

    public long getID()
        {
        return (this.longID);
        }


    /***********************************************************************************************
     * Set the ID.
     *
     * @param id
     */

    public final void setID(final long id)
        {
        this.longID = id;
        }


    /***********************************************************************************************
     * Get the Level of this object.
     *
     * @return RootLevel
     */

    public final String getLevel()
        {
        return (this.levelRoot);
        }


    /***********************************************************************************************
     * Set the Level of this object.
     *
     * @param level
     */

    public final void setLevel(final String level)
        {
        this.levelRoot = level;
        }


    /***********************************************************************************************
     * Get the Type of this object.
     *
     * @return RootType
     */

    public final RootType getType()
        {
        return (this.typeRoot);
        }


    /***********************************************************************************************
     * Set the Type of this object.
     *
     * @param type
     */

    public final void setType(final RootType type)
        {
        this.typeRoot = type;
        }


    /***********************************************************************************************
     * Get the ClassFound flag.
     *
     * @return boolean
     */

    public final boolean isClassFound()
        {
        return (this.boolClassFound);
        }


    /***********************************************************************************************
     * Set the ClassFound flag.
     *
     * @param flag
     */

    public final void setClassFound(final boolean flag)
        {
        this.boolClassFound = flag;
        }


    /***********************************************************************************************
     * Get the XML part of the Root.
     *
     * @return XmlObject
     */

     public XmlObject getXml()
         {
         return (this.xmlRoot);
         }


     /***********************************************************************************************
      * Set the XML part of the Root.
      *
      * @param xml
      */

     public final void setXml(final XmlObject xml)
         {
         this.xmlRoot = xml;
         }


//    /***********************************************************************************************
//     * Get the DateCreated field.
//     *
//     * @return Date
//     */
//
//    public abstract Date getCreatedDate();
//
//
//    /***********************************************************************************************
//     * Set the DateCreated field.
//     *
//     * @param date
//     */
//
//    public abstract void setCreatedDate(final Date date);
//
//
//    /***********************************************************************************************
//     * Get the TimeCreated field.
//     *
//     * @return Time
//     */
//
//    public abstract Time getCreatedTime();
//
//
//    /***********************************************************************************************
//     * Set the TimeCreated field.
//     *
//     * @param time
//     */
//
//    public abstract void setCreatedTime(final Time time);
//
//
//    /***********************************************************************************************
//     * Get the DateModified field.
//     *
//     * @return Date
//     */
//
//    public abstract Date getModifiedDate();
//
//
//    /***********************************************************************************************
//     * Set the DateModified field.
//     *
//     * @param date
//     */
//
//    public abstract void setModifiedDate(final Date date);
//
//
//    /***********************************************************************************************
//     * Get the TimeModified field.
//     *
//     * @return Time
//     */
//
//    public abstract Time getModifiedTime();
//
//
//    /***********************************************************************************************
//     * Set the TimeModified field.
//     *
//     * @param time
//     */
//
//    public abstract void setModifiedTime(final Time time);
//

    /***********************************************************************************************
     * Get the UIComponentPlugin.
     *
     * @return UIComponentPlugin
     */

    public final UIComponentPlugin getUIComponent()
        {
        return (this.rootUI);
        }


    /***********************************************************************************************
     * Set the UIComponentPlugin.
     *
     * @param uicomponent
     */

    public final void setUIComponent(final UIComponentPlugin uicomponent)
        {
        this.rootUI = uicomponent;
        }


    /***********************************************************************************************
     * Get the Editor UIComponentPlugin.
     *
     * @return UIComponentPlugin
     */

    public final UIComponentPlugin getEditorComponent()
        {
        return (this.rootEditor);
        }


    /***********************************************************************************************
     * Set the Editor UIComponentPlugin.
     *
     * @param component
     */

    public final void setEditorComponent(final UIComponentPlugin component)
        {
        this.rootEditor = component;
        }


    /***********************************************************************************************
     *
     * @return boolean
     */

    public final boolean getBrowseMode()
        {
        return boolBrowseMode;
        }


    /***********************************************************************************************
     *
     * @param mode
     */

    public final void setBrowseMode(final boolean mode)
        {
        this.boolBrowseMode = mode;
        }


    /***********************************************************************************************
     * Get the HostTreeNode.
     *
     * @return DefaultMutableTreeNode
     */

    public final DefaultMutableTreeNode getHostTreeNode()
        {
        return (hostTreeNode);
        }


    /***********************************************************************************************
     * Set the HostTreeNode.
     *
     * @param node
     */

    public final void setHostTreeNode(final DefaultMutableTreeNode node)
        {
        this.hostTreeNode = node;
        }


    /***********************************************************************************************
     * Mark the RootData as Updated.
     */

    public void updateRoot()
        {
        if (isUpdateAllowed())
            {
            if (!validatePlugin())
                {
                throw new FrameworkException(EXCEPTION_XML_VALIDATION);
                }

            setUpdated(true);
            }
        }


    /***********************************************************************************************
     * Allow updates of this ResourceData
     */

    public final void resumeUpdates()
        {
        boolUpdatesAllowed = true;
        }


    /***********************************************************************************************
     * Suspend updates of this ResourceData
     */

    public final void suspendUpdates()
        {
        boolUpdatesAllowed = false;
        }


    /***********************************************************************************************
     * Get the UpdatesAllowed flag
     *
     * @return boolean
     */

    public final boolean isUpdateAllowed()
        {
        return (this.boolUpdatesAllowed);
        }


    /***********************************************************************************************
     * Set the UpdatesAllowed flag
     *
     * @param updates
     */
    public final void setUpdateAllowed(final boolean updates)
        {
        this.boolUpdatesAllowed = updates;
        }


    /***********************************************************************************************
     * Get the Updated flag
     *
     * @return boolean
     */

    public final boolean isUpdated()
        {
        return (this.boolUpdated);
        }


    /***********************************************************************************************
     * Set the Updated flag
     *
     * @param flag
     */

    public final void setUpdated(final boolean flag)
        {
        this.boolUpdated = flag;
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Get the Debug Mode flag.
     *
     * @return boolean
     */

    public final boolean getDebugMode()
        {
        return (this.boolDebugMode);
        }


    /***********************************************************************************************
     * Set the Debug Mode flag.
     *
     * @param flag
     */

    public final void setDebugMode(final boolean flag)
        {
        this.boolDebugMode = flag;
        }


    /***********************************************************************************************
     * Show a debug message.
     *
     * @param message
     */

    public final void showDebugMessage(final String message)
        {
        final String strSeparator;

        if (getDebugMode())
            {
            if (message.startsWith("."))
                {
                strSeparator = "";
                }
            else
                {
                strSeparator = SPACE;
                }

            System.out.println(Chronos.timeNow()
                               + SPACE
                               + this.getClass().getName()
                               + strSeparator
                               + message);
            }
        }


    /***********************************************************************************************
     * Get the Timing Mode flag.
     *
     * @return boolean
     */

    private boolean getTimingMode()
        {
        return (this.boolTimingMode);
        }


    /***********************************************************************************************
     * Set the Timing Mode flag.
     *
     * @param flag
     */

    public final void setTimingMode(final boolean flag)
        {
        this.boolTimingMode = flag;
        }


    /***********************************************************************************************
     * Show a timing message.
     *
     * @param message
     */

    private void showTimingMessage(final String message)
        {
        final String strSeparator;

        if (getTimingMode())
            {
            if (message.startsWith("."))
                {
                strSeparator = "";
                }
            else
                {
                strSeparator = SPACE;
                }

            System.out.println(Chronos.timeNow() + SPACE
                               + Chronos.getSystemTime()
                               + SPACE +this.getClass().getName()
                               + strSeparator + message);
            }
        }


    /**********************************************************************************************/
    /* Validation                                                                                 */
    /***********************************************************************************************
     * Validate this bean.
     *
     * @return boolean
     */

    public boolean validatePlugin()
        {
        boolean boolValid;

        getValidationErrors().clear();

        //LOGGER.debug("Validating " + getName());
        boolValid = XmlBeansUtilities.isValidXml(getXml());

        if (!boolValid)
            {
            getValidationErrors().add("Failed validation in " + getName());
            }

        return (boolValid);
        }


    /***********************************************************************************************
     * Get the List of Validation Errors.
     *
     * @return List<String>
     */

    public List<String> getValidationErrors()
        {
        return (this.listValidationErrors);
        }
    }
