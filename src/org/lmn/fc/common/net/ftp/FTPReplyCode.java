package org.lmn.fc.common.net.ftp;


public interface FTPReplyCode
    {

    String VALID_REPLY = "226";

//    110 Restart marker reply.
//    120 Service ready in nnn minutes.
//    125 Data connection already open; transfer starting.
//    150 File status okay; about to open data connection.
//    200 Command okay.
//    202 Command not implemented, superfluous at this site.
//    211 System status, or system help reply.
//    212 Directory status.
//    213 File status.
//    214 Help message.
//    215 NAME system type.
//    220 Service ready for new user.
//    221 Service closing control connection.
//    225 Data connection open; no transfer in progress.
//    226 Closing data connection.
//    227 Entering Passive Mode <h1,h2,h3,h4,p1,p2>.
//    228 Entering Long Passive Mode.
//    229 Extended Passive Mode Entered.
//    230 User logged in, proceed.
//    250 Requested file action okay, completed.
//    257 "PATHNAME" created.

//    331 User name okay, need password.
//    332 Need account for login.
//    350 Requested file action pending further information.
//    421 Service not available, closing control connection.
//    425 Can't open data connection.
//    426 Connection closed; transfer aborted.
//    450 Requested file action not taken.
//    451 Requested action aborted. Local error in processing.
//    452 Requested action not taken.
//    500 Syntax error, command unrecognized.
//    501 Syntax error in parameters or arguments.
//    502 Command not implemented.
//    503 Bad sequence of commands.
//    504 Command not implemented for that parameter.
//    521 Supported address families are <af1, .., afn>
//    522 Protocol not supported.
//    530 Not logged in.
//    532 Need account for storing files.
//    550 Requested action not taken.
//    551 Requested action aborted. Page type unknown.
//    552 Requested file action aborted.
//    553 Requested action not taken.
//    554 Requested action not taken: invalid REST parameter.
//    555 Requested action not taken: type or stru mismatch
    }
