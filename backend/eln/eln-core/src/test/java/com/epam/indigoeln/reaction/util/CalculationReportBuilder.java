package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.util.PatchUtil;
import com.epam.indigoeln.eln.util.ToStringUtil;
import com.epam.indigoeln.reaction.metamodel.ExperimentMetamodel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.test.FeignUtil;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CalculationReportBuilder implements AutoCloseable {

    private final File file;
    private final ByteArrayOutputStream bytes;
    private final PrintWriter pr;
    @Nullable
    private String previousModel;
    private boolean closed;

    private final DiffRowGenerator generator = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .oldTag(x -> x ? "<span class='old'>" : "</span>")
            .newTag(x -> x ? "<span class='new'>" : "</span>")
            .build();

    @SneakyThrows
    public CalculationReportBuilder(File file) {
        this.file = file;
        System.err.println("Calculation report will be written to " + file.getAbsolutePath());
        bytes = new ByteArrayOutputStream();
        pr = new PrintWriter(bytes, false, StandardCharsets.UTF_8);
        pr.println("""
                <!DOCTYPE html>
                <html>
                    <head>
                        <meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
                        <style>
                            body { font-family: Arial, sans-serif; }
                            .diff-wrapper { display: flex; width: 100%; }
                            .diff-pane { flex: 0 0 33%; overflow-x: auto; border: 1px solid #ccc; }
                            .diff { width: 100%; border-collapse: collapse; }
                            .diff td { white-space: nowrap; padding: 0; text-align: left, vertical-align: top; }
                            pre { margin: 0; }
                            h1 { margin-top: 64pt; }
                            h1.error { color: darkred; }
                            .diff-old { background-color: #f8d7da; }
                            .diff-new { background-color: #d4edda; }
                        </style>
                    </head>
                <body>""");
    }

    @Override
    @SneakyThrows
    public void close() {
        if (!closed) {
            pr.println("</html>");
            pr.close();
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                outputStream.write(bytes.toByteArray());
            }
            closed = true;
            System.err.println("Calculation report is available at file://wsl$/Ubuntu" + file.getAbsolutePath());
        }
    }

    public void addMutation(Mutation mutation) {
        pr.printf("<h1>%s</h1>\n", mutation);
    }

    public void addModel(String patch, ExperimentSnapshot model) {
        String currentModel = ToStringUtil.toStringBuild(ExperimentMetamodel.INSTANCE, model);
        if (previousModel == null) {
            addComparison(formatPatch(patch), List.of(), currentModel.lines().toList());
        } else {
            Pair<List<String>, List<String>> result = prepareDiff(previousModel, currentModel);
            addComparison(formatPatch(patch), result.a(), result.b());
        }
        previousModel = currentModel;
    }

    public void addPicture(byte[] content, String contentType) {
        pr.printf("<img src='data:%s;base64,%s'/>", contentType, Base64.getEncoder().encodeToString(content));
    }

    public void addFailedComparison(String summary, @Nullable String patch, String expected, String applied) {
        pr.printf("<h1 class='error'>%s</h1>\n", summary);
        Pair<List<String>, List<String>> result = prepareDiff(expected, applied);
        addComparison(patch != null ? formatPatch(patch) : List.of(), result.a(), result.b());
    }

    private Pair<List<String>, List<String>> prepareDiff(String left, String right) {
        List<DiffRow> rows = generator.generateDiffRows(left.lines().toList(), right.lines().toList());
        List<String> leftContent = new ArrayList<>(), rightContent = new ArrayList<>();
        for (DiffRow row : rows) {
            leftContent.add(row.getOldLine());
            rightContent.add(row.getNewLine());
        }
        return Pair.of(leftContent, rightContent);
    }

    @SneakyThrows
    private List<String> formatPatch(String patch) {
        return PatchUtil.formatJSONDiff(FeignUtil.OBJECT_MAPPER.readTree(patch))
                .lines().toList();
    }

    private void addComparison(List<String> leftContent, List<String> middleContent, List<String> rightContent) {
        pr.println("""
                <div class='diff-wrapper'>
                """);
        for (List<String> content : List.of(leftContent, middleContent, rightContent)) {
            pr.println("""
                    <div class='diff-pane'>
                        <table class='diff-table'>
                    """);
            for (String line : content) {
                pr.printf("<tr><td><pre>%s</pre></td></tr>\n", line.isBlank() ? "&nbsp;" : line);
            }
            pr.println("""
                        </table>
                    </div>
                    """);
        }
        pr.println("""
                </div>
                """);
    }
}
