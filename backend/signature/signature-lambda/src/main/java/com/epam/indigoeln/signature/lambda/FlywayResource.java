package com.epam.indigoeln.signature.lambda;

import com.epam.indigoeln.signature.api.SignatureAPI;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

@Path(SignatureAPI.BASE_PATH)
public class FlywayResource {

    @Inject
    Flyway flyway;

    @POST
    @Path("/admin/flyway")
    public MigrateResult migrate() {
        return flyway.migrate();
    }
}
