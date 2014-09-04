package org.lmn.fc.model.plugins;

/***************************************************************************************************
 * An Enumeration of Plugin States.
 */

public enum PluginState
    {
    STOPPED (3,
             "Stopped",
             "The Plugin is in the stopped state",
             "stopped.png"),

    RUNNING (4,
              "Running",
              "The Plugin is in the running state",
              "running.png");

    private final int intState;
    private final String strStatus;
    private final String strTooltip;
    private final String strIconFilename;


    /***********************************************************************************************
     * Construct the Plugin States.
     *
     * @param state
     * @param status
     * @param tooltip
     * @param iconfilename
     */

    private PluginState(final int state,
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
