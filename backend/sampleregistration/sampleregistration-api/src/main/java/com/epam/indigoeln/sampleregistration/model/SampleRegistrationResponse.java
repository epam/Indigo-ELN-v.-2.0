package com.epam.indigoeln.sampleregistration.model;

import java.util.UUID;

public record SampleRegistrationResponse (
    STRCodeSample strCode,
    UUID sampleID
) {
}
