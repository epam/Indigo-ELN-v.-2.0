'use strict';

angular.module('indigoeln')
    .controller('NavbarController', function ($scope, $location, $state, $uibModal, $rootScope, Principal, Auth) {
        $scope.newNotebook = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/notebook/notebook-select-project.html',
                controller: 'NotebookSelectProjectController',
                resolve: {
                    projects: function (Project) {
                        return Project.query().$promise;
                    }
                },
                size: 'lg'
            });
            modalInstance.result.then(function (projectId) {
                $state.go('notebook', {projectId: projectId});
            }, function () {
            });
        };

        $scope.Principal = Principal;

        $scope.logout = function () {
            Auth.logout();
            $state.go('login');
        };
    });