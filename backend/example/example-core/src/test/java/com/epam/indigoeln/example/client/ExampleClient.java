package com.epam.indigoeln.example.client;

import com.epam.indigoeln.example.api.ExampleAPI;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "example-api")
public interface ExampleClient extends ExampleAPI {
}
