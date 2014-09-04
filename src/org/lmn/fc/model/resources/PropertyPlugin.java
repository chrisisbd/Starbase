package org.lmn.fc.model.resources;


public interface PropertyPlugin extends ResourcePlugin
    {
    // String Resources
    String PROPERTIES_ICON          = "properties.png";
    String PROPERTY_ICON            = "property.png";

    int VALUE_LENGTH = 255;
    int DESCRIPTION_LENGTH = 255;

    boolean getInstantiated();

    void setInstantiated(boolean instantiated);
    }
