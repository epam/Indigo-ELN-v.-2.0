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

import com.epam.indigoeln.core.service.util.TempFileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class TempDirectoryCleaningJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempDirectoryCleaningJob.class);

    private static final long DELAY = 1000 * 60 * 60L; // 60 minutes


    @Scheduled(fixedDelay = DELAY)
    public void execute() {
        LOGGER.debug("Temp directory cleaning job started");
        try {
            File tempDirectory = FileUtils.getTempDirectory();
            File[] directoryListing = tempDirectory.listFiles();

            if (directoryListing != null) {
                deleteFiles(directoryListing);
            } else {
                LOGGER.debug("Failed to access the temp folder");
            }
        } catch (Exception e) {
            LOGGER.error("Error executing temp directory cleaning job!", e);
        }
    }

    private void deleteFiles(File[] directoryListing) throws java.io.IOException {
        for (File file : directoryListing) {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            boolean isOlderThanOneDay = new Date().getTime() - attr.creationTime().toMillis()
                    > TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);

            if (isOlderThanOneDay && StringUtils.startsWith(file.getName(), TempFileUtil.TEMP_FILE_PREFIX)) {
                FileUtils.deleteQuietly(file);
            }
        }
    }
}
