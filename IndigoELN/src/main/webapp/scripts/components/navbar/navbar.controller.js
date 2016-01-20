'use strict';

angular
    .module('indigoeln')
    .controller('NavbarController', function ($location, $state, $uibModal, $rootScope) {
        var vm = this;
        vm.newExperiment = newExperiment;
        vm.newNotebook = newNotebook;
        vm.newProject = newProject;
        function newExperiment(event) {
            //$rootScope.$broadcast('new-experiment', {});
            //event.preventDefault();
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/experiment/dialog/new-experiment-dialog.html',
                controller: 'NewExperimentDialogController'
            });
            modalInstance.result.then(function (experiment) {
                $rootScope.$broadcast('created-experiment', {experiment: experiment});
                $state.go('experiment.new');
            }, function () {
            });
        }

        function newNotebook(event) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/notebook/new/dialog/new-notebook-dialog.html',
                controller: 'NewNotebookDialogController',
                controllerAs: 'vm',
                bindToController: true,
                size: 'lg'
            });
            modalInstance.result.then(function (notebookName) {
                $rootScope.$broadcast('created-notebook', {notebookName: notebookName});
                $state.go('notebook.new', {notebookName: notebookName});
            }, function () {
            });
        }

        function newProject(event) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/project/new-project-dialog.html',
                controller: 'NewProjectDialogController'
            });
            modalInstance.result.then(function (project) {
                $rootScope.$broadcast('created-project', {project: project});
            }, function () {
            });
        }
    });