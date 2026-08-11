package com.epam.indigoeln.common.storage;

import java.util.List;

public interface FileStorage {

    List<String> list(String key);
    void put(String key, byte[] bytes);
    byte[] get(String key);
}
