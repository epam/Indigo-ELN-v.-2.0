package com.epam.indigoeln.reaction.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SaltCodeRef {

    private final String name;
    private final int charge;
    private final double molWeight;

    @Override
    public String toString() {
        return "SaltCodeRef{" +
                "name='" + name + '\'' +
                ", charge=" + charge +
                ", molWeight=" + molWeight +
                '}';
    }
}
