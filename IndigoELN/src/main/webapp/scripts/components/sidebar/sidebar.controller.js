'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', function ($scope, $state, Project, Notebook, Experiment, AlertService) {
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

        $scope.toggleProjects = function () {
            if (!$scope.projects) {
                Project.query(function (result) {
                    $scope.projects = result;
                });
            } else {
                $scope.projects = null;
            }
        };

        $scope.toggleNotebooks = function (project) {
            if (!project.notebooks) {
                $state.go('project', {id: project.node.id})
                Notebook.query({projectId: project.node.id}, function (result) {
                    project.notebooks = result;
                });
            } else {
                project.notebooks = null;
            }
        };

        $scope.toggleExperiments = function (notebook) {
            if (!notebook.experiments) {
                $state.go('notebook', {id: notebook.node.id, projectId: notebook.projectId});
                Experiment.query({notebookId: notebook.node.id}, function (result) {
                    notebook.experiments = result;
                });
            } else {
                notebook.experiments = null;
            }
        };

        $scope.onExperimentClick = function (experiment) {
            $state.go('experiment', {id: experiment.node.id})
        };

        $scope.toggleAdministration = function() {
            if (!$scope.adminToggled) {
                $scope.adminToggled = true;
            } else {
                $scope.adminToggled = false;
            }
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

