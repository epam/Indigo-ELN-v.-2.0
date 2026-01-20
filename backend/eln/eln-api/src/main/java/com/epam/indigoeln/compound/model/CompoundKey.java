package com.epam.indigoeln.compound.model;

import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Value
public class CompoundKey {

    String canSmiles;
    @Nullable
    UUID stereoisomerCode;
    @Nullable
    UUID saltCode;
    @Nullable
    Integer saltEQ100;
}
