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

//--------------------------------------------------------------------------------------------------
// Revision History
//
//  10-11-04    LMN created file from ApplicationEditor
//  12-11-04    LMN added FrameworkExportsFolder
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.ui.editors;

//--------------------------------------------------------------------------------------------------
// Imports

import org.lmn.fc.common.comparators.CountriesByName;
import org.lmn.fc.common.exceptions.DegMinSecException;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.model.dao.mysql.FrameworkMySqlDAO;
import org.lmn.fc.model.datatypes.*;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.root.UserObjectPlugin;
import org.lmn.fc.ui.components.EditorUIComponent;
import org.lmn.fc.ui.components.EditorUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;


/***************************************************************************************************
 * The FrameworkEditor.
 *
 * ToDo remaining strings to FrameworkStrings
 * ToDo replace Queries in QueryData hashtable
 * ToDo Focus traversal policy
 * ToDo File chooser restricted browsing & grab of correct pathname
 */

public final class FrameworkEditor extends EditorUIComponent
    {
    // String Resources
    private static final String RESOURCE_KEY                = "Editor.Framework.";

    // These keys must not be more than two levels deep
    private static final String KEY_LABEL_NAME              = "Label.Name";
    private static final String KEY_LABEL_ACTIVE            = "Label.Active";
    private static final String KEY_LABEL_EDITABLE          = "Label.Editable";
    private static final String KEY_LABEL_LOADATSTART       = "Label.LoadAtStart";
    private static final String KEY_LABEL_LANGUAGE          = "Label.Language";
    private static final String KEY_LABEL_COUNTRY           = "Label.Country";
    private static final String KEY_LABEL_TIMEZONE          = "Label.TimeZone";
    private static final String KEY_LABEL_LONGITUDE         = "Label.Longitude";
    private static final String KEY_LABEL_LATITUDE          = "Label.Latitude";
    private static final String KEY_LABEL_EXPORTS           = "Label.ExportsFolder";
    private static final String KEY_LABEL_SPLASHSCREEN      = "Label.SplashScreenFilename";
    private static final String KEY_LABEL_ICON              = "Label.IconFilename";
    private static final String KEY_LABEL_HELP              = "Label.HelpFilename";
    private static final String KEY_LABEL_ABOUT             = "Label.AboutFilename";
    private static final String KEY_LABEL_MAP               = "Label.MapFilename";
    private static final String KEY_LABEL_TOPLEFTLONG       = "Label.MapTopLeftLongitude";
    private static final String KEY_LABEL_TOPLEFTLAT        = "Label.MapTopLeftLatitude";
    private static final String KEY_LABEL_BOTTOMRIGHTLONG   = "Label.MapBottomRightLongitude";
    private static final String KEY_LABEL_BOTTOMRIGHTLAT    = "Label.MapBottomRightLatitude";
    private static final String KEY_LABEL_CREATED           = "Label.DateCreated";
    private static final String KEY_LABEL_TIMECREATED       = "Label.TimeCreated";
    private static final String KEY_LABEL_DATEMODIFIED      = "Label.DateModified";
    private static final String KEY_LABEL_TIMEMODIFIED      = "Label.TimeModified";
    private static final String KEY_LABEL_QUERYLIST         = "Label.QueryList";
    private static final String KEY_LABEL_QUERYBYNAME       = "Label.QueryByName";
    private static final String KEY_LABEL_DESCRIPTION       = "Label.Description";

    private static final String KEY_LABEL_COMMIT            = "Label.Commit";
    private static final String KEY_LABEL_REVERT            = "Label.Revert";
    private static final String KEY_LABEL_EXPORTSBROWSER    = "Label.ExportsBrowser";
    private static final String KEY_LABEL_SPLASHSCREENBROWSER = "Label.SplashScreenBrowser";
    private static final String KEY_LABEL_ICONBROWSER       = "Label.IconBrowser";
    private static final String KEY_LABEL_HELPBROWSER       = "Label.HelpBrowser";
    private static final String KEY_LABEL_ABOUTBROWSER      = "Label.AboutBrowser";
    private static final String KEY_LABEL_MAPBROWSER        = "Label.MapBrowser";

    private static final String KEY_TOOLTIP_NAME            = "Tooltip.Name";
    private static final String KEY_TOOLTIP_ACTIVE          = "Tooltip.Active";
    private static final String KEY_TOOLTIP_EDITABLE        = "Tooltip.Editable";
    private static final String KEY_TOOLTIP_LOADATSTART     = "Tooltip.LoadAtStart";
    private static final String KEY_TOOLTIP_LANGUAGE        = "Tooltip.Language";
    private static final String KEY_TOOLTIP_COUNTRY         = "Tooltip.Country";
    private static final String KEY_TOOLTIP_TIMEZONE        = "Tooltip.TimeZone";
    private static final String KEY_TOOLTIP_LONGITUDE       = "Tooltip.Longitude";
    private static final String KEY_TOOLTIP_LATITUDE        = "Tooltip.Latitude";
    private static final String KEY_TOOLTIP_EXPORTS         = "Tooltip.ExportsFolder";
    private static final String KEY_TOOLTIP_SPLASHSCREEN    = "Tooltip.SplashScreenFilename";
    private static final String KEY_TOOLTIP_ICON            = "Tooltip.IconFilename";
    private static final String KEY_TOOLTIP_HELP            = "Tooltip.HelpFilename";
    private static final String KEY_TOOLTIP_ABOUT           = "Tooltip.AboutFilename";
    private static final String KEY_TOOLTIP_MAP             = "Tooltip.MapFilename";
    private static final String KEY_TOOLTIP_TOPLEFTLONG     = "Tooltip.MapTopLeftLongitude";
    private static final String KEY_TOOLTIP_TOPLEFTLAT      = "Tooltip.MapTopLeftLatitude";
    private static final String KEY_TOOLTIP_BOTTOMRIGHTLONG = "Tooltip.MapBottomRightLongitude";
    private static final String KEY_TOOLTIP_BOTTOMRIGHTLAT  = "Tooltip.MapBottomRightLatitude";
    private static final String KEY_TOOLTIP_DATECREATED     = "Tooltip.DateCreated";
    private static final String KEY_TOOLTIP_TIMECREATED     = "Tooltip.TimeCreated";
    private static final String KEY_TOOLTIP_DATEMODIFIED    = "Tooltip.DateModified";
    private static final String KEY_TOOLTIP_TIMEMODIFIED    = "Tooltip.TimeModified";
    private static final String KEY_TOOLTIP_QUERYLIST       = "Tooltip.QueryList";
    private static final String KEY_TOOLTIP_QUERYBYNAME     = "Tooltip.QueryByName";
    private static final String KEY_TOOLTIP_DESCRIPTION     = "Tooltip.Description";

    private static final String KEY_TOOLTIP_COMMIT          = "Tooltip.Commit";
    private static final String KEY_TOOLTIP_REVERT          = "Tooltip.Revert";
    private static final String KEY_TOOLTIP_EXPORTSBROWSER  = "Tooltip.ExportsBrowser";
    private static final String KEY_TOOLTIP_SPLASHSCREENBROWSER = "Tooltip.SplashScreenBrowser";
    private static final String KEY_TOOLTIP_ICONBROWSER     = "Tooltip.IconBrowser";
    private static final String KEY_TOOLTIP_HELPBROWSER     = "Tooltip.HelpBrowser";
    private static final String KEY_TOOLTIP_ABOUTBROWSER    = "Tooltip.AboutBrowser";
    private static final String KEY_TOOLTIP_MAPBROWSER      = "Tooltip.MapBrowser";

    private static final String KEY_EDITOR_TITLE            = "Title";
    private static final String KEY_WARNING_DEACTIVATING    = "Warning.Deactivating";
    private static final String KEY_WARNING_LOADATSTART     = "Warning.LoadAtStart";
    private static final String KEY_WARNING_TRUNCATED       = "Warning.Truncated";
    private static final String KEY_WARNING_SQLSYNTAX       = "Warning.SqlSyntax";
    private static final String KEY_UPDATE_LOCALE           = "Question.Locale";
    private static final String KEY_CONTINUE                = "Continue";

    private static final String KEY_EXCEPTION_CONSTRUCT     = "Construct";

    private static final String TITLE_EXPORTS_BROWSER       = "Select an Exports folder for this Framework";
    private static final String TITLE_SPLASHSCREEN_BROWSER  = "Select a Splash Screen for this Framework";
    private static final String TITLE_ICON_BROWSER          = "Select an Icon for this Framework";
    private static final String TITLE_HELP_BROWSER          = "Select a Help file for this Framework";
    private static final String TITLE_ABOUT_BROWSER         = "Select an About file for this Framework";
    private static final String TITLE_MAP_BROWSER           = "Select a Map file for this Framework";
    private static final String TITLE_SCRIPT_DIALOG         = "Framework Script Creation";

    private static final String MSG_SCRIPT                  = "The Install and Uninstall Scripts have been written to";
    private static final String ERROR_UNEXPECTED            = "An unexpected error has occurred!";
    private static final String ERROR_INCORRECT_FORMAT      = "is not in the correct format";
    private static final String ERROR_DOES_NOT_EXIST        = "does not exist";
    private static final String ERROR_ICON_SIZE             = "Icon size exceeds";
    private static final String ERROR_SCRIPT                = "An error has occurred trying to create the DELETE and INSERT scripts";
    private static final String MSG_CHARACTERS              = "characters";
    private static final String MSG_BY                      = "by";
    private static final String FILENAME_BRACKET_LEFT       = " (";
    private static final String FILENAME_BRACKET_RIGHT      = ") ";

    // The height of the Query Editor box
    private static final int HEIGHT_QUERYEDIT               = 50;
    private static final Dimension DIM_QUERYEDIT_SPACER     = new Dimension(1, HEIGHT_QUERYEDIT- EditorUtilities.HEIGHT_ROW+(int)DIM_ROW_SPACER.getHeight());

    // The width of the JComboBoxes
    private static final int WIDTH_COMBO_BOX                = 250;

    // The number of standard height rows (i.e. not the Description & Queries)
    private static final int ROW_COUNT = 23;

    private final FrameworkPlugin pluginFramework;

    private boolean boolSavedActive;
    private boolean boolSavedEditable;
    private boolean boolSavedLoadAtStart;
    private String strSavedLanguageISOCode;
    private String strSavedCountryISOCode;
    private String strSavedTimeZoneCode;
    private DegMinSecInterface dmsSavedLongitude;
    private DegMinSecInterface dmsSavedLatitude;
    private String strSavedExportsFolder;
    private String strSavedSplashScreenFilename;
    private String strSavedIconFilename;
    private String strSavedHelpFilename;
    private String strSavedAboutFilename;
    private String strSavedMapFilename;
    private DegMinSecInterface dmsSavedTopLeftLongitude;
    private DegMinSecInterface dmsSavedTopLeftLatitude;
    private DegMinSecInterface dmsSavedBottomRightLongitude;
    private DegMinSecInterface dmsSavedBottomRightLatitude;
    private String strSavedQueryList;
    private String strSavedQueryByName;
    private String strSavedDescription;
    private GregorianCalendar dateSavedDateModified;
    private GregorianCalendar timeSavedTimeModified;

    private JPanel panelEditor;
    private JPanel panelLabel;
    private JPanel panelData;
    private JPanel panelButtons;
   // private JPanel panelBrowserExports;
    private JPanel panelBrowserSplashScreen;
    private JPanel panelBrowserIcon;
    private JPanel panelBrowserHelp;
    private JPanel panelBrowserAbout;
    private JPanel panelBrowserMap;

    private JLabel labelName;
    private JLabel labelActive;
    private JLabel labelEditable;
    private JLabel labelLoadAtStart;
    private JLabel labelLanguage;
    private JLabel labelCountry;
    private JLabel labelTimeZone;
    private JLabel labelLongitude;
    private JLabel labelLatitude;
    private JLabel labelExportsFolder;
    private JLabel labelSplashScreenFilename;
    private JLabel labelIconFilename;
    private JLabel labelHelpFilename;
    private JLabel labelAboutFilename;
    private JLabel labelMapFilename;
    private JLabel labelMapTopLeftLongitude;
    private JLabel labelMapTopLeftLatitude;
    private JLabel labelMapBottomRightLongitude;
    private JLabel labelMapBottomRightLatitude;
    private JLabel labelDateCreated;
    private JLabel labelTimeCreated;
    private JLabel labelDateModified;
    private JLabel labelTimeModified;
    private JLabel labelQueryList;
    private JLabel labelQueryByName;
    private JLabel labelDescription;

    private JTextField textName;
    private JCheckBox checkActive;
    private JCheckBox checkEditable;
    private JCheckBox checkLoadAtStart;
    private JComboBox comboLanguage;
    private JComboBox comboCountry;
    private JComboBox comboTimeZone;
    private JTextField textLongitude;
    private JTextField textLatitude;
    //private JTextField textExportsFolder;
    private JTextField textSplashScreenFilename;
    private JTextField textIconFilename;
    private JTextField textHelpFilename;
    private JTextField textAboutFilename;
    private JTextField textMapFilename;
    private JTextField textMapTopLeftLongitude;
    private JTextField textMapTopLeftLatitude;
    private JTextField textMapBottomRightLongitude;
    private JTextField textMapBottomRightLatitude;
    private JTextField textDateCreated;
    private JTextField textTimeCreated;
    private JTextField textDateModified;
    private JTextField textTimeModified;
    //private JScrollPane scrollQueryList;
    //private JTextArea textQueryList;
    //private JScrollPane scrollQueryByName;
    //private JTextArea textQueryByName;
    private JTextArea textDescription;
    private JScrollPane scrollDescription;

    private Vector<JButton> vecButtons;
    private JButton buttonScript;
    private JButton buttonRevert;
    private JButton buttonCommit;
    //private JButton buttonBrowserExports;
    private JButton buttonBrowserSplashScreen;
    private JButton buttonBrowserIcon;
    private JButton buttonBrowserHelp;
    private JButton buttonBrowserAbout;
    private JButton buttonBrowserMap;


    /***********************************************************************************************
     * Constructor creates a editor panel for the supplied Framework.
     *
     * @param plugin
     */

    public FrameworkEditor(final UserObjectPlugin plugin)
        {
        // Create the EditorUtilities for <Framework>.Editor.Framework.
        super(plugin.getResourceKey() + RESOURCE_KEY);

        // Save the Framework to be edited by upcasting the incoming UserObjectPlugin
        pluginFramework = (FrameworkPlugin)plugin;
        saveFramework(pluginFramework);

        // Read colours etc.
        readResources();

        // Attempt to lay out the Editor
        if (!createEditorPanel())
            {
            LOGGER.handleAtomException(REGISTRY.getFramework(),
                                       REGISTRY.getFramework().getRootTask(),
                                       this.getClass().getName(),
                                       new FrameworkException(REGISTRY.getException(getResourceKey() + KEY_EXCEPTION_CONSTRUCT)),
                                       REGISTRY.getString(getResourceKey() + KEY_EDITOR_TITLE),
                                       EventStatus.WARNING);
            }
        }


    /***********************************************************************************************
     * Create the Editor panel.
     *
     * @return boolean
     */

    private boolean createEditorPanel()
        {
        final int intLabelHeight;

        // The left-hand label panel

        labelName = createLabel(getTextColour(),
                                getLabelFont(),
                                KEY_LABEL_NAME);

        labelActive = createLabel(getTextColour(),
                                  getLabelFont(),
                                  KEY_LABEL_ACTIVE);

        labelEditable = createLabel(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_EDITABLE);

        labelLoadAtStart = createLabel(getTextColour(),
                                       getLabelFont(),
                                       KEY_LABEL_LOADATSTART);

        labelLanguage = createLabel(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_LANGUAGE);

        labelCountry = createLabel(getTextColour(),
                                   getLabelFont(),
                                   KEY_LABEL_COUNTRY);

        labelTimeZone = createLabel(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_TIMEZONE);

        labelLongitude = createLabel(getTextColour(),
                                     getLabelFont(),
                                     KEY_LABEL_LONGITUDE);

        labelLatitude = createLabel(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_LATITUDE);

        labelExportsFolder = createLabel(getTextColour(),
                                         getLabelFont(),
                                         KEY_LABEL_EXPORTS);

        labelSplashScreenFilename = createLabel(getTextColour(),
                                                getLabelFont(),
                                                KEY_LABEL_SPLASHSCREEN);

        labelIconFilename = createLabel(getTextColour(),
                                        getLabelFont(),
                                        KEY_LABEL_ICON);

        labelHelpFilename = createLabel(getTextColour(),
                                        getLabelFont(),
                                        KEY_LABEL_HELP);

        labelAboutFilename = createLabel(getTextColour(),
                                         getLabelFont(),
                                         KEY_LABEL_ABOUT);

        labelMapFilename = createLabel(getTextColour(),
                                       getLabelFont(),
                                       KEY_LABEL_MAP);

        labelMapTopLeftLongitude = createLabel(getTextColour(),
                                               getLabelFont(),
                                               KEY_LABEL_TOPLEFTLONG);

        labelMapTopLeftLatitude = createLabel(getTextColour(),
                                              getLabelFont(),
                                              KEY_LABEL_TOPLEFTLAT);

        labelMapBottomRightLongitude = createLabel(getTextColour(),
                                                   getLabelFont(),
                                                   KEY_LABEL_BOTTOMRIGHTLONG);

        labelMapBottomRightLatitude = createLabel(getTextColour(),
                                                  getLabelFont(),
                                                  KEY_LABEL_BOTTOMRIGHTLAT);

        labelDateCreated = createLabel(getTextColour(),
                                       getLabelFont(),
                                       KEY_LABEL_CREATED);

        labelTimeCreated = createLabel(getTextColour(),
                                       getLabelFont(),
                                       KEY_LABEL_TIMECREATED);

        labelDateModified = createLabel(getTextColour(),
                                        getLabelFont(),
                                        KEY_LABEL_DATEMODIFIED);

        labelTimeModified = createLabel(getTextColour(),
                                        getLabelFont(),
                                        KEY_LABEL_TIMEMODIFIED);

        labelQueryList = createLabel(getTextColour(),
                                     getLabelFont(),
                                     KEY_LABEL_QUERYLIST);

        labelQueryByName = createLabel(getTextColour(),
                                       getLabelFont(),
                                       KEY_LABEL_QUERYBYNAME);

        labelDescription = createLabel(getTextColour(),
                                       getLabelFont(),
                                       KEY_LABEL_DESCRIPTION);

        //  The right-hand data panel

        textName = createTextField(getTextColour(),
                                   getDataFont(),
                                   pluginFramework.getPathname(),
                                   KEY_TOOLTIP_NAME,
                                   false);

        checkActive = createCheckBox(getCanvasColour(),
                                     KEY_TOOLTIP_ACTIVE,
                                     pluginFramework.isEditable(),
                                     pluginFramework.isActive());

        checkEditable = createCheckBox(getCanvasColour(),
                                       KEY_TOOLTIP_EDITABLE,
                                       pluginFramework.isEditable(),
                                       pluginFramework.isEditable());

        // This must always be editable...
        checkLoadAtStart = createCheckBox(getCanvasColour(),
                                          KEY_TOOLTIP_LOADATSTART,
                                          true,
                                          pluginFramework.isLoadAtStart());

        comboLanguage = createComboBox(getTextColour(),
                                       getDataFont(),
                                       KEY_TOOLTIP_LANGUAGE,
                                       pluginFramework.isEditable(),
                                       new Vector().iterator(),
                                       "");

        // Populate the Languages drop-down
        comboLanguage.removeAllItems();
        final Iterator iterLanguages = RegistryModelUtilities.iterateLanguages();

        while (iterLanguages.hasNext())
            {
            comboLanguage.addItem(iterLanguages.next());
            }

        // Select the Language for this Framework
        comboLanguage.setSelectedItem(REGISTRY.getLanguage(PREFIX_LANGUAGE
                                                              + KEY_DELIMITER
                                                              + pluginFramework.getLanguageISOCode()));
        EditorUtilities.setComponentWidth(comboLanguage, WIDTH_COMBO_BOX);

        comboCountry = createComboBox(getTextColour(),
                                      getDataFont(),
                                      KEY_TOOLTIP_COUNTRY,
                                      pluginFramework.isEditable(),
                                      new Vector().iterator(),
                                      "");

        // Populate the Countries drop-down with names in ISO order
        comboCountry.removeAllItems();
        final Iterator iterCountries = RegistryModelUtilities.iterateCountries(CountriesByName.ISO);

        while (iterCountries.hasNext())
            {
            comboCountry.addItem(iterCountries.next());
            }

        // Select the Country for this Framework
        comboCountry.setSelectedItem(REGISTRY.getCountry(PREFIX_COUNTRY
                                                            + KEY_DELIMITER
                                                            + pluginFramework.getCountryISOCode()));
        EditorUtilities.setComponentWidth(comboCountry, WIDTH_COMBO_BOX);

        comboTimeZone = createComboBox(getTextColour(),
                                       getDataFont(),
                                       KEY_TOOLTIP_TIMEZONE,
                                       pluginFramework.isEditable(),
                                       new Vector().iterator(),
                                       "");

        final Iterator iterTimeZones = (new TimeZoneList()).iterator();
        comboTimeZone.removeAllItems();

        while (iterTimeZones.hasNext())
            {
            comboTimeZone.addItem(iterTimeZones.next());
            }

        comboTimeZone.setSelectedItem(pluginFramework.getTimeZoneCode());
        EditorUtilities.setComponentWidth(comboTimeZone, WIDTH_COMBO_BOX);

        DegMinSecInterface dmsLongitude = pluginFramework.getLongitude();
        dmsLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        dmsLongitude.setDisplayFormat(DegMinSecFormat.SIGN);
        textLongitude = createTextField(getTextColour(),
                                        getDataFont(),
                                        dmsLongitude.toString(),
                                        KEY_TOOLTIP_LONGITUDE,
                                        pluginFramework.isEditable());
        EditorUtilities.adjustNarrowField(textLongitude);

        DegMinSecInterface dmsLatitude = pluginFramework.getLatitude();
        dmsLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        dmsLatitude.setDisplayFormat(DegMinSecFormat.SIGN);
        textLatitude = createTextField(getTextColour(),
                                       getDataFont(),
                                       dmsLatitude.toString(),
                                       KEY_TOOLTIP_LATITUDE,
                                       pluginFramework.isEditable());
        EditorUtilities.adjustNarrowField(textLatitude);

//        textExportsFolder = createTextField(getTextColour(),
//                                            getDataFont(),
//                                            pluginFramework.getExportsFolder(),
//                                            KEY_TOOLTIP_EXPORTS,
//                                            pluginFramework.isEditable());

        textSplashScreenFilename = createTextField(getTextColour(),
                                                   getDataFont(),
                                                   pluginFramework.getSplashScreenFilename(),
                                                   KEY_TOOLTIP_SPLASHSCREEN,
                                                   pluginFramework.isEditable());

        textIconFilename = createTextField(getTextColour(),
                                           getDataFont(),
                                           pluginFramework.getIconFilename(),
                                           KEY_TOOLTIP_ICON,
                                           pluginFramework.isEditable());

        textHelpFilename = createTextField(getTextColour(),
                                           getDataFont(),
                                           pluginFramework.getHelpFilename(),
                                           KEY_TOOLTIP_HELP,
                                           pluginFramework.isEditable());

        textAboutFilename = createTextField(getTextColour(),
                                            getDataFont(),
                                            pluginFramework.getAboutFilename(),
                                            KEY_TOOLTIP_ABOUT,
                                            pluginFramework.isEditable());

        textMapFilename = createTextField(getTextColour(),
                                          getDataFont(),
                                          pluginFramework.getMapFilename(),
                                          KEY_TOOLTIP_MAP,
                                          pluginFramework.isEditable());

        pluginFramework.getMapTopLeftLongitude().apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        pluginFramework.getMapTopLeftLongitude().setDisplayFormat(DegMinSecFormat.SIGN);
        textMapTopLeftLongitude = createTextField(getTextColour(),
                                                  getDataFont(),
                                                  pluginFramework.getMapTopLeftLongitude().toString(),
                                                  KEY_TOOLTIP_TOPLEFTLONG,
                                                  pluginFramework.isEditable());
        EditorUtilities.adjustNarrowField(textMapTopLeftLongitude);

        pluginFramework.getMapTopLeftLatitude().apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        pluginFramework.getMapTopLeftLatitude().setDisplayFormat(DegMinSecFormat.SIGN);
        textMapTopLeftLatitude = createTextField(getTextColour(),
                                                 getDataFont(),
                                                 pluginFramework.getMapTopLeftLatitude().toString(),
                                                 KEY_TOOLTIP_TOPLEFTLAT,
                                                 pluginFramework.isEditable());
        EditorUtilities.adjustNarrowField(textMapTopLeftLatitude);

        pluginFramework.getMapBottomRightLongitude().apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
        pluginFramework.getMapBottomRightLongitude().setDisplayFormat(DegMinSecFormat.SIGN);
        textMapBottomRightLongitude = createTextField(getTextColour(),
                                                      getDataFont(),
                                                      pluginFramework.getMapBottomRightLongitude().toString(),
                                                      KEY_TOOLTIP_BOTTOMRIGHTLONG,
                                                      pluginFramework.isEditable());
        EditorUtilities.adjustNarrowField(textMapBottomRightLongitude);

        pluginFramework.getMapBottomRightLatitude().apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
        pluginFramework.getMapBottomRightLatitude().setDisplayFormat(DegMinSecFormat.SIGN);
        textMapBottomRightLatitude = createTextField(getTextColour(),
                                                     getDataFont(),
                                                     pluginFramework.getMapBottomRightLatitude().toString(),
                                                     KEY_TOOLTIP_BOTTOMRIGHTLAT,
                                                     pluginFramework.isEditable());
        EditorUtilities.adjustNarrowField(textMapBottomRightLatitude);

        textDateCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toDateString(pluginFramework.getCreatedDate()),
                                          KEY_TOOLTIP_DATECREATED,
                                          false);

        textTimeCreated = createTextField(getTextColour(),
                                          getDataFont(),
                                          ChronosHelper.toTimeString(pluginFramework.getCreatedTime()),
                                          KEY_TOOLTIP_TIMECREATED,
                                          false);

        textDateModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toDateString(pluginFramework.getModifiedDate()),
                                           KEY_TOOLTIP_DATEMODIFIED,
                                           false);

        textTimeModified = createTextField(getTextColour(),
                                           getDataFont(),
                                           ChronosHelper.toTimeString(pluginFramework.getModifiedTime()),
                                           KEY_TOOLTIP_TIMEMODIFIED,
                                           false);

        // Adjust the sizes of the DateTime fields to reduce screen clutter
        EditorUtilities.adjustNarrowField(textDateCreated);
        EditorUtilities.adjustNarrowField(textTimeCreated);
        EditorUtilities.adjustNarrowField(textDateModified);
        EditorUtilities.adjustNarrowField(textTimeModified);

