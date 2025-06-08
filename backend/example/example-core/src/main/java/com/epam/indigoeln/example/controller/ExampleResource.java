package com.epam.indigoeln.example.controller;


import com.epam.indigoeln.example.api.ExampleAPI;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.LinkedHashMap;
import java.util.Map;

@Path(ExampleAPI.BASE_PATH)
public class ExampleResource implements ExampleAPI {

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Path("/info")
    public Map<String, String> getInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("service", "Example Service");
        info.put("anonymous", "" + securityIdentity.isAnonymous());
        info.put("principal", "" + securityIdentity.getPrincipal());
        if (securityIdentity.getPrincipal() != null) {
            info.put("principal.name", securityIdentity.getPrincipal().getName());
        }
        info.put("attributes", "" + securityIdentity.getAttributes());
        return info;
    }

    @Override
    public @Valid MinMax sum(@Positive int a, int b) {
        return new MinMax(Math.min(a, b), Math.max(a, b));
    }
}
