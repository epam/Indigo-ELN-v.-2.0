package com.epam.indigoeln.signature.service.signatureapplier;

import com.epam.indigoeln.signature.exception.DocumentIntegrityException;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfPKCS7;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

@Slf4j
public class SignatureVerifier {

    public void verifySignatures(byte[] documentContent) throws IOException, GeneralSecurityException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // !!! is it still needed with Quarkus?
        PdfReader reader = new PdfReader(documentContent);
        try {
            verifySignatures(reader);
        } finally {
            reader.close();
        }
    }

    public void verifySignatures(PdfReader reader) throws GeneralSecurityException {
        AcroFields fields = reader.getAcroFields();
        for (String name : fields.getSignedFieldNames()) {
            log.debug("===== {} =====", name);
            if (!verifySignature(fields, name)) {
                throw new DocumentIntegrityException("Reason: document was modified since last signature added.");
            }
        }
    }

    public boolean verifySignature(AcroFields fields, String name) throws GeneralSecurityException {
        log.debug("Signature covers whole document: {}", fields.signatureCoversWholeDocument(name));
        log.debug("Document revision: {} of {}", fields.getRevision(name), fields.getTotalRevisions());
        PdfPKCS7 pkcs7 = fields.verifySignature(name);
        log.debug("Integrity check OK? {}", pkcs7.verify());
        return pkcs7.verify();
    }
}
