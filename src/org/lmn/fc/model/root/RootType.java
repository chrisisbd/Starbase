package org.lmn.fc.model.root;

public enum RootType
    {
    ATOM (0, "Atom"),
    TASK (1, "Task"),
    PROPERTY (2, "Property"),
    STRING (3, "String"),
    EXCEPTION (4, "Exception"),
    QUERY (5, "Query");

    private final int intValue;
    private final String strName;


    private RootType(final int value,
                     final String name)
        {
        intValue = value;
        strName = name;
        }

    public int getTypeID()
        {
        return (this.intValue);
        }

    public String getName()
        {
        return(this.strName);
        }

    public String toString()
        {
        return (this.strName);
        }
    }
