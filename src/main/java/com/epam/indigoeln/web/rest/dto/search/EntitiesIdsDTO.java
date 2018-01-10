package com.epam.indigoeln.web.rest.dto.search;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import lombok.Data;

@Data
public class EntitiesIdsDTO {
    private final String id;
    private final String notebookId;
    private final String projectId;
    private final String name;

    public EntitiesIdsDTO(Experiment experiment){
        this.id = SequenceIdUtil.extractShortId(experiment);
        this.notebookId = SequenceIdUtil.extractParentId(experiment);
        this.projectId = SequenceIdUtil.extractFirstId(experiment);
        this.name = experiment.getExperimentFullName();
    }
}
