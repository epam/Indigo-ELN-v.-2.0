package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.storage.FileStorage;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.TestSupportAPI;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Path(BaseAPI.BASE_PATH)
public class TestSupportResource implements TestSupportAPI {

    @Inject
    FileStorage storage;

    @Override
    public List<String> storageList(String path) {
        validateInTests();
        return storage.list(path);
    }

    @Override
    public Response storageRead(String path) {
        validateInTests();
        byte[] bytes = storage.get(path);
        return Response.ok(bytes).build();
    }

    private static void validateInTests() {
        if (!ConfigUtils.isProfileActive("devtest")) {
            throw new AccessDeniedException("Can only be called in tests");
        }
    }
}
