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
package com.chemistry.enotebook.signature.archiver.ftp;

import java.util.UUID;

import com.chemistry.enotebook.signature.database.DatabaseConnector;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.chemistry.enotebook.signature.entity.Document;
import com.chemistry.enotebook.signature.entity.FtpQueueStatus;

@Service	
public class FtpUploaderImpl implements FtpUploader {
	private static final Logger log = Logger.getLogger(FtpUploaderImpl.class);
	
	@Autowired
    private DatabaseConnector database;

	@Autowired
	private FtpGateway ftpGateway;
	
	@Override
	@Async
	@Scheduled(cron="${poller.cron}")
	public void dequeueAndUpload() {
		log.debug("Dequeing document");
		UUID docId = database.dequeueForFtp();
		if (docId == null) {
			log.info("Nothing to send via ftp");
			return;
		}
		
		log.debug("Starting transfer of " + docId.toString());
		database.updateFtpQueue(docId, FtpQueueStatus.SENDING);
		Document document = database.getDocument(docId.toString());
		byte[] content = database.getDocumentContent(docId);
		try {
			ftpGateway.sendFile(content, document.getDocumentName());
			database.updateFtpQueue(docId, FtpQueueStatus.SENT);
		} catch(Exception e) {
			Throwable root = ExceptionUtils.getRootCause(e);
			String message = root == null ? e.getMessage() : root.getMessage();
			log.error("Ftp transfer failed: " + message);
			database.updateFtpQueue(docId, FtpQueueStatus.QUEUED);
		}
	}
	
	

}
