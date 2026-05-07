package com.epam.indigoeln.reports.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "reports-api")
@RegisterClientHeaders(APISecretHeaderFactory.Internal.class)
public interface ReportsClient extends ReportsAPI {
}
