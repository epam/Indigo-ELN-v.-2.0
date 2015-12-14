package com.epam.indigoeln.auth;

import com.epam.indigoeln.documents.User;
import com.epam.indigoeln.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " cannot not be found");
        }
        return new org.springframework.security.core.userdetails.User(username, user.getPassword(), Collections.EMPTY_LIST);
    }
}
