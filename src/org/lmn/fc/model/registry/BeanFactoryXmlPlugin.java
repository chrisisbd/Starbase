package org.lmn.fc.model.registry;

import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.constants.ResourceKeys;
import org.lmn.fc.model.locale.CountryPlugin;
import org.lmn.fc.model.locale.LanguagePlugin;
import org.lmn.fc.model.logging.Logger;
import org.lmn.fc.model.lookandfeels.LookAndFeelPlugin;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.plugins.FrameworkPlugin;
import org.lmn.fc.model.registry.impl.Registry;
import org.lmn.fc.model.root.RootPlugin;
import org.lmn.fc.model.users.RolePlugin;
import org.lmn.fc.model.xmlbeans.countries.Country;
import org.lmn.fc.model.xmlbeans.exceptions.ExceptionResource;
import org.lmn.fc.model.xmlbeans.frameworks.Framework;
import org.lmn.fc.model.xmlbeans.languages.Language;
import org.lmn.fc.model.xmlbeans.lookandfeels.LookAndFeel;
import org.lmn.fc.model.xmlbeans.plugins.Plugin;
import org.lmn.fc.model.xmlbeans.properties.PropertyResource;
import org.lmn.fc.model.xmlbeans.queries.QueryResource;
import org.lmn.fc.model.xmlbeans.roles.Role;
import org.lmn.fc.model.xmlbeans.strings.StringResource;
import org.lmn.fc.model.xmlbeans.tasks.Task;



/***************************************************************************************************
 * The BeanFactoryXmlPlugin.
 */

public interface BeanFactoryXmlPlugin extends FrameworkConstants,
                                              FrameworkStrings,
                                              FrameworkMetadata,
                                              ResourceKeys
    {
    Logger LOGGER = Logger.getInstance();
    RegistryPlugin REGISTRY = Registry.getInstance();

    RolePlugin createRole(Role role);

    CountryPlugin createCountry(Country country);

    LanguagePlugin createLanguage(Language language);


    LookAndFeelPlugin createLookAndFeel(LookAndFeel lookandfeel);

    FrameworkPlugin createFramework(Class frameworkclass,
                                    Framework framework);

    AtomPlugin createPlugin(AtomPlugin parent,
                            Class atomclass,
                            Plugin atom);

    RootPlugin createTask(AtomPlugin parent,
                          Class taskclass,
                          Task task);

    RootPlugin createProperty(AtomPlugin host,
                              PropertyResource property,
                              String language);

    RootPlugin createString(AtomPlugin host,
                            StringResource string,
                            String language);

    RootPlugin createException(AtomPlugin host,
                               ExceptionResource exception,
                               String language);

    RootPlugin createQuery(AtomPlugin host,
                           QueryResource query,
                           String language);

    Object validateResourceDataType(String classname,
                                    String value);
    }
