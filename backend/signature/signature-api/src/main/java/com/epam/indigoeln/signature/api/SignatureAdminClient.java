package com.epam.indigoeln.signature.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SignatureAdminAPI.BASE_PATH)
@RegisterRestClient(configKey = "signature-admin-api")
@RegisterClientHeaders(APISecretHeaderFactory.class)
public interface SignatureAdminClient extends SignatureAdminAPI {
}
