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

import com.chemistry.enotebook.signature.IndigoSignatureServiceException;
import com.chemistry.enotebook.signature.SignatureProperties;
import com.chemistry.enotebook.signature.SignatureUtil;
import com.chemistry.enotebook.signature.Util;
import com.chemistry.enotebook.signature.database.DatabaseConnector;
import com.chemistry.enotebook.signature.email.EmailSender;
import com.chemistry.enotebook.signature.entity.Document;
import com.chemistry.enotebook.signature.entity.DocumentSignatureBlock;
import com.chemistry.enotebook.signature.entity.Status;
import com.chemistry.enotebook.signature.entity.User;
import com.chemistry.enotebook.signature.exception.DocumentIntegrityException;
import com.chemistry.enotebook.signature.signatureapplier.SignatureRejector;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

@Controller
@RequestMapping("/")
public class ApplicationController {
    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private DatabaseConnector database;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private SignatureProperties signatureProperties;

    @Autowired
    private SignatureUtil signatureUtil;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getDocuments(ModelMap model, HttpServletRequest request) {
        model.addAttribute("message", getDocuments(request));
        model.addAttribute("isUploadDocumentsAllowed", signatureProperties.isUploadDocumentsAllowed());
        model.addAttribute("signingMethod", signatureProperties.getSigningMethod());
        return "index";
    }

    @RequestMapping(value = "/getStatuses", method = RequestMethod.GET)
    public @ResponseBody String getStatuses() {
        Set<Map> statuses = database.getAllStatuses();

        JsonArrayBuilder statusesJsonArray = createArrayBuilder();

        for(Map status : statuses) {
            statusesJsonArray.add(createObjectBuilder().add("id", status.get("statusid").toString()).add("name", (String)status.get("name")));
        }

        JsonObject statusesJson = createObjectBuilder()
                .add("Statuses", statusesJsonArray.build())
                .build();

        return statusesJson.toString();
    }

    @RequestMapping(value = "/api/getStatuses", method = RequestMethod.GET)
    public @ResponseBody String getStatusesAPI() {
        return getStatuses();
    }

    @RequestMapping(value = "/api/getDocuments", method = RequestMethod.GET)
    public @ResponseBody String getDocuments(@RequestParam("username")String username) {
        return getDocumentsByUsername(username);
    }

    @RequestMapping(value = "/getDocuments", method = RequestMethod.GET)
    public @ResponseBody String getDocuments(HttpServletRequest request) {
        return getDocumentsByUsername(Util.getUsername(request));
    }

    @RequestMapping(value = "/api/getDocumentsByIds", method = RequestMethod.POST)
    public @ResponseBody String getDocumentsByIds(HttpServletRequest request) throws IOException {
        try {
            JsonObject requestJson = Util.requestBodyToJsonObject(request);
            JsonArray documentsIds = requestJson.getJsonArray("documentsIds");
            List<UUID> documentsUuids = new ArrayList<UUID>();
            for(int i = 0; i < documentsIds.size(); i++) {
                documentsUuids.add(Util.uuidFromObject(documentsIds.getString(i)));
            }
            List<Document> documents = new ArrayList<Document>();
            for(UUID uuid : documentsUuids) {
                documents.add(database.getDocument(uuid.toString()));
            }
            return Util.generateObjectContainingArray("Documents", documents).toString();
        } catch(Exception e) {
            return Util.generateErrorJsonString(e.getMessage());
        }
    }

    private String getDocumentsByUsername(String username) {
        Collection<Document> documents = database.getDocuments(username);
        if(documents != null) {
            Set<Document> documentsWithActionRequired = fillterByAddToDeliveryFlag(username, documents);
            return Util.generateObjectContainingArray("Documents", documentsWithActionRequired).toString();
        } else {
            return Util.generateErrorJsonString("Error. Can't find documents for user " + username + ". Probably session was expired or not created yet.");
        }
    }

    public Set<Document> fillterByAddToDeliveryFlag(String username, Collection<Document> documents) {
        Set<Document> filteredByActionRequired = new HashSet<Document>();
        for(Document document : documents) {
            document.updateActionRequiredFlagAndAddToDeliveryFlagAndInspectedFlagForCurrentUser(username);
            if(document.isAddToDelivery()) {
                filteredByActionRequired.add(document);
            }
        }
        filteredByActionRequired = new TreeSet<Document>(filteredByActionRequired);
        return filteredByActionRequired;
    }

