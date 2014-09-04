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


import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationCellDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.SerialConfigurationNodeType;

import java.util.List;


/***************************************************************************************************
 * ConstraintValidator.
 * Taken from: http://forum.jgraph.com/questions/5222/custom-graph-validation
 */

public final class ConstraintValidator implements ConstraintValidatorInterface
    {
    // Injections
    private final ConstraintsInterface constraints;
    private final boolean boolDebug;


    /***********************************************************************************************
     * Construct a ConstraintValidator.
     *
     * @param constr
     * @param debug
     */

    public ConstraintValidator(final ConstraintsInterface constr,
                               final boolean debug)
        {
        // Injections
        this.constraints = constr;
        this.boolDebug = debug;
        }


    /**********************************************************************************************
     * Validate edges for a given source object <-> target object.
     * Return NULL on success.
     *
     * @param source
     * @param target
     *
     * @return String
     */

    public String validateEdge(final SerialConfigurationCellDataInterface source,
                               final SerialConfigurationCellDataInterface target)
        {
        final String SOURCE = "ConstraintValidator.validateEdge() ";
        final List<ConstraintEdge> listEdges;

        // Get the list of valid Edges from the Constraints
        listEdges = constraints.getEdgeConstraintList();

        // Check Edge validity, until a match is found
        for (int intEdgeIndex = 0;
             intEdgeIndex < listEdges.size();
             intEdgeIndex++)
            {
            final ConstraintEdge edge;

            edge = listEdges.get(intEdgeIndex);

            LOGGER.debug(isDebug(),
                             "Checking Edge [edge.index=" + intEdgeIndex
                                 + "] [node.from=" + edge.nodeFrom.getName()
                                 + "] [source.type=" + source.getNodeType().getName()
                                 + "] [node.to=" + edge.nodeTo.getName()
                                 + "] [target.type=" + target.getNodeType().getName()
                                 + "]");

            if (edge.nodeFrom.equals(source.getNodeType()) && edge.nodeTo.equals(target.getNodeType()))
                {
                // Valid match found
                return (null);
                }
            }

        // No match found
        return (source + " --> " + target + ": Invalid Connection");
        }


    /**********************************************************************************************
     * Validate a node against its incoming and outgoing nodes.
     * Return NULL on success, or an Error message.
     *
     * @param userobject
     * @param sourceslist
     * @param targetslist
     *
     * @return String
     */

    public String validateNode(final SerialConfigurationCellDataInterface userobject,
                               final List<SerialConfigurationCellDataInterface> sourceslist,
                               final List<SerialConfigurationCellDataInterface> targetslist)
        {
        final String SOURCE = "ConstraintValidator.validateNode() ";
        final List<ConstraintNode> listConstraintsForNodeType;
        final StringBuffer bufferReturn;

        // Get the list of Constraints appropriate to this NodeType
        listConstraintsForNodeType = constraints.getNodeConstraintListForType(userobject.getNodeType());
        bufferReturn = new StringBuffer();

        LOGGER.debug(isDebug(),
                     "Checking NodeType " + userobject.getNodeType().getName()
                           + " [constraint.count=" + listConstraintsForNodeType.size()
                           + "]");

        // Check each Constraint (single or multiple) against each Connection
        for (int intConstraintIndex = 0;
             intConstraintIndex < listConstraintsForNodeType.size();
             intConstraintIndex++)
            {
            final ConstraintNode constraintToTest;
            final List<SerialConfigurationCellDataInterface> listNodeConnections;
            final int intTotalConnectionsCount;
            int intValidConnectionsForConstraintCount;

            constraintToTest = listConstraintsForNodeType.get(intConstraintIndex);

            // Get the connections list which we need to process for each Constraint,
            // i.e. SOURCEs or TARGETs
            if (constraintToTest.boolSource)
                {
                listNodeConnections = sourceslist;
                LOGGER.debug(isDebug(),
                             "CHECKING SOURCES vs. [constraint.index=" + intConstraintIndex
                                   + "] [constraint.min=" + constraintToTest.intConnectionsMin
                                   + "] [constraint.max=" + constraintToTest.intConnectionsMax
                                   + "] [connection.count.sources=" + listNodeConnections.size()
                                   + "]");
                }
            else
                {
                listNodeConnections = targetslist;
                LOGGER.debug(isDebug(),
                             "CHECKING TARGETS vs. [constraint.index=" + intConstraintIndex
                                   + "] [constraint.min=" + constraintToTest.intConnectionsMin
                                   + "] [constraint.max=" + constraintToTest.intConnectionsMax
                                   + "] [connection.count.targets=" + listNodeConnections.size()
                                   + "]");
                }

            intTotalConnectionsCount = listNodeConnections.size();

            // Calculate number of valid connections for the current Constraint
            intValidConnectionsForConstraintCount = 0;

            for (int intConnectionIndex = 0;
                 intConnectionIndex < intTotalConnectionsCount;
                 intConnectionIndex++)
                {
                final SerialConfigurationCellDataInterface dataConnection;
                final SerialConfigurationNodeType[] arrayNodeTypesForConstraint;

                dataConnection = listNodeConnections.get(intConnectionIndex);
                arrayNodeTypesForConstraint = constraintToTest.arrayNodeTypes;

                LOGGER.debug(isDebug(),
                             "CHECKING CONNECTION [connection.index=" + intConnectionIndex
                                   + "] [connection.label=" + dataConnection.getLabel()
                                   + "] [constraint.nodetypes.count=" + arrayNodeTypesForConstraint.length
                                   + "]");

                // Test each Constraint's NodeType against the Connection's NodeType
                // The SINGLE SerialConfigurationNodeType is treated as an "UP TO nnn" Constraint
                // The ARRAY of SerialConfigurationNodeType is treated as a "ONE OF nnn" Constraint
                for (int intNodeTypeIndex = 0;
                     intNodeTypeIndex < arrayNodeTypesForConstraint.length;
                     intNodeTypeIndex++)
                    {
                    final SerialConfigurationNodeType nodeTypeOfConstraint;

                    nodeTypeOfConstraint = arrayNodeTypesForConstraint[intNodeTypeIndex];

                    // Iterating through the list is similar to an "OR" constraint
                    if (dataConnection.getNodeType().equals(nodeTypeOfConstraint))
                        {
                        intValidConnectionsForConstraintCount++;
                        LOGGER.debug(isDebug(),
                                     "    CONNECTION MEETS CONSTRAINT [nodetype.index=" + intNodeTypeIndex
                                               + "] [nodetype.name=" + nodeTypeOfConstraint.getName()
                                               + "] [connections.valid=" + intValidConnectionsForConstraintCount
                                               + "] [connections.total=" + intTotalConnectionsCount
                                               + "]");
                        }
                    else
                        {
                        LOGGER.debug(isDebug(),
                                     "    CONNECTION DOES NOT MEET CONSTRAINT, SO SKIP [nodetype.index=" + intNodeTypeIndex
                                                + "] [nodetype.name=" + nodeTypeOfConstraint.getName()
                                                + "] [connections.valid=" + intValidConnectionsForConstraintCount
                                                + "] [connections.total=" + intTotalConnectionsCount
                                                + "]");
                        }
                    }
                }

            if ((constraintToTest.intConnectionsMin != -1)
                && (intValidConnectionsForConstraintCount < constraintToTest.intConnectionsMin))
                {
                // This message will appear as the tooltip of the Cell's warning icon
                bufferReturn.append("Failed MIN [constraint=" + userobject.getLabel()
                                        + "] " + constraintToTest
                                        + " [connections.valid=" + intValidConnectionsForConstraintCount
                                        + "] [connections.total=" + intTotalConnectionsCount
                                        + "]");
                }
            else if ((constraintToTest.intConnectionsMax != -1)
                     && (intValidConnectionsForConstraintCount > constraintToTest.intConnectionsMax))
                {
                // This message will appear as the tooltip of the Cell's warning icon
                bufferReturn.append("Failed MAX [constraint=" + userobject.getLabel()
                                        + "] " + constraintToTest
                                        + " [connections.valid=" + intValidConnectionsForConstraintCount
                                        + "] [connections.total=" + intTotalConnectionsCount
                                        + "]");
                }
            else
                {
                LOGGER.debug(isDebug(),
                             "PASSED [constraint=" + userobject.getLabel()
                                   + "] " + constraintToTest
                                   + " [connections.valid=" + intValidConnectionsForConstraintCount
                                   + "] [connections.total=" + intTotalConnectionsCount
                                   + "]");
                }
            }

        // See if any Constraint failed
        // Use JGraph's horrible return method
        if (bufferReturn.length() == 0)
            {
            return (null);
            }
        else
            {
            return (bufferReturn.toString());
            }
        }


    /***********************************************************************************************
     * Indicate if we are in debug mode.
     *
     * @return boolean
     */

    private boolean isDebug()
        {
        return (this.boolDebug);
        }
    }
