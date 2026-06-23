package com.epam.indigoeln.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.File;

@Data
public class GlobalParameters {

    private String account;
    private String region;
    @Nullable
    private String permissionBoundary;

    @SneakyThrows
    public static GlobalParameters load() {
        return new ObjectMapper().readValue(new File("global.json"), GlobalParameters.class);
    }
}
