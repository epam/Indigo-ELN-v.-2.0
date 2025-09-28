package com.epam.indigoeln.eln;

import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.test.BaseTest;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

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

    public static final String WILLOW_USERNAME = "willow";
    public static final String WILLOW_FIRST_NAME = "Willow";
    public static final String WILLOW_LAST_NAME = "Johnson";
    public static final String WILLOW_DISPLAY_NAME = "Willow Johnson";
    public static final List<RoleRef> WILLOW_ROLES = List.of();

    public static final String BART_USERNAME = "bart";
    public static final String BART_FIRST_NAME = "Bart";
    public static final String BART_LAST_NAME = "Brown";
    public static final String BART_DISPLAY_NAME = "Bart Brown";
    public static final List<RoleRef> BART_ROLES = List.of(ROLE_CONTENT_EDITOR);

    public static final String LISA_USERNAME = "lisa";
    public static final String LISA_FIRST_NAME = "Lisa";
    public static final String LISA_LAST_NAME = "Green";
    public static final String LISA_DISPLAY_NAME = "Lisa Green";
    public static final List<RoleRef> LISA_ROLES = List.of(ROLE_TEMPLATE_EDITOR);

    public static final String MAGGIE_USERNAME = "maggie";
    public static final String MAGGIE_FIRST_NAME = "Maggie";
    public static final String MAGGIE_LAST_NAME = "Green";
    public static final String MAGGIE_DISPLAY_NAME = "Maggie Green";
    public static final List<RoleRef> MAGGIE_ROLES = List.of(ROLE_PROJECT_CREATOR);

    protected ProjectClient projectClient;
    protected NotebookClient notebookClient;
    protected ExperimentClient experimentClient;
    protected TemplateClient templateClient;
    protected SignatureClient signatureClient;
    protected CompoundClient compoundClient;
    protected MiscClient miscClient;
    protected TestSupportClient testSupportClient;
    protected UserClient userClient;
    protected DictionaryClient dictionaryClient;
    protected RoleClient roleClient;
    protected GlobalSearchClient globalSearchClient;

    private int lastUsedNotebookNumber = 0;

    protected UUID johnUserID;
    protected UUID willowUserID;
    protected UUID bartUserID;
    protected UUID lisaUserID;
    protected UUID maggieUserID;
    protected UUID emptyTemplateID;

    @BeforeAll
    @Timeout(10)
    void setupAllBase() throws Exception {
        projectClient = buildClient(ProjectClient.class);
        notebookClient = buildClient(NotebookClient.class);
        experimentClient = buildClient(ExperimentClient.class);
        templateClient = buildClient(TemplateClient.class);
        signatureClient = buildClient(SignatureClient.class);
        compoundClient = buildClient(CompoundClient.class);
        miscClient = buildClient(MiscClient.class);
        userClient = buildClient(UserClient.class);
        dictionaryClient = buildClient(DictionaryClient.class);
        roleClient = buildClient(RoleClient.class);
        testSupportClient = buildClient(TestSupportClient.class);
        globalSearchClient = buildClient(GlobalSearchClient.class);
        assertThat(miscClient.getInfo()).contains(entry("application", "Indigo ELN"));
        cleanupDatabase();
    }

    protected String nextNotebookName() {
        return "%08d".formatted(++lastUsedNotebookNumber);
    }

    protected void cleanupDatabase() {
        testSupportClient.cleanupDatabase();
        miscClient.migrate();
        createBasicTestData();
    }

    private void createBasicTestData() {
        johnUserID = getOrCreateUser(new UserRequest(ELNBaseTest.JOHN_USERNAME, JOHN_FIRST_NAME, JOHN_LAST_NAME, "password", JOHN_ROLES)).getId();
        willowUserID = getOrCreateUser(new UserRequest(WILLOW_USERNAME, WILLOW_FIRST_NAME, WILLOW_LAST_NAME, "password", WILLOW_ROLES)).getId();
        bartUserID = getOrCreateUser(new UserRequest(BART_USERNAME, BART_FIRST_NAME, BART_LAST_NAME, "password", BART_ROLES)).getId();
        lisaUserID = getOrCreateUser(new UserRequest(LISA_USERNAME, LISA_FIRST_NAME, LISA_LAST_NAME, "password", LISA_ROLES)).getId();
        maggieUserID = getOrCreateUser(new UserRequest(MAGGIE_USERNAME, MAGGIE_FIRST_NAME, MAGGIE_LAST_NAME, "password", MAGGIE_ROLES)).getId();
        emptyTemplateID = templateClient.createTemplate(new TemplateRequest("Empty template", List.of(new TemplateTab("tabName", List.of(new TemplateComponent.Attachments()))))).getId();
    }

    public UserRef getJohnUserRef() {
        return new UserRef(johnUserID, ELNBaseTest.JOHN_USERNAME, JOHN_DISPLAY_NAME);
    }

    public UserRef getWillowUserRef() {
        return new UserRef(willowUserID, WILLOW_USERNAME, WILLOW_DISPLAY_NAME);
    }

    public UserRef getBartUserRef() {
        return new UserRef(bartUserID, BART_USERNAME, BART_DISPLAY_NAME);
    }

    public UserRef getLisaUserRef() {
        return new UserRef(lisaUserID, LISA_USERNAME, LISA_DISPLAY_NAME);
    }

    public UserRef getMaggieUserRef() {
        return new UserRef(maggieUserID, MAGGIE_USERNAME, MAGGIE_DISPLAY_NAME);
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
}
