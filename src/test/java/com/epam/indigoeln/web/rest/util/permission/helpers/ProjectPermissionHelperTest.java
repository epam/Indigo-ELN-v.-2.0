package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.PROJECT;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectPermissionHelperTest {

    private Project testProject;
    private Notebook testNotebook;
    private Experiment testExperiment;
    @Mock
    private User testUser;
    @Mock
    private User otherTestUser;


    @Before
    public void setUp() {
        testProject = new Project();
        testNotebook = new Notebook();
        testProject.setNotebooks(Collections.singletonList(testNotebook));
        testExperiment = new Experiment();
        testNotebook.setExperiments(Collections.singletonList(testExperiment));
        when(testUser.getId()).thenReturn("testUserId");
        when(otherTestUser.getId()).thenReturn("otherTestUserId");
    }

    @Test
    public void addOwnerProjectPermission() {
        UserPermission addedPermission = new UserPermission(testUser, UserPermission.OWNER_PERMISSIONS, PROJECT);
        ProjectPermissionHelper.addPermissions(testProject, singleton(addedPermission));

        assertThat(testProject.getAccessList(), hasItem(addedPermission));
        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testExperiment.getAccessList(), hasItem(addedPermission));
    }

    @Test
    public void addUserProjectPermission() {
        UserPermission addedPermission = new UserPermission(testUser, UserPermission.USER_PERMISSIONS, PROJECT);
        ProjectPermissionHelper.addPermissions(testProject, singleton(addedPermission));

        UserPermission expectedExperimentPermission =
                new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, PROJECT);

        assertThat(testProject.getAccessList(), hasItem(addedPermission));
        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testExperiment.getAccessList(), hasItem(expectedExperimentPermission));
    }

    @Test
    public void addViewerProjectPermission() {
        UserPermission addedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, PROJECT);
        ProjectPermissionHelper.addPermissions(testProject, singleton(addedPermission));

        assertThat(testProject.getAccessList(), hasItem(addedPermission));
        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testExperiment.getAccessList(), hasItem(addedPermission));
    }

    @Test
    public void changeProjectPermission() {
        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, PROJECT);
        UserPermission presentedPermission2 =
                new UserPermission(otherTestUser, UserPermission.VIEWER_PERMISSIONS, PROJECT);
        ProjectPermissionHelper.addPermissions(testProject, Sets.newHashSet(presentedPermission, presentedPermission2));

        UserPermission changedPermission = new UserPermission(testUser, UserPermission.OWNER_PERMISSIONS, PROJECT);

        ProjectPermissionHelper.updatePermissions(testProject, singleton(changedPermission));

        assertThat(testProject.getAccessList(), containsInAnyOrder(changedPermission, presentedPermission2));
        assertThat(testNotebook.getAccessList(), containsInAnyOrder(changedPermission, presentedPermission2));
        assertThat(testExperiment.getAccessList(), containsInAnyOrder(changedPermission, presentedPermission2));
    }

    @Test
    public void removeProjectPermission() {
        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, PROJECT);
        UserPermission presentedPermission2 =
                new UserPermission(otherTestUser, UserPermission.VIEWER_PERMISSIONS, PROJECT);
        ProjectPermissionHelper.addPermissions(testProject, Sets.newHashSet(presentedPermission, presentedPermission2));

        assertThat(testProject.getAccessList(), hasItem(presentedPermission));
        assertThat(testNotebook.getAccessList(), hasItem(presentedPermission));
        assertThat(testExperiment.getAccessList(), hasItem(presentedPermission));

        ProjectPermissionHelper.removePermission(testProject, singleton(presentedPermission));

        assertThat(testProject.getAccessList(), hasItem(presentedPermission2));
        assertThat(testProject.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testNotebook.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testExperiment.getAccessList(), not(hasItem(presentedPermission)));
    }
}