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

package org.lmn.fc.frameworks.starbase.plugins.observatory.common;

import org.lmn.fc.common.constants.ControlCharacters;


public interface ObservatoryConstants
    {
    // String Resources
    String MSG_NO_CLOCK = "The Observatory Clock is not running";

    int VIRTUAL_CONTROLLER_ADDRESS = 0;

    byte STARIBUS_START = ControlCharacters.STX.getByteCode();
    byte STARIBUS_SYNCHRONISE = ControlCharacters.SYN.getByteCode();
    byte STARIBUS_DELIMITER = ControlCharacters.US.getByteCode();
    byte STARIBUS_RESPONSE_SEPARATOR = ControlCharacters.RS.getByteCode();
    String STARIBUS_RESPONSE_SEPARATOR_REGEX = "\\x1E";
    byte STARIBUS_TERMINATOR_0 = ControlCharacters.EOT.getByteCode();
    byte STARIBUS_TERMINATOR_1 = ControlCharacters.CR.getByteCode();
    byte STARIBUS_TERMINATOR_2 = ControlCharacters.LF.getByteCode();

    byte STARIBUS_BOOLEAN_TRUE = 'Y';
    byte STARIBUS_BOOLEAN_FALSE = 'N';

    String RESPONSE_LOCAL = "LOCAL";
    Class<Double> DATA_CLASS_DEFAULT = Double.class;
    }
