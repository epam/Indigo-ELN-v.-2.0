package com.epam.indigoeln.sampleregistration.entity;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import com.epam.indigoeln.eln.common.config.MolFormulaConverter;
import com.epam.indigoeln.sampleregistration.config.STRCodeCompoundConverter;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "SRSCompound")
@Table(name = "SRS_Compound")
@ToString(of = {"id", "chemicalName", "formula", "canSmiles", "strCode"})
public class SRSCompoundEntity extends IdentifiableEntity {

    @NotNull
    @Convert(converter = STRCodeCompoundConverter.class)
    private STRCodeCompound strCode;

    @NotEmpty
    private String canSmiles;

    @Nullable
    private UUID stereoisomerCode;

    @Nullable
    private UUID saltCode;

    @Nullable
    @Column(name = "salt_eq_100")
    private Integer saltEQ100;

    @Nullable
    private String chemicalName;

    @NotNull
    @Convert(converter = MolFormulaConverter.class)
    private MolFormula formula;

    @NotNull
    private Double molWeight;

    @NotNull
    private Double exactMass;

    @NotEmpty
    @Basic(fetch = FetchType.LAZY)
    private String molFile;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    private byte[] picture;
}
