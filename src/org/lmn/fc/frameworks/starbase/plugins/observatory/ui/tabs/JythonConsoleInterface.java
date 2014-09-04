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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs;

import org.python.util.InteractiveInterpreter;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.text.Caret;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.image.ImageObserver;
import java.awt.print.Printable;
import java.io.Serializable;
import java.text.MessageFormat;


/***************************************************************************************************
 * JythonConsoleInterface.
 */

public interface JythonConsoleInterface extends ImageObserver,
                                                MenuContainer,
                                                Serializable,
                                                Scrollable,
                                                Accessible,
                                                KeyListener,
                                                Printable
    {
    // String Resources
    String MSG_SIGN_ON          = "Jython Interactive Console\r\n>>> ";
    String MSG_EXECUTE_SCRIPT   = "\r\n--- ExecuteScript\r\n";
    String MSG_EXECUTE_FILE     = "\r\n--- ExecuteFile\r\n";


    /***********************************************************************************************
     * Sets the background color of this component.
     *
     * @param color
     */

    void setBackground(Color color);


    /***********************************************************************************************
     * Sets the foreground color of this component.
     *
     * @param color
     */

    void setForeground(Color color);


    /***********************************************************************************************
     * Sets the font of this component.
     *
     * @param font
     */

    void setFont(Font font);


    /***********************************************************************************************
     * Registers the text to display in a tool tip.
     *
     * @param text  the string to display; if the text is <code>null</code>,
     *              the tool tip is turned off for this component
     */

    void setToolTipText(String text);


    /***********************************************************************************************
     * Sets the number of characters to expand tabs to.
     *
     * @param size number of characters to expand to
     */

    void setTabSize(int size);


    /***********************************************************************************************
     * Sets margin space between the text component's border
     * and its text.
     *
     * @param insets the space between the border and the text
     */

    void setMargin(Insets insets);


    /***********************************************************************************************
     * Returns the text contained in this <code>TextComponent</code>.
     *
     * @return String
     */

     String getText();


    /***********************************************************************************************
     * Set the displayed text.
     *
     * @param text
     */

    void setText(String text);


    /***********************************************************************************************
     * Get the Jython InteractiveInterpreter.
     *
     * @return InteractiveInterpreter
     */

    InteractiveInterpreter getInterpreter();


    /***********************************************************************************************
     * Fetches the caret that allows text-oriented navigation over
     * the view.
     *
     * @return Caret
     */

    Caret getCaret();


    /***********************************************************************************************
     * Get the Edit Start index.
     *
     * @return int
     */

    int getEditStart();


    /***********************************************************************************************
     * Returns the selected text's start position.  Return 0 for an
     * empty document, or the value of dot if no selection.
     *
     * @return the start position >= 0
     */

    int getSelectionStart();


    /***********************************************************************************************
     * Indicate if the InteractiveInterpreter is currently running.
     *
     * @return boolean
     */

    boolean isRunning();


    /************************************************************************************************
     * Returns a {@code Printable} to use for printing the content of this
     * {@code JTextComponent}. The returned {@code Printable} prints
     * the document as it looks on the screen except being reformatted
     * to fit the paper.
     * The returned {@code Printable} can be wrapped inside another
     * {@code Printable} in order to create complex reports and
     * documents.
     *
     *
     * <p>
     * The returned {@code Printable} shares the {@code document} with this
     * {@code JTextComponent}. It is the responsibility of the developer to
     * ensure that the {@code document} is not mutated while this {@code Printable}
     * is used. Printing behavior is undefined when the {@code document} is
     * mutated during printing.
     *
     * <p>
     * Page header and footer text can be added to the output by providing
     * {@code MessageFormat} arguments. The printing code requests
     * {@code Strings} from the formats, providing a single item which may be
     * included in the formatted string: an {@code Integer} representing the
     * current page number.
     *
     * <p>
     * The returned {@code Printable} when printed, formats the
     * document content appropriately for the page size. For correct
     * line wrapping the {@code imageable width} of all pages must be the
     * same. See {@link java.awt.print.PageFormat#getImageableWidth}.
     *
     * <p>
     * This method is thread-safe, although most Swing methods are not. Please
     * see <A
     * HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">
     * How to Use Threads</A> for more information.
     *
     * <p>
     * The returned {@code Printable} can be printed on any thread.
     *
     * <p>
     * This implementation returned {@code Printable} performs all painting on
     * the <i>Event Dispatch Thread</i>, regardless of what thread it is
     * used on.
     *
     * @param headerformat the text, in {@code MessageFormat}, to be
     *        used as the header, or {@code null} for no header
     * @param footerformat the text, in {@code MessageFormat}, to be
     *        used as the footer, or {@code null} for no footer
     * @return a {@code Printable} for use in printing content of this
     *         {@code JTextComponent}
     */

    Printable getPrintable(MessageFormat headerformat,
                           MessageFormat footerformat);
    }
