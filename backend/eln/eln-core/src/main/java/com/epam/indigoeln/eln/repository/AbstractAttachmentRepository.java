package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.AbstractAttachment;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.ELNEntityType;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractAttachmentRepository<A extends AbstractAttachment<?>> extends BaseRepository<A> {

    protected AbstractAttachmentRepository(Class<A> entityClass) {
        super(ELNEntityType.ATTACHMENT, entityClass);
    }

    public A load(UUID id) {
        return doLoad(id, em.getEntityGraph(entityClass.getSimpleName() + ".download"));
    }

    public List<A> getReferences(Set<AttachmentDTO> attachments) {
        return StreamEx.of(attachments)
                .map(x -> getReference(x.getId()))
                .toList();
    }
}
