package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ExperimentCountArrayType;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@Entity(name = "TotalCounts")
@Table(name = "Total_Counts_View")
public class TotalCountsEntity implements Serializable {

    @Id
    private Integer projects;

    @NotNull
    private Integer notebooks;

    @NotNull
    @Column(name = "experiments_by_status", insertable = false, updatable = false)
    @Type(ExperimentCountArrayType.class)
    private Map<ExperimentStatus, Integer> experimentsByStatus;
}
