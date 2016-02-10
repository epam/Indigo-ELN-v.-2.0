package com.epam.indigoeln.core.service.signature;

import com.epam.indigoeln.core.repository.signature.SignatureRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignatureService {

    @Autowired
    private SignatureRepository signatureRepository;

    public String getSignatureTemplates() {
        return signatureRepository.getSignatureTemplates(SecurityUtils.getCurrentUser().getUsername());
    }

    public String uploadDocument(String templateId, String fileName, byte[] file) {
        return signatureRepository.uploadDocument(SecurityUtils.getCurrentUser().getUsername(), templateId, fileName, file);
    }
}
