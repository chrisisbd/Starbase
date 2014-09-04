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

package org.lmn.fc.ui.panels;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2013 jOpenDocument, by ILM Informatique. All rights reserved.
 *
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").
 * You may not use this file except in compliance with the License.
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each file.
 *
 */

import org.jopendocument.model.OpenDocument;
import org.jopendocument.panel.Messages;
import org.jopendocument.print.DocumentPrinter;
import org.jopendocument.renderer.ODTRenderer;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.reports.ReportTableHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public class ODSViewerPanel extends JPanel
    {
    private static final long serialVersionUID = -6113257667157151508L;
    private static final int MODE_PAGE = 0;
    private static final int MODE_WIDTH = 1;
    private static final int MODE_ZOOM = 2;

    private final ODTRenderer renderer;
    private int mode;
    private int zoom = 100;

    private JToolBar toolBar;
    private JScrollPane scroll;
    private final JPanel viewer = new JPanel();
    private final JTextField textFieldZoomValue = new JTextField(5);
    private int currentPageIndex = 0;


    public ODSViewerPanel(final OpenDocument doc)
        {
        this(doc, null);
        }


    public ODSViewerPanel(final OpenDocument doc,
                          final boolean ignoreMargin)
        {
        this(doc, null, ignoreMargin);
        }


    public ODSViewerPanel(final OpenDocument doc,
                          final DocumentPrinter printListener)
        {
        this(doc, printListener, true);
        }


    public ODSViewerPanel(final OpenDocument doc,
                          final DocumentPrinter printListener,
                          final boolean ignoreMargin)
        {
        final JLabel labelName;

        Toolkit.getDefaultToolkit().setDynamicLayout(false);
        this.setOpaque(false);

        this.toolBar = null;
        renderer = new ODTRenderer(doc);
        renderer.setIgnoreMargins(ignoreMargin);
        updateMode(MODE_ZOOM, this.zoom);

        // Create the JToolBar and initialise it
        setToolBar(new JToolBar(JToolBar.HORIZONTAL));
        getToolBar().setFloatable(false);
        getToolBar().setMinimumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        getToolBar().setPreferredSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        getToolBar().setMaximumSize(UIComponentPlugin.DIM_TOOLBAR_SIZE);
        getToolBar().setBackground(UIComponentPlugin.DEFAULT_COLOUR_TAB_BACKGROUND.getColor());

        //-------------------------------------------------------------------------------------
        // Initialise the Label

        labelName = new JLabel("Publisher",
                               RegistryModelUtilities.getCommonIcon(UIComponentPlugin.FILENAME_ICON_PAGE_SETUP),
                               SwingConstants.LEFT)
            {
            private static final long serialVersionUID = 7580736117336162922L;

            // Enable Antialiasing in Java 1.5
            protected void paintComponent(final Graphics graphics)
                {
                final Graphics2D graphics2D = (Graphics2D) graphics;

                // For antialiasing text
                graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(graphics2D);
                }
            };

        labelName.setFont(UIComponentPlugin.DEFAULT_FONT.getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
        labelName.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());
//        labelName.setFont(fontdata.getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT).deriveFont(Font.BOLD));
//        labelName.setForeground(colourforeground.getColor());
        labelName.setIconTextGap(UIComponentPlugin.TOOLBAR_ICON_TEXT_GAP);

        getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR_BUTTON);
        getToolBar().add(labelName);
        getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        getToolBar().add(Box.createHorizontalGlue());

        final JButton buttonTailleReelle = new JButton(Messages.getString("ODSViewerPanel.normalSize"));

        buttonTailleReelle.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                updateMode(MODE_ZOOM, 100);
                }
            });

        getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
        getToolBar().add(buttonTailleReelle);
        getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        final JButton buttonFullPage = new JButton(Messages.getString("ODSViewerPanel.fitPage"));
        buttonFullPage.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent e)
            {
            if (mode != MODE_PAGE)
                {
                int width = (int) scroll.getViewportBorderBounds().getWidth();
                int height = (int) scroll.getViewportBorderBounds().getHeight();
                final double resizeW = renderer.getPageWidth() / width;
                final double resizeH = renderer.getPageHeight() / height;
                double resize = resizeH;
                if (resizeW > resizeH)
                    {
                    resize = resizeW;
                    }
                updateMode(MODE_PAGE, (int) ((100 * 360) / resize));
                }
            }
        });
        getToolBar().add(buttonFullPage);
        getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        final JButton buttonFullWidth = new JButton(Messages.getString("ODSViewerPanel.fitWidth"));
        buttonFullWidth.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent e)
            {
            int width = (int) (scroll.getViewportBorderBounds().getWidth());
            final double resizeW = renderer.getPageWidth() / width;
            updateMode(MODE_WIDTH, (int) ((100 * 360) / resizeW));
            }
        });
        getToolBar().add(buttonFullWidth);
        getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        final JButton buttonZoomOut = new JButton("-");
        buttonZoomOut.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (zoom > 30)
                    {
                    updateMode(mode, zoom - 20);
                    }
                }
            });
        getToolBar().add(buttonZoomOut);

        textFieldZoomValue.setEditable(false);
        textFieldZoomValue.setBackground(Color.white);

        textFieldZoomValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        textFieldZoomValue.setMinimumSize(ReportTableHelper.DIM_VIEW_LIMIT);
        textFieldZoomValue.setPreferredSize(ReportTableHelper.DIM_VIEW_LIMIT);
        textFieldZoomValue.setMaximumSize(ReportTableHelper.DIM_VIEW_LIMIT);
        textFieldZoomValue.setMargin(new Insets(0, 5, 0, 5));
        textFieldZoomValue.setFont(UIComponentPlugin.DEFAULT_FONT.getFont().deriveFont(ReportTableHelper.SIZE_HEADER_FONT));
        textFieldZoomValue.setForeground(UIComponentPlugin.DEFAULT_COLOUR_TEXT.getColor());

        getToolBar().add(textFieldZoomValue);

        final JButton buttonZoomIn = new JButton("+");
        buttonZoomIn.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                int z = zoom + 20;
                if (z > 400)
                    {
                    z = 400;
                    }
                updateMode(mode, z);
                }
            });
        getToolBar().add(buttonZoomIn);
        getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);

        // Viewer
        viewer.setOpaque(false);
        viewer.setBackground(UIComponentPlugin.DEFAULT_COLOUR_CANVAS.getColor());
        viewer.setLayout(null);
        renderer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        viewer.add(renderer);
        this.setLayout(new BorderLayout());

        this.add(getToolBar(), BorderLayout.NORTH);

        scroll = new JScrollPane(viewer);
        scroll.setOpaque(false);
        scroll.getHorizontalScrollBar().setUnitIncrement(30);
        scroll.getVerticalScrollBar().setUnitIncrement(30);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        ((JComponent) scroll.getViewport().getView()).setOpaque(false);
        this.add(scroll, BorderLayout.CENTER);
        updateMode(MODE_ZOOM, this.zoom);

        this.addComponentListener(new ComponentAdapter()
            {

            public void componentResized(ComponentEvent e)
                {
                updateMode(mode, zoom);

                }

            });
        if (doc.getPrintedPageCount() > 1)
            {
            final JTextField page = new JTextField(5);
            page.setHorizontalAlignment(JTextField.CENTER);
            JButton previousButton = new JButton("<");
            previousButton.addActionListener(new ActionListener()
                {

                public void actionPerformed(ActionEvent e)
                    {
                    if (currentPageIndex > 0)
                        {
                        currentPageIndex--;
                        updatePage(currentPageIndex);
                        updatePageCount(doc, page);
                        }

                    }

                });
            JButton nextButton = new JButton(">");
            nextButton.addActionListener(new ActionListener()
                {

                public void actionPerformed(ActionEvent e)
                    {
                    if (currentPageIndex < doc.getPrintedPageCount() - 1)
                        {
                        currentPageIndex++;
                        updatePage(currentPageIndex);
                        updatePageCount(doc, page);
                        }

                    }

                });
            getToolBar().add(previousButton);
            updatePageCount(doc, page);
            getToolBar().add(page);
            getToolBar().add(nextButton);
            getToolBar().addSeparator(UIComponentPlugin.DIM_TOOLBAR_SEPARATOR);
            }

        if (printListener != null)
            {

            final JButton buttonPrint = new JButton(Messages.getString("ODSViewerPanel.print"));
            buttonPrint.addActionListener(new ActionListener()
            {

            public void actionPerformed(ActionEvent e)
                {
                printListener.print(doc);

                }
            });
            getToolBar().add(buttonPrint);
            }

        }


    /**
     * @param doc
     * @param page
     */
    private void updatePageCount(final OpenDocument doc,
                                 final JTextField page)
        {
        page.setText((currentPageIndex + 1) + "/" + doc.getPrintedPageCount());
        }


    protected void updatePage(int i)
        {
        this.renderer.setCurrentPage(i);
        }


    private void updateMode(int m,
                            int zoom_value)
        {
        this.mode = m;
        this.zoom = zoom_value;
        this.textFieldZoomValue.setText(zoom + " %");

        renderer.setResizeFactor(((100 * 360) / zoom_value));

        int w = this.renderer.getPageWidthInPixel();

        int h = this.renderer.getPageHeightInPixel();

        int posx = 0;
        int posy = 0;
        if (scroll != null)
            {
            posx = (scroll.getViewportBorderBounds().width - w) / 2;
            posy = (scroll.getViewportBorderBounds().height - h) / 2;
            }
        if (posy > 10)
            {
            posy = 10;
            }

        if (posx < 0)
            {
            posx = 0;
            }
        if (posy < 0)
            {
            posy = 0;
            }
        renderer.setLocation(posx, posy);
        // final int renderedHeight = renderer.getPrintHeightInPixel();
        // renderer.setSize(renderer.getPrintWidthInPixel(), renderedHeight);
        final Dimension size = new Dimension(w, h);
        viewer.setPreferredSize(size);

        // Let the scroll pane know to update itself
        // and its scrollbars.
        viewer.revalidate();
        repaint();

        }

    /***********************************************************************************************
     * Get the ODS Viewer JToolBar.
     *
     * @return JToolBar
     */

    private JToolBar getToolBar()
        {
        return (this.toolBar);
        }


    /***********************************************************************************************
     * Set the ODS Viewer JToolBar.
     *
     * @param toolbar
     */

    private void setToolBar(final JToolBar toolbar)
        {
        this.toolBar = toolbar;
        }




    public ODTRenderer getRenderer()
        {
        return renderer;
        }
    }
