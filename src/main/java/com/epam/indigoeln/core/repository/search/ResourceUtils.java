package com.epam.indigoeln.core.repository.search;

import com.epam.indigoeln.IndigoRuntimeException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static String loadFunction(Resource resource) {

        if (!resource.exists()) {
            throw new IndigoRuntimeException(String.format("Resource %s not found!", resource));
        }

        InputStream inputStream;
        try {
            inputStream = resource.getInputStream();
        } catch (IOException e) {
            throw new IndigoRuntimeException(String.format("Cannot read file %s!", resource), e);
        }
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

}
