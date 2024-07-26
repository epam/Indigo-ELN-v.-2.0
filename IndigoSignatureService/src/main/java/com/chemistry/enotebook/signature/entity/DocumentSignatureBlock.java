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
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

public class DocumentSignatureBlock extends JsonRepresentable implements Comparable<DocumentSignatureBlock> {
    private UUID id;
    private int index;
    private User signer;
    private Reason reason;
    private long actionDate;
    private Status status;
    private String comment = "";
    private boolean inspected;

    public DocumentSignatureBlock(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public User getSigner() {
        return signer;
    }

    public void setSigner(User signer) {
        this.signer = signer;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public long getActionDate() {
        return actionDate;
    }

    public void setActionDate(long actionDate) {
        this.actionDate = actionDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if(comment != null) {
            this.comment = comment;
        }
    }

    public boolean isInspected() {
        return inspected;
    }

    public void setInspected(boolean inspected) {
        this.inspected = inspected;
    }

    public static DocumentSignatureBlock generateDocumentSigningStatus(Map row) {
        User signer = new User();
        signer.setUserId((UUID)row.get("signerid"));
        signer.setUsername((String)row.get("signerusername"));
        signer.setFirstName((String) row.get("signerfirstname"));
        signer.setLastName((String) row.get("signerlastname"));
        signer.setEmail((String) row.get("signeremail"));

        long actionDate = 0;
        if(row.get("actiondate") != null) {
            actionDate = ((Timestamp)row.get("actiondate")).getTime();
        }

        DocumentSignatureBlock documentSignatureBlock = new DocumentSignatureBlock((UUID)row.get("documentsignatureblockid"));

        documentSignatureBlock.setIndex((Integer) row.get("documentsignatureblockindex"));
        documentSignatureBlock.setSigner(signer);
        documentSignatureBlock.setReason(Reason.reasonMap.get(row.get("reasonid")));
        documentSignatureBlock.setActionDate(actionDate);
        documentSignatureBlock.setStatus(Status.statuses.get(row.get("signerstatus")));
        documentSignatureBlock.setComment((String) row.get("comment"));

        if(row.get("inspected") != null) {
            documentSignatureBlock.setInspected((Boolean)row.get("inspected"));
        }

        return documentSignatureBlock;
    }

    @Override
    public JsonObject asJson() {
        return Json.createObjectBuilder()
                .add("index", index)
                .add("firstname", signer.getFirstName())
                .add("lastname", signer.getLastName())
                .add("reason", reason.id())
                .add("actionDate", Util.convertToStringDate(actionDate))
                .add("status", status.code())
                .add("comment", comment)
                .add("inspected", inspected)
                .build();
    }

    @Override
    public int compareTo(DocumentSignatureBlock o) {
        return this.getIndex() - o.getIndex();
    }
}
