package com.epam.indigoeln.signature.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "signature-admin-api")
@RegisterClientHeaders(APISecretHeaderFactory.Internal.class)
public interface SignatureAdminClient extends SignatureAdminAPI {
}
