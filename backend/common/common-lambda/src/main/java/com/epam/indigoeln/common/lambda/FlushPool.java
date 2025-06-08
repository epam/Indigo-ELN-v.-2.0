package com.epam.indigoeln.common.lambda;

import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.crac.Core;
import org.crac.Resource;

@ApplicationScoped
public class FlushPool implements Resource {

    @Inject
    AgroalDataSource dataSource;

    void onStart(@Observes StartupEvent ev) {
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(org.crac.Context<? extends Resource> context) {
    }

    @Override
    public void afterRestore(org.crac.Context<? extends Resource> context) {
        dataSource.flush(AgroalDataSource.FlushMode.ALL);
    }
}
