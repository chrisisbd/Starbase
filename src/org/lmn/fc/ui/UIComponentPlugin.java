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

package org.lmn.fc.ui;

import org.lmn.fc.common.actions.ContextActionGroup;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.datatypes.types.ColourDataType;
import org.lmn.fc.model.datatypes.types.FontDataType;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.print.PageFormat;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.net.URL;
import java.util.Vector;

//        http://java.sun.com/products/jfc/tsc/articles/painting/index.html
//
//        http://java.sun.com/j2se/1.3/docs/api/javax/swing/JComponent.html
//
//        My understanding or invalidate(), validate(), revalidate(), repaint() is as follows -
//
//        invalidate()/validate()/revalidate() only affects the layout.
// It results in a repaint if the layout actually changes.
//        invalidate()/validate() is the mechanism for batching changes (like beginPaint, paint, paint, paint, endPaint of windowing SDKs).
// You mark, using invalidate(), all the components that might affect the layout as invalid.
// Then call the parent container's validate() method. In the validate() method the parent checks
// if at least one of its immediate children is invalid. If it finds a child marked invalid, it calls layoutComponents() on its layout manager.
//
//        If the layoutmanager actually re-lays out the children, then, as a side effect, repaint() will get called.
//
//        The thing to note here is that the Container only looks for invalid immediate children.
//
//        For precisely this reason sometimes the resulting layout is not what you expected as the parent's parent
// and children's chidren may not get re-layed out. To work around this issue Swing added the revalidate() method.
//        What revalidate() does is basically marks all the container upto the top level
// (window and its subclasses or applet) as invalid. Then it calls validate() on the top level.

/***********************************************************************************************
 * The UIComponentPlugin.
 */

