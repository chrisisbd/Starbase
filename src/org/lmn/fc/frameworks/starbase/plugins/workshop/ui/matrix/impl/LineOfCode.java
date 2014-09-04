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

package org.lmn.fc.frameworks.starbase.plugins.workshop.ui.matrix.impl;


import org.lmn.fc.frameworks.starbase.plugins.workshop.ui.matrix.MatrixCodePanelInterface;

import java.awt.*;
import java.util.Random;

//  this represents a vertical line of code on the window

public final class LineOfCode
    {
    private final static int WAITING_STATE = 0;
    private final static int DRAWING_STATE = 1;
    private final static int FADING_STATE = 2;

    private static final Random rNumGen = new Random();

    private final MatrixCharacterPainter painter;
    private final int _numOfCharacters;
    private final int _numOfRows;

    private final int _column;
    private int _currentRow;
    private int _tailRow;
    private int _firstChar;
    private int _state;
    private int _waitCount;
    private int _speed;
    private int _skipCount;


    /**
     * Creates a new instance of LineOfCode
     */
    public LineOfCode(final int column, final MatrixCodePanelInterface panel)
        {
        _column = column;
        painter = panel.getCharacterPainter();
        _numOfRows = panel.getNumOfRows();
        _numOfCharacters = painter.getNumOfCharacters();
        _currentRow = 0;
        _tailRow = 0;
        _firstChar = rNumGen.nextInt(_numOfCharacters);
        _state = WAITING_STATE;
        _waitCount = rNumGen.nextInt(50);
        _speed = rNumGen.nextInt(5);
        _skipCount = 0;
        }

    // this paints the line and increments the state of the line
    public final void paint(final Graphics2D g2)
        {
        if (_skipCount < _speed)
            {
            _skipCount++;
            return;
            }
        else
            {
            _skipCount = 0;
            }

        switch (_state)
            {
            case FADING_STATE:
                painter.eraseCharacter(g2, _column, _tailRow++);
                if (_tailRow > _numOfRows)
                    {
                    _currentRow = 0;
                    _state = WAITING_STATE;
                    _waitCount = rNumGen.nextInt(10);
                    }
                break;

            case DRAWING_STATE:
                // repaint head in dim color
                if (_currentRow > 0)
                    {
                    painter.paintCharacter(_firstChar, g2, _column, _currentRow - 1, false);
                    _firstChar = rNumGen.nextInt(_numOfCharacters);
                    painter.paintCharacter(_firstChar, g2, _column, _currentRow++, true);
                    }
                else
                    {
                    painter.paintCharacter(_firstChar, g2, _column, _currentRow++, true);
                    }

                if (_currentRow > _numOfRows + 1)
                    {
                    _tailRow = 0;
                    _state = FADING_STATE;
                    }
                break;

            case WAITING_STATE:
                _waitCount--;
                if (_waitCount <= 0)
                    {
                    _state = DRAWING_STATE;
                    _speed = rNumGen.nextInt(5);
                    }
                break;
            }
        }

    }
