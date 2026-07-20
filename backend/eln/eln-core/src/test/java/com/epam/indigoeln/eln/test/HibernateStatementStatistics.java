package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.common.util.Pair;
import lombok.SneakyThrows;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Logs the application call site whenever SQL touches a watched join table, to pinpoint where a lazy collection gets loaded. */
public class HibernateStatementStatistics implements StatementInspector {

    // list of SQL fragments (lowercase) to watch
    private static final List<Pattern> WATCHED_FRAGMENTS = List.of(
//            Pattern.compile("from attachment")
    );

    private final Map<Pair<String, String>, Long> map = new ConcurrentHashMap<>();

    public HibernateStatementStatistics() {
        Runtime.getRuntime().addShutdownHook(new Thread(HibernateStatementStatistics.this::close));
    }

    @Override
    public String inspect(String sql) {
        if (sql == null) {
            return "";
        }
        String sqlLower = sql.toLowerCase();
        for (Pattern fragment : WATCHED_FRAGMENTS) {
            if (fragment.matcher(sqlLower).find()) {
                String trace = Arrays.stream(Thread.currentThread().getStackTrace())
                        .filter(e -> e.getClassName().startsWith("com.epam."))
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n    at "));
                map.compute(Pair.of(sqlLower, trace), (_, v) -> v != null ? (v + 1) : 1);
                break;
            }
        }
        return sql;
    }

    @SneakyThrows
    public void close() {
        PrintWriter writer = new PrintWriter("build/hibernate-sql-trace.csv");
        writer.print("sql,stack_trace,count\n");
        map.forEach((k, v) -> {
            writer.printf("\"%s\",\"%s\",%d\n", escapeCSV(k.a()), escapeCSV(k.b()), v.intValue());
        });
        writer.close();
    }

    private static String escapeCSV(@Nullable String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\"\"").replace("\n", "\\n").replace("\t", "\\t");
    }
}
