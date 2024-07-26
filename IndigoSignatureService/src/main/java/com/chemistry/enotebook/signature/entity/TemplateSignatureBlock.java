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

import com.chemistry.enotebook.signature.database.DatabaseConnector;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Map;
import java.util.UUID;

public class TemplateSignatureBlock {
    private UUID id;
    private User user;
    private Reason reason;
    private int index;

    public TemplateSignatureBlock() {
        super();
    }

    /*
    {
         "index":1,
         "userName":"Duke Som",
         "reason":"witness",
     }
    */
    public TemplateSignatureBlock(JsonObject signatureBlockJson, DatabaseConnector database) {
        super();
        this.index = signatureBlockJson.getInt("index");
        String userId = signatureBlockJson.getString("userId");
        this.user = database.getUserByUserId(userId);

        int reasonIdStr = signatureBlockJson.getInt("reason");
        for(Reason reason : Reason.values()) {
            if(reason.id() == reasonIdStr) {
                this.reason = reason;
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static TemplateSignatureBlock generateTemplate(Map row, DatabaseConnector database) {
        TemplateSignatureBlock templateSignatureBlock = new TemplateSignatureBlock();
        templateSignatureBlock.setId((UUID)row.get("templatesignatureblockid"));
        templateSignatureBlock.setUser(database.getUserByUserId((row.get("userid")).toString()));
        templateSignatureBlock.setReason(Reason.reasonMap.get(row.get("reasonid")));
        templateSignatureBlock.setIndex((Integer)row.get("templatesignatureblockindex"));
        return templateSignatureBlock;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("index", this.getIndex())
                .add("userid", this.getUser().getUserId().toString())
                .add("username", this.getUser().getUsername())
                .add("userfirstname", this.getUser().getFirstName())
                .add("userlastname", this.getUser().getLastName())
                .add("reason", this.getReason().id())
                .build();
    }
}
