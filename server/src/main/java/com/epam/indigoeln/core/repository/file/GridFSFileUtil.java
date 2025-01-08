/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.repository.file;

import com.epam.indigoeln.core.model.User;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import java.util.Comparator;
import java.util.Optional;

public final class GridFSFileUtil {

    private static final String FILENAME = "filename";
    private static final String CONTENT_TYPE = "contentType";
    private static final String LENGTH = "length";
    private static final String UPLOAD_DATE = "uploadDate";

    private static final Comparator<GridFsResource> FILENAME_COMPARATOR =
            (o1, o2) -> compare(o1.getFilename(), o2.getFilename());

    private static final Comparator<GridFsResource> CONTENT_TYPE_COMPARATOR =
            (o1, o2) -> compare(o1.getContentType(), o2.getContentType());

    private static final Comparator<GridFsResource> LENGTH_COMPARATOR =
            (o1, o2) -> compare(o1.getGridFSFile().getLength(), o2.getGridFSFile().getLength());

    static final Comparator<GridFsResource> UPLOAD_DATE_COMPARATOR =
            (o1, o2) -> compare(o1.getGridFSFile().getUploadDate(), o2.getGridFSFile().getUploadDate());

    private GridFSFileUtil() {
    }

    public static User getAuthorFromMetadata(Document metadata) {
        User author = new User();
        BasicBSONObject bsonAuthor = (BasicBSONObject) metadata.get("author");
        if (bsonAuthor != null) {
            author.setId(bsonAuthor.getString("id"));
            author.setFirstName(bsonAuthor.getString("firstName"));
            author.setLastName(bsonAuthor.getString("lastName"));
        }
        return author;
    }

    public static boolean isTemporaryFromMetadata(BSONObject metadata) {
        return (Boolean) metadata.get("temporary");
    }

    static void setAuthorToMetadata(Document metadata, User author) {
        BSONObject bsonAuthor = new BasicBSONObject(3);
        bsonAuthor.put("id", author.getId());
        bsonAuthor.put("firstName", author.getFirstName());
        bsonAuthor.put("lastName", author.getLastName());

        metadata.put("author", bsonAuthor);
    }

    public static void setTemporaryToMetadata(Document metadata, boolean temporary) {
        metadata.put("temporary", temporary);
    }

    static Optional<Comparator<GridFsResource>> getComparator(String gridFSFileField) {
        Comparator<GridFsResource> comparator = null;
        if (FILENAME.equals(gridFSFileField)) {
            comparator = FILENAME_COMPARATOR;
        } else if (CONTENT_TYPE.equals(gridFSFileField)) {
            comparator = CONTENT_TYPE_COMPARATOR;
        } else if (LENGTH.equals(gridFSFileField)) {
            comparator = LENGTH_COMPARATOR;
        } else if (UPLOAD_DATE.equals(gridFSFileField)) {
            comparator = UPLOAD_DATE_COMPARATOR;
        }
        return Optional.ofNullable(comparator);
    }

    private static <T> int compare(Comparable<T> o1, T o2) {
        if (o1 != null && o2 != null) {
            return o1.compareTo(o2);
        } else if (o1 != null) {
            return 1;
        } else if (o2 != null) {
            return -1;
        }
        return 0;
    }
}
