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

package org.lmn.fc.ui.layout;

import javax.swing.*;
import java.awt.*;
import java.beans.XMLEncoder;
import java.io.PrintStream;
import java.io.Serializable;


// See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4294758

/**
 * A layout manager that allows multiple components to be laid out either
 * vertically or horizontally. The components will not wrap so, for
 * example, a vertical arrangement of components will stay vertically
 * arranged when the frame is resized.
 * <TABLE ALIGN="RIGHT" BORDER="0" SUMMARY="layout">
 * <TR>
 * <TD ALIGN="CENTER">
 * <P ALIGN="CENTER"><IMG SRC="doc-files/BoxLayout-1.gif"
 * alt="The following text describes this graphic."
 * WIDTH="191" HEIGHT="201" ALIGN="BOTTOM" BORDER="0">
 * </TD>
 * </TR>
 * </TABLE>
 * <p>
 * Nesting multiple panels with different combinations of horizontal and
 * vertical gives an effect similar to GridBagLayout, without the
 * complexity. The diagram shows two panels arranged horizontally, each
 * of which contains 3 components arranged vertically.
 *
 * <p> The BoxLayoutFixed manager is constructed with an axis parameter that
 * specifies the type of layout that will be done. There are four choices:
 *
 * <blockquote><b><tt>X_AXIS</tt></b> - Components are laid out horizontally
 * from left to right.</blockquote>
 *
 * <blockquote><b><tt>Y_AXIS</tt></b> - Components are laid out vertically
 * from top to bottom.</blockquote>
 *
 * <blockquote><b><tt>LINE_AXIS</tt></b> - Components are laid out the way
 * words are laid out in a line, based on the container's
 * <tt>ComponentOrientation</tt> property. If the container's
 * <tt>ComponentOrientation</tt> is horizontal then components are laid out
 * horizontally, otherwise they are laid out vertically.  For horizontal
 * orientations, if the container's <tt>ComponentOrientation</tt> is left to
 * right then components are laid out left to right, otherwise they are laid
 * out right to left. For vertical orientations components are always laid out
 * from top to bottom.</blockquote>
 *
 * <blockquote><b><tt>PAGE_AXIS</tt></b> - Components are laid out the way
 * text lines are laid out on a page, based on the container's
 * <tt>ComponentOrientation</tt> property. If the container's
 * <tt>ComponentOrientation</tt> is horizontal then components are laid out
 * vertically, otherwise they are laid out horizontally.  For horizontal
 * orientations, if the container's <tt>ComponentOrientation</tt> is left to
 * right then components are laid out left to right, otherwise they are laid
 * out right to left.&nbsp; For vertical orientations components are always
 * laid out from top to bottom.</blockquote>
 * <p>
 * For all directions, components are arranged in the same order as they were
 * added to the container.
 * <p>
 * BoxLayoutFixed attempts to arrange components
 * at their preferred widths (for horizontal layout)
 * or heights (for vertical layout).
 * For a horizontal layout,
 * if not all the components are the same height,
 * BoxLayoutFixed attempts to make all the components
 * as high as the highest component.
 * If that's not possible for a particular component,
 * then BoxLayoutFixed aligns that component vertically,
 * according to the component's Y alignment.
 * By default, a component has a Y alignment of 0.5,
 * which means that the vertical center of the component
 * should have the same Y coordinate as
 * the vertical centers of other components with 0.5 Y alignment.
 * <p>
 * Similarly, for a vertical layout,
 * BoxLayoutFixed attempts to make all components in the column
 * as wide as the widest component.
 * If that fails, it aligns them horizontally
 * according to their X alignments.  For <code>PAGE_AXIS</code> layout,
 * horizontal alignment is done based on the leading edge of the component.
 * In other words, an X alignment value of 0.0 means the left edge of a
 * component if the container's <code>ComponentOrientation</code> is left to
 * right and it means the right edge of the component otherwise.
 * <p>
 * Instead of using BoxLayoutFixed directly, many programs use the Box class.
 * The Box class is a lightweight container that uses a BoxLayoutFixed.
 * It also provides handy methods to help you use BoxLayoutFixed well.
 * Adding components to multiple nested boxes is a powerful way to get
 * the arrangement you want.
 * <p>
 * For further information and examples see
 * <a
 * href="http://java.sun.com/docs/books/tutorial/uiswing/layout/box.html">How to Use BoxLayout</a>,
 * a section in <em>The Java Tutorial.</em>
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link XMLEncoder}.
 *
 * @author Timothy Prinzing
 * @version 1.37 04/10/06
 * @see Box
 * @see ComponentOrientation
 * @see JComponent#getAlignmentX
 * @see JComponent#getAlignmentY
 */
