package com.epam.indigoeln.eln.service.incident;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.client.IncidentClient;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.test.FeignUtil;
import com.epam.indigoeln.test.StorageClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class IncidentReportServiceTest extends ELNBaseTest {

    IncidentClient incidentClient;

    static StorageClient storage = StorageClient.instance();

    @BeforeEach
    void setUp() throws IOException {
        incidentClient = buildClient(IncidentClient.class);
        storage.mkdir("incidents");
        storage.clearDir("incidents");
    }

    @Test
    void testCreateReportDescriptionOnly() throws IOException {
        incidentClient.createIncidentReport("Something went wrong");

        JsonNode report = findReport();
        assertThat(report.path("username").asText()).isEqualTo(JOHN_USERNAME);
        assertThat(report.path("message").asText()).isEqualTo("Something went wrong");
        assertThat(report.path("incidentTime").asText()).isNotBlank();
        assertThat(report.has("experimentSnapshot")).isFalse();
        assertThat(report.has("mutation")).isFalse();
        assertThat(report.has("attachmentFilename")).isFalse();
    }

    @Test
    void testCreateReportRecordsActingUser() throws IOException {
        withUser(BART_USERNAME, () ->
                incidentClient.createIncidentReport("Bart's report"));

        JsonNode report = findReport();
        assertThat(report.path("username").asText()).isEqualTo(BART_USERNAME);
    }

    @Test
    void testCreateReportWithExperimentSnapshot() throws IOException {
        ExperimentDetailsDTO experiment = createExperiment();

        incidentClient.createIncidentReport("Experiment broke", experiment.getId(), null, null, null);

        JsonNode report = findReport();
        assertThat(report.path("experimentSnapshot").isMissingNode()).isFalse();
        assertThat(report.path("experimentSnapshot").path("status").asText()).isNotBlank();
        assertThat(report.path("experimentSnapshot").path("revision").asInt()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCreateReportWithMutationJson() throws IOException {
        String mutationJson = "{\"type\":\"CreateExperiment\",\"templateId\":\"00000000-0000-0000-0000-000000000001\"}";

        incidentClient.createIncidentReport("Mutation failed", null, mutationJson, null, null);

        JsonNode report = findReport();
        assertThat(report.path("mutation").isMissingNode()).isFalse();
        assertThat(report.path("mutation").path("type").asText()).isEqualTo("CreateExperiment");
    }

    @Test
    void testCreateReportWithAttachment() throws IOException {
        byte[] screenshot = "fake-screenshot-data".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport("UI glitch", null, null, screenshot, "screenshot.png");

        JsonNode report = findReport();
        String attachmentFilename = report.path("attachmentFilename").asText();
        assertThat(attachmentFilename).isNotBlank().endsWith("-screenshot.png");

        byte[] bytes = storage.read(attachmentFilename);
        assertThat(bytes).isEqualTo(screenshot);
    }

    @Test
    void testCreateReportWithAttachmentUsesSafeFilename() throws IOException {
        byte[] content = "malicious-filename-content".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport("Suspicious upload", null, null, content, "../../evil.sh");

        JsonNode report = findReport();
        String attachmentFilename = report.path("attachmentFilename").asText();
        assertThat(attachmentFilename).isNotBlank().doesNotContain("..").endsWith("-evil.sh");

        byte[] bytes = storage.read(attachmentFilename);
        assertThat(bytes).isEqualTo(content);
    }

    @Test
    void testCreateReportWithAllFields() throws IOException {
        ExperimentDetailsDTO experiment = createExperiment();
        String mutationJson = "{\"type\":\"EditExperimentAttributes\"}";
        byte[] screenshot = "screenshot-bytes".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport("Full report", experiment.getId(), mutationJson, screenshot, "screen.png");

        JsonNode report = findReport();
        assertThat(report.path("username").asText()).isEqualTo(JOHN_USERNAME);
        assertThat(report.path("message").asText()).isEqualTo("Full report");
        assertThat(report.path("incidentTime").asText()).isNotBlank();
        assertThat(report.path("experimentSnapshot").isMissingNode()).isFalse();
        assertThat(report.path("mutation").path("type").asText()).isEqualTo("EditExperimentAttributes");
        assertThat(report.path("attachmentFilename").asText()).endsWith("-screen.png");
    }

    @Test
    void testEachReportGetsUniqueFile() throws IOException {
        incidentClient.createIncidentReport("First report");
        incidentClient.createIncidentReport("Second report");

        assertThat(findReportFiles()).hasSize(2);
    }

    @Test
    void testCreateReportRejectsBlankDescription() {
        assertThatClientCall(() -> incidentClient.createIncidentReport(""))
                .isBadRequest("must not be blank");
    }

    @Test
    void testCreateReportWithNonExistentExperimentReturnsForbidden() {
        UUID missingId = UUID.randomUUID();
        assertThatClientCall(() ->
                incidentClient.createIncidentReport("Error", missingId, null, null, null))
                .isForbidden("not found or not accessible");
    }

    private ExperimentDetailsDTO createExperiment() {
        ProjectDetailsDTO project = projectClient.createProject(
                new ProjectRequest("IncidentTest-" + UUID.randomUUID()));
        NotebookDetailsDTO notebook = notebookClient.createNotebook(
                project.getId(), new NotebookRequest(nextNotebookName()));
        return experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
    }

    private List<String> findReportFiles() {
        return storage.listFiles("incidents").stream()
                .filter(f -> f.endsWith(".json"))
                .toList();
    }

    private JsonNode findReport() throws IOException {
        List<String> files = findReportFiles();
        assertThat(files).hasSize(1);
        byte[] bytes = storage.read(files.getFirst());
        return FeignUtil.OBJECT_MAPPER.readTree(bytes);
    }
}
