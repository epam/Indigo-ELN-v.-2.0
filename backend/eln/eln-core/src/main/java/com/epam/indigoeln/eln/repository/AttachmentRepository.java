package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.storage.FileStorage;
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
    FileStorage fileStorage;

    public AttachmentRepository() {
        super(ELNEntityType.ATTACHMENT, AttachmentEntity.class);
    }

    @Override
    public AttachmentEntity get(UUID id) {
        return loadFileContent(super.get(id));
    }

    public String persistAndCreatePresignedUrl(AttachmentEntity attachment) {
        super.persist(attachment);
        //fileStorage.put(attachment.getKey(), attachment.getContent());
        return fileStorage.createPresignedUrl(attachment.getKey());
    }

    public void store(String stringPath, byte[] content) {
        fileStorage.put(stringPath, content);
    }

    @Override
    public void persist(AttachmentEntity attachment) {
        super.persist(attachment);
        fileStorage.put(attachment.getKey(), attachment.getContent());
    }

    /*@Override
    public AttachmentEntity getReference(UUID id) {
        return loadFileContent(super.getReference(id));
    }*/

    public AttachmentEntity load(UUID id) {
        AttachmentEntity attachment = doLoadDetails(
                id,
                em.getEntityGraph("Attachment.download"),
                Function.identity()
        );
        return loadFileContent(attachment);
    }

    public List<AttachmentEntity> getReferences(Set<AttachmentDTO> attachments) {
        return StreamEx.of(attachments)
                .map(x -> getReference(x.getId()))
                .toList();
    }

    private AttachmentEntity loadFileContent(AttachmentEntity attachment) {
        byte[] content = fileStorage.get(attachment.getKey());
        attachment.setContent(content);
        return attachment;
    }
}
