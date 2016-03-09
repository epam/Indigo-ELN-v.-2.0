package com.epam.indigoeln.sheduler;

import com.epam.indigoeln.core.service.util.TempFileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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

    private static final Logger log = LoggerFactory.getLogger(TempDirectoryCleaningJob.class);

    private static final long DELAY = 1000 * 60 * 60; // 60 minutes


    @Scheduled(fixedDelay = DELAY)
    public void execute() {
        log.debug("Temp directory cleaning job started");
        try {
            File tempDirectory = FileUtils.getTempDirectory();
            File[] directoryListing = tempDirectory.listFiles();

            if (directoryListing != null) {

                for (File file : directoryListing) {
                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    boolean isOlderThanOneDay = new Date().getTime() - attr.creationTime().toMillis() > TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);

                    if (isOlderThanOneDay && StringUtils.startsWith(file.getName(), TempFileUtil.TEMP_FILE_PREFIX)) {
                        FileUtils.deleteQuietly(file);
                    }
                }
            } else {
                log.debug("Failed to access the temp folder");
            }
        } catch (Exception e) {
            log.error("Error executing temp directory cleaning job!", e);
        }
    }
}