//        textQueryList = createTextArea(getTextColour(),
//                                       getDataFont(),
//                                       pluginFramework.getSelectQueryList(),
//                                       KEY_TOOLTIP_QUERYLIST,
//                                       HEIGHT_QUERYEDIT,
//                                       pluginFramework.isEditable());
//
//        textQueryByName = createTextArea(getTextColour(),
//                                         getDataFont(),
//                                         pluginFramework.getSelectQueryByName(),
//                                         KEY_TOOLTIP_QUERYBYNAME,
//                                         HEIGHT_QUERYEDIT,
//                                         pluginFramework.isEditable());

        textDescription = createTextArea(getTextColour(),
                                         getDataFont(),
                                         pluginFramework.getDescription(),
                                         KEY_TOOLTIP_DESCRIPTION,
                                         HEIGHT_QUERYEDIT,
                                         pluginFramework.isEditable());

        //------------------------------------------------------------------------------------------
        // Add the ActionListeners now we have stopped changing component states
        // The database daemon must not write data until saveEventLog is complete

        // Checkbox and Combobox listener
        final ItemListener changeListener = new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                dataChanged();
                }
            };

        checkActive.addItemListener(changeListener);
        checkLoadAtStart.addItemListener(changeListener);
        comboLanguage.addItemListener(changeListener);
        comboCountry.addItemListener(changeListener);
        comboTimeZone.addItemListener(changeListener);

        // FrameworkEditable must be handled differently...
        final ItemListener editableListener = new ItemListener()
            {
            public void itemStateChanged(final ItemEvent event)
                {
                final boolean boolState;

                boolState = (event.getStateChange() == ItemEvent.SELECTED);

                LOGGER.debug("checkEditable changed");

                // The database daemon must not write data until saveEventLog is complete
                dataChanged();

//                textName.setEnabled(boolState);
                checkActive.setEnabled(boolState);
                checkLoadAtStart.setEnabled(boolState);
                comboLanguage.setEnabled(boolState);
                comboCountry.setEnabled(boolState);
                comboTimeZone.setEnabled(boolState);
                textLongitude.setEnabled(boolState);
                textLatitude.setEnabled(boolState);
//                textExportsFolder.setEnabled(boolState);
                textSplashScreenFilename.setEnabled(boolState);
                textIconFilename.setEnabled(boolState);
                textHelpFilename.setEnabled(boolState);
                textAboutFilename.setEnabled(boolState);
                textMapFilename.setEnabled(boolState);
                textMapTopLeftLongitude.setEnabled(boolState);
                textMapTopLeftLatitude.setEnabled(boolState);
                textMapBottomRightLongitude.setEnabled(boolState);
                textMapBottomRightLatitude.setEnabled(boolState);
//                textQueryList.setEnabled(boolState);
//                textQueryByName.setEnabled(boolState);
                textDescription.setEnabled(boolState);

//                buttonBrowserExports.setEnabled(boolState);
                buttonBrowserSplashScreen.setEnabled(boolState);
                buttonBrowserIcon.setEnabled(boolState);
                buttonBrowserHelp.setEnabled(boolState);
                buttonBrowserAbout.setEnabled(boolState);
                buttonBrowserMap.setEnabled(boolState);
                }
            };

        checkEditable.addItemListener(editableListener);

        // Text Box Listener
        final DocumentListener listenerText = new DocumentListener()
            {
            public void insertUpdate(final DocumentEvent event)
                {
                dataChanged();
                }

            public void removeUpdate(final DocumentEvent event)
                {
                dataChanged();
                }

            public void changedUpdate(final DocumentEvent event)
                {
                dataChanged();
                }
            };

        textName.getDocument().addDocumentListener(listenerText);
        textLongitude.getDocument().addDocumentListener(listenerText);
        textLatitude.getDocument().addDocumentListener(listenerText);
