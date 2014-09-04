package org.lmn.fc.common.net.telnet;

/*
 * ClassRegistry version 1.0
 * ~~~~~~~~~~~~~
 *
 * A set of classes for communicating between objects that exist in
 * different applets in different frames, windows, or documents.
 *
 * Written by Dianne Hackborn
 *
 * The design and implementation of WebTerm are available for
 * royalty-free adoption and use for non-commercial purposes, by any
 * public or private organization.  Copyright is retained by the
 * Northwest Alliance for Computational Science and Engineering and
 * Oregon State University.  Redistribution of any part of WebTerm or
 * any derivative works must include this notice.
 *
 * All Rights Reserved.
 *
 * Please address correspondence to nacse-questions@nacse.org.
 *
 * ----------------------------------------------------------------------
 *
 * Known Bugs
 * ~~~~~~~~~~
 *
 * ----------------------------------------------------------------------
 *
 * History
 * ~~~~~~~
 *
 * 1.0: First release.
 *
 */



/* This is the interface that an object must implement if it is to
   add itself to a ClassRegistry. */

public interface ClassConnection
    {

    /* This method is called when another object wishes to send a message
       a registered object.  Note that this method is executed in the
       context of the *calling* class, which is almost certainly a different
       thread than the one normally executing in your object.  Be sure you
       multi-thread protect any data this method accesses through proper use
       of the "synchronized" keyword.

       There are currently no standard commands defined, except:

       null should generally be treated as a no-op.
       "send" is the default command used by the RegistryButton.
       */

    public Object doCommand(String cmd, Object data);
    }
