package org.lmn.fc.model.tasks;


/***************************************************************************************************
 * An Enumeration of Task States.
 */

public enum TaskState
    {
    CREATED (0,
             "Created",
             "The Task is in the created state",
             "taskstates/TaskCreated.jpg"),

    INITIALISED (1,
                 "Initialised",
                 "The Task is in the initialised state",
                 "taskstates/TaskInitialised.jpg"),

    IDLE (2,
          "Idle",
          "The Task is in the idle state",
          "taskstates/TaskIdle.jpg"),

    RUNNING (3,
             "Running",
             "The Task is in the running state",
             "taskstates/TaskRunning.jpg"),

    SHUTDOWN (4,
              "Shutdown",
              "The Task is in the shutdown state",
              "taskstates/TaskShutdown.jpg");

    private final int intState;
    private final String strStatus;
    private final String strTooltip;
    private final String strIconFilename;


    /***********************************************************************************************
     * Construct the Task States.
     *
     * @param state
     * @param status
     * @param tooltip
     * @param iconfilename
     */

    private TaskState(final int state,
                      final String status,
                      final String tooltip,
                      final String iconfilename)
        {
        intState = state;
        strStatus = status;
        strTooltip = tooltip;
        strIconFilename = iconfilename;
        }


    /***********************************************************************************************
     *
     * @return int
     */

    public int getType()
        {
        return (this.intState);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getStatus()
        {
        return (this.strStatus);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getTooltip()
        {
        return (this.strTooltip);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String getIconFilename()
        {
        return (this.strIconFilename);
        }


    /***********************************************************************************************
     *
     * @return String
     */

    public String toString()
        {
        return (getStatus());
        }
    }
