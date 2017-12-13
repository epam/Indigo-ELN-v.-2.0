package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Implementation of SectionModel interface for reaction details.
 */
@Data
@Accessors(chain = true)
public class ReactionDetailsModel implements SectionModel {
    private ZonedDateTime creationDate;
    private String therapeuticArea;
    private String continuedFrom;
    private String continuedTo;
    private String projectCode;
    private String projectAlias;
    private List<String> linkedExperiment;
    private String literatureReference;
    private List<String> coAuthors;
}
