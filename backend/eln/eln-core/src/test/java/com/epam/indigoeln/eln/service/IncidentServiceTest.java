package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.client.IncidentClient;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class IncidentServiceTest extends ELNBaseTest {

    private IncidentClient incidentClient;
    private Path incidentDir;

    @BeforeEach
    void setUp() throws IOException {
        incidentClient = buildClient(IncidentClient.class);
        incidentDir = Path.of(ConfigProvider.getConfig().getValue("eln.incident.directory", String.class));
        clearDirectory();
    }

    @AfterEach
    void tearDown() throws IOException {
        clearDirectory();
    }

    @Test
    void testCreateReportDescriptionOnly() throws IOException {
        incidentClient.createIncidentReport("Something went wrong");

        List<Path> jsonFiles = listJsonFiles();
        assertThat(jsonFiles).hasSize(1);

        JsonNode report = readReport(jsonFiles.getFirst());
        assertThat(report.path("username").asText()).isEqualTo(JOHN_USERNAME);
        assertThat(report.path("description").asText()).isEqualTo("Something went wrong");
        assertThat(report.path("incidentTime").asText()).isNotBlank();
        assertThat(report.has("experimentSnapshot")).isFalse();
        assertThat(report.has("mutation")).isFalse();
        assertThat(report.has("attachmentFilename")).isFalse();
    }

    @Test
    void testCreateReportRecordsActingUser() throws IOException {
        withUser(BART_USERNAME, () ->
                incidentClient.createIncidentReport("Bart's report"));

        JsonNode report = readReport(listJsonFiles().getFirst());
        assertThat(report.path("username").asText()).isEqualTo(BART_USERNAME);
    }

    @Test
    void testCreateReportWithExperimentSnapshot() throws IOException {
        ExperimentDetailsDTO experiment = createExperiment();

        incidentClient.createIncidentReport("Experiment broke", experiment.getId(), null, null, null);

        JsonNode report = readReport(listJsonFiles().getFirst());
        assertThat(report.path("experimentSnapshot").isMissingNode()).isFalse();
        assertThat(report.path("experimentSnapshot").path("status").asText()).isNotBlank();
        assertThat(report.path("experimentSnapshot").path("revision").asInt()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCreateReportWithMutationJson() throws IOException {
        String mutationJson = "{\"type\":\"CreateExperiment\",\"templateId\":\"00000000-0000-0000-0000-000000000001\"}";

        incidentClient.createIncidentReport("Mutation failed", null, mutationJson, null, null);

        JsonNode report = readReport(listJsonFiles().getFirst());
        assertThat(report.path("mutation").isMissingNode()).isFalse();
        assertThat(report.path("mutation").path("type").asText()).isEqualTo("CreateExperiment");
    }

    @Test
    void testCreateReportWithAttachment() throws IOException {
        byte[] screenshot = "fake-screenshot-data".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport("UI glitch", null, null, screenshot, "screenshot.png");

        List<Path> jsonFiles = listJsonFiles();
        assertThat(jsonFiles).hasSize(1);

        JsonNode report = readReport(jsonFiles.getFirst());
        String attachmentFilename = report.path("attachmentFilename").asText();
        assertThat(attachmentFilename).isNotBlank().endsWith("-screenshot.png");

        Path savedAttachment = incidentDir.resolve(attachmentFilename);
        assertThat(savedAttachment).exists();
        assertThat(Files.readAllBytes(savedAttachment)).isEqualTo(screenshot);
    }

    @Test
    void testCreateReportWithAllFields() throws IOException {
        ExperimentDetailsDTO experiment = createExperiment();
        String mutationJson = "{\"type\":\"EditExperimentAttributes\"}";
        byte[] screenshot = "screenshot-bytes".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport("Full report", experiment.getId(), mutationJson, screenshot, "screen.png");

        List<Path> jsonFiles = listJsonFiles();
        assertThat(jsonFiles).hasSize(1);

        JsonNode report = readReport(jsonFiles.getFirst());
        assertThat(report.path("username").asText()).isEqualTo(JOHN_USERNAME);
        assertThat(report.path("description").asText()).isEqualTo("Full report");
        assertThat(report.path("incidentTime").asText()).isNotBlank();
        assertThat(report.path("experimentSnapshot").isMissingNode()).isFalse();
        assertThat(report.path("mutation").path("type").asText()).isEqualTo("EditExperimentAttributes");
        assertThat(report.path("attachmentFilename").asText()).endsWith("-screen.png");
    }

    @Test
    void testEachReportGetsUniqueFile() throws IOException {
        incidentClient.createIncidentReport("First report");
        incidentClient.createIncidentReport("Second report");

        assertThat(listJsonFiles()).hasSize(2);
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

    private List<Path> listJsonFiles() throws IOException {
        Files.createDirectories(incidentDir);
        try (var stream = Files.list(incidentDir)) {
            return stream
                    .filter(f -> f.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
    }

    private JsonNode readReport(Path file) throws IOException {
        return FeignUtil.OBJECT_MAPPER.readTree(file.toFile());
    }

    private void clearDirectory() throws IOException {
        if (Files.exists(incidentDir)) {
            try (var stream = Files.list(incidentDir)) {
                stream.forEach(f -> {
                    try {
                        Files.deleteIfExists(f);
                    } catch (IOException ignore) {
                    }
                });
            }
        }
    }
}
