package com.epam.indigoeln.core.security;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get the login of the current user.
     */
    public static String getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        String userName = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                userName = (String) authentication.getPrincipal();
            }
        }
        return userName;
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Collection<? extends GrantedAuthority> authorities = securityContext.getAuthentication().getAuthorities();
        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                if (authority.equals(Authority.ANONYMOUS)) {
                    return false;
                }
            }
        }
        return true;
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
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
        }
        throw new IllegalStateException("User not found!");
    }

    /**
     * If the current user has a specific authority (security role).
     * <p>
     * <p>The name of this method comes from the isUserInRole() method in the Servlet API</p>
     */
    public static boolean isCurrentUserHasAuthority(Authority authority) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                return springSecurityUser.getAuthorities().contains(authority);
            }
        }
        return false;
    }

    /**
     * Check for significant changes of authorities and perform logout for each user, if these exist
     */
    public static void checkAndLogoutUsers(Collection<com.epam.indigoeln.core.model.User> modifiedUsers,
                                    SessionRegistry sessionRegistry) {
        final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        for (com.epam.indigoeln.core.model.User modifiedUser: modifiedUsers) {
            for (Object principal : allPrincipals) {
                UserDetails userDetails = (UserDetails) principal;
                if (modifiedUser.getLogin().equals(userDetails.getUsername())) {
                    Set<String> newAuthorities = modifiedUser.getAuthorities().stream().map(Authority::getAuthority).
                            collect(Collectors.toSet());
                    Set<String> existingAuthorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).
                            collect(Collectors.toSet());
                    // Invalidate session if user's authorities set has been changed
                    if (!CollectionUtils.isEqualCollection(newAuthorities, existingAuthorities)) {
                        List<SessionInformation> sessions = sessionRegistry.getAllSessions(userDetails, false);
                        sessions.forEach(SessionInformation::expireNow);
                    }
                }
            }
        }
    }


    /**
     * Check for significant changes of authorities and perform logout for user, if these exist
     */
    public static void checkAndLogoutUser(com.epam.indigoeln.core.model.User user, SessionRegistry sessionRegistry) {
        checkAndLogoutUsers(Collections.singletonList(user), sessionRegistry);
    }
}