package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.NotebookAttachment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotebookAttachmentRepository extends AbstractAttachmentRepository<NotebookAttachment> {

    public NotebookAttachmentRepository() {
        super(NotebookAttachment.class);
    }
}