//        textExportsFolder.getDocument().addDocumentListener(listenerText);
        textSplashScreenFilename.getDocument().addDocumentListener(listenerText);
        textIconFilename.getDocument().addDocumentListener(listenerText);
        textHelpFilename.getDocument().addDocumentListener(listenerText);
        textAboutFilename.getDocument().addDocumentListener(listenerText);
        textMapFilename.getDocument().addDocumentListener(listenerText);
        textMapTopLeftLongitude.getDocument().addDocumentListener(listenerText);
        textMapTopLeftLatitude.getDocument().addDocumentListener(listenerText);
        textMapBottomRightLongitude.getDocument().addDocumentListener(listenerText);
        textMapBottomRightLatitude.getDocument().addDocumentListener(listenerText);
//        textQueryList.getDocument().addDocumentListener(listenerText);
//        textQueryByName.getDocument().addDocumentListener(listenerText);
        textDescription.getDocument().addDocumentListener(listenerText);

        //------------------------------------------------------------------------------------------
        // The Commit button and its listener

        buttonCommit = createButton(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_COMMIT,
                                    KEY_TOOLTIP_COMMIT,
                                    "buttonCommit",
                                    false);

        final ActionListener commitListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                final boolean boolActive;
                final boolean boolLoadAtStart;

                final String strExportsFolder;
                final String strSplashScreenFilename;
                final String strIconFilename;
                final String strHelpFilename;
                final String strAboutFilename;
                final String strMapFilename;

                final String strLongitude;
                final String strLatitude;
                final String strTopLeftLongitude;
                final String strTopLeftLatitude;
                final String strBottomRightLongitude;
                final String strBottomRightLatitude;

                String strQueryList;
                String strQueryByName;
                String strDescription;

                String strLogText;
                boolean boolValid;
                int intChoice;
                final ArrayList<String> listErrors;

                boolValid = true;
                listErrors = new ArrayList<String>(10);

                // We must check each item for validity, and write to the
                // RegistryModel if all is Ok, otherwise, give the user another chance
                // Initialise the chain of choices
                intChoice = JOptionPane.OK_OPTION;

                //----------------------------------------------------------------------------------
                // Read the edited items which must be validated

                boolActive = checkActive.isSelected();
                boolLoadAtStart = checkLoadAtStart.isSelected();

