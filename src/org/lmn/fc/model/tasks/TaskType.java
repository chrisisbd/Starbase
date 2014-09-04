package org.lmn.fc.model.tasks;

public enum TaskType
    {
    FRAMEWORK_TASK (0),
    APPLICATION_TASK (1),
    COMPONENT_TASK (2);

    private final int intValue;

    private TaskType(final int value)
        {
        intValue = value;
        }

    public int getType()
        {
        return (this.intValue);
        }

    public String toString()
        {
        return Integer.toString(intValue);
        }
    }
