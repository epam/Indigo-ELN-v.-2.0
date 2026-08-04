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
@EqualsAndHashCode(of = {"username"})
@NamedEntityGraph(
        name = "User.info",
        attributeNodes = {
                @NamedAttributeNode("roles")
        }
)
@NamedEntityGraph(
        name = "User.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("roles")
        }
)
public class UserEntity extends BaseEntity {

    @NotEmpty
    @Basic(fetch = FetchType.LAZY)
    private String username;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    private String firstName;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    private String lastName;

    @NotNull
    private String displayName;

    @NotNull
    @ManyToMany()
    @JoinTable(name = "User_Account_Application_Role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = HashSet.newHashSet(0);

    public Set<ApplicationPermission> collectPermissions() {
        Set<ApplicationPermission> set = EnumSet.noneOf(ApplicationPermission.class);
        for (RoleEntity role : roles) {
            Collections.addAll(set, role.getPermissions());
        }
        return set;
    }

    @Override
    public String toString() {
        return username;
    }

    public UserInfo toInfo() {
        return new UserInfo(username, displayName, id, collectPermissions());
    }
}
