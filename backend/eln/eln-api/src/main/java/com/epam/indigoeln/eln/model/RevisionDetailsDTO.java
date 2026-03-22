package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionDetailsDTO {

    @NotNull
    private Integer revision;

    @NotNull
    private ZonedDateTime datetime;

    @NotNull
    private UserRef user;

    @NotNull
    private Mutation mutation;

    @NotNull
    private String summary;

    @NotNull
    private JsonNode diff;

    @NotNull
    private String stringDiff;
}
