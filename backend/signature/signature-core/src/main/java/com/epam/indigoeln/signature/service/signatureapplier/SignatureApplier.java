package com.epam.indigoeln.signature.service.signatureapplier;

import com.epam.indigoeln.signature.entity.DocumentSignatureBlockEntity;

public interface SignatureApplier {
    byte[] signDocument(byte[] documentContent, DocumentSignatureBlockEntity DocumentSignatureBlockEntity, byte[] keyStorage, String keyStoragePassword) throws Exception;
    byte[] rejectDocument(byte[] documentContent, DocumentSignatureBlockEntity signatureTemplateBlock) throws Exception;
}
