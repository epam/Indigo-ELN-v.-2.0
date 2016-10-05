package com.epam.indigoeln.core.service.indigo;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import org.springframework.stereotype.Component;

@Component
public class IndigoProvider {

    public Indigo getIndigo() {
        Indigo indigo = new Indigo();
        indigo.setOption("ignore-stereochemistry-errors", "true");

        return indigo;
    }

    public IndigoRenderer getRenderer(Indigo indigo) {
        IndigoRenderer renderer = new IndigoRenderer(indigo);

        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);

        return renderer;
    }
}
