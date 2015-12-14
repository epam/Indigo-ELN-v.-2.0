package com.epam.indigoeln.services;

import com.epam.indigoeln.documents.User;
import com.epam.indigoeln.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUser(String name) {
        return userRepository.getUser(name);
    }
}
