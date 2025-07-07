package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CalculationReportBuilder implements AutoCloseable {

    private final File file;
    private final PrintWriter pr;
    @Nullable
    private List<String> previousModel;

    private final DiffRowGenerator generator = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .oldTag(x -> x ? "<span class='old'>" : "</span>")
            .newTag(x -> x ? "<span class='new'>" : "</span>")
            .build();

    @SneakyThrows
    public CalculationReportBuilder(File file) {
        this.file = file;
        System.out.println("Calculation report will be written to " + file.getAbsolutePath());
        pr = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));
        pr.println("""
                <!DOCTYPE html>
                <html>
                    <head>
                        <style>
                            body { font-family: Arial, sans-serif; }
                            .diff-wrapper { display: flex; width: 100%; }
                            .diff-pane { flex: 0 0 50%; overflow-x: auto; border: 1px solid #ccc; }
                            .diff { width: 100%; border-collapse: collapse; }
                            .diff td { white-space: nowrap; padding: 0; text-align: left, vertical-align: top; }
                            pre { margin: 0; }
                            h1 { margin-top: 64pt; }
                            .old { background-color: #f8d7da; }
                            .new { background-color: #d4edda; }
                        </style>
                    </head>
                <body>""");
    }

    @Override
    public void close() {
        pr.println("</html>");
        pr.close();
    }

    public void addMutation(Mutation mutation) {
        pr.printf("<h1>%s</h1>\n", mutation);
    }

    public void addModel(ExperimentModel model) {
        List<String> currentModel = model.toStringTree().lines().toList();
        List<String> leftContent = new ArrayList<>(), rightContent = new ArrayList<>();
        if (previousModel == null) {
            rightContent = currentModel;
        } else {
            List<DiffRow> rows = generator.generateDiffRows(previousModel, currentModel);
            for (DiffRow row : rows) {
                leftContent.add(row.getOldLine());
                rightContent.add(row.getNewLine());
            }
        }
        pr.println("""
                <div class='diff-wrapper'>
                    <div class='diff-pane'>
                        <table class='diff-table'>
                """);
        for (String line : leftContent) {
            pr.printf("<tr><td><pre>%s</pre></td></tr>\n", line.isBlank() ? "&nbsp;" : line);
        }
        pr.println("""
                        </table>
                    </div>
                    <div class='diff-pane'>
                        <table class='diff-table'>
                """);
        for (String line : rightContent) {
            pr.printf("<tr><td><pre>%s</pre></td></tr>\n", line.isBlank() ? "&nbsp;" : line);
        }
        pr.println("""
                        </table>
                    </div>
                </div>
                """);
        previousModel = currentModel;
    }

    public void addPicture(byte[] content, String contentType) {
        pr.printf("<img src='data:%s;base64,%s'/>", contentType, Base64.getEncoder().encodeToString(content));
    }
}
