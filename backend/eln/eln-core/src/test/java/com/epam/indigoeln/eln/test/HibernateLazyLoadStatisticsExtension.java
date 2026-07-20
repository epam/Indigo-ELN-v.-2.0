package com.epam.indigoeln.eln.test;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import org.hibernate.SessionFactory;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.Statistics;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HibernateLazyLoadStatisticsExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final String NAME = "hibernateLazyLoadStatisticsExtension";

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).computeIfAbsent(NAME, key -> new ResourceImpl(), Object.class);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        BeforeEachCallback impl = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).get(NAME, BeforeEachCallback.class);
        impl.beforeEach(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        AfterEachCallback impl = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).get(NAME, AfterEachCallback.class);
        impl.afterEach(context);
    }

    class ResourceImpl implements BeforeEachCallback, AfterEachCallback, AutoCloseable {

        private final PrintWriter writer;

        @SneakyThrows
        ResourceImpl() {
            writer = new PrintWriter(Files.newBufferedWriter(Path.of("build", "hibernate-lazy-load.csv"), StandardCharsets.UTF_8));
            writer.print("test_class,test_method,collection_name,count\n");
        }

        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            SessionFactory sessionFactory = getSessionFactory();
            if (sessionFactory != null) {
                sessionFactory.getStatistics().clear();
            }
        }

        @Override
        public void afterEach(ExtensionContext context) throws Exception {
            SessionFactory sessionFactory = getSessionFactory();
            if (sessionFactory != null) {
                String className = context.getTestClass().map(x -> x.getName().replace("\"", "\"\"")).orElse(null);
                String methodName = context.getTestMethod().map(x -> x.getName().replace("\"", "\"\"")).orElse(null);
                Statistics stats = sessionFactory.getStatistics();
                for (String collection : stats.getCollectionRoleNames()) {
                    CollectionStatistics collectionStats = stats.getCollectionStatistics(collection);
                    long count = collectionStats.getFetchCount();
                    if (count != 0) {
                        writer.printf("\"%s\",\"%s\",\"%s\",%d\n", className, methodName, collection.replace("\"", "\"\""), count);
                    }
                }
            }
        }

        @Override
        public void close() throws Exception {
            writer.close();
        }

        @Nullable
        private SessionFactory getSessionFactory() {
            if (Arc.container() != null) {
                InstanceHandle<EntityManagerFactory> instance = Arc.container().instance(EntityManagerFactory.class);
                EntityManagerFactory emf = instance.get();
                if (emf != null) {
                    return emf.unwrap(SessionFactory.class);
                }
            }
            return null;
        }
    }
}
