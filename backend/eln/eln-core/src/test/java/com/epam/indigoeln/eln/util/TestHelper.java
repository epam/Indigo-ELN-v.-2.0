package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.eln.client.TemplateClient;
import com.epam.indigoeln.eln.client.TestSupportClient;
import com.epam.indigoeln.eln.client.UserClient;
import com.epam.indigoeln.eln.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class TestHelper {

    public static final RecursiveComparisonConfiguration COMPARE_WITHOUT_MODIFIED_AT = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields("modifiedAt")
            .build();

    public static final RoleRef ROLE_ADMINISTRATOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Administrator");
    public static final RoleRef ROLE_CONTENT_EDITOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Content Editor");
    public static final RoleRef ROLE_TEMPLATE_EDITOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Template Editor");
    public static final RoleRef ROLE_PROJECT_CREATOR = new RoleRef(UUID.fromString("00000000-0000-0000-0000-000000000005"), "Project Creator");

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_DISPLAY_NAME = "Administrator";

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

    private final UserClient userClient;
    private final TemplateClient templateClient;
    private final TestSupportClient testSupportClient;
    private final AtomicReference<String> currentUsername;

    @Getter
    private UUID johnUserID;
    @Getter
    private UUID willowUserID;
    @Getter
    private UUID bartUserID;
    @Getter
    private UUID lisaUserID;
    @Getter
    private UUID maggieUserID;
    @Getter
    private UUID emptyTemplateID;

    public void cleanupDatabase() {
        testSupportClient.cleanupDatabase();
        createBasicTestData();
    }

    private void createBasicTestData() {
        johnUserID = getOrCreateUser(new UserRequest(TestHelper.JOHN_USERNAME, TestHelper.JOHN_FIRST_NAME, TestHelper.JOHN_LAST_NAME, "password", TestHelper.JOHN_ROLES)).getId();
        willowUserID = getOrCreateUser(new UserRequest(TestHelper.WILLOW_USERNAME, TestHelper.WILLOW_FIRST_NAME, TestHelper.WILLOW_LAST_NAME, "password", TestHelper.WILLOW_ROLES)).getId();
        bartUserID = getOrCreateUser(new UserRequest(TestHelper.BART_USERNAME, TestHelper.BART_FIRST_NAME, TestHelper.BART_LAST_NAME, "password", TestHelper.BART_ROLES)).getId();
        lisaUserID = getOrCreateUser(new UserRequest(TestHelper.LISA_USERNAME, TestHelper.LISA_FIRST_NAME, TestHelper.LISA_LAST_NAME, "password", TestHelper.LISA_ROLES)).getId();
        maggieUserID = getOrCreateUser(new UserRequest(TestHelper.MAGGIE_USERNAME, TestHelper.MAGGIE_FIRST_NAME, TestHelper.MAGGIE_LAST_NAME, "password", TestHelper.MAGGIE_ROLES)).getId();
        emptyTemplateID = templateClient.createTemplate(new TemplateRequest("Empty template", List.of(new TemplateComponent.Attachments()))).getId();
    }

    public UserRef getJohnUserRef() {
        return new UserRef(johnUserID, JOHN_USERNAME, JOHN_DISPLAY_NAME);
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
        String oldUsername = currentUsername.get();
        try {
            currentUsername.set(ADMIN_USERNAME);
            Page<UserDTO> found = userClient.getUsers(null, request.getUsername(), Paging.DEFAULT);
            if (!found.getItems().isEmpty()) {
                return found.getItems().getFirst();
            }
            return userClient.createUser(request);
        } finally {
            currentUsername.set(oldUsername);
        }
    }
}
