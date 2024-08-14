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
package com.chemistry.enotebook.signature.archiver;

import com.chemistry.enotebook.signature.database.DatabaseConnector;
import com.chemistry.enotebook.signature.entity.Document;
import com.chemistry.enotebook.signature.entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SignatureServiceArchiver {
    private static Logger log = LoggerFactory.getLogger(SignatureServiceArchiver.class);
    private Archiver archiver;
    @Autowired
    private DatabaseConnector database;

    public SignatureServiceArchiver(String archiverClassName) {
        this.archiver = initArchiver(archiverClassName);
    }

    private Archiver initArchiver(String archiverClassName) {
        Object newInstance = null;
        try {
            Class<?> implClazz = Thread.currentThread().getContextClassLoader().loadClass(archiverClassName);
            newInstance = implClazz.newInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new RuntimeException(
                    "Unable to load implementation of the archiver: "
                            + archiverClassName, ex);
        }
        return (Archiver)newInstance;
    }

    public void updateDocumentStatusAndArchive(Document document) {
        document.setLastUpdateDate(System.currentTimeMillis());
        if(document.getStatus() == Status.SIGNED || document.getStatus() == Status.ARCHIVING) {
            document.setStatus(Status.ARCHIVED);
            archiver.archiveDocument(document, database);
            database.updateDocument(document, false);
        }
    }
}
