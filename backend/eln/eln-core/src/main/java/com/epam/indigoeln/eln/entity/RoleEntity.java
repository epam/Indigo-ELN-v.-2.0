package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.ApplicationPermission;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Role")
@Table(name = "Application_Role")
public class RoleEntity extends IdentifiableEntity {

    @NotEmpty
    private String name;

    @Basic
    @NotNull
    @Enumerated(EnumType.STRING)
//    @JdbcTypeCode(Types.ARRAY)
//    @Type(value = EnumArrayType.class, parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE, value = "Application_Role"))
    // TODO make array of enum in Postgres when https://hibernate.atlassian.net/browse/HHH-18329 is fixed
    private ApplicationPermission[] permissions;

    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users = new HashSet<>(0);
}
