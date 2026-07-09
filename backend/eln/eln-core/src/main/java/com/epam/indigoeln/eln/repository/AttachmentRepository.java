package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.storage.S3FileStorage;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.ELNEntityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class AttachmentRepository extends BaseRepository<AttachmentEntity> {

    @Inject
    S3FileStorage s3FileStorage;

    public AttachmentRepository() {
        super(ELNEntityType.ATTACHMENT, AttachmentEntity.class);
    }

    @Override
    public AttachmentEntity get(UUID id) {
        return loadS3Content(super.get(id));
    }

    @Override
    public void persist(AttachmentEntity attachment) {
        s3FileStorage.put(attachment.getName(), attachment.getContent());
        attachment.setSize((long) 0);
        attachment.setContent(new byte[0]);
        super.persist(attachment);
    }

    @Override
    public AttachmentEntity getReference(UUID id) {
        return loadS3Content(super.getReference(id));
    }

    public AttachmentEntity load(UUID id) {
        AttachmentEntity attachment = doLoadDetails(
                id,
                em.getEntityGraph("Attachment.download"),
                Function.identity()
        );
        return loadS3Content(attachment);
    }

    public List<AttachmentEntity> getReferences(Set<AttachmentDTO> attachments) {
        return StreamEx.of(attachments)
                .map(x -> getReference(x.getId()))
                .toList();
    }

    private AttachmentEntity loadS3Content(AttachmentEntity attachment) {
        byte[] content = s3FileStorage.get(attachment.getName());
        attachment.setContent(content);
        attachment.setSize((long) content.length);
        return attachment;
    }
}
