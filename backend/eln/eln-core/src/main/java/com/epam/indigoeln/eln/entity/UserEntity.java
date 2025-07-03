package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.ApplicationPermission;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

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

    @NotNull
    @ManyToMany()
    @JoinTable(name = "User_Account_Application_Role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>(0);

    public Set<ApplicationPermission> collectPermissions() {
        Set<ApplicationPermission> set = EnumSet.noneOf(ApplicationPermission.class);
        for (RoleEntity role : roles) {
            Collections.addAll(set, role.getPermissions());
        }
        return set;
    }
}
