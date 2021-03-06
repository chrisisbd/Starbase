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

package org.lmn.fc.ui.components;


import org.lmn.fc.ui.UIComponentPlugin;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;


/**************************************************************************************************
 * TextLineNumber.
 * This class will display line numbers for a related text component. The text
 * component must use the same line height for each line. TextLineNumber
 * supports wrapped lines and will highlight the line number of the current
 * line in the text component.
 * <p/>
 * This class was designed to be used as a component added to the row header
 * of a JScrollPane.
 * <p/>
 * See: http://tips4java.wordpress.com/2009/05/23/text-component-line-number/
 *
 * Use:
 * JTextPane textPane = new JTextPane();
 * JScrollPane scrollPane = new JScrollPane(textPane);
 * TextLineNumber tln = new TextLineNumber(textPane);
 * scrollPane.setRowHeaderView( tln );
 */

public class TextLineNumber extends JPanel
                            implements CaretListener,
                                       DocumentListener,
                                       PropertyChangeListener
    {
    public final static float LEFT   = 0.0f;
    public final static float CENTER = 0.5f;
    private final static float RIGHT  = 1.0f;

    private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

    //  Text component this TextTextLineNumber component is in sync with

    private final JTextComponent component;

    //  Properties that can be changed

    private boolean updateFont;
    private int     borderGap;
    private Color   currentLineForeground;
    private float   digitAlignment;
    private int     minimumDisplayDigits;

    //  Keep history information to reduce the number of times the component
    //  needs to be repainted

    private int lastDigits;
    private int lastHeight;
    private int lastLine;

    private HashMap<String, FontMetrics> fonts;


    /**********************************************************************************************
     * Create a line number component for a text component. This minimum
     * display width will be based on 3 digits.
     *
     * @param comp the related text component
     */

    public TextLineNumber(final JTextComponent comp)
        {
        this(comp, 3);
        }


    /**********************************************************************************************
     * Create a line number component for a text component.
     *
     * @param comp            the related text component
     * @param mindigits the number of digits used to calculate
     *                             the minimum width of the component
     */

    public TextLineNumber(final JTextComponent comp,
                          final int mindigits)
        {
        this.component = comp;

        setFont(comp.getFont());
        setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());
        setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());

        setBorderGap(5);
        setCurrentLineForeground(Color.RED);
        setDigitAlignment(RIGHT);
        setMinimumDisplayDigits(mindigits);

        comp.getDocument().addDocumentListener(this);
        comp.addCaretListener(this);
        comp.addPropertyChangeListener("font", this);
        }


    /**
     * Gets the update font property
     *
     * @return the update font property
     */
    public boolean getUpdateFont()
        {
        return updateFont;
        }


    /**
     * Set the update font property. Indicates whether this Font should be
     * updated automatically when the Font of the related text component
     * is changed.
     *
     * @param update when true update the Font and repaint the line
     *                   numbers, otherwise just repaint the line numbers.
     */
    public void setUpdateFont(final boolean update)
        {
        this.updateFont = update;
        }


    /**
     * Gets the border gap
     *
     * @return the border gap in pixels
     */
    public int getBorderGap()
        {
        return borderGap;
        }


    /**
     * The border gap is used in calculating the left and right insets of the
     * border. Default value is 5.
     *
     * @param gap the gap in pixels
     */
    public void setBorderGap(final int gap)
        {
        this.borderGap = gap;
        final Border inner = new EmptyBorder(0, gap, 0, gap);
        setBorder(new CompoundBorder(OUTER, inner));
        lastDigits = 0;
        setPreferredWidth();
        }


    /**
     * Gets the current line rendering Color
     *
     * @return the Color used to render the current line number
     */
    public Color getCurrentLineForeground()
        {
        if (currentLineForeground == null)
            {
            return getForeground();
            }
        else
            {
            return currentLineForeground;
            }
        }


    /**
     * The Color used to render the current line digits. Default is Color.RED.
     *
     * @param foreground the Color used to render the current line
     */
    public void setCurrentLineForeground(final Color foreground)
        {
        this.currentLineForeground = foreground;
        }


    /**
     * Gets the digit alignment
     *
     * @return the alignment of the painted digits
     */
    public float getDigitAlignment()
        {
        return digitAlignment;
        }


    /**
     * Specify the horizontal alignment of the digits within the component.
     * Common values would be:
     * <ul>
     * <li>TextLineNumber.LEFT
     * <li>TextLineNumber.CENTER
     * <li>TextLineNumber.RIGHT (default)
     * </ul>
     *
     * @param alignment
     */
    public void setDigitAlignment(final float alignment)
        {
        if (alignment < 0.0f)
            {
            if (alignment > 1.0f)
                {
                this.digitAlignment =
                        1.0f;
                }
            else
                {
                this.digitAlignment =
                        -1.0f;
                }
            }
        else
            {
            if (alignment > 1.0f)
                {
                this.digitAlignment =
                        1.0f;
                }
            else
                {
                this.digitAlignment =
                        alignment;
                }
            }
        }


    /**
     * Gets the minimum display digits
     *
     * @return the minimum display digits
     */
    public int getMinimumDisplayDigits()
        {
        return minimumDisplayDigits;
        }


    /**
     * Specify the minimum number of digits used to calculate the preferred
     * width of the component. Default is 3.
     *
     * @param mindigits the number digits used in the preferred
     *                             width calculation
     */
    public void setMinimumDisplayDigits(final int mindigits)
        {
        this.minimumDisplayDigits = mindigits;
        setPreferredWidth();
        }


    /**
     * Calculate the width needed to display the maximum line number
     */
    private void setPreferredWidth()
        {
        final Element root = component.getDocument().getDefaultRootElement();
        final int lines = root.getElementCount();
        final int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

        //  Update sizes when number of digits in the line number changes

        if (lastDigits != digits)
            {
            lastDigits = digits;
            final FontMetrics fontMetrics = getFontMetrics(getFont());
            final int width = fontMetrics.charWidth('0') * digits;
            final Insets insets = getInsets();
            final int preferredWidth = insets.left + insets.right + width;

            final Dimension d = getPreferredSize();
            d.setSize(preferredWidth, HEIGHT);
            setPreferredSize(d);
            setSize(d);
            }
        }


    /**
     * Draw the line numbers
     */
    @Override
    public void paintComponent(final Graphics g)
        {
        super.paintComponent(g);

        //	Determine the width of the space available to draw the line number

        final FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        final Insets insets = getInsets();
        final int availableWidth = getSize().width - insets.left - insets.right;

        //  Determine the rows to draw within the clipped bounds.

        final Rectangle clip = g.getClipBounds();
        int rowStartOffset = component.viewToModel(new Point(0, clip.y));
        final int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));

        while (rowStartOffset <= endOffset)
            {
            try
                {
                if (isCurrentLine(rowStartOffset))
                    {
                    g.setColor(getCurrentLineForeground());
                    }
                else
                    {
                    g.setColor(getForeground());
                    }

                //  Get the line number as a string and then determine the
                //  "X" and "Y" offsets for drawing the string.

                final String lineNumber = getTextLineNumber(rowStartOffset);
                final int stringWidth = fontMetrics.stringWidth(lineNumber);
                final int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                final int y = getOffsetY(rowStartOffset, fontMetrics);

                g.drawString(lineNumber, x, y);

                //  Move to the next row
                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
                }

            catch (Exception e)
                {
                }
            }
        }


    /*
    *  We need to know if the caret is currently positioned on the line we
    *  are about to paint so the line number can be highlighted.
    */
    private boolean isCurrentLine(final int rowStartOffset)
        {
        final int caretPosition = component.getCaretPosition();
        final Element root = component.getDocument().getDefaultRootElement();

        return root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition);
        }


    /*
    *	Get the line number to be drawn. The empty string will be returned
    *  when a line of text has wrapped.
    */
    protected String getTextLineNumber(final int rowStartOffset)
        {
        final Element root = component.getDocument().getDefaultRootElement();
        final int index = root.getElementIndex(rowStartOffset);
        final Element line = root.getElement(index);

        if (line.getStartOffset() == rowStartOffset)
            {
            return String.valueOf(index + 1);
            }
        else
            {
            return "";
            }
        }


    /*
    *  Determine the X offset to properly align the line number when drawn
    */
    private int getOffsetX(final int availableWidth,
                           final int stringWidth)
        {
        return (int) ((availableWidth - stringWidth) * digitAlignment);
        }


    /*
    *  Determine the Y offset for the current row
    */
    private int getOffsetY(final int rowStartOffset,
                           final FontMetrics fontMetrics)
            throws BadLocationException
        {
        //  Get the bounding rectangle of the row

        final Rectangle r = component.modelToView(rowStartOffset);
        final int lineHeight = fontMetrics.getHeight();
        final int y = r.y + r.height;
        int descent = 0;

        //  The text needs to be positioned above the bottom of the bounding
        //  rectangle based on the descent of the font(s) contained on the row.

        if (r.height == lineHeight)  // default font is being used
            {
            descent = fontMetrics.getDescent();
            }
        else  // We need to check all the attributes for font changes
            {
            if (fonts == null)
                {
                fonts = new HashMap<String, FontMetrics>();
                }

            final Element root = component.getDocument().getDefaultRootElement();
            final int index = root.getElementIndex(rowStartOffset);
            final Element line = root.getElement(index);

            for (int i = 0;
                 i < line.getElementCount();
                 i++)
                {
                final Element child = line.getElement(i);
                final AttributeSet as = child.getAttributes();
                final String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
                final Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
                final String key = fontFamily + fontSize;

                FontMetrics fm = fonts.get(key);

                if (fm == null)
                    {
                    final Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    fm = component.getFontMetrics(font);
                    fonts.put(key, fm);
                    }

                descent = Math.max(descent, fm.getDescent());
                }
            }

        return y - descent;
        }

    //
    //  Implement CaretListener interface
    //


    public void caretUpdate(final CaretEvent e)
        {
        //  Get the line the caret is positioned on

        final int caretPosition = component.getCaretPosition();
        final Element root = component.getDocument().getDefaultRootElement();
        final int currentLine = root.getElementIndex(caretPosition);

        //  Need to repaint so the correct line number can be highlighted

        if (lastLine != currentLine)
            {
            repaint();
            lastLine = currentLine;
            }
        }

    //
    //  Implement DocumentListener interface
    //


    public void changedUpdate(final DocumentEvent e)
        {
        documentChanged();
        }


    public void insertUpdate(final DocumentEvent e)
        {
        documentChanged();
        }


    public void removeUpdate(final DocumentEvent e)
        {
        documentChanged();
        }


    /*
    *  A document change may affect the number of displayed lines of text.
    *  Therefore the lines numbers will also change.
    */
    private void documentChanged()
        {
        //  Preferred size of the component has not been updated at the time
        //  the DocumentEvent is fired

        SwingUtilities.invokeLater(new Runnable()
        {
        public void run()
            {
            final int preferredHeight = component.getPreferredSize().height;

            //  Document change has caused a change in the number of lines.
            //  Repaint to reflect the new line numbers

            if (lastHeight != preferredHeight)
                {
                setPreferredWidth();
                repaint();
                lastHeight = preferredHeight;
                }
            }
        });
        }

    //
    //  Implement PropertyChangeListener interface
    //


    public void propertyChange(final PropertyChangeEvent evt)
        {
        if (evt.getNewValue() instanceof Font)
            {
            if (updateFont)
                {
                final Font newFont = (Font) evt.getNewValue();
                setFont(newFont);
                lastDigits = 0;
                setPreferredWidth();
                }
            else
                {
                repaint();
                }
            }
        }
    }
