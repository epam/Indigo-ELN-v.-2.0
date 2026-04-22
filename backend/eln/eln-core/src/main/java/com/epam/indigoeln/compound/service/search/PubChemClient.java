package com.epam.indigoeln.compound.service.search;

import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;
import java.util.Map;

@RegisterRestClient(configKey = "pubchem")
interface PubChemClient {

    @POST
    @Path("/property/MolecularFormula,MolecularWeight,IUPACName,InChI/JSON")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    PubChemResponse search(@Url URI url, @RestQuery Map<String, Object> queryParams, MultivaluedMap<String, Object> formParams);
}
