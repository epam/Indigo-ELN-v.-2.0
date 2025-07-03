package com.epam.indigoeln.compound.config;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoAPIImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class IndigoProvider {

    @Produces
    @ApplicationScoped
    IndigoAPI getIndigo() {
        String path = System.getenv("NATIVE_LIB_PATH");
        if (path == null) {
            throw new RuntimeException("NATIVE_LIB_PATH not defined");
        }
        Indigo indigo = new Indigo(path);
        indigo.setOption("ignore-stereochemistry-errors", "true");
        IndigoRenderer indigoRenderer = new IndigoRenderer(indigo);
        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);
        return new IndigoAPIImpl(indigo, indigoRenderer);
    }
}
