package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.ProjectAttachment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectAttachmentRepository extends AbstractAttachmentRepository<ProjectAttachment> {

    public ProjectAttachmentRepository() {
        super(ProjectAttachment.class);
    }
}
