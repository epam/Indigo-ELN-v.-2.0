'use strict';

angular
    .module('indigoeln')
    .controller('NavbarController', function ($scope, $location, $state, $uibModal, $rootScope, Principal, Auth) {
        $scope.newExperiment = function () {
            //$rootScope.$broadcast('new-experiment', {});
            //event.preventDefault();
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/experiment/dialog/new-experiment-dialog.html',
                controller: 'NewExperimentDialogController'
            });
            modalInstance.result.then(function (experiment) {
                $rootScope.$broadcast('created-experiment', {experiment: experiment});
                $state.go('newexperiment');
            }, function () {
            });
        };

        $scope.newNotebook = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/notebook/new/dialog/new-notebook-dialog.html',
                controller: 'NewNotebookDialogController',
                size: 'lg'
            });
            modalInstance.result.then(function (notebookName) {
                $rootScope.$broadcast('created-notebook', {notebookName: notebookName});
                $state.go('newnotebook', {notebookName: notebookName, projectId: 'unknown_project'});
            }, function () {
            });
        };

        $scope.newProject = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/project/new-project-dialog.html',
                controller: 'NewProjectDialogController',
                resolve: {
                    users: ['User',
                        function (User) {
                            return User.query();
                        }
                    ]
                }
            });
            modalInstance.result.then(function (project) {
                $rootScope.$broadcast('created-project', {project: project});
            }, function () {
            });
        };

        $scope.Principal = Principal;

        $scope.logout = function () {
            Auth.logout();
            $state.go('login');
        };
    });