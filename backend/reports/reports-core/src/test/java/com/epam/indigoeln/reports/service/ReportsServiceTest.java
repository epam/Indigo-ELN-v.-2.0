package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reports.api.ReagentDTO;
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
import java.nio.file.Path;
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
        String experimentJson = Files.readString(Path.of("src/test/resources/experiment-model.json"));
        ProjectDTO project = new ProjectDTO();
        project.setName("Demo project");
        ExperimentDetailsDTO experiment = null;
        try {
            experiment = objectMapper.readValue(experimentJson, ExperimentDetailsDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        ExperimentDetailsDTO experiment = new ExperimentDetailsDTO();
        experiment.setName("00000001-0001");
        experiment.setCreatedBy(new UserRef(UUID.randomUUID(), "test", "Test User"));
        experiment.setStatus(ExperimentStatus.OPEN);
        experiment.setCreatedAt(ZonedDateTime.of(2025, 2, 19, 8, 41, 48, 0, ZoneId.of("UTC")));
        experiment.setTherapeuticArea(new DictionaryItemRef(UUID.randomUUID(), "Diabet"));
        experiment.setProjectCode(new DictionaryItemRef(UUID.randomUUID(), "Code 1"));
        experiment.setDescription("To a suspension of salicylic acid (2 g) in acetic anhydride (4.5 mL) in a conical flask add anhydrous sodium acetate\n(0.4 g) with stirring.");
        */
        return List.of(new ReportsAPI.ExperimentReportDataDTO(
                project,
                experiment,
                new String(ModelUtil.loadResource(ReportsServiceTest.class, "/experiment-image.svg"), StandardCharsets.UTF_8),
                ReagentDTO.allExperimentReagents(experiment.getModel())
        ));

        // Reaction Details, Experiment Subject / Title = O-acetylation of salicylic acid
//        experiment.setCreatedAt(ZonedDateTime.parse("2025-07-29T13:22:05Z"));
        // Reaction Details, Therapeutic Area = Diabetes
        // other Reaction Details fields?
//        experiment.setPicture(ModelUtil.loadResource(ReportingServiceTest.class, "/experiment-image.svg"));
    }

    @Test
    void testReport() throws Exception {
        ReportsAPI.ExperimentReportDataDTO data = new ReportsAPI.ExperimentReportDataDTO(null, null, null, null);
        try (Response response = reportsClient.generateExperimentReport(fillExperimentDataForJasperReportsStudio().getFirst())) {
            Files.write(Paths.get("report.pdf"), response.readEntity(byte[].class));
        }
    }
}
