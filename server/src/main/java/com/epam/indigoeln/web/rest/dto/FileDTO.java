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


    public FileDTO() {
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
        return uploadDate == null ? null : new Date(uploadDate.getTime());
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate == null ? null : new Date(uploadDate.getTime());
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
