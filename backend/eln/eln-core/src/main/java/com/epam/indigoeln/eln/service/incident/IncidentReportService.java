package com.epam.indigoeln.eln.service.incident;

import com.epam.indigoeln.common.config.UserHolder;
import com.epam.indigoeln.common.storage.FileStorage;
import com.epam.indigoeln.eln.api.IncidentReportForm;
import com.epam.indigoeln.eln.service.ExperimentService;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class IncidentReportService {

    @Inject
    FileStorage fileStorage;

    @Inject
    UserHolder userHolder;

    @Inject
    ExperimentService experimentService;

    @Inject
    ObjectMapper objectMapper;

    @SneakyThrows
    public void createIncidentReport(IncidentReportForm form) {
        UUID incidentId = UUID.randomUUID();
        Instant incidentTime = Instant.now();

        IncidentReport report = new IncidentReport();
        report.setIncidentTime(incidentTime);
        report.setUsername(userHolder.getUserName());
        report.setMessage(form.getMessage());
        report.setExperimentFrontend(form.getExperiment());
        report.setExperimentSnapshot(loadSnapshot(form.getExperimentId()));
        report.setMutation(parseMutation(form.getMutationJson()));
        report.setAttachmentFilename(saveAttachment(incidentId, form.getFile()));

        String reportKey = "incidents/incident-" + incidentId + ".json";
        fileStorage.put(reportKey, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(report));
        log.info("Incident report saved: {}", reportKey);
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
    private String saveAttachment(UUID incidentId, @Nullable FileUpload file) {
        if (file == null) {
            return null;
        }
        String basename = Path.of(file.fileName()).getFileName().toString();
        String filename = "incidents/incident-" + incidentId + "-" + basename;
        fileStorage.put(filename, Files.readAllBytes(file.filePath()));
        return filename;
    }
}
