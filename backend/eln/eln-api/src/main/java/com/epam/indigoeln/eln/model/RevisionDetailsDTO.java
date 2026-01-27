package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.mutation.Mutation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionDetailsDTO<P> {

    @NotNull
    private Integer revision;

    @NotNull
    private ZonedDateTime datetime;

    @NotNull
    private UserRef user;

    @NotNull
    private Mutation mutation;

    @Nullable
    private Mutation reverseMutation;

    @NotNull
    private String summary;

    @NotNull
    private P diff;
}
