package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.util.ModelUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.epam.indigoeln.test.FeignUtil.OBJECT_MAPPER;

class PatchUtilTest {

    @Test
    void testFormatDiff() throws Exception {
        JsonNode before = OBJECT_MAPPER.readTree(ModelUtil.loadResource(getClass(), "/visual-diff-before.json"));
        JsonNode patch = OBJECT_MAPPER.readTree(ModelUtil.loadResource(getClass(), "/visual-diff-patch.json"));
        String html = PatchUtil.formatJSONDiff(before, patch, rxn -> "(RXN schema)", compoundID -> "(compound ID)");
        html = """
                <!doctype html>
                <html>
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <meta http-equiv="Content-Security-Policy" content="img-src file: 'self' 'unsafe-inline';" />
                    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
                    <style>
                        .key { font-weight: bold; }
                        .old { background: pink; }
                        .new { background: lawngreen; }
                        .comment { font-weight: normal; font-style: italic; }
                    </style>
                </head>
                <body>
                """ + html;
        Files.writeString(Paths.get("build/visual-diff.html"), html);
    }
}
