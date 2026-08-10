package com.epam.indigoeln.eln.service.incident;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.client.IncidentClient;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentRequest;
import com.epam.indigoeln.eln.model.NotebookDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectDetailsDTO;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import feign.form.FormData;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class IncidentReportServiceTest extends ELNBaseTest {

    IncidentClient incidentClient;

    @BeforeEach
    void setUp() {
        incidentClient = buildClient(IncidentClient.class);
        testSupportClient.storageMkdir("incidents");
        testSupportClient.storageClear("incidents");
    }

    @Test
    void testCreateReportBasicFieldsOnly() throws IOException {
        incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder()
                .url("http://localhost/")
                .message("Something went wrong")
                .build()
        );

        JsonNode report = findReport();
        assertThat(report.path("username").asText()).isEqualTo(JOHN_USERNAME);
        assertThat(report.path("url").asText()).isEqualTo("http://localhost/");
        assertThat(report.path("message").asText()).isEqualTo("Something went wrong");
        assertThat(report.path("incidentTime").asText()).isNotBlank();
        assertThat(report.has("experimentSnapshot")).isFalse();
        assertThat(report.has("requestBody")).isFalse();
        assertThat(report.has("attachmentFilename")).isFalse();
    }

    @Test
    void testCreateReportRecordsActingUser() throws IOException {
        withUser(BART_USERNAME, () ->
                incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder().message("Bart's report").build()));

        JsonNode report = findReport();
        assertThat(report.path("username").asText()).isEqualTo(BART_USERNAME);
    }

    @Test
    void testCreateReportWithExperimentSnapshot() throws IOException {
        ProjectDetailsDTO project = getOrCreateProject("IncidentReportServiceTest");
        NotebookDetailsDTO notebook = createNotebook(project.getId());
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder()
                .message("Experiment broke")
                .experiment("{\"id\": \"%s\"}".formatted(experiment.getId()))
                .build()
        );

        JsonNode report = findReport();
        assertThat(report.path("experimentFrontend").path("id").asText()).isEqualTo(experiment.getId().toString());
        assertThat(report.path("experimentBackend").path("status").asText()).isEqualTo("OPEN");
        assertThat(report.path("experimentBackend").path("revision").asInt()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCreateReportWithRequestAndResponse() throws IOException {
        String mutationJson = "{\"type\":\"CreateExperiment\",\"templateId\":\"00000000-0000-0000-0000-000000000001\"}";

        incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder()
                .message("Mutation failed")
                .requestURL("http://localhost:8080/api/find")
                .requestMethod("POST")
                .requestBody(mutationJson)
                .responseBody("{\"error\": \"Not found!\"}")
                .build()
        );

        JsonNode report = findReport();
        assertThat(report.path("requestURL").asText()).isEqualTo("http://localhost:8080/api/find");
        assertThat(report.path("requestMethod").asText()).isEqualTo("POST");
        assertThat(report.path("requestBody").path("type").asText()).isEqualTo("CreateExperiment");
        assertThat(report.path("responseBody").path("error").asText()).isEqualTo("Not found!");
    }

    @Test
    void testCreateReportWithAttachment() throws IOException {
        byte[] screenshot = "fake-screenshot-data".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder()
                .message("UI glitch")
                .file(new FormData(MediaType.APPLICATION_OCTET_STREAM, "screenshot.png", screenshot))
                .build()
        );

        JsonNode report = findReport();
        String attachmentFilename = report.path("attachmentFilename").asText();
        assertThat(attachmentFilename).isNotBlank().endsWith("-screenshot.png");

        Response response = testSupportClient.storageRead(attachmentFilename);
        assertThat((byte[]) response.getEntity()).isEqualTo(screenshot);
    }

    @Test
    void testCreateReportWithAttachmentUsesSafeFilename() throws IOException {
        byte[] content = "malicious-filename-content".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder()
                .message("Suspicious upload")
                .file(new FormData(MediaType.TEXT_PLAIN, "../../evil.sh", content))
                .build()
        );

        JsonNode report = findReport();
        String attachmentFilename = report.path("attachmentFilename").asText();
        assertThat(attachmentFilename).isNotBlank().doesNotContain("..").endsWith("-evil.sh");

        Response response = testSupportClient.storageRead(attachmentFilename);
        assertThat((byte[]) response.getEntity()).isEqualTo(content);
    }

    @Test
    void testEachReportGetsUniqueFile() {
        incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder().message("First report").build());
        incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder().message("Second report").build());

        assertThat(findReportFiles()).hasSize(2);
    }

    @Test
    void testCreateReportRejectsBlankDescription() {
        assertThatClientCall(() -> incidentClient.createIncidentReport(IncidentClient.ClientIncidentReportForm.builder().message("").build()))
                .isBadRequest("must not be blank");
    }

    private List<String> findReportFiles() {
        return testSupportClient.storageList("incidents").stream()
                .filter(f -> f.endsWith(".json"))
                .toList();
    }

    private JsonNode findReport() throws IOException {
        List<String> files = findReportFiles();
        assertThat(files).hasSize(1);
        Response response = testSupportClient.storageRead(files.getFirst());
        return FeignUtil.OBJECT_MAPPER.readTree((byte[]) response.getEntity());
    }
}
