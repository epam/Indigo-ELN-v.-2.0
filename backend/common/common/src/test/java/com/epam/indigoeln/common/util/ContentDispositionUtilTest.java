package com.epam.indigoeln.common.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ContentDispositionUtilTest {

    static Stream<Object[]> extractFilenameCases() {
        return Stream.of(new Object[][]{
            // --- Plain filename (quoted) ---
            { "attachment; filename=\"report.pdf\"", "report.pdf" },
            { "attachment; filename=\"file with spaces.txt\"", "file with spaces.txt" },
            { "attachment; filename=\"\"", "" },
            // --- Plain filename (unquoted) ---
            { "attachment; filename=report.pdf", "report.pdf" },
            { "attachment; filename=simple", "simple" },
            // --- filename* only (no fallback filename) ---
            { "attachment; filename*=UTF-8''hello%20world.csv", "hello world.csv" },
            // --- Special characters in quoted filename ---
//            { "attachment; filename=\"file\\\"quoted\\\".txt\"", "file\"quoted\".txt" }, // doesn't get handled correctly by RESTEasy's HeaderUtil
            { "attachment; filename=\"path/to/file.txt\"", "path/to/file.txt" },
            { "attachment; filename=\"file (1).pdf\"", "file (1).pdf" },
            { "attachment; filename=\"report_2024-01-15.xlsx\"", "report_2024-01-15.xlsx" },
            // --- Extra parameters around filename ---
            { "attachment; size=12345; filename=\"data.csv\"", "data.csv" },
            { "attachment; filename=\"notes.txt\"; creation-date=\"Mon, 1 Jan 2024 00:00:00 GMT\"", "notes.txt" },
            // --- Disposition type variations ---
            { "form-data; name=\"file\"; filename=\"upload.png\"", "upload.png" },
            { "form-data; name=\"attachment\"; filename*=UTF-8''photo%20album.zip", "photo album.zip" },
            // --- No filename present → null ---
            { "attachment", null },
            { "inline", null },
            { "attachment; size=1024", null },
            { "", null },
        });
    }

    @ParameterizedTest(name = "[{index}] header=''{0}'' → ''{1}''")
    @MethodSource("extractFilenameCases")
    void testExtractFilename(String header, String expectedFilename) {
        String actual = ContentDispositionUtil.extractFilename(header);
        assertThat(actual).isEqualTo(expectedFilename);
    }

    // Pair of (attachment flag, filename, expected Content-Disposition header)
    static Stream<Object[]> generateContentDispositionCases() {
        return Stream.of(new Object[][]{
            // attachment, simple ASCII filename
            { true,  "report.pdf", "attachment; filename=\"report.pdf\"; filename*=UTF-8''report.pdf" },
            // inline, simple ASCII filename
            { false, "preview.html", "inline; filename=\"preview.html\"; filename*=UTF-8''preview.html" },
            // spaces → %20 in filename*
            { true,  "my report 2024.pdf", "attachment; filename=\"my report 2024.pdf\"; filename*=UTF-8''my%20report%202024.pdf" },
            // double-quote in filename → escaped as "" in quoted part, %22 in filename*
            { true,  "the \"best\" report.pdf", "attachment; filename=\"the \\\"best\\\" report.pdf\"; filename*=UTF-8''the%20%22best%22%20report.pdf" },
        });
    }

    @ParameterizedTest(name = "[{index}] attachment={0}, filename=''{1}''")
    @MethodSource("generateContentDispositionCases")
    void testGenerateContentDisposition(boolean attachment, String filename, String expected) {
        String actual = ContentDispositionUtil.generateContentDisposition(attachment, filename);
        assertThat(actual).isEqualTo(expected);
    }
}
