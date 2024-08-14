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
package com.chemistry.enotebook.signature;

import com.chemistry.enotebook.signature.archiver.SignatureServiceArchiver;
import com.chemistry.enotebook.signature.database.DatabaseConnector;
import com.chemistry.enotebook.signature.email.EmailSender;
import com.chemistry.enotebook.signature.entity.Document;
import com.chemistry.enotebook.signature.entity.DocumentSignatureBlock;
import com.chemistry.enotebook.signature.entity.Status;
import com.chemistry.enotebook.signature.entity.User;
import com.chemistry.enotebook.signature.exception.DocumentIntegrityException;
import com.chemistry.enotebook.signature.exception.MissingDocumentException;
import com.chemistry.enotebook.signature.exception.SignatureException;
import com.chemistry.enotebook.signature.signatureapplier.SignatureApplier;
import com.chemistry.enotebook.signature.signatureapplier.SignatureFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SignatureUtil {
    @Autowired
    private EmailSender emailSender;

    @Autowired
    private DatabaseConnector database;

    @Autowired
    private SignatureProperties signatureProperties;

    @Autowired
    private SignatureServiceArchiver archiver;

    public String doSign(String id, String username, String comment, byte[] keyStorage, String password) throws Exception {

        Document document = database.getDocument(id);
        if(document == null) {
            throw new MissingDocumentException("Error. Can't find document " + id + " in database.");
        }

        document.setContent(database.getDocumentContent(id));
        if(document.getContent() == null || document.getContent().length == 0) {
            throw new MissingDocumentException("Error. Content of document " + id + " is empty.");
        }

        User user = database.getUserByUsername(username);

        try {
            SignatureApplier signatureApplier = new SignatureFactory().getSignatureApplier(signatureProperties.getSigningMethod());
            DocumentSignatureBlock signatureBlock = document.getDocumentSignatureBlockForUser(user);
            if(signatureBlock != null) {
                signatureBlock.setComment(comment);
                byte[] signedDocument = signatureApplier.signDocument(document.getContent(), signatureBlock, keyStorage, password);
                if(signedDocument != null && signedDocument.length > 0) {
                    signatureBlock.setStatus(Status.SIGNED);
                    signatureBlock.setActionDate(System.currentTimeMillis());

                    for(DocumentSignatureBlock anySignatureBlock : document.getDocumentSignatureBlocks()) {
                        if(anySignatureBlock.getIndex() == signatureBlock.getIndex() + 1) {
                            anySignatureBlock.setStatus(Status.SIGNING);
                        }
                    }

                    document.setContent(signedDocument);
                    document.updateStatusAccordingToSignatureBlocks();
                    document.setLastUpdateDate(System.currentTimeMillis());

                    if(document.getStatus().equals(Status.SIGNED) && Status.ARCHIVED.toString().equalsIgnoreCase(signatureProperties.getFinalStatus())) {
                        archiver.updateDocumentStatusAndArchive(document);
                    }
                    database.updateDocument(document, true);

                    emailSender.sendEmailToNextSignerInSeparateThread(document, signatureProperties.getIndigoElnAddress());
                }
            } else {
                throw new SignatureException("User " + user.getUsername() + " is not able to sign document " + id + ".");
            }
        } catch (DocumentIntegrityException e) {
            removeDocument(document);
            return Util.generateErrorJsonString(Util.getMessageAboutDocumentRemove(document.getDocumentName()));
        } catch (Exception e) {
            throw new SignatureException("Failed to sign document " + document.getDocumentName() + ". " + e.getMessage());
        }

        document.updateActionRequiredFlagAndAddToDeliveryFlagAndInspectedFlagForCurrentUser(user.getUsername());

        return document.asJson().toString();
    }

    public void removeDocument(Document document) {
        emailSender.sendEmailToDocumentAuthorAboutRemovingBrokenDocumentInSeparateThread(document);
        database.removeDocument(document.getId());
    }
}
