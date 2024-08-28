/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.security;

import com.chemistry.enotebook.signature.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsImpl extends org.springframework.security.core.userdetails.User {

    private User dbUser;

    private UserDetailsImpl(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public User getDbUser() {
        return dbUser;
    }

    public void setDbUser(User dbUser) {
        this.dbUser = dbUser;
    }

    public static UserDetailsImpl createUser(User dbUser) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        String username = dbUser.getUsername();
        String password = dbUser.getPassword();

        boolean active = dbUser.isActive();

        String role = dbUser.isAdmin() ? "ADMIN" : "USER";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        UserDetailsImpl ud = new UserDetailsImpl(username, password, active, active, active, active, authorities);
        ud.setDbUser(dbUser);

        return ud;
    }
}
