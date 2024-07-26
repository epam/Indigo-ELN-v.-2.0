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
package com.chemistry.enotebook.signature.entity;

import com.chemistry.enotebook.signature.Util;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.*;

public class Document extends JsonRepresentable implements Comparable<Document> {
    private UUID id;
    private String documentName;
    private Status status;
    private long creationDate;
    private long lastUpdateDate;
    private User author;
    private Collection<DocumentSignatureBlock> documentSignatureBlocks;
    private byte[] content;

    private boolean actionRequired;
    private boolean addThisDocumentToDelivery;
    private boolean inspected;

    public Document(UUID id, User author, String documentName) {
        this.id = id;
        this.author = author;
        this.documentName = documentName;

        this.creationDate = System.currentTimeMillis();
        this.lastUpdateDate = this.creationDate;
        this.status = Status.SUBMITTED;
        documentSignatureBlocks = new TreeSet<DocumentSignatureBlock>();
    }

    public UUID getId() {
        return id;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public User getAuthor() {
        return author;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void addDocumentSignatureBlock(DocumentSignatureBlock documentSignatureBlock) {
        this.documentSignatureBlocks.add(documentSignatureBlock);
    }

    public Collection<DocumentSignatureBlock> getDocumentSignatureBlocks() {
        return documentSignatureBlocks;
    }

    public DocumentSignatureBlock getDocumentSignatureBlockForUser(User user) {
        return getDocumentSignatureBlockForUser(user.getUsername());
    }

    public DocumentSignatureBlock getDocumentSignatureBlockForUser(String username) {
        for(DocumentSignatureBlock documentSignatureBlock : documentSignatureBlocks) {
            if(documentSignatureBlock.getSigner().getUsername().equals(username)) {
                return documentSignatureBlock;
            }
        }
        return null;
    }

    public DocumentSignatureBlock getDocumentSignatureBlockForIndex(int index) {
        for(DocumentSignatureBlock documentSignatureBlock : documentSignatureBlocks) {
            if(documentSignatureBlock.getIndex() == index) {
                return documentSignatureBlock;
            }
        }
        return null;
    }

    public Status updateStatusAccordingToSignatureBlocks() {
        if(!this.getStatus().equals(Status.SIGNED) && !this.getStatus().equals(Status.REJECTED)) {
            boolean allSigned = true;
            boolean someSigned = false;
            for(DocumentSignatureBlock signatureBlock : this.getDocumentSignatureBlocks()) {
                allSigned = allSigned && signatureBlock.getStatus().equals(Status.SIGNED);
                someSigned = someSigned || signatureBlock.getStatus().equals(Status.SIGNED);
                if(signatureBlock.getStatus().equals(Status.REJECTED)) {
                    this.setStatus(Status.REJECTED);
                    return this.getStatus();
                }
            }

            if(allSigned){
                this.setStatus(Status.SIGNED);
            } else if(someSigned) {
                this.setStatus(Status.SIGNING);
            } else {
                this.setStatus(Status.SUBMITTED);
            }
        }

        return this.getStatus();
    }

    public static Document generateDocument(Map row) {
        User author = new User();
        author.setUserId((UUID)row.get("authorid"));
        author.setUsername((String) row.get("authorusername"));
        author.setFirstName((String) row.get("authorfirstname"));
        author.setLastName((String) row.get("authorlastname"));
        author.setEmail((String) row.get("authoremail"));
        Document document = new Document((UUID)row.get("documentid"),author, (String)row.get("name"));
        document.setStatus(Status.statuses.get(row.get("documentstatus")));
        document.setCreationDate(Util.dateLongFromObject(row.get("creationdate")));
        document.setLastUpdateDate(Util.dateLongFromObject(row.get("lastupdatedate")));
        if(row.containsKey("documentfile")) {
            document.setContent(((byte[]) row.get("documentfile")));
        }

        return document;
    }

    public void updateActionRequiredFlagAndAddToDeliveryFlagAndInspectedFlagForCurrentUser(String username) {
        if(this.getAuthor().getUsername().equals(username)) {
            this.setAddToDelivery(true);
        }
        DocumentSignatureBlock usersDocumentSignatureBlock = this.getDocumentSignatureBlockForUser(username);
        if(usersDocumentSignatureBlock != null) {
            if(usersDocumentSignatureBlock.getStatus().equals(Status.SIGNING)) {
                this.setActionRequired(true);
            }
            if(usersDocumentSignatureBlock.getStatus().equals(Status.SIGNING) ||
               usersDocumentSignatureBlock.getStatus().equals(Status.SIGNED) ||
               usersDocumentSignatureBlock.getStatus().equals(Status.REJECTED)) {
                this.setAddToDelivery(true);
            }
            this.setInspected(usersDocumentSignatureBlock.isInspected());
        }
    }

    @Override
    public JsonObject asJson() {
        return Json.createObjectBuilder()
                .add("id", id.toString())
                .add("documentName", documentName)
                .add("status", status.code())
                .add("creationDate", Util.convertToStringDate(creationDate))
                .add("lastUpdateDate", Util.convertToStringDate(lastUpdateDate))
                .add("firstname", author.getFirstName())
                .add("lastname", author.getLastName())
                .add("witnesses", Util.generateJsonArray(documentSignatureBlocks))
                .add("actionRequired", actionRequired)
                .add("inspected", inspected)
                .build();
    }

    public void setActionRequired(boolean actionRequired) {
        this.actionRequired = actionRequired;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public void setAddToDelivery(boolean addToDelivery) {
        this.addThisDocumentToDelivery = addToDelivery;
    }

    public boolean isAddToDelivery() {
        return addThisDocumentToDelivery;
    }

    public boolean isInspected() {
        return inspected;
    }

    public void setInspected(boolean inspected) {
        this.inspected = inspected;
    }

    @Override
    public int compareTo(Document o) {
        long byCreationDate = o.getCreationDate() - this.getCreationDate();
        if(byCreationDate == 0) {
            int byName = this.getDocumentName().compareTo(o.getDocumentName());
            if(byName == 0) {
                return this.getId().compareTo(o.getId());
            }
            return byName;

        }
        return byCreationDate > 0 ? 1 : -1;
    }
}
