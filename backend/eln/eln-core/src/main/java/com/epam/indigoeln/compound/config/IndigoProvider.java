package com.epam.indigoeln.compound.config;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.io.File;

@ApplicationScoped
public class IndigoProvider {

    @Produces
    @ApplicationScoped
    IndigoAPI getIndigo() {
        System.out.println("!!! IndigoProvider.getIndigo()");
        String path = System.getenv("NATIVE_LIB_PATH");
        System.out.println("!!! IndigoProvider.getIndigo(): NATIVE_LIB_PATH = " + path);
        System.out.println("!!! IndigoProvider.getIndigo(): pwd = " + new File(".").getAbsolutePath());
        if (path == null) {
//            new File(".", "lib").getAbsolutePath()
            throw new RuntimeException("NATIVE_LIB_PATH not defined");
        }
        System.out.println("!!! IndigoProvider.getIndigo(): 1");
        Indigo indigo = new Indigo(path);
        System.out.println("!!! IndigoProvider.getIndigo(): 2");
        indigo.setOption("ignore-stereochemistry-errors", "true");
        System.out.println("!!! IndigoProvider.getIndigo(): 3");
        return new IndigoAPI.Impl(indigo);
    }

    @Produces
    @ApplicationScoped
    IndigoRendererAPI getIndigoRenderer(IndigoAPI indigo) {
        System.out.println("!!! IndigoProvider.getIndigoRenderer(): 1");
        IndigoRenderer indigoRenderer = new IndigoRenderer(((IndigoAPI.Impl) indigo).getIndigo());
        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);
        System.out.println("!!! IndigoProvider.getIndigoRenderer(): 99");
        return new IndigoRendererAPI.Impl(indigoRenderer);
    }
}
