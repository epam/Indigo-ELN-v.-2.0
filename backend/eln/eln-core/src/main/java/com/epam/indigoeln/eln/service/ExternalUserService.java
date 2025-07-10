package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.UserRequest;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public interface ExternalUserService {

    void createUser(UserRequest request);

    class FakeExternalServiceImpl implements ExternalUserService {

        @Override
        public void createUser(UserRequest request) {
            // no nothing
        }
    }

    class Provider {

        @Produces
        ExternalUserService getExternalUserService(Instance<CognitoIdentityProviderClient> cognitoClient) {
            // TODO use Cognito integration when networking is resolved
            if (1 == 1) {
//            if (ConfigUtils.isProfileActive("devtest")) {
                return new FakeExternalServiceImpl();
            }
            return new CognitoExternalUserService(cognitoClient.get(), ConfigProvider.getConfig().getValue("eln.cognito.user-pool-id", String.class));
        }
    }
}
