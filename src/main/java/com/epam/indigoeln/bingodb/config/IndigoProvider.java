package com.epam.indigoeln.bingodb.config;

import com.epam.indigo.Indigo;
import org.springframework.context.annotation.Configuration;

/**
 * Provider for Indigo instances creation with common properties
 */
@Configuration
public class IndigoProvider {

    /**
     * Create a new Indigo instance with common properties
     *
     * @return Indigo instance with common properties
     */
    public Indigo indigo() {
        Indigo indigo = new Indigo();
        indigo.setOption("ignore-stereochemistry-errors", "true");

        return indigo;
    }
}
