package com.epam.indigoeln.eln.service.incident;

import com.epam.indigoeln.common.config.UserHolder;
import com.epam.indigoeln.eln.api.IncidentReportForm;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class IncidentReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss").withZone(ZoneId.systemDefault());

    @ConfigProperty(name = "eln.incident.directory")
    String incidentDirectory;

    @Inject
    UserHolder userHolder;

    @Inject
    ExperimentService experimentService;

    @Inject
    ObjectMapper objectMapper;

    @SneakyThrows
    public void createIncidentReport(IncidentReportForm form) {
        Path directory = Path.of(incidentDirectory);
        Files.createDirectories(directory);
        UUID incidentId = UUID.randomUUID();
        Instant incidentTime = Instant.now();
        String baseName = "incident-%s-%s-".formatted(DATE_FORMATTER.format(incidentTime), incidentId);

        IncidentReport report = new IncidentReport();
        report.setIncidentTime(incidentTime);
        report.setUsername(userHolder.getUserName());
        report.setMessage(form.getMessage());
        if (form.getExperiment() != null) {
            try {
                JsonNode json = objectMapper.readTree(form.getExperiment());
                report.setExperimentFrontend(json);
                UUID experimentId = UUID.fromString(json.get("id").textValue());
                report.setExperimentBackend(experimentService.getExperimentSnapshot(experimentId));
            } catch (Exception e) {
                log.error("Failed to load experiment", e);
            }
        }
        report.setRequestURL(form.getRequestURL());
        report.setRequestMethod(form.getRequestMethod());
        if (form.getRequestBody() != null) {
            try {
                report.setRequestBody(objectMapper.readTree(form.getRequestBody()));
            } catch (Exception e) {
                log.error("Failed to parse request body", e);
            }
        }
        report.setResponseBody(form.getResponseBody());
        if (form.getFile() != null) {
            try {
                report.setAttachmentFilename(saveAttachment(directory, baseName, form.getFile()));
            } catch (Exception e) {
                log.error("Failed to save attachment", e);
            }
        }

        Path reportFile = directory.resolve(baseName + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile.toFile(), report);
        log.info("Incident report saved: {}", reportFile);
    }

    @SneakyThrows
    private String saveAttachment(Path directory, String baseName, FileUpload file) {
        String attachmentName = Path.of(file.fileName()).getFileName().toString();
        String filename = baseName + attachmentName;
        Files.copy(file.filePath(), directory.resolve(filename));
        return filename;
    }
}
