package com.epam.indigoeln.signature.client;

import com.epam.indigoeln.signature.api.SignatureAPI;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "signature-api")
public interface SignatureClient extends SignatureAPI {
}
