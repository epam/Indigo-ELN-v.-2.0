package com.epam.indigoeln.signature.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Reason {
    AUTHOR("I am the author"),
    WITNESS("I am the witness");

    private final String title;
}
