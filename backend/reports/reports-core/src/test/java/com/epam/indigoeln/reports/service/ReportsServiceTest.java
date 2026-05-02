package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.epam.indigoeln.test.BaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@QuarkusTest
@JwtSecurity
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
        ObjectMapper objectMapper = com.epam.indigoeln.test.FeignUtil.OBJECT_MAPPER;
        byte[] experimentJson = ModelUtil.loadResource(ReportsServiceTest.class, "/experiment-model.json");
        ProjectDTO project = new ProjectDTO();
        project.setName("Demo project");
        ExperimentDetailsDTO experiment = objectMapper.readValue(experimentJson, ExperimentDetailsDTO.class);

        return List.of(new ReportsAPI.ExperimentReportDataDTO(
                project,
                experiment,
                new String(ModelUtil.loadResource(ReportsServiceTest.class, "/experiment-image.svg"), StandardCharsets.UTF_8)
        ));
    }

    @Test
    void testReport() throws Exception {
        ReportsAPI.ExperimentReportDataDTO data = new ReportsAPI.ExperimentReportDataDTO(null, null, null);
        try (Response response = reportsClient.generateExperimentReport(fillExperimentDataForJasperReportsStudio().getFirst())) {
            Files.write(Paths.get("report.pdf"), response.readEntity(byte[].class));
        }
    }
}
