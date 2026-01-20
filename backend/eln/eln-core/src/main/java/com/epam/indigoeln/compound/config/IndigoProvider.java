package com.epam.indigoeln.compound.config;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class IndigoProvider {

    private final Supplier<Indigo> indigoSupplier = Suppliers.memoize(() -> {
        String path = System.getenv("NATIVE_LIB_PATH");
        if (path == null) {
            throw new RuntimeException("NATIVE_LIB_PATH not defined");
        }
        Indigo indigo = new Indigo(path);
        indigo.setOption("ignore-stereochemistry-errors", "true");
        return indigo;
    });

    @Produces
    @ApplicationScoped
    IndigoAPI getIndigoAPI() {
        return new IndigoAPI(indigoSupplier.get());
    }

    @Produces
    @ApplicationScoped
    IndigoRendererAPI getIndigoRendererAPI() {
        Indigo indigo = indigoSupplier.get();
        IndigoRenderer indigoRenderer = new IndigoRenderer(indigo);
        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);
        return new IndigoRendererAPI(indigo, indigoRenderer);
    }
}
