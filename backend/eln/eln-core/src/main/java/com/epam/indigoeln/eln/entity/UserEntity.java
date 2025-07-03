package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.ApplicationRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")
@Table(name = "User_Account")
@ToString(of = {"id", "username"}, includeFieldNames = false)
public class UserEntity extends BaseEntity {

    @NotEmpty
    @Basic(fetch = FetchType.LAZY)
    private String username;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "first_name")
    private String firstName;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @Column(name = "display_name")
    private String displayName;

    @Basic
    @NotNull
    @Enumerated(EnumType.STRING)
//    @JdbcTypeCode(Types.ARRAY)
//    @Type(value = EnumArrayType.class, parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE, value = "Application_Role"))
    // TODO make array of enum in Postgres when https://hibernate.atlassian.net/browse/HHH-18329 is fixed
    private ApplicationRole[] roles;

    public boolean hasRole(ApplicationRole role) {
        return Arrays.asList(roles).contains(role);
    }
}
