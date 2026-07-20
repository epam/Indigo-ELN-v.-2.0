package com.epam.indigoeln.eln.test;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Logs the application call site whenever SQL touches a watched join table, to pinpoint where a lazy collection gets loaded. */
public class RolesLoadStatementInspector implements StatementInspector {

    private static final Logger LOG = Logger.getLogger(RolesLoadStatementInspector.class);

    private static final List<String> WATCHED_TABLES = List.of(
            "user_account_application_role",
            "project_attachment",
            "notebook_attachment",
            "experiment_attachment"
    );

    @Override
    public String inspect(String sql) {
        if (sql == null) {
            return sql;
        }
        String lower = sql.toLowerCase();
        for (String table : WATCHED_TABLES) {
            if (lower.contains(table) && !lower.contains("join " + table)) {
                String trace = Arrays.stream(Thread.currentThread().getStackTrace())
                        .filter(e -> e.getClassName().startsWith("com.epam"))
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n    at "));
//                LOG.infof("LAZY_SQL[%s] call site:%n    at %s%nSQL: %s", table, trace, sql);
                break;
            }
        }
        return sql;
    }
}
