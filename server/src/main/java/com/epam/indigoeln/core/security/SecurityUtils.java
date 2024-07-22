/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.security;

import com.epam.indigoeln.core.util.WebSocketUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for Spring Security.
 */
@Component
public final class SecurityUtils {

    /**
     * WebSocketUtil instance to work with web socket.
     */
    private final WebSocketUtil webSocketUtil;

    /**
     * Create a new SecurityUtils instance.
     *
     * @param webSocketUtil WebSocketUtil instance to work with web socket
     */
    @Autowired
    private SecurityUtils(WebSocketUtil webSocketUtil) {
        this.webSocketUtil = webSocketUtil;
    }

    /**
     * Return the current user, or throws an exception, if the user is not
     * authenticated yet.
     *
     * @return the current user
     */
    public static User getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new IllegalStateException("User not found!");
    }

    /**
     * Check for significant changes of authorities and perform logout for each user, if these exist.
     *
     * @param modifiedUsers   Users
     * @param sessionRegistry Session registry
     */
    public void checkAndLogoutUsers(Collection<com.epam.indigoeln.core.model.User> modifiedUsers,
                                    SessionRegistry sessionRegistry) {
        final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        for (com.epam.indigoeln.core.model.User modifiedUser : modifiedUsers) {
            for (Object principal : allPrincipals) {
                UserDetails userDetails = (UserDetails) principal;
                if (modifiedUser.getLogin().equals(userDetails.getUsername())) {
                    checkAndInvalidate(sessionRegistry, modifiedUser, userDetails);
                }
            }
        }
    }

    private void checkAndInvalidate(SessionRegistry sessionRegistry,
                                    com.epam.indigoeln.core.model.User modifiedUser, UserDetails userDetails) {
        Set<String> newAuthorities = modifiedUser.getAuthorities().stream().map(Authority::getAuthority).
                collect(Collectors.toSet());
        Set<String> existingAuthorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).
                collect(Collectors.toSet());
        // Invalidate session if user's authorities set has been changed
        if (!CollectionUtils.isEqualCollection(newAuthorities, existingAuthorities)) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(userDetails, false);
            sessions.forEach(SessionInformation::expireNow);
            webSocketUtil.updateUser(userDetails.getUsername());
        }
    }

    /**
     * Check for significant changes of authorities and perform logout for user, if these exist.
     *
     * @param user            User
     * @param sessionRegistry Session registry
     */
    public void checkAndLogoutUser(com.epam.indigoeln.core.model.User user, SessionRegistry sessionRegistry) {
        checkAndLogoutUsers(Collections.singletonList(user), sessionRegistry);
    }
}
