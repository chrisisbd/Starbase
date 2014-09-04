package org.lmn.fc.model.logging.impl;

import org.apache.xmlbeans.XmlObject;
import org.lmn.fc.common.constants.*;
import org.lmn.fc.common.utilities.time.ChronosHelper;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.model.logging.EventLogInterface;
import org.lmn.fc.model.logging.EventStatus;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.RegistryModelUtilities;
import org.lmn.fc.model.xmlbeans.events.Event;
import org.lmn.fc.model.xmlbeans.events.EventsDocument;

import javax.swing.*;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***********************************************************************************************
 * The Framework EventLog.
 *
 * Check <b>http://wiki.apache.org/xmlbeans/XmlBeansFaq#whatJars</b>
 * to ensure that you have XmlBeans and XPath set up correctly!
 *
 * What jars do I need on the classpath to use XMLBeans?
 * The obvious one is xbean.jar, which contains the XMLBeans code.
 * For XMLBeans V1, this is all you need, but for XMLBeans 2, the JSR173 API jar is also required,
 * since it contains classes that XMLBeans depends on. For XMLBeans 2.0.0,
 * the name of the jar is jsr173_api.jar and for XMLBeans 2.1.0, it's jsr173_1.0_api.jar.
 * In both cases, the jar comes with the XMLBeans binary distribution (for source distributions,
 * the build process will download it and place it in the build/lib directory.
 * If XPath/XQuery support is required, the xbean_xpath.jar contains the XPath "glue" code
 * and needs to be included on the classpath. Then, depending on the version of XMLBeans in use,
 * the following are also required: jaxen.jar for XMLBeans V1 (only supports XPath, no XQuery),
 * saxon8.jar for XMLBeans V2.0.0 and V2.1.0 (the only version supported is Saxon 8.1.1),
 * saxon8.jar and saxon8-dom.jar for XMLBeans built from SVN head (this supports Saxon >= 8.6.1).
 * In addition to those, compiling Schemas using the scomp script requires tools.jar from the JDK
 * and resolver.jar from Apache xml-commons (the latter one only in case support for
 * OASIS XML catalogs for resolving external entities is needed).
 * We make another jar available, xmlpublic.jar, which contains the classes in the
 * "org.apache.xmlbeans" package. If your code can be compiled with xmlpublic.jar on the classpath
 * instead of the full xbean.jar, it means that your code uses only "public",
 * supported APIs and will likely not need any modification if you need to upgrade to a newer XMLBeans version.
 */

public final class EventLog implements EventLogInterface,
                                       FrameworkConstants,
                                       FrameworkStrings,
                                       FrameworkSingletons,
                                       FrameworkXpath,
                                       FrameworkMetadata,
                                       ResourceKeys
    {
    private volatile static EventLogInterface EVENTLOG_INSTANCE;

    private static final int REPORT_COLUMN_COUNT = 6;

    // The EventLog
    private EventsDocument docEvents;
    private boolean boolUpdated;


    /***********************************************************************************************
     * The EventLog is a Singleton!
     *
     * @return EventLog
     */

    public static EventLogInterface getInstance()
        {
        if (EVENTLOG_INSTANCE == null)
            {
            synchronized (EventLog.class)
                {
                if (EVENTLOG_INSTANCE == null)
                    {
                    EVENTLOG_INSTANCE = new EventLog();
                    }
                }
            }

        return (EVENTLOG_INSTANCE);
        }


    /***********************************************************************************************
     * Privately construct the EventLog.
     */

    private EventLog()
        {
        this.docEvents = EventsDocument.Factory.newInstance();
        this.docEvents.addNewEvents();
        this.boolUpdated = false;
        }


    /**********************************************************************************************/
    /* Events                                                                                     */
    /***********************************************************************************************
     * Add the specified Event to the Registry EventLog.
     *
     * @param frameworkid
     * @param atomid
     * @param taskid
     * @param classname
     * @param message
     * @param status
     */

    public void logEvent(final long frameworkid,
                         final long atomid,
                         final long taskid,
                         final String classname,
                         final String message,
                         final EventStatus status)
        {
        final Event event;

        if ((classname != null)
            && (message != null)
            && (status != null))
            {
            final List<Event> listEvents;
            long longEventID;

            // Re-initialise the EventLog if it has disappeared!
            if ((getEventLog() == null)
                || (getEventLog().getEvents() == null))
                {
                setEventLog(EventsDocument.Factory.newInstance());
                getEventLog().addNewEvents();
                }

            // Get the EventID of the last Event added, if any
            listEvents = getEventLog().getEvents().getEventList();

            if ((listEvents != null)
                && (listEvents.size() > 0))
                {
                longEventID = listEvents.get(listEvents.size()-1).getEventID();
                longEventID++;
                }
            else
                {
                longEventID = 0;
                }

            event = getEventLog().getEvents().addNewEvent();
            event.setEventID(longEventID);
            event.setEventDate(new GregorianCalendar());
            event.setEventTime(new GregorianCalendar());
            event.setFrameworkID(frameworkid);
            event.setAtomID(atomid);
            event.setTaskID(taskid);
            event.setClassName(classname);
            event.setMessage(message);
            event.setStatus((short)status.getStatusID());

            // Mark the EventLog as being updated
            setUpdated(true);
            }
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log.
     */

    public void deleteAllEvents()
        {
        setEventLog(EventsDocument.Factory.newInstance());
        getEventLog().addNewEvents();

        // Mark the EventLog as being updated
        setUpdated(true);
        }


    /***********************************************************************************************
     * Delete all Events in the Event Log related to the specified Atom.
     *
     * @param atom
     */

    public void deleteAtomEvents(final AtomPlugin atom)
        {
//        if ((atom != null)
//            && (getEventLogFragment() != null)
//            && (getEventLogFragment().getEvents() != null)
//            && (getEventLogFragment().getEvents().getEventList() != null)
//            && (XmlBeansUtilities.isValidXml(getEventLogFragment())))
//            {
//            final XmlObject[] events;
//
//            // Select only those Events originating from the specified Atom
//            events = getEventLogFragment().getEvents().execQuery(XPATH_DELETE_ATOM_EVENTS);
//
//            for (int i = 0; i < events.length; i++)
//                {
//                // Todo !!!!!!!!!  Delete all Events in the Event Log related to the specified Atom ONLY.
//                final Event event;
//
//                event = (Event)events[i];
//
//                }
//            }

        // Mark the EventLog as being updated
        setUpdated(true);
        }


    /***********************************************************************************************
     * Truncate the EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param length
     */

    public void truncateEventLog(final int length)
        {
        if ((getEventLog() != null)
            && (getEventLog().getEvents() != null)
            && (getEventLog().getEvents().getEventList() != null)
            && (XmlBeansUtilities.isValidXml(getEventLog())))
            {
            final int intChoice;
            final String [] strMessage =
                {
                "Are you sure that you wish to truncate the Event Log",
                "to leave" + SPACE + length + SPACE + "items?"
                };

            intChoice = JOptionPane.showOptionDialog(null,
                                                     strMessage,
                                                     "Truncate Event Log",
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     null,
                                                     null);

            while ((intChoice == JOptionPane.YES_OPTION)
                && (getEventLog().getEvents().getEventList() != null)
                && (getEventLog().getEvents().getEventList().size() > length))
                {
                getEventLog().getEvents().getEventList().remove(0);
                }

            // Mark the EventLog as updated
            setUpdated(true);
            }
        }


    /***********************************************************************************************
     * Truncate the Atom EventLog, leaving (at most) the specified number of Events in the Log.
     *
     * @param length
     */

    public void truncateAtomEventLog(final AtomPlugin atom, final int length)
        {
        if ((getEventLog() != null)
            && (getEventLog().getEvents() != null)
            && (getEventLog().getEvents().getEventList() != null)
            && (XmlBeansUtilities.isValidXml(getEventLog())))
            {
            final int intChoice;
            final String [] strMessage =
                {
                "Are you sure that you wish to truncate the Plugin Event Log",
                "to leave" + SPACE + length + SPACE + "items?"
                };

            intChoice = JOptionPane.showOptionDialog(null,
                                                     strMessage,
                                                     "Truncate Plugin Event Log",
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     null,
                                                     null);

            while ((intChoice == JOptionPane.YES_OPTION)
                && (getEventLog().getEvents().getEventList() != null)
                && (getEventLog().getEvents().getEventList().size() > length))
                {
                // ToDo restrict truncation to Atom Events only
                getEventLog().getEvents().getEventList().remove(0);
                }

            // Mark the EventLog as updated
            setUpdated(true);
            }
        }


    /***********************************************************************************************
     * Select all events in the Event Log.
     * Return a Vector suitable for an EventReport.
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getAllEventsReport()
        {
        final Vector<Vector> vecEvents;

        vecEvents = new Vector<Vector>(1);

        if ((getEventLog() != null)
            && (getEventLog().getEvents() != null)
            && (getEventLog().getEvents().getEventList() != null)
            && (XmlBeansUtilities.isValidXml(getEventLog())))
            {
            final List<Event> listEvents;

            listEvents = getEventLog().getEvents().getEventList();

            for (int i = listEvents.size()-1; i >= 0; i--)
                {
                final Event event;
                final Vector<Object> vecRow;
                final EventStatus statusEvent;
                final ImageIcon imageIcon;

                event = listEvents.get(i);
                vecRow = new Vector<Object>(REPORT_COLUMN_COUNT);

                statusEvent = EventStatus.getEventStatusForID(event.getStatus());
                imageIcon = RegistryModelUtilities.getCommonIcon(statusEvent.getIconFilename());

                vecRow.add(imageIcon);
                vecRow.add(ChronosHelper.toDateString(event.getEventDate()));
                vecRow.add(ChronosHelper.toTimeString(event.getEventTime()));
                vecRow.add(event.getMessage());
                vecRow.add(event.getClassName());
                vecRow.add(statusEvent.getTooltip());

                vecEvents.add(vecRow);
                }
            }

        return (vecEvents);
        }


    /***********************************************************************************************
     * Select all events relevant to the specified Atom.
     * Return a Vector suitable for a Report.
     *
     * @param atom
     *
     * @return Vector<Vector>
     */

    public Vector<Vector> getAtomEventsReport(final AtomPlugin atom)
        {
        final Vector<Vector> vecEvents;

        vecEvents = new Vector<Vector>(1);

        if ((atom != null)
            && (atom.validatePlugin())
            && (getEventLog() != null)
            && (getEventLog().getEvents() != null)
            && (getEventLog().getEvents().getEventList() != null)
            && (XmlBeansUtilities.isValidXml(getEventLog())))
            {
            final StringBuffer expression;
            final XmlObject[] events;

            expression = new StringBuffer();
            expression.append(XPATH_EVENTS_NAMESPACE);

            if ((atom.getParentAtom() == null)
                && (REGISTRY.getFramework().equals(atom)))
                {
                // We are dealing with the Framework itself, so ignore the AtomID
                expression.append(XPATH_EVENTS_FOR_FRAMEWORK_ID);
                expression.append(Long.toString(atom.getID()));
                expression.append(XPATH_QUOTE_TERMINATOR);
                }
            else
                {
                // Find the Framework ID directly from the Registry
                expression.append(XPATH_EVENTS_FOR_FRAMEWORK_ID);
                expression.append(Long.toString(REGISTRY.getFramework().getID()));
                expression.append(XPATH_EVENTS_FOR_FRAMEWORK_AND_ATOM);
                expression.append(Long.toString(atom.getID()));
                expression.append(XPATH_QUOTE_TERMINATOR);
                }

            //LOGGER.debug("XmlPath Query {" + expression + "}");

            // Select only those Events originating from the specified Atom in this Framework
            events = getEventLog().getEvents().selectPath(expression.toString());

            // Remember that empty selections return as a *single* XmlObject (i.e. not an array)
            if ((events != null)
                && (events instanceof Event[]))
                {
                // ToDo WARNING! Does the Xpath return a set in the correct order for the following?
                for (int i = events.length-1; i >= 0; i--)
                    {
                    final Event event;
                    final Vector<Object> vecRow;
                    final EventStatus statusEvent;
                    final ImageIcon imageIcon;

                    event = (Event)events[i];
                    vecRow = new Vector<Object>(REPORT_COLUMN_COUNT);

                    statusEvent = EventStatus.getEventStatusForID(event.getStatus());
                    imageIcon = RegistryModelUtilities.getCommonIcon(statusEvent.getIconFilename());

                    vecRow.add(imageIcon);
                    vecRow.add(ChronosHelper.toDateString(event.getEventDate()));
                    vecRow.add(ChronosHelper.toTimeString(event.getEventTime()));
                    vecRow.add(event.getMessage());
                    vecRow.add(event.getClassName());
                    vecRow.add(statusEvent.getTooltip());

                    vecEvents.add(vecRow);
                    }
                }
            }

        return (vecEvents);
        }


    /***********************************************************************************************
     * Get the Registry EventsDocument.
     *
     * @return EventsDocument
     */

    public EventsDocument getEventLog()
        {
        return (this.docEvents);
        }


    /***********************************************************************************************
     * Set the Registry EventsDocument.
     *
     * @param doc
     */

    public void setEventLog(final EventsDocument doc)
        {
        this.docEvents = doc;
        }


    /***********************************************************************************************
     * Get a flag indicating if the EventLog has been updated.
     *
     * @return boolean
     */

    public boolean isUpdated()
        {
        return (this.boolUpdated);
        }


    /***********************************************************************************************
     * Set the EventLog update status.
     *
     * @param flag
     */

    public void setUpdated(final boolean flag)
        {
        this.boolUpdated = true;
        }


    /***********************************************************************************************
     * Show the EventLog for debugging.
     *
     * @param log
     */

    public void showDebugEventLog(final Vector<Vector> log)
        {
        LOGGER.debug("Event Log");

        if ((log != null)
            && (!log.isEmpty()))
            {
            final Iterator iterLog;

            iterLog = log.iterator();

            while (iterLog.hasNext())
                {
                final Vector<Object> vecRow = (Vector<Object>) iterLog.next();

                if ((vecRow != null)
                    && (!vecRow.isEmpty())
                    && (vecRow.size() == REPORT_COLUMN_COUNT))
                    {
                    // Ignore the Icon
                    LOGGER.debug(INDENT + (vecRow.get(1)));
                    LOGGER.debug(INDENT + (vecRow.get(2)));
                    LOGGER.debug(INDENT + (vecRow.get(3)));
                    LOGGER.debug(INDENT + (vecRow.get(4)));
                    LOGGER.debug(INDENT + (vecRow.get(5)));
                    LOGGER.debug("\n");
                    }
                else
                    {
                    LOGGER.debug(INDENT + "Report Row is incorrect");
                    }
                }
            }
        else
            {
            LOGGER.debug(INDENT + "No logged Events");
            }
        }
    }
