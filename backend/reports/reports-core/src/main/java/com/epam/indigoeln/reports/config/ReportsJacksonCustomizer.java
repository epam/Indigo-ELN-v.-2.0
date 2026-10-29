package com.epam.indigoeln.reports.config;

import com.epam.indigoeln.reaction.config.ReactionModelModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class ReportsJacksonCustomizer implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        objectMapper.registerModule(new ReactionModelModule());
    }
}
