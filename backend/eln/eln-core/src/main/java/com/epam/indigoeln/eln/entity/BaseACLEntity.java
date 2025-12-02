package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.AccessLevel;

public interface BaseACLEntity {

    UserEntity getUser();
    void setUser(UserEntity user);

    AccessLevel getLevel();
    void setLevel(AccessLevel level);
}
