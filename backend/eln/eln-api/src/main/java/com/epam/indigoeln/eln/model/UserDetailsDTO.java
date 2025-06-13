package com.epam.indigoeln.eln.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsDTO extends UserDTO {

    @Override
    public String toString() {
        return "UserDetailsDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
