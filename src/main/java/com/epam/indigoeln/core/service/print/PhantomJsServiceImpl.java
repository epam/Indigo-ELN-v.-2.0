package com.epam.indigoeln.core.service.print;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.service.util.TempFileUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PhantomJsServiceImpl implements PhantomJsService {

    private static final String OS_WINDOWS = "Win";
    private static final String OS_MACOS = "Mac";
    private static final String OS_LINUX = "Linux";

    private static final Logger LOGGER = LoggerFactory.getLogger(PhantomJsServiceImpl.class);
    private static final String[] commandLineArguments = new String[]{
            "--web-security=no",
            "--ssl-protocol=any",
            "--ignore-ssl-errors=yes"
    };
    private PhantomJSDriverService phantomJSDriverService;
    private PhantomJSDriver driver;
    private String rasterize;
    private FileAlterationMonitor fileMonitor;
    private FileAlterationObserver fileObserver;
    private int timeout = 10;

    public PhantomJsServiceImpl() throws IOException {
        rasterize = Resources.toString(Resources.getResource("phantomjs/rasterize.js"), Charsets.UTF_8);
    }

    @PostConstruct
    public void init() {
        try {
            File file = getPhantomExecutable();
            phantomJSDriverService = new PhantomJSDriverService.Builder()
                    .usingPhantomJSExecutable(file)
                    .usingAnyFreePort()
                    .usingCommandLineArguments(commandLineArguments)
                    .build();
            phantomJSDriverService.start();

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setJavascriptEnabled(false);
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, commandLineArguments);
            driver = new PhantomJSDriver(phantomJSDriverService, capabilities);
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

            fileObserver = new FileAlterationObserver(
                    FileUtils.getTempDirectory(),
                    FileFilterUtils.and(FileFilterUtils.suffixFileFilter(TempFileUtil.TEMP_PDF_DONE_SUFFIX),
                            FileFilterUtils.prefixFileFilter(TempFileUtil.TEMP_FILE_PREFIX)));
            fileMonitor = new FileAlterationMonitor((long) 1000);
            fileMonitor.addObserver(fileObserver);
            fileMonitor.start();
        } catch (Exception e) {
            LOGGER.error("PhantomJs init error", e);
        }

    }

    @PreDestroy
    public void destroy() {
        try {
            if (phantomJSDriverService != null) {
                phantomJSDriverService.stop();
            }
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            LOGGER.error("PhantomJs destroy error", e);
        }
    }

    private static File getPhantomExecutable() {
        StringBuilder sb = new StringBuilder(22);
        String separator = "/";
        String os = getOs();
        sb.append("phantomjs").append(separator).append(os).append(separator);

        String arch = getArch(os);
        if (arch != null) {
            sb.append(arch).append(separator);
        }

        sb.append("phantomjs");

        if (StringUtils.equals(os, OS_WINDOWS)) {
            sb.append(".exe");
        }

        File file = new File(System.getProperty("java.io.tmpdir") + "/" + sb.toString());
        if (!file.exists()) {
            try (InputStream is = PhantomJsServiceImpl.class.getClassLoader().getResourceAsStream(sb.toString())) {
                FileUtils.copyToFile(is, file);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new IndigoRuntimeException("Phantom executable path is incorrect");
            }
        }
        file.setExecutable(true);
        return file;
    }

    private static String getArch(String os) {
        if (StringUtils.equals(os, OS_LINUX)) {
            String arch = System.getProperty("os.arch");

            if (StringUtils.equals(arch, "x86") || StringUtils.equals(arch, "i386")) {
                return "i686";
            }

            if (StringUtils.equals(arch, "x86_64") || StringUtils.equals(arch, "amd64")) {
                return "x86_64";
            }
        }
        return null;
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
            throw new IndigoRuntimeException("Operating system not recognized");
        }
    }


    @Override
    public String createPdf(HtmlWrapper wrapper) {
        String fileName = String.format("%s%s.pdf", TempFileUtil.TEMP_FILE_PREFIX, wrapper.getFileName());
        String filePath = FileUtils.getFile(FileUtils.getTempDirectory(), fileName).getAbsolutePath();
        AtomicBoolean isDone = new AtomicBoolean(false);
        String doneFilePath = filePath + ".done";
        FileAlterationListenerAdaptor listener = createFileListener(doneFilePath,isDone);
        fileObserver.addListener(listener);
        try {
            LOGGER.info("Start of creation pdf");
            createPdf(wrapper, filePath);
            waitForDone(isDone);
            return fileName;
        } finally {
            fileObserver.removeListener(listener);
            LOGGER.info("Listener was removed");
            FileUtils.deleteQuietly(new File(doneFilePath));
            LOGGER.info("Done file was deleted");
        }
    }

    private void waitForDone(AtomicBoolean isDone){
        double time = 0;
        while (!isDone.get()){
            if (time > timeout){
                LOGGER.error("Timeout error.");
                throw new IndigoRuntimeException("Timeout error.");
            }
            try {
                Thread.sleep(500);
                time += 0.5;
            } catch (InterruptedException e) {
                LOGGER.error("Error during waiting for done file.");
            }
        }
    }

    private void createPdf(HtmlWrapper wrapper, String to) {
        createPdf(wrapper, to, 0, 5);
    }

    private void createPdf(HtmlWrapper wrapper, String to, int count, int limit) {
        try {
            synchronized (this) {
                LOGGER.info("PhantomJS started execution of script");
                driver.executePhantomJS(rasterize, wrapper.getHtml(), wrapper.getHeader(), to, wrapper.getHeaderHeight());
            }
        } catch (Exception e) {
            LOGGER.error("Create pdf error", e);
            if (count < limit) {
                destroy();
                init();
                createPdf(wrapper, to, count + 1, limit);
            } else {
                throw new IndigoRuntimeException("Error creating pdf from HTML",e);
            }
        }
    }

    private FileAlterationListenerAdaptor createFileListener(final String filePath, final AtomicBoolean isDone) {
        return new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                LOGGER.info("Done file was created: {}", file.getPath());
                if (file.getPath().equals(filePath)) {
                    LOGGER.info("File listener successfully detect done file: {}", file.getPath());
                    isDone.set(true);
                }
            }
        };
    }
}
