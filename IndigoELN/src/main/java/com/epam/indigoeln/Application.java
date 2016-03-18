package com.epam.indigoeln;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.core.env.SimpleCommandLinePropertySource;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static class Profile {
        public static final String DEV = "dev";
    }

    /**
     * Main method, used to run the application.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);

        if (!source.containsProperty("spring.profiles.active") && !System.getenv().containsKey("SPRING_PROFILES_ACTIVE")) {
            app.setAdditionalProfiles(Application.Profile.DEV);
        }

        app.run(args);
    }

    /**
     * Run application in servlet container
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }
}
