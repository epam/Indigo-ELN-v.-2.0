package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.PermissionCreationLevel;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.*;
import static com.epam.indigoeln.core.model.UserPermission.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionUtilTest {

    @Mock
    private User testUser;
    @Mock
    private User otherTestUser;
    private final static String TEST_USER_ID = "userId";
    private final static String OTHER_TEST_USER_ID = "otherUserId";


    @Before
    public void setUp() {
        when(testUser.getId()).thenReturn(TEST_USER_ID);
        when(otherTestUser.getId()).thenReturn(OTHER_TEST_USER_ID);

    }

    @Test
    public void hasPermissionsOnPermissionsContainsUser() {

        UserPermission userPermissionsContainsTestUser = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS);
        boolean actualResult = PermissionUtil.hasPermissions(TEST_USER_ID,
                Collections.singleton(userPermissionsContainsTestUser),
                UserPermission.READ_ENTITY);

        assertThat(actualResult, is(true));
    }

    @Test
    public void hasPermissionsOnPermissionsDoNotContainsUser() {

        UserPermission userPermissionsContainsTestUser = new UserPermission(otherTestUser, UserPermission.VIEWER_PERMISSIONS);
        boolean actualResult = PermissionUtil.hasPermissions(TEST_USER_ID,
                Collections.singleton(userPermissionsContainsTestUser),
                UserPermission.READ_ENTITY);

        assertThat(actualResult, is(false));
    }

    @Test
    public void addOwnerToEmptyAccessList() {
        Set<UserPermission> accessList = new HashSet<>(2);
        UserPermission expectedPermission = new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK);

        PermissionUtil.addOwnerToAccessList(accessList, testUser, NOTEBOOK);
        assertThat(accessList, contains(expectedPermission));
        assertThat(accessList.size(), is(1));
    }

    @Test
    public void addOwnerToAccessList() {
        Set<UserPermission> accessList = new HashSet<>(3);
        UserPermission presentedPermission = new UserPermission(otherTestUser, OWNER_PERMISSIONS, NOTEBOOK);
        accessList.add(presentedPermission);
        int initialAccessListSize = accessList.size();
        UserPermission expectedPermission = new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK);

        PermissionUtil.addOwnerToAccessList(accessList, testUser, NOTEBOOK);

        assertThat(accessList, hasItem(expectedPermission));
        assertThat(accessList.size(), is(initialAccessListSize + 1));
    }

    @Test
    public void addOwnerToAccessListThatAlreadyContainsUser() {
        Set<UserPermission> accessList = new HashSet<>(3);

        UserPermission presentedPermission =
                new UserPermission(testUser, OWNER_PERMISSIONS, PermissionCreationLevel.PROJECT);

        accessList.add(presentedPermission);
        int initialAccessListSize = accessList.size();

        UserPermission expectedPermission =
                new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK);

        PermissionUtil.addOwnerToAccessList(accessList, testUser, NOTEBOOK);

        //if this presentedPermission was changed it might be changed in other accessLists and it's not correct
        assertThat(presentedPermission.getPermissionCreationLevel(), is(PermissionCreationLevel.PROJECT));

        assertThat(accessList, hasItem(expectedPermission));
        assertThat(accessList.size(), is(initialAccessListSize));
    }

    @Test
    public void addUsersFromNotebookLevel() {
        HashSet<UserPermission> accessList = new HashSet<>();

        UserPermission projectLevelUserPermission = new UserPermission(new User(), OWNER_PERMISSIONS, PROJECT);
        UserPermission notebookLevelUserPermission = new UserPermission(testUser, USER_PERMISSIONS, NOTEBOOK);
        UserPermission experimentLevelUserPermission = new UserPermission(otherTestUser, OWNER_PERMISSIONS, EXPERIMENT);

        HashSet<UserPermission> upperLevelUserPermissions =
                Sets.newHashSet(notebookLevelUserPermission, experimentLevelUserPermission, projectLevelUserPermission);

        PermissionUtil.addUsersFromUpperLevel(accessList, upperLevelUserPermissions, NOTEBOOK);

        assertThat(accessList, containsInAnyOrder(notebookLevelUserPermission, projectLevelUserPermission));
        assertThat(accessList, not(hasItem(experimentLevelUserPermission)));
    }

    @Test
    public void addUsersFromProjectLevel() {
        HashSet<UserPermission> accessList = new HashSet<>();

        UserPermission projectLevelUserPermission = new UserPermission(new User(), OWNER_PERMISSIONS, PROJECT);
        UserPermission notebookLevelUserPermission = new UserPermission(testUser, USER_PERMISSIONS, NOTEBOOK);
        UserPermission experimentLevelUserPermission = new UserPermission(otherTestUser, OWNER_PERMISSIONS, EXPERIMENT);

        HashSet<UserPermission> upperLevelUserPermissions =
                Sets.newHashSet(notebookLevelUserPermission, experimentLevelUserPermission, projectLevelUserPermission);

        PermissionUtil.addUsersFromUpperLevel(accessList, upperLevelUserPermissions, PROJECT);

        assertThat(accessList, containsInAnyOrder(projectLevelUserPermission));
        assertThat(accessList, not(hasItem(experimentLevelUserPermission)));
        assertThat(accessList, not(hasItem(notebookLevelUserPermission)));
    }

    @Test
    public void equalsByUserIdWithSameUser() {
        UserPermission userPermission = new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK);
        UserPermission sameUserPermission = new UserPermission(testUser, VIEWER_PERMISSIONS, EXPERIMENT);
        assertThat(PermissionUtil.equalsByUserId(userPermission, sameUserPermission), is(true));
    }

    @Test
    public void equalsByUserIdWithOtherUser() {
        UserPermission userPermission = new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK);
        UserPermission otherUserPermission = new UserPermission(otherTestUser, VIEWER_PERMISSIONS, EXPERIMENT);
        assertThat(PermissionUtil.equalsByUserId(userPermission, otherUserPermission), is(false));
    }

    @Test
    public void canBeChangedFromThisLevel() {
        assertThat(PermissionUtil.canBeChangedFromThisLevel(EXPERIMENT, EXPERIMENT), is(true));
        assertThat(PermissionUtil.canBeChangedFromThisLevel(EXPERIMENT, NOTEBOOK), is(true));
        assertThat(PermissionUtil.canBeChangedFromThisLevel(EXPERIMENT, PROJECT), is(true));

        assertThat(PermissionUtil.canBeChangedFromThisLevel(NOTEBOOK, NOTEBOOK), is(true));
        assertThat(PermissionUtil.canBeChangedFromThisLevel(NOTEBOOK, PROJECT), is(true));

        assertThat(PermissionUtil.canBeChangedFromThisLevel(PROJECT, PROJECT), is(true));

        assertThat(PermissionUtil.canBeChangedFromThisLevel(PROJECT, NOTEBOOK), is(false));
        assertThat(PermissionUtil.canBeChangedFromThisLevel(PROJECT, EXPERIMENT), is(false));

        assertThat(PermissionUtil.canBeChangedFromThisLevel(NOTEBOOK, EXPERIMENT), is(false));
    }
}