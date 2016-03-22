package com.epam.indigoeln.core.repository.file;

import com.epam.indigoeln.core.model.User;
import com.mongodb.gridfs.GridFSFile;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import java.util.Comparator;
import java.util.Optional;

public final class GridFSFileUtil {

    public static final String FILENAME = "filename";
    public static final String CONTENT_TYPE = "contentType";
    public static final String LENGTH = "length";
    public static final String UPLOAD_DATE = "uploadDate";

    private GridFSFileUtil() {
    }

    public static User getAuthorFromMetadata(BSONObject metadata) {
        User author = new User();
        BasicBSONObject bsonAuthor = (BasicBSONObject) metadata.get("author");
        if (bsonAuthor != null) {
            author.setId(bsonAuthor.getString("id"));
            author.setFirstName(bsonAuthor.getString("firstName"));
            author.setLastName(bsonAuthor.getString("lastName"));
        }
        return author;
    }

    public static void setAuthorToMetadata(BSONObject metadata, User author) {
        BSONObject bsonAuthor = new BasicBSONObject(3);
        bsonAuthor.put("id", author.getId());
        bsonAuthor.put("firstName", author.getFirstName());
        bsonAuthor.put("lastName", author.getLastName());

        metadata.put("author", bsonAuthor);
    }

    public static Optional<Comparator<GridFSFile>> getComparator(String gridFSFileField) {
        Comparator<GridFSFile> comparator = null;
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

    private static final Comparator<GridFSFile> FILENAME_COMPARATOR =
            (o1, o2) -> compare(o1.getFilename(), o2.getFilename());

    private static final Comparator<GridFSFile> CONTENT_TYPE_COMPARATOR =
            (o1, o2) -> compare(o1.getContentType(), o2.getContentType());

    private static final Comparator<GridFSFile> LENGTH_COMPARATOR =
            (o1, o2) -> compare(o1.getLength(), o2.getLength());

    private static final Comparator<GridFSFile> UPLOAD_DATE_COMPARATOR =
            (o1, o2) -> compare(o1.getUploadDate(), o2.getUploadDate());

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