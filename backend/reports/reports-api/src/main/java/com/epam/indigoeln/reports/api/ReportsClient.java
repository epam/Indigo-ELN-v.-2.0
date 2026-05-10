package com.epam.indigoeln.reports.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(ReportsAPI.BASE_PATH)
@RegisterRestClient(configKey = "reports-api")
@RegisterClientHeaders(APISecretHeaderFactory.class)
public interface ReportsClient extends ReportsAPI {
}
