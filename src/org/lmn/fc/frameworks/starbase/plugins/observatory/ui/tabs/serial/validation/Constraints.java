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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.validation;

import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.SerialConfigurationNodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/***************************************************************************************************
 * Constraints.
 * Taken from: http://forum.jgraph.com/questions/5222/custom-graph-validation
 */

public final class Constraints implements ConstraintsInterface
    {
    private static final boolean SOURCE = true;
    private static final boolean TARGET = false;

    private final List<ConstraintEdge> listEdgeConstraints;
    private final Map<SerialConfigurationNodeType, List<ConstraintNode>> mapNodeConstraints;


    /**********************************************************************************************
     * Create the list of allowed Edges.
     */

    private static void createEdgeConstraintList(final List<ConstraintEdge> edgelist)
        {
        edgelist.add(new ConstraintEdge(SerialConfigurationNodeType.STARIBUS_INSTRUMENT, SerialConfigurationNodeType.STARIBUS_HUB));
        // Allowing these changes could get complicated!
        //edgelist.add(new ConstraintEdge(SerialConfigurationNodeType.STARIBUS_INSTRUMENT, SerialConfigurationNodeType.SERIAL_PORT));
        //edgelist.add(new ConstraintEdge(SerialConfigurationNodeType.STARIBUS_INSTRUMENT, SerialConfigurationNodeType.UNKNOWN_PORT));

        edgelist.add(new ConstraintEdge(SerialConfigurationNodeType.STARIBUS_HUB, SerialConfigurationNodeType.SERIAL_PORT));
        edgelist.add(new ConstraintEdge(SerialConfigurationNodeType.STARIBUS_HUB, SerialConfigurationNodeType.UNKNOWN_PORT));

        edgelist.add(new ConstraintEdge(SerialConfigurationNodeType.SERIAL_INSTRUMENT, SerialConfigurationNodeType.SERIAL_PORT));
        edgelist.add(new ConstraintEdge(SerialConfigurationNodeType.SERIAL_INSTRUMENT, SerialConfigurationNodeType.UNKNOWN_PORT));
        }


    /**********************************************************************************************
     * Create the map of allowed Node connections.
     * NOTE the different behaviour depending on whether a SINGLE SerialConfigurationNodeType
     * is supplied, or an ARRAY.
     *
     * The SINGLE SerialConfigurationNodeType is treated as an "UP TO nnn" Constraint.
     * The ARRAY of SerialConfigurationNodeType is treated as a "ONE OF nnn" Constraint.
     *
     * @param nodemap
     */

    private static void createNodeConstraintMap(final Map<SerialConfigurationNodeType, List<ConstraintNode>> nodemap)
        {
        List<ConstraintNode> list;

        //-----------------------------------------------------------------------------------------
        // STARIBUS_INSTRUMENT has ONE possible single Target,
        // but no Sources

        list = new ArrayList<ConstraintNode>(1);
        list.add(new ConstraintNode(TARGET,
                                    new SerialConfigurationNodeType[]
                                        {
                                        SerialConfigurationNodeType.STARIBUS_HUB
                                        // Allowing these changes could get complicated!
                                        // SerialConfigurationNodeType.SERIAL_PORT,
                                        //SerialConfigurationNodeType.UNKNOWN_PORT
                                        },
                                    0,
                                    1));

        nodemap.put(SerialConfigurationNodeType.STARIBUS_INSTRUMENT, list);

        //-----------------------------------------------------------------------------------------
        // STARIBUS_HUB could have UP TO 254 STARIBUS_INSTRUMENT Sources,
        // but only ONE OF two single Targets
        // It is possible not to use the STARIBUS_HUB at all

        list = new ArrayList<ConstraintNode>(3);

        list.add(new ConstraintNode(SOURCE,
                                    SerialConfigurationNodeType.STARIBUS_INSTRUMENT,
                                    0,
                                    254));

        list.add(new ConstraintNode(TARGET,
                                    new SerialConfigurationNodeType[]
                                        {
                                        SerialConfigurationNodeType.SERIAL_PORT,
                                        SerialConfigurationNodeType.UNKNOWN_PORT
                                        },
                                    0,
                                    1));

        nodemap.put(SerialConfigurationNodeType.STARIBUS_HUB, list);

        //-----------------------------------------------------------------------------------------
        // SERIAL_INSTRUMENT has ONE OF two possible single Targets,
        // but no Sources

        list = new ArrayList<ConstraintNode>(2);
        list.add(new ConstraintNode(TARGET,
                                    new SerialConfigurationNodeType[]
                                        {
                                        SerialConfigurationNodeType.SERIAL_PORT,
                                        SerialConfigurationNodeType.UNKNOWN_PORT
                                        },
                                    0,
                                    1));

        nodemap.put(SerialConfigurationNodeType.SERIAL_INSTRUMENT, list);

        //-----------------------------------------------------------------------------------------
        // SERIAL_PORT has ONE OF two possible single Sources,
        // but no Targets

        list = new ArrayList<ConstraintNode>(3);
        list.add(new ConstraintNode(SOURCE,
                                    new SerialConfigurationNodeType[]
                                        {
                                        // Allowing this change could get complicated!
                                        // SerialConfigurationNodeType.STARIBUS_INSTRUMENT,
                                        SerialConfigurationNodeType.STARIBUS_HUB,
                                        SerialConfigurationNodeType.SERIAL_INSTRUMENT
                                        },
                                    0,
                                    1));

        nodemap.put(SerialConfigurationNodeType.SERIAL_PORT, list);

        //-----------------------------------------------------------------------------------------
        // UNKNOWN_PORT has UP TO two possible multiple Sources,
        // but no Targets

        list = new ArrayList<ConstraintNode>(2);
        // Allowing this change could get complicated!
//        list.add(new ConstraintNode(SOURCE,
//                                    SerialConfigurationNodeType.STARIBUS_INSTRUMENT,
//                                    0,
//                                    254));
        list.add(new ConstraintNode(SOURCE,
                                    SerialConfigurationNodeType.STARIBUS_HUB,
                                    0,
                                    1));
        list.add(new ConstraintNode(SOURCE,
                                    SerialConfigurationNodeType.SERIAL_INSTRUMENT,
                                    0,
                                    100));

        nodemap.put(SerialConfigurationNodeType.UNKNOWN_PORT, list);
        }


    /***********************************************************************************************
     * Construct Constraints.
     */

    public Constraints()
        {
        this.listEdgeConstraints = new ArrayList<ConstraintEdge>(10);
        this.mapNodeConstraints = new HashMap<SerialConfigurationNodeType, List<ConstraintNode>>(10);

        createEdgeConstraintList(this.listEdgeConstraints);
        createNodeConstraintMap(this.mapNodeConstraints);
        }


    /***********************************************************************************************
     * Get the List of Edge Constraints.
     *
     * @return List<ConstraintEdge>
     */

    public List<ConstraintEdge> getEdgeConstraintList()
        {
        return (this.listEdgeConstraints);
        }


    /***********************************************************************************************
     * Get the List of Node Constraints for the specified NodeType.
     *
     * @return List<ConstraintNode>
     */

    public List<ConstraintNode> getNodeConstraintListForType(final SerialConfigurationNodeType type)
        {
        List<ConstraintNode> listConstraints;

        listConstraints = this.mapNodeConstraints.get(type);

        if (listConstraints == null)
            {
            listConstraints = new ArrayList<ConstraintNode>(10);
            }

        return (listConstraints);
        }
    }