public interface UIComponentPlugin extends ImageObserver,
                                           MenuContainer,
                                           Serializable,
                                           Accessible,
                                           FrameworkConstants,
                                           FrameworkStrings,
                                           FrameworkMetadata,
                                           FrameworkRegex,
                                           FrameworkSingletons,
                                           ResourceKeys
    {
    // String Resources
    String MSG_WAITING_FOR_DATA = "Waiting for data...";
    String MSG_PLEASE_WAIT = "Please wait";
    String MSG_LOADING_PLEASE_WAIT = "Loading, please wait";
    String MSG_PRINT_CANCELLED = "The Print job was cancelled by the User";
    String TOOLTIP_COPY = "You may copy text using ctrl-C";

    // Icons for Toolbars
    String FILENAME_ICON_BACK                   = "toolbars/toolbar-backward.png";
    String FILENAME_ICON_CONFIG_COMMIT          = "toolbars/toolbar-validate.png";
    String FILENAME_ICON_CONFIG_REVERT          = "toolbars/toolbar-reload.png";
    String FILENAME_ICON_COPY                   = "toolbars/toolbar-copy.png";
    String FILENAME_ICON_CLEAR_ALL              = "toolbars/toolbar-clearall.png";
    String FILENAME_ICON_CUT                    = "toolbars/toolbar-cut.png";
    String FILENAME_ICON_DELETE                 = "toolbars/toolbar-delete.png";
    String FILENAME_ICON_DILBERT                = "toolbars/toolbar-dilbert.png";
    String FILENAME_ICON_DISPOSE                = "toolbars/toolbar-dispose.png";
    String FILENAME_ICON_END                    = "toolbars/toolbar-end.png";
    String FILENAME_ICON_EXECUTE_JYTHON_FILE    = "toolbars/toolbar-execute-jython-file.png";
    String FILENAME_ICON_EXECUTE_JYTHON_SCRIPT  = "toolbars/toolbar-execute-jython-script.png";
    String FILENAME_ICON_EXPORT_CONFIG          = "toolbars/toolbar-export-config.png";
    String FILENAME_ICON_EXPORT_JYTHON          = "toolbars/toolbar-export-jython.png";
    String FILENAME_ICON_FAST_BACKWARD          = "toolbars/toolbar-fast-backward.png";
    String FILENAME_ICON_FAST_FORWARD           = "toolbars/toolbar-fast-forward.png";
    String FILENAME_ICON_FORWARD                = "toolbars/toolbar-forward.png";
    String FILENAME_ICON_HELP                   = "toolbars/toolbar-help.png";
    String FILENAME_ICON_HEX_EDITOR             = "toolbars/toolbar-hex-editor.png";
    String FILENAME_ICON_JENKINS                = "toolbars/toolbar-jenkins.png";
    String FILENAME_ICON_MANTIS                 = "toolbars/toolbar-mantis.png";
    String FILENAME_ICON_METADATA_EDITOR        = "toolbars/toolbar-metadata-editor.png";
    String FILENAME_ICON_MEDIA_STOP             = "toolbars/toolbar-media-stop.png";
    String FILENAME_ICON_OPEN_FILE              = "toolbars/toolbar-open-file.png";
    String FILENAME_ICON_PAGE_SETUP             = "toolbars/toolbar-page-setup.png";
    String FILENAME_ICON_PASTE                  = "toolbars/toolbar-paste.png";
    String FILENAME_ICON_PAUSE                  = "toolbars/toolbar-pause.png";
    String FILENAME_ICON_PLAY                   = "toolbars/toolbar-play.png";
    String FILENAME_ICON_PRINT                  = "toolbars/toolbar-print.png";
    String FILENAME_ICON_PYTHON                 = "toolbars/toolbar-python.png";
    String FILENAME_ICON_REDO                   = "toolbars/toolbar-redo.png";
    String FILENAME_ICON_REFRESH                = "toolbars/toolbar-reload.png";
    String FILENAME_ICON_RELOAD                 = "toolbars/toolbar-reload.png";
    String FILENAME_ICON_REMOVE                 = "toolbars/toolbar-remove.png";
    String FILENAME_ICON_RESET_JYTHON           = "toolbars/toolbar-reset-jython.png";
    String FILENAME_ICON_SAVE_AS_FILE           = "toolbars/toolbar-saveas-file.png";
    String FILENAME_ICON_START                  = "toolbars/toolbar-start.png";
    String FILENAME_ICON_STOP                   = "toolbars/toolbar-stop.png";
    String FILENAME_ICON_SUBVERSION             = "toolbars/toolbar-subversion.png";
    String FILENAME_ICON_TRUNCATE               = "toolbars/toolbar-truncate.png";
    String FILENAME_ICON_UNDO                   = "toolbars/toolbar-undo.png";
    String FILENAME_ICON_ZOOM_IN                = "toolbars/toolbar-zoom-in.png";
    String FILENAME_ICON_ZOOM_OUT               = "toolbars/toolbar-zoom-out.png";

    // Icons for Dialog Boxes
    String FILENAME_ICON_DIALOG_COMMIT          = "dialog/dialog-commit.png";
    String FILENAME_ICON_DIALOG_EXPORT_CONFIG   = "dialog/dialog-export-config.png";
    String FILENAME_ICON_DIALOG_PRINT           = "dialog/dialog-print.png";

    //String MSG_NOT_RUNNING = "Not Running";

    ColourInterface COLOUR_RAG_NIGHT                 = new ColourDataType("r=0 g=0 b=0");
    ColourInterface COLOUR_RAG_SKY                   = new ColourDataType("r=0 g=67 b=191");
    ColourInterface COLOUR_RAG_TEXT                  = new ColourDataType("r=234 g=225 b=14");

    ColourInterface DEFAULT_COLOUR_TEXT              = new ColourDataType("r=42 g=123 b=198");
    ColourInterface DEFAULT_COLOUR_TEXT_HIGHLIGHT    = new ColourDataType("r=255 g=10 b=10");
    ColourInterface DEFAULT_COLOUR_CANVAS            = new ColourDataType("r=172 g=210 b=248");
    ColourInterface DEFAULT_COLOUR_PANEL             = new ColourDataType("r=239 g=239 b=239");
    ColourInterface DEFAULT_COLOUR_GRADIENT_TOP      = new ColourDataType("r=0 g=0 b=0");
    ColourInterface DEFAULT_COLOUR_GRADIENT_BOTTOM   = new ColourDataType("r=11 g=11 b=197");
    ColourInterface DEFAULT_COLOUR_TAB_BACKGROUND    = new ColourDataType("r=255 g=255 b=204");
    ColourInterface DEFAULT_COLOUR_HELP_BACKGROUND   = new ColourDataType("r=255 g=255 b=221");

    ColourInterface COLOUR_INFO_TEXT              = new ColourDataType("r=255 g=255 b=255");
    ColourInterface COLOUR_WARN_TEXT              = new ColourDataType("r=255 g=0 b=0");

    String DEFAULT_FONT_SPEC = "font=SansSerif style=plain size=12";
    String DEFAULT_FONT_SPEC_MONOSPACED = "font=Monospaced style=plain size=12";

    FontInterface DEFAULT_FONT = new FontDataType(DEFAULT_FONT_SPEC);
    FontInterface DEFAULT_FONT_MONOSPACED = new FontDataType(DEFAULT_FONT_SPEC_MONOSPACED);

    // Adjust the Toolbar to suit 24x24px Icons
    int HEIGHT_TOOLBAR_ICON = 24;
    int WIDTH_TOOLBAR_ICON = 24;
    int TOOLBAR_ICON_TEXT_GAP = 15;

    Dimension DIM_TOOLBAR_SIZE = new Dimension(2000, HEIGHT_TOOLBAR_ICON + 11);
    Dimension DIM_TOOLBAR_SEPARATOR_START = new Dimension(10, 5);
    Dimension DIM_TOOLBAR_SEPARATOR_NAME = new Dimension(40, 5);
    Dimension DIM_TOOLBAR_SEPARATOR = new Dimension(15, 5);
    Dimension DIM_TOOLBAR_SEPARATOR_HALFWIDTH = new Dimension(10, 5);
    Dimension DIM_TOOLBAR_SEPARATOR_BUTTON = new Dimension(10, 10);
    Dimension DIM_LABEL_SEPARATOR = new Dimension(5, 5);
    Dimension DIM_RADIOBUTTON_SEPARATOR = new Dimension(20, 5);
    Dimension DIM_KNOB_SEPARATOR = new Dimension(15, 5);
    Dimension DIM_SIDEBAR_MARGIN = new Dimension(20, 5);

    Color COLOR_TOOLBAR = new Color(5, 119, 167);

    Border BORDER_BUTTON = BorderFactory.createEmptyBorder(3, 3, 3, 3);
    Border BORDER_SIDEBAR = BorderFactory.createEmptyBorder(8, 0, 5, 0);
    Border BORDER_SIDEBAR_ITEM = BorderFactory.createEmptyBorder(0, 10, 0, 10);

    int MAX_UNIVERSE = Integer.MAX_VALUE;
    int HEIGHT_HEADER = 25;
    Dimension DIM_UNIVERSE = new Dimension(MAX_UNIVERSE, MAX_UNIVERSE);

    // Related to Atom and Task state
    void initialiseUI();

    void runUI();

    void stopUI();

    void disposeUI();


    /***********************************************************************************************
     * Remove any Data associated with this UIComponent's appearance on the UI.
     * For instance, remove a Chart regardless of it being visible.
     */

    void removeUIIdentity();

    // Painting
    // The validate method is used to cause a container to lay out its subcomponents again.
    // It should be invoked when this container's subcomponents are modified
    // (added to or removed from the container, or layout-related information changed)
    // after the container has been displayed.
    void validate();

    void add(Component component, Object constraints);

    void add(Component component, Object constraints, int index);

    void removeAll();

    void setBackground(Color background);

    int getWidth();

    int getHeight();

    void setAlignmentX(float alignmentX);

    void setAlignmentY(float alignmentY);


    /**
     * Sets the border of this component.  The <code>Border</code> object is
     * responsible for defining the insets for the component
     * (overriding any insets set directly on the component) and
     * for optionally rendering any border decorations within the
     * bounds of those insets.  Borders should be used (rather
     * than insets) for creating both decorative and non-decorative
     * (such as margins and padding) regions for a swing component.
     * Compound borders can be used to nest multiple borders within a
     * single component.
     * <p>
     * Although technically you can set the border on any object
     * that inherits from <code>JComponent</code>, the look and
     * feel implementation of many standard Swing components
     * doesn't work well with user-set borders.  In general,
     * when you want to set a border on a standard Swing
     * component other than <code>JPanel</code> or <code>JLabel</code>,
     * we recommend that you put the component in a <code>JPanel</code>
     * and set the border on the <code>JPanel</code>.
     * <p>
     * This is a bound property.
     *
     * @param border the border to be rendered for this component
     * @see Border
     * @see javax.swing.border.CompoundBorder
     * @beaninfo
     *        bound: true
     *    preferred: true
     *    attribute: visualUpdate true
     *  description: The component's border.
     */
    void setBorder(Border border);


    void setOpaque(boolean opaque);

    Dimension getMinimumSize();

    void setMinimumSize(Dimension minimumSize);

    Dimension getMaximumSize();

    void setMaximumSize(Dimension maximumSize);

    Dimension getPreferredSize();

    void setPreferredSize(Dimension preferredSize);

    boolean isVisible();

    void setVisible(boolean flag);

    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    URL findResource(String path, String resourceName);

    // UIComponent ContextActions
    Vector<ContextActionGroup> getUIComponentContextActionGroups();

    void setUIComponentContextActionGroups(Vector<ContextActionGroup> groups);

    void addUIComponentContextActionGroup(ContextActionGroup actiongroup);

    void removeUIComponentContextActionGroup(ContextActionGroup actiongroup);

    void clearUIComponentContextActionGroups();

    void clearUIComponentContextActionGroup(ContextActionGroup group);


    /************************************************************************************************
     * Get the PageFormat for printing.
     *
     * @return PageFormat
     */

    PageFormat getPageFormat();


    /*********************************************************************************************
     * Set the PageFormat for printing.
     *
     * @param pageformat
     */

    void setPageFormat(PageFormat pageformat);


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    boolean isDebug();


    /************************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @param debug
     */

    void setDebug(boolean debug);
    }
