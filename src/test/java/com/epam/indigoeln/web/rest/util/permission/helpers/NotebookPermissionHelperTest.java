package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.NOTEBOOK;
import static com.epam.indigoeln.core.model.PermissionCreationLevel.PROJECT;
import static com.epam.indigoeln.core.model.UserPermission.OWNER_PERMISSIONS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotebookPermissionHelperTest {

    private Project testProject;
    private Notebook testNotebook;
    private Notebook otherNotebook;
    private Experiment testExperiment;
    @Mock
    private User testUser;
    @Mock
    private User otherTestUser;


    @Before
    public void setUp() {
        testProject = new Project();
        testProject.setId("testProjectId");
        testNotebook = new Notebook();
        testNotebook.setId("testNotebookId");
        otherNotebook = new Notebook();
        otherNotebook.setId("otherNotebookId");
        testProject.setNotebooks(asList(otherNotebook, testNotebook));
        testExperiment = new Experiment();
        testNotebook.setExperiments(Collections.singletonList(testExperiment));
        when(testUser.getId()).thenReturn("testUserId");
        when(otherTestUser.getId()).thenReturn("otherTestUserId");
    }

    @Test
    public void addNotebookOwnerPermission() {

        UserPermission addedPermission = new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK);
        NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(addedPermission));

        UserPermission expectedProjectPermissions = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);

        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testProject.getAccessList(), hasItem(expectedProjectPermissions));
        assertThat(testExperiment.getAccessList(), hasItem(addedPermission));
    }

    @Test
    public void addNotebookUserPermission() {

        UserPermission addedPermission = new UserPermission(testUser, UserPermission.USER_PERMISSIONS, NOTEBOOK);
        NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(addedPermission));

        UserPermission expectedProjectAndExperimentPermissions =
                new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);

        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testProject.getAccessList(), hasItem(expectedProjectAndExperimentPermissions));
        assertThat(testExperiment.getAccessList(), hasItem(expectedProjectAndExperimentPermissions));
    }

    @Test
    public void addNotebookViewerPermission() {

        UserPermission addedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);
        NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(addedPermission));

        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testProject.getAccessList(), hasItem(addedPermission));
        assertThat(testExperiment.getAccessList(), hasItem(addedPermission));
    }

    @Test
    public void addNotebookPermissionForUserThatAlreadyPresentedInProject() {

        UserPermission addedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);

        boolean projectWasUpdated1 =
                NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(addedPermission));
        boolean projectWasUpdated2 =
                NotebookPermissionHelper.addPermissions(testProject, otherNotebook, singleton(addedPermission));

        assertThat(projectWasUpdated1, is(true));
        assertThat(projectWasUpdated2, is(false));
        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testProject.getAccessList(), hasItem(addedPermission));
        assertThat(testExperiment.getAccessList(), hasItem(addedPermission));
    }

    @Test
    public void updateNotebookPermissions() {

        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);
        NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(presentedPermission));

        UserPermission updatedPermission = new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK);

        NotebookPermissionHelper.updatePermissions(testNotebook, singleton(updatedPermission));

        UserPermission expectedProjectPermissions = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);

        assertThat(testNotebook.getAccessList(), hasItem(updatedPermission));
        assertThat(testProject.getAccessList(), hasItem(expectedProjectPermissions));
        assertThat(testExperiment.getAccessList(), hasItem(updatedPermission));
    }

    @Test
    public void removeNotebookPermissionSimpleCase() {

        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);
        NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(presentedPermission));

        NotebookPermissionHelper.removePermissions(testProject, testNotebook, singleton(presentedPermission));

        assertThat(testProject.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testNotebook.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testExperiment.getAccessList(), not(hasItem(presentedPermission)));
    }

    @Test
    public void removeNotebookPermissionTrickyCase() {

        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);
        NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(presentedPermission));
        NotebookPermissionHelper.addPermissions(testProject, otherNotebook, singleton(presentedPermission));

        boolean projectWasUpdated
                = NotebookPermissionHelper.removePermissions(testProject, testNotebook, singleton(presentedPermission));

        assertThat(projectWasUpdated, is(false));
        assertThat(otherNotebook.getAccessList(), hasItem(presentedPermission));
        assertThat(testNotebook.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testProject.getAccessList(), hasItem(presentedPermission));
        assertThat(testExperiment.getAccessList(), not(hasItem(presentedPermission)));
    }

    @Test
    public void removeNotebookPermissionTrickyCaseWithFinalRemoving() {

        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, NOTEBOOK);
        NotebookPermissionHelper.addPermissions(testProject, testNotebook, singleton(presentedPermission));
        NotebookPermissionHelper.addPermissions(testProject, otherNotebook, singleton(presentedPermission));

        boolean projectWasUpdated
                = NotebookPermissionHelper.removePermissions(testProject, testNotebook, singleton(presentedPermission));
        boolean projectWasUpdated2
                = NotebookPermissionHelper.removePermissions(testProject, otherNotebook, singleton(presentedPermission));

        assertThat(projectWasUpdated, is(false));
        assertThat(projectWasUpdated2, is(true));
        assertThat(otherNotebook.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testNotebook.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testProject.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testExperiment.getAccessList(), not(hasItem(presentedPermission)));
    }

    @Test
    public void fillNewNotebookPermissions() {
        UserPermission projectAuthor = new UserPermission(testUser, OWNER_PERMISSIONS, PROJECT, false);
        UserPermission notebookPermission = new UserPermission(testUser, OWNER_PERMISSIONS);

        testProject.getAccessList().add(projectAuthor);
        testNotebook.getAccessList().add(notebookPermission);

        NotebookPermissionHelper.fillNewNotebooksPermissions(testProject, testNotebook, testUser);

        UserPermission expectedPermission = new UserPermission(testUser, OWNER_PERMISSIONS, NOTEBOOK, false);

        assertThat(testNotebook.getAccessList(), hasItem(expectedPermission));
        assertThat(testNotebook.getAccessList().size(), is(1));
    }

    @Test
    public void fillNewNotebookPermissions2() {
        UserPermission projectAuthor = new UserPermission(testUser, OWNER_PERMISSIONS, PROJECT, false);
        UserPermission notebookPermission = new UserPermission(otherTestUser, OWNER_PERMISSIONS);

        testProject.getAccessList().add(projectAuthor);
        testNotebook.getAccessList().add(notebookPermission);

        NotebookPermissionHelper.fillNewNotebooksPermissions(testProject, testNotebook, otherTestUser);

        UserPermission expectedPermission = new UserPermission(otherTestUser, OWNER_PERMISSIONS, NOTEBOOK, false);

        assertThat(testNotebook.getAccessList(), hasItem(expectedPermission));
        assertThat(testNotebook.getAccessList(), hasItem(projectAuthor));
        assertThat(testNotebook.getAccessList().size(), is(2));
    }
}