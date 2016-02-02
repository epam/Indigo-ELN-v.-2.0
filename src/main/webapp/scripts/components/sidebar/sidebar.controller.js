'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', function ($scope, $state, Project, Notebook, Experiment, AlertService) {
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
                Notebook.query({projectId: project.node.id}, function (result) {
                    project.notebooks = result;
                });
            } else {
                project.notebooks = null;
            }
        };

        $scope.toggleExperiments = function (notebook) {
            if (!notebook.experiments) {
                Experiment.query({notebookId: notebook.node.id}, function (result) {
                    notebook.experiments = result;
                });
            } else {
                notebook.experiments = null;
            }
        };

        $scope.onExperimentClick = function (experiment) {
            AlertService.info('experiment with id: ' + experiment.node.id + ' clicked');
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

        $scope.togglePermissions = function() {

        };

        $scope.toggleTemplates = function() {

        };

        $scope.toggleDictionaries = function() {

        };
    });