    @RequestMapping(value = "/rejectDocument", method = RequestMethod.POST)
    public @ResponseBody String rejectDocument(HttpServletRequest request) throws IOException {
        User user = database.getUserByUsername(Util.getUsername(request));
        JsonObject requestJson = Util.requestBodyToJsonObject(request);
        String id  = requestJson.getString("id");
        String comment  = requestJson.getString("comment");

        Document document = database.getDocument(id);
        if(document == null)
            return Util.generateErrorJsonString("Error. Can't find document " + id + " in database.");

        DocumentSignatureBlock signatureBlock = document.getDocumentSignatureBlockForUser(user);

        signatureBlock.setStatus(Status.REJECTED);
        signatureBlock.setComment(comment);
        signatureBlock.setActionDate(System.currentTimeMillis());

        for(DocumentSignatureBlock anySignatureBlock : document.getDocumentSignatureBlocks()) {
            if(anySignatureBlock.getIndex() > signatureBlock.getIndex()) {
                anySignatureBlock.setStatus(Status.CANCELLED);
            }
        }

        document.updateStatusAccordingToSignatureBlocks();
        document.setLastUpdateDate(System.currentTimeMillis());

        try {
            document.setContent(new SignatureRejector().rejectDocument(database.getDocumentContent(document.getId().toString()), signatureBlock));
        } catch (DocumentIntegrityException e) {
            document.setContent(database.getDocumentContent(document.getId()));
            signatureUtil.removeDocument(document);
            return Util.generateErrorJsonString("The document " + document.getDocumentName() + " was changed after the last signature and it should be removed.");
        } catch (Exception e) {
            log.error("Failed to set rejection stamp to document " + document.getDocumentName());
        }

        database.updateDocument(document, true);

        if(!document.getAuthor().equals(user)) {
            emailSender.sendEmailToDocumentAuthorAboutRejectionInSeparateThread(document, user, comment);
        }

        return document.asJson().toString();
    }

    @RequestMapping(value = "/getDocumentInfo", method = RequestMethod.GET)
    public @ResponseBody String getDocumentInfo(@RequestParam("id") String id, HttpServletRequest request) throws IOException {
        Document document = database.getDocument(id);
        document.updateActionRequiredFlagAndAddToDeliveryFlagAndInspectedFlagForCurrentUser(Util.getUsername(request));
        return document.asJson().toString();
    }

    @RequestMapping(value = "/api/getDocumentInfo", method = RequestMethod.GET)
    public @ResponseBody String getDocumentInfoAPI(@RequestParam("id") String id, HttpServletRequest request) throws IOException {
        return getDocumentInfo(id, request);
    }

    @RequestMapping(value = "/downloadDocument", method = RequestMethod.GET)
    public void downloadDocument(@RequestParam("id") String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            byte[] content = database.getDocumentContent(id);
            InputStream is;
            if(content != null && content.length > 0) {
                is = new ByteArrayInputStream(content);
            } else {
                is = new ByteArrayInputStream(("Content of file " + id + " is empty.").getBytes("UTF-8"));
            }
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();

            Document document = database.getDocument(id);
            for(DocumentSignatureBlock documentSignatureBlock : document.getDocumentSignatureBlocks()) {
                if(documentSignatureBlock.getSigner().getUsername().equals(Util.getUsername(request))) {
                    documentSignatureBlock.setInspected(true);
                    database.updateDocument(document, false);
                    return;
                }
            }
        } catch (IOException ex) {
            throw new IndigoSignatureServiceException("IOError writing file to output stream.'" + id + "'");
        }
    }

    @RequestMapping(value = "/api/downloadDocument", method = RequestMethod.GET)
    public void downloadDocumentAPI(@RequestParam("id") String id, HttpServletRequest request, HttpServletResponse response) {
        downloadDocument(id, request, response);
    }

    @RequestMapping(value = "/api/getFinalStatus", method = RequestMethod.GET)
    public @ResponseBody String getFinalStatus() {
        return signatureProperties.getFinalStatus();
    }

    @RequestMapping(value = "/break", method = RequestMethod.GET)
    public void breakDocument(@RequestParam("id") String id, HttpServletResponse response) {
        Document document = database.getDocument(id);
        try {
            document.setContent(new SignatureRejector().breakDocumentIntegrity(database.getDocumentContent(document.getId().toString())));
        } catch (Exception e) {
            log.warn("Failed to break the document " + document.getDocumentName());
        }
        database.updateDocument(document, true);

        try {
            byte[] content = database.getDocumentContent(id);
            InputStream is;
            if(content != null && content.length > 0) {
                is = new ByteArrayInputStream(content);
            } else {
                is = new ByteArrayInputStream(("Content of file " + id + " is empty.").getBytes("UTF-8"));
            }
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new IndigoSignatureServiceException("IOError writing file to output stream.'" + id + "'");
        }
        
    }
}
