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
package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.repository.file.FileRepository;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TemporaryFileCleaningJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryFileCleaningJob.class);

    private static final long DELAY = 1000 * 60 * 60L; // 60 minutes

    @Autowired
    private FileRepository fileRepository;

    @Scheduled(fixedDelay = DELAY)
    public void execute() {
        LOGGER.debug("Temporary file cleaning job started");
        try {
            final LocalDateTime threshold = LocalDateTime.now().minus(1, ChronoUnit.WEEKS);
            final List<GridFSDBFile> temporaryFiles = fileRepository.findAllTemporary();
            final Set<String> fileIdsToDelete = temporaryFiles.stream().filter(tf -> {
                final LocalDateTime uploadDate = tf.getUploadDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                return uploadDate.isBefore(threshold);
            }).map(tf -> (String) tf.getId()).collect(Collectors.toSet());
            if (!fileIdsToDelete.isEmpty()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Deleting temporary files: " + fileIdsToDelete);
                }
                fileRepository.delete(fileIdsToDelete);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing temporary file cleaning job!", e);
        }
    }
}
