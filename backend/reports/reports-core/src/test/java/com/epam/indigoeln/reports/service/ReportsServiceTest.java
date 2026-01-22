package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.epam.indigoeln.test.BaseTest;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


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
        ProjectDTO project = new ProjectDTO();
        project.setName("Demo project");
        ExperimentDetailsDTO experiment = new ExperimentDetailsDTO();
        experiment.setName("00000001-0001");
        experiment.setCreatedBy(new UserRef(UUID.randomUUID(), "test", "Test User"));
        experiment.setStatus(ExperimentStatus.OPEN);
        experiment.setCreatedAt(ZonedDateTime.of(2025, 2, 19, 8, 41, 48, 0, ZoneId.of("UTC")));
        experiment.setTherapeuticArea(new DictionaryItemRef(UUID.randomUUID(), "Diabet"));
        experiment.setProjectCode(new DictionaryItemRef(UUID.randomUUID(), "Code 1"));
        experiment.setDescription("To a suspension of salicylic acid (2 g) in acetic anhydride (4.5 mL) in a conical flask add anhydrous sodium acetate\n(0.4 g) with stirring.");
        return List.of(new ReportsAPI.ExperimentReportDataDTO(
                project,
                experiment,
                new String(ModelUtil.loadResource(ReportsServiceTest.class, "/experiment-image.svg"), StandardCharsets.UTF_8)
        ));

        // Reaction Details, Experiment Subject / Title = O-acetylation of salicylic acid
//        experiment.setCreatedAt(ZonedDateTime.parse("2025-07-29T13:22:05Z"));
        // Reaction Details, Therapeutic Area = Diabetes
        // other Reaction Details fields?
//        experiment.setPicture(ModelUtil.loadResource(ReportingServiceTest.class, "/experiment-image.svg"));
    }

    @Test
    void testReport() throws Exception {
        ReportsAPI.ExperimentReportDataDTO data = new ReportsAPI.ExperimentReportDataDTO(null, null, null);
        try (Response response = reportsClient.generateExperimentReport(fillExperimentDataForJasperReportsStudio().getFirst())) {
            Files.write(Paths.get("report.pdf"), response.readEntity(byte[].class));
        }
    }
}
