//--------------------------------------------------------------------------------------------------
// Revision History
//
//  24-09-03    LMN created file from StringData
//  29-09-03    LMN tidying up...
//  20-10-04    LMN adding ExecutionCount & ExecutionTime
//  09-11-04    LMN added Select.Framework.LoadAtStart to setBootstrapQueries()
//  14-04-06    LMN rewriting for XMLBeans
//  24-07-06    LMN removed SQL Trace to RegistryManager
//
//--------------------------------------------------------------------------------------------------

package org.lmn.fc.model.resources.impl;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.utilities.time.Chronos;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.database.DatabasePlugin;
import org.lmn.fc.model.dao.DataStore;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.resources.QueryPlugin;
import org.lmn.fc.model.xmlbeans.queries.QueryResource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;


/***************************************************************************************************
 * QueryData.
 */

public final class QueryData extends ResourceData
                             implements QueryPlugin
    {
    private static final long VERSION_ID = 6267291227866329293L;

    // Properties not available in RootData
    private long longExecutionCount;
    private long longExecutionTime;


    /**********************************************************************************************/
    /* Constructors                                                                               */
    /***********************************************************************************************
     * Construct a QueryData from an QueryResource XMLBean.
     *
     * @param host
     * @param query
     * @param language
     */

    public QueryData(final AtomPlugin host,
                     final QueryResource query,
                     final String language)
        {
        super(VERSION_ID, host, query, language);

        // Check that we know enough to try to import this Query
        if ((host != null)
            && (query != null)
            && (XmlBeansUtilities.isValidXml(query))
            && ((getXml()).getStatements() != null)
            && ((getXml()).getStatements().getStatementList() != null))
            {
            final List<String> listStatements;
            boolean boolValidStatement;

            // These setters must not use the XMLBean data!
            setUpdateAllowed(false);
            setUpdated(false);
            setInstalled(false);
            setClassFound(false);
            setResourceKey(EMPTY_STRING);
            setName(EMPTY_STRING);
            setIconFilename(EMPTY_STRING);

            listStatements = (getXml()).getStatements().getStatementList();
            boolValidStatement = true;

            for (int i = 0; i < listStatements.size(); i++)
                {
                final Object objStatement;

                if (listStatements.get(i) != null)
                    {
                    objStatement = BEAN_FACTORY_XML.validateResourceDataType(QUERY_CLASSNAME,
                                                                        listStatements.get(i));
                    if (objStatement == null)
                        {
                        // We are unable to instantiate the Statement with the correct DataType
                        boolValidStatement = false;
                        }
                    }
                else
                    {
                    // A null item in the List means that it must be invalid!
                    boolValidStatement = false;
                    }
                }

            if (boolValidStatement)
                {
                final StringBuffer key;

                key = new StringBuffer();

                // Initialise the ResourceKey with the host's ResourceKey
                // which always ends with DELIMITER_PATH
                key.append(host.getResourceKey());
                key.append(createResourceKey(query.getResourceKey().getKeyList()));
                setResourceKey(key.toString());

                // The Resource is the List of SQL Statements
                setResource((getXml()).getStatements().getStatementList());
                setName(getResourceKey());
                setInstalled(true);
                setClassFound(true);
                setIconFilename(QUERY_ICON);
                setUpdateAllowed(true);
                setUpdated(false);
                }
            else
                {
                LOGGER.error("QueryData found an invalid Statement");
                }
            }
        else
            {
            LOGGER.error(EXCEPTION_PARAMETER_INVALID);
            }
        }


    /**********************************************************************************************/
    /* Instance Methods                                                                           */
    /***********************************************************************************************
     * Get the ResourceKey List.
     *
     * @return String
     */

    public final List<String> getResourceKeys()
        {
        return (getXml().getResourceKey().getKeyList());
        }


    /***********************************************************************************************
     * Set the ResourceKey List.
     *
     * @param keys
     */

    public final void setResourceKeys(final List<String> keys)
        {
        final QueryResource.ResourceKey resourceKey;

        resourceKey = QueryResource.ResourceKey.Factory.newInstance();
        resourceKey.setKeyArray((String[])keys.toArray());
        getXml().setResourceKey(resourceKey);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Language ISO code.
     *
     * @return String
     */

    public final String getISOLanguageCode()
        {
        return (LanguagePlugin.DEFAULT_LANGUAGE);
        }


    /***********************************************************************************************
     * Set the Language ISO code.
     *
     * @param code
     */

    public final void setISOLanguageCode(final String code)
        {
        }


    /***********************************************************************************************
     * Get the DataTypeClassName field
     *
     * @return String
     */

    public final String getDataType()
        {
        return (QUERY_CLASSNAME);
        }


    /***********************************************************************************************
     * Set the DataTypeClassName field
     *
     * @param classname
     */

    public final void setDataType(final String classname)
        {
        }



     /**********************************************************************************************
      * Get the class name of the ResourceEditor.
      *
      * @return String
      */

     public final String getEditorClassname()
         {
         return ((getXml()).getEditorClassname());
         }


     /**********************************************************************************************
      * Set the class name of the ResourceEditor.
      *
      * @param classname
      */

     public final void setEditorClassname(final String classname)
         {
         (getXml()).setEditorClassname(classname);
         updateRoot();
         }


    /***********************************************************************************************
     * Get the Editable field.
     *
     * @return boolean
     */

    public final boolean isEditable()
        {
        return ((getXml()).getEditable());
        }


    /***********************************************************************************************
     * Set the Editable field.
     *
     * @param editable
     */

    public final void setEditable(final boolean editable)
        {
        (getXml()).setEditable(editable);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Description field
     *
     * @return String
     */

    public String getDescription()
        {
        return ((getXml()).getDescription());
        }


    /***********************************************************************************************
     * Set the Description field
     *
     * @param description
     */

    public final void setDescription(final String description)
        {
        (getXml()).setDescription(description);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the DateCreated field.
     *
     * @return Date
     */

    public final GregorianCalendar getCreatedDate()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getCreatedDate() != null)
            {
            calendar.setTimeInMillis(getXml().getCreatedDate().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the DateCreated field.
     *
     * @param calendar
     */

    public final void setCreatedDate(final GregorianCalendar calendar)
        {
        getXml().setCreatedDate(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the TimeCreated field.
     *
     * @return Time
     */

    public final GregorianCalendar getCreatedTime()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getCreatedTime() != null)
            {
            calendar.setTimeInMillis(getXml().getCreatedTime().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the TimeCreated field.
     *
     * @param calendar
     */

    public final void setCreatedTime(final GregorianCalendar calendar)
        {
        getXml().setCreatedTime(calendar);
        updateRoot();
        }


    /***********************************************************************************************
     * Get the DateModified field.
     *
     * @return Date
     */

    public final GregorianCalendar getModifiedDate()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getModifiedDate() != null)
            {
            calendar.setTimeInMillis(getXml().getModifiedDate().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the DateModified field.
     *
     * @param calendar
     */

    public final void setModifiedDate(final GregorianCalendar calendar)
        {
        // Do not mark the RootData as updated if this item changes!
        getXml().setModifiedDate(calendar);
        }


    /***********************************************************************************************
     * Get the TimeModified field.
     *
     * @return Time
     */

    public final GregorianCalendar getModifiedTime()
        {
        final GregorianCalendar calendar;

        calendar = getFrameworkCalendar();

        if (getXml().getModifiedTime() != null)
            {
            calendar.setTimeInMillis(getXml().getModifiedTime().getTimeInMillis());
            }

        return (calendar);
        }


    /***********************************************************************************************
     * Set the TimeModified field.
     *
     * @param calendar
     */

    public final void setModifiedTime(final GregorianCalendar calendar)
        {
        // Do not mark the RootData as updated if this item changes!
        getXml().setModifiedTime(calendar);
        }


    /***********************************************************************************************
     * Fully debug the QueryData.
     */

    public void showDebugData()
        {
        if (getDebugMode())
            {
            LOGGER.log("Query");
            LOGGER.log(INDENT + "[getID=" + getID() + "]");
            LOGGER.log(INDENT + "[ResourceKeys]");
            final Iterator iterKeys = getResourceKeys().iterator();

            while (iterKeys.hasNext())
                {
                LOGGER.log(INDENT + INDENT + "[Key=" + iterKeys.next() + "]");
                }

            LOGGER.log(INDENT + "[getPathname=" + getPathname() + "]");
            LOGGER.log(INDENT + "[getName=" + getName() + "]");
            LOGGER.log(INDENT + "[getResource=" + getResource() + "]");
            LOGGER.log(INDENT + "[getChannelDataType=" + getDataType() + "]");
            LOGGER.log(INDENT + "[getIconFilename=" + getIconFilename() + "]");
            LOGGER.log(INDENT + "[getISOLanguageCode=" + getISOLanguageCode() + "]");
            LOGGER.log(INDENT + "[isInstalled=" + isInstalled() + "]");
            LOGGER.log(INDENT + "[isEditable=" + isEditable() + "]");
            LOGGER.log(INDENT + "[isUpdated=" + isUpdated() + "]");
            LOGGER.log(INDENT + "[isUpdateAllowed=" + isUpdateAllowed() + "]");
            LOGGER.log(INDENT + "[getExecutionCount=" + getExecutionCount() + "]");
            LOGGER.log(INDENT + "[getExecutionTime=" + getExecutionTime() + "]");
            LOGGER.log(INDENT + "[getDescription=" + getDescription() + "]");
            LOGGER.log(INDENT + "[getDebugMode=" + getDebugMode() + "]");

            LOGGER.log(INDENT + "[getCreatedDate=" + getCreatedDate() + "]");
            LOGGER.log(INDENT + "[getCreatedTime=" + getCreatedTime() + "]");
            LOGGER.log(INDENT + "[getModifiedDate=" + getModifiedDate() + "]");
            LOGGER.log(INDENT + "[getModifiedTime=" + getModifiedTime() + "]");
            }
        }


    /***********************************************************************************************
     * Get the List of SQL Statements for all supported DataStores.
     *
     * @return List<String>
     *
     * @throws SQLException
     */

    public List<String> getStatements() throws SQLException
        {
        if ((getResource() == null)
            || (!(getResource() instanceof List)))
            {
            throw new SQLException(EXCEPTION_PARAMETER_NULL);
            }

        return ((List<String>)getResource());
        }


    /***********************************************************************************************
     * Get the SQL Statement for the specified DataStore.
     *
     * @param store
     *
     * @return String
     *
     * @throws SQLException
     */

    public String getStatement(final DataStore store) throws SQLException
        {
        if ((store == null)
            || (getStatements() == null)
            || (store.getTypeID() >= getStatements().size())
            || (getStatements().get(store.getTypeID()) == null))
            {
            throw new SQLException(EXCEPTION_PARAMETER_NULL);
            }

//        System.out.println("get from hashtable for store " + store.getName());
//        System.out.println("sql={" + getStatements().get(store.getTypeID()) + "}");
        return (getStatements().get(store.getTypeID()));
        }


    /***********************************************************************************************
     * Get the PreparedStatement for the DatabasePlugin connection.
     *
     * @return PreparedStatement
     *
     * @throws SQLException
     */

    public final PreparedStatement getPreparedStatement(final DatabasePlugin database,
                                                        final DataStore store) throws SQLException
        {
        final PreparedStatement psQuery;

        if ((store == null)
            || (!store.isAvailable())
            || (database == null)
            || (database.getDatabaseOptions() == null)
            || (database.getDatabaseOptions().getDataStore() == null)
            || (!database.getDatabaseOptions().getDataStore().equals(store))
            || (database.getConnection() == null)
            || (database.getConnection().isClosed()))
            {
            throw new SQLException(EXCEPTION_PARAMETER_NULL);
            }

//        System.out.println("getting real statement for store=" + store.getName());
        psQuery = database.getConnection().prepareStatement(getStatement(store));

        return (psQuery);
        }


    /***********************************************************************************************
     * Execute a Query, optionally recording the ExecutionTime and incrementing the ExecutionCount,
     * and optionally saving a trace of the Date, Time and host class.
     *
     * @param objhost
     * @param statement
     * @param traced
     * @param timed
     *
     * @return ResultSet
     *
     * @throws SQLException
     */

    public final ResultSet executeQuery(final Object objhost,
                                        final PreparedStatement statement,
                                        final boolean traced,
                                        final boolean timed) throws SQLException
        {
        final ResultSet resultSet;
        long longStartTime;
        final long longStopTime;

        if ((objhost == null)
            || (statement == null))
            {
            throw new SQLException(EXCEPTION_PARAMETER_NULL);
            }

        longStartTime = 0;

        // Time this execution, if required
        if (timed)
            {
            longStartTime = Chronos.getSystemTime();
            }

        // Always execute the statement!
        resultSet = statement.executeQuery();

        if (timed)
            {
            longStopTime = Chronos.getSystemTime();

            // If we get this far the execution time will be valid
            // Keep the longest time (for now)
            setExecutionTime(Math.max((longStopTime - longStartTime), getExecutionTime()));

            // Record this execution
            setExecutionCount(getExecutionCount() + 1);
            }

        // Add this SQLTrace Item to the SqlTrace, if required
        if (traced)
            {
            REGISTRY_MANAGER.traceQuery(this, objhost);
            }

        return (resultSet);
        }


    /***********************************************************************************************
     * Execute an Update Query, optionally recording the ExecutionTime and incrementing the ExecutionCount,
     * and optionally saving a trace of the Date, Time and host class.
     *
     * @param objhost
     * @param statement
     * @param timed
     * @param traced
     *
     * @return int either (1) the row count for INSERT, UPDATE, or DELETE statements or (2) 0 for SQL statements that return nothing
     *
     * @throws SQLException
     */

    public final int executeUpdate(final Object objhost,
                                   final PreparedStatement statement,
                                   final boolean timed,
                                   final boolean traced) throws SQLException
        {
        final int intReturnCode;
        long longStartTime;
        final long longStopTime;

        if ((objhost == null)
            || (statement == null))
            {
            throw new SQLException(EXCEPTION_PARAMETER_NULL);
            }

        longStartTime = 0;

        // Time this execution, if required
        if (timed)
            {
            longStartTime = Chronos.getSystemTime();
            }

        // Always execute the statement!
        intReturnCode = statement.executeUpdate();

        if (timed)
            {
            longStopTime = Chronos.getSystemTime();

            // If we get this far the execution time will be valid
            // Keep the longest time (for now)
            setExecutionTime(Math.max((longStopTime - longStartTime), getExecutionTime()));

            // Record this execution
            setExecutionCount(getExecutionCount() + 1);
            }

        // Add this SQLTrace Item to the SqlTrace, if required
        if (traced)
            {
            REGISTRY_MANAGER.traceQuery(this, objhost);
            }

        return (intReturnCode);
        }


    /***********************************************************************************************
     * Get the Query Execution Count.
     *
     * @return long
     */

    public final long getExecutionCount()
        {
        return (this.longExecutionCount);
        }


    /***********************************************************************************************
     * Set the Query Execution Count.
     *
     * @param executioncount
     */

    public void setExecutionCount(final long executioncount)
        {
        this.longExecutionCount = executioncount;
        updateRoot();
        }


    /***********************************************************************************************
     * Get the Query Execution Time.
     *
     * @return long
     */

    public final long getExecutionTime()
        {
        return (this.longExecutionTime);
        }


    /***********************************************************************************************
     * Set the Query Execution Time.
     *
     * @param executiontime
     */

    public final void setExecutionTime(final long executiontime)
        {
        this.longExecutionTime = executiontime;
        updateRoot();
        }


    /***********************************************************************************************
     * Get the XML part of the Query.
     *
     * @return XmlObject
     */

     public final QueryResource getXml()
         {
         if (super.getXml() == null)
             {
             throw new FrameworkException(EXCEPTION_PARAMETER_NULL);
             }

         return ((QueryResource)super.getXml());
         }
    }


//--------------------------------------------------------------------------------------------------
// End of File