//                strExportsFolder = textExportsFolder.getText().trim();
                strSplashScreenFilename = textSplashScreenFilename.getText().trim();
                strIconFilename = textIconFilename.getText().trim();
                strHelpFilename = textHelpFilename.getText().trim();
                strAboutFilename = textAboutFilename.getText().trim();
                strMapFilename = textMapFilename.getText().trim();

                strLongitude = textLongitude.getText().trim();
                strLatitude = textLatitude.getText().trim();
                strTopLeftLongitude = textMapTopLeftLongitude.getText().trim();
                strTopLeftLatitude = textMapTopLeftLatitude.getText().trim();
                strBottomRightLongitude = textMapBottomRightLongitude.getText().trim();
                strBottomRightLatitude = textMapBottomRightLatitude.getText().trim();

//                strQueryList = textQueryList.getText().trim();
//                strQueryByName = textQueryByName.getText().trim();
                strDescription = textDescription.getText().trim();

                //----------------------------------------------------------------------------------
                // Warn the user if the Framework is being deactivated

                if ((!boolActive)
                    && (boolActive != boolSavedActive))
                    {
                    final String [] strWarning =
                        {
                        pluginFramework.getName() + SPACE +
                        REGISTRY.getString(getResourceKey() + KEY_WARNING_DEACTIVATING),
                        REGISTRY.getString(getResourceKey() + KEY_CONTINUE)
                        };

                    intChoice = JOptionPane.showOptionDialog(null,
                                                             strWarning,
                                                             pluginFramework.getName() + SPACE + REGISTRY.getString(getResourceKey() + KEY_EDITOR_TITLE),
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.WARNING_MESSAGE,
                                                             null,
                                                             null,
                                                             null);
                    }

                // Does the user want to proceed?
                if (intChoice == JOptionPane.OK_OPTION)
                    {
                    // Warn the user if this Framework won't be loaded next time...
                    if (!boolLoadAtStart)
                        {
                        final String [] strWarning =
                            {
                            pluginFramework.getName() + SPACE +
                            REGISTRY.getString(getResourceKey() + KEY_WARNING_LOADATSTART),
                            REGISTRY.getString(getResourceKey() + KEY_CONTINUE)
                            };

                        intChoice = JOptionPane.showOptionDialog(null,
                                                                 strWarning,
                                                                 pluginFramework.getName() + SPACE + REGISTRY.getString(getResourceKey() + KEY_EDITOR_TITLE),
                                                                 JOptionPane.YES_NO_OPTION,
                                                                 JOptionPane.WARNING_MESSAGE,
                                                                 null,
                                                                 null,
                                                                 null);
                        }

                    // Does the user still want to proceed?
                    if (intChoice == JOptionPane.OK_OPTION)
                        {
                        // Check that the various filenames are valid
                        // Blank entries are acceptable

//                        if (strExportsFolder.equals(""))
//                            {
//                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_EXPORTS)
//                                           + FILENAME_BRACKET_LEFT
//                                           + strExportsFolder
//                                           + FILENAME_BRACKET_RIGHT
//                                           + ERROR_DOES_NOT_EXIST);
//                            boolValid = false;
//                            }

                        if (!strSplashScreenFilename.equals(""))
                            {
                            // See if the LoginPanel image file exists
                            final File file = RegistryModelUtilities.getCommonImageAsFile(strSplashScreenFilename);

                            if (!file.exists())
                                {
                                listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_SPLASHSCREEN)
                                               + FILENAME_BRACKET_LEFT
                                               + strSplashScreenFilename
                                               + FILENAME_BRACKET_RIGHT
                                               + ERROR_DOES_NOT_EXIST);
                                boolValid = false;
                                }
                            }

                        // Check that the Icon filename is valid
                        if (!strIconFilename.equals(""))
                            {
                            // See if the Icon file exists
                            final File file = RegistryModelUtilities.getCommonImageAsFile(strIconFilename);

                            if (file.exists())
                                {
                                // The file exists, so try to make an Icon of valid size
                                final ImageIcon imageIcon = RegistryModelUtilities.getCommonIcon(strIconFilename);
                                LOGGER.debug("Icon size: width=" + imageIcon.getIconWidth() + " height=" + imageIcon.getIconHeight());

                                if ((imageIcon.getIconWidth() > RegistryModelPlugin.ICON_WIDTH)
                                    || (imageIcon.getIconHeight() > RegistryModelPlugin.ICON_HEIGHT))
                                    {
                                    listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_ICON)
                                                   + FILENAME_BRACKET_LEFT
                                                   + strIconFilename
                                                   + FILENAME_BRACKET_RIGHT
                                                   + ERROR_ICON_SIZE
                                                   + SPACE
                                                   + RegistryModelPlugin.ICON_WIDTH
                                                   + SPACE + MSG_BY + SPACE
                                                   + RegistryModelPlugin.ICON_HEIGHT);
                                    boolValid = false;
                                    }
                                }
                            else
                                {
                                listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_ICON)
                                               + FILENAME_BRACKET_LEFT
                                               + strIconFilename
                                               + FILENAME_BRACKET_RIGHT
                                               + ERROR_DOES_NOT_EXIST);
                                boolValid = false;
                                }
                            }

                        if (!strHelpFilename.equals(""))
                            {
                            // See if the Help file exists
                            final File file = new File(RegistryModelUtilities.getHelpRoot()
                                                       + strHelpFilename);

                            if (!file.exists())
                                {
                                listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_HELP)
                                               + FILENAME_BRACKET_LEFT
                                               + strHelpFilename
                                               + FILENAME_BRACKET_RIGHT
                                               + ERROR_DOES_NOT_EXIST);
                                boolValid = false;
                                }
                            }

                        if (!strAboutFilename.equals(""))
                            {
                            // See if the About file exists
                            final File file = new File(RegistryModelUtilities.getHelpRoot()
                                                       + strAboutFilename);

                            if (!file.exists())
                                {
                                listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_ABOUT)
                                               + FILENAME_BRACKET_LEFT
                                               + strAboutFilename
                                               + FILENAME_BRACKET_RIGHT
                                               + ERROR_DOES_NOT_EXIST);
                                boolValid = false;
                                }
                            }

                        if (!strMapFilename.equals(""))
                            {
                            // See if the Map file exists
                            final File file = RegistryModelUtilities.getCommonImageAsFile(strMapFilename);

                            if (!file.exists())
                                {
                                listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_MAP)
                                               + FILENAME_BRACKET_LEFT
                                               + strMapFilename
                                               + FILENAME_BRACKET_RIGHT
                                               + ERROR_DOES_NOT_EXIST);
                                boolValid = false;
                                }
                            }

                        //--------------------------------------------------------------------------
                        // Validate the DegMinSec

                        if (DataTypeHelper.validateDataTypeOfValueField(strLongitude, DataTypeDictionary.SIGNED_LONGITUDE, listErrors) > 0)
                            {
                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_LONGITUDE) + SPACE + ERROR_INCORRECT_FORMAT);
                            boolValid = false;
                            }

                        if (DataTypeHelper.validateDataTypeOfValueField(strLatitude, DataTypeDictionary.LATITUDE, listErrors) > 0)
                            {
                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_LATITUDE) + SPACE + ERROR_INCORRECT_FORMAT);
                            boolValid = false;
                            }

                        if (DataTypeHelper.validateDataTypeOfValueField(strTopLeftLongitude, DataTypeDictionary.SIGNED_LONGITUDE, listErrors) > 0)
                            {
                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_TOPLEFTLONG) + SPACE + ERROR_INCORRECT_FORMAT);
                            boolValid = false;
                            }

                        if (DataTypeHelper.validateDataTypeOfValueField(strTopLeftLatitude, DataTypeDictionary.LATITUDE, listErrors) > 0)
                            {
                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_TOPLEFTLAT) + SPACE + ERROR_INCORRECT_FORMAT);
                            boolValid = false;
                            }

                        if (DataTypeHelper.validateDataTypeOfValueField(strBottomRightLongitude, DataTypeDictionary.SIGNED_LONGITUDE, listErrors) > 0)
                            {
                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_BOTTOMRIGHTLONG) + SPACE + ERROR_INCORRECT_FORMAT);
                            boolValid = false;
                            }

                        if (DataTypeHelper.validateDataTypeOfValueField(strBottomRightLatitude, DataTypeDictionary.LATITUDE, listErrors) > 0)
                            {
                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_BOTTOMRIGHTLAT) + SPACE + ERROR_INCORRECT_FORMAT);
                            boolValid = false;
                            }

                        //--------------------------------------------------------------------------
                        // Validate Strings

                        // Check that the Queries and Description are not too large
                        // Issue a truncation warning if so

