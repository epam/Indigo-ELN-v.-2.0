package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.User;

import java.util.Collection;

public interface UserRepository {
    void saveUser(User user);
    void deleteUser(String id);
    User getUser(String name);
    Collection<User> getUsers();
}
