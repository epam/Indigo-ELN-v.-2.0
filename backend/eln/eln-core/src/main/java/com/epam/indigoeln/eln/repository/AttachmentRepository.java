package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.EntityType;
import jakarta.enterprise.context.ApplicationScoped;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class AttachmentRepository extends BaseRepository<AttachmentEntity> {

    public AttachmentRepository() {
        super(EntityType.ATTACHMENT, AttachmentEntity.class);
    }

    public AttachmentEntity load(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("Attachment.download"),
                Function.identity()
        );
    }

    public List<AttachmentEntity> getReferences(Set<AttachmentDTO> attachments) {
        return StreamEx.of(attachments)
                .map(x -> getReference(x.getId()))
                .toList();
    }
}
