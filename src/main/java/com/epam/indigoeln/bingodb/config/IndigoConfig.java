package com.epam.indigoeln.bingodb.config;

import com.epam.indigo.Indigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Indigo instances beans
 */
@Configuration
public class IndigoConfig {

    /**
     * IndigoProvider instance which provides Indigo instances with common properties
     */
    private final IndigoProvider indigoProvider;

    /**
     * Create a new IndigoConfig instance
     *
     * @param indigoProvider IndigoProvider instance
     */
    @Autowired
    public IndigoConfig(IndigoProvider indigoProvider) {
        this.indigoProvider = indigoProvider;
    }

    /**
     * Create a new Indigo instance for molecule database Bingo instance
     *
     * @return Indigo instance for reaction database Bingo instance
     */
    @Bean
    public Indigo moleculeIndigo() {
        return indigoProvider.indigo();
    }

    /**
     * Create a new Indigo instance for reaction database Bingo instance
     *
     * @return Indigo instance for reaction database Bingo instance
     */
    @Bean
    public Indigo reactionIndigo() {
        return indigoProvider.indigo();
    }
}
