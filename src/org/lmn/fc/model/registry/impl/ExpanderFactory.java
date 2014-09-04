package org.lmn.fc.model.registry.impl;

import org.lmn.fc.model.plugins.ExpanderInterface;
import org.lmn.fc.model.plugins.impl.ExpanderData;


/***************************************************************************************************
 * The ExpanderFactory.
 */

public final class ExpanderFactory
    {
    public static final String EXPANDER_PLUGINS = "Plugins";
    public static final String EXPANDER_TASKS = "Tasks";
    public static final String EXPANDER_RESOURCES = "Resources";
    public static final String EXPANDER_PROPERTIES = "Properties";
    public static final String EXPANDER_STRINGS = "Strings";
    public static final String EXPANDER_EXCEPTIONS = "Exceptions";
    public static final String EXPANDER_QUERIES = "Queries";
    public static final String EXPANDER_USERS = "Users";
    public static final String EXPANDER_ROLES = "Roles";
    public static final String EXPANDER_COUNTRIES = "Countries";
    public static final String EXPANDER_LANGUAGES = "Languages";
    public static final String EXPANDER_LOOKANDFEELS = "LookAndFeels";
    public static final String EXPANDER_DATATYPES = "DataTypes";

    private volatile static ExpanderFactory FACTORY_INSTANCE;


    /***********************************************************************************************
     * Get an instance of the ExpanderFactory.
     *
     * @return ExpanderFactory
     */

    public static ExpanderFactory getInstance()
        {
        if (FACTORY_INSTANCE == null)
            {
            synchronized (ExpanderFactory.class)
                {
                if (FACTORY_INSTANCE == null)
                    {
                    FACTORY_INSTANCE = new ExpanderFactory();
                    }
                }
            }

        return (FACTORY_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the ExpanderFactory.
     */

    private ExpanderFactory()
        {
        }


    /***********************************************************************************************
     * Get an expander, defined by a pathname, name and iconfilename.
     *
     * @param pathname
     * @param name
     * @param iconfilename
     *
     * @return AtomPlugin
     */

    public ExpanderInterface getExpander(final String pathname,
                                         final String name,
                                         final String iconfilename)
        {
        return (new ExpanderData(pathname, name, iconfilename));
        }
    }
