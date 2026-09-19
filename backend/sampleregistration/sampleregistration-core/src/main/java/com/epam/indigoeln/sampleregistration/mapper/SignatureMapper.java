package com.epam.indigoeln.sampleregistration.mapper;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.sampleregistration.entity.DocumentEntity;
import com.epam.indigoeln.sampleregistration.entity.DocumentSignatureEntity;
import com.epam.indigoeln.sampleregistration.entity.SignatureTemplateBlockEntity;
import com.epam.indigoeln.sampleregistration.entity.SignatureTemplateEntity;
import com.epam.indigoeln.sampleregistration.entity.UserEntity;
import com.epam.indigoeln.signature.entity.*;
import com.epam.indigoeln.signature.model.*;
import com.epam.indigoeln.sampleregistration.service.UserService;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

import static com.epam.indigoeln.common.model.DocumentStatus.SIGNING;
import static com.epam.indigoeln.common.model.DocumentStatus.SUBMITTED;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SignatureMapper {

    @Inject
    UserService userService;

    // template

    public abstract SignatureTemplateDetailsDTO entityToTemplateDetails(SignatureTemplateEntity entity);

    public abstract SignatureTemplateDTO entityToTemplate(SignatureTemplateEntity entity);

    public abstract List<SignatureTemplateDTO> entityToTemplateList(List<SignatureTemplateEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", source = "date")
    @Mapping(target = "createdBy", expression = "java(userService.getCurrentUser())")
    @Mapping(target = "modifiedAt", source = "date")
    @Mapping(target = "modifiedBy", expression = "java(userService.getCurrentUser())")
    @Mapping(target = "blocks", ignore = true)
    public abstract SignatureTemplateEntity requestToEntity(SignatureTemplateRequest request, Instant date);

    @AfterMapping
    protected void afterRequestToEntity(@MappingTarget SignatureTemplateEntity entity, SignatureTemplateRequest request) {
        entity.getBlocks().addAll(request.getBlocks().stream()
                .map(b -> requestBlockToEntity(b, entity))
                .toList()
        );
    }

    @Mapping(target = "template", source = "entity")
    protected abstract SignatureTemplateBlockEntity requestBlockToEntity(SignatureTemplateBlock block, SignatureTemplateEntity entity);

    // document

    public abstract DocumentDTO entityToDocument(DocumentEntity entity);

    @Mapping(target = "canSignOrReject", expression = "java(canSignOrReject(entity))")
    protected abstract DocumentSignatureDTO entityToDocumentSignature(DocumentSignatureEntity entity);

    public abstract List<DocumentDTO> entityToDocumentList(List<DocumentEntity> entities);

    protected boolean canSignOrReject(DocumentSignatureEntity block) {
        DocumentStatus documentStatus = block.getDocument().getStatus();
        return (documentStatus == SUBMITTED || documentStatus == SIGNING)
                && userService.getCurrentUser().equals(block.getUser())
                && block.getStatus() == SignatureStatus.WAITING;
    }

    @Nullable
    protected UserRef entityToUserRef(@Nullable UserEntity entity) {
        return entity != null ? entity.toRef() : null;
    }

    @Nullable
    protected UserEntity userRefToEntity(@Nullable UserRef ref) {
        return ref != null ? userService.getOrCreateUser(ref.getUsername(), null, null) : null;
    }
}
