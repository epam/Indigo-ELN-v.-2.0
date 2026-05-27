package com.epam.indigoeln.compound.service.search.pubchem;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;
import java.util.Map;

@RegisterRestClient(configKey = "pubchem")
interface PubChemClient {

    String ERROR_NOT_FOUND = "PUGREST.NotFound";

    @POST
    @Path("/property/MolecularFormula,MolecularWeight,IUPACName,InChI/JSON")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    PubChemResponse search(@Url URI url, @RestQuery Map<String, Object> queryParams, MultivaluedMap<String, Object> formParams);

    @ClientExceptionMapper
    static PubChemException toException(Response response) {
        int status = response.getStatus();
        PubChemException ex;
        try {
            PubChemFault fault = response.readEntity(PubChemFault.class);
            StringBuilder message = new StringBuilder("PubChem error: HTTP status ").append(status);
            if (fault.fault() != null) {
                message.append(", ").append(fault.fault().code()).append(": ").append(fault.fault().message());
            }
            ex = fault.fault() != null && ERROR_NOT_FOUND.equals(fault.fault().code())
                    ? new PubChemException.NotFound(message.toString())
                    : new PubChemException(message.toString());
        } catch (Exception e) {
            ex = new PubChemException("Cannot read PubChem error response: " + e.getMessage(), e);
        }
        throw ex;
    }
}
