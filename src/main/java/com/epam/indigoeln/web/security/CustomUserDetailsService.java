package com.epam.indigoeln.web.security;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.RolePermission;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserRole;
import com.epam.indigoeln.core.service.role.RoleService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.model.security.CustomUserDetails;
import com.epam.indigoeln.web.model.security.Permission;
import com.epam.indigoeln.web.model.security.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
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
        String userId = user.getId();
        Collection<UserRole> userRoles = userService.getUserRoles(userId);
        Set<String> rolesIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());

        Collection<Role> roles = roleService.getRoles(rolesIds);

        Set<String> rolesNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
        Collection<RolePermission> rolesPermissions = roleService.getRolesPermissions(rolesIds);

        rolesPermissions.stream().filter(rp -> {
            return !Permission.isPermission(rp.getValue());
        }).findFirst().ifPresent((rp) -> {
            throw new UsernameNotFoundException("Unknown permission " + rp.getValue());
        });

        Set<String> permissions = rolesPermissions.stream().map(RolePermission::getValue).collect(Collectors.toSet());

        UserInfo userInfo = new UserInfo(userId, username,
                rolesNames, permissions);

        return new CustomUserDetails(username, user.getPassword(),
                permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                userInfo
        );
    }
}
