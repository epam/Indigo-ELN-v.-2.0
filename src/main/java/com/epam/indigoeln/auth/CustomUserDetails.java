package com.epam.indigoeln.auth;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private UserInfo userInfo;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userInfo = new UserInfo(username, authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
