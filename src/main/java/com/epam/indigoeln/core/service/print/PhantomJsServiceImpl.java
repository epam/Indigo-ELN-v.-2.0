package com.epam.indigoeln.core.service.print;

import com.epam.indigoeln.core.service.util.TempFileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PhantomJsServiceImpl implements PhantomJsService {

    private static final String OS_WINDOWS = "Win";
    private static final String OS_MACOS = "Mac";
    private static final String OS_LINUX = "Linux";

    private static final Logger log = LoggerFactory.getLogger(PhantomJsServiceImpl.class);
    private static final String[] commandLineArguments = new String[]{
            "--web-security=no",
            "--ssl-protocol=any",
            "--ignore-ssl-errors=yes"
    };
    private PhantomJSDriverService phantomJSDriverService;
    private PhantomJSDriver driver;

    @PostConstruct
    public void init() {
        try {
            File file = new File(getPhantomPath());
            file.setExecutable(true);
            phantomJSDriverService = new PhantomJSDriverService.Builder()
                    .usingPhantomJSExecutable(file)
                    .usingAnyFreePort()
                    .usingCommandLineArguments(commandLineArguments)
                    .build();
            phantomJSDriverService.start();

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setJavascriptEnabled(false);
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, commandLineArguments);
            capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
            driver = new PhantomJSDriver(phantomJSDriverService, capabilities);
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("PhantomJs init error", e);
        }

    }

    @PreDestroy
    public void destroy() {
        try {
            if (driver != null) {
                driver.quit();
            }
            if (phantomJSDriverService != null) {
                phantomJSDriverService.stop();
            }
        } catch (Exception e) {
            log.error("PhantomJs destroy error", e);
        }
    }

    @Override
    public <T> T takesScreenshot(String html, OutputType<T> type) {
        File tmpFile = null;
        try {
            tmpFile = new File(FileUtils.getTempDirectory(), String.format("%s%s.%s", TempFileUtil.TEMP_FILE_PREFIX, UUID.randomUUID().toString(), "html"));
            FileUtils.writeStringToFile(tmpFile, html);
            return takesScreenshot(tmpFile.toURI(), type);
        } catch (Exception e) {
            log.error("Take screenshot error", e);
            throw new RuntimeException("Error creating snapshot from HTML");
        } finally {
            FileUtils.deleteQuietly(tmpFile);
        }
    }

    @Override
    public <T> T takesScreenshot(URI uri, OutputType<T> type) {
        return getScreenshotAs(uri, type, 0, 5);
    }

    private <T> T getScreenshotAs(URI uri, OutputType<T> type, int count, int limit) {
        try {
            synchronized (this) {
                driver.get(uri.toString());
                return driver.getScreenshotAs(type);
            }
        } catch (Exception e) {
            log.error("Get screenshot error", e);
            if (count < limit) {
                destroy();
                init();
                return getScreenshotAs(uri, type, count + 1, limit);
            } else {
                throw new RuntimeException("Error creating snapshot from HTML");
            }
        }
    }

    private static URI getPhantomPath() {
        StringBuilder sb = new StringBuilder(22);

        String separator = "/";
        String os = getOs();

        sb.append("phantomjs").append(separator).append(os).append(separator);

        if (StringUtils.equals(os, OS_LINUX)) {
            String arch = System.getProperty("os.arch");

            if (StringUtils.equals(arch, "x86") || StringUtils.equals(arch, "i386")) {
                sb.append("i686").append(separator);
            }

            if (StringUtils.equals(arch, "x86_64") || StringUtils.equals(arch, "amd64")) {
                sb.append("x86_64").append(separator);
            }
        }

        sb.append("phantomjs");

        if (StringUtils.equals(os, OS_WINDOWS)) {
            sb.append(".exe");
        }

        URL url = PhantomJsServiceImpl.class.getClassLoader().getResource(sb.toString());

        try {
            return url != null ? url.toURI() : null;
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Phantom executable path is incorrect");
        }
    }

    private static String getOs() {
        String os = System.getProperty("os.name");
        if (os.matches("^Windows.*")) {
            return OS_WINDOWS;
        } else if (os.matches("^Mac OS.*")) {
            return OS_MACOS;
        } else if (os.matches("^Linux.*")) {
            return OS_LINUX;
        } else {
            throw new RuntimeException("Operating system not recognized");
        }
    }
}
