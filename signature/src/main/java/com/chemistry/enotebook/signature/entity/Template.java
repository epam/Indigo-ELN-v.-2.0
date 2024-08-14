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

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Template extends JsonRepresentable {
    private UUID id;
    private String name;
    private User author;
    private List<TemplateSignatureBlock> templateSignatureBlocks = new ArrayList<TemplateSignatureBlock>();

    public Template(UUID id){
        super();
        this.id = id;
    }
    /*
     {
         "name":"Template 1",
         "signatureBlocks":[
             {
             "index":1,
             "userName":"Duke Som",
             "reason":"I'm an author",
             "comment":"comment for signature block author"
             },
             {
             "index":2,
             "userName":"Kole Snider",
             "reason":"witness",
             "comment":"comment for signature block witness"
             }
         ]
     }
     */
    public Template(String jsonSrt, DatabaseConnector database) {
        super();
        JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(jsonSrt.getBytes()));
        JsonObject templateJson = jsonReader.readObject();

        this.id = templateJson.containsKey("id") ? java.util.UUID.fromString(templateJson.getString("id")) : java.util.UUID.randomUUID();

        this.name = templateJson.getString("name");

        JsonArray signatureBlocks = templateJson.getJsonArray("signatureBlocks");

        for(JsonObject signatureBlock : signatureBlocks.toArray(new JsonObject[0])) {
            this.templateSignatureBlocks.add(new TemplateSignatureBlock(signatureBlock, database));
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    private String getAuthorFullName() {
        if(author != null) {
            return author.getFirstName() + " " + author.getLastName();
        }
        return "";
    }

    public List<TemplateSignatureBlock> getTemplateSignatureBlocks() {
        return templateSignatureBlocks;
    }

    public void addSignatureBlock(TemplateSignatureBlock templateSignatureBlock) {
        this.templateSignatureBlocks.add(templateSignatureBlock);
    }

    public JsonArray signatureBlocksJsonArray() {
        JsonArrayBuilder builder = Json.createArrayBuilder();

        for(TemplateSignatureBlock templateSignatureBlock : templateSignatureBlocks) {
            builder.add(templateSignatureBlock.toJson());
        }

        return builder.build();
    }

    public static Template generateTemplate(Map row, DatabaseConnector database) {
        Template template = new Template((UUID)row.get("templateid"));
        template.setName((String) row.get("templatename"));

        UUID author;
        if(row.containsKey("templateauthorid")) {
            author = (UUID)row.get("templateauthorid");
        } else {
            author = (UUID)row.get("userid");
        }
        if(author != null) {
            template.setAuthor(database.getUserByUserId(author.toString()));
        }
        return template;
    }

    @Override
    public JsonObject asJson() {
        JsonObject document = Json.createObjectBuilder()
                .add("id", id.toString())
                .add("name", name)
                .add("author", getAuthorFullName())
                .add("signatureBlocks", signatureBlocksJsonArray())
                .build();
        return document;
    }
}
