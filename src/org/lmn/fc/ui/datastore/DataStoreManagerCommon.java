// Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013
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


package org.lmn.fc.ui.datastore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

// sqlbob@users 20020401 - patch 1.7.0 by sqlbob (RMP) - enhancements
// sqlbob@users 20020407 - patch 1.7.0 - reengineering
// nickferguson@users 20021005 - patch 1.7.1 - enhancements
// fredt@users 20021012 - patch 1.7.1 - changes to test database DDL

/**
 * Common code in Swing and AWT versions of DatabaseManager
 *
 * @version 1.7.0
 */
final class DataStoreManagerCommon
    {

    private static final Random rRandom = new Random(100);
    static final String[] selectHelp = {
        "SELECT * FROM ",
        "SELECT [LIMIT n m] [DISTINCT] \n"
            + "{ selectExpression | table.* | * } [, ... ] \n"
            + "[INTO [CACHED|TEMP|TEXT] newTable] \n" + "FROM tableList \n"
            + "[WHERE Expression] \n"
            + "[ORDER BY selectExpression [{ASC | DESC}] [, ...] ] \n"
            + "[GROUP BY Expression [, ...] ] \n"
            + "[UNION [ALL] selectStatement]"
    };
    static final String[] insertHelp = {
        "INSERT INTO ",
        "INSERT INTO table [ (column [,...] ) ] \n"
            + "{ VALUES(Expression [,...]) | SelectStatement }"
    };
    static final String[] updateHelp = {
        "UPDATE ",
        "UPDATE table SET column = Expression [, ...] \n"
            + "[WHERE Expression]"
    };
    static final String[] deleteHelp = {
        "DELETE FROM ", "DELETE FROM table [WHERE Expression]"
    };
    static final String[] createTableHelp = {
        "CREATE TABLE ",
        "CREATE [TEMP] [CACHED|MEMORY|TEXT] TABLE name \n"
            + "( columnDefinition [, ...] ) \n\n" + "columnDefinition: \n"
            + "column DataType [ [NOT] NULL] [PRIMARY KEY] \n" + "DataType: \n"
            + "{ INTEGER | DOUBLE | VARCHAR | DATE | TIME |... }"
    };
    static final String[] dropTableHelp = {
        "DROP TABLE ", "DROP TABLE table"
    };
    static final String[] createIndexHelp = {
        "CREATE INDEX ",
        "CREATE [UNIQUE] INDEX index ON \n" + "table (column [, ...])"
    };
    static final String[] dropIndexHelp = {
        "DROP INDEX ", "DROP INDEX table.index"
    };
    static final String[] checkpointHelp = {
        "CHECKPOINT", "(HSQLDB SQL only)"
    };
    static final String[] scriptHelp = {
        "SCRIPT", "SCRIPT ['file']\n\n" + "(HSQLDB SQL only)"
    };
    static final String[] shutdownHelp = {
        "SHUTDOWN", "SHUTDOWN [COMPACT|IMMEDIATELY]\n\n" + "(HSQLDB SQL only)"
    };
    static final String[] setHelp = {
        "SET ",
        "AUTOCOMMIT { TRUE | FALSE }\n" + "IGNORECASE { TRUE | FALSE }\n"
            + "LOGSIZE size\n" + "MAXROWS maxrows\n" + "PASSWORD password\n"
            + "READONLY { TRUE | FALSE }\n"
            + "REFERENTIAL_INTEGRITY { TRUE | FALSE }\n"
            + "TABLE table READONLY { TRUE | FALSE }\n"
            + "TABLE table SOURCE \"file\" [DESC]\n"
            + "WRITE_DELAY { TRUE | FALSE }\n\n" + "(HSQLDB SQL only)"
    };
    static final String[] testHelp = {
        "-->>>TEST<<<-- ;\n" + "--#1000;\n" + "DROP TABLE Test ;\n"
            + "CREATE TABLE Test(\n" + "  Id INTEGER PRIMARY KEY,\n"
            + "  FirstName VARCHAR(20),\n" + "  Name VARCHAR(50),\n"
            + "  ZIP INTEGER) ;\n" + "INSERT INTO Test \n"
            + "  VALUES(#,'Julia','Peterson-Clancy',#) ;\n"
            + "UPDATE Test SET Name='Hans' WHERE Id=# ;\n"
            + "SELECT * FROM Test WHERE Id=# ;\n"
            + "DELETE FROM Test WHERE Id=# ;\n" + "DROP TABLE Test",
        "This test script is parsed by the DatabaseManager\n"
            + "It may be changed manually. Rules:\n"
            + "- it must start with -->>>TEST<<<--.\n"
            + "- each line must end with ';' (no spaces after)\n"
            + "- lines starting with -- are comments\n"
            + "- lines starting with --#<count> means set new count\n"
    };
    static final String[] testDataSql = {
        "SELECT * FROM Product", "SELECT * FROM Invoice",
        "SELECT * FROM Item",
        "SELECT * FROM Customer a INNER JOIN Invoice i ON a.ID=i.CustomerID",
        "SELECT * FROM Customer a LEFT OUTER JOIN Invoice i ON a.ID=i.CustomerID",
        "SELECT * FROM Invoice d INNER JOIN Item i ON d.ID=i.InvoiceID",
        "SELECT * FROM Customer WHERE Street LIKE '1%' ORDER BY Lastname",
        "SELECT a.id, a.firstname, a.lastname, count(i.Total) \"COUNT\", "
            + "COALESCE(sum(i.Total), 0) \"TOTAL\", COALESCE(AVG(i.Total),0) \"AVG\" FROM Customer a "
            + "LEFT OUTER JOIN Invoice i ON a.ID=i.CustomerID GROUP BY a.id, a.firstname, a.lastname"
    };

    /**
     * Method declaration
     *
     * @param s
     * @return String
     */
    static String random(final String[] s)
        {
        return s[random(s.length)];
        }

    /**
     * Method declaration
     *
     * @param i
     * @return int
     */
    static int random(int i)
        {

        i = rRandom.nextInt() % i;

        return i < 0 ? -i
            : i;
        }

    /**
     * Method declaration
     */
    static void createTestTables(final Statement sStatement)
        {

        final String[] demo = {
            "DROP TABLE Item IF EXISTS;", "DROP TABLE Invoice IF EXISTS;",
            "DROP TABLE Product IF EXISTS;", "DROP TABLE Customer IF EXISTS;",
            "CREATE TABLE Customer(ID INTEGER PRIMARY KEY,FirstName VARCHAR,"
                + "LastName VARCHAR,Street VARCHAR,City VARCHAR);",
            "CREATE TABLE Product(ID INTEGER PRIMARY KEY,Name VARCHAR,"
                + "Price DECIMAL);",
            "CREATE TABLE Invoice(ID INTEGER PRIMARY KEY,CustomerID INTEGER,"
                + "Total DECIMAL, FOREIGN KEY (CustomerId) "
                + "REFERENCES Customer(ID) ON DELETE CASCADE);",
            "CREATE TABLE Item(InvoiceID INTEGER,Item INTEGER,"
                + "ProductID INTEGER,Quantity INTEGER,Cost DECIMAL,"
                + "PRIMARY KEY(InvoiceID,Item), "
                + "FOREIGN KEY (InvoiceId) REFERENCES "
                + "Invoice (ID) ON DELETE CASCADE, FOREIGN KEY (ProductId) "
                + "REFERENCES Product(ID) ON DELETE CASCADE);"
        };

        for (int i = 0; i < demo.length; i++)
            {

            // drop table may fail
            try
                {
                sStatement.execute(demo[i]);
                }
            catch (SQLException e)
                {
                }
            }
        }

    /**
     * Method declaration
     */
    static String createTestData(final Statement sStatement) throws SQLException
        {

        final String[] name = {
            "White", "Karsen", "Smith", "Ringer", "May", "King", "Fuller",
            "Miller", "Ott", "Sommer", "Schneider", "Steel", "Peterson",
            "Heiniger", "Clancy"
        };
        final String[] firstname = {
            "Mary", "James", "Anne", "George", "Sylvia", "Robert", "Janet",
            "Michael", "Andrew", "Bill", "Susanne", "Laura", "Bob", "Julia",
            "John"
        };
        final String[] street = {
            "Upland Pl.", "College Av.", "- 20th Ave.", "Seventh Av."
        };
        final String[] city = {
            "New York", "Dallas", "Boston", "Chicago", "Seattle",
            "San Francisco", "Berne", "Oslo", "Paris", "Lyon", "Palo Alto",
            "Olten"
        };
        final String[] product = {
            "Iron", "Ice Tea", "Clock", "Chair", "Telephone", "Shoe"
        };
        final int max = 50;

        sStatement.execute("SET REFERENTIAL_INTEGRITY FALSE");

        for (int i = 0; i < max; i++)
            {
            sStatement.execute("INSERT INTO Customer VALUES(" + i + ",'"
                + random(firstname) + "','" + random(name)
                + "','" + random(554) + " " + random(street)
                + "','" + random(city) + "')");
            sStatement.execute("INSERT INTO Product VALUES(" + i + ",'"
                + random(product) + " " + random(product)
                + "'," + (20 + 2 * random(120)) + ")");
            sStatement.execute("INSERT INTO Invoice VALUES(" + i + ","
                + random(max) + ",0.0)");

            for (int j = random(20) + 2; j >= 0; j--)
                {
                sStatement.execute("INSERT INTO Item VALUES(" + i + "," + j
                    + "," + random(max) + ","
                    + (1 + random(24)) + ",1.5)");
                }
            }

        sStatement.execute("SET REFERENTIAL_INTEGRITY TRUE");
        sStatement.execute("UPDATE Product SET Price=ROUND(Price*.1,2)");
        sStatement.execute(
            "UPDATE Item SET Cost=Cost*"
                + "SELECT Price FROM Product prod WHERE ProductID=prod.ID");
        sStatement.execute(
            "UPDATE Invoice SET Total=SELECT SUM(Cost*"
                + "Quantity) FROM Item WHERE InvoiceID=Invoice.ID");

        return ("SELECT * FROM Customer");
        }

    /**
     * Method declaration
     * Redid this file to remove sizing requirements and to make it faster
     * Speeded it up 10 fold.
     *
     * @param file
     * @return String
     */
    static String readFile(final String file)
        {

        try
            {
            final FileReader reader = new FileReader(file);
            final BufferedReader read = new BufferedReader(reader);
            final StringBuffer b = new StringBuffer();
            String s;
            int count = 0;

            while ((s = read.readLine()) != null)
                {
                count++;

                b.append(s);
                b.append('\n');
                }

            read.close();
            reader.close();

            return b.toString();
            }
        catch (IOException e)
            {
            return e.getMessage();
            }
        }

    /**
     * Method declaration
     *
     * @param file
     * @param text
     */
    static void writeFile(final String file, final String text)
        {

        try
            {
            final FileWriter write = new FileWriter(file);

            write.write(text.toCharArray());
            write.close();
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }
        }

    /**
     * Method declaration
     *
     * @param sql
     * @param max
     * @return
     * @throws SQLException
     */
    static long testStatement(final Statement sStatement, final String sql,
                              int max) throws SQLException
        {

        final long start = System.currentTimeMillis();

        if (sql.indexOf('#') == -1)
            {
            max = 1;
            }

        for (int i = 0; i < max; i++)
            {
            String s = sql;

            while (true)
                {
                final int j = s.indexOf("#r#");

                if (j == -1)
                    {
                    break;
                    }

                s = s.substring(0, j) + ((int) (Math.random() * i))
                    + s.substring(j + 3);
                }

            while (true)
                {
                final int j = s.indexOf('#');

                if (j == -1)
                    {
                    break;
                    }

                s = s.substring(0, j) + i + s.substring(j + 1);
                }

            sStatement.execute(s);
            }

        return (System.currentTimeMillis() - start);
        }

    private DataStoreManagerCommon()
        {
        }
    }
