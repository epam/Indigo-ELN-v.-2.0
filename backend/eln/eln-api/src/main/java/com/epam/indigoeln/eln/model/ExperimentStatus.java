package com.epam.indigoeln.eln.model;

public enum ExperimentStatus {
    OPEN, // open
    REOPEN, // open
    COMPLETED, // completed
    SUBMITTED, // waiting signature
    SIGNING, // waiting signature
    REJECTED, // rejected
    SIGNED, // waiting signature (?)
    ARCHIVED, // completed (?)
    CANCELLED, // don't show or rejected (?)
}
