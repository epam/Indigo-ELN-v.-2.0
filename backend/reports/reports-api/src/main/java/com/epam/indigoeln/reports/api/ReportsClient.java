package com.epam.indigoeln.reports.api;

import com.epam.indigoeln.reports.api.config.InternalAPIHeaderFactory;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "reports-api")
@RegisterClientHeaders(InternalAPIHeaderFactory.class)
public interface ReportsClient extends ReportsAPI {
}
