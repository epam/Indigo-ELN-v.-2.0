package com.epam.indigoeln.common.storage;

import jakarta.validation.constraints.NotEmpty;

public interface FileStorage {
    void put(String key, byte[] bytes);

    byte[] get(@NotEmpty String name);

    String createPresignedUrl(String keyName);
}
