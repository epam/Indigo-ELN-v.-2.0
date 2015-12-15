package com.epam.indigoeln.auth;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

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
