package com.epam.indigoeln.eln.model;

public enum ExperimentStatus {
    OPEN, // open
    REOPEN, // open
    COMPLETED, // completed
    SUBMITTED, // waiting signature
    SIGNING, // waiting signature
    REJECTED, // rejected
    SIGNED, // don't show
    ARCHIVED, // don't show
    CANCELLED, // cancelled
}
