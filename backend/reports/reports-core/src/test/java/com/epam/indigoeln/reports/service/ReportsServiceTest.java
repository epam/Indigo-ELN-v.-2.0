package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.epam.indigoeln.test.FeignUtil.OBJECT_MAPPER;

@QuarkusTest
@TestSecurity(user = BaseTest.ADMIN_USERNAME)
public class ReportsServiceTest extends BaseTest {

    ReportsClient reportsClient;

    @BeforeAll
    void setUpAll() {
        reportsClient = buildClient(ReportsClient.class);
    }

    @SneakyThrows
    @SuppressWarnings("unused")
    public static List<ReportsAPI.ExperimentReportDataDTO> fillExperimentDataForJasperReportsStudio() {
        return List.of(
                OBJECT_MAPPER.readValue(ModelUtil.loadResource(ReportsServiceTest.class, "/experiment-input.json"), ReportsAPI.ExperimentReportDataDTO.class)
        );
    }

    @Test
    void testReport() throws Exception {
        ReportsAPI.ExperimentReportDataDTO data = new ReportsAPI.ExperimentReportDataDTO(null, null, null);
        try (Response response = reportsClient.generateExperimentReport(fillExperimentDataForJasperReportsStudio().getFirst())) {
            Files.write(Paths.get("build/report.pdf"), response.readEntity(byte[].class));
        }
    }
}
