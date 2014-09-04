package org.lmn.fc.model.registry;


import org.lmn.fc.common.constants.ResourceKeys;


/***************************************************************************************************
 * InstallationFolder.
 */

public enum InstallationFolder
    {
    ROOT_COMMON         (0, "org/lmn/fc/common/"),
    FONTS               (1, "fonts"),
    IMAGES              (2, "images"),
    FLAGS_COUNTRIES     (3, "flags/countries"),
    FLAGS_LANGUAGES     (4, "flags/languages"),
    HELP                (5, "help"),
    ABOUT               (6, "help"),
    JAVADOC             (7, "javadoc"),
    PLUGINS             (8, "plugins"),

    PLATFORM            (9, "platform"),
    PLATFORM_WINDOWS    (10, "windows"),
    PLATFORM_LINUX      (11, "linux"),
    PLATFORM_APPLE_MAC  (12, "osx"),

    LIBRARIES           (13, "libraries"),
    DATASTORE           (14, "datastore"),
    IMPORTS             (15, "imports"),
    EXPORTS             (16, "exports"),
    WORKSPACE           (17, "workspace"),
    MAPS                (18, "maps"),
    LICENCES            (19, "licences"),
    LOGS                (20, "logs"),
    DAO                 (21, "dao"),
    DOC                 (22, "doc"),
    PUBLICATIONS        (23, "doc/publications");


    private final int intValue;
    private final String strName;


    /***********************************************************************************************
     * Get the System Property user.dir, guaranteed to be always terminated with a trailing
     * file.separator. Always keep user.home in step.
     *
     * @return String
     */

    public static String getTerminatedUserDir()
        {
        final String strUserDir;

        // Mark's fix to try to get JFileChooser working to the correct directory
        if (!System.getProperty(ResourceKeys.KEY_SYSTEM_USER_DIR).endsWith(System.getProperty("file.separator")))
            {
            strUserDir = System.getProperty(ResourceKeys.KEY_SYSTEM_USER_DIR) + System.getProperty("file.separator");
            }
        else
            {
            strUserDir = System.getProperty(ResourceKeys.KEY_SYSTEM_USER_DIR);
            }

        System.setProperty(ResourceKeys.KEY_SYSTEM_USER_DIR, strUserDir);
        //System.setProperty(ResourceKeys.KEY_SYSTEM_USER_HOME, strUserDir);

        //System.out.println("[user.dir=" + System.getProperty(ResourceKeys.KEY_SYSTEM_USER_DIR) + "] [user.home=" + System.getProperty("user.home") + "]");

        return (strUserDir);
        }


    /***********************************************************************************************
     * InstallationFolder.
     *
     * @param value
     * @param name
     */

    private InstallationFolder(final int value,
                               final String name)
        {
        this.intValue = value;
        this.strName = name;
        }


    /***********************************************************************************************
     * Get the InstallationFolder Type ID.
     *
     * @return int
     */

    public int getTypeID()
        {
        return (this.intValue);
        }


    /***********************************************************************************************
     * Get the InstallationFolder Name.
     *
     * @return String
     */

    public String getName()
        {
        return(this.strName);
        }


    /***********************************************************************************************
     * Get the InstallationFolder Name.
     *
     * @return String
     */

    public String toString()
        {
        return (this.strName);
        }
    }
