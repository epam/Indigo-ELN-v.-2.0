package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.file.GridFSFileUtil;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSFile;

import java.util.Date;

public class FileDTO {
    private String id;
    private String filename;
    private String contentType;
    private Date uploadDate;
    private long length;
    private User author;


    public FileDTO(){
        super();
    }

    public FileDTO(GridFSFile file) {
        this.id = file.getId().toString();
        this.filename = file.getFilename();
        this.contentType = file.getContentType();
        this.uploadDate = file.getUploadDate();
        this.length = file.getLength();
        convertMetadata(file.getMetaData());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    private void convertMetadata(DBObject metadata) {
        author = GridFSFileUtil.getAuthorFromMetadata(metadata);
    }
}