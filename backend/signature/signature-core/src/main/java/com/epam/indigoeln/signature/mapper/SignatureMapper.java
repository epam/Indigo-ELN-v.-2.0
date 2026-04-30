package com.epam.indigoeln.signature.mapper;

import com.epam.indigoeln.signature.entity.*;
import com.epam.indigoeln.signature.model.*;
import one.util.streamex.EntryStream;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SignatureMapper {

    // template

    public Template entityToTemplate(TemplateEntity entity) {
        return new Template(
                entity.getId(),
                entity.getName(),
                entity.getAuthor().getFullName(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate(),
                entity.getSignatureBlocks().stream().map(b -> new TemplateSignatureBlock(
                        b.getUser() != null ? b.getUser().getFullName() : null,
                        b.getReason()
                )).toList()
        );
    }

    public TemplateEntity templateToEntity(TemplateRequest template, UserEntity author, ZonedDateTime date) {
        TemplateEntity entity = new TemplateEntity(
                null,
                template.getName(),
                author,
                null,
                date,
                date
        );
        entity.getSignatureBlocks().addAll(EntryStream.of(template.getSignatureBlocks())
                .mapKeyValue((ix, block) -> new TemplateSignatureBlockEntity(
                        null,
                        entity,
                        ix,
                        null,
                        block.getReason()
                )).toList()
        );
        return entity;
    }

    // document

    public DocumentEntity makeDocumentEntity(TemplateEntity template, String name, UserEntity author, ZonedDateTime now, byte[] content) {
        DocumentEntity entity = new DocumentEntity(
                null,
                name,
                template,
                author,
                Status.SUBMITTED,
                now,
                now,
                null,
                content
        );
        entity.getSignatureBlocks().addAll(EntryStream.of(template.getSignatureBlocks())
                .mapKeyValue((ix, block) -> new DocumentSignatureBlockEntity(
                        null,
                        entity,
                        ix,
                        block,
                        block.getUser(),
                        block.getReason(),
                        null,
                        SignatureStatus.WAITING,
                        null
                )).toList()
        );
        return entity;
    }

    public Document entityToDocument(DocumentEntity entity, UserEntity currentUser) {
        return new Document(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate(),
                entity.getAuthor().getFullName(),
                entity.getSignatureBlocks().stream().map(b -> new DocumentSignatureBlock(
                        b.getUser().getFullName(),
                        b.getReason(),
                        b.getActionDate(),
                        b.getStatus(),
                        b.getComment(),
                        canSignOrReject(b, currentUser)
                )).toList()
        );
    }

    public List<Document> entityToDocumentList(List<DocumentEntity> entities, UserEntity currentUser) {
        return entities.stream().map(e -> entityToDocument(e, currentUser)).toList();
    }

    private boolean canSignOrReject(DocumentSignatureBlockEntity block, UserEntity currentUser) {
        Status documentStatus = block.getDocument().getStatus();
        return (documentStatus == Status.SUBMITTED || documentStatus == Status.SIGNING)
                && currentUser.equals(block.getUser())
                && block.getStatus() == SignatureStatus.WAITING;
    }
}
