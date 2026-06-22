package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.client.IncidentClient;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.incident.IncidentReport;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.test.FeignUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class IncidentReportServiceTest extends ELNBaseTest {

    private IncidentClient incidentClient;
    private Path incidentDir;
    private List<Path> existingReports;

    @BeforeEach
    void setUp() throws IOException {
        incidentClient = buildClient(IncidentClient.class);
        incidentDir = Path.of(ConfigProvider.getConfig().getValue("eln.incident.directory", String.class));
        existingReports = listAllJsonFiles();
    }

    @Test
    void testCreateReportMessageOnly() throws IOException {
        createIncidentReport("Something went wrong");

        List<Path> jsonFiles = listJsonFiles();
        assertThat(jsonFiles).hasSize(1);

        IncidentReport report = readReport(jsonFiles.getFirst());
        assertThat(report.getUsername()).isEqualTo(JOHN_USERNAME);
        assertThat(report.getMessage()).isEqualTo("Something went wrong");
        assertThat(report.getIncidentTime()).isNotNull();
        assertThat(report.getExperimentFrontend()).isNull();
        assertThat(report.getRequestBody()).isNull();
        assertThat(report.getAttachmentFilename()).isNull();
    }

    @Test
    void testCreateReportRecordsActingUser() throws IOException {
        withUser(BART_USERNAME, () ->
                createIncidentReport("Bart's report")
        );

        IncidentReport report = readReport(listJsonFiles().getFirst());
        assertThat(report.getUsername()).isEqualTo(BART_USERNAME);
    }

    @Test
    void testCreateReportWithExperimentSnapshot() throws IOException {
        ExperimentDetailsDTO experiment = createExperiment();

        incidentClient.createIncidentReport(
                null, "Experiment broke", serialize(experiment), null, null, null, null, null, null
        );

        IncidentReport report = readReport(listJsonFiles().getFirst());
        assertThat(report.getExperimentFrontend()).isNotNull();
        assertThat(report.getExperimentBackend()).isNotNull();
        assertThat(report.getExperimentBackend().getStatus()).isNotNull();
        assertThat(report.getExperimentBackend().getRevision()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCreateReportWithMutationJson() throws IOException {
        Mutation mutation = new ExperimentMutation.CreateExperiment(emptyTemplateID, null, null, null);

        incidentClient.createIncidentReport(
                null, "Mutation failed", null, null, null, serialize(mutation), null, null, null
        );

        IncidentReport report = readReport(listJsonFiles().getFirst());
        assertThat(report.getRequestBody()).isNotNull();
        assertThat(report.getRequestBody().path("type").asText()).isEqualTo("CreateExperiment");
    }

    @Test
    void testCreateReportWithAttachment() throws IOException {
        byte[] screenshot = "fake-screenshot-data".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport(
                null, "UI glitch", null, null, null, null, null, screenshot, "screenshot.png"
        );

        List<Path> jsonFiles = listJsonFiles();
        assertThat(jsonFiles).hasSize(1);

        IncidentReport report = readReport(jsonFiles.getFirst());
        assertThat(report.getAttachmentFilename()).isNotBlank().endsWith("-screenshot.png");

        Path savedAttachment = incidentDir.resolve(report.getAttachmentFilename());
        assertThat(savedAttachment).exists();
        assertThat(Files.readAllBytes(savedAttachment)).isEqualTo(screenshot);
    }

    @Test
    void testCreateReportWithAttachmentUsesSafeFilename() throws IOException {
        byte[] content = "malicious-filename-content".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport(
                null, "Suspicious upload", null, null, null, null, null, content, "../../evil.sh"
        );

        IncidentReport report = readReport(listJsonFiles().getFirst());
        String attachmentFilename = report.getAttachmentFilename();
        assertThat(attachmentFilename).isNotBlank().doesNotContain("..").endsWith("-evil.sh");

        Path savedAttachment = incidentDir.resolve(attachmentFilename);
        assertThat(savedAttachment).exists();
        assertThat(Files.readAllBytes(savedAttachment)).isEqualTo(content);
    }

    @Test
    void testCreateReportWithAllFields() throws IOException {
        ExperimentDetailsDTO experiment = createExperiment();
        Mutation mutation = new ExperimentMutation.CreateExperimentAttachment(UUID.randomUUID());
        byte[] screenshot = "screenshot-bytes".getBytes(StandardCharsets.UTF_8);

        incidentClient.createIncidentReport(
                "/experiments/1", "Full report", serialize(experiment), "/api/experiments/1/mutate", "POST", serialize(mutation), null, screenshot, "screen.png"
        );

        List<Path> jsonFiles = listJsonFiles();
        assertThat(jsonFiles).hasSize(1);

        IncidentReport report = readReport(jsonFiles.getFirst());
        assertThat(report.getUsername()).isEqualTo(JOHN_USERNAME);
        assertThat(report.getMessage()).isEqualTo("Full report");
        assertThat(report.getIncidentTime()).isNotNull();
        assertThat(report.getExperimentFrontend()).isNotNull();
        assertThat(report.getExperimentBackend()).isNotNull();
        assertThat(report.getRequestBody()).isNotNull();
        assertThat(report.getRequestBody().path("type").asText()).isEqualTo("CreateExperimentAttachment");
        assertThat(report.getAttachmentFilename()).endsWith("-screen.png");
    }

    @Test
    void testEachReportGetsUniqueFile() throws IOException {
        createIncidentReport("First report");
        createIncidentReport("Second report");

        assertThat(listJsonFiles()).hasSize(2);
    }

    @Test
    void testCreateReportRejectsBlankDescription() {
        assertThatClientCall(() -> createIncidentReport(""))
                .isBadRequest("must not be blank");
    }

    @Test
    void testCreateReportWithNonExistentExperimentReturnsForbidden() throws IOException {
        Map<String, Object> content = Map.of("id", UUID.randomUUID());
        incidentClient.createIncidentReport(
                null, "Error", serialize(content), null, null, null, null, null, null
        );

        List<Path> jsonFiles = listJsonFiles();
        assertThat(jsonFiles).hasSize(1);

        IncidentReport report = readReport(jsonFiles.getFirst());
        assertThat(report.getExperimentBackend()).isNull();
    }

    private ExperimentDetailsDTO createExperiment() {
        ProjectDetailsDTO project = projectClient.createProject(
                new ProjectRequest("IncidentTest-" + UUID.randomUUID()));
        NotebookDetailsDTO notebook = notebookClient.createNotebook(
                project.getId(), new NotebookRequest(nextNotebookName()));
        return experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
    }

    private List<Path> listJsonFiles() throws IOException {
        return listAllJsonFiles().stream()
                .filter(f -> !existingReports.contains(f))
                .toList();
    }

    private List<Path> listAllJsonFiles() throws IOException {
        Files.createDirectories(incidentDir);
        try (var stream = Files.list(incidentDir)) {
            return stream
                    .filter(f -> f.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
    }

    @SneakyThrows
    private String serialize(Object object) {
        return FeignUtil.OBJECT_MAPPER.writeValueAsString(object);
    }

    private IncidentReport readReport(Path file) throws IOException {
        return FeignUtil.OBJECT_MAPPER.readValue(file.toFile(), IncidentReport.class);
    }

    private void createIncidentReport(String message) {
        incidentClient.createIncidentReport(null, message, null, null, null, null, null, null, null);
    }
}
