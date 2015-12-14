package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.User;

public interface UserRepository {
    User getUser(String name);
}
