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

package org.lmn.fc.common.constants;

/***************************************************************************************************
 * FrameworkXpath.
 */

public interface FrameworkXpath
    {
    // When using IntelliJ Xpath, remember to Edit Context to set the namespace!

    // Some examples
    // Find the Command with CC of cc in the Plugin with CCB of bb
    // Note the slash! '/'
    // ins:Plugin[ins:CommandCodeBase/text()='bb']/ins:Command[ins:CommandCode/text()='cc']

    // Find the Command with CC of cc in the DataCapture Plugin
    // Note the slash! '/'
    // ins:Plugin[ins:Identifier/text()='DataCapture']/ins:Command[ins:CommandCode/text()='cc']

    String CORE                                     = "Core";

    // Namespaces
    String XPATH_INSTRUMENTS_NAMESPACE              = "declare namespace ins='instruments.xmlbeans.model.fc.lmn.org';";
    String XPATH_ATTRIBUTES_NAMESPACE               = "declare namespace attr='attributes.xmlbeans.model.fc.lmn.org';";
    String XPATH_EVENTS_NAMESPACE                   = "declare namespace even='events.xmlbeans.model.fc.lmn.org';";

    // Instruments
    String XPATH_INSTRUMENT_FROM_IDENTIFIER         = "/ins:Instruments/ins:Instrument[ins:Identifier/text()='";
    String XPATH_INSTRUMENT_FROM_STARIBUS_ADDRESS   = "/ins:Instruments/ins:Instrument[ins:Controller/ins:StaribusAddress/text()='";

    // Plugins (Modules)
    String XPATH_PLUGIN_FROM_PLUGIN_IDENTIFIER      = "ins:Plugin[ins:Identifier/text()='";
    String XPATH_PLUGIN_FROM_COMMAND_IDENTIFIER     = "ins:Plugin[ins:Command[ins:Identifier/text()='";
    String XPATH_PLUGIN_FROM_CCB                    = "ins:Plugin[ins:CommandCodeBase/text()='";
    String XPATH_PLUGIN_DATA_CAPTURE                = "ins:Plugin[ins:Identifier/text()='DataCapture']";

    // Commands and CommandVariant
    String XPATH_PLUGIN_COMMAND_FROM_COMMAND_IDENTIFIER = "ins:Plugin/ins:Command[ins:Identifier/text()='";
    String XPATH_COMMAND_FROM_IDENTIFIER            = "ins:Command[ins:Identifier/text()='";
    String XPATH_COMMAND_FROM_CC                    = "ins:Command[ins:CommandCode/text()='";
    String XPATH_AND_COMMAND_VARIANT                = "' and ins:CommandVariant/text()='";

    // Miscellaneous
    String XPATH_TERMINATOR                         = "]";
    String XPATH_QUOTE_TERMINATOR                   = "']";
    String XPATH_ATTRIBUTES_CONFIGURATION_FOR_IDENTIFIER = "/attr:Attributes/attr:Configuration[attr:Identifier/text()='";

    // Event Log
    String XPATH_EVENTS_FOR_FRAMEWORK_ID            = "/even:Events/even:Event[even:FrameworkID/text()='";
    String XPATH_EVENTS_FOR_FRAMEWORK_AND_ATOM      = "' and even:AtomID/text()='";
    }



// Useful XPATH

// These work in IntelliJ
// /ins:Instruments/ins:Instrument/ins:Controller/ins:Command[ins:CommandCode/text()='02' and ins:CommandVariant/text()='0002']
// /ins:Instruments/ins:Instrument/ins:Controller[ins:Command/ins:CommandCode/text()='08'] --> Controller
// /ins:Instruments/ins:Instrument/ins:Controller/ins:Command/ins:CommandCode/text()='08' --> Boolean
// /ins:Instruments/ins:Instrument[2]/ins:Controller[ins:Command/ins:CommandCode/text()='08'] --> Controller
// /ins:Instruments/ins:Instrument[2]/ins:Controller/ins:Command   Command nodeset
// /ins:Instruments/ins:Instrument[2]/ins:Controller/ins:Command/ins:CommandCode  CommandCode nodeset
// /ins:Instruments/ins:Instrument[2]/ins:Controller/ins:Command[ins:CommandCode/text()='08']   Command !
// ins:Command[ins:CommandCode/text()='07'] Command from Controller XML


// These work in IntelliJ too!
// the up arrow ^ indicates where selectPath() should start the search

// Query the entire Instruments document to find the specific Instrument
// /ins:Instruments/ins:Instrument[ins:Identifier/text()='GenericInstrument']
// ^

// Find a Plugin, query from root of **Controller**
// /ins:Instruments/ins:Instrument/ins:Controller/ins:Plugin[ins:Identifier/text()='Macros']
//                                               ^

// Find a Command, query from the root of the **Plugin**
// /ins:Instruments/ins:Instrument/ins:Controller/ins:Plugin[ins:Identifier/text()='DataProcessor']/ins:Command[ins:Identifier/text()='applyLinearTransform']
//                                                                                                 ^


// Find a Command, query from the root of the **Controller**
// /ins:Instruments/ins:Instrument/ins:Controller/ins:Command[ins:Identifier/text()='ping']
//                                               ^



