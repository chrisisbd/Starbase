package org.lmn.fc.model.resources;

import org.lmn.fc.database.DatabasePlugin;
import org.lmn.fc.model.dao.DataStore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public interface QueryPlugin extends ResourcePlugin
    {
    // String Resources
    String QUERIES_ICON            = "queries.png";
    String QUERY_ICON              = "query.png";
    String QUERY_CLASSNAME         = "java.lang.String";

    int QUERY_ATOM_LENGTH = 255;
    int QUERY_LENGTH = QUERY_ATOM_LENGTH << 2;
    int DESCRIPTION_LENGTH = 255;

    List<String> getStatements() throws SQLException;

    String getStatement(DataStore store) throws SQLException;

    PreparedStatement getPreparedStatement(DatabasePlugin database,
                                           DataStore store) throws SQLException;

    ResultSet executeQuery(Object host,
                           PreparedStatement statement,
                           boolean traced,
                           boolean timed) throws SQLException;

    int executeUpdate(Object host,
                      PreparedStatement statement,
                      boolean timed,
                      boolean traced) throws SQLException;

    long getExecutionCount();

    void setExecutionCount(long count);

    long getExecutionTime();

    void setExecutionTime(long time);
    }
