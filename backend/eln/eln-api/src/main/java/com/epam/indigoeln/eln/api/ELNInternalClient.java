package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(ELNInternalAPI.BASE_PATH)
@RegisterRestClient(configKey = "eln-internal-api")
@RegisterClientHeaders(APISecretHeaderFactory.class)
public interface ELNInternalClient extends ELNInternalAPI {
}
