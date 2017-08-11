package com.epam.indigoeln.bingodb.config;

import com.epam.indigo.Indigo;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndigoProvider {

    public Indigo indigo() {
        Indigo indigo = new Indigo();
        indigo.setOption("ignore-stereochemistry-errors", "true");

        return indigo;
    }
}
