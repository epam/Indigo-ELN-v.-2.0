package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.ExperimentAttachment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExperimentAttachmentRepository extends AbstractAttachmentRepository<ExperimentAttachment> {

    public ExperimentAttachmentRepository() {
        super(ExperimentAttachment.class);
    }
}
