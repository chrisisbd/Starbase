package org.lmn.fc.model.registry.impl;

import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.locale.impl.CountryData;
import org.lmn.fc.model.locale.impl.LanguageData;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.lookandfeels.impl.LookAndFeelData;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.BeanFactoryXmlPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.resources.impl.ExceptionData;
import org.lmn.fc.model.resources.impl.PropertyData;
import org.lmn.fc.model.resources.impl.QueryData;
import org.lmn.fc.model.resources.impl.StringData;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.model.users.impl.RoleData;
import org.lmn.fc.model.users.impl.UserData;
import org.lmn.fc.model.xmlbeans.countries.Country;
import org.lmn.fc.model.xmlbeans.exceptions.ExceptionResource;
import org.lmn.fc.model.xmlbeans.frameworks.Framework;
import org.lmn.fc.model.xmlbeans.languages.Language;
import org.lmn.fc.model.xmlbeans.lookandfeels.LookAndFeel;
import org.lmn.fc.model.xmlbeans.plugins.Plugin;
import org.lmn.fc.model.xmlbeans.properties.PropertyResource;
import org.lmn.fc.model.xmlbeans.queries.QueryResource;
import org.lmn.fc.model.xmlbeans.roles.Role;
import org.lmn.fc.model.xmlbeans.strings.StringResource;
import org.lmn.fc.model.xmlbeans.tasks.Task;
import org.lmn.fc.model.xmlbeans.users.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;


/***************************************************************************************************
 * The BeanFactoryXml.
 */

