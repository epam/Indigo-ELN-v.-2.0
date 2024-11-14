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

import com.chemistry.enotebook.signature.database.DatabaseConnector;
import com.chemistry.enotebook.signature.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private DatabaseConnector database;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        System.out.println("!!! encoded 1234 = " + passwordEncoder.encode("1234"));
        System.out.println("!!! reference:     81dc9bdb52d04dc20036dbd8313ed055");
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User dbUser;

        try {
            dbUser = database.getUserByUsername(s);
            log.warn("!!! loadUserByUsername: username={}, password={}, admin={}, active={}", dbUser.getUsername(), dbUser.getPassword(), dbUser.isAdmin(), dbUser.isActive());
        } catch (Exception e) {
            log.error("Error getting user from DB: " + e.getMessage(), e);
            throw new UsernameNotFoundException("Error getting user from DB: ", e);
        }

        if (dbUser == null) {
            log.error("Username not found!");
            throw new UsernameNotFoundException("Username not found!");
        }

        return UserDetailsImpl.createUser(dbUser);
    }
}
