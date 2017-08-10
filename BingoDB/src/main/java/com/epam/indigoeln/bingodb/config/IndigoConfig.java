package com.epam.indigoeln.bingodb.config;

import com.epam.indigo.Indigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndigoConfig {

    private final IndigoProvider indigoProvider;

    @Autowired
    public IndigoConfig(IndigoProvider indigoProvider) {
        this.indigoProvider = indigoProvider;
    }

    @Bean
    public Indigo moleculeIndigo() {
        return indigoProvider.indigo();
    }

    @Bean
    public Indigo reactionIndigo() {
        return indigoProvider.indigo();
    }
}