public final class BeanFactoryXml implements BeanFactoryXmlPlugin
    {
    private volatile static BeanFactoryXml FACTORY_INSTANCE;


    /***********************************************************************************************
     * The BeanFactoryXml is a Singleton!
     *
     * @return BeanFactoryXml
     */

    public static BeanFactoryXml getInstance()
        {
        if (FACTORY_INSTANCE == null)
            {
            synchronized (BeanFactoryXml.class)
                {
                if (FACTORY_INSTANCE == null)
                    {
                    FACTORY_INSTANCE = new BeanFactoryXml();
                    }
                }
            }

        return (FACTORY_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the BeanFactoryXml.
     */

    private BeanFactoryXml()
        {
        }


    /**********************************************************************************************/
    /* Framework Beans                                                                            */
    /***********************************************************************************************
     * Create a UserPlugin from the specified XMLObject.
     * The host is assumed to be the Framework,
     * since the Framework was unavailable when the User was loaded.
     *
     * @param user
     *
     * @return UserPlugin
     */

    public final UserPlugin createUser(final User user)
        {
        UserPlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((user == null)
            || (!XmlBeansUtilities.isValidXml(user)))
            {
            LOGGER.log(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new UserData(user);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create a RolePlugin from the specified XMLObject.
     * The host is assumed to be the Framework,
     * since the Framework was unavailable when the Role was loaded.
     *
     * @param role
     *
     * @return RolePlugin
     */

    public final RolePlugin createRole(final Role role)
        {
        RolePlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((role == null)
            || (!XmlBeansUtilities.isValidXml(role)))
            {
            LOGGER.log(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new RoleData(role);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create a CountryPlugin from the specified XMLObject.
     * The host is assumed to be the Framework,
     * since the Framework was unavailable when the Country was loaded.
     *
     * @param country
     *
     * @return CountryPlugin
     */

    public final CountryPlugin createCountry(final Country country)
        {
        CountryPlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((country == null)
            || (!XmlBeansUtilities.isValidXml(country))
            || (country.getISO2() == null)
            || (EMPTY_STRING.equals(country.getISO2()))
            || (country.getISO2().length() != 2))
            {
            LOGGER.log(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new CountryData(country);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create a LanguagePlugin from the specified XMLObject.
     * The host is assumed to be the Framework,
     * since the Framework was unavailable when the Language was loaded.
     *
     * @param language
     *
     * @return LanguagePlugin
     */

    public final LanguagePlugin createLanguage(final Language language)
        {
        LanguagePlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((language == null)
            || (!XmlBeansUtilities.isValidXml(language))
            || (language.getISOCode() == null)
            || (EMPTY_STRING.equals(language.getISOCode()))
            || (language.getISOCode().length() != 2))
            {
            LOGGER.log(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new LanguageData(language);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create a LookAndFeelPlugin from the specified XMLObject.
     * The host is assumed to be the Framework,
     * since the Framework was unavailable when the LookAndFeel was loaded.
     *
     * @param lookandfeel
     *
     * @return LookAndFeelPlugin
     */

    public LookAndFeelPlugin createLookAndFeel(final LookAndFeel lookandfeel)
        {
        LookAndFeelPlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((lookandfeel == null)
            || (!XmlBeansUtilities.isValidXml(lookandfeel)))
            {
            LOGGER.log(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new LookAndFeelData(lookandfeel);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create the Framework from the specified XML document.
     *
     * @param frameworkclass
     * @param frameworkxml
     *
     * @return FrameworkPlugin
     */

    public final FrameworkPlugin createFramework(final Class frameworkclass,
                                                 final Framework frameworkxml)
        {
        FrameworkPlugin plugin;
        final String strFrameworkName;
        final String strClassName;
        final String strPackageName;
        final String strInterface;
        boolean boolLoaded;

        // Check incoming XML
        if ((frameworkclass == null)
            || (frameworkclass.getName() == null)
            || (frameworkxml == null)
            || (!XmlBeansUtilities.isValidXml(frameworkxml))
            || (frameworkxml.getName() == null)
            || (EMPTY_STRING.equals(frameworkxml.getName())))
            {
            LOGGER.log(EXCEPTION_PARAMETER_NULL);

            return (null);
            }

        strFrameworkName = frameworkxml.getName();
//        LOGGER.log("Creating an instance of " + strFrameworkName);

        // Try to find the Class for the target Framework
        strPackageName = RegistryModelPlugin.PACKAGE_ROOT
                           + RegistryModelPlugin.PACKAGE_FRAMEWORKS
                           + strFrameworkName.toLowerCase()
                           + RegistryModelPlugin.DELIMITER_PACKAGE;
        strClassName = strPackageName + strFrameworkName;
        strInterface = strClassName + SUFFIX_MBEAN;

//        LOGGER.log("[package=" + strPackageName + "]");
//        LOGGER.log("[classname=" + strClassName + "]");
//        LOGGER.log("[interface=" + strInterface + "]");

        // Assume initially that we will NOT find a class to instantiate...
        boolLoaded = false;
        plugin = null;

        // Instantiate a real Framework *only* if the correct class & interface are found
        try
            {
            final Class classObject = Class.forName(strClassName);
            //LOGGER.log("[found " + classObject.getPackage() + "]");

            // Does the target implement the <plugin>MBean interface?
            final Class[] interfaces = classObject.getInterfaces();

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0; ((i < interfaces.length) && (!boolLoaded)); i++)
                        {
                        if (strInterface.equals(interfaces[i].getName()))
                            {
                            // We have found the <plugin>MBean interface
                            //LOGGER.log("[" + strClassName + " implements " + strInterface + "]");

                            // Prove that the real Framework is a subclass of frameworkclass
                            final Class superClass = classObject.getSuperclass();

                            if (superClass != null)
                                {
                                if (frameworkclass.getName().equals(superClass.getName()))
                                    {
                                    // We are dealing with the right kind of object...
                                    //LOGGER.log("[" + strClassName + " is a subclass of " + frameworkclass.getName() + "]");

                                    // Now get hold of the single Constructor
                                    final Constructor[] constructors = classObject.getDeclaredConstructors();

                                    if ((constructors != null) && (constructors.length == 1))
                                        {
                                        //LOGGER.log("[Constructor=" + constructors[0].getName() + "]");

                                        // Find the access flags for the Constructor
                                        //LOGGER.log("[Constructor Modifiers=" + Utilities.showModifiers(constructors[0]) + "]");

                                        if (Modifier.isPrivate(constructors[0].getModifiers()))
                                            {
                                            constructors[0].setAccessible(true);

                                            // Make a subclass of frameworkclass
                                            plugin = (FrameworkPlugin)constructors[0].newInstance();

                                            // XML Persistence
                                            // Initialise the XML part of the Framework
                                            plugin.setXml(frameworkxml);

                                            // Set the level of the Framework to the first entry
                                            // in the RecursionLevels list
                                            plugin.setLevel(plugin.getRecursionLevels().get(0));

                                            // Database Persistence
                                            // Reset the Dates and times
                                            plugin.setCreatedDate(Chronos.getCalendarDateNow());
                                            plugin.setCreatedTime(Chronos.getCalendarTimeNow());
                                            plugin.setModifiedDate(Chronos.getCalendarDateNow());
                                            plugin.setModifiedTime(Chronos.getCalendarTimeNow());

                                            // We don't want to look for any more interfaces!
                                            plugin.setClassFound(true);
                                            boolLoaded = true;
                                            }
                                        else
                                            {
                                            LOGGER.error("Framework not loaded! The Constructor must not be public");
                                            boolLoaded = false;
                                            }
                                        }
                                    else
                                        {
                                        LOGGER.error("Framework not loaded! Single Constructor not found");
                                        boolLoaded = false;
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error("Framework not loaded! Class is not a subclass of " + frameworkclass.getName());
                                    boolLoaded = false;
                                    }
                                }
                            else
                                {
                                LOGGER.error("Framework not loaded! Class has no superclass");
                                boolLoaded = false;
                                }
                            }
                        else
                            {
                            LOGGER.error("Framework not loaded! Incorrect interface " + interfaces[i].getName());
                            boolLoaded = false;
                            }
                        }
                    }
                else
                    {
                    LOGGER.error("Framework not loaded! Class is an interface only");
                    boolLoaded = false;
                    }
                }
            else
                {
                LOGGER.error("Framework not loaded! No interfaces found");
                boolLoaded = false;
                }
            }

        catch(SecurityException exception)
            {
            LOGGER.error("SecurityException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (InstantiationException exception)
            {
            LOGGER.error("InstantiationException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error("IllegalAccessException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error("IllegalArgumentException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error("InvocationTargetException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (ClassNotFoundException exception)
            {
            // If we can't find the Class,
            LOGGER.error("ClassNotFoundException [classname=" + strClassName + "]");
            plugin.setClassFound(false);
            boolLoaded = true;
            }

        // Make sure we trap all failures...
        if (!boolLoaded)
            {
            plugin = null;
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create the Plugin with the specified parent Atom from the specified XML document.
     *
     * @param parent
     * @param pluginclass
     * @param pluginxml
     *
     * @return RootPlugin
     */

    public final AtomPlugin createPlugin(final AtomPlugin parent,
                                         final Class pluginclass,
                                         final Plugin pluginxml)
        {
        final String SOURCE = "BeanFactoryXml.createPlugin() ";
        AtomPlugin plugin;
        final String strPluginName;
        final String strClassName;
        final String strPackageName;
        final String strInterface;
        boolean boolLoaded;

        // Check incoming parameters
        if ((parent == null)
            || (!parent.validatePlugin())
            || (pluginclass == null)
            || (pluginclass.getName() == null)
            || (pluginxml == null)
            || (!XmlBeansUtilities.isValidXml(pluginxml)))
            {
            LOGGER.error(EXCEPTION_PARAMETER_INVALID);

            return (null);
            }

        strPluginName = pluginxml.getName();
//        LOGGER.log("Creating an instance of " + strPluginName);

        // Try to find the Class for the target Plugin
        strPackageName = parent.getClass().getPackage().getName()
                           + RegistryModelPlugin.DELIMITER_PACKAGE
                           + RegistryModelPlugin.PACKAGE_PLUGINS
                           + strPluginName.toLowerCase()
                           + RegistryModelPlugin.DELIMITER_PACKAGE;
        strClassName = strPackageName + strPluginName;
        strInterface = strClassName + SUFFIX_MBEAN;

//        LOGGER.log("[package=" + strPackageName + "]");
//        LOGGER.log("[classname=" + strClassName + "]");
//        LOGGER.log("[interface=" + strInterface + "]");

        // Assume initially that we will NOT find a class to instantiate...
        boolLoaded = false;
        plugin = null;

        // Instantiate a real Plugin *only* if the correct class & interface are found
        try
            {
            final Class classObject = Class.forName(strClassName);
//            LOGGER.log("[found " + classObject.getPackage() + "]");

            // Does the target implement the <plugin>MBean interface?
            final Class[] interfaces = classObject.getInterfaces();

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0; ((i < interfaces.length) && (!boolLoaded)); i++)
                        {
                        if (strInterface.equals(interfaces[i].getName()))
                            {
                            // We have found the <plugin>MBean interface
//                            LOGGER.log("[" + strClassName + " implements " + strInterface + "]");

                            // Prove that the real Plugin is a subclass of pluginclass
                            final Class superClass = classObject.getSuperclass();

                            if (superClass != null)
                                {
                                if (pluginclass.getName().equals(superClass.getName()))
                                    {
                                    // We are dealing with the right kind of object...
//                                    LOGGER.log("[" + strClassName + " is a subclass of " + pluginclass.getName() + "]");

                                    // Now get hold of the single Constructor
                                    final Constructor[] constructors = classObject.getDeclaredConstructors();

                                    if ((constructors != null) && (constructors.length == 1))
                                        {
//                                        LOGGER.log("[Constructor=" + constructors[0].getName() + "]");

                                        // Find the access flags for the Constructor
//                                        LOGGER.log("[Constructor Modifiers=" + Utilities.showModifiers(constructors[0]) + "]");

                                        if (Modifier.isPrivate(constructors[0].getModifiers()))
                                            {
                                            constructors[0].setAccessible(true);

                                            // Make a subclass of pluginclass
                                            plugin = (AtomPlugin)constructors[0].newInstance();

                                            // XML Persistence
                                            // Initialise the XML part of the Framework
                                            plugin.setXml(pluginxml);

                                            // Set the level of the Plugin to that of the host + 1
                                            if ((REGISTRY.getLevelID(parent) + 1) < REGISTRY.getMaxRecursionDepth())
                                                {
                                                // The PluginData constructor set the parent to null,
                                                // so link to the parent Atom and set the correct Level
                                                plugin.setParentAtom(parent);
                                                plugin.setLevel(REGISTRY.getFramework().getRecursionLevels().get(REGISTRY.getLevelID(parent) + 1));

                                                // Database Persistence
                                                // Reset the Dates and times
                                                plugin.setCreatedDate(Chronos.getCalendarDateNow());
                                                plugin.setCreatedTime(Chronos.getCalendarTimeNow());
                                                plugin.setModifiedDate(Chronos.getCalendarDateNow());
                                                plugin.setModifiedTime(Chronos.getCalendarTimeNow());

                                                // We don't want to look for any more interfaces!
                                                plugin.setClassFound(true);
                                                boolLoaded = true;
                                                }
                                            else
                                                {
                                                LOGGER.error(SOURCE + "Plugin not loaded! Framework maximum recursion depth exceeded");
                                                boolLoaded = false;
                                                }
                                            }
                                        else
                                            {
                                            LOGGER.error(SOURCE + "Plugin not loaded! The Constructor must not be public");
                                            boolLoaded = false;
                                            }
                                        }
                                    else
                                        {
                                        LOGGER.error(SOURCE + "Plugin not loaded! Single Constructor not found");
                                        boolLoaded = false;
                                        }
                                    }
                                else
                                    {
                                    LOGGER.error(SOURCE + "Plugin not loaded! Class is not a subclass of " + pluginclass.getName());
                                    boolLoaded = false;
                                    }
                                }
                            else
                                {
                                LOGGER.error(SOURCE + "Plugin not loaded! Class has no superclass");
                                boolLoaded = false;
                                }
                            }
                        else
                            {
                            LOGGER.error(SOURCE + "Plugin not loaded! Incorrect interface " + interfaces[i].getName());
                            boolLoaded = false;
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(SOURCE + "Plugin not loaded! Class is an interface only");
                    boolLoaded = false;
                    }
                }
            else
                {
                LOGGER.error(SOURCE + "Plugin not loaded! No interfaces found");
                boolLoaded = false;
                }
            }

        catch(SecurityException exception)
            {
            LOGGER.error(SOURCE + "SecurityException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (InstantiationException exception)
            {
            LOGGER.error(SOURCE + "InstantiationException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error(SOURCE + "IllegalAccessException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error(SOURCE + "IllegalArgumentException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error(SOURCE + "InvocationTargetException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (ClassNotFoundException exception)
            {
            // If we can't find the Class, this may just not be selected in the installer
            LOGGER.login(SOURCE + "ClassNotFoundException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        // Make sure we trap all failures...
        if (!boolLoaded)
            {
            plugin = null;
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create the Task with the specified parent Atom from the specified XML document.
     *
     * @param parent
     * @param taskclass
     * @param task
     *
     * @return RootPlugin
     */

    public final RootPlugin createTask(final AtomPlugin parent,
                                       final Class taskclass,
                                       final Task task)
        {
        TaskPlugin plugin;
        final String strTaskName;
        final String strClassName;
        final String strPackageName;
        boolean boolLoaded;

        // Check incoming XML
        if ((parent == null)
            || (taskclass == null)
            || (taskclass.getName() == null)
            || (task == null)
            || (!XmlBeansUtilities.isValidXml(task))
            || (task.getName() == null)
            || (EMPTY_STRING.equals(task.getName())))
            {
            LOGGER.error(EXCEPTION_PARAMETER_NULL);

            return (null);
            }

        strTaskName = task.getName();
//        LOGGER.log("\n[Creating an instance of " + strTaskName + "]");
//        LOGGER.log("[Parent package=" + parent.getClass().getPackage().getName() + "]");

        // Try to find the Class for the target Task
        strPackageName = parent.getClass().getPackage().getName()
                            + RegistryModelPlugin.DELIMITER_PACKAGE
                            + RegistryModelPlugin.PACKAGE_TASKS;
        strClassName = strPackageName + strTaskName;

//        LOGGER.log("[package=" + strPackageName + "]");
//        LOGGER.log("[classname=" + strClassName + "]");

        // Assume initially that we will NOT find a class to instantiate...
        plugin = null;

        // Instantiate a real Task *only* if the correct class & interface are found
        try
            {
            final Class classObject = Class.forName(strClassName);
//            LOGGER.log("[found " + classObject.getPackage().getName() + "]");

            if (!classObject.isInterface())
                {
                // Prove that the real Task is a subclass of the correct Class?
                final Class superClass = classObject.getSuperclass();
                //final Class superSuperClass = superClass.getSuperclass();

                if (superClass != null)
                    {
//                    LOGGER.log("[Super class=" + superClass.getName() + "]" );
//                    LOGGER.log("[Super super class=" + superSuperClass.getName() + "]" );

                    if (taskclass.getName().equals(superClass.getName()))
                        {
                        // We are dealing with the right kind of object...
//                        LOGGER.log("[" + strClassName + " is a subclass of " + taskclass.getName() + "]");

                        // Now get hold of the *single* Constructor of the subclass
                        final Constructor[] constructors = classObject.getDeclaredConstructors();

                        if ((constructors != null) && (constructors.length == 1))
                            {
//                            LOGGER.log("[Constructor=" + constructors[0].getName() + "]");

                            // Find the access flags for the Constructor
//                            LOGGER.log("[Constructor Modifiers=" + Utilities.showModifiers(constructors[0]) + "]");

                            if (Modifier.isPrivate(constructors[0].getModifiers()))
                                {
                                constructors[0].setAccessible(true);

                                // Make a subclass of TaskPlugin
                                plugin = (TaskPlugin)constructors[0].newInstance();

                                // XML Persistence
                                // Initialise the XML part of the Task
                                plugin.setXml(task);

                                // The TaskData constructor set the parent to null,
                                // so link to the parent Atom and set the correct Level
                                plugin.setParentAtom(parent);
                                plugin.setLevel(parent.getLevel());

                                // Database Persistence
                                // Reset the Dates and times
                                plugin.setCreatedDate(Chronos.getCalendarDateNow());
                                plugin.setCreatedTime(Chronos.getCalendarTimeNow());
                                plugin.setModifiedDate(Chronos.getCalendarDateNow());
                                plugin.setModifiedTime(Chronos.getCalendarTimeNow());

                                plugin.setClassFound(true);
                                plugin.setDebugMode(false);

                                // We don't want to look for any more interfaces!
                                boolLoaded = true;
                                }
                            else
                                {
                                LOGGER.error("Task not loaded! The Constructor must not be public");
                                boolLoaded = false;
                                }
                            }
                        else
                            {
                            LOGGER.error("Task not loaded! Single Constructor not found");
                            boolLoaded = false;
                            }
                        }
                    else
                        {
                        LOGGER.error("Task not loaded! Class is not a subclass of " + taskclass.getName());
                        boolLoaded = false;
                        }
                    }
                else
                    {
                    LOGGER.error("Task not loaded! Class has no superclass");
                    boolLoaded = false;
                    }
                }
            else
                {
                LOGGER.error("Task not loaded! Class is an interface only");
                boolLoaded = false;
                }
            }

        catch(SecurityException exception)
            {
            LOGGER.error("SecurityException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (InstantiationException exception)
            {
            LOGGER.error("InstantiationException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error("IllegalAccessException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error("IllegalArgumentException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error("InvocationTargetException [classname=" + strClassName + "]");
            boolLoaded = false;
            }

        catch (ClassNotFoundException exception)
            {
            LOGGER.error("ClassNotFoundException [classname=" + strClassName + "]");
            plugin.setClassFound(false);
            boolLoaded = true;
            }

        // Make sure we trap all failures...
        if (!boolLoaded)
            {
            plugin = null;
            }

        return (plugin);
        }


    /**********************************************************************************************/
    /* Resources                                                                                  */
    /***********************************************************************************************
     * Create the Property with the specified host Atom from the specified XML document.
     *
     * @param host
     * @param property
     * @param language
     *
     * @return RootPlugin
     */

    public final RootPlugin createProperty(final AtomPlugin host,
                                           final PropertyResource property,
                                           final String language)
        {
        RootPlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((host == null)
            || (host.getLevel() == null)
            || (property == null)
            || (!XmlBeansUtilities.isValidXml(property))
            || (language == null)
            || (language.length() != 2))
            {
            LOGGER.error(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new PropertyData(host, property, language);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create the String with the specified host Atom from the specified XML document.
     *
     * @param host
     * @param string
     * @param language
     *
     * @return RootPlugin
     */

    public final RootPlugin createString(final AtomPlugin host,
                                         final StringResource string,
                                         final String language)
        {
        RootPlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((host == null)
            || (host.getLevel() == null)
            || (string == null)
            || (!XmlBeansUtilities.isValidXml(string))
            || (language == null)
            || (language.length() != 2))
            {
            LOGGER.error(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new StringData(host, string, language);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create the Exception with the specified host Atom from the specified XML document.
     *
     * @param host
     * @param exception
     * @param language
     *
     * @return RootPlugin
     */

    public final RootPlugin createException(final AtomPlugin host,
                                            final ExceptionResource exception,
                                            final String language)
        {
        RootPlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((host == null)
            || (host.getLevel() == null)
            || (exception == null)
            || (!XmlBeansUtilities.isValidXml(exception))
            || (language == null)
            || (language.length() != 2))
            {
            LOGGER.error(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new ExceptionData(host, exception, language);
            }

        return (plugin);
        }


    /***********************************************************************************************
     * Create the Query with the specified host Atom from the specified XML document.
     *
     * @param host
     * @param query
     * @param language
     *
     * @return RootPlugin
     */

    public final RootPlugin createQuery(final AtomPlugin host,
                                        final QueryResource query,
                                        final String language)
        {
        RootPlugin plugin;

        plugin = null;

        // Check incoming XML etc.
        if ((host == null)
            || (host.getLevel() == null)
            || (query == null)
            || (!XmlBeansUtilities.isValidXml(query))
            || (query.getStatements() == null)
            || (query.getStatements().getStatementList() == null)
            || (language == null)
            || (language.length() != 2))
            {
            LOGGER.error(EXCEPTION_PARAMETER_INVALID);
            }
        else
            {
            // The constructor is only used here in the Factory!
            plugin = new QueryData(host, query, language);
            }

        return (plugin);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Try to instantiate a DataType of the specified class in order to
     * check that it is possible to store a Resource of that type.
     * Used by ResourceData and the PropertyEditor.
     *
     * @param classname
     * @param value
     *
     * @return Object
     */

    public Object validateResourceDataType(final String classname,
                                           final String value)
        {
        Object objReturn;
        final Class classObject;
        boolean boolInstantiated;

        // Some basic error traps...
        if ((classname == null)
            || (EMPTY_STRING.equals(classname))
            || (value == null))
//            || (EMPTY_STRING.equals(value)))   ToDo review if empty string is ok
            {
            return (null);
            }

        objReturn = null;
        boolInstantiated = false;

        try
            {
            classObject = Class.forName(classname);

            // Get hold of the Constructors for the requested class name
            final Constructor[] constructors = classObject.getDeclaredConstructors();

            if ((constructors != null)
                && (constructors.length >= 1))
                {
                // Step through all constructors, and find one which takes a String
                Class classParameters[];

                for (int i = 0;
                     ((!boolInstantiated) && (i < constructors.length));
                     i++)
                    {
                    classParameters = constructors[i].getParameterTypes();

                    // Only check the single-parameter constructors, class name in [0]
                    if (classParameters.length == 1)
                        {
                        // Check that the parameter type is a String
                        if (classParameters[0].getName().equals(String.class.getName()))
                            {
                            // It is safe to try to instantiate
                            // Constructor must take only a single String
                            final Object[] objArguments = new Object[1];
                            objArguments[0] = value;

                            // If we get this far, we have a valid DataType
                            objReturn = constructors[i].newInstance(objArguments);
                            boolInstantiated = true;
                            }
                        else
                            {
                            // Constructor has incorrect parameter type
                            //LOGGER.log("instantiateDataType Incorrect parameter type [parameter=" + classParameters[0].getName() + "]");
                            }
                        }
                    else
                        {
                        // Constructor has incorrect number of parameters
                        //LOGGER.log("instantiateDataType Skipping constructor which has zero or > 1 parameters ");
                        }
                    }
                }
            else
                {
                // Constructor is null or empty
                LOGGER.error("instantiateDataType Constructor is null or empty");
                }
            }

        catch (InstantiationException exception)
            {
            LOGGER.error("instantiateDataType InstantiationException [classname=" + classname + "]");
            }

        catch (IllegalAccessException exception)
            {
            LOGGER.error("instantiateDataType IllegalAccessException [classname=" + classname + "]");
            }

        catch (IllegalArgumentException exception)
            {
            LOGGER.error("instantiateDataType IllegalArgumentException [classname=" + classname + "]");
            }

        catch (InvocationTargetException exception)
            {
            LOGGER.error("instantiateDataType InvocationTargetException [classname=" + classname + "]");
            }

        catch (ClassNotFoundException exception)
            {
            LOGGER.error("instantiateDataType ClassNotFoundException [classname=" + classname + "]");
            }

        catch (NullPointerException exception)
            {
            LOGGER.error("instantiateDataType NullPointerException [classname=" + classname + "]");
            }

        return (objReturn);
        }
    }