//                        if (strQueryList.length() > FrameworkMySqlDAO.QUERY_LENGTH)
//                            {
//                            strQueryList = strQueryList.substring(0, FrameworkMySqlDAO.QUERY_LENGTH-1);
//
//                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_QUERYLIST)
//                                           + SPACE
//                                           + REGISTRY.getString(getResourceKey() + KEY_WARNING_TRUNCATED)
//                                           + SPACE
//                                           + FrameworkMySqlDAO.QUERY_LENGTH
//                                           + SPACE
//                                           + MSG_CHARACTERS);
//                            }

                        // Check the syntax of the SQL by trying to make a PreparedStatement
                        // This doesn't always show anything useful!
//                        try
//                            {
//                            if ((pluginFramework != null)
//                                && (FrameworkDatabase.getInstance() != null)
//                                && (FrameworkDatabase.getInstance().getConnection() != null)
//                                && (!FrameworkDatabase.getInstance().getConnection().isClosed()))
//                                {
//                                FrameworkDatabase.getInstance().getConnection().prepareStatement(strQueryList);
//                                }
//                            }
//
//                        catch (SQLException exception)
//                            {
//                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_QUERYLIST)
//                                           + SPACE
//                                           + REGISTRY.getString(getResourceKey() + KEY_WARNING_SQLSYNTAX));
//                            boolValid = false;
//                            }

//                        if (strQueryByName.length() > FrameworkMySqlDAO.QUERY_LENGTH)
//                            {
//                            strQueryByName = strQueryByName.substring(0, FrameworkMySqlDAO.QUERY_LENGTH-1);
//
//                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_QUERYBYNAME)
//                                           + SPACE
//                                           + REGISTRY.getString(getResourceKey() + KEY_WARNING_TRUNCATED)
//                                           + SPACE
//                                           + FrameworkMySqlDAO.QUERY_LENGTH
//                                           + SPACE
//                                           + MSG_CHARACTERS);
//                            }

                        // Check the syntax of the SQL by trying to make a PreparedStatement
