package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@Alternative
@ApplicationScoped
class FakeExternalUserService implements ExternalUserService {

    @Override
    public void createUser(UserRequest request) {
        // No-op implementation for testing purposes
    }
}
