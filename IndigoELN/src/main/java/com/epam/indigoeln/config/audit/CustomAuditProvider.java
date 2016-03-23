package com.epam.indigoeln.config.audit;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Custom Provider for auditing
 * Need to be defined for support Mongo Audit functionality
 *(enable annotations @LastModifiedDate, @CreatedDate, @LastModifiedBy, @CreatedBy)
 */
@Configuration
public class CustomAuditProvider implements AuditorAware<User>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuditProvider.class);

    @Autowired
    UserService userService;

    @Override
    public User getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user;
        try {
            user = userService.getUserWithAuthoritiesByLogin(auth.getName());
        } catch (EntityNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("User with name " + auth.getName() + " cannot be found.", e);
            }
            user = null;
        }
        return user;
    }
}