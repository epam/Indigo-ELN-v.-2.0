package com.epam.indigoeln.signature.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "UserAccount")
@EqualsAndHashCode(of = "id")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotEmpty
    private String username;
    private String firstName;
    private String lastName;

    public String getFullName() {
        StringBuilder str = new StringBuilder();
        if (firstName != null) {
            str.append(firstName);
        }
        if (lastName != null) {
            if (!str.isEmpty()) {
                str.append(' ');
            }
            str.append(lastName);
        }
        if (str.isEmpty()) {
            str.append(username);
        }
        return str.toString();
    }
}
