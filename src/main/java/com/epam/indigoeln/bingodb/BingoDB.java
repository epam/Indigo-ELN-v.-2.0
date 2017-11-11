package com.epam.indigoeln.bingodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Application main class
 */
@SpringBootApplication
public class BingoDB extends SpringBootServletInitializer {

    /**
     * Application entry point
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BingoDB.class, args);
    }

    /**
     * web.xml replacement with Spring Boot's Servlet Initializer
     *
     * @param builder Builder for SpringApplication
     * @return Builder for SpringApplication
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BingoDB.class);
    }
}
