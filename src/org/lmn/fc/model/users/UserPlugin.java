package org.lmn.fc.model.users;

import org.lmn.fc.model.root.RootPlugin;

import java.sql.Date;
import java.sql.Time;


/***************************************************************************************************
 * The UserPlugin.
 */

public interface UserPlugin extends RootPlugin
    {
    String getPassword();

    void setPassword(String password);

    String getRoleName();

    void setRoleName(String name);

    String getCountryCode();

    void setCountryCode(String code);

    String getLanguageCode();

    void setLanguageCode(String code);

    String getEmail();

    void setEmail(String email);

    Date getDateLastLogin();

    void setDateLastLogin(Date date);

    Time getTimeLastLogin();

    void setTimeLastLogin(Time time);

    RolePlugin getRole();

    void setRole(RolePlugin role);
    }
