package com.epam.indigoeln.config.audit;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.user.UserService;

/**
 * Custom Provider for auditing
 * Need to be defined for support Mongo Audit functionality
 *(enable annotations @LastModifiedDate, @CreatedDate, @LastModifiedBy, @CreatedBy)
 */
@Configuration
public class CustomAuditProvider implements AuditorAware<User>  {

    @Autowired
    UserService userService;

    @Override
    public User getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user =  userService.getUserWithAuthoritiesByLogin(auth.getName());
        return user.isPresent() ? user.get() : null;
    }
}
