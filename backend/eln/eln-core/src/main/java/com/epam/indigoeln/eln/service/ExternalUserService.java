package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;

interface ExternalUserService {

    void createUser(UserRequest request);
}