//                        try
//                            {
//                            if ((pluginFramework != null)
//                                && (FrameworkDatabase.getInstance() != null)
//                                && (FrameworkDatabase.getInstance().getConnection() != null)
//                                && (!FrameworkDatabase.getInstance().getConnection().isClosed()))
//                                {
//                                FrameworkDatabase.getInstance().getConnection().prepareStatement(strQueryByName);
//                                }
//                            }
//
//                        catch (SQLException exception)
//                            {
//                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_QUERYBYNAME)
//                                           + SPACE
//                                           + REGISTRY.getString(getResourceKey() + KEY_WARNING_SQLSYNTAX));
//                            boolValid = false;
//                            }

                        if (strDescription.length() > FrameworkMySqlDAO.DESCRIPTION_LENGTH)
                            {
                            strDescription = strDescription.substring(0, FrameworkMySqlDAO.DESCRIPTION_LENGTH-1);

                            listErrors.add(REGISTRY.getString(getResourceKey() + KEY_LABEL_DESCRIPTION)
                                           + SPACE
                                           + REGISTRY.getString(getResourceKey() + KEY_WARNING_TRUNCATED)
                                           + SPACE
                                           + FrameworkMySqlDAO.DESCRIPTION_LENGTH
                                           + SPACE
                                           + MSG_CHARACTERS);
                            }

                        //--------------------------------------------------------------------------
                        // The edited data seems to be Ok, so write it back to the Framework

                        if (boolValid)
                            {
                            // Are there any warnings?
                            if (!listErrors.isEmpty())
                                {
                                JOptionPane.showMessageDialog(null,
                                                              listErrors.toArray(),
                                                              pluginFramework.getName() + SPACE + REGISTRY.getString(getResourceKey() + KEY_EDITOR_TITLE),
                                                              JOptionPane.WARNING_MESSAGE);
                                }

                            // Set all changed values ready for the update
                            // This creates an ActiveChangeEvent when the state changes
                            pluginFramework.setActive(checkActive.isSelected());
                            pluginFramework.setEditable(checkEditable.isSelected());
                            pluginFramework.setLoadAtStart(checkLoadAtStart.isSelected());

                            if ((comboLanguage != null)
                                && (comboLanguage.getSelectedItem() != null)
                                && (comboLanguage.getSelectedItem() instanceof LanguagePlugin))
                                {
                                pluginFramework.setLanguageISOCode(((LanguagePlugin)comboLanguage.getSelectedItem()).getISOCode2());
                                }

                            if ((comboCountry != null)
                                && (comboCountry.getSelectedItem() != null)
                                && (comboCountry.getSelectedItem() instanceof CountryPlugin))
                                {
                                pluginFramework.setCountryISOCode(((CountryPlugin)comboCountry.getSelectedItem()).getISOCode2());
                                }

                            if ((comboTimeZone != null)
                                && (comboTimeZone.getSelectedItem() != null)
                                && (comboTimeZone.getSelectedItem() instanceof String))
                                {
                                pluginFramework.setTimeZoneCode((String)comboTimeZone.getSelectedItem());
                                }

//                            pluginFramework.setExportsFolder(replaceNull(strExportsFolder));
                            pluginFramework.setSplashScreenFilename(EditorUtilities.replaceNull(strSplashScreenFilename));
                            pluginFramework.setIconFilename(EditorUtilities.replaceNull(strIconFilename));
                            pluginFramework.setHelpFilename(EditorUtilities.replaceNull(strHelpFilename));
                            pluginFramework.setAboutFilename(EditorUtilities.replaceNull(strAboutFilename));
                            pluginFramework.setMapFilename(EditorUtilities.replaceNull(strMapFilename));

                            try
                                {
                                final java.util.List<String> errors;

                                errors = new ArrayList<String>(10);

                                pluginFramework.setLongitude((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strLongitude,
                                                                                                                             DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                                             EMPTY_STRING,
                                                                                                                             EMPTY_STRING,
                                                                                                                             errors));

                                pluginFramework.setLatitude((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strLatitude,
                                                                                                                            DataTypeDictionary.LATITUDE,
                                                                                                                            EMPTY_STRING,
                                                                                                                            EMPTY_STRING,
                                                                                                                            errors));

                                pluginFramework.setMapTopLeftLongitude((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strTopLeftLongitude,
                                                                                                                                       DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                                                       EMPTY_STRING,
                                                                                                                                       EMPTY_STRING,
                                                                                                                                       errors));

                                pluginFramework.setMapTopLeftLatitude((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strTopLeftLatitude,
                                                                                                                                      DataTypeDictionary.LATITUDE,
                                                                                                                                      EMPTY_STRING,
                                                                                                                                      EMPTY_STRING,
                                                                                                                                      errors));

                                pluginFramework.setMapBottomRightLongitude((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strBottomRightLongitude,
                                                                                                                                           DataTypeDictionary.SIGNED_LONGITUDE,
                                                                                                                                           EMPTY_STRING,
                                                                                                                                           EMPTY_STRING,
                                                                                                                                           errors));

                                pluginFramework.setMapBottomRightLatitude((DegMinSecInterface) DataTypeHelper.parseDataTypeFromValueField(strBottomRightLatitude,
                                                                                                                                          DataTypeDictionary.LATITUDE,
                                                                                                                                          EMPTY_STRING,
                                                                                                                                          EMPTY_STRING,
                                                                                                                                          errors));
                                }

                            catch (DegMinSecException exception)
                                {
                                // This should never happen!
                                listErrors.add(ERROR_UNEXPECTED);
                                boolValid = false;
                                }

                            if (boolValid)
                                {
                                final String [] strUpdate =
                                    {
                                    REGISTRY.getString(getResourceKey() + KEY_UPDATE_LOCALE),
                                    REGISTRY.getString(getResourceKey() + KEY_CONTINUE)
                                    };

//                                pluginFramework.setSelectQueryList(replaceNull(strQueryList));
                                // ToDo replace Query in QueryData hashtable

//                                pluginFramework.setSelectQueryByName(replaceNull(strQueryByName));
                                // ToDo replace Query in QueryData hashtable

                                pluginFramework.setDescription(
                                        EditorUtilities.replaceNull(strDescription));

                                pluginFramework.setModifiedDate(Chronos.getCalendarDateNow());
                                pluginFramework.setModifiedTime(Chronos.getCalendarTimeNow());

                                // Update the display
                                // Do not update the checkboxes or the combo boxes, because they don't need to be redrawn,
                                // and the change would produce an event we don't want
//                                textExportsFolder.setText(pluginFramework.getExportsFolder());
                                textSplashScreenFilename.setText(pluginFramework.getSplashScreenFilename());
                                textIconFilename.setText(pluginFramework.getIconFilename());
                                textHelpFilename.setText(pluginFramework.getHelpFilename());
                                textAboutFilename.setText(pluginFramework.getAboutFilename());
                                textMapFilename.setText(pluginFramework.getMapFilename());

                                final DegMinSecInterface dmsLongitude = pluginFramework.getLongitude();
                                dmsLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
                                dmsLongitude.setDisplayFormat(DegMinSecFormat.SIGN);
                                textLongitude.setText(dmsLongitude.toString());

                                final DegMinSecInterface dmsLatitude = pluginFramework.getLatitude();
                                dmsLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
                                dmsLatitude.setDisplayFormat(DegMinSecFormat.SIGN);
                                textLatitude.setText(dmsLatitude.toString());

                                pluginFramework.getMapTopLeftLongitude().apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
                                pluginFramework.getMapTopLeftLongitude().setDisplayFormat(DegMinSecFormat.SIGN);
                                textMapTopLeftLongitude.setText(pluginFramework.getMapTopLeftLongitude().toString());

                                pluginFramework.getMapTopLeftLatitude().apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
                                pluginFramework.getMapTopLeftLatitude().setDisplayFormat(DegMinSecFormat.SIGN);
                                textMapTopLeftLatitude.setText(pluginFramework.getMapTopLeftLatitude().toString());

                                pluginFramework.getMapBottomRightLongitude().apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
                                pluginFramework.getMapBottomRightLongitude().setDisplayFormat(DegMinSecFormat.SIGN);
                                textMapBottomRightLongitude.setText(pluginFramework.getMapBottomRightLongitude().toString());

                                pluginFramework.getMapBottomRightLatitude().apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
                                pluginFramework.getMapBottomRightLatitude().setDisplayFormat(DegMinSecFormat.SIGN);
                                textMapBottomRightLatitude.setText(pluginFramework.getMapBottomRightLatitude().toString());

//                                textQueryList.setText(pluginFramework.getSelectQueryList());
//                                textQueryByName.setText(pluginFramework.getSelectQueryByName());
                                textDescription.setText(pluginFramework.getDescription());

                                textDateModified.setText(ChronosHelper.toDateString(pluginFramework.getModifiedDate()));
                                textTimeModified.setText(ChronosHelper.toTimeString(pluginFramework.getModifiedTime()));

                                // Has the Locale or TimeZone been changed?
                                // Everything must be Ok before we change the Framework!
                                if ((!pluginFramework.getLanguageISOCode().equals(strSavedLanguageISOCode))
                                    || (!pluginFramework.getCountryISOCode().equals(strSavedCountryISOCode))
                                    || (!pluginFramework.getTimeZoneCode().equals(strSavedTimeZoneCode)))
                                    {
                                    intChoice = JOptionPane.showOptionDialog(null,
                                                                             strUpdate,
                                                                             pluginFramework.getName() + SPACE + REGISTRY.getString(getResourceKey() + KEY_EDITOR_TITLE),
                                                                             JOptionPane.YES_NO_OPTION,
                                                                             JOptionPane.QUESTION_MESSAGE,
                                                                             null,
                                                                             null,
                                                                             null);

                                    // Does the user want to update the Locale and TimeZone?
                                    if (intChoice == JOptionPane.OK_OPTION)
                                        {
                                        pluginFramework.notifyFrameworkChangedEvent(pluginFramework);
                                        }
                                    }

                                // The edit was completed successfully!
                                saveFramework(pluginFramework);

                                // Prevent further user interaction
                                buttonCommit.setEnabled(false);
                                buttonRevert.setEnabled(false);

                                // Log the changes
                                strLogText = METADATA_FRAMEWORK_EDIT + DELIMITER;
                                strLogText = strLogText + METADATA_NAME + pluginFramework.getPathname() + TERMINATOR + DELIMITER;
                                strLogText = strLogText + METADATA_ACTIVE + pluginFramework.isActive() + TERMINATOR + DELIMITER;
                                strLogText = strLogText + METADATA_LOADATSTART + pluginFramework.isLoadAtStart() + TERMINATOR + DELIMITER;
                                strLogText = strLogText + METADATA_LANGUAGE + pluginFramework.getLanguageISOCode() + TERMINATOR + DELIMITER;
                                strLogText = strLogText + METADATA_COUNTRY + pluginFramework.getCountryISOCode() + TERMINATOR + DELIMITER;
                                strLogText = strLogText + METADATA_TIMEZONE + pluginFramework.getTimeZoneCode() + TERMINATOR;

                                LOGGER.logAtomEvent(pluginFramework,
                                                    pluginFramework.getRootTask(),
                                                    getClass().getName(),
                                                    strLogText,
                                                    EventStatus.INFO);

                                // The database daemon can write Framework data when it next runs
                                pluginFramework.setUpdateAllowed(true);
                                pluginFramework.setUpdated(true);
                                }
                            else
                                {
                                // Another error was found, so we cannot saveEventLog to the changes
                                JOptionPane.showMessageDialog(null,
                                                              listErrors.toArray(),
                                                              pluginFramework.getName() + SPACE + REGISTRY.getString(getResourceKey() + KEY_EDITOR_TITLE),
                                                              JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        else
                            {
                            // An error was found, so we cannot saveEventLog to the changes
                            JOptionPane.showMessageDialog(null,
                                                          listErrors.toArray(),
                                                          pluginFramework.getName() + SPACE + REGISTRY.getString(getResourceKey() + KEY_EDITOR_TITLE),
                                                          JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            };

        buttonCommit.addActionListener(commitListener);

        //--------------------------------------------------------------------------------------------------------------------------------------------
        // The Revert button and its listener

        buttonRevert = createButton(getTextColour(),
                                    getLabelFont(),
                                    KEY_LABEL_REVERT,
                                    KEY_TOOLTIP_REVERT,
                                    "buttonRevert",
                                    false);

        final ActionListener revertListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // Undo any edits
                // These changes will enable the buttons!

//                pluginFramework.setName(strSavedName);
//                textName.setText(pluginFramework.getName());

                pluginFramework.setActive(boolSavedActive);
                checkActive.setSelected(pluginFramework.isActive());

                pluginFramework.setEditable(boolSavedEditable);
                checkEditable.setSelected(pluginFramework.isEditable());

                pluginFramework.setLoadAtStart(boolSavedLoadAtStart);
                checkLoadAtStart.setSelected(pluginFramework.isLoadAtStart());

                pluginFramework.setLanguageISOCode(strSavedLanguageISOCode);
                comboLanguage.setSelectedItem(REGISTRY.getLanguage(PREFIX_LANGUAGE
                                                                      + KEY_DELIMITER
                                                                      + pluginFramework.getLanguageISOCode()));

                pluginFramework.setCountryISOCode(strSavedCountryISOCode);
                comboCountry.setSelectedItem(REGISTRY.getCountry(PREFIX_COUNTRY
                                                                    + KEY_DELIMITER
                                                                    + pluginFramework.getCountryISOCode()));

                pluginFramework.setTimeZoneCode(strSavedTimeZoneCode);
                comboTimeZone.setSelectedItem(pluginFramework.getTimeZoneCode());

                dmsSavedLongitude.apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
                dmsSavedLongitude.setDisplayFormat(DegMinSecFormat.SIGN);
                pluginFramework.setLongitude(dmsSavedLongitude);
                textLongitude.setText(dmsSavedLongitude.toString());

                dmsSavedLatitude.apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
                dmsSavedLatitude.setDisplayFormat(DegMinSecFormat.SIGN);
                pluginFramework.setLatitude(dmsSavedLatitude);
                textLatitude.setText(dmsSavedLatitude.toString());

//                pluginFramework.setExportsFolder(strSavedExportsFolder);
//                textExportsFolder.setText(pluginFramework.getExportsFolder());

                pluginFramework.setSplashScreenFilename(strSavedSplashScreenFilename);
                textSplashScreenFilename.setText(pluginFramework.getSplashScreenFilename());

                pluginFramework.setIconFilename(strSavedIconFilename);
                textIconFilename.setText(pluginFramework.getIconFilename());

                pluginFramework.setHelpFilename(strSavedHelpFilename);
                textHelpFilename.setText(pluginFramework.getHelpFilename());

                pluginFramework.setAboutFilename(strSavedAboutFilename);
                textAboutFilename.setText(pluginFramework.getAboutFilename());

                pluginFramework.setMapFilename(strSavedMapFilename);
                textMapFilename.setText(pluginFramework.getMapFilename());

                pluginFramework.setMapTopLeftLongitude(dmsSavedTopLeftLongitude);
                pluginFramework.getMapTopLeftLongitude().apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
                pluginFramework.getMapTopLeftLongitude().setDisplayFormat(DegMinSecFormat.SIGN);
                textMapTopLeftLongitude.setText(pluginFramework.getMapTopLeftLongitude().toString());

                pluginFramework.setMapTopLeftLatitude(dmsSavedTopLeftLatitude);
                pluginFramework.getMapTopLeftLatitude().apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
                pluginFramework.getMapTopLeftLatitude().setDisplayFormat(DegMinSecFormat.SIGN);
                textMapTopLeftLatitude.setText(pluginFramework.getMapTopLeftLatitude().toString());

                pluginFramework.setMapBottomRightLongitude(dmsSavedBottomRightLongitude);
                pluginFramework.getMapBottomRightLongitude().apply360DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LONGITUDE);
                pluginFramework.getMapBottomRightLongitude().setDisplayFormat(DegMinSecFormat.SIGN);
                textMapBottomRightLongitude.setText(pluginFramework.getMapBottomRightLongitude().toString());

                pluginFramework.setMapBottomRightLatitude(dmsSavedBottomRightLatitude);
                pluginFramework.getMapBottomRightLatitude().apply90DegreeSecondsPattern(DecimalFormatPattern.SECONDS_LATITUDE);
                pluginFramework.getMapBottomRightLatitude().setDisplayFormat(DegMinSecFormat.SIGN);
                textMapBottomRightLatitude.setText(pluginFramework.getMapBottomRightLatitude().toString());

//                pluginFramework.setSelectQueryList(strSavedQueryList);
//                textQueryList.setText(pluginFramework.getSelectQueryList());

//                pluginFramework.setSelectQueryByName(strSavedQueryByName);
//                textQueryByName.setText(pluginFramework.getSelectQueryByName());

                pluginFramework.setDescription(strSavedDescription);
                textDescription.setText(pluginFramework.getDescription());

                pluginFramework.setModifiedDate(dateSavedDateModified);
                textDateModified.setText(ChronosHelper.toDateString(pluginFramework.getModifiedDate()));

                pluginFramework.setModifiedTime(timeSavedTimeModified);
                textTimeModified.setText(ChronosHelper.toTimeString(pluginFramework.getModifiedTime()));

                pluginFramework.setUpdated(false);

                // Prevent further user interaction until the next edit
                buttonCommit.setEnabled(false);
                buttonRevert.setEnabled(false);

                // The database daemon can write data again (but not this Framework)
                pluginFramework.setUpdateAllowed(true);
                }
            };

        buttonRevert.addActionListener(revertListener);

        //------------------------------------------------------------------------------------------
        // The Script button and its listener

        buttonScript = createButton(getTextColour(),
                                    getLabelFont(),
                                    "Label.Script",
                                    "Tooltip.Script",
                                    "buttonScript",
                                    true);

        final ActionListener scriptListener = new ActionListener()
            {
            public void actionPerformed(final ActionEvent event)
                {
                // ToDo redo!!!
//                if (pluginFramework.writeScripts(pluginFramework,
//                                               pluginFramework.getExportsFolder()))
//                    {
//                    String [] strMessage =
//                        {
//                        MSG_SCRIPT + SPACE,
//                        pluginFramework.getExportsFolder()
//                            + System.getProperty("file.separator")
//                            + pluginFramework.getName()
//                            + FILETYPE_SQL
//                        };
//
//                    JOptionPane.showMessageDialog(null,
//                                                  strMessage,
//                                                  TITLE_SCRIPT_DIALOG,
//                                                  JOptionPane.INFORMATION_MESSAGE);
//                    }
//                else
//                    {
//                    String [] strMessage =
//                        {
//                        ERROR_SCRIPT
//                        };
//
//                    JOptionPane.showMessageDialog(null,
//                                                  strMessage,
//                                                  TITLE_SCRIPT_DIALOG,
//                                                  JOptionPane.ERROR_MESSAGE);
//                    }
                }
            };

        buttonScript.addActionListener(scriptListener);

        //------------------------------------------------------------------------------------------
        // The ExportsFolder button and its listener

//        buttonBrowserExports = createBrowserButton(getTextColour(),
//                                                   getLabelFont(),
//                                                   KEY_LABEL_EXPORTSBROWSER,
//                                                   KEY_TOOLTIP_EXPORTSBROWSER,
//                                                   "buttonBrowserExports",
//                                                   true);
//
//        buttonBrowserExports.addActionListener(createBrowserListener(System.getProperty(ResourceKeys.KEY_SYSTEM_USER_DIR) + "/exports",
//                                                                     TITLE_EXPORTS_BROWSER,
//                                                                     textExportsFolder));

        //------------------------------------------------------------------------------------------
        // The SplashScreenBrowser button and its listener

        buttonBrowserSplashScreen = createBrowserButton(getTextColour(),
                                                        getLabelFont(),
                                                        KEY_LABEL_SPLASHSCREENBROWSER,
                                                        KEY_TOOLTIP_SPLASHSCREENBROWSER,
                                                        "buttonBrowserIcon",
                                                        true);

        buttonBrowserSplashScreen.addActionListener(EditorUtilities.createBrowserListener(
                RegistryModelUtilities.getFilesystemFolder(
                        InstallationFolder.ROOT_COMMON.getName() + InstallationFolder.IMAGES.getName(),
                        false),
                TITLE_SPLASHSCREEN_BROWSER,
                textSplashScreenFilename));

        //------------------------------------------------------------------------------------------
        // The IconBrowser button and its listener

        buttonBrowserIcon = createBrowserButton(getTextColour(),
                                                getLabelFont(),
                                                KEY_LABEL_ICONBROWSER,
                                                KEY_TOOLTIP_ICONBROWSER,
                                                "buttonBrowserIcon",
                                                true);

        buttonBrowserIcon.addActionListener(EditorUtilities.createBrowserListener(
                RegistryModelUtilities.getFilesystemFolder(
                        InstallationFolder.ROOT_COMMON.getName() + InstallationFolder.IMAGES.getName(),
                        false),
                TITLE_ICON_BROWSER,
                textIconFilename));

        //------------------------------------------------------------------------------------------
        // The Help Browser button and its listener

        buttonBrowserHelp = createBrowserButton(getTextColour(),
                                                getLabelFont(),
                                                KEY_LABEL_HELPBROWSER,
                                                KEY_TOOLTIP_HELPBROWSER,
                                                "buttonBrowserHelp",
                                                true);

        buttonBrowserHelp.addActionListener(EditorUtilities.createBrowserListener(
                RegistryModelUtilities.getFilesystemFolder(InstallationFolder.HELP.getName(),
                                                           false),
                TITLE_HELP_BROWSER,
                textHelpFilename));

        //------------------------------------------------------------------------------------------
        // The About Browser button and its listener

        buttonBrowserAbout = createBrowserButton(getTextColour(),
                                                 getLabelFont(),
                                                 KEY_LABEL_ABOUTBROWSER,
                                                 KEY_TOOLTIP_ABOUTBROWSER,
                                                 "buttonBrowserAbout",
                                                 true);

        buttonBrowserAbout.addActionListener(EditorUtilities.createBrowserListener(
                RegistryModelUtilities.getFilesystemFolder(InstallationFolder.HELP.getName(),
                                                           false),
                TITLE_ABOUT_BROWSER,
                textAboutFilename));

        //------------------------------------------------------------------------------------------
        // The Map Browser button and its listener

        buttonBrowserMap = createBrowserButton(getTextColour(),
                                               getLabelFont(),
                                               KEY_LABEL_MAPBROWSER,
                                               KEY_TOOLTIP_MAPBROWSER,
                                               "buttonBrowserMap",
                                               true);

        buttonBrowserMap.addActionListener(EditorUtilities.createBrowserListener(
                RegistryModelUtilities.getFilesystemFolder(
                        InstallationFolder.ROOT_COMMON.getName() + InstallationFolder.IMAGES.getName(),
                        false),
                TITLE_MAP_BROWSER,
                textMapFilename));

        //------------------------------------------------------------------------------------------
        // Put all the panels together in the right order

        intLabelHeight = (int)(DIM_ROW_SPACER.getHeight() * ROW_COUNT)
                          + (EditorUtilities.HEIGHT_ROW * ROW_COUNT)
                          + HEIGHT_QUERYEDIT        // Changed from Description
                          + (HEIGHT_QUERYEDIT << 1);

        panelEditor = EditorUtilities.createEditorPanel(getCanvasColour());
        panelLabel = EditorUtilities.createLabelPanel(getCanvasColour(), intLabelHeight);
        panelData = EditorUtilities.createDataPanel(getCanvasColour());

        vecButtons = new Vector<JButton>(3);
        //vecButtons.add(buttonScript);
        vecButtons.add(buttonRevert);
        vecButtons.add(buttonCommit);
        panelButtons = EditorUtilities.createButtonPanel(getCanvasColour(), vecButtons);

//        panelBrowserExports = createBrowserPanel(getCanvasColour(),
//                                                 textExportsFolder,
//                                                 buttonBrowserExports);

        panelBrowserSplashScreen = EditorUtilities.createBrowserPanel(getCanvasColour(),
                                                                      textSplashScreenFilename,
                                                                      buttonBrowserSplashScreen);

        panelBrowserIcon = EditorUtilities.createBrowserPanel(getCanvasColour(),
                                                              textIconFilename,
                                                              buttonBrowserIcon);

        panelBrowserHelp = EditorUtilities.createBrowserPanel(getCanvasColour(),
                                                              textHelpFilename,
                                                              buttonBrowserHelp);

        panelBrowserAbout = EditorUtilities.createBrowserPanel(getCanvasColour(),
                                                               textAboutFilename,
                                                               buttonBrowserAbout);

        panelBrowserMap = EditorUtilities.createBrowserPanel(getCanvasColour(),
                                                             textMapFilename,
                                                             buttonBrowserMap);

        panelLabel.add(labelName);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelActive);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelEditable);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelLoadAtStart);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelLabel.add(labelLanguage);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelCountry);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelTimeZone);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelLabel.add(labelLongitude);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelLatitude);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelLabel.add(labelExportsFolder);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelSplashScreenFilename);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelIconFilename);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelHelpFilename);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelAboutFilename);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelMapFilename);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelLabel.add(labelMapTopLeftLongitude);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelMapTopLeftLatitude);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelMapBottomRightLongitude);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelMapBottomRightLatitude);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelLabel.add(labelQueryList);
        panelLabel.add(Box.createRigidArea(DIM_QUERYEDIT_SPACER));
        panelLabel.add(labelQueryByName);
        panelLabel.add(Box.createRigidArea(DIM_QUERYEDIT_SPACER));
        panelLabel.add(labelDescription);
        panelLabel.add(Box.createRigidArea(DIM_QUERYEDIT_SPACER));

        panelLabel.add(labelDateCreated);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelTimeCreated);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelDateModified);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelLabel.add(labelTimeModified);
        panelLabel.add(Box.createRigidArea(DIM_ROW_SPACER));

        // The Data Panel
        panelData.add(textName);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkActive);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkEditable);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(checkLoadAtStart);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelData.add(comboLanguage);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(comboCountry);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(comboTimeZone);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelData.add(textLongitude);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textLatitude);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

