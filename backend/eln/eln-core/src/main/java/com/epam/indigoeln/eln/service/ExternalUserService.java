package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

public interface ExternalUserService {

    void createUser(UserRequest request);

    @DefaultBean
    @ApplicationScoped
    class FakeExternalServiceImpl implements ExternalUserService {

        @Override
        public void createUser(UserRequest request) {
            // do nothing
        }
    }
}
