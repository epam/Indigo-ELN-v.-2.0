package com.epam.indigoeln.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Profile("cors")
public class CorsConfiguration extends WebMvcConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsConfiguration.class);

    @Value("${cors.mapping}")
    private String mapping;

    @Value("${cors.origin}")
    private String origin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(mapping).allowedOrigins(origin);
        LOGGER.warn("Enabled CORS mapping '" + mapping + "' for origin '" + origin + "'");
    }
}
