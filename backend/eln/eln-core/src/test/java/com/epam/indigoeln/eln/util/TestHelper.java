package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.eln.client.MiscClient;
import com.epam.indigoeln.eln.client.UsersClient;
import com.epam.indigoeln.eln.model.ApplicationRole;
import com.epam.indigoeln.eln.model.UserRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

import java.util.UUID;

@RequiredArgsConstructor
public class TestHelper {

    public static final RecursiveComparisonConfiguration COMPARE_WITHOUT_MODIFIED_AT = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields("modifiedAt")
            .build();

    public static final String JOHN_USERNAME = "john";
    public static final String JOHN_FIRST_NAME = "John";
    public static final String JOHN_LAST_NAME = "Doe";
    public static final String JOHN_DISPLAY_NAME = "John Doe";
    public static final ApplicationRole[] JOHN_ROLES = {ApplicationRole.CONTENT_EDITOR, ApplicationRole.TEMPLATE_EDITOR, ApplicationRole.ADMINISTRATOR};

    public static final String WILLOW_USERNAME = "willow";
    public static final String WILLOW_FIRST_NAME = "Willow";
    public static final String WILLOW_LAST_NAME = "Johnson";
    public static final String WILLOW_DISPLAY_NAME = "Willow Johnson";
    public static final ApplicationRole[] WILLOW_ROLES = {};

    public static final String BART_USERNAME = "bart";
    public static final String BART_FIRST_NAME = "Bart";
    public static final String BART_LAST_NAME = "Brown";
    public static final String BART_DISPLAY_NAME = "Bart Brown";
    public static final ApplicationRole[] BART_ROLES = {ApplicationRole.CONTENT_EDITOR};

    public static final String LISA_USERNAME = "lisa";
    public static final String LISA_FIRST_NAME = "Lisa";
    public static final String LISA_LAST_NAME = "Green";
    public static final String LISA_DISPLAY_NAME = "Lisa Green";
    public static final ApplicationRole[] LISA_ROLES = {ApplicationRole.TEMPLATE_EDITOR};

    private final UsersClient usersClient;
    private final MiscClient miscClient;

    @Getter
    private UUID johnUserID;
    @Getter
    private UUID willowUserID;
    @Getter
    private UUID bartUserID;
    @Getter
    private UUID lisaUserID;

    public void cleanupDatabase() {
        miscClient.cleanupDatabase();
    }

    public void createTestUsers() {
        johnUserID = usersClient.createUser(new UserRequest(TestHelper.JOHN_USERNAME, TestHelper.JOHN_FIRST_NAME, TestHelper.JOHN_LAST_NAME, TestHelper.JOHN_ROLES)).getId();
        willowUserID = usersClient.createUser(new UserRequest(TestHelper.WILLOW_USERNAME, TestHelper.WILLOW_FIRST_NAME, TestHelper.WILLOW_LAST_NAME, TestHelper.WILLOW_ROLES)).getId();
        bartUserID = usersClient.createUser(new UserRequest(TestHelper.BART_USERNAME, TestHelper.BART_FIRST_NAME, TestHelper.BART_LAST_NAME, TestHelper.BART_ROLES)).getId();
        lisaUserID = usersClient.createUser(new UserRequest(TestHelper.LISA_USERNAME, TestHelper.LISA_FIRST_NAME, TestHelper.LISA_LAST_NAME, TestHelper.LISA_ROLES)).getId();
    }
}
