package com.epam.indigoeln.reaction.model;

import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Value
public class CompoundKey {

    String canSmiles;
    UUID stereoisomerCode;
    UUID saltCode;
    @Nullable
    Integer saltEQ100;
}
