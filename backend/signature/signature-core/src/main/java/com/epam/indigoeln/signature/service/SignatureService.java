package com.epam.indigoeln.signature.service;

import com.epam.indigoeln.signature.entity.*;
import com.epam.indigoeln.signature.exception.InvalidInputException;
import com.epam.indigoeln.signature.mapper.SignatureMapper;
import com.epam.indigoeln.signature.model.*;
import com.epam.indigoeln.signature.service.signatureapplier.SignatureApplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import java.time.ZonedDateTime;
import java.util.List;

@Transactional
@ApplicationScoped
public class SignatureService {

    @Inject
    EntityManager em;
    @Inject
    SignatureMapper mapper;
    @Inject
    UserService userService;
    @Inject
    SignatureApplier signatureApplier;

    public List<Reason> getReasons() {
        return List.of(Reason.values());
    }

    public List<Status> getStatuses() {
        return List.of(Status.values());
    }

    public Template createTemplate(TemplateRequest request) {
        TemplateEntity entity = mapper.templateToEntity(request, userService.getCurrentUser(), ZonedDateTime.now());
        for (int i = 0; i < request.getSignatureBlocks().size(); i++) {
            TemplateSignatureBlockRequest block = request.getSignatureBlocks().get(i);
            TemplateSignatureBlockEntity entityBlock = entity.getSignatureBlocks().get(i);
            switch (block.getReason()) {
                case AUTHOR -> {}
                case WITNESS -> {
                    UserEntity user = userService.getOrCreateUser(block.getUsername(), null, null);
                    entityBlock.setUser(user);
                }
            }
        }
        em.persist(entity);
        return mapper.entityToTemplate(entity);
    }

    public List<TemplateEntity> getTemplates() {
        return em.createQuery("from Template order by name", TemplateEntity.class).getResultList();
    }

    public Document createDocument(int templateID, String name, byte[] content) {
        TemplateEntity template = em.find(TemplateEntity.class, templateID);
        // TODO if template is not found or not visible to current user
        DocumentEntity entity = mapper.makeDocumentEntity(template, name, userService.getCurrentUser(), ZonedDateTime.now(), content);
        for (DocumentSignatureBlockEntity block : entity.getSignatureBlocks()) {
            if (block.getReason() == Reason.AUTHOR) {
                block.setUser(userService.getCurrentUser());
            }
        }
        em.persist(entity);
        return mapper.entityToDocument(entity, userService.getCurrentUser());
    }

    public List<Document> getDocuments() {
        List<DocumentEntity> documents = em.createQuery("from Document order by createdDate desc", DocumentEntity.class).getResultList();
        return mapper.entityToDocumentList(documents, userService.getCurrentUser());
    }

    public DocumentEntity getDocument(int documentId) {
        return em.find(DocumentEntity.class, documentId);
    }

    @SneakyThrows
    public Document signOrRejectDocument(int documentId, boolean reject, byte[] keyStore, String keyStorePassword) {
        DocumentEntity document = em.find(DocumentEntity.class, documentId);
        boolean found = false;
        for (DocumentSignatureBlockEntity block : document.getSignatureBlocks()) {
            if (block.getUser().equals(userService.getCurrentUser())) {
                if (block.getStatus() != SignatureStatus.WAITING) {
                    throw new InvalidInputException("Document already signed or rejected by current user");
                }
                block.setStatus(reject ? SignatureStatus.REJECTED : SignatureStatus.SIGNED);
                block.setActionDate(ZonedDateTime.now());
                document.setStatus(Status.SIGNING);
                byte[] content = reject
                        ? signatureApplier.rejectDocument(document.getContent(), block)
                        : signatureApplier.signDocument(document.getContent(), block, keyStore, keyStorePassword);
                document.setContent(content);
                found = true;
            }
        }
        if (!found) {
            throw new InvalidInputException("Document doesn't require signature by current user");
        }
        boolean hasOtherWaitingBlocks = document.getSignatureBlocks().stream()
                .anyMatch(b -> b.getStatus() == SignatureStatus.WAITING);
        if (!hasOtherWaitingBlocks) {
            boolean hasRejections = document.getSignatureBlocks().stream()
                            .anyMatch(b -> b.getStatus() == SignatureStatus.REJECTED);
            document.setStatus(hasRejections ? Status.REJECTED : Status.SIGNED);
        }
        document.setLastModifiedDate(ZonedDateTime.now());
        return mapper.entityToDocument(document, userService.getCurrentUser());
    }
}
