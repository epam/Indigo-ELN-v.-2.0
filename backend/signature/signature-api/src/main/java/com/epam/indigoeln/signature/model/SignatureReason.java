package com.epam.indigoeln.signature.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SignatureReason {

    AUTHOR("I am the author"),
    WITNESS("I am the witness");

    private final String signatureText;
}
