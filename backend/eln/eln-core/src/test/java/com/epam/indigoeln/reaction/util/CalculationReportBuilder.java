package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.util.ModelUtil;
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
import java.util.stream.Collectors;

public class CalculationReportBuilder implements AutoCloseable {

    private static final DiffRowGenerator GENERATOR = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .oldTag(x -> x ? "<span class='old'>" : "</span>")
            .newTag(x -> x ? "<span class='new'>" : "</span>")
            .build();
    private static final String REPORT_HEADER = loadReportHeader();

    private final File file;
    private final ByteArrayOutputStream bytes;
    private final PrintWriter pr;
    @Nullable
    private String previousModel;
    private boolean closed;
    private int ordinal;

    @SneakyThrows
    private static String loadReportHeader() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ModelUtil.loadResourceAsStream(CalculationReportBuilder.class, "/com/epam/indigoeln/reaction/util/report-header.html")))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    @SneakyThrows
    public CalculationReportBuilder(File file) {
        this.file = file;
        System.err.println("Calculation report will be written to " + file.getAbsolutePath());
        bytes = new ByteArrayOutputStream();
        pr = new PrintWriter(bytes, false, StandardCharsets.UTF_8);
        pr.println(REPORT_HEADER);
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

    public void addMutation(String reportClass, Mutation mutation) {
        pr.printf("<h1 id='section%s' class='%s'>%s</h1>\n", ++ordinal, reportClass, mutation);
    }

    public void addModel(String reportClass, String patch, ExperimentSnapshot model) {
        String currentModel = ToStringUtil.toStringBuild(ExperimentMetamodel.INSTANCE, model);
        if (previousModel == null) {
            addComparison(reportClass, formatPatch(patch), List.of(), currentModel.lines().toList());
        } else {
            Pair<List<String>, List<String>> result = prepareDiff(previousModel, currentModel);
            addComparison(reportClass, formatPatch(patch), result.a(), result.b());
        }
        previousModel = currentModel;
    }

    public void addPicture(String reportClass, byte[] content, String contentType) {
        pr.printf("<img class='%s' src='data:%s;base64,%s'/>", reportClass, contentType, Base64.getEncoder().encodeToString(content));
    }

    public void addHeader(String reportClass, String header) {
        pr.printf("<h1 id='section%s' class='%s'>%s</h1>\n", ++ordinal, reportClass, header);
    }

    public void addMessage(String reportClass, String message) {
        pr.printf("<div class='%s'>%s</h1>\n", reportClass, message);
    }

    public void addFailedComparison(String reportClass, String summary, @Nullable String patch, String expected, String applied) {
        pr.printf("<h1 id='section%s' class='error %s'>%s</h1>\n", ++ordinal, reportClass, summary);
        Pair<List<String>, List<String>> result = prepareDiff(expected, applied);
        addComparison(reportClass, patch != null ? formatPatch(patch) : List.of(), result.a(), result.b());
    }

    private Pair<List<String>, List<String>> prepareDiff(String left, String right) {
        List<DiffRow> rows = GENERATOR.generateDiffRows(left.lines().toList(), right.lines().toList());
        List<String> leftContent = new ArrayList<>(), rightContent = new ArrayList<>();
        for (DiffRow row : rows) {
            leftContent.add(row.getOldLine());
            rightContent.add(row.getNewLine());
        }
        return Pair.of(leftContent, rightContent);
    }

    @SneakyThrows
    private List<String> formatPatch(String patch) {
        return PatchUtil.formatJSONDiff(FeignUtil.OBJECT_MAPPER.readTree(patch)).lines().toList();
    }

    private void addComparison(String reportClass, List<String> leftContent, List<String> middleContent, List<String> rightContent) {
        pr.printf("<div class='diff-wrapper %s'>\n", reportClass);
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
