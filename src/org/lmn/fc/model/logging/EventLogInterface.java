package org.lmn.fc.model.logging;

import org.lmn.fc.model.xmlbeans.events.EventsDocument;
import org.lmn.fc.model.plugins.AtomPlugin;

import java.util.Vector;


public interface EventLogInterface
    {
    void logEvent(long frameworkid,
                  long atomid,
                  long taskid,
                  String classname,
                  String message,
                  EventStatus status);

    void deleteAllEvents();

    void deleteAtomEvents(AtomPlugin atom);

    void truncateEventLog(int length);

    void truncateAtomEventLog(AtomPlugin atom, int length);

    Vector<Vector> getAllEventsReport();

    Vector<Vector> getAtomEventsReport(AtomPlugin atom);

    EventsDocument getEventLog();

    void setEventLog(EventsDocument doc);

    boolean isUpdated();

    void setUpdated(boolean flag);

    void showDebugEventLog(Vector<Vector> log);
    }
