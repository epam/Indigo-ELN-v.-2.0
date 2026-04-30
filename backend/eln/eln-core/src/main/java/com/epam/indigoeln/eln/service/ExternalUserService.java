package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public interface ExternalUserService {

    void createUser(UserRequest request);

    class FakeExternalServiceImpl implements ExternalUserService {

        @Override
        public void createUser(UserRequest request) {
            // do nothing
        }
    }

    class Provider {

        @Produces
        ExternalUserService getExternalUserService(Instance<CognitoIdentityProviderClient> cognitoClient) {
            if (ConfigUtils.isProfileActive("devtest")) {
                return new FakeExternalServiceImpl();
            }
            return new CognitoExternalUserService(cognitoClient.get(), ConfigProvider.getConfig().getValue("eln.cognito.user-pool-id", String.class));
        }
    }
}
