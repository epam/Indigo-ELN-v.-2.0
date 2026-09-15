package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.reaction.model.ReactionRole;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentSearchCompound {

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ReactionRole reactionRole;

    @NotNull
    @ManyToOne
    private CompoundEntity compound;
}
