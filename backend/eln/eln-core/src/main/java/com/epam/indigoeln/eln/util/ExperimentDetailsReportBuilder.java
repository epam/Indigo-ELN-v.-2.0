package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class ExperimentDetailsReportBuilder {

    private static final String[] REPORT_HTML;

    @Inject
    SnapshotMapper snapshotMapper;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ExperimentModelService experimentModelService;
    
    @Inject
    JSONPatcher jsonPatcher;

    static {
        REPORT_HTML = ModelUtil.loadResourceAsString("/experiment-details-report.html").split("%JSON_DATA%", 2);
    }

    @SneakyThrows
    public byte[] build(ExperimentEntity experiment, List<ExperimentRevisionEntity> revisions) {
        List<Revision> revisionsData = new ArrayList<>();
        ExperimentSnapshot snapshot = snapshotMapper.createSnapshot(experiment, false);
        JsonNode snapshotAfter = objectMapper.valueToTree(snapshot);
        for (ExperimentRevisionEntity r : revisions.reversed()) {
            JsonNode snapshotBefore = jsonPatcher.reverse(snapshotAfter, r.getDiff());
            String formattedDiff = experimentModelService.formatDiff(snapshotBefore, r);
            List<String> messages = new ArrayList<>();
            if (r.getMessages() != null) {
                messages.addAll(Arrays.asList(r.getMessages()));
            }
            if (r.getDebugMessages() != null) {
                messages.addAll(Arrays.asList(r.getDebugMessages()));
            }
            Revision data = new Revision(
                    r.getRevision(),
                    r.getSummary(),
                    r.getUser().getUsername(),
                    r.getDatetime(),
                    r.getMutation() instanceof ExperimentMutation.Undo ? Category.UNDO
                            : r.getMutation() instanceof ExperimentMutation.Redo ? Category.REDO
                              : null,
                    r.getMutation(),
                    r.getDiff(),
                    formattedDiff,
                    messages,
                    snapshotAfter
            );
            revisionsData.add(data);
            snapshotAfter = snapshotBefore;
        }
        Data data = new Data(
                experiment.getName(),
                revisionsData.reversed()
        );
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Writer wr = new BufferedWriter(new OutputStreamWriter(bytes))) {
            wr.append(REPORT_HTML[0]);
            wr.append(objectMapper.writeValueAsString(data));
            wr.append(REPORT_HTML[1]);
        }
        return bytes.toByteArray();
    }

    enum Category {

        UNDO,
        REDO
    }

    @RegisterForReflection
    record Revision (
            int revision,
            String summary,
            String user,
            Instant datetime,
            @Nullable Category category,
            Mutation mutation,
            JsonNode jsonDiff,
            String diff,
            List<String> messages,
            JsonNode entityState
    ) {}

    @RegisterForReflection
    record Data (
            String name,
            List<Revision> revisions
    ) {}
}
