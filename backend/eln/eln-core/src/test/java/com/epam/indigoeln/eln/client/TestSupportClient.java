package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.api.TestSupportAPI;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.nio.file.Path;

public interface TestSupportClient extends TestSupportAPI {
}
