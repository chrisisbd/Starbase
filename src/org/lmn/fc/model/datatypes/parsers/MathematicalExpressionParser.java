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

package org.lmn.fc.model.datatypes.parsers;

import net.astesana.javaluator.DoubleEvaluator;
import net.astesana.javaluator.StaticVariableSet;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.datatypes.DataTypeParserInterface;
import org.lmn.fc.model.datatypes.RootDataTypeInterface;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.util.List;
import java.util.regex.Pattern;


/***************************************************************************************************
 * MathematicalExpressionParser.
 */

public class MathematicalExpressionParser implements DataTypeParserInterface
    {
    // The Parser is a Singleton!
    private volatile static DataTypeParserInterface PARSER_INSTANCE;


    /***********************************************************************************************
     * The Parser is a Singleton!
     *
     * @return DataTypeParserInterface
     */

    public static DataTypeParserInterface getInstance()
        {
        if (PARSER_INSTANCE == null)
            {
            synchronized (MathematicalExpressionParser.class)
                {
                if (PARSER_INSTANCE == null)
                    {
                    PARSER_INSTANCE = new MathematicalExpressionParser();
                    }
                }
            }

        return (PARSER_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the MathematicalExpressionParser.
     */

    private MathematicalExpressionParser()
        {
        super();
        }


    /**********************************************************************************************/
    /* DataTypeParserInterface implementations                                                    */
    /***********************************************************************************************
     * Attempt to parse the Starscript ParameterType Value into the specified DataType.
     * Uses the Name, Regex and Value in the Parameter.
     * Called only from the static helper.
     *
     * @param parameter
     * @param datatype
     * @param errors
     *
     * @return boolean
     */

    public boolean validateParameterValueAsDataType(final ParameterType parameter,
                                                    final DataTypeDictionary datatype,
                                                    final List<String> errors)
        {
        final String SOURCE = "MathematicalExpressionParser.validateParameterValueAsDataType() ";
        int intFailures;

        intFailures = 0;

        if ((parameter.getRegex() != null)
            && (!EMPTY_STRING.equals(parameter.getRegex())))
            {
            // Validate using Regex
            if (Pattern.matches(parameter.getRegex(), parameter.getValue()))
                {
                try
                    {
                    final DoubleEvaluator evaluator;
                    final StaticVariableSet<Double> variables;
                    final Double result;

                    evaluator = new DoubleEvaluator();
                    variables = new StaticVariableSet<Double>();

                    // Create the default set of variables, since we don't know how this is being used
                    variables.set(VARIABLE_X, 0.0);
                    variables.set(VARIABLE_Y, 0.0);
                    variables.set(VARIABLE_T, 0.0);

                    result = evaluator.evaluate(parameter.getValue(), variables);
                    }

                catch (IllegalArgumentException exception)
                    {
                    errors.add("[" + parameter.getValue() + "]" + INVALID_EXPRESSION);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + parameter.getName() + "]" + INVALID_EXPRESSION);
                errors.add("[name=" + parameter.getName() + "]" + MSG_REGEX + parameter.getRegex());
                intFailures++;
                }
            }
        else
            {
            // ToDo Consider using Regex from the DataType
            // Do the best we can with no Regex
            try
                {
                final DoubleEvaluator evaluator;
                final StaticVariableSet<Double> variables;
                final Double result;

                evaluator = new DoubleEvaluator();
                variables = new StaticVariableSet<Double>();

                // Create the default set of variables, since we don't know how this is being used
                variables.set(VARIABLE_X, 0.0);
                variables.set(VARIABLE_Y, 0.0);
                variables.set(VARIABLE_T, 0.0);

                result = evaluator.evaluate(parameter.getValue(), variables);
                }

            catch (IllegalArgumentException  exception)
                {
                errors.add("[" + parameter.getValue() + "]" + INVALID_EXPRESSION);
                intFailures++;
                }
            }

        return (intFailures == 0);
        }


    /***********************************************************************************************
     * Attempt to validate the ValueField as being of the specified DataType.
     * Called only from the static helpers.
     *
     * @param valuefield
     * @param datatype
     * @param errors
     *
     * @return boolean
     */

    public boolean validateValueFieldAsDataType(final String valuefield,
                                                final DataTypeDictionary datatype,
                                                final List<String> errors)
        {
        final String SOURCE = "MathematicalExpressionParser.validateValueFieldAsDataType() ";
        int intFailures;

        intFailures = 0;

        if ((datatype.getRegex() != null)
            && (!EMPTY_STRING.equals(datatype.getRegex())))
            {
            // Validate using Regex
            if (Pattern.matches(datatype.getRegex(), valuefield))
                {
                try
                    {
                    final DoubleEvaluator evaluator;
                    final StaticVariableSet<Double> variables;
                    final Double result;

                    evaluator = new DoubleEvaluator();
                    variables = new StaticVariableSet<Double>();

                    // Create the default set of variables, since we don't know how this is being used
                    variables.set(VARIABLE_X, 0.0);
                    variables.set(VARIABLE_Y, 0.0);
                    variables.set(VARIABLE_T, 0.0);

                    result = evaluator.evaluate(valuefield, variables);
                    }

                catch (IllegalArgumentException  exception)
                    {
                    errors.add("[" + valuefield + "]" + INVALID_EXPRESSION);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + valuefield + "]" + INVALID_EXPRESSION);
                errors.add("[name=" + valuefield + "]" + MSG_REGEX + datatype.getRegex());
                intFailures++;
                }
            }
        else
            {
            // Do the best we can with no Regex
            try
                {
                final DoubleEvaluator evaluator;
                final StaticVariableSet<Double> variables;
                final Double result;

                evaluator = new DoubleEvaluator();
                variables = new StaticVariableSet<Double>();

                // Create the default set of variables, since we don't know how this is being used
                variables.set(VARIABLE_X, 0.0);
                variables.set(VARIABLE_Y, 0.0);
                variables.set(VARIABLE_T, 0.0);

                result = evaluator.evaluate(valuefield, variables);
                }

            catch (IllegalArgumentException  exception)
                {
                errors.add("[" + valuefield + "]" + INVALID_EXPRESSION);
                intFailures++;
                }
            }

        return (intFailures == 0);
        }


    /***********************************************************************************************
     * Attempt to parse the ValueField into the specified DataType.
     * Used for e.g. return of primitive types.
     * Return NULL if the parsing failed.
     * Called only from the static helper.
     *
     * @param valuefield
     * @param datatype
     * @param name
     * @param regex
     * @param errors
     *
     * @return RootDataTypeInterface
     */

    public RootDataTypeInterface parseValueFieldToDataType(final String valuefield,
                                                           final DataTypeDictionary datatype,
                                                           final String name,
                                                           final String regex,
                                                           final List<String> errors)
        {
        final String SOURCE = "MathematicalExpressionParser.parseValueFieldToDataType() ";

        // Not required for this DataType

        if (errors != null)
            {
            errors.add(ERROR_NO_PARSER);

            if (!datatype.isNumeric())
                {
                errors.add(ERROR_NON_NUMERIC);
                }
            }

        return (null);
        }


    /***********************************************************************************************
     * Attempt to parse the ValueField into a Number.
     * Used for e.g. return of primitive types.
     * In this case, return the double value of the validated MathematicalExpression, as a Number.
     * Return NULL if the parsing failed, or if the DataType is not a numeric.
     * Called only from the static helper.
     *
     * @param valuefield
     * @param datatype
     * @param name
     * @param regex
     * @param errors
     *
     * @return Number
     */

    public Number parseValueFieldToNumber(final String valuefield,
                                          final DataTypeDictionary datatype,
                                          final String name,
                                          final String regex,
                                          final List<String> errors)
        {
        final String SOURCE = "MathematicalExpressionParser.parseValueFieldToNumber() ";
        Number number;

        number = null;

        if ((valuefield != null)
            && (valuefield.length() > 0)
            && (datatype.isNumeric()))
            {
            if ((datatype.getRegex() != null)
                && (!EMPTY_STRING.equals(datatype.getRegex())))
                {
                // Validate using Regex
                if (Pattern.matches(datatype.getRegex(), valuefield))
                    {
                    try
                        {
                        final DoubleEvaluator evaluator;
                        final StaticVariableSet<Double> variables;
                        final Double result;

                        evaluator = new DoubleEvaluator();
                        variables = new StaticVariableSet<Double>();

                        // Create the default set of variables, since we don't know how this is being used
                        variables.set(VARIABLE_X, 0.0);
                        variables.set(VARIABLE_Y, 0.0);
                        variables.set(VARIABLE_T, 0.0);

                        result = evaluator.evaluate(valuefield, variables);

                        // Don't assign the return value until we are sure there won't be an Exception
                        number = result;
                        }

                    catch (IllegalArgumentException  exception)
                        {
                        errors.add("[" + valuefield + "]" + INVALID_EXPRESSION);
                        }
                    }
                else
                    {
                    errors.add("[" + valuefield + "]" + INVALID_EXPRESSION);
                    errors.add("[" + valuefield + "]" + MSG_REGEX + datatype.getRegex());
                    }
                }
            else
                {
                // Do the best we can with no Regex
                try
                    {
                    final DoubleEvaluator evaluator;
                    final StaticVariableSet<Double> variables;
                    final Double result;

                    evaluator = new DoubleEvaluator();
                    variables = new StaticVariableSet<Double>();

                    // Create the default set of variables, since we don't know how this is being used
                    variables.set(VARIABLE_X, 0.0);
                    variables.set(VARIABLE_Y, 0.0);
                    variables.set(VARIABLE_T, 0.0);

                    result = evaluator.evaluate(valuefield, variables);

                    // Don't assign the return value until we are sure there won't be an Exception
                    number = result;
                    }

                catch (IllegalArgumentException  exception)
                    {
                    errors.add("[" + valuefield + "]" + INVALID_EXPRESSION);
                    }
                }
            }

        return (number);
        }


    /***********************************************************************************************
     * Parse the byte array into a valid ResponseValue, returned in the ResponseType.
     * Uses the Name and Regex in the response.
     * Return the MathematicalExpression verbatim, if it passes validation.
     * Return a Response Value of NO_DATA if the parsing failed.
     * Called only from the static helper.
     *
     * @param bytes
     * @param datatype
     * @param responsetype
     * @param errors
     *
     * @return boolean
     */

    public boolean parseBytesToResponseValue(final byte[] bytes,
                                             final DataTypeDictionary datatype,
                                             final ResponseType responsetype,
                                             final List<String> errors)
        {
        final String SOURCE = "MathematicalExpressionParser.parseBytesToResponseValue() ";
        int intFailures;
        final String valuefield;

        intFailures = 0;
        valuefield = new String(bytes);

        if ((responsetype.getRegex() != null)
            && (!EMPTY_STRING.equals(responsetype.getRegex())))
            {
            // Validate using Regex
            if (Pattern.matches(responsetype.getRegex(), valuefield))
                {
                try
                    {
                    final DoubleEvaluator evaluator;
                    final StaticVariableSet<Double> variables;
                    final Double result;

                    evaluator = new DoubleEvaluator();
                    variables = new StaticVariableSet<Double>();

                    // Create the default set of variables, since we don't know how this is being used
                    variables.set(VARIABLE_X, 0.0);
                    variables.set(VARIABLE_Y, 0.0);
                    variables.set(VARIABLE_T, 0.0);

                    result = evaluator.evaluate(valuefield, variables);
                    }

                catch (IllegalArgumentException  exception)
                    {
                    errors.add("[" + valuefield + "]" + INVALID_EXPRESSION);
                    intFailures++;
                    }
                }
            else
                {
                errors.add("[name=" + responsetype.getName() + "]" + INVALID_EXPRESSION);
                errors.add("[name=" + responsetype.getName() + "]" + MSG_REGEX + responsetype.getRegex());
                intFailures++;
                }
            }
        else
            {
            // Do the best we can with no Regex
            try
                {
                final DoubleEvaluator evaluator;
                final StaticVariableSet<Double> variables;
                final Double result;

                evaluator = new DoubleEvaluator();
                variables = new StaticVariableSet<Double>();

                // Create the default set of variables, since we don't know how this is being used
                variables.set(VARIABLE_X, 0.0);
                variables.set(VARIABLE_Y, 0.0);
                variables.set(VARIABLE_T, 0.0);

                result = evaluator.evaluate(valuefield, variables);
                }

            catch (IllegalArgumentException  exception)
                {
                errors.add("[" + valuefield + "]" + INVALID_EXPRESSION);
                intFailures++;
                }
            }

        // If all went well, return the ResponseValue
        if (intFailures == 0)
            {
            responsetype.setValue(valuefield);
            }
        else
            {
            responsetype.setValue(NO_DATA);
            }

        return (intFailures == 0);
        }
    }
