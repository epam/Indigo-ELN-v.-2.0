package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.model.EntityType;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class AttachmentRepository extends BaseRepository<AttachmentEntity> {

    public AttachmentRepository() {
        super(EntityType.ATTACHMENT);
    }

    public AttachmentEntity load(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("Attachment.download"),
                Function.identity()
        );
    }

    public AttachmentEntity getReference(UUID id) {
        return em.getReference(AttachmentEntity.class, id);
    }
}
