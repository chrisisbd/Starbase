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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkSingletons;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentDAOInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ObservatoryInstrumentInterface;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.ResetMode;
import org.lmn.fc.frameworks.starbase.portcontroller.CommandMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.DaoPortInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageInterface;
import org.lmn.fc.frameworks.starbase.portcontroller.ResponseMessageStatus;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.registry.InstallationFolder;
import org.lmn.fc.model.xmlbeans.datatypes.SchemaDataType;
import org.lmn.fc.model.xmlbeans.instruments.CommandType;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.model.xmlbeans.instruments.ParameterType;
import org.lmn.fc.model.xmlbeans.instruments.ResponseType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;


/***************************************************************************************************
 * DAOHelper.
 */

public final class DAOHelper implements FrameworkConstants,
                                        FrameworkStrings,
                                        FrameworkMetadata,
                                        FrameworkSingletons
    {
    /***********************************************************************************************
     * Instantiate a ObservatoryInstrumentDAO.
     *
     * @param obsinstrument
     * @param classname
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public static ObservatoryInstrumentDAOInterface instantiateDAO(final ObservatoryInstrumentInterface obsinstrument,
                                                                   final String classname)
        {
        ObservatoryInstrumentDAOInterface daoInterface;

        daoInterface = null;

        try
            {
            final Class classObject;
            final Class[] interfaces;
            final String strInterface;
            boolean boolLoaded;

            classObject = Class.forName(classname);

            // Does the target implement the ObservatoryInstrumentDAOInterface?
            interfaces = classObject.getInterfaces();
            strInterface = ObservatoryInstrumentDAOInterface.class.getName();
            boolLoaded = false;

            if ((interfaces != null)
                && (interfaces.length > 0))
                {
                if (!classObject.isInterface())
                    {
                    // Try to find the mandatory interface
                    for (int i = 0;
                         ((i < interfaces.length) && (!boolLoaded));
                         i++)
                        {
                        if ((strInterface.equals(interfaces[i].getName()))
                            || (isSuperInterface(interfaces[i], strInterface)))
                            {
                            // We have found the correct interface
//                            LOGGER.log("DAOHelper: [" + classname + " implements " + strInterface + "]");
//                            LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
//                                                   "DAOHelper: [" + classname + " implements " + strInterface + "]");

                            final Constructor constructor;
                            final Class[] parameterTypes =
                                {
                                ObservatoryInstrumentInterface.class
                                };

                            // Now get hold of the Constructor
                            constructor = classObject.getDeclaredConstructor(parameterTypes);

                            if (constructor != null)
                                {
                                final Object objArguments[];

                                objArguments = new Object[1];
                                objArguments[0] = obsinstrument;

                                daoInterface = (ObservatoryInstrumentDAOInterface)constructor.newInstance(objArguments);
                                boolLoaded = true;
                                }
                            else
                                {
                                LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "Constructor not found");
                                }
                            }
                        else
                            {
                            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "Incorrect interface " + interfaces[i].getName());
                            }
                        }
                    }
                else
                    {
                    LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "Class is an interface only");
                    }
                }
            else
                {
                LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "No interfaces found");
                }
            }

        catch(final NoSuchMethodException exception)
            {
            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "NoSuchMethodException [classname=" + classname + "]");
            }

        catch(final SecurityException exception)
            {
            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "SecurityException [classname=" + classname + "]");
            }

        catch (final InstantiationException exception)
            {
            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "InstantiationException [classname=" + classname + "]");
            }

        catch (final IllegalAccessException exception)
            {
            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "IllegalAccessException [classname=" + classname + "]");
            }

        catch (final IllegalArgumentException exception)
            {
            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "IllegalArgumentException [classname=" + classname + "]");
            }

        catch (final InvocationTargetException exception)
            {
            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "InvocationTargetException [classname=" + classname + "]");
            exception.printStackTrace();
            }

        catch (final ClassNotFoundException exception)
            {
            LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "ClassNotFoundException [classname=" + classname + "]");
            }

        return (daoInterface);
        }


    /***********************************************************************************************
     * Check to see if the specified name is in the list of super Interfaces.
     *
     * @param anInterface
     * @param name
     *
     * @return boolean
     */

    private static boolean isSuperInterface(final Class anInterface,
                                            final String name)
        {
        final Class[] arraySuperInterfaces;
        boolean boolFoundIt;

        arraySuperInterfaces = anInterface.getInterfaces();
        boolFoundIt = false;

        for (int i = 0;
             ((!boolFoundIt)
                && (i < arraySuperInterfaces.length));
             i++)
            {
            final Class superInterface;

            superInterface = arraySuperInterfaces[i];
            boolFoundIt = (name.equals(superInterface.getName()));
            }

        return (boolFoundIt);
        }

    // Removed from above to allow subclassing of StaribusDAO!
//                            // Prove that the real DAO is a subclass of AbstractObservatoryInstrumentDAO
//                            final Class superClass = classObject.getSuperclass();
//
//                            if (superClass != null)
//                                {
//                                if (AbstractObservatoryInstrumentDAO.class.getName().equals(superClass.getName()))
//                                    {
//                                    }
//                                else
//                                    {
//                                    LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "Class is not a subclass of " + AbstractObservatoryInstrumentDAO.class.getName());
//                                    }
//                                }
//                            else
//                                {
//                                LOGGER.error(ObservatoryInstrumentDAOInterface.ERROR_NOT_INSTANTIATED + "Class has no superclass");
//                                }


    /***********************************************************************************************
     * Validate a set of parameters for Command execution.
     *
     * @param instrument
     * @param instrumentxml
     * @param module
     * @param command
     * @param starscript
     * @param port
     *
     * @return boolean
     */

    public synchronized static boolean isCommandValid(final ObservatoryInstrumentInterface instrument,
                                                      final Instrument instrumentxml,
                                                      final XmlObject module,
                                                      final CommandType command,
                                                      final String starscript,
                                                      final DaoPortInterface port)
        {
        final boolean boolValid;

        boolValid =  ((instrument != null)
                        && (instrumentxml != null)
//                        && (XmlBeansUtilities.isValidXml(instrumentxml))
                        && (module != null)
//                        && (XmlBeansUtilities.isValidXml(module))
                        && (command != null)
//                        && (XmlBeansUtilities.isValidXml(command))
                        && (starscript != null)
                        && (!EMPTY_STRING.equals(starscript.trim()))
                        && (instrumentxml.getControllable())
                        && (instrumentxml.getController() != null));

        // Do not test Port for null!

        return (boolValid);
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Find the originating DAO responsible for the specified Command.
     *
     * @param daos
     * @param commandtype
     *
     * @return ObservatoryInstrumentDAOInterface
     */

    public static ObservatoryInstrumentDAOInterface findOriginatingDAO(final List<ObservatoryInstrumentDAOInterface> daos,
                                                                       final CommandType commandtype)
        {
        //System.out.println("DAO LIST [size=" + daos.size() + "]");

        return (daos.get(0));
        }


    /***********************************************************************************************
     * A utility to show the state of the Port.
     *
     * @param daoport
     *
     * @return StringBuffer
     */

    public static StringBuffer showPortState(final DaoPortInterface daoport)
        {
        final StringBuffer bufferState;

        bufferState = new StringBuffer();

        if (daoport == null)
            {
            bufferState.append("[port=NULL]");
            }
        else
            {
            bufferState.append("[port.name=");
            bufferState.append(daoport.getName());
            bufferState.append("] [port.open=");
            bufferState.append(daoport.isPortOpen());
            bufferState.append("] [port.busy=");
            bufferState.append(daoport.isPortBusy());
            bufferState.append("] [port.staribusport=");
            bufferState.append(daoport.isStaribusPort());
            bufferState.append("]");
            }

        return (bufferState);
        }


    /***********************************************************************************************
     * Extract the ResetMode from a CommandType, i.e. SOFT or DEFAULTS or STARIBUS.
     * Return DEFAULTS on an error.
     *
     * @param command
     *
     * @return ResetMode
     */

    public static ResetMode extractResetMode(final CommandType command)
        {
        ResetMode resetMode;

        resetMode = ResetMode.DEFAULTS;

        if ((command != null)
            && (ObservatoryInstrumentDAOInterface.COMMAND_RESET.equals(command.getIdentifier()))
            && (command.getParameterList() != null)
            && (command.getParameterList().size() == 1))
            {
            final ParameterType parameter;

            parameter = command.getParameterList().get(0);

            if (parameter != null)
                {
                if (ResetMode.DEFAULTS.getName().equals(parameter.getValue()))
                    {
                    resetMode = ResetMode.DEFAULTS;
                    }
                else if (ResetMode.SOFT.getName().equals(parameter.getValue()))
                    {
                    resetMode = ResetMode.SOFT;
                    }
                else if (ResetMode.STARIBUS.getName().equals(parameter.getValue()))
                    {
                    resetMode = ResetMode.STARIBUS;
                    }
                else
                    {
                    LOGGER.error("DAOHelper.extractResetMode() Command Parameter contains an invalid ResetMode");
                    }
                }
            }

        LOGGER.debugTimedEvent(LOADER_PROPERTIES.isTimingDebug(),
                               "RESET MODE=" + resetMode.getName());

        return (resetMode);
        }


    /**********************************************************************************************/
    /* Help Resource Bundles                                                                      */
    /***********************************************************************************************
     * Load any common context sensitive help relevant to the DAO,
     * from a file called CommonHelpBundle.properties.
     *
     * @param dao
     */

    public static void loadCommonResourceBundle(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DAOHelper.loadCommonResourceBundle() ";
        final boolean boolDebug;

        boolDebug = false;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getHostAtom() != null)
            && (dao.getHostInstrument().getHostAtom().getClass() != null)
            && (dao.getHostInstrument().getHostAtom().getClass().getPackage() != null)
            && (dao.getResourceBundles() != null))
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            try
                {
                final ResourceBundle bundleHelp;

                // The common help is in observatory/help/CommonHelpBundle.properties
                // which is in the Observatory-plugin.jar
                buffer.append(dao.getHostInstrument().getHostAtom().getClass().getPackage().getName());
                buffer.append(DOT);
                buffer.append(InstallationFolder.HELP.getName());
                buffer.append(DOT);
                buffer.append(ObservatoryInstrumentDAOInterface.COMMON_HELP_BUNDLE);

                bundleHelp = ResourceBundle.getBundle(buffer.toString());

                //LOGGER.debug(boolDebug, SOURCE + "Adding Resource Bundle [bundle=" + buffer + "]");
                LOGGER.debug(true, SOURCE + "Adding Common Resource Bundle [bundle=" + buffer + "]");

                // Add the bundle to the List of bundles to search for this DAO
                dao.getResourceBundles().add(bundleHelp);
                }

            catch (PatternSyntaxException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "PatternSyntaxException [bundle=" + buffer + "]");
                }

            catch (NullPointerException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "NullPointerException [bundle=" + buffer + "]");
                }

            catch (MissingResourceException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "MissingResourceException [bundle=" + buffer + "]");
                }

            catch (IllegalArgumentException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "IllegalArgumentException [bundle=" + buffer + "]");
                }
            }
        }


    /***********************************************************************************************
     * Load any context sensitive help relevant to the common superclass DAO.
     * This is intended for e.g StaribusCoreDAO.
     * Without this mechanism it is hard to see how to load the hierarchy of Resource Bundles.
     *
     * @param dao
     */

    public static void loadCommonSuperclassResourceBundle(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DAOHelper.loadCommonSuperclassResourceBundle() ";
        final boolean boolDebug;

        boolDebug = false;

        if ((dao != null)
            && (dao.getClass() != null)
            && (dao.getClass().getSuperclass() != null)
            && (dao.getResourceBundles() != null))
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            try
                {
                final String strClassname;
                final ResourceBundle bundleHelp;

                // The DAO **superclass** is always in observatory/ui/instruments/common/dao/<DAO>.class
                // The DAO help is in observatory/ui/instruments/common/help/<DAO>HelpBundle.properties
                // which are in the Observatory-plugin.jar

                strClassname = dao.getClass().getSuperclass().getName();

                buffer.append(strClassname.replaceFirst(DOT + InstallationFolder.DAO.getName() + DOT,
                                                        DOT + InstallationFolder.HELP.getName() + DOT));
                buffer.append(ObservatoryInstrumentDAOInterface.DAO_HELP_BUNDLE_SUFFIX);

                bundleHelp = ResourceBundle.getBundle(buffer.toString());

                //LOGGER.debug(boolDebug, SOURCE + "Adding Common Superclass Resource Bundle [bundle=" + buffer + "]");
                LOGGER.debug(true, SOURCE + "Adding Common Superclass Resource Bundle [bundle=" + buffer + "]");

                // Add the bundle to the List of bundles to search for this DAO
                dao.getResourceBundles().add(bundleHelp);
                }

            catch (PatternSyntaxException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "PatternSyntaxException [bundle=" + buffer + "]");
                }

            catch (NullPointerException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "NullPointerException [bundle=" + buffer + "]");
                }

            catch (MissingResourceException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "MissingResourceException [bundle=" + buffer + "]");
                }

            catch (IllegalArgumentException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "IllegalArgumentException [bundle=" + buffer + "]");
                }
            }
        }


    /***********************************************************************************************
     * Load any context sensitive help relevant to the sub-classed DAO.
     *
     * @param dao
     */

    public static void loadSubClassResourceBundle(final ObservatoryInstrumentDAOInterface dao)
        {
        final String SOURCE = "DAOHelper.loadSubClassResourceBundle() ";
        final boolean boolDebug;

        boolDebug = false;

        if ((dao != null)
            && (dao.getHostInstrument() != null)
            && (dao.getHostInstrument().getInstrument() != null)
            && (dao.getHostInstrument().getInstrument().getDAO() != null)
            && (dao.getResourceBundles() != null))
            {
            final StringBuffer buffer;

            buffer = new StringBuffer();

            try
                {
                final String strClassname;
                final ResourceBundle bundleHelp;

                strClassname = dao.getHostInstrument().getInstrument().getDAO().getDaoClassname();

                buffer.append(strClassname.replaceFirst(DOT + InstallationFolder.DAO.getName() + DOT,
                                                        DOT + InstallationFolder.HELP.getName() + DOT));
                buffer.append(ObservatoryInstrumentDAOInterface.DAO_HELP_BUNDLE_SUFFIX);

                bundleHelp = ResourceBundle.getBundle(buffer.toString());

                //LOGGER.debug(boolDebug, SOURCE + "Adding Subclass Resource Bundle [bundle=" + buffer + "]");
                LOGGER.debug(true, SOURCE + "Adding Subclass Resource Bundle [bundle=" + buffer + "]");

                // Add the bundle to the List of bundles to search for this DAO
                dao.getResourceBundles().add(bundleHelp);
                }

            catch (PatternSyntaxException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "PatternSyntaxException [bundle=" + buffer + "]");
                }

            catch (NullPointerException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "NullPointerException [bundle=" + buffer + "]");
                }

            catch (MissingResourceException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "MissingResourceException [bundle=" + buffer + "]");
                }

            catch (IllegalArgumentException exception)
                {
                // Do nothing if no valid help bundle exists, all messages will return EMPTY_STRING
                LOGGER.debug(boolDebug, SOURCE + "IllegalArgumentException [bundle=" + buffer + "]");
                }
            }
        }


    /**********************************************************************************************/
    /* Utilities                                                                                  */
    /***********************************************************************************************
     * Check for an unexpected error in Command execution.
     *
     * @param commandmessage
     * @param responsemessage
     * @param errors
     */

    public static void addErrorIfAnyNull(final CommandMessageInterface commandmessage,
                                         final ResponseMessageInterface responsemessage,
                                         final List<String> errors)
        {
        if (((commandmessage == null) || (responsemessage == null))
            && (errors != null))
            {
            errors.add(ObservatoryInstrumentInterface.COMMAND_ERROR);
            }
        }


    /***********************************************************************************************
     * Set the ResponseValue of the specified CommandType, if possible.
     *
     * @param command
     * @param responsesuccess
     */

    public static void setResponseValue(final CommandType command,
                                        final boolean responsesuccess)
        {
        if ((command != null)
            && (command.getResponse() != null))
            {
            if (responsesuccess)
                {
                command.getResponse().setValue(ResponseMessageStatus.SUCCESS.getResponseValue());
                }
            else
                {
                command.getResponse().setValue(ResponseMessageStatus.RESPONSE_NODATA);
                }
            }
        }


    /***********************************************************************************************
     * Insert a dummy ResponseValue into the supplied ResponseType.
     * All other ResponseType elements are left untouched.
     *
     * @param responsetype
     */

    public static void insertDummyResponseValue(final ResponseType responsetype)
        {
        final SchemaDataType.Enum datatypeRequired;
        final DataTypeDictionary datatypeDictionary;

        datatypeRequired = responsetype.getDataTypeName();

        // Find the DataTypeDictionary entry which corresponds to the XML schema SchemaDataType
        datatypeDictionary = DataTypeDictionary.getDataTypeDictionaryEntryForName(datatypeRequired.toString());

        // Simulate a response value
        responsetype.setValue(datatypeDictionary.getDummyValue());
        }


    /***********************************************************************************************
     * Create a dummy ResponseMessageStatus.
     * i.e. set a single bit out of the 16 available, or no bits at all.
     *
     * @return int
     */

    private static ResponseMessageStatus createDummyResponseMessageStatus()
        {
        final Random random;
        ResponseMessageStatus responseMessageStatus;

        // Randomly change the state
        random = new Random(Chronos.getSystemTime());

        // Returns a pseudorandom uniformly distributed int value
        // between 0 (inclusive) and the specified value (exclusive)
        switch (random.nextInt(17))
            {
            case  0: { responseMessageStatus = ResponseMessageStatus.SUCCESS;                   break; }
            case  1: { responseMessageStatus = ResponseMessageStatus.TIMEOUT;                   break; }
            case  2: { responseMessageStatus = ResponseMessageStatus.ABORT;                     break; }
            case  3: { responseMessageStatus = ResponseMessageStatus.PREMATURE_TERMINATION;     break; }
            case  4: { responseMessageStatus = ResponseMessageStatus.INVALID_PARAMETER;         break; }
            case  5: { responseMessageStatus = ResponseMessageStatus.INVALID_MESSAGE;           break; }
            case  6: { responseMessageStatus = ResponseMessageStatus.INVALID_COMMAND;           break; }
            case  7: { responseMessageStatus = ResponseMessageStatus.INVALID_MODULE;            break; }
            case  8: { responseMessageStatus = ResponseMessageStatus.INVALID_INSTRUMENT;        break; }
            case  9: { responseMessageStatus = ResponseMessageStatus.MODULE_DATABUS;            break; }
            case 10: { responseMessageStatus = ResponseMessageStatus.CRC_ERROR;                 break; }
            case 11: { responseMessageStatus = ResponseMessageStatus.INVALID_XML;               break; }
            case 12: { responseMessageStatus = ResponseMessageStatus.ERROR_12;                  break; }
            case 13: { responseMessageStatus = ResponseMessageStatus.ERROR_13;                  break; }
            case 14: { responseMessageStatus = ResponseMessageStatus.LOOK_AT_ME;                break; }
            case 15: { responseMessageStatus = ResponseMessageStatus.BUSY;                      break; }
            case 16: { responseMessageStatus = ResponseMessageStatus.CAPTURE_ACTIVE;            break; }

            default: { responseMessageStatus = ResponseMessageStatus.TIMEOUT; }
            }

        // Half of the time discard the above and return a SUCCESS anyway
        if (random.nextBoolean())
            {
            responseMessageStatus = ResponseMessageStatus.SUCCESS;
            }

        return (responseMessageStatus);
        }


    /***********************************************************************************************
     * Set random bits in the status word, or no bits at all.
     *
     * @return int
     */

    public static int createDummyResponseMessageStatusBitMask()
        {
        //return (createDummyResponseMessageStatus().getBitMask());
        return (setRandomStatusBits());
        }


    /***********************************************************************************************
     * Set random bits in the status word, or no bits at all.
     *
     * @return int
     */

    private static int setRandomStatusBits()
        {
        final Random random;
        int intStatus;

        // Half of the time return a SUCCESS anyway
        intStatus = 0;

        // Randomly change the state
        random = new Random(Chronos.getSystemTime());

        // Randomly set up to four error bits
        if (random.nextBoolean())
            {
            if (random.nextBoolean())
                {
                // Returns a pseudorandom uniformly distributed int value
                // between 0 (inclusive) and the specified value (exclusive)
                switch (random.nextInt(4))
                    {
                    case 0: { intStatus = intStatus | 0x8000; break; }
                    case 1: { intStatus = intStatus | 0x4000; break; }
                    case 2: { intStatus = intStatus | 0x2000; break; }
                    case 3: { intStatus = intStatus | 0x1000; break; }
                    default:{ intStatus = intStatus | 0x8000; break; }
                    }
                }

            if (random.nextBoolean())
                {
                switch (random.nextInt(4))
                    {
                    case 0: { intStatus = intStatus | 0x0800; break; }
                    case 1: { intStatus = intStatus | 0x0400; break; }
                    case 2: { intStatus = intStatus | 0x0200; break; }
                    case 3: { intStatus = intStatus | 0x0100; break; }
                    default:{ intStatus = intStatus | 0x0800; break; }
                    }
                }

            if (random.nextBoolean())
                {
                switch (random.nextInt(4))
                    {
                    case 0: { intStatus = intStatus | 0x0080; break; }
                    case 1: { intStatus = intStatus | 0x0040; break; }
                    case 2: { intStatus = intStatus | 0x0020; break; }
                    case 3: { intStatus = intStatus | 0x0010; break; }
                    default:{ intStatus = intStatus | 0x0080; break; }
                    }
                }

            if (random.nextBoolean())
                {
                switch (random.nextInt(4))
                    {
                    case 0: { intStatus = intStatus | 0x0008; break; }
                    case 1: { intStatus = intStatus | 0x0004; break; }
                    case 2: { intStatus = intStatus | 0x0002; break; }
                    case 3: { intStatus = intStatus | 0x0001; break; }
                    default:{ intStatus = intStatus | 0x0008; break; }
                    }
                }
            }

        return (intStatus);
        }
    }
