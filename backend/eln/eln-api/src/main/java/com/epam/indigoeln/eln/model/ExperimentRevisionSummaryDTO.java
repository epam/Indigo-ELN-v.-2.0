package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentRevisionSummaryDTO {

    @Nullable
    private UUID editSessionID;

    @NotNull
    private UserRef user;

    @NotNull
    private String summary;

    @Nullable
    private ZonedDateTime dateFrom;

    @NotNull
    private ZonedDateTime dateTo;
}
