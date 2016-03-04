package com.epam.indigoeln.core.service.print;

import org.openqa.selenium.OutputType;

import java.net.URI;

public interface PhantomJsService {

    <T> T takesScreenshot(String html, OutputType<T> type);

    <T> T takesScreenshot(URI uri, OutputType<T> type);
}
