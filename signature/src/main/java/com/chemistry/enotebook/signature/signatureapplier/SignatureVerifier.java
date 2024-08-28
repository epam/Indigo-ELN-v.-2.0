/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.signatureapplier;

import com.chemistry.enotebook.signature.exception.DocumentIntegrityException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.ArrayList;

public class SignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(SignatureVerifier.class);

    public void verifySignatures(byte[] documentContent) throws IOException, GeneralSecurityException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PdfReader reader = null;
        try {
            reader = new PdfReader(documentContent);
            verifySignatures(reader);
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
    }

    public void verifySignatures(PdfReader reader) throws GeneralSecurityException {
        AcroFields fields = reader.getAcroFields();
        ArrayList<String> names = fields.getSignatureNames();
        for (String name : names) {
            log.debug("===== " + name + " =====");
            if(!verifySignature(fields, name)) {
                throw new DocumentIntegrityException("Reason: document was modified since last signature added.");
            }
        }
    }

    public boolean verifySignature(AcroFields fields, String name)
            throws GeneralSecurityException {
        log.debug("Signature covers whole document: " + fields.signatureCoversWholeDocument(name));
        log.debug("Document revision: " + fields.getRevision(name) + " of " + fields.getTotalRevisions());
        PdfPKCS7 pkcs7 = fields.verifySignature(name);
        log.debug("Integrity check OK? " + pkcs7.verify());
        return pkcs7.verify();
    }
}
