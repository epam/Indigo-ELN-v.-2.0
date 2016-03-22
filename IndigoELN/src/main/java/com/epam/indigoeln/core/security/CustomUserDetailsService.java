package com.epam.indigoeln.core.security;


import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        LOGGER.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase();
        User user = userRepository.findOneByLogin(lowercaseLogin);
        if (user == null) {
            throw new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database");
        }

        return new org.springframework.security.core.userdetails.User(
                lowercaseLogin, user.getPassword(), user.getAuthorities());
    }
}