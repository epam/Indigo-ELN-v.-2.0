package com.epam.indigoeln.core.service.print;

public class HtmlWrapper {
    private String html;
    private String header;
    private String fileName;
    private double headerHeight;

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getHeaderHeight() {
        return headerHeight;
    }

    public void setHeaderHeight(double headerHeight) {
        this.headerHeight = headerHeight;
    }
}
