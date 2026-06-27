package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.common.model.UserRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevisionSummaryDTO {

    @NotNull
    private UserRef user;

    @NotNull
    private String summary;

    @NotNull
    private Instant date;

    @Nullable
    private Instant dateTo;

    @NotNull
    private int revision;

    @Nullable
    private Integer revisionTo;

    @Nullable
    private List<RevisionSummaryDTO> details;
}