//        panelData.add(panelBrowserExports);
//        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelBrowserSplashScreen);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelBrowserIcon);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelBrowserHelp);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelBrowserAbout);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(panelBrowserMap);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelData.add(textMapTopLeftLongitude);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textMapTopLeftLatitude);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textMapBottomRightLongitude);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textMapBottomRightLatitude);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        // Remove the QueryList sizes and border...
//        textQueryList.setMinimumSize(null);
//        textQueryList.setPreferredSize(null);
//        textQueryList.setMaximumSize(null);
//        textQueryList.setBorder(null);

        // Put the QueryList in a JScrollPane, sized appropriately
//        scrollQueryList = new JScrollPane(textQueryList);
//        scrollQueryList.setMinimumSize(new Dimension(0, HEIGHT_QUERYEDIT));
//        scrollQueryList.setMaximumSize(new Dimension(MAX_UNIVERSE, HEIGHT_QUERYEDIT));
//        scrollQueryList.setPreferredSize(new Dimension(DIM_MAGIC, HEIGHT_QUERYEDIT));

//        panelData.add(scrollQueryList);
//        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        // Remove the QueryByName sizes and border...
//        textQueryByName.setMinimumSize(null);
//        textQueryByName.setPreferredSize(null);
//        textQueryByName.setMaximumSize(null);
//        textQueryByName.setBorder(null);

        // Put the QueryByName in a JScrollPane, sized appropriately
