package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.ELNEntityType;
import jakarta.enterprise.context.ApplicationScoped;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class AttachmentRepository extends BaseRepository<AttachmentEntity> {

    public AttachmentRepository() {
        super(ELNEntityType.ATTACHMENT, AttachmentEntity.class);
    }

    public AttachmentEntity load(UUID id) {
        return doLoad(id, em.getEntityGraph("Attachment.download"));
    }

    public List<AttachmentEntity> getReferences(Set<AttachmentDTO> attachments) {
        return StreamEx.of(attachments)
                .map(x -> getReference(x.getId()))
                .toList();
    }
}
