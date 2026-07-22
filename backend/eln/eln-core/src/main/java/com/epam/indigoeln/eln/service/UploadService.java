package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class UploadService {

    @Inject
    AttachmentRepository attachmentRepository;

    public void uploadAttachment(String stringPath, FileUpload file) {
        attachmentRepository.store(stringPath, readFile(file));
    }

    private byte[] readFile(FileUpload file) {
        try {
            return Files.readAllBytes(file.filePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read attachment content", e);
        }
    }
}
