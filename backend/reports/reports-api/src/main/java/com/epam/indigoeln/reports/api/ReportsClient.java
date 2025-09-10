package com.epam.indigoeln.reports.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "reports-api")
public interface ReportsClient extends ReportsAPI {
}
