package com.epam.indigoeln.reaction.metamodel.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Metamodel<C> {

    private final String name;
    private final List<ModelProperty<C, ?>> properties;
}
