package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")
@Table(name = "User_Account")
@EqualsAndHashCode(of = {"username"})
public class UserEntity extends IdentifiableEntity {

    @NotEmpty
    private String username;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @NotNull
    private String displayName;

    @Override
    public String toString() {
        return username;
    }

    public UserRef toRef() {
        return new UserRef(id, username, displayName);
    }
}
