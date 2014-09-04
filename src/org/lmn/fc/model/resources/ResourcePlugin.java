package org.lmn.fc.model.resources;

import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.registry.impl.BeanFactoryXml;
import org.lmn.fc.model.root.UserObjectPlugin;

import java.util.List;


public interface ResourcePlugin extends UserObjectPlugin
    {
    BeanFactoryXml BEAN_FACTORY_XML = BeanFactoryXml.getInstance();

    // String Resources
    String RESOURCES_ICON = "resources.png";

    AtomPlugin getHostAtom();

    void setResourceKey(String key);

    List<String> getResourceKeys();

    void setResourceKeys(List<String> keys);

    String getISOLanguageCode();

    void setISOLanguageCode(String code);

    Object getResource();

    void setResource(Object resource);

    boolean isInstalled();

    void setInstalled(boolean installed);

    String getDataType();

    void setDataType(String classname);
    }
