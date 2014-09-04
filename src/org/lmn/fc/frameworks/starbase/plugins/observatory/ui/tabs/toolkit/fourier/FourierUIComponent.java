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

// Fourier.java (C) 2001 by Paul Falstad, www.falstad.com

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.toolkit.fourier;


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryUIInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.InstrumentUIComponentDecorator;
import org.lmn.fc.model.datatypes.ColourInterface;
import org.lmn.fc.model.datatypes.FontInterface;
import org.lmn.fc.model.tasks.TaskPlugin;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.UIComponentPlugin;
import org.lmn.fc.ui.layout.BoxLayoutFixed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;


public final class FourierUIComponent
        extends InstrumentUIComponentDecorator
        implements UIComponentPlugin,
                   AdjustmentListener,
                   ActionListener,
                   ItemListener,
                   ComponentListener,
                   MouseListener,
                   MouseMotionListener
    {
    private static final int sampleCount = 1024;
    private static final int halfSampleCount = sampleCount >> 1;
    private static final double halfSampleCountFloat = sampleCount >> 1;
    private static final Dimension DIM_BUTTON = new Dimension(200,
                                                              25);
    private static final int SEL_NONE = 0;
    private static final int SEL_FUNC = 1;
    private static final int SEL_MAG = 2;
    private static final int SEL_PHASE = 3;
    private static final int SEL_MUTES = 4;
    private static final int SEL_SOLOS = 5;
    private final int rate = 22050;
    private final int playSampleCount = 16384;
    private static final double pi = 3.14159265358979323846;
    private static final double step = 2 * pi / sampleCount;


    private Dimension dimFFT;
    private Random random;

    private JPanel panelFFT;
    private JPanel panelControls;

    private NumberFormat showFormat;
    private JButton sineButton;
    private JButton cosineButton;
    private JButton rectButton;
    private JButton fullRectButton;
    private JButton triangleButton;
    private JButton sawtoothButton;
    private JButton squareButton;
    private JButton noiseButton;
    private JButton blankButton;
    private JButton phaseButton;
    private JButton clipButton;
    private JButton resampleButton;
    private JButton quantizeButton;
    private JButton highPassButton;
    private JCheckBox magPhaseCheck;
    JCheckBox soundCheck;
    private JCheckBox logCheck;
    private JScrollBar termBar;
    private JScrollBar freqBar;

    private double[] arrayMagnitudeCoefficients;
    private double[] phasecoef;
    private boolean[] mutes;
    private boolean[] solos;

    private boolean hasSolo;
    private double[] func;

    private int maxTerms = 160;
    private int selectedCoef;
    private int selection;
    private int dragX;
    private int dragY;
    private int quantizeCount;
    private int resampleCount;
    private boolean dragging;
    private boolean freqAdjusted;

    private View viewFunc;
    private View viewMag;
    private View viewPhase;
    private View viewMutes;
    private View viewSolos;

    private FFT fft;


    /**
     * ********************************************************************************************
     *
     * @param resourcekey
     */

    public FourierUIComponent(final ObservatoryInstrumentInterface hostinstrument,
                              final Instrument instrumentxml,
                              final ObservatoryUIInterface hostui,
                              final TaskPlugin task,
                              final FontInterface font,
                              final ColourInterface colour,
                              final String resourcekey)
        {
        super(hostinstrument,
              instrumentxml,
              hostui,
              task,
              font,
              colour,
              resourcekey, 1);
        }


    /**
     * ********************************************************************************************
     * Initialise this UIComponent.
     */

    public void initialiseUI()
        {
        // DO NOT USE super.initialiseUI()

        // Get the latest Resources
        //readResources();

        // Remove everything from the host UIComponent
        removeAll();
        }


    /**
     * ********************************************************************************************
     * Run this UIComponent.
     */

    public void runUI()
        {
        try
            {
            // The host UIComponent uses BorderLayout
            setLayout(new BoxLayoutFixed(this, BoxLayoutFixed.X_AXIS));

            setBackground(Color.black);
            setForeground(Color.lightGray);

            panelFFT = new JPanel()
                {
                public void paintComponent(final Graphics graphics)
                    {
                    dimFFT = panelFFT.getSize();

                    super.paintComponent(graphics);

                    System.out.println("PAINT FFT COMPONENT");
                    paintFFT(graphics);
                    }
                };

            panelFFT.setBackground(Color.black);
            panelFFT.setBorder(BorderFactory.createLineBorder(Color.yellow, 3));
            add(panelFFT);

            panelControls = new JPanel();
            panelControls.setLayout(new BoxLayoutFixed(panelControls, BoxLayoutFixed.Y_AXIS));
            panelControls.setBorder(BorderFactory.createLineBorder(Color.red,
                                                               3));
            panelControls.setMinimumSize(new Dimension(DIM_BUTTON.width,
                                                       Integer.MAX_VALUE));
            panelControls.setPreferredSize(new Dimension(DIM_BUTTON.width,
                                                         Integer.MAX_VALUE));
            panelControls.setMaximumSize(new Dimension(DIM_BUTTON.width,
                                                       Integer.MAX_VALUE));
            add(panelControls);

            selectedCoef = -1;
            arrayMagnitudeCoefficients = new double[maxTerms];
            phasecoef = new double[maxTerms];
            mutes = new boolean[maxTerms];
            solos = new boolean[maxTerms];
            func = new double[sampleCount + 1];
            random = new Random();
            fft = new FFT(sampleCount);

            panelFFT.addComponentListener(this);
            panelFFT.addMouseMotionListener(this);
            panelFFT.addMouseListener(this);

            sineButton = createButton("Sine");
            cosineButton = createButton("Cosine");
            triangleButton = createButton("Triangle");
            sawtoothButton = createButton("Sawtooth");
            squareButton = createButton("Square");
            noiseButton = createButton("Noise");
            phaseButton = createButton("Phase Shift");
            clipButton = createButton("Clip");
            resampleButton = createButton("Resample");
            quantizeButton = createButton("Quantize");
            rectButton = createButton("Rectify");
            fullRectButton = createButton("Full Rectify");
            highPassButton = createButton("High-Pass Filter");
            blankButton = createButton("Clear");

            magPhaseCheck = createCheckbox("Mag/Phase View");
            logCheck = createCheckbox("Log View");
            logCheck.disable();

            panelControls.add(new Label("Number of Terms",
                                        Label.CENTER));
            termBar = new JScrollBar(JScrollBar.HORIZONTAL,
                                     50,
                                     1,
                                     1,
                                     maxTerms);
            panelControls.add(termBar);
            termBar.addAdjustmentListener(this);

            panelControls.add(new Label("Playing Frequency",
                                        Label.CENTER));
            freqBar = new JScrollBar(Scrollbar.HORIZONTAL,
                                     251,
                                     1,
                                     -100,
                                     500);
            freqBar.addAdjustmentListener(this);

            panelControls.add(freqBar);

            panelControls.add(new Label("http://www.falstad.com"));
            panelControls.setBackground(Color.LIGHT_GRAY);
            panelControls.setForeground(Color.BLUE);

            // ToDo Consider ThreadLocal
            showFormat = DecimalFormat.getInstance();
            showFormat.setMaximumFractionDigits(5);

            doSine();
            panelFFT.repaint();
            }
        catch (Exception e)
            {
            e.printStackTrace();
            }
        }


    /***********************************************************************************************
     *
     * @param graphics
     */

    void paintFFT(final Graphics graphics)
        {

        if (dimFFT == null || dimFFT.width == 0)
            {
            System.out.println("nothing to paint!");
            return;
            }

        graphics.setColor(Color.black);
        graphics.fillRect(0,
                          0,
                          dimFFT.width,
                          dimFFT.height);

        final Color gray1 = new Color(76,
                                      76,
                                      76);
        final Color gray2 = new Color(127,
                                      127,
                                      127);


        graphics.setColor(getForeground());
        int i;
        int ox = -1, oy = -1;
        int midy = viewFunc.midy;
        final int periodWidth = viewFunc.periodWidth;
        double ymult = viewFunc.ymult;
        for (i = -1;
             i <= 1;
             i++)
            {
            graphics.setColor((i == 0) ? gray2 : gray1);
            graphics.drawLine(0,
                              midy + (i * (int) ymult),
                              dimFFT.width,
                              midy + (i * (int) ymult));
            }
        for (i = 2;
             i <= 4;
             i++)
            {
            graphics.setColor((i == 3) ? gray2 : gray1);
            graphics.drawLine(periodWidth * i >> 1,
                              midy - (int) ymult,
                              periodWidth * i >> 1,
                              midy + (int) ymult);
            }
        graphics.setColor(Color.white);
        if (!(dragging && selection != SEL_FUNC))
            {
            for (i = 0;
                 i != sampleCount + 1;
                 i++)
                {
                final int x = periodWidth * i / sampleCount;
                final int y = midy - (int) (ymult * func[i]);
                if (ox != -1)
                    {
                    graphics.drawLine(ox,
                                      oy,
                                      x,
                                      y);
                    graphics.drawLine(ox + periodWidth,
                                      oy,
                                      x + periodWidth,
                                      y);
                    graphics.drawLine(ox + (periodWidth << 1),
                                      oy,
                                      x + (periodWidth << 1),
                                      y);
                    }
                ox = x;
                oy = y;
                }
            }

        final int terms = termBar.getValue();

        if (!(dragging && selection == SEL_FUNC))
            {
            graphics.setColor(Color.red);
            ox = -1;
            for (i = 0;
                 i != sampleCount + 1;
                 i++)
                {
                final int x = periodWidth * i / sampleCount;
                int j;
                double dy = 0;
                for (j = 0;
                     j != terms;
                     j++)
                    {
                    dy += arrayMagnitudeCoefficients[j] * Math.cos(
                            step * (i - halfSampleCount) * j + phasecoef[j]);
                    }
                final int y = midy - (int) (ymult * dy);
                if (ox != -1)
                    {
                    graphics.drawLine(ox,
                                      oy,
                                      x,
                                      y);
                    graphics.drawLine(ox + periodWidth,
                                      oy,
                                      x + periodWidth,
                                      y);
                    graphics.drawLine(ox + (periodWidth << 1),
                                      oy,
                                      x + (periodWidth << 1),
                                      y);
                    }
                ox = x;
                oy = y;
                }
            }
        final int texty = viewFunc.height + 10;
        if (selectedCoef != -1)
            {
            graphics.setColor(Color.yellow);
            ox = -1;
            double phase = phasecoef[selectedCoef];
            int x;
            final double n = (selectedCoef << 1) * pi / periodWidth;
            final int dx = periodWidth >> 1;
            double mag = arrayMagnitudeCoefficients[selectedCoef];
            if (!magPhaseCheck.isEnabled())
                {
                if (selection == SEL_MAG)
                    {
                    mag *= -Math.sin(phase);
                    phase = -pi / 2;
                    }
                else
                    {
                    mag *= Math.cos(phase);
                    phase = 0;
                    }
                }
            ymult *= mag;
            if (!dragging)
                {
                for (i = 0;
                     i != sampleCount + 1;
                     i++)
                    {
                    x = periodWidth * i / sampleCount;
                    final double dy = Math.cos(
                            step * (i - halfSampleCount) * selectedCoef + phase);
                    final int y = midy - (int) (ymult * dy);
                    if (ox != -1)
                        {
                        graphics.drawLine(ox,
                                          oy,
                                          x,
                                          y);
                        graphics.drawLine(ox + periodWidth,
                                          oy,
                                          x + periodWidth,
                                          y);
                        graphics.drawLine(ox + (periodWidth << 1),
                                          oy,
                                          x + (periodWidth << 1),
                                          y);
                        }
                    ox = x;
                    oy = y;
                    }
                }
            if (selectedCoef > 0)
                {
                final int f = (int) (getFreq() * selectedCoef);
                centerString(graphics,
                             f +
                                     ((f > rate >> 1) ? " Hz (filtered)" : " Hz"),
                             texty);
                }
            if (selectedCoef != -1)
                {
                String harm;
                if (selectedCoef == 0)
                    {
                    harm = showFormat.format(mag) + "";
                    }
                else
                    {
                    String function = "cos";
                    if (!magPhaseCheck.isEnabled() && selection == SEL_MAG)
                        {
                        function = "sin";
                        }
                    if (selectedCoef == 1)
                        {
                        harm = showFormat.format(mag) + " " + function + "(x";
                        }
                    else
                        {
                        harm = showFormat.format(mag) +
                                " " + function + "(" + selectedCoef + "x";
                        }
                    if (!magPhaseCheck.isEnabled() || phase == 0)
                        {
                        harm += ")";
                        }
                    else
                        {
                        harm += (phase < 0) ? " - " : " + ";
                        harm += showFormat.format(Math.abs(phase)) + ")";
                        }
                    if (logCheck.isEnabled())
                        {
                        showFormat.setMaximumFractionDigits(2);
                        harm += "   (" +
                                showFormat.format(20 * Math.log(mag) / Math.log(10)) +
                                " dB)";
                        showFormat.setMaximumFractionDigits(5);
                        }
                    }
                centerString(graphics,
                             harm,
                             texty + 15);
                }
            }
        if (selectedCoef == -1 && freqAdjusted)
            {
            final int f = (int) getFreq();
            graphics.setColor(Color.yellow);
            centerString(graphics,
                         f + " Hz",
                         texty);
            }
        freqAdjusted = false;
        final int termWidth = getTermWidth();

        ymult = viewMag.ymult;
        midy = viewMag.midy;
        graphics.setColor(Color.white);
        if (magPhaseCheck.isEnabled())
            {
            centerString(graphics,
                         "Magnitudes",
                         viewMag.labely);
            centerString(graphics,
                         "Phases",
                         viewPhase.labely);
            graphics.setColor(gray2);
            graphics.drawLine(0,
                              midy,
                              dimFFT.width,
                              midy);
            graphics.setColor(gray1);
            graphics.drawLine(0,
                              midy - (int) ymult,
                              dimFFT.width,
                              midy - (int) ymult);
            final int dotSize = termWidth - 3;
            for (i = 0;
                 i != terms;
                 i++)
                {
                final int t = termWidth * i + (termWidth >> 1);
                final int y = midy - (int) (showMag(i) * ymult);
                graphics.setColor(i == selectedCoef ? Color.yellow : Color.white);
                graphics.drawLine(t,
                                  midy,
                                  t,
                                  y);
                graphics.fillOval(t - (dotSize >> 1),
                                  y - (dotSize >> 1),
                                  dotSize,
                                  dotSize);
                }

            ymult = viewPhase.ymult;
            midy = viewPhase.midy;
            for (i = -2;
                 i <= 2;
                 i++)
                {
                graphics.setColor((i == 0) ? gray2 : gray1);
                graphics.drawLine(0,
                                  midy + ((i * (int) ymult) >> 1),
                                  dimFFT.width,
                                  midy + ((i * (int) ymult) >> 1));
                }
            ymult /= pi;
            for (i = 0;
                 i != terms;
                 i++)
                {
                final int t = termWidth * i + (termWidth >> 1);
                final int y = midy - (int) (phasecoef[i] * ymult);
                graphics.setColor(i == selectedCoef ? Color.yellow : Color.white);
                graphics.drawLine(t,
                                  midy,
                                  t,
                                  y);
                graphics.fillOval(t - (dotSize >> 1),
                                  y - (dotSize >> 1),
                                  dotSize,
                                  dotSize);
                }
            }
        else
            {
            centerString(graphics,
                         "Sines",
                         viewMag.labely);
            centerString(graphics,
                         "Cosines",
                         viewPhase.labely);
            graphics.setColor(gray2);
            graphics.drawLine(0,
                              midy,
                              dimFFT.width,
                              midy);
            graphics.setColor(gray1);
            graphics.drawLine(0,
                              midy - (int) ymult,
                              dimFFT.width,
                              midy - (int) ymult);
            graphics.drawLine(0,
                              midy + (int) ymult,
                              dimFFT.width,
                              midy + (int) ymult);
            final int dotSize = termWidth - 3;
            for (i = 1;
                 i != terms;
                 i++)
                {
                final int t = termWidth * i + (termWidth >> 1);
                final int y = midy + (int) (arrayMagnitudeCoefficients[i] * Math.sin(phasecoef[i]) * ymult);
                graphics.setColor(i == selectedCoef ? Color.yellow : Color.white);
                graphics.drawLine(t,
                                  midy,
                                  t,
                                  y);
                graphics.fillOval(t - (dotSize >> 1),
                                  y - (dotSize >> 1),
                                  dotSize,
                                  dotSize);
                }

            ymult = viewPhase.ymult;
            midy = viewPhase.midy;
            for (i = -2;
                 i <= 2;
                 i += 2)
                {
                graphics.setColor((i == 0) ? gray2 : gray1);
                graphics.drawLine(0,
                                  midy + ((i * (int) ymult) >> 1),
                                  dimFFT.width,
                                  midy + ((i * (int) ymult) >> 1));
                }
            for (i = 0;
                 i != terms;
                 i++)
                {
                final int t = termWidth * i + (termWidth >> 1);
                final int y = midy - (int) (arrayMagnitudeCoefficients[i] * Math.cos(phasecoef[i]) * ymult);
                graphics.setColor(i == selectedCoef ? Color.yellow : Color.white);
                graphics.drawLine(t,
                                  midy,
                                  t,
                                  y);
                graphics.fillOval(t - (dotSize >> 1),
                                  y - (dotSize >> 1),
                                  dotSize,
                                  dotSize);
                }
            }
        final double basef = getFreq();
        if (viewMutes.height > 8)
            {
            final Font f = new Font("SansSerif",
                                    0,
                                    viewMutes.height);
            graphics.setFont(f);
            final FontMetrics fm = graphics.getFontMetrics();
            for (i = 1;
                 i != terms;
                 i++)
                {
                if (basef * i > rate >> 1)
                    {
                    break;
                    }
                final int t = termWidth * i + (termWidth >> 1);
                int y = viewMutes.y + fm.getAscent();
                graphics.setColor(i == selectedCoef ? Color.yellow : Color.white);
                if (hasSolo && !solos[i])
                    {
                    graphics.setColor(Color.gray);
                    }
                String pm = "-";
                if (mutes[i])
                    {
                    pm = "M";
                    }
                int w = fm.stringWidth(pm);
                graphics.drawString(pm,
                                    t - (w >> 1),
                                    y);
                y = viewSolos.y + fm.getAscent();
                pm = "-";
                if (solos[i])
                    {
                    pm = "S";
                    }
                w = fm.stringWidth(pm);
                graphics.drawString(pm,
                                    t - (w >> 1),
                                    y);
                }
            }
        System.out.println("end of paint");
        }


    void handleResize()
        {
        final Dimension d;

        //dimFFT = panelFFT.getSize();

        d = dimFFT;

        if (dimFFT.width == 0)
            {
            return;
            }
        final int margin = 20;
        final int pheight = (d.height - (margin << 1)) / 3;
        viewFunc = new View(0,
                            0,
                            d.width,
                            pheight);
        int y = pheight + (margin << 1);
        viewMag = new View(0,
                           y,
                           d.width,
                           pheight);
        if (magPhaseCheck.isEnabled())
            {
            viewMag.ymult *= 1.6;
            viewMag.midy += (int) viewMag.ymult >> 1;
            logCheck.enable();
            }
        else
            {
            logCheck.disable();
            logCheck.setEnabled(false);
            }
        y += pheight;
        viewPhase = new View(0,
                             y,
                             d.width,
                             pheight);
        final int pmy = viewPhase.midy + (int) viewPhase.ymult + 10;
        final int h = (d.height - pmy) >> 1;
        //System.out.println("height " + h);
        viewMutes = new View(0,
                             pmy,
                             d.width,
                             h);
        viewSolos = new View(0,
                             pmy + h,
                             d.width,
                             h);
        //System.out.println("create image width=" + d.width + " height = " + d.height);
        }


    void transform()
        {
        int x, y;
        final double[] data = new double[(sampleCount << 1)];
        int i;
        for (i = 0;
             i != sampleCount;
             i++)
            {
            data[(i << 1)] = func[i];
            }
        fft.transform(data,
                      false);
        final double epsilon = 0.00001;
        final double mult = 2.0 / sampleCount;
        for (y = 0;
             y != maxTerms;
             y++)
            {
            double acoef = data[(y << 1)] * mult;
            double bcoef = -data[(y << 1) + 1] * mult;
            if ((y & 1) == 1)
                {
                acoef = -acoef;
                }
            else
                {
                bcoef = -bcoef;
                }
            //System.out.println(y + " " + acoef + " " + bcoef);
            if (acoef < epsilon && acoef > -epsilon)
                {
                acoef = 0;
                }
            if (bcoef < epsilon && bcoef > -epsilon)
                {
                bcoef = 0;
                }
            if (y == 0)
                {
                arrayMagnitudeCoefficients[0] = acoef / 2;
                phasecoef[0] = 0;
                }
            else
                {
                arrayMagnitudeCoefficients[y] = Math.sqrt(acoef * acoef + bcoef * bcoef);
                phasecoef[y] = Math.atan2(-bcoef,
                                          acoef);
                }
            // System.out.print("phasecoef " + phasecoef[y] + "\n");
            }
        }

    //------------------------------------------------------------------------------------------------------------------


    JButton createButton(final String name)
        {
        final JButton button;

        button = new JButton(name);
        button.setMinimumSize(DIM_BUTTON);
        button.setPreferredSize(DIM_BUTTON);
        button.setMaximumSize(DIM_BUTTON);
        button.setForeground(DEFAULT_COLOUR_TEXT.getColor());
        button.setFont(DEFAULT_FONT.getFont());

        panelControls.add(button);
        button.addActionListener(this);

        return button;
        }


    JCheckBox createCheckbox(final String s)
        {
        final JCheckBox b;

        b = new JCheckBox(s);
        panelControls.add(b);

        try
            {
            final String param = "true"; //applet.getParameter(s);
            if (param != null && param.equalsIgnoreCase("true"))
                {
                b.setEnabled(true);
                }
            }
        catch (Exception e)
            {
            e.printStackTrace();
            }
        b.addItemListener(this);
        return b;
        }


    void doBeats()
        {
        int x;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            final double q = (x - halfSampleCount) * step;
            func[x] = 0.5 * (Math.cos(q * 20) + Math.cos(q * 21));
            }
        func[sampleCount] = func[0];
        transform();
        freqBar.setValue(-100);
        }


    void doLoudSoft()
        {
        int x;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            final double q = (x - halfSampleCount) * step;
            func[x] = Math.cos(q) + 0.05 * Math.cos(q * 10);
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doSawtooth()
        {
        int x;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            func[x] = (x - (sampleCount >> 1)) / halfSampleCountFloat;
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doTriangle()
        {
        int x;
        for (x = 0;
             x != halfSampleCount;
             x++)
            {
            func[x] = ((x << 1) - halfSampleCount) / halfSampleCountFloat;
            func[x + halfSampleCount] =
                    (((halfSampleCount - x) << 1) - halfSampleCount) / halfSampleCountFloat;
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doSine()
        {
        int x;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            func[x] = Math.sin((x - halfSampleCount) * step);
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doCosine()
        {
        int x;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            func[x] = Math.cos((x - halfSampleCount) * step);
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doRect()
        {
        int x;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            if (func[x] < 0)
                {
                func[x] = 0;
                }
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doFullRect()
        {
        int x;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            if (func[x] < 0)
                {
                func[x] = -func[x];
                }
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doHighPass()
        {
        int i;
        final int terms = termBar.getValue();
        for (i = 0;
             i != terms;
             i++)
            {
            if (arrayMagnitudeCoefficients[i] != 0)
                {
                arrayMagnitudeCoefficients[i] = 0;
                break;
                }
            }
        doSetFunc();
        }


    void doSquare()
        {
        int x;
        for (x = 0;
             x != halfSampleCount;
             x++)
            {
            func[x] = -1;
            func[x + halfSampleCount] = 1;
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doNoise()
        {
        int x;
        final int blockSize = 3;
        for (x = 0;
             x != sampleCount / blockSize;
             x++)
            {
            final double q = Math.random() * 2 - 1;
            int i;
            for (i = 0;
                 i != blockSize;
                 i++)
                {
                func[x * blockSize + i] = q;
                }
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doPhaseShift()
        {
        int i;
        final int sh = sampleCount / 20;
        final double[] copyf = new double[sh];
        for (i = 0;
             i != sh;
             i++)
            {
            copyf[i] = func[i];
            }
        for (i = 0;
             i != sampleCount - sh;
             i++)
            {
            func[i] = func[i + sh];
            }
        for (i = 0;
             i != sh;
             i++)
            {
            func[sampleCount - sh + i] = copyf[i];
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doBlank()
        {
        int x;
        for (x = 0;
             x <= sampleCount;
             x++)
            {
            func[x] = 0;
            }
        for (x = 0;
             x != termBar.getValue();
             x++)
            {
            mutes[x] = solos[x] = false;
            }
        transform();
        }


    void doSetFunc()
        {
        int i;
        final double[] data = new double[(sampleCount << 1)];
        final int terms = termBar.getValue();
        for (i = 0;
             i != terms;
             i++)
            {
            final int sgn = (i & 1) == 1 ? -1 : 1;
            data[(i << 1)] = sgn * arrayMagnitudeCoefficients[i] * Math.cos(phasecoef[i]);
            data[(i << 1) + 1] = -sgn * arrayMagnitudeCoefficients[i] * Math.sin(phasecoef[i]);
            }
        fft.transform(data,
                      true);
        for (i = 0;
             i != sampleCount;
             i++)
            {
            func[i] = data[(i << 1)];
            }
        func[sampleCount] = func[0];
        }


    void doClip()
        {
        int x;
        final double mult = 1.2;
        for (x = 0;
             x != sampleCount;
             x++)
            {
            func[x] *= mult;
            if (func[x] > 1)
                {
                func[x] = 1;
                }
            if (func[x] < -1)
                {
                func[x] = -1;
                }
            }
        func[sampleCount] = func[0];
        transform();
        }


    void doResample()
        {
        int x, i;
        if (resampleCount == 0)
            {
            resampleCount = 32;
            }
        if (resampleCount == sampleCount)
            {
            return;
            }
        for (x = 0;
             x != sampleCount;
             x += resampleCount)
            {
            for (i = 1;
                 i != resampleCount;
                 i++)
                {
                func[x + i] = func[x];
                }
            }
        func[sampleCount] = func[0];
        transform();
        resampleCount <<= 1;
        }


    private double[] origFunc;


    void doQuantize()
        {
        int x;
        if (quantizeCount == 0)
            {
            quantizeCount = 8;
            origFunc = new double[sampleCount];
            System.arraycopy(func,
                             0,
                             origFunc,
                             0,
                             sampleCount);
            }
        for (x = 0;
             x != sampleCount;
             x++)
            {
            func[x] = Math.round(origFunc[x] * quantizeCount) /
                    (double) quantizeCount;
            }
        func[sampleCount] = func[0];
        transform();
        quantizeCount >>= 1;
        }


    private int dfreq0;


    void edit(final MouseEvent e)
        {
        if (selection == SEL_NONE)
            {
            return;
            }
        final int x = e.getX();
        final int y = e.getY();
        switch (selection)
            {
            case SEL_MAG:
                editMag(x,
                        y);
                break;
            case SEL_FUNC:
                editFunc(x,
                         y);
                break;
            case SEL_PHASE:
                editPhase(x,
                          y);
                break;
            case SEL_MUTES:
                editMutes(e,
                          x,
                          y);
                break;
            case SEL_SOLOS:
                editSolos(e,
                          x,
                          y);
                break;
            }
        quantizeCount = resampleCount = 0;
        }


    void editMag(final int x,
                 final int y)
        {
        if (selectedCoef == -1)
            {
            return;
            }
        final double ymult = viewMag.ymult;
        final double midy = viewMag.midy;
        double coef = -(y - midy) / ymult;
        if (magPhaseCheck.isEnabled())
            {
            if (selectedCoef > 0)
                {
                if (coef < 0)
                    {
                    coef = 0;
                    }
                coef = getMagValue(coef);
                }
            else if (coef < -1)
                {
                coef = -1;
                }
            if (coef > 1)
                {
                coef = 1;
                }
            if (arrayMagnitudeCoefficients[selectedCoef] == coef)
                {
                return;
                }
            arrayMagnitudeCoefficients[selectedCoef] = coef;
            }
        else
            {
            final int c = selectedCoef;
            if (c == 0)
                {
                return;
                }
            final double m2 = arrayMagnitudeCoefficients[c] * Math.cos(phasecoef[c]);
            if (coef > 1)
                {
                coef = 1;
                }
            if (coef < -1)
                {
                coef = -1;
                }
            final double m1 = coef;
            arrayMagnitudeCoefficients[c] = Math.sqrt(m1 * m1 + m2 * m2);
            phasecoef[c] = Math.atan2(-m1,
                                      m2);
            }
        panelFFT.repaint();
        }


    void editFunc(final int x,
                  int y)
        {
        int x3 = x;
        if (dragX == x3)
            {
            editFuncPoint(x3,
                          y);
            dragY = y;
            }
        else
            {
            // need to draw a line from old x,y to new x,y and
            // call editFuncPoint for each point on that line.  yuck.
            final int x1 = (x3 < dragX) ? x3 : dragX;
            final int y1 = (x3 < dragX) ? y : dragY;
            final int x2 = (x3 > dragX) ? x3 : dragX;
            final int y2 = (x3 > dragX) ? y : dragY;
            dragX = x3;
            dragY = y;
            for (x3 = x1;
                 x3 <= x2;
                 x3++)
                {
                y = y1 + (y2 - y1) * (x3 - x1) / (x2 - x1);
                editFuncPoint(x3,
                              y);
                }
            }
        }


    void editFuncPoint(final int x,
                       final int y)
        {
        final int midy = viewFunc.midy;
        final int periodWidth = viewFunc.periodWidth;
        final double ymult = viewFunc.ymult;
        int lox = (x % periodWidth) * sampleCount / periodWidth;
        final int hix = (((x % periodWidth) + 1) * sampleCount / periodWidth) - 1;
        double val = (midy - y) / ymult;
        if (val > 1)
            {
            val = 1;
            }
        if (val < -1)
            {
            val = -1;
            }
        for (;
             lox <= hix;
             lox++)
            {
            func[lox] = val;
            }
        func[sampleCount] = func[0];
        panelFFT.repaint();
        }


    void editPhase(final int x,
                   final int y)
        {
        if (selectedCoef == -1)
            {
            return;
            }
        final double ymult = viewPhase.ymult;
        final double midy = viewPhase.midy;
        double coef = -(y - midy) / ymult;
        if (magPhaseCheck.isEnabled())
            {
            coef *= pi;
            if (coef < -pi)
                {
                coef = -pi;
                }
            if (coef > pi)
                {
                coef = pi;
                }
            if (phasecoef[selectedCoef] == coef)
                {
                return;
                }
            phasecoef[selectedCoef] = coef;
            }
        else
            {
            final int c = selectedCoef;
            final double m1 = -arrayMagnitudeCoefficients[c] * Math.sin(phasecoef[c]);
            if (coef > 1)
                {
                coef = 1;
                }
            if (coef < -1)
                {
                coef = -1;
                }
            final double m2 = coef;
            arrayMagnitudeCoefficients[c] = Math.sqrt(m1 * m1 + m2 * m2);
            phasecoef[c] = Math.atan2(-m1,
                                      m2);
            }
        panelFFT.repaint();
        }


    void editMutes(final MouseEvent e,
                   final int x,
                   final int y)
        {
        if (e.getID() != MouseEvent.MOUSE_PRESSED)
            {
            return;
            }
        if (selectedCoef == -1)
            {
            return;
            }
        mutes[selectedCoef] = !mutes[selectedCoef];
        panelFFT.repaint();
        }


    void editSolos(final MouseEvent e,
                   final int x,
                   final int y)
        {
        if (e.getID() != MouseEvent.MOUSE_PRESSED)
            {
            return;
            }
        if (selectedCoef == -1)
            {
            return;
            }
        solos[selectedCoef] = !solos[selectedCoef];
        final int terms = termBar.getValue();
        hasSolo = false;
        int i;
        for (i = 0;
             i != terms;
             i++)
            {
            if (solos[i])
                {
                hasSolo = true;
                break;
                }
            }
        panelFFT.repaint();
        }


    double getMagValue(final double m)
        {
        if (!logCheck.isEnabled())
            {
            return m;
            }
        if (m == 0)
            {
            return 0;
            }
        return Math.exp(6 * (m - 1));
        }


    double showMag(final int n)
        {
        double m = arrayMagnitudeCoefficients[n];
        if (!logCheck.isEnabled() || n == 0)
            {
            return m;
            }
        m = Math.log(m) / 6.0 + 1;
        //System.out.println(arrayMagnitudeCoefficients[i] + " " + m);
        return (m < 0) ? 0 : m;
        }


    double getFreq()
        {
        // get approximate freq from slider (log scale)
        final double freq = 27.5 * Math.exp(freqBar.getValue() * 0.004158883084 * 2);
        // get offset into FFT array for frequency selected (as close as possible;
        // it can't be exact because we use an FFT to generate the wave, and so the
        // frequency choices must be integer multiples of a base frequency)
        dfreq0 = ((int) (freq * (double) playSampleCount / rate)) << 1;
        // get exact frequency being played
        return rate * dfreq0 / (playSampleCount * 2.0);
        }


    int getTermWidth()
        {
        final int terms = termBar.getValue();
        int termWidth = dimFFT.width / terms;
        final int maxTermWidth = dimFFT.width / 30;
        if (termWidth > maxTermWidth)
            {
            termWidth = maxTermWidth;
            }
        if (termWidth > 12)
            {
            termWidth = 12;
            }
        termWidth &= ~1;
        return termWidth;
        }


    void centerString(final Graphics g,
                      final String s,
                      final int y)
        {
        final FontMetrics fm = g.getFontMetrics();
        g.drawString(s,
                     (dimFFT.width - fm.stringWidth(s)) >> 1,
                     y);
        }


    public void componentHidden(final ComponentEvent e)
        {
        }


    public void componentMoved(final ComponentEvent e)
        {
        }


    public void componentShown(final ComponentEvent e)
        {
        panelFFT.repaint();
        }


    public void componentResized(final ComponentEvent e)
        {
        handleResize();
        panelFFT.repaint(100);
        }


    public void actionPerformed(final ActionEvent e)
        {
        pressButton(e.getSource());
        }


    void pressButton(final Object b)
        {
        //System.out.println("pressing button");
        if (b.equals(triangleButton))
            {
            doTriangle();
            panelFFT.repaint();
            }
        if (b.equals(sineButton))
            {
            doSine();
            panelFFT.repaint();
            }
        if (b.equals(cosineButton))
            {
            doCosine();
            panelFFT.repaint();
            }
        if (b.equals(rectButton))
            {
            doRect();
            panelFFT.repaint();
            }
        if (b.equals(fullRectButton))
            {
            doFullRect();
            panelFFT.repaint();
            }
        if (b.equals(squareButton))
            {
            doSquare();
            panelFFT.repaint();
            }
        if (b.equals(highPassButton))
            {
            doHighPass();
            panelFFT.repaint();
            }
        if (b.equals(noiseButton))
            {
            doNoise();
            panelFFT.repaint();
            }
        if (b.equals(phaseButton))
            {
            doPhaseShift();
            panelFFT.repaint();
            }
        if (b.equals(blankButton))
            {
            doBlank();
            panelFFT.repaint();
            }
        if (b.equals(sawtoothButton))
            {
            doSawtooth();
            panelFFT.repaint();
            }
        if (b.equals(clipButton))
            {
            doClip();
            panelFFT.repaint();
            }
        if (b.equals(quantizeButton))
            {
            doQuantize();
            panelFFT.repaint();
            }
        else
            {
            quantizeCount = 0;
            }
        if (b.equals(resampleButton))
            {
            doResample();
            panelFFT.repaint();
            }
        else
            {
            resampleCount = 0;
            }
        }


    public void itemStateChanged(final ItemEvent e)
        {
        //System.out.println("checkbox");
        if (e.getSource().equals(magPhaseCheck))
            {
            handleResize();
            }
        panelFFT.repaint();
        }


    public void adjustmentValueChanged(final AdjustmentEvent e)
        {
        //System.out.print(((JScrollBar) e.getSource()).getValue() + "\n");
        if (e.getSource().equals(termBar))
            {
            panelFFT.repaint();
            }

        if (e.getSource().equals(freqBar))
            {
            freqAdjusted = true;
            panelFFT.repaint();
            }
        }


    public void mouseDragged(final MouseEvent e)
        {
        dragging = true;
        edit(e);
        }


    public void mouseMoved(final MouseEvent e)
        {
        final int x = e.getX();
        final int y = e.getY();
        dragX = x;
        dragY = y;
        final int oldCoef = selectedCoef;
        selectedCoef = -1;
        selection = 0;
        final int oldsel = selection;
        if (viewFunc.contains(x,
                              y))
            {
            selection = SEL_FUNC;
            }
        else
            {
            final int termWidth = getTermWidth();
            selectedCoef = x / termWidth;
            if (selectedCoef > termBar.getValue())
                {
                selectedCoef = -1;
                }
            if (selectedCoef != -1)
                {
                if (viewMag.contains(x,
                                     y))
                    {
                    selection = SEL_MAG;
                    }
                else if (viewMutes.contains(x,
                                            y))
                    {
                    selection = SEL_MUTES;
                    }
                else if (viewSolos.contains(x,
                                            y))
                    {
                    selection = SEL_SOLOS;
                    }
                else if (viewPhase.contains(x,
                                            y))
                    {
                    selection = SEL_PHASE;
                    }
                }
            }
        if (selectedCoef != oldCoef || oldsel != selection)
            {
            panelFFT.repaint();
            }
        }


    public void mouseClicked(final MouseEvent e)
        {
        if (e.getClickCount() == 2 && selectedCoef != -1 &&
                selection != SEL_MUTES && selection != SEL_SOLOS)
            {
            int i;

            for (i = 0;
                 i != termBar.getValue();
                 i++)
                {
                phasecoef[i] = 0;
                if (selectedCoef != i)
                    {
                    arrayMagnitudeCoefficients[i] = 0;
                    }
                }

            arrayMagnitudeCoefficients[selectedCoef] = 1;
            if (!magPhaseCheck.isEnabled())
                {
                phasecoef[selectedCoef] = (selection == SEL_MAG) ? -pi / 2 : 0;
                }
            doSetFunc();

            panelFFT.repaint();
            }
        }


    public void mouseEntered(final MouseEvent e)
        {
        }


    public void mouseExited(final MouseEvent e)
        {
        }


    public void mousePressed(final MouseEvent e)
        {
        mouseMoved(e);
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0 &&
                selectedCoef != -1)
            {
            termBar.setValue(selectedCoef + 1);
            panelFFT.repaint();
            }
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
            {
            return;
            }
        dragging = true;
        edit(e);
        }


    public void mouseReleased(final MouseEvent e)
        {
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
            {
            return;
            }
        dragging = false;
        if (selection == SEL_FUNC)
            {
            transform();
            }
        else if (selection != SEL_NONE)
            {
            doSetFunc();
            }
        panelFFT.repaint();
        }


    public boolean handleEvent(final Event ev)
        {
        if (ev.id == Event.WINDOW_DESTROY)
            {
            return true;
            }
        return super.handleEvent(ev);
        }


    }
