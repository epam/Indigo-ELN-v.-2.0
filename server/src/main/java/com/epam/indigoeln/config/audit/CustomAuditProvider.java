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

import java.util.Optional;

/**
 * Custom Provider for auditing.
 * Need to be defined for support Mongo Audit functionality
 * (enable annotations @LastModifiedDate, @CreatedDate, @LastModifiedBy, @CreatedBy).
 */
@Configuration
public class CustomAuditProvider implements AuditorAware<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuditProvider.class);

    @Autowired
    private UserService userService;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            return Optional.ofNullable(auth).map(Authentication::getName)
                    .map(userService::getUserWithAuthoritiesByLogin);
        } catch (EntityNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("User with name " + auth.getName() + " cannot be found.", e);
            }
            return Optional.empty();
        }
    }
}
