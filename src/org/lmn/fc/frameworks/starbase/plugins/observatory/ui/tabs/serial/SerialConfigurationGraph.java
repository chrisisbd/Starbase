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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.SerialConfigurationCellDataInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.validation.ConstraintValidator;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.validation.ConstraintValidatorInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.tabs.serial.validation.Constraints;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/***************************************************************************************************
 * SerialConfigurationGraph.
 * Taken from: http://forum.jgraph.com/questions/5222/custom-graph-validation
 */

public class SerialConfigurationGraph extends mxGraph
                                      implements FrameworkConstants,
                                                 FrameworkStrings,
                                                 FrameworkMetadata,
                                                 FrameworkSingletons,
                                                 FrameworkRegex,
                                                 ResourceKeys
    {
    // Injections
    private final boolean boolDebug;

    private final ConstraintValidatorInterface constraintValidator;


    /***********************************************************************************************
     * Construct a SerialConfigurationGraph.
     *
     * @param debug
     */

    public SerialConfigurationGraph(final boolean debug)
        {
        super();

        // Injections
        this.boolDebug = debug;

        this.constraintValidator = new ConstraintValidator(new Constraints(),
                                                           debug);
        }


    /**********************************************************************************************
     * Edge Validation.
     *
     * @param edge Cell that represents the edge to validate.
     * @param source Cell that represents the source terminal.
     * @param target Cell that represents the target terminal.
     *
     * @return String
     */

    @Override
    public String validateEdge(final Object edge,
                               final Object source,
                               final Object target)
        {
        final Object objSource;
        final Object objTarget;
        final SerialConfigurationCellDataInterface dataSource;
        final SerialConfigurationCellDataInterface dataTarget;
        final String strError;

        // Check object types
        objSource = getModel().getValue(source);
        objTarget = getModel().getValue(target);

        // Check that the UserObjects at both ends of the Edge are of the correct Type
        if (!(objSource instanceof SerialConfigurationCellDataInterface)
            || !(objTarget instanceof SerialConfigurationCellDataInterface))
            {
            // Return immediately with NULL, i.e. valid
            return (super.validateEdge(edge, source, target));
            }

        // Both Source and Target are SerialConfigurationCellData, so validate
        dataSource = (SerialConfigurationCellDataInterface) ((mxCell) source).getValue();
        dataTarget = (SerialConfigurationCellDataInterface) ((mxCell) target).getValue();

        strError = constraintValidator.validateEdge(dataSource, dataTarget);

        if (strError != null)
            {
            return (strError + "\n");
            }

        // The Edge is valid
        return (null);
        }


    /**********************************************************************************************
     * Perform Node validation of the specified Cell.
     *
     * @param cell
     * @param context
     *
     * @return String
     */

    @Override
    public String validateCell(final Object cell,
                               final Hashtable<Object, Object> context)
        {
        final String SOURCE = "SerialConfigurationGraph.validateCell() ";
        final Object userObject;
        final SerialConfigurationCellDataInterface dataValue;
        final List<SerialConfigurationCellDataInterface> listSources;
        final List<SerialConfigurationCellDataInterface> listTargets;
        final String strError;

        // Check object type
        userObject = getModel().getValue(cell);

        if (!(userObject instanceof SerialConfigurationCellDataInterface))
            {
            // Return immediately with NULL, i.e. valid
            return super.validateCell(cell, context);
            }

        // We check only vertices
        if (!((mxCell) cell).isVertex())
            {
            // Return immediately with NULL, i.e. valid
            return super.validateCell(cell, context);
            }

        // Validate userObject as SerialConfigurationCellData
        dataValue = (SerialConfigurationCellDataInterface) userObject;

        LOGGER.debug(isDebug(),
                     SOURCE + "checking: " + dataValue + ", edges: " + ((mxCell) cell).getEdgeCount());

        // Create lists of source and target terminals
        listSources = new ArrayList<SerialConfigurationCellDataInterface>(10);
        listTargets = new ArrayList<SerialConfigurationCellDataInterface>(10);

        for (int intEdgeIndex = 0;
             intEdgeIndex < ((mxCell) cell).getEdgeCount();
             intEdgeIndex++)
            {
            final Object edge;
            final Object objEdgeSource;
            final Object objEdgeTarget;

            // Examine each Edge connected to this Cell
            edge = ((mxCell) cell).getEdgeAt(intEdgeIndex);

            objEdgeSource = ((mxCell) edge).getSource();
            objEdgeTarget = ((mxCell) edge).getTarget();

            if ((objEdgeSource != null)
                && (!objEdgeSource.equals(cell))
                && (objEdgeTarget != null)
                && (objEdgeTarget.equals(cell)))
                {
                // This must be an incoming Edge
                listSources.add((SerialConfigurationCellDataInterface) ((mxCell) objEdgeSource).getValue());
                }

            if ((objEdgeTarget != null)
                && (!objEdgeTarget.equals(cell))
                && (objEdgeSource != null)
                && (objEdgeSource.equals(cell)))
                {
                // This must be an outgoing Edge
                listTargets.add((SerialConfigurationCellDataInterface) ((mxCell) objEdgeTarget).getValue());
                }
            }

        // ToDO logging
        for (final Object item : listSources)
            {
            LOGGER.debug(isDebug(),
                         "  in: " + item);
            }

        for (final Object item : listTargets)
            {
            LOGGER.debug(isDebug(),
                         "  out: " + item);
            }

        // Check Node rules
        strError = constraintValidator.validateNode(dataValue,
                                                    listSources,
                                                    listTargets
        );

        if (strError != null)
            {
            // The Cell is invalid
            return (strError + "\n");
            }

        // The Cell is valid
        return (null);
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
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