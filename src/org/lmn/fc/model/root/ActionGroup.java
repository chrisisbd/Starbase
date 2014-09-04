package org.lmn.fc.model.root;

public enum ActionGroup
    {
    STATIC (0, "Static"),
    DYNAMIC (1, "Dynamic"),
    PLUGIN (2, "Plugin");

    private final int intIndex;


    private ActionGroup(final int index,
                        final String name)
        {
        intIndex = index;
        }

    public int getIndex()
        {
        return (this.intIndex);
        }

    public String getName()
        {
        return (this.name());
        }
    }
