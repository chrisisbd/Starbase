package org.lmn.fc.model.logging;


/***************************************************************************************************
 * Enumeration of EventStatus.
 */

public enum EventStatus
    {
    FATAL       (0, "Fatal",    "messages/MessageFatal.gif",        "An Error has occurred"),
    INFO        (1, "Info",     "messages/info.png",                "Information only"),
    SUCCESS     (2, "Success",  "messages/success.png",             "Operation successful"),
    ABORT       (3, "Abort",    "messages/abort.png",               "Operation aborted by User"),
    TIMEOUT     (4, "Timeout",  "messages/timeout.png",             "Operation timed out"),
    WARNING     (5, "Warning",  "messages/MessageWarning.gif",      "Warning!"),
    QUESTION    (6, "Question", "messages/MessageQuestion.gif",     "Question"),
    PLAIN       (7, "Plain",    "messages/MessagePlain.gif",        "Message only"),
    SILENT      (8, "Silent",   "messages/MessageWarning.gif",      "Warning!");


    private final int intID;
    private final String strStatusName;
    private final String strIconFilename;
    private final String strTooltip;


    /***********************************************************************************************
     * Get the EventStatus corresponding to the specified ID.
     * Return NULL if the EventStatus ID is not found.
     *
     * @param id
     *
     * @return EventStatus
     */

    public static EventStatus getEventStatusForID(final int id)
        {
        final EventStatus[] arrayStatus;
        EventStatus statusForID;
        boolean boolFoundIt;

        arrayStatus = values();
        statusForID = null;
        boolFoundIt = false;

        for (int i = 0;
             (!boolFoundIt) && (i < arrayStatus.length);
             i++)
            {
            final EventStatus status;

            status = arrayStatus[i];

            if (status.getStatusID() == id)
                {
                statusForID = status;
                boolFoundIt = true;
                }
            }

        return (statusForID);
        }


    /***********************************************************************************************
     * Privately construct an EventStatus.
     *
     * @param id
     * @param status
     * @param icon
     * @param tooltip
     */

    private EventStatus(final int id,
                        final String status,
                        final String icon,
                        final String tooltip)
        {
        intID = id;
        strStatusName = status;
        strIconFilename = icon;
        strTooltip = tooltip;
        }


    /***********************************************************************************************
     * Get the EventStatus ID.
     *
     * @return int
     */

    public int getStatusID()
        {
        return (this.intID);
        }


    /***********************************************************************************************
     * Get the EventStatus Name.
     *
     * @return String
     */

    public String getStatusName()
        {
        return(this.strStatusName);
        }


    /***********************************************************************************************
     * Get the EventStatus IconFilename.
     *
     * @return String
     */

    public String getIconFilename()
        {
        return (this.strIconFilename);
        }


    /***********************************************************************************************
     * Get the EventStatus Tooltip.
     *
     * @return String
     */

    public String getTooltip()
        {
        return (this.strTooltip);
        }


    /***********************************************************************************************
      * Get the EventStatus Name.
     *
      * @return String
      */

     public String toString()
        {
        return (this.strStatusName);
        }
    }
