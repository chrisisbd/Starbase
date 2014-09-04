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

package org.lmn.fc.database;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.registry.RegistryManagerPlugin;
import org.lmn.fc.model.registry.RegistryModelPlugin;
import org.lmn.fc.model.registry.RegistryPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.registry.impl.RegistryManager;
import org.lmn.fc.model.registry.impl.RegistryModel;

import java.sql.Connection;

/***************************************************************************************************
*
*/

public interface DatabasePlugin extends FrameworkConstants,
                                        FrameworkMetadata,
                                        FrameworkStrings,
    ResourceKeys
    {
    RegistryPlugin REGISTRY = Registry.getInstance();
    RegistryManagerPlugin REGISTRY_MANAGER = RegistryManager.getInstance();
    RegistryModelPlugin REGISTRY_MODEL = RegistryModel.getInstance();
    Logger LOGGER = Logger.getInstance();

    boolean getActive();

    void setActive(final boolean flag);

    DatabaseOptions getDatabaseOptions();

    Connection getConnection();

    void setConnection(Connection connection);

    void openConnection();

    void closeConnection();
    }
