package com.epam.indigoeln.eln;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.api.ELNInternalClient;
import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.util.ExperimentObject;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.epam.indigoeln.signature.api.SignatureAdminClient;
import com.epam.indigoeln.signature.api.SignatureClient;
import com.epam.indigoeln.test.BaseTest;
import com.google.common.base.Suppliers;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import lombok.SneakyThrows;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public abstract class ELNBaseTest extends BaseTest {

    public static final RecursiveComparisonConfiguration COMPARE_WITHOUT_MODIFIED_AT = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields("modifiedAt")
            .build();

    public static final RoleRef ROLE_ADMINISTRATOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Administrator");
    public static final RoleRef ROLE_CONTENT_EDITOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Content Editor");
    public static final RoleRef ROLE_TEMPLATE_EDITOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Template Editor");
    public static final RoleRef ROLE_PROJECT_CREATOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000005"), "Project Creator");

    public static final String JOHN_USERNAME = "john";
    public static final String JOHN_FIRST_NAME = "John";
    public static final String JOHN_LAST_NAME = "Doe";
    public static final String JOHN_DISPLAY_NAME = "John Doe";
    public static final List<RoleRef> JOHN_ROLES = List.of(ROLE_CONTENT_EDITOR, ROLE_TEMPLATE_EDITOR, ROLE_ADMINISTRATOR);
    public static final UserRef JOHN_USER_REF = new UserRef(JOHN_USERNAME, JOHN_DISPLAY_NAME);

    public static final String WILLOW_USERNAME = "willow";
    public static final String WILLOW_FIRST_NAME = "Willow";
    public static final String WILLOW_LAST_NAME = "Johnson";
    public static final String WILLOW_DISPLAY_NAME = "Willow Johnson";
    public static final List<RoleRef> WILLOW_ROLES = List.of();
    public static final UserRef WILLOW_USER_REF = new UserRef(WILLOW_USERNAME, WILLOW_DISPLAY_NAME);

    public static final String BART_USERNAME = "bart";
    public static final String BART_FIRST_NAME = "Bart";
    public static final String BART_LAST_NAME = "Brown";
    public static final String BART_DISPLAY_NAME = "Bart Brown";
    public static final List<RoleRef> BART_ROLES = List.of(ROLE_CONTENT_EDITOR);
    public static final UserRef BART_USER_REF = new UserRef(BART_USERNAME, BART_DISPLAY_NAME);

    public static final String LISA_USERNAME = "lisa";
    public static final String LISA_FIRST_NAME = "Lisa";
    public static final String LISA_LAST_NAME = "Green";
    public static final String LISA_DISPLAY_NAME = "Lisa Green";
    public static final List<RoleRef> LISA_ROLES = List.of(ROLE_TEMPLATE_EDITOR);
    public static final UserRef LISA_USER_REF = new UserRef(LISA_USERNAME, LISA_DISPLAY_NAME);

    public static final String MAGGIE_USERNAME = "maggie";
    public static final String MAGGIE_FIRST_NAME = "Maggie";
    public static final String MAGGIE_LAST_NAME = "Green";
    public static final String MAGGIE_DISPLAY_NAME = "Maggie Green";
    public static final List<RoleRef> MAGGIE_ROLES = List.of(ROLE_PROJECT_CREATOR);
    public static final UserRef MAGGIE_USER_REF = new UserRef(MAGGIE_USERNAME, MAGGIE_DISPLAY_NAME);

    protected ProjectClient projectClient;
    protected NotebookClient notebookClient;
    protected ExperimentClient experimentClient;
    protected TemplateClient templateClient;
    protected CompoundClient compoundClient;
    protected MiscClient miscClient;
    protected UserClient userClient;
    protected DictionaryClient dictionaryClient;
    protected RoleClient roleClient;
    protected GlobalSearchClient globalSearchClient;
    protected ELNInternalClient elnInternalClient;

    protected ReportsClient reportsClient;
    protected SignatureClient signatureClient;

    private final static Supplier<AgroalDataSource> databasePool = Suppliers.memoize(ELNBaseTest::createDatabasePool);
    private final AtomicInteger lastUsedNotebookNumber = new AtomicInteger();

    protected UUID johnUserID;
    protected UUID willowUserID;
    protected UUID bartUserID;
    protected UUID lisaUserID;
    protected UUID maggieUserID;
    protected UUID emptyTemplateID;

    @BeforeAll
    void setupAllBase() {
        projectClient = buildClient(ProjectClient.class);
        notebookClient = buildClient(NotebookClient.class);
        experimentClient = buildClient(ExperimentClient.class);
        templateClient = buildClient(TemplateClient.class);
        compoundClient = buildClient(CompoundClient.class);
        miscClient = buildClient(MiscClient.class);
        userClient = buildClient(UserClient.class);
        dictionaryClient = buildClient(DictionaryClient.class);
        roleClient = buildClient(RoleClient.class);
        globalSearchClient = buildClient(GlobalSearchClient.class);
        elnInternalClient = buildClient(ELNInternalClient.class);
        reportsClient = buildClient(ReportsClient.class);
        signatureClient = buildClient(SignatureClient.class);
        if (integrationTest) {
            SignatureAdminClient signatureAdminClient;
            signatureAdminClient = buildClient(SignatureAdminClient.class);
            signatureAdminClient.migrate();
        }
        miscClient.migrate();
        createBasicTestData();
        cleanupDatabase();
    }

    protected ProjectDetailsDTO getOrCreateProject(String projectName) {
        Page<ProjectDTO> existingProjects = projectClient.getProjects(projectName, SortOrder.EARLIEST, null, Paging.DEFAULT);
        return existingProjects.getItems().isEmpty()
                ? projectClient.createProject(new ProjectRequest(projectName))
                : projectClient.getProject(existingProjects.getItems().getFirst().getId());
    }

    protected ExperimentObject createExperiment(NotebookDetailsDTO notebook, ExperimentRequest request) {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), request);
        return new ExperimentObject(experiment, experimentClient, compoundClient, miscClient);
    }

    protected String nextNotebookName() {
        return "%08d".formatted(lastUsedNotebookNumber.incrementAndGet());
    }

    @SuppressWarnings("SqlWithoutWhere")
    protected void cleanupDatabase() {
        try (Connection connection = databasePool.get().getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                // experiments, notebooks, projects
                statement.executeUpdate("delete from Attachment");
                statement.executeUpdate("delete from Experiment_Revision");
                statement.executeUpdate("delete from Experiment");
                statement.executeUpdate("delete from Notebook_Revision");
                statement.executeUpdate("delete from Notebook");
                statement.executeUpdate("delete from Project_Revision");
                statement.executeUpdate("delete from Project");
                statement.executeUpdate("delete from Template where name != 'Default'");
                // samples, compounds
                statement.executeUpdate("delete from Sample");
                statement.executeUpdate("delete from Compound");
                statement.executeUpdate("alter sequence compound_str_code_compound_seq restart");
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean up test database", e);
        }
    }

    private void createBasicTestData() {
        johnUserID = getOrCreateUser(new UserRequest(ELNBaseTest.JOHN_USERNAME, JOHN_FIRST_NAME, JOHN_LAST_NAME, "password", JOHN_ROLES)).getId();
        willowUserID = getOrCreateUser(new UserRequest(WILLOW_USERNAME, WILLOW_FIRST_NAME, WILLOW_LAST_NAME, "password", WILLOW_ROLES)).getId();
        bartUserID = getOrCreateUser(new UserRequest(BART_USERNAME, BART_FIRST_NAME, BART_LAST_NAME, "password", BART_ROLES)).getId();
        lisaUserID = getOrCreateUser(new UserRequest(LISA_USERNAME, LISA_FIRST_NAME, LISA_LAST_NAME, "password", LISA_ROLES)).getId();
        maggieUserID = getOrCreateUser(new UserRequest(MAGGIE_USERNAME, MAGGIE_FIRST_NAME, MAGGIE_LAST_NAME, "password", MAGGIE_ROLES)).getId();
        emptyTemplateID = templateClient.getByName("Default").getId();
    }

    private UserDTO getOrCreateUser(UserRequest request) {
        String oldUsername = username.get();
        try {
            username.set(ADMIN_USERNAME);
            Page<UserDTO> found = userClient.getUsers(null, request.getUsername(), Paging.DEFAULT);
            if (!found.getItems().isEmpty()) {
                return found.getItems().getFirst();
            }
            return userClient.createUser(request);
        } finally {
            username.set(oldUsername);
        }
    }

    @SneakyThrows
    private static AgroalDataSource createDatabasePool() {
        String jdbcUrl = integrationTest ? System.getProperty("eln.test.datasource.jdbc-url") : ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String.class);
        String username = integrationTest ? System.getProperty("eln.test.datasource.username") : ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class);
        String password = integrationTest ? System.getProperty("eln.test.datasource.password") : ConfigProvider.getConfig().getValue("quarkus.datasource.password", String.class);
        return AgroalDataSource.from(new io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier()
                .connectionPoolConfiguration(cp -> cp
                        .minSize(0)
                        .maxSize(2)
                        .connectionFactoryConfiguration(cf -> cf
                                .jdbcUrl(jdbcUrl)
                                .principal(new NamePrincipal(username))
                                .credential(new SimplePassword(password)))));
    }
}
