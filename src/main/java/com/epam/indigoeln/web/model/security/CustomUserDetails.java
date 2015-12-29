package com.epam.indigoeln.web.model.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private UserInfo userInfo;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
                             UserInfo userInfo) {
        super(username, password, authorities);
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
