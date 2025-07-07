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
    private final int charge;
    private final double molWeight;

    @Override
    public String toString() {
        return "SaltCodeRef{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", charge=" + charge +
                ", molWeight=" + molWeight +
                '}';
    }
}
