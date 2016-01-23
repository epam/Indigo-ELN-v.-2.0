'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', function ($scope, projectService, notebookService, experimentService, AlertService) {
        $scope.toggleProjects = function () {
            if (!$scope.projects) {
                projectService.query(function (result) {
                    $scope.projects = result;
                });
            } else {
                $scope.projects = null;
            }
        };

        $scope.toggleNotebooks = function (project) {
            if (!project.notebooks) {
                notebookService.query({projectId: project.node.id}, function (result) {
                    project.notebooks = result;
                })
            } else {
                project.notebooks = null;
            }
        };

        $scope.toggleExperiments = function (notebook) {
            if (!notebook.experiments) {
                experimentService.query({notebookId: notebook.node.id}, function (result) {
                    notebook.experiments = result;
                })
            } else {
                notebook.experiments = null;
            }
        };

        $scope.onExperimentClick = function (experiment) {
            AlertService.info('experiment with id: ' + experiment.node.id + ' clicked');
        }
    });

