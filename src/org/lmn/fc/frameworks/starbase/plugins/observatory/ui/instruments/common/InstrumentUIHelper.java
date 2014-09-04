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

import info.clearthought.layout.TableLayout;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.common.datatranslators.DataAnalyser;
import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.frameworks.starbase.plugins.observatory.events.InstrumentStateChangedEvent;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.data.DataUpdateType;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.misc.DigitalPanelMeterUIComponent;
import org.lmn.fc.frameworks.starbase.ui.userinterface.HeaderUIComponent;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;
import org.lmn.fc.ui.widgets.IndicatorInterface;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;


/***************************************************************************************************
 * InstrumentUIHelper.
 */

public final class InstrumentUIHelper implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkSingletons,
                                                 ResourceKeys
    {
    // String Resources
    private static final String TOOLTIP_ADDRESS_STARIBUS = "The Staribus Address of the Instrument";
    private static final String TOOLTIP_ADDRESS_IP = "The IP Address of the Instrument";
    private static final String TOOLTIP_ADDRESS_VIRTUAL = "Virtual Instrument";
    private static final String TOOLTIP_ADDRESS_ERROR = "Address Error";
    private static final String TOOLTIP_ADDRESS_NO_CONTROLLER = "No Controller";
    private static final String TOOLTIP_PORT = "The Instrument Port Identifier";

    public static final int CONFIG_INDEX_ICON = 0;
    public static final int CONFIG_INDEX_UPDATED = 1;
    public static final int CONFIG_INDEX_KEY = 2;
    public static final int CONFIG_INDEX_VALUE = 3;

    private static final int WIDTH_COMPONENT_SEPARATOR = 5;
    private static final int HEIGHT_COMPONENT_SEPARATOR = 5;
    public static final float SIZE_FONT_HEADER = 18.0f;
    private static final int LOW_CONTRAST = 20;


    /***********************************************************************************************
     * Get an ImageIcon for a ControlPanel (SelectorPanel), using a URL.
     *
     * @param decorator
     * @param filename
     *
     * @return ImageIcon
     */

    public static ImageIcon getSelectorPanelIcon(final InstrumentUIComponentDecoratorInterface decorator,
                                                 final String filename)
        {
        final String SOURCE = "InstrumentUIHelper.getSelectorPanelIcon() ";
        final ImageIcon icon;

        if ((decorator != null)
            && (filename != null)
            && (!EMPTY_STRING.equals(filename)))
            {
            URL url;

            // See if the default icon filename has been overridden in the XML
            if ((decorator.getInstrument().getSelectorPanelIconFilename() != null)
                && (!EMPTY_STRING.equals(decorator.getInstrument().getSelectorPanelIconFilename())))
                {
                try
                    {
                    // Find a resource of the specified name from the search path used to load classes
                    // This method locates the resource through the system class loader
                    // The usual path is "/org/lmn/fc/frameworks/starbase/plugins/observatory/imports"
                    url = decorator.getClass().getResource(decorator.getInstrument().getSelectorPanelIconFilename());
                    }

                catch (final Exception exception)
                    {
                    exception.printStackTrace();
                    url = null;
                    }
                }
            else
                {
                // Use the default in the XXxxControlPanel.java file
                url = decorator.getClass().getResource(InstrumentHelper.getInstrumentInstallationFolder(decorator, InstallationFolder.IMAGES)
                                                        + filename);
                }

            if (url != null)
                {
//                System.out.println(SOURCE + decorator.getInstrument().getIdentifier() + " SELECTOR PANEL ICON URL=" + url);
                icon = new ImageIcon(url);
                }
            else
                {
                // If we can't find the image, just return an empty Icon
//                System.out.println(SOURCE + decorator.getInstrument().getIdentifier() + " SELECTOR PANEL ICON NOT FOUND! [filename=" + filename + "]");
                icon = new ImageIcon();
                }
            }
        else
            {
            // If we can't find the image, just return an empty Icon
//            System.out.println(SOURCE + "SELECTOR PANEL ICON NOT FOUND! [filename=" + filename + "]");
            icon = new ImageIcon();
            }

        return (icon);
        }


    /***********************************************************************************************
     * Get an ImageIcon for an InstrumentPanel (Header), using a URL.
     *
     * @param decorator
     * @param filename
     *
     * @return ImageIcon
     */

    public static ImageIcon getInstrumentPanelIcon(final InstrumentUIComponentDecoratorInterface decorator,
                                                   final String filename)
        {
        final String SOURCE = "InstrumentUIHelper.getInstrumentPanelIcon() ";
        final ImageIcon icon;

        if ((decorator != null)
            && (filename != null)
            && (!EMPTY_STRING.equals(filename)))
            {
            URL url;

            // See if the default icon filename has been overridden in the XML
            if ((decorator.getInstrument().getInstrumentPanelIconFilename() != null)
                && (!EMPTY_STRING.equals(decorator.getInstrument().getInstrumentPanelIconFilename())))
                {
                try
                    {
                    // Find a resource of the specified name from the search path used to load classes
                    // This method locates the resource through the system class loader
                    // The usual path is "/org/lmn/fc/frameworks/starbase/plugins/observatory/imports"
                    url = decorator.getClass().getResource(decorator.getInstrument().getInstrumentPanelIconFilename());
                    }

                //catch (final MalformedURLException exception)
                catch (final Exception exception)
                    {
                    exception.printStackTrace();
                    url = null;
                    }
                }
            else
                {
                // Use the default in the XxxInstrumentPanel.java file
                url = decorator.getClass().getResource(InstrumentHelper.getInstrumentInstallationFolder(decorator, InstallationFolder.IMAGES)
                                                        + filename);
                }

            if (url != null)
                {
//                System.out.println(SOURCE + decorator.getInstrument().getIdentifier() + " INSTRUMENT PANEL ICON URL=" + url);
                icon = new ImageIcon(url);
                }
            else
                {
//                System.out.println(SOURCE + decorator.getInstrument().getIdentifier() + " INSTRUMENT PANEL ICON NOT FOUND! [filename=" + filename + "]");
                icon = new ImageIcon();
                }
            }
        else
            {
            // If we can't find the image, just return an empty Icon
//            System.out.println(SOURCE + "INSTRUMENT PANEL ICON NOT FOUND! [filename=" + filename + "]");
            icon = new ImageIcon();
            }

        return (icon);
        }


    /***********************************************************************************************
     * Get an ImageIcon for the Images folder below a Class, using a URL.
     *
     * @param aClass
     * @param filename
     *
     * @return ImageIcon
     */

    public static ImageIcon getIconForClass(final Class aClass,
                                            final String filename)
        {
        final String SOURCE = "InstrumentUIHelper.getIconForClass() ";
        final ImageIcon icon;

        if ((aClass != null)
            && (filename != null)
            && (!EMPTY_STRING.equals(filename)))
            {
            final URL url;

            url = aClass.getResource(InstrumentHelper.getInstallationFolderBelowClass(aClass,
                                                                                      InstallationFolder.IMAGES) + filename);

//            System.out.println("INSTRUMENT ICON PATH=" + getInstallationFolderBelowClass(aClass,
//                                                                               InstallationFolder.IMAGES) + filename);
//            System.out.println("INSTRUMENT URL=" + url);

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
            }

        return (icon);
        }


    /***********************************************************************************************
     * Show a tooltip (for a Tab) which indicates that the data are truncated.
     *
     * @param obsinstrument
     *
     * @return String
     */

    public static String showTruncatedTooltip(final ObservatoryInstrumentInterface obsinstrument)
        {
//        return (InstrumentUIComponentDecoratorInterface.TOOLTIP_AUTO_TRUNCATES
//                + REGISTRY.getIntegerProperty(obsinstrument.getHostAtom().getResourceKey() + ResourceKeys.KEY_DISPLAY_DATA_MAX)
//                   + InstrumentUIComponentDecoratorInterface.TOOLTIP_ENTRIES);
        return (EMPTY_STRING);
        }


    /***********************************************************************************************
     * Show a Chart Tab tooltip which indicates that the data are preserved, truncated or decimated.
     *
     * @param obsinstrument      *
     * @param updatetype
     *
     * @return String
     */

    public static String showChartTabTooltip(final ObservatoryInstrumentInterface obsinstrument,
                                             final DataUpdateType updatetype)
        {
        final String SOURCE = "InstrumentUIHelper.showChartTabTooltip() ";
        final String strMessage;

        switch (updatetype)
            {
            case PRESERVE:
                {
                strMessage = InstrumentUIComponentDecoratorInterface.TOOLTIP_PRESERVED;
                break;
                }

            case TRUNCATE:
                {
                strMessage = InstrumentUIComponentDecoratorInterface.TOOLTIP_AUTO_TRUNCATES
                                + REGISTRY.getIntegerProperty(obsinstrument.getHostAtom().getResourceKey() + ResourceKeys.KEY_DISPLAY_DATA_MAX)
                                + InstrumentUIComponentDecoratorInterface.TOOLTIP_ENTRIES;
                break;
                }

            case DECIMATE:
                {
                strMessage = InstrumentUIComponentDecoratorInterface.TOOLTIP_AUTO_DECIMATES
                                + REGISTRY.getIntegerProperty(obsinstrument.getHostAtom().getResourceKey() + ResourceKeys.KEY_DISPLAY_DATA_MAX)
                                + InstrumentUIComponentDecoratorInterface.TOOLTIP_ENTRIES;
                break;
                }

            default:
                {
                strMessage = "Error: Invalid DataUpdateType";
                }
            }

        return (strMessage);
        }


    /***********************************************************************************************
     * Configure the Header for the InstrumentPanel specified by the Decorator.
     *
     * @param header
     * @param hostui
     * @param decorator
     * @param atom
     * @param instrumentxml
     * @param iconfilename
     * @param font
     * @param colour
     */

    public static void configureInstrumentPanelHeader(final HeaderUIComponent header,
                                                      final ObservatoryUIInterface hostui,
                                                      final InstrumentUIComponentDecoratorInterface decorator,
                                                      final AtomPlugin atom,
                                                      final Instrument instrumentxml,
                                                      final String iconfilename,
                                                      final FontInterface font,
                                                      final ColourInterface colour)
        {
        final String SOURCE = "InstrumentUIHelper.configureInstrumentPanelHeader() ";

        if ((header != null)
            && (hostui != null)
            && (decorator != null)
            && (atom != null)
            && (instrumentxml != null)
//            && (XmlBeansUtilities.isValidXml(instrumentxml))
            && (font != null)
            && (colour != null))
            {
            // r=234 g=225 b=14
            header.setComponentColour(UIComponentPlugin.COLOUR_RAG_TEXT);

            // font=dialog style=plain size=28
            header.setComponentFont(FontInterface.DEFAULT_FONT_BANNER);

            header.setHeaderText(instrumentxml.getName());
            header.setToolTipText(instrumentxml.getDescription());
            header.setHeaderIcon(getInstrumentPanelIcon(decorator, iconfilename));
            }
        else
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /***********************************************************************************************
     * Assemble a ControlPanel of one Icon and the decorator's List of ToolbarIndicators.
     * Arrange the Indicators to fit two columns of a standard ControlPanel.
     *
     * @param decorator
     * @param controlpanel
     * @param iconname
     */

    public static void assembleControlPanel(final InstrumentUIComponentDecoratorInterface decorator,
                                            final JComponent controlpanel,
                                            final String iconname)
        {
        final String SOURCE = "InstrumentUIHelper.assembleControlPanel() ";
        final JPanel panelIcon;
        final JPanel panelComponents;
        final List<IndicatorInterface> listToolbarIndicators;
        final ImageIcon icon;

        controlpanel.setLayout(new BoxLayoutFixed(controlpanel, BoxLayoutFixed.X_AXIS));
        controlpanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        controlpanel.removeAll();

        panelIcon = new JPanel(new BorderLayout());
        icon = getSelectorPanelIcon(decorator, iconname);
        panelIcon.add(new JLabel(icon), BorderLayout.CENTER);
        panelIcon.setMinimumSize(InstrumentSelector.DIM_PANEL_ICON);
        panelIcon.setMaximumSize(new Dimension(InstrumentSelector.DIM_PANEL_ICON.width, icon.getIconHeight()));
        panelIcon.setPreferredSize(InstrumentSelector.DIM_PANEL_ICON);
        panelIcon.setOpaque(false);

        panelComponents = new JPanel();
        panelComponents.setOpaque(false);

        // Find out how many Indicators are required
        // We leave it up to the User to select the correct ControlPanel background image!
        listToolbarIndicators = decorator.getControlPanelIndicators();

        switch (listToolbarIndicators.size())
            {
            case 0:
                {
                panelIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelIcon.setAlignmentY(Component.CENTER_ALIGNMENT);

                // No indicator required, so do nothing

                // Assemble the ControlPanel
                controlpanel.add(panelIcon);
                controlpanel.add(Box.createHorizontalGlue());
                break;
                }

            case 1:
                {
                final IndicatorInterface indicator;

                panelIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelIcon.setAlignmentY(Component.CENTER_ALIGNMENT);

                panelComponents.setLayout(new BoxLayoutFixed(panelComponents, BoxLayoutFixed.X_AXIS));
                panelComponents.setAlignmentY(Component.CENTER_ALIGNMENT);

                panelComponents.add(Box.createHorizontalGlue());
                indicator = listToolbarIndicators.get(0);

                if (indicator != null)
                    {
                    // We have an opportunity to widen the indicator a little to improve readability
//                    indicator.setIndicatorDimension((int)ControlPanelInterface.DIM_CONTROL_PANEL_INDICATOR_DOUBLE.getWidth()
//                                                        + ControlPanelInterface.WIDTH_INCREASE_SINGLE_INDICATOR,
//                                                    (int)ControlPanelInterface.DIM_CONTROL_PANEL_INDICATOR_DOUBLE.getHeight());
//                    indicator.initialiseUI();
                    panelComponents.add((Component)indicator);
                    }

                panelComponents.add(Box.createHorizontalGlue());

                // Assemble the ControlPanel
                controlpanel.add(panelIcon);
                controlpanel.add(Box.createHorizontalGlue());
                controlpanel.add(panelComponents);
                controlpanel.add(Box.createHorizontalGlue());
                break;
                }

            case 2:
                {
                panelIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelIcon.setAlignmentY(Component.CENTER_ALIGNMENT);

                panelComponents.setLayout(new BoxLayoutFixed(panelComponents, BoxLayoutFixed.X_AXIS));
                panelComponents.setAlignmentY(Component.CENTER_ALIGNMENT);
                panelComponents.add(Box.createHorizontalGlue());

                for (int intIndicatorIndex = 0;
                     intIndicatorIndex < listToolbarIndicators.size();
                     intIndicatorIndex++)
                    {
                    final IndicatorInterface indicator;

                    indicator = listToolbarIndicators.get(intIndicatorIndex);

                    if (indicator != null)
                        {
                        if (intIndicatorIndex > 0)
                            {
                            panelComponents.add(Box.createHorizontalStrut(WIDTH_COMPONENT_SEPARATOR));
                            }

                        panelComponents.add((Component)indicator);
                        }
                    }

                panelComponents.add(Box.createHorizontalGlue());

                // Assemble the ControlPanel
                controlpanel.add(panelIcon);
                controlpanel.add(Box.createHorizontalGlue());
                controlpanel.add(panelComponents);
                controlpanel.add(Box.createHorizontalGlue());
                break;
                }

            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                {
                // TableLayout row and column size definitions
                final double[][] size =
                    {
                        { // Columns
                        TableLayout.PREFERRED,
                        WIDTH_COMPONENT_SEPARATOR,
                        TableLayout.PREFERRED
                        },

                        { // Rows
                        TableLayout.PREFERRED,
                        HEIGHT_COMPONENT_SEPARATOR,
                        TableLayout.PREFERRED,
                        HEIGHT_COMPONENT_SEPARATOR,
                        TableLayout.PREFERRED,
                        HEIGHT_COMPONENT_SEPARATOR,
                        TableLayout.PREFERRED
                        }
                    };

                // TableLayout constraints for Indicators
                // The horizontal justification is specified before the vertical justification
                final String[] constraints =
                    { // Column, Row, HorizontalJustification, VerticalJustification
                     "0, 0, CENTER, CENTER",
                     "2, 0, CENTER, CENTER",
                     "0, 2, CENTER, CENTER",
                     "2, 2, CENTER, CENTER",
                     "0, 4, CENTER, CENTER",
                     "2, 4, CENTER, CENTER",
                     "0, 6, CENTER, CENTER",
                     "2, 6, CENTER, CENTER"
                    };

                panelIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelIcon.setAlignmentY(Component.CENTER_ALIGNMENT);

                panelComponents.setLayout(new TableLayout(size));
                panelComponents.setBorder(BorderFactory.createEmptyBorder(5, 7, 0, 0));
                panelComponents.setAlignmentY(Component.CENTER_ALIGNMENT);

                for (int intIndicatorIndex = 0;
                     intIndicatorIndex < listToolbarIndicators.size();
                     intIndicatorIndex++)
                    {
                    final IndicatorInterface indicator;

                    indicator = listToolbarIndicators.get(intIndicatorIndex);

                    if (indicator != null)
                        {
                        // The TableLayout will respect the Indicator PreferredSizes
                        panelComponents.add((Component)indicator, constraints[intIndicatorIndex]);
                        }
                    }

                // Assemble the ControlPanel
                controlpanel.add(panelIcon);
                controlpanel.add(Box.createHorizontalGlue());
                controlpanel.add(panelComponents);
                controlpanel.add(Box.createHorizontalGlue());
                break;
                }

            default:
                {
                final JLabel labelError;

                labelError = new JLabel("Error!");
                labelError.setForeground(Color.red);
                panelComponents.add(Box.createHorizontalGlue());
                panelComponents.add(labelError);
                panelComponents.add(Box.createHorizontalGlue());

                // Assemble the ControlPanel
                controlpanel.add(panelIcon);
                controlpanel.add(Box.createHorizontalGlue());
                controlpanel.add(panelComponents);
                controlpanel.add(Box.createHorizontalGlue());

                LOGGER.error(SOURCE + "Unable to render ControlPanel [indicator_count=" + listToolbarIndicators.size() + "]");
                }
            }
        }


    /***********************************************************************************************
     * Update Control Panel Indicators, if possible
     * Use the supplied IndicatorMetadataKeys or default Metadata as appropriate.
     *
     * @param event
     * @param controlpanel
     */

    public static void updateControlPanelIndicators(final InstrumentStateChangedEvent event,
                                                    final InstrumentUIComponentDecoratorInterface controlpanel)
        {
        final String SOURCE = "InstrumentUIHelper.updateControlPanelIndicators() ";

        // Be certain, but these should all be valid from the decorator
        // There may be no Indicators to update for this Instrument, which is valid
        // Fail silently with NULLs or zero Indicators
        if ((event != null)
            && (controlpanel != null)
            && (controlpanel.getHostInstrument() != null)
            && (controlpanel.getHostInstrument().getInstrument() != null)
            && (controlpanel.getControlPanelIndicators() != null)
            && (controlpanel.getControlPanelIndicators().size() > 0))
            {
            // If there are non-zero Indicators, we should try to update
            // Check that all Indicators have default Keys, Units and Tooltips
            if ((controlpanel.getControlPanelIndicators().size() <= ControlPanelInterface.MAX_CONTROL_PANEL_INDICATORS)
                && (controlpanel.getControlPanelIndicatorDefaultValueKeys() != null)
                && (controlpanel.getControlPanelIndicatorDefaultValueKeys().size() == controlpanel.getControlPanelIndicators().size())
                && (controlpanel.getControlPanelIndicatorDefaultUnits() != null)
                && (controlpanel.getControlPanelIndicatorDefaultUnits().size() == controlpanel.getControlPanelIndicators().size())
                && (controlpanel.getControlPanelIndicatorDefaultTooltipKeys() != null)
                && (controlpanel.getControlPanelIndicatorDefaultTooltipKeys().size() == controlpanel.getControlPanelIndicators().size()))
                {
                final List<Metadata> metadataCollection;

                // Gather together the full set of Metadata associated with this Observatory,
                // this Instrument and its DAO and translator to search for required items
                metadataCollection = controlpanel.getHostInstrument().getAggregateMetadata();

//                MetadataHelper.showMetadataList(metadataCollection,
//                                                controlpanel.getHostInstrument().getInstrument().getIdentifier() + " Metadata pool for Control Panel",
//                                                true);

                // Update each Indicator in turn
                // Remember to discard the Temperature Channel if one exists,
                // so the first Indicator is data Channel 0

                // The Timestamped sample format in the Vector is one of:
                //
                // 0           1            2           3
                // <Calendar> <Temperature> <Channel0> [<Channel1> <Channel2> ...]
                //
                // 0           1           2
                // <Calendar> <Channel0> [<Channel1> <Channel2> ...]

                // The Indexed sample format in the Vector is one of:
                //
                // 0          1             2           3
                // <X-Value>  <Temperature> <Channel0> [<Channel1> <Channel2> ...]
                //
                // 0          1           2
                // <X-Value>  <Channel0> [<Channel1> <Channel2> ...]

                for (int intIndicatorIndex = 0;
                     intIndicatorIndex < controlpanel.getControlPanelIndicators().size();
                     intIndicatorIndex++)
                    {
                    final IndicatorInterface indicator;
                    final int intChannelIndex;

                    //intChannelIndex = mapIndicatorToChannelIndex(controlpanel, intIndicatorIndex);
                    intChannelIndex = intIndicatorIndex;

                    // Handle each Indicator in turn
                    indicator = controlpanel.getControlPanelIndicators().get(intIndicatorIndex);

                    // Only update the Indicator value if the Instrument is doing something
                    if (InstrumentState.isDoingSomething(controlpanel.getHostInstrument()))
                        {
                        Metadata metadata;
                        final String strValue;
                        final String strUnits;
                        final String strTooltip;

                        metadata = null;

                        //--------------------------------------------------------------------------
                        // Show the channel colours if there is a DAO

                        if (controlpanel.getHostInstrument().getDAO() != null)
                            {
                            final ColourInterface colour;

                            // This will try to find Observation.Channel.Colour.n
                            // Returns r=0 g=0 b=0 if the colour is not specified correctly
                            colour = MetadataHelper.getChannelColour(metadataCollection,
                                                                     intChannelIndex,
                                                                     controlpanel.getHostInstrument().getDAO().hasTemperatureChannel());
                            if ((colour != null)
                                && ((colour.getColor().getRed() >= LOW_CONTRAST)
                                    || (colour.getColor().getGreen() >= LOW_CONTRAST)
                                    || (colour.getColor().getBlue() >= LOW_CONTRAST)))
                                {
                                indicator.setValueForeground(colour.getColor());
                                //System.out.println(SOURCE + "colour set ok [tempchannel=" + decorator.getHostInstrument().getDAO().hasTemperatureChannel() + "]");
                                }
                            else
                                {
                                // Always use the default if no Colour specified in the metadata,
                                // or if the Colour was Black (which would be invisible with the the default settings)
                                indicator.setValueForeground(IndicatorInterface.DEFAULT_VALUE_COLOR);
                                //System.out.println(SOURCE + "colour set at default since key not found");
                                }
                            }
                        else
                            {
                            // Always use the default colours if there is no DAO
                            // This shouldn't happen because there wouldn't be any channel data either
                            indicator.setForeground(IndicatorInterface.DEFAULT_VALUE_COLOR);
                            //System.out.println(SOURCE + "colour set at default because no dao");
                            }

                        //--------------------------------------------------------------------------
                        // See if there are any custom Metadata keys for the control panel indicators
                        // in the Instrument XML <IndicatorMetadataKey>
                        // These will take precedence over any Metadata already present
                        // otherwise the User can't influence what appears on the panel

                        if ((controlpanel.getHostInstrument().getInstrument().getIndicatorMetadataKeyList() != null)
                            && (controlpanel.getHostInstrument().getInstrument().getIndicatorMetadataKeyList().size() >= controlpanel.getControlPanelIndicators().size()))
                            {
                            System.out.println(SOURCE + "IndicatorMetadataKey searching --> [indicator=" + intIndicatorIndex
                                                   + "] [key="
                                                   + controlpanel.getHostInstrument().getInstrument().getIndicatorMetadataKeyList().get(intIndicatorIndex)
                                                   + "]");
                            metadata = MetadataHelper.getMetadataByKey(metadataCollection,
                                                                       controlpanel.getHostInstrument().getInstrument().getIndicatorMetadataKeyList().get(intIndicatorIndex));
                            }

                        // Did the supplied keys in the Instrument XML <IndicatorMetadataKey> map to a real Metadata item?
                        // Assume that {Value, Units, Tooltip} come from the one item (Colour not stored here)
                        if (metadata != null)
                            {
                            strValue = metadata.getValue();
                            strUnits = metadata.getUnits().toString();
                            strTooltip = metadata.getDescription();
                            System.out.println(SOURCE + "IndicatorMetadataKey --> found custom mapping of IndicatorMetadataKey metadata "
                                                   + "[indicator=" + intIndicatorIndex
                                                   + "] [key=" + controlpanel.getHostInstrument().getInstrument().getIndicatorMetadataKeyList().get(intIndicatorIndex)
                                                   + "] [value=" + strValue + "]");
                            }
                        else if (MetadataDictionary.KEY_INSTRUMENT_RESERVED_ADDRESS.getKey().equals(controlpanel.getControlPanelIndicatorDefaultValueKeys().get(intIndicatorIndex)))
                            {
                            // See if the defaults require special formatting for an Address or Port
                            strValue = InstrumentHelper.findInstrumentAddress(controlpanel.getHostInstrument());
                            strUnits = controlpanel.getControlPanelIndicatorDefaultUnits().get(intIndicatorIndex).toString();

                            if (controlpanel.getHostInstrument().getInstrument().getController() != null)
                                {
                                if (controlpanel.getHostInstrument().getInstrument().getController().isSetStaribusAddress())
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_STARIBUS;
                                    }
                                else if (controlpanel.getHostInstrument().getInstrument().getController().isSetIPAddress())
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_IP;
                                    }
                                else if (controlpanel.getHostInstrument().getInstrument().getController().isSetVirtualAddress())
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_VIRTUAL;
                                    }
                                else
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_ERROR;
                                    }
                                }
                            else
                                {
                                strTooltip = TOOLTIP_ADDRESS_NO_CONTROLLER;
                                }
                            }
                        else if (MetadataDictionary.KEY_INSTRUMENT_RESERVED_PORT.getKey().equals(controlpanel.getControlPanelIndicatorDefaultValueKeys().get(intIndicatorIndex)))
                            {
                            strValue = InstrumentHelper.findInstrumentPort(controlpanel.getHostInstrument());
                            strUnits = controlpanel.getControlPanelIndicatorDefaultUnits().get(intIndicatorIndex).toString();
                            strTooltip = TOOLTIP_PORT;
                            }
                        else
                            {
                            final String strItemValue;
                            final SchemaUnits.Enum unitsItem;
                            final String strItemDescription;

                            //----------------------------------------------------------------------
                            // The Control Panel indicator requires:
                            //
                            //  Value           Observation.Channel.Value.n
                            //  Units           Observation.Channel.Units.n
                            //  Tooltip         Observation.Channel.Description.n
                            //  Colour          Observation.Channel.Colour.n
                            //
                            // The Value item *should* have the correct Units and Tooltip
                            // but... in case it doesn't, use the above Metadata keys
                            //
                            //----------------------------------------------------------------------

                            // Try to find the Value, but remember that if there is no DAO there are no Channels
                            // Return a NO_DATA if the key cannot be found
                            strItemValue = MetadataHelper.getChannelValue(metadataCollection,
                                                                          intChannelIndex,
                                                                          controlpanel.getHostInstrument().getDAO().hasTemperatureChannel());

                            // Try to find the Units - this looks for Observation.Channel.Units.n
                            // This returns DIMENSIONLESS if not found, or if the Units were DIMENSIONLESS
                            unitsItem = MetadataHelper.getChannelUnits(metadataCollection,
                                                                       intChannelIndex,
                                                                       controlpanel.getHostInstrument().getDAO().hasTemperatureChannel());

                            // Try to find the Tooltip - this looks for Observation.Channel.Description.n
                            // This returns an NO_DATA if no Description is found
                            strItemDescription = MetadataHelper.getChannelDescription(metadataCollection,
                                                                                      intChannelIndex,
                                                                                      controlpanel.getHostInstrument().getDAO().hasTemperatureChannel());
                            // Did we find a valid Value
                            if ((strItemValue != null)
                                && (!NO_DATA.equals(strItemValue)))
                                {
                                strValue = strItemValue;
                                strUnits = unitsItem.toString();
                                strTooltip = strItemDescription;
                                //System.out.println(SOURCE + "found real metadata for indicator index=" + intIndicatorIndex);
                                }
                            else
                                {
                                // Incomplete information supplied, so no choice now but to use the defaults individually
                                // This is very unlikely, because the Keys should have been found above
                                strValue = MetadataHelper.getMetadataValueByKey(metadataCollection,
                                                                                controlpanel.getControlPanelIndicatorDefaultValueKeys().get(intIndicatorIndex));
                                strUnits = controlpanel.getControlPanelIndicatorDefaultUnits().get(intIndicatorIndex).toString();
                                strTooltip = MetadataHelper.getMetadataValueByKey(metadataCollection,
                                                                                  controlpanel.getControlPanelIndicatorDefaultTooltipKeys().get(intIndicatorIndex));
                                //System.out.println(SOURCE + "No choice now but to use the default metadata for ControlPanel");
                                }
                            }

                        // Update the Value and Units if possible
                        if (strValue != null)
                            {
                            if (SchemaUnits.DIMENSIONLESS.toString().equals(strUnits))
                                {
                                // Don't display DIMENSIONLESS Units (no point and too long)
                                indicator.setValue(strValue);
                                }
                            else if (NO_DATA.equals(strValue))
                                {
                                // Don't display Units if there's NO_DATA
                                indicator.setValue(strValue);
                                }
                            else
                                {
                                // See if we have been asked to display Units
                                if (indicator.areUnitsVisible())
                                    {
                                    indicator.setValue(strValue + SPACE + strUnits);
                                    }
                                else
                                    {
                                    indicator.setValue(strValue);
                                    }
                                }
                            }
                        else
                            {
                            // Don't display Units if there's no Value
                            indicator.setValue(EMPTY_STRING);
                            }

                        // Finally the Tooltip
                        indicator.setToolTip(strTooltip);
                        }
                    else
                        {
                        // The Instrument isn't doing anything, so don't display anything
                        indicator.setValue(QUERY);
                        indicator.setToolTip(QUERY);
                        }
                    }
                }
            else
                {
                // The ObservatoryClock is different, and produces an unwanted warning message
                if (!(controlpanel.getHostInstrument() instanceof ObservatoryClockInterface))
                    {
                    LOGGER.warn(SOURCE + "Unable to update Control Panel [instrument=" + controlpanel.getHostInstrument().getInstrument().getIdentifier() + "]");
                    }

                // Some debugging...
                if (false)
                    {
                    LOGGER.error(SOURCE + "Number of indicators specified=" + controlpanel.getControlPanelIndicators().size());

                    if ((controlpanel.getControlPanelIndicatorDefaultValueKeys() != null)
                        && (controlpanel.getControlPanelIndicatorDefaultValueKeys().size() == controlpanel.getControlPanelIndicators().size()))
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultValueKeys size=" + controlpanel.getControlPanelIndicatorDefaultValueKeys().size());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultValueKeys incorrectly specified");
                        }

                    if ((controlpanel.getControlPanelIndicatorDefaultUnits() != null)
                        && (controlpanel.getControlPanelIndicatorDefaultUnits().size() == controlpanel.getControlPanelIndicators().size()))
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultUnits size=" + controlpanel.getControlPanelIndicatorDefaultUnits().size());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultUnits incorrectly specified");
                        }

                    if ((controlpanel.getControlPanelIndicatorDefaultTooltipKeys() != null)
                        && (controlpanel.getControlPanelIndicatorDefaultTooltipKeys().size() == controlpanel.getControlPanelIndicators().size()))
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultTooltipKeys size=" + controlpanel.getControlPanelIndicatorDefaultTooltipKeys().size());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultTooltipKeys incorrectly specified");
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Update Digital Panel Meters, if possible
     * Use the ControlPanel IndicatorMetadataKeys or default Metadata as appropriate,
     * in the same manner as updating a ControlPanel.
     *
     * @param hostinstrument
     * @param controlpanel
     * @param meterpanel
     */

    public static void updateDigitalPanelMeters(final ObservatoryInstrumentInterface hostinstrument,
                                                final InstrumentUIComponentDecoratorInterface controlpanel,
                                                final DigitalPanelMeterUIComponent meterpanel)
        {
        final String SOURCE = "InstrumentUIHelper.updateDigitalPanelMeters() ";

        // Be certain, but these should all be valid from the decorator
        // Fail silently with NULLs or zero Indicators
        if ((hostinstrument != null)
            && (hostinstrument.getInstrument() != null)
            // Ensure the Meters are well-behaved
            && (meterpanel != null)
            && (meterpanel.getIndicatorCount() > 0)
            && (meterpanel.getMeters() != null)
            && (meterpanel.getMeters().size() == meterpanel.getIndicatorCount())
            // Ensure that there's enough Metadata specified for the Meters
            && (controlpanel != null)
            && (controlpanel.getControlPanelIndicators() != null)
            && (controlpanel.getControlPanelIndicators().size() >= meterpanel.getIndicatorCount()))
            {
            // If there are non-zero Indicators, we should try to update all of them
            // Check that all ControlPanel Indicators have default Keys, Units and Tooltips
            if ((controlpanel.getControlPanelIndicatorDefaultValueKeys() != null)
                && (controlpanel.getControlPanelIndicatorDefaultValueKeys().size() == controlpanel.getControlPanelIndicators().size())
                && (controlpanel.getControlPanelIndicatorDefaultUnits() != null)
                && (controlpanel.getControlPanelIndicatorDefaultUnits().size() == controlpanel.getControlPanelIndicators().size())
                && (controlpanel.getControlPanelIndicatorDefaultTooltipKeys() != null)
                && (controlpanel.getControlPanelIndicatorDefaultTooltipKeys().size() == controlpanel.getControlPanelIndicators().size()))
                {
                final List<Metadata> metadataCollection;

                // Gather together the full set of Metadata associated with this Observatory,
                // this Instrument and its DAO and translator to search for required items
                metadataCollection = hostinstrument.getAggregateMetadata();

//                MetadataHelper.showMetadataList(metadataCollection,
//                                                decorator.getHostInstrument().getInstrument().getIdentifier() + " Metadata pool for Digital Panel Meter");

                // Update each Meter in turn
                // Remember to discard the Temperature Channel if one exists,
                // so the first Indicator is data Channel 0
                for (int intMeterIndex = 0;
                     intMeterIndex < meterpanel.getMeters().size();
                     intMeterIndex++)
                    {
                    final IndicatorInterface meter;

                    // Handle each Meter in turn
                    meter = meterpanel.getMeters().get(intMeterIndex);

                    // Only update the Meter value if the Instrument is doing something
                    if (InstrumentState.isDoingSomething(hostinstrument))
                        {
                        Metadata metadata;
                        final String strName;
                        final String strValue;
                        final String strUnits;
                        final String strTooltip;
                        final int intChannelIndex;
                        final boolean boolHasTemperatureChannel;

                        metadata = null;

                        //intChannelIndex = mapIndicatorToChannelIndex(controlpanel, intMeterIndex);
                        intChannelIndex = intMeterIndex;
                        boolHasTemperatureChannel = hostinstrument.getDAO().hasTemperatureChannel();

                        //--------------------------------------------------------------------------
                        // Show the channel colours if there is a DAO

                        if (hostinstrument.getDAO() != null)
                            {
                            final ColourInterface colour;

                            // This will try to find Observation.Channel.Colour.n
                            // Returns r=0 g=0 b=0 if the colour is not specified correctly
                            colour = MetadataHelper.getChannelColour(metadataCollection,
                                                                     intChannelIndex,
                                                                     boolHasTemperatureChannel);
                            if ((colour != null)
                                && ((colour.getColor().getRed() >= LOW_CONTRAST)
                                    || (colour.getColor().getGreen() >= LOW_CONTRAST)
                                    || (colour.getColor().getBlue() >= LOW_CONTRAST)))
                                {
                                meter.setValueForeground(colour.getColor());
                                }
                            else
                                {
                                // Always use the default if no Colour specified in the metadata,
                                // or if the Colour was Black (which would be invisible with the the default settings)
                                meter.setValueForeground(IndicatorInterface.DEFAULT_VALUE_COLOR);
                                }
                            }
                        else
                            {
                            // Always use the default colours if there is no DAO
                            // This shouldn't happen because there wouldn't be any channel data either
                            meter.setForeground(IndicatorInterface.DEFAULT_VALUE_COLOR);
                            }

                        //--------------------------------------------------------------------------
                        // See if there are any custom Metadata Value keys for the control panel indicators
                        // in the Instrument XML <IndicatorMetadataKey>
                        // These will take precedence over any Metadata already present
                        // otherwise the User can't influence what appears on the panel

                        if ((hostinstrument.getInstrument().getIndicatorMetadataKeyList() != null)
                            && (hostinstrument.getInstrument().getIndicatorMetadataKeyList().size() >= controlpanel.getControlPanelIndicators().size()))
                            {
                            metadata = MetadataHelper.getMetadataByKey(metadataCollection,
                                                                       hostinstrument.getInstrument().getIndicatorMetadataKeyList().get(intMeterIndex));
                            }

                        // Did the supplied keys in the Instrument XML <IndicatorMetadataKey> map to a real Metadata item?
                        // Assume that {Value, Units, Tooltip} come from the one item (Colour not stored here)
                        if (metadata != null)
                            {
                            // If there was a valid Value, we'll assume there's a valid Name (otherwise get NO_DATA)
                            strName = MetadataHelper.getChannelName(metadataCollection,
                                                                    intChannelIndex,
                                                                    boolHasTemperatureChannel);
                            strValue = metadata.getValue();
                            strUnits = metadata.getUnits().toString();
                            strTooltip = metadata.getDescription();
                            }
                        else if (MetadataDictionary.KEY_INSTRUMENT_RESERVED_ADDRESS.getKey().equals(controlpanel.getControlPanelIndicatorDefaultValueKeys().get(intMeterIndex)))
                            {
                            // See if the defaults require special formatting for an Address or Port
                            strName = "Address";
                            strValue = InstrumentHelper.findInstrumentAddress(hostinstrument);
                            strUnits = controlpanel.getControlPanelIndicatorDefaultUnits().get(intMeterIndex).toString();

                            if (hostinstrument.getInstrument().getController() != null)
                                {
                                if (hostinstrument.getInstrument().getController().isSetStaribusAddress())
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_STARIBUS;
                                    }
                                else if (hostinstrument.getInstrument().getController().isSetIPAddress())
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_IP;
                                    }
                                else if (hostinstrument.getInstrument().getController().isSetVirtualAddress())
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_VIRTUAL;
                                    }
                                else
                                    {
                                    strTooltip = TOOLTIP_ADDRESS_ERROR;
                                    }
                                }
                            else
                                {
                                strTooltip = TOOLTIP_ADDRESS_NO_CONTROLLER;
                                }
                            }
                        else if (MetadataDictionary.KEY_INSTRUMENT_RESERVED_PORT.getKey().equals(controlpanel.getControlPanelIndicatorDefaultValueKeys().get(intMeterIndex)))
                            {
                            strName = "Port";
                            strValue = InstrumentHelper.findInstrumentPort(hostinstrument);
                            strUnits = controlpanel.getControlPanelIndicatorDefaultUnits().get(intMeterIndex).toString();
                            strTooltip = TOOLTIP_PORT;
                            }
                        else
                            {
                            final String strItemName;
                            final String strItemValue;
                            final SchemaUnits.Enum unitsItem;
                            final String strItemDescription;

                            //----------------------------------------------------------------------
                            // The Control Panel indicator requires:
                            //
                            //  Name            Observation.Channel.Name.n
                            //  Value           Observation.Channel.Value.n
                            //  Units           Observation.Channel.Units.n
                            //  Tooltip         Observation.Channel.Description.n
                            //  Colour          Observation.Channel.Colour.n
                            //
                            // The Value item *should* have the correct Units and Tooltip
                            // but... in case it doesn't, use the above Metadata keys
                            //
                            //----------------------------------------------------------------------

                            // The Name will appear in the Status field
                            // Return NO_DATA if no Name is found
                            strItemName = MetadataHelper.getChannelName(metadataCollection,
                                                                        intChannelIndex,
                                                                        boolHasTemperatureChannel);

                            // Try to find the Value, but remember that if there is no DAO there are no Channels
                            // Return a NO_DATA if the key cannot be found
                            strItemValue = MetadataHelper.getChannelValue(metadataCollection,
                                                                          intChannelIndex,
                                                                          boolHasTemperatureChannel);

                            // Try to find the Units - this looks for Observation.Channel.Units.n
                            // This returns DIMENSIONLESS if not found, or if the Units were DIMENSIONLESS
                            unitsItem = MetadataHelper.getChannelUnits(metadataCollection,
                                                                       intChannelIndex,
                                                                       boolHasTemperatureChannel);

                            // Try to find the Tooltip - this looks for Observation.Channel.Description.n
                            // This returns an NO_DATA if no Description is found
                            strItemDescription = MetadataHelper.getChannelDescription(metadataCollection,
                                                                                      intChannelIndex,
                                                                                      boolHasTemperatureChannel);
                            // Did we find a valid Value
                            if ((strItemValue != null)
                                && (!NO_DATA.equals(strItemValue)))
                                {
                                strName = strItemName;
                                strValue = strItemValue;
                                strUnits = unitsItem.toString();
                                strTooltip = strItemDescription;
                                }
                            else
                                {
                                // Incomplete information supplied, so no choice now but to use the defaults individually
                                // This is very unlikely, because the Keys should have been found above
                                strName = "Channel " + intMeterIndex;
                                strValue = MetadataHelper.getMetadataValueByKey(metadataCollection,
                                                                                controlpanel.getControlPanelIndicatorDefaultValueKeys().get(intMeterIndex));
                                strUnits = controlpanel.getControlPanelIndicatorDefaultUnits().get(intMeterIndex).toString();
                                strTooltip = MetadataHelper.getMetadataValueByKey(metadataCollection,
                                                                                  controlpanel.getControlPanelIndicatorDefaultTooltipKeys().get(intMeterIndex));
                                }
                            }

                        // Update the Value and Units if possible
                        if (strValue != null)
                            {
                            if (SchemaUnits.DIMENSIONLESS.toString().equals(strUnits))
                                {
                                // Don't display DIMENSIONLESS Units (no point and too long)
                                meter.setValue(strValue);
                                meter.setUnits(EMPTY_STRING);
                                }
                            else if (NO_DATA.equals(strValue))
                                {
                                // Don't display Units if there's NO_DATA
                                meter.setValue(strValue);
                                meter.setUnits(EMPTY_STRING);
                                }
                            else
                                {
                                // See if we have been asked to display Units
                                if (meter.areUnitsVisible())
                                    {
                                    meter.setValue(strValue);
                                    meter.setUnits(strUnits);
                                    }
                                else
                                    {
                                    meter.setValue(strValue);
                                    meter.setUnits(EMPTY_STRING);
                                    }
                                }
                            }
                        else
                            {
                            // Don't display Units if there's no Value
                            meter.setValue(EMPTY_STRING);
                            meter.setUnits(EMPTY_STRING);
                            }

                        // Finally the Name and Tooltip
                        meter.setStatus(strName);
                        meter.setToolTip(strTooltip);
                        }
                    else
                        {
                        // The Instrument isn't doing anything, so don't display anything
                        meter.setValue(QUERY);
                        meter.setUnits(EMPTY_STRING);
                        meter.setStatus(QUERY);
                        meter.setToolTip(QUERY);
                        }
                    }
                }
            else
                {
                LOGGER.warn(SOURCE + "Unable to update Digital Panel Meters [instrument=" + hostinstrument.getInstrument().getIdentifier() + "]");

                // Some debugging...
                if (false)
                    {
                    LOGGER.error(SOURCE + "Number of indicators specified=" + controlpanel.getControlPanelIndicators().size());

                    if ((controlpanel.getControlPanelIndicatorDefaultValueKeys() != null)
                        && (controlpanel.getControlPanelIndicatorDefaultValueKeys().size() == controlpanel.getControlPanelIndicators().size()))
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultValueKeys size=" + controlpanel.getControlPanelIndicatorDefaultValueKeys().size());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultValueKeys incorrectly specified");
                        }

                    if ((controlpanel.getControlPanelIndicatorDefaultUnits() != null)
                        && (controlpanel.getControlPanelIndicatorDefaultUnits().size() == controlpanel.getControlPanelIndicators().size()))
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultUnits size=" + controlpanel.getControlPanelIndicatorDefaultUnits().size());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultUnits incorrectly specified");
                        }

                    if ((controlpanel.getControlPanelIndicatorDefaultTooltipKeys() != null)
                        && (controlpanel.getControlPanelIndicatorDefaultTooltipKeys().size() == controlpanel.getControlPanelIndicators().size()))
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultTooltipKeys size=" + controlpanel.getControlPanelIndicatorDefaultTooltipKeys().size());
                        }
                    else
                        {
                        LOGGER.error(SOURCE + "ControlPanelIndicatorDefaultTooltipKeys incorrectly specified");
                        }
                    }
                }
            }
        }


    /***********************************************************************************************
     * Map an Indicator Index to a Channel Index, taking into account the Temperature Channel,
     * and the possible absence of a DAO (in which case there are no Channels as such).
     * Also take into account whether the data are Timestamped or Indexed (columnar).
     *
     * @param decorator
     * @param indicatorindex
     *
     * @return int
     */

    public static int mapIndicatorToChannelIndex(final InstrumentUIComponentDecoratorInterface decorator,
                                                 final int indicatorindex)
        {
        final String SOURCE = "InstrumentUIHelper.mapIndicatorToChannelIndex() ";
        final int intChannelIndex;
        final boolean boolDebug;

        boolDebug = true;

        // The Timestamped sample format in the Vector is one of:
        //
        // 0           1            2           3
        // <Calendar> <Temperature> <Channel0> [<Channel1> <Channel2> ...]
        //
        // 0           1           2
        // <Calendar> <Channel0> [<Channel1> <Channel2> ...]

        // The Indexed sample format in the Vector is one of:
        //
        // 0          1             2           3
        // <X-Value>  <Temperature> <Channel0> [<Channel1> <Channel2> ...]
        //
        // 0          1           2
        // <X-Value>  <Channel0> [<Channel1> <Channel2> ...]
        //
        // Timestamped
        //                                                                  Channel Index Offset
        // <Calendar> <Temperature> <Channel0> [<Channel1> <Channel2> ...]  1
        // <Calendar>               <Channel0> [<Channel1> <Channel2> ...]  0
        //
        // Indexed
        //
        // <X-Value>  <Temperature> <Channel0> [<Channel1> <Channel2> ...]  2  (this should never occur?)
        // <X-Value>                <Channel0> [<Channel1> <Channel2> ...]  1

        if (decorator.getHostInstrument().getDAO() != null)
            {
            if ((DataAnalyser.isCalendarisedRawData(decorator.getHostInstrument().getDAO().getRawData()))
                && (decorator.getHostInstrument().getDAO().hasTemperatureChannel()))
                {
                // Skip the Temperature channel when getting Channel information
                intChannelIndex = indicatorindex + 1;
                LOGGER.debug(boolDebug,
                             SOURCE + "Calendarised RawData, Temperature [index.channel = " + intChannelIndex + "] [index.indicator=" + indicatorindex + "]");
                }
            else if ((DataAnalyser.isCalendarisedRawData(decorator.getHostInstrument().getDAO().getRawData()))
                     && (!decorator.getHostInstrument().getDAO().hasTemperatureChannel()))
                {
                intChannelIndex = indicatorindex;
                LOGGER.debug(boolDebug,
                             SOURCE + "Calendarised RawData, NO Temperature [index.channel = " + intChannelIndex + "] [index.indicator=" + indicatorindex + "]");
                }
            else if ((DataAnalyser.isColumnarRawData(decorator.getHostInstrument().getDAO().getRawData()))
                     && (decorator.getHostInstrument().getDAO().hasTemperatureChannel()))
                {
                // This should never occur?
                intChannelIndex = indicatorindex + 2;
                LOGGER.debug(boolDebug,
                             SOURCE + "Columnar RawData, Temperature [index.channel = " + intChannelIndex + "] [index.indicator=" + indicatorindex + "]");
                }
            else if ((DataAnalyser.isColumnarRawData(decorator.getHostInstrument().getDAO().getRawData()))
                     && (!decorator.getHostInstrument().getDAO().hasTemperatureChannel()))
                {
                intChannelIndex = indicatorindex + 1;
                LOGGER.debug(boolDebug,
                             SOURCE + "Columnar RawData, NO Temperature [index.channel = " + intChannelIndex + "] [index.indicator=" + indicatorindex + "]");
                }
            else
                {
                // This will occur when there are no RawData, which is allowed
                intChannelIndex = indicatorindex;
                LOGGER.debug(boolDebug,
                             SOURCE + "NO RawData [index.channel = " + intChannelIndex + "] [index.indicator=" + indicatorindex + "]");
                }
            }
        else
            {
            intChannelIndex = indicatorindex;
            LOGGER.debug(boolDebug,
                         SOURCE + "NULL DAO [index.channel = " + intChannelIndex + "] [index.indicator=" + indicatorindex + "]");
            }

        return (intChannelIndex);
        }


    /***********************************************************************************************
     * Set the display Indicator at the specified Index.
     *
     * @param decorator
     * @param indicator
     * @param index
     */

    public static void setIndicatorAtIndex(final InstrumentUIComponentDecoratorInterface decorator,
                                           final IndicatorInterface indicator,
                                           final int index)
        {
        switch (index)
            {
            // This is a real bodge, but we need to make progress....
            case 0: { decorator.setIndicator0(indicator); break; }
            case 1: { decorator.setIndicator1(indicator); break; }
            case 2: { decorator.setIndicator2(indicator); break; }
            case 3: { decorator.setIndicator3(indicator); break; }
            case 4: { decorator.setIndicator4(indicator); break; }
            case 5: { decorator.setIndicator5(indicator); break; }
            case 6: { decorator.setIndicator6(indicator); break; }
            case 7: { decorator.setIndicator7(indicator); break; }
            default: { }  // Do nothing
            }
        }
    }
