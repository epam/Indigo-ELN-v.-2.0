package com.epam.indigoeln.config.dbchangelogs;

import org.springframework.core.env.Environment;

public class ChangeLogBase {

    private static Environment environment;

    public static void setEnvironment(Environment environment) {
        ChangeLogBase.environment = environment;
    }

    public static Environment getEnvironment() {
        return environment;
    }
}
