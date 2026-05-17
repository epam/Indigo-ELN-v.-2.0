package com.epam.indigoeln.signature.service;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.api.ELNInternalClient;
import com.epam.indigoeln.signature.entity.DocumentEntity;
import com.epam.indigoeln.signature.entity.DocumentSignatureEntity;
import com.epam.indigoeln.signature.entity.SignatureTemplateEntity;
import com.epam.indigoeln.signature.entity.UserEntity;
import com.epam.indigoeln.signature.exception.InvalidInputException;
import com.epam.indigoeln.signature.mapper.SignatureMapper;
import com.epam.indigoeln.signature.model.*;
import com.epam.indigoeln.signature.repository.DocumentRepository;
import com.epam.indigoeln.signature.service.signatureapplier.SignatureApplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.epam.indigoeln.common.model.DocumentStatus.*;
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

    private final byte[] keyStore;
    private final String keyStorePassword;

    @SneakyThrows
    SignatureService(/*@ConfigProperty(name = "eln.signature.keystore.path") String keyStorePath, */@ConfigProperty(name = "eln.signature.keystore.password") String keyStorePassword) {
        this.keyStore = ModelUtil.loadResource(getClass(), "/keystore.p12");
        this.keyStorePassword = keyStorePassword;
    }

    public SignatureTemplateDetailsDTO createTemplate(@Valid SignatureTemplateRequest request) {
        SignatureTemplateEntity entity = mapper.requestToEntity(request, ZonedDateTime.now());
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
        document.setCreatedDate(ZonedDateTime.now());
        document.setLastModifiedDate(document.getCreatedDate());
        document.setFilename(file.fileName());
        document.setContent(Files.readAllBytes(file.filePath()));
        document.getSignatures().addAll(StreamEx.of(template.getBlocks())
                .map(block -> {
                    DocumentSignatureEntity signature = new DocumentSignatureEntity();
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

    public Page<DocumentDTO> getDocuments(String search, SortOrder sort, Boolean waitingMySignature, Paging paging) {
        return documentRepository.findAll(search, sort, waitingMySignature == Boolean.TRUE ? userService.getCurrentUser() : null, paging);
    }

    public DocumentEntity getDocumentEntity(UUID documentId) {
        return em.find(DocumentEntity.class, documentId);
    }

    public DocumentDTO getDocument(UUID documentId) {
        return mapper.entityToDocument(getDocumentEntity(documentId));
    }

    @SneakyThrows
    public DocumentDTO signOrRejectDocument(UUID documentId, boolean reject) {
        DocumentEntity document = em.find(DocumentEntity.class, documentId);
        boolean found = false;
        String message = null;
        for (DocumentSignatureEntity block : document.getSignatures()) {
            if (block.getUser().equals(userService.getCurrentUser())) {
                if (block.getStatus() != WAITING) {
                    throw new InvalidInputException("User already signed or rejected this document");
                }
                block.setStatus(reject ? SignatureStatus.REJECTED : SignatureStatus.APPROVED);
                block.setActionDate(ZonedDateTime.now());
                document.setStatus(SIGNING);
                int signatureIndex = document.getSignatures().indexOf(block);
                byte[] content = reject
                        ? signatureApplier.rejectDocument(document.getContent(), block, signatureIndex)
                        : signatureApplier.signDocument(document.getContent(), block, signatureIndex, keyStore, keyStorePassword);
                document.setContent(content);
                message = "%s by %s".formatted(reject ? "Rejected" : "Approved", userService.getCurrentUser().getDisplayName());
                found = true;
            }
        }
        if (!found) {
            throw new InvalidInputException("Document doesn't require signature by current user");
        }
        updateDocumentStatus(document);
        String message1 = message;
        return useTempFile(document.getFilename(), document.getContent(), file -> {
            elnInternalClient.internalSignatureUpdatedClient(document.getId(), message1, document.getStatus(), file);
            document.setLastModifiedDate(ZonedDateTime.now());
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
