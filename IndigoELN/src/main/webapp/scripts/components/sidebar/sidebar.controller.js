'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', function ($scope, $state, User, Project, Notebook, Experiment, AlertService) {
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

        $scope.toggleUsers = function (project) {
            //TODO: get users from server by project.node.id and set to project.users
        };

        $scope.toggleProjects = function (userId) {
            var params = (!!userId) ? {userId: userId} : {};
            if (!$scope.projects) {
                Project.query(params, function (result) {
                    $scope.projects = result;
                });
            } else {
                $scope.projects = null;
            }
        };

        $scope.toggleNotebooks = function (project, userId) {
            var params = (!!userId) ? {projectId: project.node.id, userId: userId} : {projectId: project.node.id};
            $state.go('project', {id: project.node.id});
            if (!project.notebooks && project.hasChildren) {
                Notebook.query(params, function (result) {
                    project.notebooks = result;
                });
            } else {
                project.notebooks = null;
            }
        };

        $scope.toggleNotebooksByUser = function (user) {
            //TODO: get notebooks from server by user.node.id and set to user.notebooks
        };

        $scope.toggleExperiments = function (notebook, userId) {
            var params = (!!userId) ? {notebookId: notebook.node.id, userId: userId} : {notebookId: notebook.node.id};
            $state.go('notebook', {id: notebook.node.id, projectId: notebook.projectId});
            if (!notebook.experiments && notebook.hasChildren) {
                Experiment.query(params, function (result) {
                    notebook.experiments = result;
                });
            } else {
                notebook.experiments = null;
            }
        };

        $scope.onExperimentClick = function (experiment) {
            $state.go('experiment', {id: experiment.node.id});
        };

        $scope.toggleAdministration = function() {
            $scope.adminToggled = !$scope.adminToggled;
        };

        $scope.toggleUsersAndRoles = function() {
            $state.go('user-management');
        };

        $scope.toggleAuthorities = function() {

        };

        $scope.toggleTemplates = function() {
            $state.go('template');
        };

        $scope.toggleDictionaries = function() {

        };
    });

