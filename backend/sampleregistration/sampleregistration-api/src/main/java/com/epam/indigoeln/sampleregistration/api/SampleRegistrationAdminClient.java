package com.epam.indigoeln.sampleregistration.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SampleRegistrationAPI.BASE_PATH)
@RegisterRestClient(configKey = "sampleregistration-api")
@RegisterClientHeaders(APISecretHeaderFactory.class)
public interface SampleRegistrationAdminClient extends SampleRegistrationAdminAPI {
}