public final class BoxLayoutFixed implements LayoutManager2,
                                             Serializable
    {
    /**
     * Specifies that components should be laid out left to right.
     */
    public static final int X_AXIS = 0;

    /**
     * Specifies that components should be laid out top to bottom.
     */
    public static final int Y_AXIS = 1;

    /**
     * Specifies that components should be laid out in the direction of
     * a line of text as determined by the target container's
     * <code>ComponentOrientation</code> property.
     */
    public static final int LINE_AXIS = 2;

    /**
     * Specifies that components should be laid out in the direction that
     * lines flow across a page as determined by the target container's
     * <code>ComponentOrientation</code> property.
     */
    public static final int PAGE_AXIS = 3;


    private int axis;
    private Container target;

    private transient SizeRequirements[] xChildren;
    private transient SizeRequirements[] yChildren;
    private transient SizeRequirements xTotal;
    private transient SizeRequirements yTotal;

    private transient PrintStream dbg;


    /**********************************************************************************************
     * Given one of the 4 axis values, resolve it to an absolute axis.
     * The relative axis values, PAGE_AXIS and LINE_AXIS are converted
     * to their absolute couterpart given the target's ComponentOrientation
     * value.  The absolute axes, X_AXIS and Y_AXIS are returned unmodified.
     *
     * @param axis the axis to resolve
     * @param o    the ComponentOrientation to resolve against
     *
     * @return the resolved axis
     */

    private static int resolveAxis(final int axis,
                                   final ComponentOrientation o)
        {
        final int absoluteAxis;

        if (axis == LINE_AXIS)
            {
            absoluteAxis = o.isHorizontal() ? X_AXIS : Y_AXIS;
            }
        else if (axis == PAGE_AXIS)
            {
            absoluteAxis = o.isHorizontal() ? Y_AXIS : X_AXIS;
            }
        else
            {
            absoluteAxis = axis;
            }

        return absoluteAxis;
        }


    /**
     * Creates a layout manager that will lay out components along the
     * given axis.
     *
     * @param target the container that needs to be laid out
     * @param axis   the axis to lay out components along. Can be one of:
     *               <code>BoxLayoutFixed.X_AXIS</code>,
     *               <code>BoxLayoutFixed.Y_AXIS</code>,
     *               <code>BoxLayoutFixed.LINE_AXIS</code> or
     *               <code>BoxLayoutFixed.PAGE_AXIS</code>
     *
     * @throws AWTError if the value of <code>axis</code> is invalid
     */
    public BoxLayoutFixed(final Container target,
                          final int axis)
        {
        if (axis != X_AXIS && axis != Y_AXIS &&
                axis != LINE_AXIS && axis != PAGE_AXIS)
            {
            throw new AWTError("Invalid axis");
            }

        this.axis = axis;
        this.target = target;
        }


    /**
     * Constructs a BoxLayoutFixed that
     * produces debugging messages.
     *
     * @param target the container that needs to be laid out
     * @param axis   the axis to lay out components along. Can be one of:
     *               <code>BoxLayoutFixed.X_AXIS</code>,
     *               <code>BoxLayoutFixed.Y_AXIS</code>,
     *               <code>BoxLayoutFixed.LINE_AXIS</code> or
     *               <code>BoxLayoutFixed.PAGE_AXIS</code>
     * @param dbg    the stream to which debugging messages should be sent,
     *               null if none
     */
    BoxLayoutFixed(final Container target,
                   final int axis,
                   final PrintStream dbg)
        {
        this(target,
             axis);
        this.dbg = dbg;
        }


    /**
     * Returns the container that uses this layout manager.
     *
     * @return the container that uses this layout manager
     *
     * @since 1.6
     */
    public synchronized final Container getTarget()
        {
        return this.target;
        }


    /**
     * Returns the axis that was used to lay out components.
     * Returns one of:
     * <code>BoxLayoutFixed.X_AXIS</code>,
     * <code>BoxLayoutFixed.Y_AXIS</code>,
     * <code>BoxLayoutFixed.LINE_AXIS</code> or
     * <code>BoxLayoutFixed.PAGE_AXIS</code>
     *
     * @return the axis that was used to lay out components
     *
     * @since 1.6
     */
    public synchronized final int getAxis()
        {
        return this.axis;
        }


    /**
     * Indicates that a child has changed its layout related information,
     * and thus any cached calculations should be flushed.
     * <p>
     * This method is called by AWT when the invalidate method is called
     * on the Container.  Since the invalidate method may be called
     * asynchronously to the event thread, this method may be called
     * asynchronously.
     *
     * @param target the affected container
     *
     * @throws AWTError if the target isn't the container specified to the
     *                  BoxLayoutFixed constructor
     */
    public synchronized void invalidateLayout(final Container target)
        {
        checkContainer(target);

        xChildren = null;
        yChildren = null;

        xTotal = null;
        yTotal = null;
        }


    /**
     * Not used by this class.
     *
     * @param name the name of the component
     * @param comp the component
     */
    public void addLayoutComponent(final String name,
                                   final Component comp)
        {
        if (comp != null)
            {
            invalidateLayout(comp.getParent());
            }
        }


    /**
     * Not used by this class.
     *
     * @param comp the component
     */
    public void removeLayoutComponent(final Component comp)
        {
        if (comp != null)
            {
            invalidateLayout(comp.getParent());
            }
        }


    /**
     * Not used by this class.
     *
     * @param comp        the component
     * @param constraints constraints
     */
    public void addLayoutComponent(final Component comp,
                                   final Object constraints)
        {
        if (comp != null)
            {
            invalidateLayout(comp.getParent());
            }
        }


    /**
     * Returns the preferred dimensions for this layout, given the components
     * in the specified target container.
     *
     * @param target the container that needs to be laid out
     *
     * @return the dimensions >= 0 && <= Integer.MAX_VALUE
     *
     * @throws AWTError if the target isn't the container specified to the
     *                  BoxLayoutFixed constructor
     * @see Container
     * @see #minimumLayoutSize
     * @see #maximumLayoutSize
     */
    public Dimension preferredLayoutSize(final Container target)
        {
        final Dimension size;

        synchronized (this)
            {
            checkContainer(target);
            checkRequests();

            if ((xTotal != null)
                && (yTotal != null))
                {
                size = new Dimension(xTotal.preferred,
                                     yTotal.preferred);
                }
            else
                {
                size = new Dimension(0, 0);
                }
            }

        if (target != null)
            {
            final Insets insets = target.getInsets();

            size.width = (int) Math.min((long) size.width + (long) insets.left + (long) insets.right,
                                        Integer.MAX_VALUE);
            size.height = (int) Math.min((long) size.height + (long) insets.top + (long) insets.bottom,
                                         Integer.MAX_VALUE);
            }

        return size;
        }


    /**
     * Returns the minimum dimensions needed to lay out the components
     * contained in the specified target container.
     *
     * @param target the container that needs to be laid out
     *
     * @return the dimensions >= 0 && <= Integer.MAX_VALUE
     *
     * @throws AWTError if the target isn't the container specified to the
     *                  BoxLayoutFixed constructor
     * @see #preferredLayoutSize
     * @see #maximumLayoutSize
     */
    public Dimension minimumLayoutSize(final Container target)
        {
        final Dimension size;
        synchronized (this)
            {
            checkContainer(target);
            checkRequests();

            if ((xTotal != null)
                && (yTotal != null))
                {
                size = new Dimension(xTotal.minimum,
                                     yTotal.minimum);
                }
            else
                {
                size = new Dimension(0, 0);
                }
            }

        if (target != null)
            {
            final Insets insets = target.getInsets();
            size.width = (int) Math.min((long) size.width + (long) insets.left + (long) insets.right,
                                        Integer.MAX_VALUE);
            size.height = (int) Math.min((long) size.height + (long) insets.top + (long) insets.bottom,
                                         Integer.MAX_VALUE);
            }

        return size;
        }


    /**
     * Returns the maximum dimensions the target container can use
     * to lay out the components it contains.
     *
     * @param target the container that needs to be laid out
     *
     * @return the dimenions >= 0 && <= Integer.MAX_VALUE
     *
     * @throws AWTError if the target isn't the container specified to the
     *                  BoxLayoutFixed constructor
     * @see #preferredLayoutSize
     * @see #minimumLayoutSize
     */
    public Dimension maximumLayoutSize(final Container target)
        {
        final Dimension size;
        synchronized (this)
            {
            checkContainer(target);
            checkRequests();

            if ((xTotal != null)
                && (yTotal != null))
                {
                size = new Dimension(xTotal.maximum,
                                     yTotal.maximum);
                }
            else
                {
                size = new Dimension(0, 0);
                }
            }

        if (target != null)
            {
            final Insets insets = target.getInsets();
            size.width = (int) Math.min((long) size.width + (long) insets.left + (long) insets.right,
                                        Integer.MAX_VALUE);
            size.height = (int) Math.min((long) size.height + (long) insets.top + (long) insets.bottom,
                                         Integer.MAX_VALUE);
            }

        return size;
        }


    /**
     * Returns the alignment along the X axis for the container.
     * If the box is horizontal, the default
     * alignment will be returned. Otherwise, the alignment needed
     * to place the children along the X axis will be returned.
     *
     * @param target the container
     *
     * @return the alignment >= 0.0f && <= 1.0f
     *
     * @throws AWTError if the target isn't the container specified to the
     *                  BoxLayoutFixed constructor
     */
    public synchronized float getLayoutAlignmentX(final Container target)
        {
        checkContainer(target);
        checkRequests();

        if (xTotal != null)
            {
            return xTotal.alignment;
            }
        else
            {
            return (0.0f);
            }
        }


    /**
     * Returns the alignment along the Y axis for the container.
     * If the box is vertical, the default
     * alignment will be returned. Otherwise, the alignment needed
     * to place the children along the Y axis will be returned.
     *
     * @param target the container
     *
     * @return the alignment >= 0.0f && <= 1.0f
     *
     * @throws AWTError if the target isn't the container specified to the
     *                  BoxLayoutFixed constructor
     */
    public synchronized float getLayoutAlignmentY(final Container target)
        {
        checkContainer(target);
        checkRequests();

        if (yTotal != null)
            {
            return yTotal.alignment;
            }
        else
            {
            return (0.0f);
            }
        }


    /***********************************************************************************************
     * Called by the AWT <!-- XXX CHECK! --> when the specified container
     * needs to be laid out.
     *
     * @param target the container to lay out
     *
     * @throws AWTError if the target isn't the container specified to the
     *                  BoxLayoutFixed constructor
     */

    public synchronized void layoutContainer(final Container target)
        {
        if (target != null)
            {
            final int MAX_RETRIES = 20;
            boolean boolSuccess;

            boolSuccess = false;

            for (int k = 0;
                 (!boolSuccess) && (k < MAX_RETRIES);
                 k++)
                {
                try
                    {
                    final int nChildren;
                    final int[] xOffsets;
                    final int[] xSpans;
                    final int[] yOffsets;
                    final int[] ySpans;
                    final Dimension dimSize;
                    final Insets insets;
                    final ComponentOrientation orientation;
                    final int absoluteAxis;
                    final boolean boolForward;
                    boolean boolPlaced;

                    checkContainer(target);

                    nChildren = target.getComponentCount();

                    xOffsets = new int[nChildren];
                    xSpans = new int[nChildren];
                    yOffsets = new int[nChildren];
                    ySpans = new int[nChildren];

                    dimSize = target.getSize();
                    insets = target.getInsets();

                    dimSize.width -= insets.left + insets.right;
                    dimSize.height -= insets.top + insets.bottom;

                    // Resolve axis to an absolute value (either X_AXIS or Y_AXIS)
                    orientation = target.getComponentOrientation();
                    absoluteAxis = resolveAxis(axis, orientation);

                    boolForward = (absoluteAxis != axis) ? orientation.isLeftToRight() : true;

                    boolPlaced = false;

                    // Determine the child placements
                    synchronized (this)
                        {
                        checkRequests();

                        if ((xTotal != null)
                            && (yTotal != null)
                            && (xChildren != null)
                            && (yChildren != null))
                            {
                            // The following MUST NOT be called with NULL totals or children!!
                            if (absoluteAxis == X_AXIS)
                                {
                                SizeRequirements.calculateTiledPositions(dimSize.width,
                                                                         xTotal,
                                                                         xChildren,
                                                                         xOffsets,
                                                                         xSpans,
                                                                         boolForward);
                                SizeRequirements.calculateAlignedPositions(dimSize.height,
                                                                           yTotal,
                                                                           yChildren,
                                                                           yOffsets,
                                                                           ySpans);
                                }
                            else
                                {
                                SizeRequirements.calculateAlignedPositions(dimSize.width,
                                                                           xTotal,
                                                                           xChildren,
                                                                           xOffsets,
                                                                           xSpans,
                                                                           boolForward);
                                SizeRequirements.calculateTiledPositions(dimSize.height,
                                                                         yTotal,
                                                                         yChildren,
                                                                         yOffsets,
                                                                         ySpans);
                                }

                            boolPlaced = true;
                            }
                        else
                            {
                            // Try again
                            //System.out.println("WARNING! BoxLayoutFixed.layoutContainer() Unexpected NULL Children, possible race hazard");
                            boolSuccess = false;
                            }
                        }

                    // Flush changes to the container, but only if there were any...
                    if (boolPlaced)
                        {
                        for (int i = 0;
                             i < nChildren;
                             i++)
                            {
                            final Component component;

                            component = target.getComponent(i);

                            if (component != null)
                                {
                                component.setBounds((int) Math.min((long) insets.left + (long) xOffsets[i],
                                                           Integer.MAX_VALUE),
                                            (int) Math.min((long) insets.top + (long) yOffsets[i],
                                                           Integer.MAX_VALUE),
                                            xSpans[i],
                                            ySpans[i]);

                                // If we get this far, it worked...
                                boolSuccess = true;
                                }
                            else
                                {
                                // Try again
                                boolSuccess = false;
                                }
                            }
                        }

                    // We don't mind if debug doesn't work, unless it throws NPE
                    if (dbg != null)
                        {
                        for (int i = 0;
                             i < nChildren;
                             i++)
                            {
                            final Component c = target.getComponent(i);

                            if (c != null)
                                {
                                dbg.println(c.toString());
                                dbg.println("X: " + xChildren[i]);
                                dbg.println("Y: " + yChildren[i]);
                                }
                            }
                        }
                    }

                catch (NullPointerException exception)
                    {
                    //System.out.println("----------------------------------------");
                    //System.out.println("WARNING! BoxLayoutFixed.layoutContainer() NPE Possible race hazard, retrying [retry=" + k + "]");
                    //exception.printStackTrace();
                    //System.out.println("----------------------------------------");

                    // Try again
                    boolSuccess = false;
                    }

                // Fail silently if there's no NPE
//                if (!boolSuccess)
//                    {
//                    System.out.println("WARNING! BoxLayoutFixed.layoutContainer() Retrying [retry=" + k + "]");
//                    }
                }
            }
        else
            {
            System.out.println("WARNING! BoxLayoutFixed.layoutContainer() NULL Target");
            }
        }


    /***********************************************************************************************
     *
     * @param target
     */

    private synchronized void checkContainer(final Container target)
        {
        if ((this.target != null)
            && (!this.target.equals(target)))
            {
            throw new AWTError("BoxLayoutFixed.checkContainer() Layout can't be shared");
            }
        }


    /***********************************************************************************************
     *
     */

    private synchronized void checkRequests()
        {
        if ((xChildren == null)
            || (yChildren == null))
            {
            // The requests have been invalidated... recalculate
            // the request information.
            final int n = target.getComponentCount();

            xChildren = new SizeRequirements[n];
            yChildren = new SizeRequirements[n];

            for (int i = 0;
                 i < n;
                 i++)
                {
                final Component c = target.getComponent(i);

                if ((c != null)
                    && (!c.isVisible()))
                    {
                    xChildren[i] = new SizeRequirements(0,
                                                        0,
                                                        0,
                                                        c.getAlignmentX());
                    yChildren[i] = new SizeRequirements(0,
                                                        0,
                                                        0,
                                                        c.getAlignmentY());
                    continue;
                    }

                if (c != null)
                    {
                    final Dimension min = c.getMinimumSize();
                    final Dimension typ = c.getPreferredSize();
                    final Dimension max = c.getMaximumSize();

                    xChildren[i] = new SizeRequirements(min.width,
                                                        typ.width,
                                                        max.width,
                                                        c.getAlignmentX());
                    yChildren[i] = new SizeRequirements(min.height,
                                                        typ.height,
                                                        max.height,
                                                        c.getAlignmentY());
                    }
                else
                    {
                    System.out.println("WARNING! BoxLayoutFixed.checkRequests() Unexpected NULL Component, possible race hazard");
                    }
                }

            // Resolve axis to an absolute value (either X_AXIS or Y_AXIS)
            final int absoluteAxis = resolveAxis(axis,
                                                 target.getComponentOrientation());

            if ((xChildren != null)
                && (yChildren != null))
                {
                if (absoluteAxis == X_AXIS)
                    {
                    xTotal = SizeRequirements.getTiledSizeRequirements(xChildren);
                    yTotal = SizeRequirements.getAlignedSizeRequirements(yChildren);
                    }
                else
                    {
                    xTotal = SizeRequirements.getAlignedSizeRequirements(xChildren);
                    yTotal = SizeRequirements.getTiledSizeRequirements(yChildren);
                    }
                }
            else
                {
                System.out.println("WARNING! BoxLayoutFixed.checkRequests() Unexpected NULL Children, possible race hazard");
                }
            }
        }
    }

