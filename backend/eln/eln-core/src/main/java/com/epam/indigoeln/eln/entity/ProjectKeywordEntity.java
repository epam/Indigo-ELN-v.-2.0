package com.epam.indigoeln.eln.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "ProjectKeyword")
@Table(name = "Project_Keyword")
public class ProjectKeywordEntity extends IdentifiableEntity {

    @ManyToMany(mappedBy = "keywords")
    private Set<ProjectEntity> projects = new HashSet<>(0);

    @NotEmpty
    private String name;

    public ProjectKeywordEntity(String name) {
        this.name = name;
    }
}
