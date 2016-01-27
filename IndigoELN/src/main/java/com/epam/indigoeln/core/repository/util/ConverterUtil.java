package com.epam.indigoeln.core.repository.util;

import com.epam.indigoeln.core.model.User;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public final class ConverterUtil {

    private ConverterUtil() {
    }

    public static User getAuthorFromFileMetadata(BSONObject metadata) {
        User author = new User();
        BasicBSONObject bsonAuthor = (BasicBSONObject) metadata.get("author");
        if (bsonAuthor != null) {
            author.setId(bsonAuthor.getString("id"));
            author.setFirstName(bsonAuthor.getString("firstName"));
            author.setLastName(bsonAuthor.getString("lastName"));
        }
        return author;
    }

    public static void setAuthorToFileMetadata(BSONObject metadata, User author) {
        BSONObject bsonAuthor = new BasicBSONObject(3);
        bsonAuthor.put("id", author.getId());
        bsonAuthor.put("firstName", author.getFirstName());
        bsonAuthor.put("lastName", author.getLastName());

        metadata.put("author", bsonAuthor);
    }
}