package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.epam.indigoeln.test.FeignUtil.OBJECT_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

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
                OBJECT_MAPPER.readValue(ModelUtil.loadResource("/experiment-input.json"), ReportsAPI.ExperimentReportDataDTO.class)
        );
    }

    @Test
    void testReport() throws Exception {
        try (Response response = reportsClient.generateExperimentReport(fillExperimentDataForJasperReportsStudio().getFirst())) {
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).contains("application/pdf");
            byte[] pdf = response.readEntity(byte[].class);
            assertThat(pdf).isNotEmpty();
            assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
            Files.write(Paths.get("build/report.pdf"), pdf);
        }
    }
}
