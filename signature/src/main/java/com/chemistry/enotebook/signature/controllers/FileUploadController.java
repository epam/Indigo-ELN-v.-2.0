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
package com.chemistry.enotebook.signature.controllers;

import com.chemistry.enotebook.signature.SignatureProperties;
import com.chemistry.enotebook.signature.Util;
import com.chemistry.enotebook.signature.database.DatabaseConnector;
import com.chemistry.enotebook.signature.email.EmailSender;
import com.chemistry.enotebook.signature.entity.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private static final String METHOD_INFO = "You can upload a file by posting to this same URL.";

    @Autowired
    private DatabaseConnector database;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private SignatureProperties signatureProperties;

    // Methods for web

    @RequestMapping(value = "/uploadDocument", method = RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return METHOD_INFO;
    }

    @RequestMapping(value = "/uploadDocument", method = RequestMethod.POST)
    public @ResponseBody String handleFileUpload(@RequestParam("templateId") String templateId,
                                                 @RequestParam("file") MultipartFile file,
                                                 HttpServletRequest request) {
        return uploadDocumentByUsername(Util.getUsername(request), templateId, file);
    }

    // Methods for API

    @RequestMapping(value = "/api/uploadDocument", method = RequestMethod.GET)
    public @ResponseBody String provideUploadInfoAPI() {
        return METHOD_INFO;
    }

    @RequestMapping(value = "/api/uploadDocument", method = RequestMethod.POST)
    public @ResponseBody String handleFileUploadAPI(@RequestParam("username") String username,
                                                    @RequestParam("templateId") String templateId,
                                                    @RequestParam("file") MultipartFile file) {
        return uploadDocumentByUsername(username, templateId, file);
    }

    // Helpers

    private String uploadDocumentByUsername(String username, String templateId, MultipartFile file) {
        String fileName = file.getOriginalFilename();

        if(!"pdf".equalsIgnoreCase(FilenameUtils.getExtension(fileName))) {
            return Util.generateErrorJsonString("You failed to upload " + fileName + ". Indigo Signature Service processes only PDF documents.");
        }

        String documentId = java.util.UUID.randomUUID().toString();
        User author = database.getUserByUsername(username);

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Document document = this.addDocument(documentId, fileName, templateId, bytes, author);
                emailSender.sendEmailToNextSignerInSeparateThread(document, signatureProperties.getIndigoElnAddress());
                document.updateActionRequiredFlagAndAddToDeliveryFlagAndInspectedFlagForCurrentUser(author.getUsername());
                return document.asJson().toString();
            } catch (Exception e) {
                log.warn("Error uploading document: ", e);
                return Util.generateErrorJsonString("You failed to upload " + fileName + " => " + e.getMessage());
            }
        } else {
            return Util.generateErrorJsonString("You failed to upload " + fileName + " because the file was empty.");
        }
    }

    private Document addDocument(String documentId,
                                 String documentName,
                                 String templateId,
                                 byte[] documentContent,
                                 User author) {
        Document document = new Document(UUID.fromString(documentId), author, documentName);
        document.setContent(documentContent);

        Template template = database.getTemplate(UUID.fromString(templateId));

        for(TemplateSignatureBlock templateSignatureBlock : template.getTemplateSignatureBlocks()) {
            DocumentSignatureBlock documentSignatureBlock = new DocumentSignatureBlock(UUID.randomUUID());

            documentSignatureBlock.setIndex(templateSignatureBlock.getIndex());
            documentSignatureBlock.setSigner(templateSignatureBlock.getUser());
            documentSignatureBlock.setReason(templateSignatureBlock.getReason());

            if(templateSignatureBlock.getIndex() == 1) {
                documentSignatureBlock.setStatus(Status.SIGNING);
            } else {
                documentSignatureBlock.setStatus(Status.WAITING);
            }

            document.addDocumentSignatureBlock(documentSignatureBlock);
        }

        database.addDocument(document);

        return document;
    }
}
