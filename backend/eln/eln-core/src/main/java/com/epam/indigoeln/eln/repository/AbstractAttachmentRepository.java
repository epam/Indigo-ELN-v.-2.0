package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.storage.FileStorage;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.AbstractAttachment;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.ELNEntityType;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractAttachmentRepository<A extends AbstractAttachment<?>> extends BaseRepository<A> {

    @Inject
    FileStorage fileStorage;

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

    public String persistAndCreatePresignedUrl(A attachment) {
        super.persist(attachment);
        return fileStorage.createPresignedUrl(attachment.getKey());
    }

    public String createPresignedUrl(String keyName) {
        return fileStorage.createPresignedUrl(keyName);
    }
}
