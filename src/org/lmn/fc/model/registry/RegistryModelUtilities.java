package org.lmn.fc.model.registry;

import org.lmn.fc.common.comparators.CountriesByName;
import org.lmn.fc.common.comparators.LanguagesByName;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.files.FileUtilities;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.dao.CountriesDAOInterface;
import org.lmn.fc.model.dao.LanguagesDAOInterface;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.registry.impl.RegistryModel;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.users.UserPlugin;
import org.lmn.fc.ui.reports.FlagIcon;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.*;


/***************************************************************************************************
 * The RegistryModelUtilities.
 */

public final class RegistryModelUtilities implements FrameworkStrings
    {
    private static final RegistryPlugin REGISTRY = Registry.getInstance();
    private static final RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();
    private static final Logger LOGGER = Logger.getInstance();


    /**********************************************************************************************/
    /* Common Images and Files                                                                    */
    /***********************************************************************************************
     * Get a common ImageIcon, using a URL.
     *
     * @param filename
     *
     * @return ImageIcon
     */

      public static ImageIcon getCommonIcon(final String filename)
          {
          final ImageIcon icon;

          if ((filename != null)
              && (!EMPTY_STRING.equals(filename)))
              {
              final URL url = RegistryModelUtilities.class.getResource(getCommonImagesRoot() + filename);

              if (url != null)
                  {
                  icon = new ImageIcon(url);
                  }
              else
                  {
                  icon = new ImageIcon();
                  }
              }
          else
              {
              icon = new ImageIcon();
              }

          return (icon);
          }


    /***********************************************************************************************
     * Get a common File from the filesystem or Jar.
     *
     * @param filename
     *
     * @return File
     */

    public static File getCommonImageAsFile(final String filename)
        {
        return (new File(getCommonImagesRoot() + filename));
        }


    /***********************************************************************************************
     * Get the root of the common Images folder.
     *
     * @return String
     */

    public static String getCommonImagesRoot()
        {
        return ("/"
                + InstallationFolder.ROOT_COMMON.getName()
                + InstallationFolder.IMAGES.getName()
                + "/");
        }


    /***********************************************************************************************
     * Get the root of the common Fonts folder.
     *
     * @return String
     */

    public static String getCommonFontsRoot()
        {
        return ("/"
                + InstallationFolder.ROOT_COMMON.getName()
                + InstallationFolder.FONTS.getName()
                + "/");
        }


    /**********************************************************************************************/
    /* Atom Images and Files                                                                      */
    /***********************************************************************************************
     * Get an ImageIcon for an Atom, using a URL.
     * Never returns NULL.
     *
     * @param plugin
     *
     * @return ImageIcon
     */

    public static ImageIcon getAtomIcon(final AtomPlugin plugin,
                                        final String filename)
        {
        final ImageIcon icon;

        if ((plugin != null)
            && (filename != null)
            && (!EMPTY_STRING.equals(filename)))
            {
            final URL url;

            url = plugin.getClass().getResource(getAtomFolderRoot(plugin, InstallationFolder.IMAGES) + filename);

            //            System.out.println("ATOM ICON PATH=" + getAtomFolderRoot(plugin, InstallationFolder.IMAGES) + filename);
//            System.out.println("ATOM URL=" + url);

            if (url != null)
                {
                icon = new ImageIcon(url);
                }
            else
                {
                icon = new ImageIcon();
                }
            }
        else
            {
            // If we can't find the image, just return an empty Icon
            icon = new ImageIcon();

            //LOGGER.error("RegistryModelUtilities.getAtomIcon() Unable to locate Icon [plugin=" + plugin.getName() + "] [filename=" + filename + "]");
            }

        return (icon);
        }


    /***********************************************************************************************
     * Get the pathname of the root of the folder for the specified AtomPlugin.
     *
     * @param plugin
     * @param installationFolder
     *
     * @return String
     */

    public static String getAtomFolderRoot(final AtomPlugin plugin,
                                           final InstallationFolder installationFolder)
        {
        String strRoot;

        strRoot = FrameworkStrings.EMPTY_STRING;

        if ((plugin != null)
            && (installationFolder != null))
            {
            strRoot = '/' + plugin.getClass().getPackage().getName();
            strRoot = strRoot.replace('.', '/');
            strRoot = strRoot + "/" + installationFolder.getName() + "/";
            strRoot = strRoot.replace(System.getProperty("file.separator").charAt(0), '/');
            }

        //System.out.println("ATOM FOLDER ROOT=" + strRoot);
        return (strRoot);
        }


    /***********************************************************************************************
     * Get the URL of the About file for the specified AtomData.
     * This assumes that the AtomData About is in a folder beneath the Atom root, called FOLDER_ABOUT.
     *
     * @param atom
     *
     * @return URL
     */

    public static URL getAboutURL(final AtomPlugin atom)
        {
        URL urlFile;
        String strFilePath;

        urlFile = null;

        if ((atom != null)
            && (atom.getAboutFilename() != null))
            {
//            System.out.println("Atom {" + atom.getName() + "}");
//            System.out.println("Atom package {" + atom.getClass().getPackage().getName() + "}");
//            System.out.println("Atom help {" + atom.getAboutFilename() + "}");

            strFilePath = '/' + atom.getClass().getPackage().getName();
            strFilePath = strFilePath.replace('.', '/');
            strFilePath = strFilePath
                            + "/"
                            + InstallationFolder.ABOUT.getName()
                            + "/"
                            + atom.getAboutFilename();
            strFilePath = strFilePath.replace(System.getProperty("file.separator").charAt(0), '/');

            // Try to find the URL of the resource
            urlFile = atom.getClass().getResource(strFilePath);

//            System.out.println("Atom About file {" + strFilePath + "}");
//
//            if (urlFile != null)
//                {
//                System.out.println("URL path=" + urlFile.getPath());
//                System.out.println("protocol=" + urlFile.getProtocol());
//                }
//            else
//                {
//                System.out.println("can't find file");
//                }
            }

        return (urlFile);
        }


    /***********************************************************************************************
     * Get the URL of the Help file for the specified AtomPlugin.
     * This assumes that the AtomPlugin help is in a folder beneath the Atom root, called FOLDER_HELP.
     *
     * @param atom
     *
     * @return URL
     */

    public static URL getHelpURL(final AtomPlugin atom)
        {
        URL urlFile;

        urlFile = null;

        if ((atom != null)
            && (atom.getHelpFilename() != null))
            {
            urlFile = getHelpURL(atom, atom.getHelpFilename());
            }

        return (urlFile);
        }


    /***********************************************************************************************
     * Get the URL of a specified file in the Help folder for the specified AtomPlugin.
     * This assumes that the AtomPlugin help is in a folder beneath the Atom root.
     *
     * @param atom
     * @param filename
     *
     * @return URL
     */

    public static URL getHelpURL(final AtomPlugin atom,
                                 final String filename)
        {
        URL urlFile;
        String strFilePath;

        urlFile = null;

        if ((atom != null)
            && (filename != null))
            {
//            System.out.println("Atom {" + atom.getName() + "}");
//            System.out.println("Atom package {" + atom.getClass().getPackage().getName() + "}");
//            System.out.println("Atom help {" + filename + "}");

            strFilePath = '/' + atom.getClass().getPackage().getName();
            strFilePath = strFilePath.replace('.', '/');
            strFilePath = strFilePath
                            + "/"
                            + InstallationFolder.HELP.getName()
                            + "/"
                            + filename;
            strFilePath = strFilePath.replace(System.getProperty("file.separator").charAt(0), '/');

            // Try to find the URL of the resource
            urlFile = atom.getClass().getResource(strFilePath);

//            System.out.println("Atom help file {" + strFilePath + "}");

//            if (urlFile != null)
//                {
//                System.out.println("URL path=" + urlFile.getPath());
//                System.out.println("protocol=" + urlFile.getProtocol());
//                }
//            else
//                {
//                System.out.println("can't find file");
//                }
            }

        return (urlFile);
        }


    /***********************************************************************************************
     * Get the URL of a specified file in the specified folder for the specified AtomPlugin.
     * This assumes that the required folder is beneath the Atom root.
     *
     * @param atom
     * @param folder
     * @param filename
     *
     * @return URL
     */

    public static URL getDistributionURL(final AtomPlugin atom,
                                         final InstallationFolder folder,
                                         final String filename)
        {
        URL urlFile;
        String strFilePath;

        urlFile = null;

        if ((atom != null)
            && (folder != null)
            && (filename != null))
            {
            strFilePath = '/' + atom.getClass().getPackage().getName();
            strFilePath = strFilePath.replace('.', '/');
            strFilePath = strFilePath
                            + "/"
                            + folder.getName()
                            + "/"
                            + filename;
            strFilePath = strFilePath.replace(System.getProperty("file.separator").charAt(0), '/');

            // Try to find the URL of the resource
            urlFile = atom.getClass().getResource(strFilePath);
            }

        return (urlFile);
        }


    /***********************************************************************************************
     * Get the URL of the Javadoc for the specified Class.
     * This assumes that the Javadoc is *all* in a root folder.
     *
     * @param javaclass
     *
     * @return URL
     */

    public static URL getJavadocURL(final Class javaclass)
        {
        URL urlJavadoc;
        String strFilePath;

        urlJavadoc = null;

        if (javaclass != null)
            {
            // Form the path to the package summary of the specified Class
            strFilePath = javaclass.getPackage().getName();
            strFilePath = strFilePath.replace('.', '/');
            strFilePath = "/" + InstallationFolder.JAVADOC.getName() + "/"
                         + strFilePath
                         + '/'
                         + "package-summary.html";
            strFilePath = strFilePath.replace(System.getProperty("file.separator").charAt(0), '/');

            // Try to find the URL of the resource
            urlJavadoc = javaclass.getResource(strFilePath);
            }

        return (urlJavadoc);
        }


    /***********************************************************************************************
     * Get the URL of the file associated with the specified Class,
     * i.e. the full pathname is derived from the class package name.
     * Return NULL if the resource is not found.
     *
     * @param javaclass
     *
     * @return URL
     */

    public static URL getFileURLForClass(final Class javaclass,
                                         final String filename)
        {
        URL urlHelp;

        urlHelp = null;

        if (javaclass != null)
            {
            String strFilePath;

            // Form the path to the Help file associated with the specified Class
            // Search from the Root
            strFilePath = '/' + javaclass.getPackage().getName();
            strFilePath = strFilePath.replace('.', '/');
            strFilePath = strFilePath + "/" + filename;
            strFilePath = strFilePath.replace(System.getProperty("file.separator").charAt(0), '/');

            System.out.println("FILE PATH=" + strFilePath);
            // Try to find the URL of the resource
            urlHelp = javaclass.getResource(strFilePath);
            }

        return (urlHelp);
        }


    /***********************************************************************************************
     * Get the path of the specified folder relative to the host filesystem.
     *
     * @param folder
     * @param separator
     *
     * @return String
     */

    public static String getFilesystemFolder(final String folder,
                                             final boolean separator)
        {
        if (separator)
            {
            return (InstallationFolder.getTerminatedUserDir()
                        + folder
                        + System.getProperty("file.separator"));
            }
        else
            {
            return (InstallationFolder.getTerminatedUserDir()
                        + folder);
            }
        }


    /***********************************************************************************************
     * Get the root of the Help folder.
     *
     * @return String
     */

    public static String getHelpRoot()
        {
        return (InstallationFolder.HELP.getName() + "/");
        }


    /**********************************************************************************************/
    /* Countries                                                                                  */
    /***********************************************************************************************
     * Get an iteration of the installed Countries sorted by the specified CountryName type.
     *
     * @param type CountriesByName.ISO or CountriesByName.IOC
     *
     * @return Iterator
     */

    public static Iterator iterateCountries(final int type)
        {
        final Vector<CountryPlugin> vecCountries;
        final Enumeration<CountryPlugin> enumCountries;

        vecCountries = new Vector<CountryPlugin>(10);
        enumCountries = REGISTRY.getCountries().elements();

        while (enumCountries.hasMoreElements())
            {
            vecCountries.add(enumCountries.nextElement());
            }

        Collections.sort(vecCountries, new CountriesByName(type));

        return (vecCountries.iterator());
        }


    /***********************************************************************************************
     * Get the ISO name of the Country corresponding to the specified <b>two-letter</b> country code.
     *
     * @param code
     *
     * @return String
     */

    public static String getISOCountryName(final String code)
        {
        if ((code != null)
            && (code.length() == CountriesDAOInterface.FIELDSIZE_ISO_CODE_2)
            && (REGISTRY.getCountries().containsKey(code.toUpperCase())))
            {
            return (REGISTRY.getCountries().get(code.toUpperCase()).getISOCountryName());
            }
        else
            {
            return ("XX");
            }
        }

    /***********************************************************************************************
     * Get the IOC name of the Country corresponding to the specified <b>two-letter</b> country code.
     *
     * @param code
     *
     * @return String
     */

    public static String getIOCCountryName(final String code)
        {
        if ((code != null)
            && (code.length() == CountriesDAOInterface.FIELDSIZE_ISO_CODE_2)
            && (REGISTRY.getCountries().containsKey(code.toUpperCase())))
            {
            return (REGISTRY.getCountries().get(code.toUpperCase()).getIOCCountryName());
            }
        else
            {
            return ("XX");
            }
        }

    /***********************************************************************************************
     * Get the national flag FlagIcon for the specified specified <b>two-letter</b> ISO-3166 Country Code.
     * The image files are assumed to be <code>GIF</code> format.
     *
     * @param code
     *
     * @return FlagIcon
     */

    public static FlagIcon getNationalFlagIcon(final String code)
        {
        final String strFlagName;

        if ((code != null)
            && (code.length() == CountriesDAOInterface.FIELDSIZE_ISO_CODE_2)
            && (REGISTRY.getCountries().containsKey(code.toUpperCase())))
            {
            strFlagName = CountryPlugin.FOLDER_FLAGS_COUNTRIES
                          + System.getProperty("file.separator")
                          + code
                          + "."
                          + FileUtilities.gif;

            return (FlagIcon.getIcon(code, strFlagName));
            }
        else
            {
            return (FlagIcon.getIcon(CountryPlugin.DEFAULT_COUNTRY, ""));
            }
        }

    /***********************************************************************************************
     * Get the pathname of the national flag for the specified <b>two-letter</b> ISO-3166 Country Code.
     * The image files are assumed to be <code>GIF</code> format.
     *
     * @param code
     *
     * @return String
     */

    public static String getNationalFlagPathname(final String code)
        {
        final String strFlagName;

        if ((code != null)
            && (code.length() == CountriesDAOInterface.FIELDSIZE_ISO_CODE_2)
            && (REGISTRY.getCountries().containsKey(code.toUpperCase())))
            {
            strFlagName = CountryPlugin.FOLDER_FLAGS_COUNTRIES
                          + System.getProperty("file.separator")
                          + code
                          + "."
                          + FileUtilities.gif;

            return (strFlagName);
            }
        else
            {
            return ("");
            }
        }


    /**********************************************************************************************/
    /* Languages                                                                                  */
    /***********************************************************************************************
     * Get an iteration of the Languages sorted by Language name.
     *
     * @return Iterator
     */

    public static Iterator iterateLanguages()
        {
        final Vector<LanguagePlugin> vecLanguages;
        final Enumeration<LanguagePlugin> enumLanguages;

        vecLanguages = new Vector<LanguagePlugin>(10);
        enumLanguages = REGISTRY.getLanguages().elements();

        while (enumLanguages.hasMoreElements())
            {
            vecLanguages.add(enumLanguages.nextElement());
            }

        Collections.sort(vecLanguages, new LanguagesByName());

        return (vecLanguages.iterator());
        }


    /***********************************************************************************************
     * Get the name of the specified Language.
     *
     * @param code
     * @return String
     */

    public static String getLanguageName(final String code)
        {
        if ((code != null)
            && (code.length() == LanguagePlugin.LENGTH_CODE)
            && (REGISTRY.getLanguages().containsKey(code.toLowerCase())))
            {
            return (REGISTRY.getLanguages().get(code.toLowerCase()).getName());
            }
        else
            {
            return ("");
            }
        }


    /***********************************************************************************************
     * Get the flag FlagIcon for the specified specified <b>two-letter</b> ISO-639 Language Code.
     * The image files are assumed to be <code>GIF</code> format.
     *
     * @param code
     *
     * @return FlagIcon
     */

    public static FlagIcon getLanguageFlagIcon(final String code)
        {
        final String strFlagName;

        if ((code != null)
            && (code.length() == LanguagesDAOInterface.FIELDSIZE_ISO_CODE_2)
            && (REGISTRY.getLanguages().containsKey(code.toLowerCase())))
            {
            strFlagName = LanguagePlugin.FOLDER_FLAGS_LANGUAGES
                          + System.getProperty("file.separator")
                          + code
                          + "."
                          + FileUtilities.gif;

            return (FlagIcon.getIcon(code, strFlagName));
            }
        else
            {
            return (FlagIcon.getIcon(LanguagePlugin.DEFAULT_LANGUAGE, ""));
            }
        }


    /***********************************************************************************************
     * Get the pathname of the flag for the specified <b>two-letter</b> ISO-639 Language Code.
     * The image files are assumed to be <code>GIF</code> format.
     *
     * @param code
     *
     * @return String
     */

    public static String getLanguageFlagPathname(final String code)
        {
        final String strFlagName;

        if ((code != null)
            && (code.length() == LanguagesDAOInterface.FIELDSIZE_ISO_CODE_2)
            && (REGISTRY.getLanguages().containsKey(code.toLowerCase())))
            {
            strFlagName = LanguagePlugin.FOLDER_FLAGS_LANGUAGES
                          + System.getProperty("file.separator")
                          + code
                          + "."
                          + FileUtilities.gif;

            return (strFlagName);
            }
        else
            {
            return ("");
            }
        }


    /***********************************************************************************************
     * Unlink all plugins by resetting the host TreeNodes and UserObjectPlugin links.
     * The parent-child relationships held in the Registry remain untouched.
     *
     * @param plugins
     */

    public static void unlinkPlugins(final Hashtable<String, RootPlugin> plugins)
        {
//        if (plugins != null)
//            {
//            final Enumeration<RootPlugin> enumPlugins;
//
//            enumPlugins = plugins.elements();
//
//            while (enumPlugins.hasMoreElements())
//                {
//                final RootPlugin plugin;
//
//                plugin = enumPlugins.nextElement();
//                plugin.setHostTreeNode(new DefaultMutableTreeNode());
//                plugin.getHostTreeNode().setUserObject(plugin);
//                }
//            }
        }


    /**********************************************************************************************/
    /* Debugging                                                                                  */
    /***********************************************************************************************
     * Show a read-only version of the RegistryModel tree for testing.
     *
     * @param model
     * @param debugmode
     */

    public static void debugModel(final RegistryModelPlugin model,
                                  final boolean debugmode)
        {
        if (model != null)
            {
            if (debugmode)
                {
                final JFrame frameUI = new JFrame("Registry Model Tree Tester");
                frameUI.getContentPane().setLayout(new BorderLayout());

                if (model.getMenuBar() != null)
                    {
                    frameUI.setJMenuBar(model.getMenuBar());
                    }

                if (model.getRootNode() != null)
                    {
                    final JTree treeNavigator = new JTree(model.getRootNode());
                    ToolTipManager.sharedInstance().registerComponent(treeNavigator);
                    treeNavigator.scrollRowToVisible(0);
                    treeNavigator.setSelectionPath(treeNavigator.getPathForRow(0));

                    final JScrollPane scrollpaneTree = new JScrollPane(treeNavigator);
                    frameUI.getContentPane().add(scrollpaneTree);
                    }

                NavigationUtilities.updateComponentTreeUI(frameUI);
                frameUI.pack();
                frameUI.setVisible(true);
                frameUI.validate();

                // Show a list of the running Plugins and Tasks
                showRunners(REGISTRY_MODEL.getRunners());
                }
            }
        else
            {
            LOGGER.debug("debugModel() No Registry Model loaded!");
            }
        }


    /***********************************************************************************************
     * Show all running Plugins and Tasks, in the reverse order to which they were started.
     *
     * @param stack
     */

    public static void showRunners(final Stack<RootPlugin> stack)
        {
        // Traverse the RegistryModel stack of Runners from top to bottom
        if (stack != null)
            {
            LOGGER.log("Registry Model - Running Plugins and Tasks");

            for (int i = stack.size() - 1;
                 (i >= 0);
                 i--)
                {
                final RootPlugin plugin;

                plugin = stack.get(i);
                LOGGER.log(INDENT + plugin.getName());
                }
            }
        }


    /***********************************************************************************************
     * Check that the UserData is valid (but not necessarily Active).
     *
     * @param user
     *
     * @return boolean
     */

    public static boolean isValidUser(final UserPlugin user)
        {
        final boolean boolValid;

        boolValid = ((user != null)
                    && (XmlBeansUtilities.isValidXml(user.getXml()))
                    && (user.getName() != null)
                    && (!EMPTY_STRING.equals(user.getName()))
                    && (user.getPassword() != null)
                    && (!EMPTY_STRING.equals(user.getPassword()))
                    && (user.getRoleName() != null)
                    && (!EMPTY_STRING.equals(user.getRoleName()))
                    && (user.getRole() != null)
                    && (user.getID() >= 0));

        return (boolValid);
        }
    }
