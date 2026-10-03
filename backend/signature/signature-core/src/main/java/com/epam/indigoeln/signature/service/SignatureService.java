package com.epam.indigoeln.signature.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.api.ELNInternalClient;
import com.epam.indigoeln.signature.entity.DocumentEntity;
import com.epam.indigoeln.signature.entity.DocumentSignatureEntity;
import com.epam.indigoeln.signature.entity.SignatureTemplateBlockEntity;
import com.epam.indigoeln.signature.entity.SignatureTemplateEntity;
import com.epam.indigoeln.signature.entity.UserEntity;
import com.epam.indigoeln.signature.mapper.SignatureMapper;
import com.epam.indigoeln.signature.model.DocumentDTO;
import com.epam.indigoeln.signature.model.SignatureStatus;
import com.epam.indigoeln.signature.model.SignatureTemplateDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateDetailsDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateRequest;
import com.epam.indigoeln.signature.repository.DocumentRepository;
import com.epam.indigoeln.signature.service.signatureapplier.SignatureApplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.epam.indigoeln.common.model.DocumentStatus.REJECTED;
import static com.epam.indigoeln.common.model.DocumentStatus.SIGNED;
import static com.epam.indigoeln.common.model.DocumentStatus.SIGNING;
import static com.epam.indigoeln.common.model.DocumentStatus.SUBMITTED;
import static com.epam.indigoeln.common.util.ModelUtil.useTempFile;
import static com.epam.indigoeln.signature.model.SignatureStatus.WAITING;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
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
    @Inject
    @RestClient
    ELNInternalClient elnInternalClient;
    @Inject
    DocumentRepository documentRepository;

    public SignatureTemplateDetailsDTO createTemplate(@Valid SignatureTemplateRequest request) {
        SignatureTemplateEntity entity = mapper.requestToEntity(request, Instant.now());
        em.persist(entity);
        return mapper.entityToTemplateDetails(entity);
    }

    public List<SignatureTemplateDTO> getTemplates() {
        return mapper.entityToTemplateList(
                em.createQuery("from SignatureTemplate order by name", SignatureTemplateEntity.class).getResultList()
        );
    }

    @SneakyThrows
    public DocumentDTO createDocument(UUID templateID, String name, FileUpload file) {
        SignatureTemplateEntity template = em.find(SignatureTemplateEntity.class, templateID);
        DocumentEntity document = new DocumentEntity();
        document.setName(name);
        document.setTemplate(template);
        document.setAuthor(userService.getCurrentUser());
        document.setStatus(SUBMITTED);
        document.setCreatedDate(Instant.now());
        document.setLastModifiedDate(document.getCreatedDate());
        document.setFilename(file.fileName());
        document.setContent(Files.readAllBytes(file.filePath()));
        document.getSignatures().addAll(EntryStream.of(template.getBlocks())
                .map(entry -> {
                    Integer index = entry.getKey();
                    SignatureTemplateBlockEntity block = entry.getValue();
                    DocumentSignatureEntity signature = new DocumentSignatureEntity();
                    signature.setOrdinal(index);
                    signature.setDocument(document);
                    signature.setTemplateBlock(block);
                    signature.setUser(switch (block.getReason()) {
                        case AUTHOR -> userService.getCurrentUser();
                        case WITNESS -> {
                            UserEntity user = checkNotNull(block.getUser());
                            yield userService.getOrCreateUser(user.getUsername(), null, null);
                        }
                    });
                    signature.setReason(block.getReason());
                    signature.setStatus(WAITING);
                    return signature;
                })
                .toList());
        em.persist(document);
        updateDocumentStatus(document);
        return mapper.entityToDocument(document);
    }

    public Page<DocumentDTO> getDocuments(@Nullable String search, @Nullable SortOrder sort, @Nullable Boolean waitingMySignature, Paging paging) {
        return documentRepository.findAll(search, sort, waitingMySignature == Boolean.TRUE ? userService.getCurrentUser() : null, paging);
    }

    public DocumentEntity getDocumentEntity(UUID documentId) {
        return em.find(DocumentEntity.class, documentId);
    }

    public DocumentDTO getDocument(UUID documentId) {
        return mapper.entityToDocument(getDocumentEntity(documentId));
    }

    public DocumentDTO signDocument(UUID documentId, byte[] keyStore, String keyStorePassword) {
        return signOrRejectDocument(documentId, keyStore, keyStorePassword);
    }

    public DocumentDTO rejectDocument(UUID documentId) {
        return signOrRejectDocument(documentId, null, null);
    }

    @SneakyThrows
    private DocumentDTO signOrRejectDocument(UUID documentId, byte @Nullable [] keyStore, @Nullable String keyStorePassword) {
        boolean reject = keyStore == null;
        DocumentEntity document = em.find(DocumentEntity.class, documentId);
        boolean found = false;
        String message = null;
        for (DocumentSignatureEntity block : document.getSignatures()) {
            if (block.getUser().equals(userService.getCurrentUser())) {
                if (block.getStatus() != WAITING) {
                    throw new InvalidRequestException("User already signed or rejected this document");
                }
                block.setStatus(reject ? SignatureStatus.REJECTED : SignatureStatus.APPROVED);
                block.setActionDate(Instant.now());
                document.setStatus(SIGNING);
                int signatureIndex = document.getSignatures().indexOf(block);
                byte[] content = reject
                        ? signatureApplier.rejectDocument(document.getContent(), block, signatureIndex)
                        : signatureApplier.signDocument(document.getContent(), block, signatureIndex, keyStore, checkNotNull(keyStorePassword));
                document.setContent(content);
                message = "%s by %s".formatted(reject ? "Rejected" : "Approved", userService.getCurrentUser().getDisplayName());
                found = true;
            }
        }
        if (!found) {
            throw new InvalidRequestException("Document doesn't require signature by current user");
        }
        updateDocumentStatus(document);
        String message1 = message;
        return useTempFile(document.getFilename(), document.getContent(), file -> {
            try {
                elnInternalClient.internalSignatureUpdatedClient(document.getId(), message1, document.getStatus(), file);
            } catch (Exception e) {
                log.warn("Error notifying ELN on document status change", e);
            }
            document.setLastModifiedDate(Instant.now());
            return mapper.entityToDocument(document);
        });
    }

    private void updateDocumentStatus(DocumentEntity document) {
        Set<SignatureStatus> statuses = document.getSignatures().stream()
                .map(DocumentSignatureEntity::getStatus)
                .collect(Collectors.toSet());
        boolean hasApproved = statuses.contains(SignatureStatus.APPROVED);
        boolean hasRejected = statuses.contains(SignatureStatus.REJECTED);
        boolean hasWaiting = statuses.contains(WAITING);
        DocumentStatus newStatus = switch (document.getStatus()) {
            case SUBMITTED -> {
                if (hasRejected) {
                    yield REJECTED;
                }
                if (!hasWaiting) {
                    yield SIGNED;
                }
                if (hasApproved) {
                    yield SIGNING;
                }
                yield null;
            }
            case SIGNING -> {
                if (hasRejected) {
                    yield REJECTED;
                }
                if (!hasWaiting) {
                    yield SIGNED;
                }
                yield null;
            }
            default -> null;
        };
        if (newStatus != null) {
            document.setStatus(newStatus);
        }
    }
}
