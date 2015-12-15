package com.epam.indigoeln.auth;

import com.epam.indigoeln.documents.Role;
import com.epam.indigoeln.documents.User;
import com.epam.indigoeln.services.RoleService;
import com.epam.indigoeln.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " cannot not be found");
        }
        Collection<Role> roles = roleService.getRoles(user.getId());
        return new CustomUserDetails(username, user.getPassword(),
                roles.stream().map(Role::getName).map(SimpleGrantedAuthority::new).collect(Collectors.toList())
        );
    }
}
