package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.epam.indigoeln.core.model.PermissionCreationLevel.EXPERIMENT;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentPermissionHelperTest {

    private Project testProject;
    private Notebook testNotebook;
    private Experiment testExperiment;
    private Experiment otherExperiment;
    @Mock
    private User testUser;
    @Mock
    private User otherTestUser;


    @Before
    public void setUp() {
        testProject = new Project();
        testNotebook = new Notebook();
        testProject.setNotebooks(singletonList(testNotebook));
        testExperiment = new Experiment();
        testExperiment.setId("testExperimentId");
        otherExperiment = new Experiment();
        otherExperiment.setId("otherExperimentId");
        testNotebook.setExperiments(asList(testExperiment, otherExperiment));
        when(testUser.getId()).thenReturn("testUserId");
        when(otherTestUser.getId()).thenReturn("otherTestUserId");
    }

    @Test
    public void addExperimentPermission() {
        UserPermission addedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, EXPERIMENT);

        Pair<Boolean, Boolean> notebookAndProjectWasChanged = ExperimentPermissionHelper
                .addPermissions(testProject, testNotebook, testExperiment, singleton(addedPermission));

        assertThat(notebookAndProjectWasChanged, is(Pair.of(true, true)));
        assertThat(testExperiment.getAccessList(), hasItem(addedPermission));
        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testProject.getAccessList(), hasItem(addedPermission));
        assertThat(otherExperiment.getAccessList(), not(hasItem(addedPermission)));
    }

    @Test
    public void addTwoExperimentsPermissions() {
        UserPermission addedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, EXPERIMENT);
        UserPermission addedToOtherExperimentPermission =
                new UserPermission(testUser, UserPermission.OWNER_PERMISSIONS, EXPERIMENT);

        Pair<Boolean, Boolean> notebookAndProjectWasChanged = ExperimentPermissionHelper
                .addPermissions(testProject, testNotebook, testExperiment, singleton(addedPermission));

        Pair<Boolean, Boolean> notebookAndProjectWasChangedWithSecondExperiment = ExperimentPermissionHelper
                .addPermissions(testProject, testNotebook, otherExperiment, singleton(addedToOtherExperimentPermission));

        assertThat(notebookAndProjectWasChanged, is(Pair.of(true, true)));
        assertThat(notebookAndProjectWasChangedWithSecondExperiment, is(Pair.of(false, false)));
        assertThat(testExperiment.getAccessList(), hasItem(addedPermission));
        assertThat(otherExperiment.getAccessList(), hasItem(addedToOtherExperimentPermission));
        assertThat(testNotebook.getAccessList(), hasItem(addedPermission));
        assertThat(testProject.getAccessList(), hasItem(addedPermission));
        assertThat(testNotebook.getAccessList().size(), is(1));
        assertThat(testProject.getAccessList().size(), is(1));
    }

    @Test
    public void updateExperimentPermissions() {
        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, EXPERIMENT);

        ExperimentPermissionHelper.addPermissions(
                testProject, testNotebook, testExperiment, singleton(presentedPermission));

        UserPermission updatedPermission = new UserPermission(testUser, UserPermission.OWNER_PERMISSIONS, EXPERIMENT);
        ExperimentPermissionHelper.updatePermission(testExperiment, singleton(updatedPermission));

        assertThat(testExperiment.getAccessList(), hasItem(updatedPermission));
        assertThat(testNotebook.getAccessList(), hasItem(presentedPermission));
        assertThat(testProject.getAccessList(), hasItem(presentedPermission));
    }

    @Test
    public void removeExperimentPermissionSimpleCase() {
        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, EXPERIMENT);

        ExperimentPermissionHelper.addPermissions(
                testProject, testNotebook, testExperiment, singleton(presentedPermission));

        Pair<Boolean, Boolean> projectAndNotebookWasUpdated = ExperimentPermissionHelper.removePermissions(
                testProject, testNotebook, testExperiment, singleton(presentedPermission));

        assertThat(projectAndNotebookWasUpdated, is(Pair.of(true, true)));
        assertThat(testExperiment.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testNotebook.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testProject.getAccessList(), not(hasItem(presentedPermission)));

    }

    @Test
    public void removeExperimentPermissionTrickyCase() {
        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, EXPERIMENT);

        ExperimentPermissionHelper.addPermissions(
                testProject, testNotebook, testExperiment, singleton(presentedPermission));
        ExperimentPermissionHelper.addPermissions(
                testProject, testNotebook, otherExperiment, singleton(presentedPermission));

        Pair<Boolean, Boolean> projectAndNotebookWasUpdated = ExperimentPermissionHelper.removePermissions(
                testProject, testNotebook, testExperiment, singleton(presentedPermission));

        assertThat(projectAndNotebookWasUpdated, is(Pair.of(false, false)));
        assertThat(testExperiment.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testNotebook.getAccessList(), hasItem(presentedPermission));
        assertThat(testProject.getAccessList(), hasItem(presentedPermission));
    }

    @Test
    public void removeExperimentPermissionTrickyCaseFinalRemoving() {
        UserPermission presentedPermission = new UserPermission(testUser, UserPermission.VIEWER_PERMISSIONS, EXPERIMENT);

        ExperimentPermissionHelper.addPermissions(
                testProject, testNotebook, testExperiment, singleton(presentedPermission));
        ExperimentPermissionHelper.addPermissions(
                testProject, testNotebook, otherExperiment, singleton(presentedPermission));

        Pair<Boolean, Boolean> projectAndNotebookWasUpdated = ExperimentPermissionHelper.removePermissions(
                testProject, testNotebook, testExperiment, singleton(presentedPermission));
        Pair<Boolean, Boolean> projectAndNotebookWasUpdatedOnSecondRemove =
                ExperimentPermissionHelper.removePermissions(
                        testProject, testNotebook, otherExperiment, singleton(presentedPermission));

        assertThat(projectAndNotebookWasUpdated, is(Pair.of(false, false)));
        assertThat(projectAndNotebookWasUpdatedOnSecondRemove, is(Pair.of(true, true)));
        assertThat(testExperiment.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(otherExperiment.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testNotebook.getAccessList(), not(hasItem(presentedPermission)));
        assertThat(testProject.getAccessList(), not(hasItem(presentedPermission)));
    }
}