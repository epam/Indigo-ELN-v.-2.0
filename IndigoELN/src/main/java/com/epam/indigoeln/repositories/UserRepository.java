package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.User;

public interface UserRepository {
    void saveUser(User user);
    void deleteUser(String id);
    User getUser(String name);
}
