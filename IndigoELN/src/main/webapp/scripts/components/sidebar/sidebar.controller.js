'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', function ($scope, $state, User, Project, Notebook, Experiment, AllProjects, AllNotebooks, AllExperiments) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.USER_EDITOR = 'USER_EDITOR';
        $scope.ROLE_EDITOR = 'ROLE_EDITOR';
        $scope.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
        $scope.ADMINISTRATION_AUTHORITIES = [$scope.USER_EDITOR, $scope.ROLE_EDITOR,
            $scope.TEMPLATE_EDITOR].join(',');
        $scope.myBookmarks = {};
        $scope.allProjects = {};
        $scope.$on('project-created', function(event, data) {
            if ($scope.projects) {
                Project.query(function (result) {
                    $scope.projects = result;
                });
            }
        });

        $scope.$on('notebook-created', function(event, data) {
            var project = {};
            for (var itemId = 0; itemId < $scope.projects.length; itemId++) {
                if ($scope.projects[itemId].node.id === data.projectId) {
                    project = $scope.projects[itemId];
                    break;
                }
            }
            if (project.notebooks) {
                Notebook.query({projectId: project.node.id}, function (result) {
                    project.notebooks = result;
                });
            }
        });

        $scope.$on('experiment-created', function(event, data) {
            var project = {}, notebook = {};
            for (var itemId = 0; itemId < $scope.projects.length; itemId++) {
                if ($scope.projects[itemId].node.id === data.projectId) {
                    project = $scope.projects[itemId];
                    break;
                }
            }
            for (itemId = 0; itemId < project.notebooks.length; itemId++) {
                if (project.notebooks[itemId].node.id === data.notebookId) {
                    notebook = project.notebooks[itemId];
                    break;
                }
            }

            if (notebook.experiments) {
                Experiment.query({notebookId: notebook.node.id}, function (result) {
                    notebook.experiments = result;
                });
            }
        });

        $scope.toggleProjects = function (parent, needAll) {
            if (!parent.projects) {
                var agent = needAll ? AllProjects : Project;
                agent.query({}, function (result) {
                    parent.projects = result;
                });
            } else {
                parent.projects = null;
            }
        };

        $scope.toggleNotebooks = function (project, needAll) {
            $state.go('project', {id: project.node.id});
            if (!project.notebooks) {
                var agent = needAll ? AllNotebooks : Notebook;
                agent.query({projectId: project.node.id}, function (result) {
                    project.notebooks = result;
                });
            } else {
                project.notebooks = null;
            }
        };

        $scope.toggleExperiments = function (notebook, needAll) {
            $state.go('notebook', {id: notebook.node.id, projectId: notebook.projectId});
            if (!notebook.experiments) {
                var agent = needAll ? AllExperiments : Experiment;
                agent.query({notebookId: notebook.node.id}, function (result) {
                    notebook.experiments = result;
                });
            } else {
                notebook.experiments = null;
            }
        };

        $scope.onExperimentClick = function (experiment, notebook) {
            $state.go('experiment.detail', {id: experiment.node.id, notebookId: notebook.node.id});
        };

        $scope.toggleAdministration = function() {
            $scope.adminToggled = !$scope.adminToggled;
        };

        $scope.toggleUsersAndRoles = function() {
            $state.go('user-management');
        };

        $scope.toggleAuthorities = function() {
            $state.go('role-management');
        };

        $scope.toggleTemplates = function() {
            $state.go('template');
        };

        $scope.toggleDictionaries = function() {

        };
    });