//        scrollQueryByName = new JScrollPane(textQueryByName);
//        scrollQueryByName.setMinimumSize(new Dimension(0, HEIGHT_QUERYEDIT));
//        scrollQueryByName.setMaximumSize(new Dimension(MAX_UNIVERSE, HEIGHT_QUERYEDIT));
//        scrollQueryByName.setPreferredSize(new Dimension(DIM_MAGIC, HEIGHT_QUERYEDIT));

//        panelData.add(scrollQueryByName);
//        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        // Remove the Description sizes and border...
        textDescription.setMinimumSize(null);
        textDescription.setPreferredSize(null);
        textDescription.setMaximumSize(null);
        textDescription.setBorder(null);

        // Put the Description in a JScrollPane, sized appropriately
        scrollDescription = new JScrollPane(textDescription);
        scrollDescription.setMinimumSize(new Dimension(0, HEIGHT_DESCRIPTION_SHORT));
        scrollDescription.setMaximumSize(new Dimension(MAX_UNIVERSE, HEIGHT_DESCRIPTION_SHORT));
        scrollDescription.setPreferredSize(new Dimension(EditorUtilities.DIM_MAGIC, HEIGHT_DESCRIPTION_SHORT));

        panelData.add(scrollDescription);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));

        panelData.add(textDateCreated);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textTimeCreated);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textDateModified);
        panelData.add(Box.createRigidArea(DIM_ROW_SPACER));
        panelData.add(textTimeModified);

        installPanels(getCanvasColour(),
                      panelEditor,
                      panelLabel,
                      panelData,
                      panelButtons);

        return (true);
        }


    /***********************************************************************************************
     * Save the Framework for later Revert.
     *
     * @param plugin
     */

    private void saveFramework(final FrameworkPlugin plugin)
        {
        System.out.println("saving framework");
        boolSavedActive = plugin.isActive();
        boolSavedEditable = plugin.isEditable();
        boolSavedLoadAtStart = plugin.isLoadAtStart();
        strSavedLanguageISOCode = EditorUtilities.replaceNull(plugin.getLanguageISOCode());
        strSavedCountryISOCode = EditorUtilities.replaceNull(plugin.getCountryISOCode());
        strSavedTimeZoneCode = EditorUtilities.replaceNull(plugin.getTimeZoneCode());
        dmsSavedLongitude = plugin.getLongitude();
        dmsSavedLatitude = plugin.getLatitude();
//        strSavedExportsFolder = replaceNull(plugin.getExportsFolder());
        strSavedSplashScreenFilename = EditorUtilities.replaceNull(plugin.getSplashScreenFilename());
        strSavedIconFilename = EditorUtilities.replaceNull(plugin.getIconFilename());
        strSavedHelpFilename = EditorUtilities.replaceNull(plugin.getHelpFilename());
        strSavedAboutFilename = EditorUtilities.replaceNull(plugin.getAboutFilename());
        strSavedMapFilename = EditorUtilities.replaceNull(plugin.getMapFilename());
        dmsSavedTopLeftLongitude = plugin.getMapTopLeftLongitude();
        dmsSavedTopLeftLatitude = plugin.getMapTopLeftLatitude();
        dmsSavedBottomRightLongitude = plugin.getMapBottomRightLongitude();
        dmsSavedBottomRightLatitude = plugin.getMapBottomRightLatitude();
//        strSavedQueryList = replaceNull(plugin.getSelectQueryList());
//        strSavedQueryByName = replaceNull(plugin.getSelectQueryByName());
        strSavedDescription = EditorUtilities.replaceNull(plugin.getDescription());
        dateSavedDateModified = EditorUtilities.replaceNull(plugin.getModifiedDate());
        timeSavedTimeModified = EditorUtilities.replaceNull(plugin.getModifiedTime());
        }


    /***********************************************************************************************
     * A utility to indicate when the data has changed as a result of an edit.
     */

    private void dataChanged()
        {
        pluginFramework.setUpdateAllowed(false);
        buttonCommit.setEnabled(true);
        buttonRevert.setEnabled(true);
        }
    }


//--------------------------------------------------------------------------------------------------
// End of File
