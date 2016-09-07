package com.epam.indigoeln.core.service.indigo;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import org.springframework.stereotype.Component;

@Component
public class IndigoProvider {

    private Indigo indigo;

    private IndigoRenderer renderer;

    public IndigoProvider() {

        indigo = new Indigo();
        indigo.setOption("ignore-stereochemistry-errors", "true");

        renderer = new IndigoRenderer(indigo);

        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);

    }

    public Indigo getIndigo() {
        return indigo;
    }

    public IndigoRenderer getRenderer() {
        return renderer;
    }
}
