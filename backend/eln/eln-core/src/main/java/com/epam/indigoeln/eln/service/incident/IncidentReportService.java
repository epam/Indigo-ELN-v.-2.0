package com.epam.indigoeln.eln.service.incident;

import com.epam.indigoeln.common.config.UserHolder;
import com.epam.indigoeln.eln.api.IncidentReportForm;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class IncidentReportService {

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

        IncidentReport report = new IncidentReport();
        report.setIncidentTime(Instant.now());
        report.setUsername(userHolder.getUserName());
        report.setDescription(form.getDescription());
        report.setExperimentSnapshot(loadSnapshot(form.getExperimentId()));
        report.setMutation(parseMutation(form.getMutationJson()));
        report.setAttachmentFilename(saveAttachment(directory, incidentId, form.getFile()));

        Path reportFile = directory.resolve("incident-" + incidentId + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile.toFile(), report);
        log.info("Incident report saved: {}", reportFile);
    }

    @Nullable
    private ExperimentSnapshot loadSnapshot(@Nullable UUID experimentId) {
        if (experimentId == null) {
            return null;
        }
        return experimentService.getExperimentSnapshot(experimentId);
    }

    @SneakyThrows
    @Nullable
    private JsonNode parseMutation(@Nullable String mutationJson) {
        if (mutationJson == null || mutationJson.isBlank()) {
            return null;
        }
        return objectMapper.readTree(mutationJson);
    }

    @SneakyThrows
    @Nullable
    private String saveAttachment(Path directory, UUID incidentId, @Nullable FileUpload file) {
        if (file == null) {
            return null;
        }
        String filename = "incident-" + incidentId + "-" + file.fileName();
        Files.copy(file.filePath(), directory.resolve(filename));
        return filename;
    }
}
