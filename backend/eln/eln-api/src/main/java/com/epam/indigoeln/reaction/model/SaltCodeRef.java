package com.epam.indigoeln.reaction.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class SaltCodeRef {

    private final UUID id;
    private final String code;
    private final String name;

    @Override
    public String toString() {
        return "SaltCodeRef{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
